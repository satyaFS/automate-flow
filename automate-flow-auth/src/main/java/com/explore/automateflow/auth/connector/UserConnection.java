package com.explore.automateflow.auth.connector;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Represents a user's connection to a specific connector.
 * Stores OAuth tokens or API keys for authenticated access.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "user_connections")
public class UserConnection {

    @Id
    private String id;
    private String userId;
    private String connectorId; // References ConnectorDefinition.id

    // OAuth tokens
    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private LocalDateTime expiresAt;

    // API Key (for non-OAuth connectors)
    private String apiKey;

    // Connection metadata (e.g., email, workspace name)
    private Map<String, Object> metadata;

    // Status tracking
    private ConnectionStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String errorMessage;

    public enum ConnectionStatus {
        ACTIVE, EXPIRED, REVOKED, ERROR
    }

    public boolean isExpired() {
        return expiresAt != null && LocalDateTime.now().isAfter(expiresAt);
    }
}
