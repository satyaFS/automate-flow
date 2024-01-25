package com.explore.automateflow.workflow.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WorkFlowConfiguration {
    @Bean
    public WebClient getWebClient() {
        return WebClient.create();
    }
}
