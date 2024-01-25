package com.explore.automateflow.workflow.shared.enums;

public enum TriggerType {
    WEBHOOK, POLLING;
    @Override
    public String toString() {
        return super.toString().toLowerCase();
    }
}
