package com.explore.automateflow.workflow.entity;

import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Document(collection = "workflows")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class WorkFlow {
    @Id
    private String workflowId;
    private String workflowName;
    private String workflowDescription;
    private String userId;
    private String triggerId;
    private List<String> actionIds;
}
