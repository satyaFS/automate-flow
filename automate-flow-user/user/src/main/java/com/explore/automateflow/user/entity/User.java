package com.explore.automateflow.user.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Document(collection = "users")
public class User {
    @Id
    private String userId;
    private String email;
    private String firstName;
    private String lastName;
    private String userName;
    private String password;
}
