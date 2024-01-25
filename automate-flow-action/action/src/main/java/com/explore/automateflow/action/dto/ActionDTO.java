package com.explore.automateflow.action.dto;

import com.explore.automateflow.action.entity.Action;

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
    private String name;
    private String description;
    private String url;
    private String method;

    public Action toEntity() {
        return new Action(actionId, name, description, url, method);
    }
}