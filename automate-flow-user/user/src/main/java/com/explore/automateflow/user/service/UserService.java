package com.explore.automateflow.user.service;

import com.explore.automateflow.user.entity.User;

import reactor.core.publisher.Mono;

public interface UserService {
    Mono<User> getUser(String userId);
    Mono<User> updateUser(User user);
    Mono<Void> deleteUser(String userId);
    Mono<User> createUser(User user);
}
