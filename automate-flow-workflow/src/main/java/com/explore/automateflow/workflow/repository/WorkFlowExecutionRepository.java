package com.explore.automateflow.workflow.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import com.explore.automateflow.workflow.entity.WorkFlowExecution;

import reactor.core.publisher.Flux;

public interface WorkFlowExecutionRepository extends ReactiveMongoRepository<WorkFlowExecution, String> {
    Flux<WorkFlowExecution> findByWorkflowId(String workflowId);
}
