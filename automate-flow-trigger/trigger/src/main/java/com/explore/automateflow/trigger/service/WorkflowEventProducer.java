package com.explore.automateflow.trigger.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import com.explore.automateflow.trigger.event.WorkflowTriggeredEvent;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
public class WorkflowEventProducer {

    private static final Logger logger = LoggerFactory.getLogger(WorkflowEventProducer.class);
    private static final String TOPIC = "workflow.trigger";

    private final KafkaTemplate<String, WorkflowTriggeredEvent> kafkaTemplate;

    public WorkflowEventProducer(KafkaTemplate<String, WorkflowTriggeredEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public CompletableFuture<SendResult<String, WorkflowTriggeredEvent>> sendWorkflowTriggeredEvent(
            String workflowId, String triggerId, Map<String, Object> payload) {

        WorkflowTriggeredEvent event = WorkflowTriggeredEvent.of(workflowId, triggerId, payload);

        logger.info("Publishing workflow trigger event: workflowId={}, triggerId={}", workflowId, triggerId);

        return kafkaTemplate.send(TOPIC, workflowId, event)
                .whenComplete((result, ex) -> {
                    if (ex == null) {
                        logger.info("Event sent successfully: offset={}, partition={}",
                                result.getRecordMetadata().offset(),
                                result.getRecordMetadata().partition());
                    } else {
                        logger.error("Failed to send event: {}", ex.getMessage());
                    }
                });
    }
}
