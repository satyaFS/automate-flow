package com.explore.automateflow.trigger.service.impl;

import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.explore.automateflow.trigger.dao.TriggerDAO;
import com.explore.automateflow.trigger.dto.TriggerDTO;
import com.explore.automateflow.trigger.service.TriggerService;
import com.explore.automateflow.trigger.shared.enums.TriggerType;

import reactor.core.publisher.Mono;
@Service
public class TriggerServiceImpl implements TriggerService {
    private TriggerDAO triggerDAO;
    private final WebClient webClient;

    public TriggerServiceImpl(TriggerDAO triggerDAO, WebClient webClient) {
        this.webClient = webClient;
        this.triggerDAO = triggerDAO;
    }

    @Override
    public Mono<TriggerDTO> createTrigger(String id) {
        TriggerDTO triggerDTO = new TriggerDTO(null, id, generateTriggerURL(id),TriggerType.WEBHOOK, null );
        return this.triggerDAO.saveTrigger(triggerDTO.toEntity())
        .map(it->TriggerDTO.of(it));
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

    private String generateTriggerURL(String id) {
        return "http://localhost:8082/workflow/execute/"+id;
    }

    @Override
    public Mono<TriggerDTO> getTriggerByWorkflowId(String workflowId) {
        return triggerDAO.getTriggerByWorkflowId(workflowId)
        .map(it->TriggerDTO.of(it));
    }

}
