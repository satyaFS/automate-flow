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
    private List<String> actionIds;
    private String triggerId;

    public static WorkFlowDTO fromEntity(WorkFlow entity) {
        WorkFlowDTO dto = new WorkFlowDTO();
        dto.setWorkflowId(entity.getWorkflowId());
        dto.setWorkflowName(entity.getWorkflowName());
        dto.setWorkflowDescription(entity.getWorkflowDescription());
        dto.setUserId(entity.getUserId());
        dto.setActionIds(entity.getActionIds());
        dto.setTriggerId(entity.getTriggerId());
        return dto;
    }

    public WorkFlow toEntity() {
        WorkFlow entity = new WorkFlow();
        entity.setWorkflowId(this.getWorkflowId());
        entity.setWorkflowName(this.getWorkflowName());
        entity.setWorkflowDescription(this.getWorkflowDescription());
        entity.setUserId(this.getUserId());
        entity.setActionIds(this.getActionIds());
        entity.setTriggerId(this.getTriggerId());
        return entity;
    }
}