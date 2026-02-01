package com.explore.automateflow.auth.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import com.explore.automateflow.auth.repository.CredentialRepository;
import com.fasterxml.jackson.databind.JsonNode;

import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * Service for sending Slack messages using stored OAuth credentials
 */
@Service
public class SlackMessageService {

    private static final Logger logger = LoggerFactory.getLogger(SlackMessageService.class);
    private static final String SLACK_API_BASE = "https://slack.com/api";

    private final CredentialRepository credentialRepository;
    private final WebClient webClient;

    public SlackMessageService(CredentialRepository credentialRepository) {
        this.credentialRepository = credentialRepository;
        this.webClient = WebClient.builder()
                .baseUrl(SLACK_API_BASE)
                .build();
    }

    /**
     * Send a message to a Slack channel
     */
    public Mono<Map<String, Object>> sendMessage(String userId, String channel, String text) {
        return credentialRepository.findByUserIdAndIntegrationId(userId, "slack")
                .switchIfEmpty(Mono.error(new RuntimeException("No Slack credential found for user: " + userId)))
                .flatMap(credential -> {
                    logger.info("Sending Slack message to channel {} for user {}", channel, userId);

                    return webClient.post()
                            .uri("/chat.postMessage")
                            .header("Authorization", "Bearer " + credential.getAccessToken())
                            .header("Content-Type", "application/json")
                            .body(BodyInserters.fromValue(Map.of(
                                    "channel", channel,
                                    "text", text)))
                            .retrieve()
                            .bodyToMono(JsonNode.class)
                            .map(response -> {
                                boolean ok = response.has("ok") && response.get("ok").asBoolean();
                                if (ok) {
                                    logger.info("Slack message sent successfully");
                                    return Map.of(
                                            "success", true,
                                            "channel", channel,
                                            "ts", response.has("ts") ? response.get("ts").asText() : "");
                                } else {
                                    String error = response.has("error") ? response.get("error").asText()
                                            : "Unknown error";
                                    logger.error("Slack API error: {}", error);
                                    return Map.of(
                                            "success", false,
                                            "error", error);
                                }
                            });
                });
    }

    /**
     * List available Slack channels
     */
    public Mono<JsonNode> listChannels(String userId) {
        return credentialRepository.findByUserIdAndIntegrationId(userId, "slack")
                .switchIfEmpty(Mono.error(new RuntimeException("No Slack credential found for user: " + userId)))
                .flatMap(credential -> webClient.get()
                        .uri("/conversations.list?types=public_channel,private_channel")
                        .header("Authorization", "Bearer " + credential.getAccessToken())
                        .retrieve()
                        .bodyToMono(JsonNode.class));
    }
}
