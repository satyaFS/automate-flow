package com.explore.automateflow.auth.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Stores OAuth2 tokens for a user's integration
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "credentials")
public class Credential {
    @Id
    private String id;
    private String userId;
    private String integrationId;
    private String teamId; // For Slack: workspace ID
    private String teamName; // For Slack: workspace name
    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private String scope;
    private LocalDateTime expiresAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
