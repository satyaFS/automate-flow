package com.explore.automateflow.workflow.consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.explore.automateflow.workflow.event.WorkflowTriggeredEvent;
import com.explore.automateflow.workflow.service.WorkFlowService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class WorkflowTriggerConsumer {

    private static final Logger logger = LoggerFactory.getLogger(WorkflowTriggerConsumer.class);

    private final WorkFlowService workFlowService;
    private final ObjectMapper objectMapper;

    public WorkflowTriggerConsumer(WorkFlowService workFlowService) {
        this.workFlowService = workFlowService;
        this.objectMapper = new ObjectMapper();
    }

    @KafkaListener(topics = "workflow.trigger", groupId = "workflow-service")
    public void handleWorkflowTriggered(WorkflowTriggeredEvent event) {
        logger.info("Received workflow trigger event: workflowId={}, triggerId={}",
                event.getWorkflowId(), event.getTriggerId());

        try {
            // Convert payload Map to JsonNode for executeWorkFlow
            JsonNode payload = objectMapper.valueToTree(event.getPayload());

            // Execute workflow asynchronously
            workFlowService.executeWorkFlow(event.getWorkflowId(), payload)
                    .subscribe(
                            result -> logger.info("Workflow {} executed successfully", event.getWorkflowId()),
                            error -> logger.error("Workflow {} execution failed: {}",
                                    event.getWorkflowId(), error.getMessage()));
        } catch (Exception e) {
            logger.error("Error processing workflow trigger event: {}", e.getMessage(), e);
        }
    }
}
