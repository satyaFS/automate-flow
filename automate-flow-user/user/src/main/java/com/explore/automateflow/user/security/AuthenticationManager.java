package com.explore.automateflow.user.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.explore.automateflow.user.repository.UserRepository;
import com.explore.automateflow.user.service.UserService;

import java.util.ArrayList;

@Service
public class AuthenticationManager implements UserDetailsService {

    // Assuming you have a UserRepository to fetch user details
    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Fetch user from your database or other data source
        var user = userRepository.findByUserName(username).
        orElseThrow(()-> new UsernameNotFoundException("User not found with username: " + username));
        return new org.springframework.security.core.userdetails.User(
                user.getUserName(),
                user.getPassword(),
                new ArrayList<>()
        );
    }
}