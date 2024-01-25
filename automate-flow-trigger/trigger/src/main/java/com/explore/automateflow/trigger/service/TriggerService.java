package com.explore.automateflow.trigger.service;

import com.explore.automateflow.trigger.dto.TriggerDTO;

import reactor.core.publisher.Mono;

public interface TriggerService {
    Mono<String> saveTrigger(TriggerDTO triggerDTO);
    
    Mono<TriggerDTO> getTrigger(String triggerId);
    
    Mono<Void> updateTrigger(String triggerId, TriggerDTO triggerDTO);

    Mono<Void> deleteTrigger(String triggerId);
}
