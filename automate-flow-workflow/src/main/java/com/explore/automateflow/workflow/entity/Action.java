package com.explore.automateflow.workflow.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Document(collection = "actions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Action  {
    @Id
    private String actionId;
    private String actionName;
    private String actionDescription;
    private String url;
    private String method;
}
