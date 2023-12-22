package com.explore.automateflow.workflow.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

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

    public WorkFlowServiceImpl(WorkFlowRepository workFlowRepository, WebClient webClient) {
        this.workFlowRepository = workFlowRepository;
        this.webClient = webClient;
    }

    @Override
    public Mono<Void> createWorkFlow(WorkFlowDTO workFlow) {
        return workFlowRepository.save(workFlow.toEntity()).then();
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

    // @Override
    // public Mono<Void> executeWorkFlow(String workflowId, JsonNode triggerResponse) {
    //     return workFlowRepository.findById(workflowId).flatMapMany(workFlow ->
    //         Flux.fromIterable(workFlow.getActionIds())
    //             .concatMap(actionId -> getActionUrl(actionId)
    //                 .flatMap(url -> webClient.post().uri(url).bodyValue(triggerResponse).retrieve().bodyToMono(String.class))
    //             )
    //     ).then();
    // }
    @Override
    public Mono<Void> executeWorkFlow(String workflowId, JsonNode triggerResponse) {
    return workFlowRepository.findById(workflowId)
        .flatMap(workFlow -> executeActionsSequentially(workFlow.getActionIds(), triggerResponse))
        .then();
    }

    private Mono<JsonNode> executeActionsSequentially(List<String> actionIds, JsonNode previousResponse) {
        if (actionIds.isEmpty()) {
            return Mono.just(previousResponse);
        }
    
        String actionId = actionIds.get(0);
        List<String> remainingActionIds = actionIds.subList(1, actionIds.size());
    
        return getActionUrl(actionId)
            .flatMap(url -> webClient.post().uri(url).bodyValue(previousResponse).retrieve().bodyToMono(JsonNode.class))
            .flatMap(response -> executeActionsSequentially(remainingActionIds, response));
    }

    private Mono<String> getActionUrl(String actionId) {
        return webClient.get().uri("/action/{actionId}", actionId).retrieve().bodyToMono(String.class);
    }
}
