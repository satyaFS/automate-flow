package com.explore.automateflow.trigger.service;

import org.springframework.web.reactive.function.server.ServerResponse;

import com.explore.automateflow.trigger.dto.TriggerDTO;
import com.explore.automateflow.trigger.entity.Trigger;

import reactor.core.publisher.Mono;

public interface TriggerService {
    Mono<TriggerDTO> createTrigger(String id);
    
    Mono<TriggerDTO> getTrigger(String triggerId);
    
    Mono<Void> updateTrigger(String triggerId, TriggerDTO triggerDTO);

    Mono<Void> deleteTrigger(String triggerId);

    Mono<TriggerDTO> getTriggerByWorkflowId(String workflowId);
}
