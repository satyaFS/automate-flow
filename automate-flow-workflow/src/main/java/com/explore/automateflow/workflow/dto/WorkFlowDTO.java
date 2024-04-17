package com.explore.automateflow.workflow.dto;

import java.util.List;

import com.explore.automateflow.workflow.entity.WorkFlow;

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

    public static WorkFlowDTO fromEntity(WorkFlow entity) {
        WorkFlowDTO dto = new WorkFlowDTO();
        dto.setWorkflowId(entity.getWorkflowId());
        dto.setWorkflowName(entity.getWorkflowName());
        dto.setWorkflowDescription(entity.getWorkflowDescription());
        dto.setUserId(entity.getUserId());
        return dto;
    }

    public WorkFlow toEntity() {
        WorkFlow entity = new WorkFlow();
        entity.setWorkflowId(this.getWorkflowId());
        entity.setWorkflowName(this.getWorkflowName());
        entity.setWorkflowDescription(this.getWorkflowDescription());
        entity.setUserId(this.getUserId());
        return entity;
    }
}