package com.explore.automateflow.auth.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.explore.automateflow.auth.service.SlackMessageService;
import com.fasterxml.jackson.databind.JsonNode;

import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/slack")
public class SlackController {

    private final SlackMessageService slackMessageService;

    public SlackController(SlackMessageService slackMessageService) {
        this.slackMessageService = slackMessageService;
    }

    /**
     * Send a message to Slack
     * Body: { "userId": "...", "channel": "#general", "text": "Hello!" }
     */
    @PostMapping("/send")
    public Mono<ResponseEntity<Map<String, Object>>> sendMessage(@RequestBody Map<String, String> request) {
        String userId = request.get("userId");
        String channel = request.get("channel");
        String text = request.get("text");

        if (userId == null || channel == null || text == null) {
            return Mono.just(ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", "Missing required fields: userId, channel, text")));
        }

        return slackMessageService.sendMessage(userId, channel, text)
                .map(ResponseEntity::ok)
                .onErrorResume(e -> Mono.just(ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "error", e.getMessage()))));
    }

    /**
     * List available channels for a user
     */
    @GetMapping("/channels/{userId}")
    public Mono<ResponseEntity<JsonNode>> listChannels(@PathVariable String userId) {
        return slackMessageService.listChannels(userId)
                .map(ResponseEntity::ok)
                .onErrorResume(e -> Mono.just(ResponseEntity.badRequest().build()));
    }
}
