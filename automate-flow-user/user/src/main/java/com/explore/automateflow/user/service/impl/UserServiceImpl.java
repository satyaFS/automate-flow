package com.explore.automateflow.user.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.explore.automateflow.user.entity.User;
import com.explore.automateflow.user.repository.UserRepository;
import com.explore.automateflow.user.service.UserService;

import reactor.core.publisher.Mono;





@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserRepository userRepository;
    
    @Override
    public Mono<User> getUser(String userId) {
        return userRepository.findById(userId);
    }
    
    @Override
    public Mono<User> updateUser(User user) {
        return userRepository.save(user);
    }
    
    @Override
    public Mono<Void> deleteUser(String userId) {
        return userRepository.deleteById(userId).then();
    }
    
    @Override
    public Mono<User> createUser(User user) {
        return userRepository.save(user);
    }
}

