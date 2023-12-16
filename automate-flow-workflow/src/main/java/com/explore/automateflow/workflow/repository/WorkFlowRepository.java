package com.explore.automateflow.workflow.repository;

import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import com.explore.automateflow.workflow.entity.Trigger;
import com.explore.automateflow.workflow.entity.WorkFlow;

import ch.qos.logback.core.joran.action.Action;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface WorkFlowRepository extends ReactiveMongoRepository<WorkFlow, String> {

    Flux<WorkFlow> findByUserId(String userId);
}
