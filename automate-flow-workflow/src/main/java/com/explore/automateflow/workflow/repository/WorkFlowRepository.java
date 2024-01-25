package com.explore.automateflow.workflow.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import com.explore.automateflow.workflow.entity.WorkFlow;

import reactor.core.publisher.Flux;

public interface WorkFlowRepository extends ReactiveMongoRepository<WorkFlow, String> {

    Flux<WorkFlow> findByUserId(String userId);
}
