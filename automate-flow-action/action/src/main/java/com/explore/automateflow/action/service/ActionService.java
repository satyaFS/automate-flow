package com.explore.automateflow.action.service;

import java.util.List;

import com.explore.automateflow.action.dto.ActionDTO;
import com.explore.automateflow.action.entity.Action;

import reactor.core.publisher.Mono;

public interface ActionService {
    Mono<Action> getActionById(String id );

    Mono<Action> saveAction(Action action);

    Mono<List<String>> saveActions(List<ActionDTO> actionDTOs);

    Mono<Void> updateAction(String id, Action action);

    Mono<Void> deleteAction(String id);
    
    Mono<String> getActionUrl(String id);
}
