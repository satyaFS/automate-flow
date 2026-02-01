package com.explore.automateflow.auth.oauth;

import com.explore.automateflow.auth.connector.*;
import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Generic OAuth2 service that works with any connector.
 */
@Service
public class GenericOAuthService {

    private static final Logger logger = LoggerFactory.getLogger(GenericOAuthService.class);

    private final ConnectorRegistry registry;
    private final UserConnectionRepository connectionRepository;
    private final WebClient webClient;

    // Store client configs for each OAuth connector
    private final Map<String, OAuthClientConfig> clientConfigs;

    public GenericOAuthService(
            ConnectorRegistry registry,
            UserConnectionRepository connectionRepository,
            SlackOAuthClientConfig slackConfig,
            GmailOAuthClientConfig gmailConfig) {
        this.registry = registry;
        this.connectionRepository = connectionRepository;
        this.webClient = WebClient.builder().build();

        // Initialize client configs from properties
        this.clientConfigs = new HashMap<>();
        this.clientConfigs.put("slack", new OAuthClientConfig(
                slackConfig.getClientId(),
                slackConfig.getClientSecret(),
                slackConfig.getRedirectUri()));
        this.clientConfigs.put("gmail", new OAuthClientConfig(
                gmailConfig.getClientId(),
                gmailConfig.getClientSecret(),
                gmailConfig.getRedirectUri()));
    }

    /**
     * Generate authorization URL for any OAuth2 connector
     */
    public Mono<String> getAuthorizationUrl(String connectorId, String userId) {
        return registry.getConnector(connectorId)
                .map(connector -> {
                    if (connector.getAuthType() != ConnectorDefinition.AuthType.OAUTH2) {
                        throw new RuntimeException("Connector does not use OAuth2");
                    }

                    OAuthClientConfig clientConfig = clientConfigs.get(connectorId);
                    if (clientConfig == null) {
                        throw new RuntimeException("No client config for connector: " + connectorId);
                    }

                    ConnectorDefinition.OAuthConfig oauth = connector.getOauth();
                    String state = connectorId + ":" + userId + ":" + UUID.randomUUID();

                    String scopes = String.join(" ", oauth.getScopes());

                    StringBuilder url = new StringBuilder();
                    url.append(oauth.getAuthUrl())
                            .append("?client_id=").append(encode(clientConfig.clientId()))
                            .append("&redirect_uri=").append(encode(clientConfig.redirectUri()))
                            .append("&response_type=code")
                            .append("&scope=").append(encode(scopes))
                            .append("&state=").append(encode(state));

                    // Google requires access_type=offline for refresh tokens
                    if (connectorId.equals("gmail")) {
                        url.append("&access_type=offline")
                                .append("&prompt=consent");
                    }

                    return url.toString();
                });
    }

    /**
     * Exchange authorization code for tokens
     */
    public Mono<UserConnection> exchangeCodeForToken(String code, String state) {
        String[] parts = state.split(":");
        if (parts.length < 2) {
            return Mono.error(new RuntimeException("Invalid state"));
        }

        String connectorId = parts[0];
        String userId = parts[1];

        return registry.getConnector(connectorId)
                .flatMap(connector -> {
                    OAuthClientConfig clientConfig = clientConfigs.get(connectorId);
                    if (clientConfig == null) {
                        return Mono.error(new RuntimeException("No client config for: " + connectorId));
                    }

                    ConnectorDefinition.OAuthConfig oauth = connector.getOauth();

                    return webClient.post()
                            .uri(oauth.getTokenUrl())
                            .body(BodyInserters.fromFormData("client_id", clientConfig.clientId())
                                    .with("client_secret", clientConfig.clientSecret())
                                    .with("code", code)
                                    .with("redirect_uri", clientConfig.redirectUri())
                                    .with("grant_type", "authorization_code"))
                            .retrieve()
                            .bodyToMono(JsonNode.class)
                            .flatMap(response -> saveConnection(connectorId, userId, response));
                });
    }

    private Mono<UserConnection> saveConnection(String connectorId, String userId, JsonNode response) {
        logger.info("Saving connection for user {} to connector {}", userId, connectorId);

        // Check for error response
        if (response.has("error")) {
            String error = response.get("error").asText();
            logger.error("OAuth error: {}", error);
            return Mono.error(new RuntimeException("OAuth failed: " + error));
        }

        UserConnection connection = UserConnection.builder()
                .userId(userId)
                .connectorId(connectorId)
                .accessToken(response.has("access_token") ? response.get("access_token").asText() : null)
                .refreshToken(response.has("refresh_token") ? response.get("refresh_token").asText() : null)
                .tokenType(response.has("token_type") ? response.get("token_type").asText() : "Bearer")
                .status(UserConnection.ConnectionStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        // Calculate expiry if provided
        if (response.has("expires_in")) {
            int expiresIn = response.get("expires_in").asInt();
            connection.setExpiresAt(LocalDateTime.now().plusSeconds(expiresIn));
        }

        // Extract metadata
        if (response.has("team")) {
            JsonNode team = response.get("team");
            connection.setMetadata(Map.of(
                    "teamId", team.has("id") ? team.get("id").asText() : "",
                    "teamName", team.has("name") ? team.get("name").asText() : ""));
        }

        // Upsert - update if exists, create if not
        return connectionRepository.findByUserIdAndConnectorId(userId, connectorId)
                .flatMap(existing -> {
                    existing.setAccessToken(connection.getAccessToken());
                    existing.setRefreshToken(connection.getRefreshToken());
                    existing.setExpiresAt(connection.getExpiresAt());
                    existing.setMetadata(connection.getMetadata());
                    existing.setStatus(UserConnection.ConnectionStatus.ACTIVE);
                    existing.setUpdatedAt(LocalDateTime.now());
                    return connectionRepository.save(existing);
                })
                .switchIfEmpty(connectionRepository.save(connection));
    }

    /**
     * Store API key for non-OAuth connectors
     */
    public Mono<UserConnection> saveApiKey(String connectorId, String userId, String apiKey) {
        UserConnection connection = UserConnection.builder()
                .userId(userId)
                .connectorId(connectorId)
                .apiKey(apiKey)
                .status(UserConnection.ConnectionStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        return connectionRepository.findByUserIdAndConnectorId(userId, connectorId)
                .flatMap(existing -> {
                    existing.setApiKey(apiKey);
                    existing.setStatus(UserConnection.ConnectionStatus.ACTIVE);
                    existing.setUpdatedAt(LocalDateTime.now());
                    return connectionRepository.save(existing);
                })
                .switchIfEmpty(connectionRepository.save(connection));
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    /**
     * Client config record
     */
    public record OAuthClientConfig(String clientId, String clientSecret, String redirectUri) {
    }
}
