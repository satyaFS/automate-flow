package com.explore.automateflow.action.handler;


import java.util.List;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import com.explore.automateflow.action.dto.ActionDTO;
import com.explore.automateflow.action.entity.Action;
import com.explore.automateflow.action.service.ActionService;

import reactor.core.publisher.Mono;
@Component
public class ActionHandler {
    private ActionService actionService;
    private Integer counter=0;

    public ActionHandler(ActionService actionService) {
        this.actionService = actionService;
    }

    public Mono<ServerResponse> getActionById(ServerRequest request) {
        String id = request.pathVariable("id");
        return actionService.getActionById(id)
            .flatMap(action-> ServerResponse.ok().bodyValue(action));
    }

    public Mono<ServerResponse> saveAction(ServerRequest request) {
        return request.bodyToMono(Action.class)
            .flatMap(action-> actionService.saveAction(action))
            .flatMap(action-> ServerResponse.ok().bodyValue(action));
    }

    public Mono<ServerResponse> saveActions(ServerRequest request) {
        return request.bodyToMono(new ParameterizedTypeReference<List<ActionDTO>>() {})
        .flatMap(actionDTOs->actionService.saveActions(actionDTOs))
        .flatMap(ids->ServerResponse.ok().bodyValue(ids));
    }

    public Mono<ServerResponse> updateAction(ServerRequest request) {
        return request.bodyToMono(Action.class)
            .flatMap(action->{
                return actionService.updateAction( request.pathVariable("id"),action)
                    .flatMap(it->ServerResponse.ok().bodyValue(it));
            });
    }

    public Mono<ServerResponse> deleteAction(ServerRequest request) {
        return actionService.deleteAction(request.pathVariable("id"))
            .flatMap(it->ServerResponse.ok().bodyValue(it));
    }

    public Mono<ServerResponse> sampleAction(ServerRequest request) {
        counter++;
        return ServerResponse.ok().bodyValue(counter);
    }
} 
