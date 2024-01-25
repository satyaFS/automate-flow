package com.explore.automateflow.trigger.service.impl;

import org.springframework.stereotype.Service;

import com.explore.automateflow.trigger.dao.TriggerDAO;
import com.explore.automateflow.trigger.dto.TriggerDTO;
import com.explore.automateflow.trigger.service.TriggerService;

import reactor.core.publisher.Mono;
@Service
public class TriggerServiceImpl implements TriggerService {
    private TriggerDAO triggerDAO;

    public TriggerServiceImpl(TriggerDAO triggerDAO) {
        this.triggerDAO = triggerDAO;
    }

    @Override
    public Mono<String> saveTrigger(TriggerDTO triggerDTO) {
        return triggerDAO.saveTrigger(triggerDTO.toEntity())
            .flatMap(trigger-> Mono.just(trigger.getTriggerId()));
    }

    @Override
    public Mono<TriggerDTO> getTrigger(String triggerId) {
       return triggerDAO.getTrigger(triggerId).map(trigger->TriggerDTO.of(trigger));
    }

    @Override
    public Mono<Void> updateTrigger(String triggerId, TriggerDTO triggerDTO) {
        return triggerDAO.updateTrigger(triggerId, triggerDTO.toEntity());
    }

    @Override
    public Mono<Void> deleteTrigger(String triggerId) {
        return triggerDAO.deleteTrigger(triggerId);
    }

}
