package com.explore.automateflow.trigger.handler;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import com.explore.automateflow.trigger.dto.TriggerDTO;
import com.explore.automateflow.trigger.service.TriggerService;

import reactor.core.publisher.Mono;
@Component
public class TriggerHandler {
    private TriggerService triggerService;

    public TriggerHandler(TriggerService triggerService) {
        this.triggerService = triggerService;
    }

    public Mono<ServerResponse> saveTrigger(ServerRequest request) {
        return request.bodyToMono(TriggerDTO.class)
                .flatMap(trigger -> triggerService.saveTrigger(trigger)
                        .flatMap(it -> ServerResponse.ok().bodyValue(it)));
    }

    public Mono<ServerResponse> getTrigger(ServerRequest request) {
        return triggerService.getTrigger(request.pathVariable("triggerId"))
                .flatMap(trigger -> ServerResponse.ok().bodyValue(trigger));
    }

    public Mono<ServerResponse> updateTrigger(ServerRequest request) {
        return request.bodyToMono(TriggerDTO.class)
        .flatMap(trigger->triggerService.updateTrigger(request.pathVariable("triggerId"), trigger))
        .flatMap(it->ServerResponse.ok().bodyValue(it));
    }

    public Mono<ServerResponse> deleteTrigger(ServerRequest request) {
        return triggerService.deleteTrigger(request.pathVariable("triggerId"))
        .flatMap(it->ServerResponse.ok().bodyValue(it));
    } 

   
}