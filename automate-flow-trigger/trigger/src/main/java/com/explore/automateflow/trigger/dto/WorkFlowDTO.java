package com.explore.automateflow.trigger.dto;

import java.util.List;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class WorkFlowDTO {
    private String workflowId;
    private String workflowName;
    private String workflowDescription;
    private String userId;
}
