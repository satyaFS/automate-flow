package com.explore.automateflow.auth.connector.impl;

import com.explore.automateflow.auth.connector.ConnectorExecutor;
import com.explore.automateflow.auth.connector.UserConnection;
import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

/**
 * Telegram Bot connector.
 * Handles sending messages and interactive buttons for approvals.
 */
@Component
public class TelegramConnector implements ConnectorExecutor {

    private static final Logger logger = LoggerFactory.getLogger(TelegramConnector.class);
    private static final String TELEGRAM_API_BASE = "https://api.telegram.org";

    @Value("${telegram.bot-token:}")
    private String defaultBotToken;

    private final WebClient webClient;

    public TelegramConnector() {
        this.webClient = WebClient.builder()
                .baseUrl(TELEGRAM_API_BASE)
                .build();
    }

    @Override
    public String getConnectorId() {
        return "telegram";
    }

    @Override
    public Mono<Map<String, Object>> executeAction(
            String actionId,
            UserConnection connection,
            Map<String, Object> inputs) {

        String botToken = connection != null && connection.getApiKey() != null
                ? connection.getApiKey()
                : defaultBotToken;

        return switch (actionId) {
            case "send_message" -> sendMessage(botToken, inputs);
            case "send_approval_request" -> sendApprovalRequest(botToken, inputs);
            default -> Mono.error(new RuntimeException("Unknown action: " + actionId));
        };
    }

    @Override
    public Mono<Boolean> validateConnection(UserConnection connection) {
        String botToken = connection != null && connection.getApiKey() != null
                ? connection.getApiKey()
                : defaultBotToken;

        return webClient.get()
                .uri("/bot" + botToken + "/getMe")
                .retrieve()
                .bodyToMono(JsonNode.class)
                .map(response -> response.has("ok") && response.get("ok").asBoolean())
                .onErrorReturn(false);
    }

    /**
     * Send a simple text message
     */
    private Mono<Map<String, Object>> sendMessage(String botToken, Map<String, Object> inputs) {
        String chatId = String.valueOf(inputs.get("chatId"));
        String text = (String) inputs.get("text");
        String parseMode = (String) inputs.getOrDefault("parseMode", "Markdown");

        logger.info("Sending Telegram message to chat: {}", chatId);

        Map<String, Object> body = new HashMap<>();
        body.put("chat_id", chatId);
        body.put("text", text);
        body.put("parse_mode", parseMode);

        return webClient.post()
                .uri("/bot" + botToken + "/sendMessage")
                .header("Content-Type", "application/json")
                .bodyValue(body)
                .retrieve()
                .bodyToMono(JsonNode.class)
                .map(response -> {
                    Map<String, Object> result = new HashMap<>();
                    result.put("success", response.has("ok") && response.get("ok").asBoolean());
                    if (response.has("result")) {
                        result.put("messageId", response.get("result").get("message_id").asInt());
                    }
                    return result;
                });
    }

    /**
     * Send a message with Approve/Reject inline buttons
     */
    private Mono<Map<String, Object>> sendApprovalRequest(String botToken, Map<String, Object> inputs) {
        String chatId = String.valueOf(inputs.get("chatId"));
        String text = (String) inputs.get("text");
        String callbackData = (String) inputs.getOrDefault("callbackData", "approval");

        logger.info("Sending approval request to chat: {}", chatId);

        // Create inline keyboard with Approve/Reject buttons
        Map<String, Object> approveButton = Map.of(
                "text", "✅ Approve",
                "callback_data", callbackData + "_approve");
        Map<String, Object> rejectButton = Map.of(
                "text", "❌ Reject",
                "callback_data", callbackData + "_reject");

        Map<String, Object> keyboard = Map.of(
                "inline_keyboard", new Object[][] {
                        { approveButton, rejectButton }
                });

        Map<String, Object> body = new HashMap<>();
        body.put("chat_id", chatId);
        body.put("text", text);
        body.put("parse_mode", "Markdown");
        body.put("reply_markup", keyboard);

        return webClient.post()
                .uri("/bot" + botToken + "/sendMessage")
                .header("Content-Type", "application/json")
                .bodyValue(body)
                .retrieve()
                .bodyToMono(JsonNode.class)
                .map(response -> {
                    Map<String, Object> result = new HashMap<>();
                    result.put("success", response.has("ok") && response.get("ok").asBoolean());
                    if (response.has("result")) {
                        result.put("messageId", response.get("result").get("message_id").asInt());
                    }
                    return result;
                });
    }
}
