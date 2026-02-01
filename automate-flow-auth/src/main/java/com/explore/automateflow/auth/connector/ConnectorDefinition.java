package com.explore.automateflow.auth.connector;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;
import java.util.Map;

/**
 * Defines a connector (integration) that can be used in workflows.
 * This is the "plugin" definition that makes adding new integrations easy.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "connector_definitions")
public class ConnectorDefinition {

    @Id
    private String id; // e.g., "slack", "outlook", "gemini"
    private String name; // e.g., "Slack", "Microsoft Outlook"
    private String description;
    private String icon; // URL or icon name
    private String category; // e.g., "Communication", "Email", "AI"

    private AuthType authType;
    private OAuthConfig oauth; // If authType == OAUTH2
    private String apiKeyHeader; // If authType == API_KEY

    private List<TriggerDefinition> triggers;
    private List<ActionDefinition> actions;

    public enum AuthType {
        OAUTH2, API_KEY, NONE
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OAuthConfig {
        private String authUrl;
        private String tokenUrl;
        private List<String> scopes;
        private String redirectUri;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TriggerDefinition {
        private String id; // e.g., "new_email"
        private String name; // e.g., "New Email Received"
        private String description;
        private Map<String, FieldSchema> outputSchema; // What data the trigger provides
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ActionDefinition {
        private String id; // e.g., "send_message"
        private String name; // e.g., "Send Message"
        private String description;
        private Map<String, FieldSchema> inputSchema; // What data the action needs
        private Map<String, FieldSchema> outputSchema; // What data the action returns
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FieldSchema {
        private String type; // "string", "number", "boolean", "object", "array"
        private String label; // UI display name
        private String description;
        private boolean required;
        private Object defaultValue;
        private List<String> enumValues; // For dropdown selections
    }
}
