package com.explore.automateflow.workflow.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;
import org.springframework.web.reactive.function.client.WebClient;

import com.explore.automateflow.workflow.dto.ActionDTO;
import com.explore.automateflow.workflow.dto.TriggerDTO;
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
    // private final TransactionalOperator transactionalOperator;

    @Value("${trigger.service.url}")
    private String triggerServiceUrl;

    @Value("${action.service.url}")
    private String actionServiceUrl;
    
    private static final Logger logger = LoggerFactory.getLogger(WorkFlowServiceImpl.class);
    public WorkFlowServiceImpl(WorkFlowRepository workFlowRepository, WebClient webClient) {
        this.workFlowRepository = workFlowRepository;
        this.webClient = webClient; 
    }

    @Override
    public Mono<WorkFlowDTO> createWorkFlow(WorkFlowDTO workFlowDTO) {
        // 1. Save Workflow first to get an ID
        return workFlowRepository.save(workFlowDTO.toEntity())
            .flatMap(savedWorkflow -> {
                // 2. Create Actions
                Mono<List<String>> actionIdsMono;
                if (workFlowDTO.getActions() != null && !workFlowDTO.getActions().isEmpty()) {
                    actionIdsMono = webClient.post()
                        .uri(actionServiceUrl + "/actions/bulk")
                        .bodyValue(workFlowDTO.getActions())
                        .retrieve()
                        .bodyToMono(new ParameterizedTypeReference<List<String>>() {});
                } else {
                    actionIdsMono = Mono.just(List.of());
                }

                // 3. Create Trigger
                Mono<TriggerDTO> triggerMono = webClient.post()
                    .uri(triggerServiceUrl + "/triggers")
                    .bodyValue(savedWorkflow.getWorkflowId())
                    .retrieve()
                    .bodyToMono(TriggerDTO.class);

                return Mono.zip(actionIdsMono, triggerMono)
                    .flatMap(tuple -> {
                        List<String> actionIds = tuple.getT1();
                        TriggerDTO trigger = tuple.getT2();

                        // Update Workflow with IDs
                        savedWorkflow.setActionIds(actionIds);
                        savedWorkflow.setTriggerId(trigger.getTriggerId());

                        // If there is trigger config in input, we might want to update the trigger
                        // For now, we return the IDs in the DTO
                        return workFlowRepository.save(savedWorkflow);
                    });
            })
            .map(WorkFlowDTO::fromEntity);
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
        return workFlowRepository.findById(workflowId).flatMap(workFlow -> {
            workFlow.setActionIds(actionIds);
            return workFlowRepository.save(workFlow).then();
        });
    }

    @Override
    public Mono<Void> executeWorkFlow(String workflowId, JsonNode triggerResponse) {
        return workFlowRepository.findById(workflowId)
            .flatMap(workFlow -> {
                if (workFlow.getActionIds() == null || workFlow.getActionIds().isEmpty()) {
                    return Mono.empty();
                }
                return executeActionsSequentially(workFlow.getActionIds(), triggerResponse);
            })
            .then();
    }

    private Mono<JsonNode> executeActionsSequentially(List<String> actionIds, JsonNode previousResponse) {
        if (actionIds.isEmpty()) {
            return Mono.just(previousResponse);
        }
        String actionId = actionIds.get(0);
        logger.info("prevouResponse {}", previousResponse);
        List<String> remainingActionIds = actionIds.subList(1, actionIds.size());
        return getAction(actionId)
                .flatMap(action -> executeAction(action, previousResponse))
                .flatMap(response -> {
                    logger.info("response {}", response);
                    return remainingActionIds.isEmpty() ? Mono.just(response)
                            : executeActionsSequentially(remainingActionIds, response);
                });
    }

    private Mono<String> getActionUrl(String actionId) {
        return webClient.get().uri(actionServiceUrl + "/action/{actionId}", actionId).retrieve().bodyToMono(String.class);
    }

    private Mono<ActionDTO> getAction(String actionId) {
        return webClient.get().uri(actionServiceUrl + "/action/{actionId}", actionId).retrieve().bodyToMono(ActionDTO.class);
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
