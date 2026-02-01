package com.explore.automateflow.auth.service;

import java.time.LocalDateTime;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import com.explore.automateflow.auth.config.SlackOAuthConfig;
import com.explore.automateflow.auth.entity.Credential;
import com.explore.automateflow.auth.repository.CredentialRepository;
import com.fasterxml.jackson.databind.JsonNode;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class SlackOAuthService {

    private static final Logger logger = LoggerFactory.getLogger(SlackOAuthService.class);

    private final SlackOAuthConfig config;
    private final CredentialRepository credentialRepository;
    private final WebClient webClient;

    public SlackOAuthService(SlackOAuthConfig config, CredentialRepository credentialRepository) {
        this.config = config;
        this.credentialRepository = credentialRepository;
        this.webClient = WebClient.builder().build();
    }

    /**
     * Generate authorization URL for Slack OAuth
     */
    public String getAuthorizationUrl(String userId) {
        // Use userId as state for callback identification
        String state = userId + ":" + UUID.randomUUID().toString();
        return config.getAuthorizationUrl(state);
    }

    /**
     * Exchange authorization code for access token
     */
    public Mono<Credential> exchangeCodeForToken(String code, String state) {
        String userId = state.split(":")[0];

        return webClient.post()
                .uri("https://slack.com/api/oauth.v2.access")
                .body(BodyInserters.fromFormData("client_id", config.getClientId())
                        .with("client_secret", config.getClientSecret())
                        .with("code", code)
                        .with("redirect_uri", config.getRedirectUri()))
                .retrieve()
                .bodyToMono(JsonNode.class)
                .flatMap(response -> {
                    if (response.has("ok") && response.get("ok").asBoolean()) {
                        logger.info("Successfully obtained Slack token for user: {}", userId);
                        return saveCredential(userId, response);
                    } else {
                        String error = response.has("error") ? response.get("error").asText() : "Unknown error";
                        logger.error("Slack OAuth error: {}", error);
                        return Mono.error(new RuntimeException("Slack OAuth failed: " + error));
                    }
                });
    }

    private Mono<Credential> saveCredential(String userId, JsonNode response) {
        Credential credential = new Credential();
        credential.setUserId(userId);
        credential.setIntegrationId("slack");
        credential.setAccessToken(response.get("access_token").asText());
        credential.setTokenType(response.has("token_type") ? response.get("token_type").asText() : "bot");
        credential.setScope(response.has("scope") ? response.get("scope").asText() : "");

        if (response.has("team")) {
            JsonNode team = response.get("team");
            credential.setTeamId(team.has("id") ? team.get("id").asText() : null);
            credential.setTeamName(team.has("name") ? team.get("name").asText() : null);
        }

        credential.setCreatedAt(LocalDateTime.now());
        credential.setUpdatedAt(LocalDateTime.now());

        // Check if credential already exists and update it
        return credentialRepository.findByUserIdAndIntegrationId(userId, "slack")
                .flatMap(existing -> {
                    existing.setAccessToken(credential.getAccessToken());
                    existing.setScope(credential.getScope());
                    existing.setTeamId(credential.getTeamId());
                    existing.setTeamName(credential.getTeamName());
                    existing.setUpdatedAt(LocalDateTime.now());
                    return credentialRepository.save(existing);
                })
                .switchIfEmpty(credentialRepository.save(credential));
    }

    /**
     * Get stored credential for a user
     */
    public Mono<Credential> getCredential(String userId) {
        return credentialRepository.findByUserIdAndIntegrationId(userId, "slack");
    }

    /**
     * Get all credentials for a user
     */
    public Flux<Credential> getAllCredentials(String userId) {
        return credentialRepository.findByUserId(userId);
    }

    /**
     * Revoke a credential
     */
    public Mono<Void> revokeCredential(String credentialId) {
        return credentialRepository.deleteById(credentialId);
    }
}
