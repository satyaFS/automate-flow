package com.explore.automateflow.auth.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Represents a connector definition (e.g., Slack, Gmail)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "integrations")
public class Integration {
    @Id
    private String id;
    private String name;
    private String description;
    private String iconUrl;
    private AuthType authType;
    private String authUrl;
    private String tokenUrl;
    private List<String> scopes;

    public enum AuthType {
        OAUTH2, API_KEY, BASIC
    }
}
