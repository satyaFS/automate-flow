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
    private TriggerDTO trigger;
    private List<ActionDTO> actions;

    public static WorkFlowDTO fromEntity(WorkFlow entity) {
        WorkFlowDTO dto = new WorkFlowDTO();
        dto.setWorkflowId(entity.getWorkflowId());
        dto.setWorkflowName(entity.getWorkflowName());
        dto.setWorkflowDescription(entity.getWorkflowDescription());
        dto.setUserId(entity.getUserId());
        // Trigger and Actions are not populated here because they reside in other services
        // The service layer is responsible for fetching them if needed, or we just keep the IDs here if we want?
        // For now, let's just allow passing them in.
        return dto;
    }

    public WorkFlow toEntity() {
        WorkFlow entity = new WorkFlow();
        entity.setWorkflowId(this.getWorkflowId());
        entity.setWorkflowName(this.getWorkflowName());
        entity.setWorkflowDescription(this.getWorkflowDescription());
        entity.setUserId(this.getUserId());
        if (this.trigger != null) {
            entity.setTriggerId(this.trigger.getTriggerId());
        }
        if (this.actions != null) {
            entity.setActionIds(this.actions.stream().map(ActionDTO::getActionId).toList());
        }
        return entity;
    }
}