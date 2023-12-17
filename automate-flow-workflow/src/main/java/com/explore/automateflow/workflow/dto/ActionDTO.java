package com.explore.automateflow.workflow.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ActionDTO {
    private String actionId;
    private String actionName;
    private String actionDescription;
    private String url;
    private String method;
}