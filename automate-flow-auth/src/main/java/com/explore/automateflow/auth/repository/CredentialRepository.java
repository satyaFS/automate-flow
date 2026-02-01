package com.explore.automateflow.auth.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

import com.explore.automateflow.auth.entity.Credential;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface CredentialRepository extends ReactiveMongoRepository<Credential, String> {
    Flux<Credential> findByUserId(String userId);

    Mono<Credential> findByUserIdAndIntegrationId(String userId, String integrationId);

    Mono<Credential> findByUserIdAndTeamId(String userId, String teamId);
}
