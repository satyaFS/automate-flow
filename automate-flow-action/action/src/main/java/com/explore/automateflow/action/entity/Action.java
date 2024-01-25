package com.explore.automateflow.action.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "actions")
public class Action {
    @Id
    private String actionId;
    private String name;
    private String description;
    private String url;
    private String method;
}
