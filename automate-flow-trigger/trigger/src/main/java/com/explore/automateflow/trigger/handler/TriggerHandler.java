package com.explore.automateflow.trigger.handler;

import java.util.Map;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import com.explore.automateflow.trigger.dto.TriggerDTO;
import com.explore.automateflow.trigger.service.TriggerService;
import com.explore.automateflow.trigger.service.WorkflowEventProducer;

import reactor.core.publisher.Mono;

@Component
public class TriggerHandler {
    private TriggerService triggerService;
    private WorkflowEventProducer workflowEventProducer;

    public TriggerHandler(TriggerService triggerService, WorkflowEventProducer workflowEventProducer) {
        this.triggerService = triggerService;
        this.workflowEventProducer = workflowEventProducer;
    }

    public Mono<ServerResponse> saveTrigger(ServerRequest request) {
        return request.bodyToMono(String.class)
                .flatMap(id -> triggerService.createTrigger(id)
                        .flatMap(it -> ServerResponse.ok().bodyValue(it)));
    }

    public Mono<ServerResponse> getTrigger(ServerRequest request) {
        return triggerService.getTrigger(request.pathVariable("triggerId"))
                .flatMap(trigger -> ServerResponse.ok().bodyValue(trigger));
    }

    public Mono<ServerResponse> getTriggerByWorkflowId(ServerRequest request) {
        return triggerService.getTriggerByWorkflowId(request.pathVariable("workflowId"))
                .flatMap(trigger -> ServerResponse.ok().bodyValue(trigger));
    }

    public Mono<ServerResponse> updateTrigger(ServerRequest request) {
        return request.bodyToMono(TriggerDTO.class)
                .flatMap(trigger -> triggerService.updateTrigger(request.pathVariable("triggerId"), trigger))
                .flatMap(it -> ServerResponse.ok().bodyValue(it));
    }

    public Mono<ServerResponse> deleteTrigger(ServerRequest request) {
        return triggerService.deleteTrigger(request.pathVariable("triggerId"))
                .flatMap(it -> ServerResponse.ok().bodyValue(it));
    }

    @SuppressWarnings("unchecked")
    public Mono<ServerResponse> fireTrigger(ServerRequest request) {
        String triggerId = request.pathVariable("triggerId");

        return triggerService.getTrigger(triggerId)
                .flatMap(trigger -> request.bodyToMono(Map.class)
                        .defaultIfEmpty(Map.of())
                        .flatMap(payload -> {
                            workflowEventProducer.sendWorkflowTriggeredEvent(
                                    trigger.getWorkflowId(),
                                    triggerId,
                                    (Map<String, Object>) payload);
                            return ServerResponse.accepted().bodyValue(
                                    Map.of("message", "Workflow triggered asynchronously",
                                            "workflowId", trigger.getWorkflowId(),
                                            "triggerId", triggerId));
                        }));
    }
}
