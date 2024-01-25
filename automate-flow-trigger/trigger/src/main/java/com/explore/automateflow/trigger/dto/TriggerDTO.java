package com.explore.automateflow.trigger.dto;

import org.springframework.data.annotation.Id;

import com.explore.automateflow.trigger.entity.Trigger;
import com.explore.automateflow.trigger.shared.enums.TriggerType;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class TriggerDTO {
    private String triggerId;
    private String url;
    private TriggerType triggerType;
    private Long pollingInterval;

    public Trigger toEntity() {
        return new Trigger(triggerId, url, triggerType, pollingInterval);
    }

    public static TriggerDTO of(Trigger trigger) {
        return new TriggerDTO(trigger.getTriggerId(), trigger.getUrl(), trigger.getTriggerType(),
                trigger.getPollingInterval());
    }
}


