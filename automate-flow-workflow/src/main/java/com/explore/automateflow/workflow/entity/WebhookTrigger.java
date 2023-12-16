package com.explore.automateflow.workflow.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class WebhookTrigger extends Trigger {
    private String url;
    private String method;
}
