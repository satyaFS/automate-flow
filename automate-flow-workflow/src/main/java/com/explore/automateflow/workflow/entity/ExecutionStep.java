package com.explore.automateflow.workflow.entity;

import java.util.Map;

public class ExecutionStep {
    private String actionId;
    private String actionName;
    private String status; // SUCCESS, FAILURE
    private Map<String, Object> input;
    private Map<String, Object> output;
    private String errorMessage;
    private Long durationMs;
    private int retryCount;

    public ExecutionStep() {
    }

    public ExecutionStep(String actionId, String actionName, String status, Map<String, Object> input,
            Map<String, Object> output,
            String errorMessage, Long durationMs) {
        this.actionId = actionId;
        this.actionName = actionName;
        this.status = status;
        this.input = input;
        this.output = output;
        this.errorMessage = errorMessage;
        this.durationMs = durationMs;
    }

    public String getActionId() {
        return actionId;
    }

    public void setActionId(String actionId) {
        this.actionId = actionId;
    }

    public String getActionName() {
        return actionName;
    }

    public void setActionName(String actionName) {
        this.actionName = actionName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Map<String, Object> getInput() {
        return input;
    }

    public void setInput(Map<String, Object> input) {
        this.input = input;
    }

    public Map<String, Object> getOutput() {
        return output;
    }

    public void setOutput(Map<String, Object> output) {
        this.output = output;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public Long getDurationMs() {
        return durationMs;
    }

    public void setDurationMs(Long durationMs) {
        this.durationMs = durationMs;
    }

    public int getRetryCount() {
        return retryCount;
    }

    public void setRetryCount(int retryCount) {
        this.retryCount = retryCount;
    }
}
