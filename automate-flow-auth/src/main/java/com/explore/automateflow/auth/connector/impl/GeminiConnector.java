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
import java.util.List;
import java.util.Map;

/**
 * Google Gemini AI connector.
 * Handles text classification, generation, and analysis.
 */
@Component
public class GeminiConnector implements ConnectorExecutor {

    private static final Logger logger = LoggerFactory.getLogger(GeminiConnector.class);
    private static final String GEMINI_API_BASE = "https://generativelanguage.googleapis.com/v1beta";

    @Value("${gemini.api-key:}")
    private String defaultApiKey;

    private final WebClient webClient;

    public GeminiConnector() {
        this.webClient = WebClient.builder()
                .baseUrl(GEMINI_API_BASE)
                .build();
    }

    @Override
    public String getConnectorId() {
        return "gemini";
    }

    @Override
    public Mono<Map<String, Object>> executeAction(
            String actionId,
            UserConnection connection,
            Map<String, Object> inputs) {

        String apiKey = connection != null && connection.getApiKey() != null
                ? connection.getApiKey()
                : defaultApiKey;

        return switch (actionId) {
            case "classify_text" -> classifyText(apiKey, inputs);
            case "generate_text" -> generateText(apiKey, inputs);
            case "analyze_email" -> analyzeEmail(apiKey, inputs);
            case "draft_response" -> draftResponse(apiKey, inputs);
            default -> Mono.error(new RuntimeException("Unknown action: " + actionId));
        };
    }

    @Override
    public Mono<Boolean> validateConnection(UserConnection connection) {
        // For API key based auth, just check if key exists
        return Mono.just(connection.getApiKey() != null && !connection.getApiKey().isEmpty());
    }

    /**
     * Classify text into categories
     */
    private Mono<Map<String, Object>> classifyText(String apiKey, Map<String, Object> inputs) {
        String text = (String) inputs.get("text");
        String categories = (String) inputs.getOrDefault("categories", "hiring, spam, personal, work, other");

        String prompt = String.format(
                "Classify the following text into one of these categories: %s\n\n" +
                        "Text: %s\n\n" +
                        "Respond with ONLY the category name, nothing else.",
                categories, text);

        return callGemini(apiKey, prompt)
                .map(response -> {
                    Map<String, Object> result = new HashMap<>();
                    result.put("category", response.trim().toLowerCase());
                    result.put("originalText", text);
                    return result;
                });
    }

    /**
     * Generate text based on prompt
     */
    private Mono<Map<String, Object>> generateText(String apiKey, Map<String, Object> inputs) {
        String prompt = (String) inputs.get("prompt");

        return callGemini(apiKey, prompt)
                .map(response -> {
                    Map<String, Object> result = new HashMap<>();
                    result.put("generatedText", response);
                    return result;
                });
    }

    /**
     * Analyze an email to determine if it's a hiring/recruiter email
     */
    private Mono<Map<String, Object>> analyzeEmail(String apiKey, Map<String, Object> inputs) {
        String subject = (String) inputs.getOrDefault("subject", "");
        String body = (String) inputs.getOrDefault("body", "");
        String from = (String) inputs.getOrDefault("from", "");

        String prompt = String.format(
                "Analyze this email and determine if it's a hiring/recruiter email.\n\n" +
                        "From: %s\n" +
                        "Subject: %s\n" +
                        "Body: %s\n\n" +
                        "Respond in JSON format:\n" +
                        "{\n" +
                        "  \"isHiringEmail\": true/false,\n" +
                        "  \"confidence\": 0.0-1.0,\n" +
                        "  \"companyName\": \"extracted company name or null\",\n" +
                        "  \"position\": \"job position if mentioned or null\",\n" +
                        "  \"summary\": \"brief summary of the email\"\n" +
                        "}",
                from, subject, body);

        return callGemini(apiKey, prompt)
                .map(response -> {
                    Map<String, Object> result = new HashMap<>();
                    result.put("analysis", response);
                    result.put("rawResponse", response);
                    // Try to parse as JSON
                    try {
                        // Basic parsing for key fields
                        result.put("isHiringEmail", response.toLowerCase().contains("\"ishiringemail\": true")
                                || response.toLowerCase().contains("\"ishiringemail\":true"));
                    } catch (Exception e) {
                        result.put("isHiringEmail", false);
                    }
                    return result;
                });
    }

    /**
     * Draft a professional email response
     */
    private Mono<Map<String, Object>> draftResponse(String apiKey, Map<String, Object> inputs) {
        String originalEmail = (String) inputs.getOrDefault("originalEmail", "");
        String responseType = (String) inputs.getOrDefault("responseType", "interested");
        String additionalContext = (String) inputs.getOrDefault("context", "");

        String prompt = String.format(
                "Draft a professional email response to the following hiring/recruiter email.\n\n" +
                        "Original Email:\n%s\n\n" +
                        "Response Type: %s\n" +
                        "Additional Context: %s\n\n" +
                        "Write a polite, professional response. Include:\n" +
                        "- Acknowledgment of their email\n" +
                        "- Expression of interest (if applicable)\n" +
                        "- Request for more details or next steps\n" +
                        "- Professional sign-off\n\n" +
                        "Respond with ONLY the email body, no subject line.",
                originalEmail, responseType, additionalContext);

        return callGemini(apiKey, prompt)
                .map(response -> {
                    Map<String, Object> result = new HashMap<>();
                    result.put("draftedResponse", response);
                    result.put("responseType", responseType);
                    return result;
                });
    }

    /**
     * Call Gemini API
     */
    private Mono<String> callGemini(String apiKey, String prompt) {
        logger.info("Calling Gemini API with prompt length: {}", prompt.length());

        Map<String, Object> requestBody = Map.of(
                "contents", List.of(
                        Map.of("parts", List.of(
                                Map.of("text", prompt)))));

        return webClient.post()
                .uri("/models/gemini-2.0-flash:generateContent")
                .header("Content-Type", "application/json")
                .header("X-goog-api-key", apiKey)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(JsonNode.class)
                .map(response -> {
                    try {
                        return response
                                .get("candidates").get(0)
                                .get("content")
                                .get("parts").get(0)
                                .get("text").asText();
                    } catch (Exception e) {
                        logger.error("Error parsing Gemini response: {}", e.getMessage());
                        return "Error: Could not parse response";
                    }
                })
                .doOnError(e -> logger.error("Gemini API error: {}", e.getMessage()));
    }
}
