package com.explore.automateflow.trigger.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.explore.automateflow.trigger.shared.enums.TriggerType;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Document(collection = "triggers")
public class Trigger {
    @Id
    private String triggerId;
    private String url;
    private TriggerType triggerType;
    private Long pollingInterval;
}
