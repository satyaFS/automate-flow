package com.explore.automateflow.trigger.router;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import com.explore.automateflow.trigger.handler.TriggerHandler;

@Configuration
public class TriggerRouter {
    @Autowired
    private TriggerHandler triggerHandler;
    @Bean
    public RouterFunction<ServerResponse> getTriggerPaths() {
        return RouterFunctions.route()
        .GET("/triggers/{triggerId}", triggerHandler::getTrigger )
        .GET("/triggers/workflow/{workflowId}", triggerHandler::getTriggerByWorkflowId )
        .POST("/triggers", triggerHandler::saveTrigger)
        .PUT("/triggers/{triggerId}", triggerHandler::updateTrigger)
        .DELETE("/triggers/{triggerId}", triggerHandler::deleteTrigger)
        .build();
    }
}
