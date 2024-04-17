package com.explore.automateflow.workflow.service;

import java.util.List;

import com.explore.automateflow.workflow.dto.WorkFlowDTO;
import com.fasterxml.jackson.databind.JsonNode;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface WorkFlowService {
    Mono<WorkFlowDTO> createWorkFlow(WorkFlowDTO workFlow);

    Mono<WorkFlowDTO> updateWorkFlow(WorkFlowDTO workFlow);

    Mono<WorkFlowDTO> getWorkFlow(String workflowId);

    Mono<Void> deleteWorkFlow(String workflowId);

    Flux<WorkFlowDTO> getAllWorkFlows();

    Flux<WorkFlowDTO> getAllWorkFlowsByUserId(String userId);

    Mono<Void> updateActions(String workflowId, List<String> actionIds);
    
    // Mono<Void> executeWorkFlow(String workflowId, JsonNode triggerResponse);
}
   