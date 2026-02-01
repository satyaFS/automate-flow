package com.explore.automateflow.action.router;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import com.explore.automateflow.action.handler.ActionHandler;

@Configuration
public class ActionRouter {
    private ActionHandler actionHandler;

    public ActionRouter(ActionHandler actionHandler) {
        this.actionHandler = actionHandler;
    }

    @Bean
    public RouterFunction<ServerResponse> getActionRouter() {
        return RouterFunctions.route()
        .GET("/actions/{id}", this.actionHandler::getActionById)
        .GET("/actions/builtin/sampleAction", this.actionHandler::sampleAction)
        .POST("/actions", this.actionHandler::saveAction)
        .POST("/actions/bulk", this.actionHandler::saveActions)
        .PUT("/actions/{id}", this.actionHandler::updateAction)
        .DELETE("/actions/{id}", this.actionHandler::deleteAction)
        .build();
    }
}
