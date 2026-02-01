package com.explore.automateflow.auth.connector.impl;

import com.explore.automateflow.auth.connector.ConnectorExecutor;
import com.explore.automateflow.auth.connector.UserConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

/**
 * Slack connector implementation.
 * Handles all Slack actions like sending messages, listing channels, etc.
 */
@Component
public class SlackConnector implements ConnectorExecutor {

    private static final Logger logger = LoggerFactory.getLogger(SlackConnector.class);
    private static final String SLACK_API_BASE = "https://slack.com/api";

    private final WebClient webClient;

    public SlackConnector() {
        this.webClient = WebClient.builder()
                .baseUrl(SLACK_API_BASE)
                .build();
    }

    @Override
    public String getConnectorId() {
        return "slack";
    }

    @Override
    public Mono<Map<String, Object>> executeAction(
            String actionId,
            UserConnection connection,
            Map<String, Object> inputs) {

        return switch (actionId) {
            case "send_message" -> sendMessage(connection, inputs);
            case "send_channel_message" -> sendChannelMessage(connection, inputs);
            default -> Mono.error(new RuntimeException("Unknown action: " + actionId));
        };
    }

    @Override
    public Mono<Boolean> validateConnection(UserConnection connection) {
        return webClient.get()
                .uri("/auth.test")
                .header("Authorization", "Bearer " + connection.getAccessToken())
                .retrieve()
                .bodyToMono(Map.class)
                .map(response -> Boolean.TRUE.equals(response.get("ok")))
                .onErrorReturn(false);
    }

    private Mono<Map<String, Object>> sendMessage(UserConnection connection, Map<String, Object> inputs) {
        String channel = (String) inputs.get("channel");
        String text = (String) inputs.get("text");

        logger.info("Sending Slack message to channel: {}", channel);

        return webClient.post()
                .uri("/chat.postMessage")
                .header("Authorization", "Bearer " + connection.getAccessToken())
                .header("Content-Type", "application/json")
                .bodyValue(Map.of("channel", channel, "text", text))
                .retrieve()
                .bodyToMono(Map.class)
                .map(response -> {
                    Map<String, Object> result = new HashMap<>();
                    result.put("success", response.get("ok"));
                    result.put("channel", response.get("channel"));
                    result.put("ts", response.get("ts"));
                    if (response.containsKey("error")) {
                        result.put("error", response.get("error"));
                    }
                    return result;
                });
    }

    private Mono<Map<String, Object>> sendChannelMessage(UserConnection connection, Map<String, Object> inputs) {
        // Same as sendMessage but with additional formatting options
        return sendMessage(connection, inputs);
    }
}
