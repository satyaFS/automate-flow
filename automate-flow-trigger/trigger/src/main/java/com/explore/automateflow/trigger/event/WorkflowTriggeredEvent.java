package com.explore.automateflow.trigger.event;

import java.time.LocalDateTime;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WorkflowTriggeredEvent {
    private String workflowId;
    private String triggerId;
    private Map<String, Object> payload;
    private LocalDateTime triggeredAt;

    public static WorkflowTriggeredEvent of(String workflowId, String triggerId, Map<String, Object> payload) {
        return new WorkflowTriggeredEvent(workflowId, triggerId, payload, LocalDateTime.now());
    }
}
