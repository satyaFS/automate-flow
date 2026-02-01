package com.explore.automateflow.auth.connector.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.HashSet;
import java.util.Set;

/**
 * Polls Telegram for updates (button clicks).
 * In a production env, we'd use Webhooks, but for local dev polling is easier.
 */
@Component
public class TelegramPollingService {

    private static final Logger logger = LoggerFactory.getLogger(TelegramPollingService.class);
    private static final String TELEGRAM_API_BASE = "https://api.telegram.org";

    @Value("${telegram.bot-token:}")
    private String botToken;

    // In-memory set to dedupe processed updates
    private final Set<Long> processedUpdateIds = new HashSet<>();
    private long lastUpdateId = 0;

    private final WebClient webClient;
    private final ApplicationEventPublisher eventPublisher;

    public TelegramPollingService(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
        this.webClient = WebClient.builder()
                .baseUrl(TELEGRAM_API_BASE)
                .build();
    }

    @PostConstruct
    public void init() {
        if (botToken == null || botToken.isEmpty()) {
            logger.warn("Telegram bot token not configured. Polling disabled.");
            return;
        }
        logger.info("Starting Telegram polling service...");
        // Fetch initial updates to set offset, but don't process old ones
        fetchUpdates(false).subscribe();
    }

    @Scheduled(fixedDelay = 2000) // Poll every 2 seconds
    public void pollUpdates() {
        if (botToken == null || botToken.isEmpty())
            return;
        fetchUpdates(true).subscribe();
    }

    private Mono<Void> fetchUpdates(boolean process) {
        return webClient.get()
                .uri("/bot" + botToken + "/getUpdates?offset=" + (lastUpdateId + 1) + "&timeout=1")
                .retrieve()
                .bodyToMono(JsonNode.class)
                .flatMap(response -> {
                    if (response.has("ok") && response.get("ok").asBoolean()) {
                        JsonNode result = response.get("result");
                        if (result.isArray()) {
                            for (JsonNode update : result) {
                                long updateId = update.get("update_id").asLong();
                                if (updateId > lastUpdateId) {
                                    lastUpdateId = updateId;
                                    if (process) {
                                        processUpdate(update);
                                    }
                                }
                            }
                        }
                    }
                    return Mono.<Void>empty();
                })
                .onErrorResume(e -> {
                    logger.error("Error polling Telegram: " + e.getMessage());
                    return Mono.<Void>empty();
                })
                .then();
    }

    private void processUpdate(JsonNode update) {
        // Check for callback queries (button clicks)
        if (update.has("callback_query")) {
            JsonNode callback = update.get("callback_query");
            String data = callback.get("data").asText();
            String id = callback.get("id").asText();
            JsonNode from = callback.get("from");
            String username = from.has("username") ? from.get("username").asText() : "unknown";

            logger.info("Received Telegram callback: {} from {}", data, username);

            // Publish event for the demo workflow to pick up
            eventPublisher.publishEvent(new TelegramCallbackEvent(this, data, id, username, callback));

            // Answer callback to stop loading animation
            answerCallback(id, "Processing " + data + "...").subscribe();
        }
    }

    private Mono<Void> answerCallback(String callbackId, String text) {
        return webClient.post()
                .uri("/bot" + botToken + "/answerCallbackQuery")
                .header("Content-Type", "application/json")
                .bodyValue("{\"callback_query_id\": \"" + callbackId + "\", \"text\": \"" + text + "\"}")
                .retrieve()
                .bodyToMono(Void.class)
                .onErrorResume(e -> Mono.empty()); // Ignore errors on answer
    }

    // Event class
    public static class TelegramCallbackEvent extends org.springframework.context.ApplicationEvent {
        public final String data;
        public final String callbackId;
        public final String username;
        public final JsonNode rawCallback;

        public TelegramCallbackEvent(Object source, String data, String callbackId, String username,
                JsonNode rawCallback) {
            super(source);
            this.data = data;
            this.callbackId = callbackId;
            this.username = username;
            this.rawCallback = rawCallback;
        }
    }
}
