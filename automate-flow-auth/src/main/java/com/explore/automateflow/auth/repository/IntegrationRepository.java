package com.explore.automateflow.auth.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

import com.explore.automateflow.auth.entity.Integration;

import reactor.core.publisher.Mono;

@Repository
public interface IntegrationRepository extends ReactiveMongoRepository<Integration, String> {
    Mono<Integration> findByName(String name);
}
