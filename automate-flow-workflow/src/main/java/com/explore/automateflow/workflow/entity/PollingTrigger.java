package com.explore.automateflow.workflow.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PollingTrigger extends Trigger {
    private String pollingUrl;
    private long pollingInterval;
}