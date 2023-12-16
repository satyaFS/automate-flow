package com.explore.automateflow.workflow.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Document(collection = "triggers")
public abstract class Trigger {
    @Id
    private String triggerId;
    private String triggerName;
}
