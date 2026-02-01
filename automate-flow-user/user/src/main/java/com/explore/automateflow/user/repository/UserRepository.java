package com.explore.automateflow.user.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

import com.explore.automateflow.user.entity.User;

import reactor.core.publisher.Mono;


@Repository
public interface UserRepository extends ReactiveMongoRepository<User, String> {
    Mono<User> findByUserName(String userName);
}
