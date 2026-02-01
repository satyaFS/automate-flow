package com.explore.automateflow.user.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.User.UserBuilder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.explore.automateflow.user.entity.User;
import com.explore.automateflow.user.security.JwtService;
import com.explore.automateflow.user.service.UserService;

import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;
    @Autowired
    private JwtService jwtService;
    @GetMapping("/{userId}")
    public Mono<User> getUser(@PathVariable String userId) {
        return userService.getUser(userId);
    }

    @PutMapping
    public Mono<User> updateUser(@RequestBody User user) {
        return userService.updateUser(user);
    }

    @DeleteMapping("/{userId}")
    public Mono<Void> deleteUser(@PathVariable String userId) {
        return userService.deleteUser(userId);
    }

    @PostMapping
    public Mono<User> createUser(@RequestBody User user) {
        return userService.createUser(user);
    }

    @PostMapping("/login")
    public Mono<String> login(@RequestBody User user) {
       var userdetails =  org.springframework.security.core.userdetails.User.builder()
        .username(user.getUserName())
        .password("")
        .build();
            return userService.verifyCredentials(user)
            .filter(it->it.equals(true))
            .map(it->jwtService.generateToken(userdetails));
    }
}
