package com.explore.automateflow.workflow.dto;

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
    private String triggerName;
    private String triggerDescription;
}