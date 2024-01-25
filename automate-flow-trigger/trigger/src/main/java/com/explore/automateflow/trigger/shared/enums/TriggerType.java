package com.explore.automateflow.trigger.shared.enums;

public enum TriggerType {
    WEBHOOK, POLLING;
    @Override
    public String toString() {
        return super.toString().toLowerCase();
    }
}
