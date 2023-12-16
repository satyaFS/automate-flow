package com.explore.automateflow.workflow.service;

import java.util.List;

import com.explore.automateflow.workflow.entity.WorkFlow;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface WorkFlowService {
    Mono<WorkFlow> createWorkFlow(WorkFlow workFlow);

    Mono<WorkFlow> updateWorkFlow(WorkFlow workFlow);

    Mono<WorkFlow> getWorkFlow(String workflowId);

    Mono<Void> deleteWorkFlow(String workflowId);

    Flux<WorkFlow> getAllWorkFlows();

    Flux<WorkFlow> getAllWorkFlowsByUserId(String userId);

    Mono<Void> updateActions(String workflowId, List<String> actionIds);
}
