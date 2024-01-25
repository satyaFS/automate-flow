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
        .GET("/trigger/{triggerId}", triggerHandler::getTrigger )
        .POST("/trigger", triggerHandler::saveTrigger)
        .PUT("/trigger/{triggerId}", triggerHandler::updateTrigger)
        .DELETE("/trigger/{triggerId}", triggerHandler::deleteTrigger)
        .build();
    }
}
