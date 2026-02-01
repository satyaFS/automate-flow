package com.explore.automateflow.auth.connector.impl;

import com.explore.automateflow.auth.connector.ConnectorExecutor;
import com.explore.automateflow.auth.connector.UserConnection;
import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Gmail connector using Google API.
 * Handles reading and sending emails via Gmail REST API.
 */
@Component
public class GmailConnector implements ConnectorExecutor {

    private static final Logger logger = LoggerFactory.getLogger(GmailConnector.class);
    private static final String GMAIL_API_BASE = "https://gmail.googleapis.com/gmail/v1";

    private final WebClient webClient;

    public GmailConnector() {
        this.webClient = WebClient.builder()
                .baseUrl(GMAIL_API_BASE)
                .build();
    }

    @Override
    public String getConnectorId() {
        return "gmail";
    }

    @Override
    public Mono<Map<String, Object>> executeAction(
            String actionId,
            UserConnection connection,
            Map<String, Object> inputs) {

        if (connection == null || connection.getAccessToken() == null) {
            return Mono.error(new RuntimeException("Gmail requires OAuth connection. Please connect first."));
        }

        return switch (actionId) {
            case "list_emails" -> listEmails(connection, inputs);
            case "get_email" -> getEmail(connection, inputs);
            case "send_email" -> sendEmail(connection, inputs);
            case "get_latest_unread" -> getLatestUnread(connection);
            default -> Mono.error(new RuntimeException("Unknown action: " + actionId));
        };
    }

    @Override
    public Mono<Boolean> validateConnection(UserConnection connection) {
        if (connection == null || connection.getAccessToken() == null) {
            return Mono.just(false);
        }

        return webClient.get()
                .uri("/users/me/profile")
                .header("Authorization", "Bearer " + connection.getAccessToken())
                .retrieve()
                .bodyToMono(JsonNode.class)
                .map(response -> response.has("emailAddress"))
                .onErrorReturn(false);
    }

    /**
     * List recent emails
     */
    private Mono<Map<String, Object>> listEmails(UserConnection connection, Map<String, Object> inputs) {
        int maxResults = (int) inputs.getOrDefault("maxResults", 10);
        String query = (String) inputs.getOrDefault("query", "");

        logger.info("Listing {} emails with query: {}", maxResults, query);

        String uri = "/users/me/messages?maxResults=" + maxResults;
        if (!query.isEmpty()) {
            uri += "&q=" + query;
        }

        return webClient.get()
                .uri(uri)
                .header("Authorization", "Bearer " + connection.getAccessToken())
                .retrieve()
                .bodyToMono(JsonNode.class)
                .map(response -> {
                    Map<String, Object> result = new HashMap<>();
                    List<String> messageIds = new ArrayList<>();

                    if (response.has("messages")) {
                        for (JsonNode msg : response.get("messages")) {
                            messageIds.add(msg.get("id").asText());
                        }
                    }

                    result.put("messageIds", messageIds);
                    result.put("resultSizeEstimate",
                            response.has("resultSizeEstimate") ? response.get("resultSizeEstimate").asInt() : 0);
                    return result;
                });
    }

    /**
     * Get a single email by ID
     */
    private Mono<Map<String, Object>> getEmail(UserConnection connection, Map<String, Object> inputs) {
        String messageId = (String) inputs.get("messageId");

        if (messageId == null) {
            return Mono.error(new RuntimeException("messageId is required"));
        }

        logger.info("Getting email: {}", messageId);

        return webClient.get()
                .uri("/users/me/messages/" + messageId + "?format=full")
                .header("Authorization", "Bearer " + connection.getAccessToken())
                .retrieve()
                .bodyToMono(JsonNode.class)
                .map(this::parseEmail);
    }

    /**
     * Get latest unread email (for trigger)
     */
    private Mono<Map<String, Object>> getLatestUnread(UserConnection connection) {
        return webClient.get()
                .uri("/users/me/messages?maxResults=1&q=is:unread")
                .header("Authorization", "Bearer " + connection.getAccessToken())
                .retrieve()
                .bodyToMono(JsonNode.class)
                .flatMap(response -> {
                    if (response.has("messages") && response.get("messages").size() > 0) {
                        String messageId = response.get("messages").get(0).get("id").asText();
                        return getEmail(connection, Map.of("messageId", messageId));
                    }
                    return Mono.just(Map.of("hasUnread", false));
                });
    }

    /**
     * Send an email
     */
    private Mono<Map<String, Object>> sendEmail(UserConnection connection, Map<String, Object> inputs) {
        String to = (String) inputs.get("to");
        String subject = (String) inputs.get("subject");
        String body = (String) inputs.get("body");

        if (to == null || subject == null || body == null) {
            return Mono.error(new RuntimeException("to, subject, and body are required"));
        }

        logger.info("Sending email to: {}", to);

        // Build RFC 2822 formatted email
        String email = "To: " + to + "\r\n" +
                "Subject: " + subject + "\r\n" +
                "Content-Type: text/plain; charset=utf-8\r\n\r\n" +
                body;

        // Base64 URL-safe encode
        String encodedEmail = java.util.Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(email.getBytes());

        Map<String, String> requestBody = Map.of("raw", encodedEmail);

        return webClient.post()
                .uri("/users/me/messages/send")
                .header("Authorization", "Bearer " + connection.getAccessToken())
                .header("Content-Type", "application/json")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(JsonNode.class)
                .map(response -> {
                    Map<String, Object> result = new HashMap<>();
                    result.put("success", true);
                    result.put("messageId", response.has("id") ? response.get("id").asText() : null);
                    result.put("threadId", response.has("threadId") ? response.get("threadId").asText() : null);
                    return result;
                });
    }

    /**
     * Parse email JSON into a friendly map
     */
    private Map<String, Object> parseEmail(JsonNode emailJson) {
        Map<String, Object> result = new HashMap<>();
        result.put("id", emailJson.get("id").asText());
        result.put("threadId", emailJson.get("threadId").asText());
        result.put("hasUnread", true);

        // Extract headers
        if (emailJson.has("payload") && emailJson.get("payload").has("headers")) {
            for (JsonNode header : emailJson.get("payload").get("headers")) {
                String name = header.get("name").asText().toLowerCase();
                String value = header.get("value").asText();

                switch (name) {
                    case "from" -> result.put("from", value);
                    case "to" -> result.put("to", value);
                    case "subject" -> result.put("subject", value);
                    case "date" -> result.put("date", value);
                }
            }
        }

        // Extract body (simplified - just get text/plain part)
        if (emailJson.has("payload")) {
            JsonNode payload = emailJson.get("payload");
            String body = extractBody(payload);
            result.put("body", body);
        }

        return result;
    }

    /**
     * Extract body from email payload (handles multipart)
     */
    private String extractBody(JsonNode payload) {
        // Direct body
        if (payload.has("body") && payload.get("body").has("data")) {
            return decodeBase64(payload.get("body").get("data").asText());
        }

        // Multipart
        if (payload.has("parts")) {
            for (JsonNode part : payload.get("parts")) {
                String mimeType = part.has("mimeType") ? part.get("mimeType").asText() : "";
                if (mimeType.equals("text/plain") && part.has("body") && part.get("body").has("data")) {
                    return decodeBase64(part.get("body").get("data").asText());
                }
            }
            // Fallback to first part
            for (JsonNode part : payload.get("parts")) {
                if (part.has("body") && part.get("body").has("data")) {
                    return decodeBase64(part.get("body").get("data").asText());
                }
            }
        }

        return "";
    }

    private String decodeBase64(String data) {
        try {
            return new String(java.util.Base64.getUrlDecoder().decode(data));
        } catch (Exception e) {
            return data;
        }
    }
}
