package com.explore.automateflow.auth.controller;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.explore.automateflow.auth.entity.Credential;
import com.explore.automateflow.auth.service.SlackOAuthService;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final SlackOAuthService slackOAuthService;

    public AuthController(SlackOAuthService slackOAuthService) {
        this.slackOAuthService = slackOAuthService;
    }

    /**
     * Initiate Slack OAuth flow - redirects to Slack
     */
    @GetMapping("/slack/authorize")
    public Mono<ResponseEntity<Void>> authorizeSlack(@RequestParam String userId) {
        String authUrl = slackOAuthService.getAuthorizationUrl(userId);
        return Mono.just(ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(authUrl))
                .build());
    }

    /**
     * Handle OAuth callback from Slack
     */
    @GetMapping("/slack/callback")
    public Mono<ResponseEntity<Map<String, Object>>> handleSlackCallback(
            @RequestParam String code,
            @RequestParam String state) {

        return slackOAuthService.exchangeCodeForToken(code, state)
                .map(credential -> {
                    Map<String, Object> response = new HashMap<>();
                    response.put("success", true);
                    response.put("message", "Slack integration connected successfully!");
                    response.put("teamName", credential.getTeamName() != null ? credential.getTeamName() : "Unknown");
                    return ResponseEntity.ok(response);
                })
                .onErrorResume(e -> {
                    Map<String, Object> errorResponse = new HashMap<>();
                    errorResponse.put("success", false);
                    errorResponse.put("error", e.getMessage());
                    return Mono.just(ResponseEntity.badRequest().body(errorResponse));
                });
    }

    /**
     * Get Slack credential for a user
     */
    @GetMapping("/credentials/slack/{userId}")
    public Mono<ResponseEntity<Credential>> getSlackCredential(@PathVariable String userId) {
        return slackOAuthService.getCredential(userId)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    /**
     * Get all credentials for a user
     */
    @GetMapping("/credentials/{userId}")
    public Flux<Credential> getAllCredentials(@PathVariable String userId) {
        return slackOAuthService.getAllCredentials(userId);
    }

    /**
     * Revoke/delete a credential
     */
    @DeleteMapping("/credentials/{credentialId}")
    public Mono<ResponseEntity<Object>> revokeCredential(@PathVariable String credentialId) {
        return slackOAuthService.revokeCredential(credentialId)
                .then(Mono.just(ResponseEntity.ok().body(Map.of("success", true, "message", "Credential revoked"))));
    }
}
