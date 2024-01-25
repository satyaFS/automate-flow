package com.explore.automateflow.workflow.dto;

import com.explore.automateflow.workflow.shared.enums.TriggerType;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TriggerDTO {
    private String triggerId;
    private String url;
    private TriggerType triggerType;
    private Long pollingInterval;
}