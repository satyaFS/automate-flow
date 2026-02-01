package com.explore.automateflow.auth.connector;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface UserConnectionRepository extends ReactiveMongoRepository<UserConnection, String> {
    Flux<UserConnection> findByUserId(String userId);

    Mono<UserConnection> findByUserIdAndConnectorId(String userId, String connectorId);

    Flux<UserConnection> findByUserIdAndStatus(String userId, UserConnection.ConnectionStatus status);
}
