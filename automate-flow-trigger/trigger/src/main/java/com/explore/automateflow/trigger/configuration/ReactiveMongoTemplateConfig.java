package com.explore.automateflow.trigger.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.web.reactive.function.client.WebClient;

import com.mongodb.reactivestreams.client.MongoClient;

@Configuration
public class ReactiveMongoTemplateConfig {
    @Autowired
    private MongoClient mongoClient;
    
    public ReactiveMongoTemplate getReactiveMonogMongoTemplate() {
        return new ReactiveMongoTemplate(mongoClient, "automateFlowTriggerDB");
    }

    @Bean
    public WebClient getWebClient() {
        return WebClient.create();
    }
}
