package com.explore.automateflow.user.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

import com.explore.automateflow.user.entity.User;

@Repository
public interface UserRepository extends ReactiveMongoRepository<User, String> {
    
}
