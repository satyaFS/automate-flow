package com.explore.automateflow.workflow.event;

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
}
