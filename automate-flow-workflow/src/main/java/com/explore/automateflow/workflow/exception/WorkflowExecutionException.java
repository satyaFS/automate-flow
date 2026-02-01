package com.explore.automateflow.workflow.exception;

public class WorkflowExecutionException extends RuntimeException {

    private final String workflowId;
    private final String actionId;

    public WorkflowExecutionException(String message) {
        super(message);
        this.workflowId = null;
        this.actionId = null;
    }

    public WorkflowExecutionException(String message, Throwable cause) {
        super(message, cause);
        this.workflowId = null;
        this.actionId = null;
    }

    public WorkflowExecutionException(String workflowId, String actionId, String message, Throwable cause) {
        super(message, cause);
        this.workflowId = workflowId;
        this.actionId = actionId;
    }

    public String getWorkflowId() {
        return workflowId;
    }

    public String getActionId() {
        return actionId;
    }
}
