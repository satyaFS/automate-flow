package com.explore.automateflow.auth.connector;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface ConnectorDefinitionRepository extends ReactiveMongoRepository<ConnectorDefinition, String> {
    Flux<ConnectorDefinition> findByCategory(String category);

    Mono<ConnectorDefinition> findByName(String name);
}
