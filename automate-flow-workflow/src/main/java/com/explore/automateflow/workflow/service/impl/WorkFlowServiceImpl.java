package com.explore.automateflow.workflow.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.explore.automateflow.workflow.dto.ActionDTO;
import com.explore.automateflow.workflow.dto.WorkFlowDTO;
import com.explore.automateflow.workflow.entity.WorkFlow;
import com.explore.automateflow.workflow.repository.WorkFlowRepository;
import com.explore.automateflow.workflow.service.WorkFlowService;
import com.fasterxml.jackson.databind.JsonNode;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.util.List;


@Service
public class WorkFlowServiceImpl implements WorkFlowService {
    private final WorkFlowRepository workFlowRepository;
    private final WebClient webClient;
    
    private static final Logger logger = LoggerFactory.getLogger(WorkFlowServiceImpl.class);
    public WorkFlowServiceImpl(WorkFlowRepository workFlowRepository, WebClient webClient) {
        this.workFlowRepository = workFlowRepository;
        this.webClient = webClient; 
    }

    @Override
    public Mono<Void> createWorkFlow(WorkFlowDTO workFlowDTO) {
        Mono<List<String>> actionIds = webClient.post().uri("http://localhost:8084/actions/bulk").bodyValue(workFlowDTO.getActionDTOs())
        .retrieve().bodyToMono(new ParameterizedTypeReference<List<String>>() {});
        Mono<String> triggerId = webClient.post().uri("http://localhost:8083/trigger").bodyValue(workFlowDTO.getTriggerDTO())
        .retrieve().bodyToMono(new ParameterizedTypeReference<String>(){});

        return Mono.zip(actionIds, triggerId).flatMap(data->{
           WorkFlow workFlow = workFlowDTO.toEntity();
           workFlow.setActionIds(data.getT1());
           workFlow.setTriggerId(data.getT2());
           return workFlowRepository.save(workFlow);
        }).then();
    }

    @Override
    public Mono<WorkFlowDTO> updateWorkFlow(WorkFlowDTO workFlow) {
        return workFlowRepository.save(workFlow.toEntity()).flatMap(workFlowEntity -> Mono.just(WorkFlowDTO.fromEntity(workFlowEntity)));
    }

    @Override
    public Mono<WorkFlowDTO> getWorkFlow(String workflowId) {
        return workFlowRepository.findById(workflowId).flatMap(workFlowEntity -> Mono.just(WorkFlowDTO.fromEntity(workFlowEntity)));
    }

    @Override
    public Mono<Void> deleteWorkFlow(String workflowId) {
        return workFlowRepository.deleteById(workflowId);
    }

    @Override
    public Flux<WorkFlowDTO> getAllWorkFlows() {
        return workFlowRepository.findAll().flatMap(workFlowEntity -> Mono.just(WorkFlowDTO.fromEntity(workFlowEntity)));
    }

    @Override
    public Flux<WorkFlowDTO> getAllWorkFlowsByUserId(String userId) {
        return workFlowRepository.findByUserId(userId).flatMap(workFlowEntity -> Mono.just(WorkFlowDTO.fromEntity(workFlowEntity)));
    }

    @Override
    public Mono<Void> updateActions(String workflowId, List<String> actionIds) {
        // Implement the logic to update actions
        return workFlowRepository.findById(workflowId).flatMap(workFlow -> {
            workFlow.setActionIds(actionIds);
            return workFlowRepository.save(workFlow).then();
        });
    }

    @Override
    public Mono<Void> executeWorkFlow(String workflowId, JsonNode triggerResponse) {
    return workFlowRepository.findById(workflowId)
        .flatMap(workFlow -> executeActionsSequentially(workFlow.getActionIds(), triggerResponse))
        .then();
    }

    private Mono<JsonNode> executeActionsSequentially(List<String> actionIds, JsonNode previousResponse) {
        String actionId = actionIds.get(0);
        logger.info("prevouResponse {}", previousResponse);
        List<String> remainingActionIds = actionIds.subList(1, actionIds.size());
        return getAction(actionId)
                .flatMap(action -> executeAction(action, previousResponse))
                .flatMap(response -> {
                    logger.info("response {}", response);
                    return remainingActionIds.isEmpty() ? Mono.just(previousResponse)
                            : executeActionsSequentially(remainingActionIds, response);
                });
    }

    private Mono<String> getActionUrl(String actionId) {
        return webClient.get().uri("/action/{actionId}", actionId).retrieve().bodyToMono(String.class);
    }

    private Mono<ActionDTO> getAction(String actionId) {
        return webClient.get().uri("http://localhost:8084/actions/{actionId}", actionId).retrieve().bodyToMono(ActionDTO.class);
    }
    
    private Mono<JsonNode> executeAction(ActionDTO actionDTO, JsonNode previousResponse) {
        String uri = actionDTO.getUrl();
        Mono<JsonNode> response;
        switch (actionDTO.getMethod()) {
            case "GET":
                response = webClient.get().uri(uri).retrieve().bodyToMono(JsonNode.class);
                break;
            case "POST":
                response = webClient.post().uri(uri).bodyValue(previousResponse).retrieve().bodyToMono(JsonNode.class);
                break;
            case "PUT":
                response = webClient.put().uri(uri).bodyValue(previousResponse).retrieve().bodyToMono(JsonNode.class);
                break;
            case "DELETE":
                response = webClient.delete().uri(uri).retrieve().bodyToMono(JsonNode.class);
                break;
            default:
                response = webClient.post().uri(uri).bodyValue(previousResponse).retrieve().bodyToMono(JsonNode.class);
                break;
        }
        return response;
    }
}
