package com.explore.automateflow.auth.oauth;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

/**
 * Generic OAuth controller that works with any connector.
 */
@RestController
@RequestMapping("/oauth")
@CrossOrigin(origins = "*")
public class OAuthController {

    private final GenericOAuthService oauthService;

    public OAuthController(GenericOAuthService oauthService) {
        this.oauthService = oauthService;
    }

    /**
     * Start OAuth flow for any connector
     * GET /oauth/{connectorId}/authorize?userId=xxx
     */
    @GetMapping("/{connectorId}/authorize")
    public Mono<ResponseEntity<Void>> authorize(
            @PathVariable String connectorId,
            @RequestParam String userId) {

        return oauthService.getAuthorizationUrl(connectorId, userId)
                .map(url -> ResponseEntity.status(HttpStatus.FOUND)
                        .location(URI.create(url))
                        .build());
    }

    /**
     * Handle OAuth callback
     * GET /oauth/callback?code=xxx&state=connectorId:userId:uuid
     */
    @GetMapping("/callback")
    public Mono<ResponseEntity<Map<String, Object>>> callback(
            @RequestParam String code,
            @RequestParam String state) {

        return oauthService.exchangeCodeForToken(code, state)
                .map(connection -> {
                    Map<String, Object> response = new HashMap<>();
                    response.put("success", true);
                    response.put("connectorId", connection.getConnectorId());
                    response.put("message", "Successfully connected!");
                    if (connection.getMetadata() != null) {
                        response.putAll(connection.getMetadata());
                    }
                    return ResponseEntity.ok(response);
                })
                .onErrorResume(e -> {
                    Map<String, Object> error = new HashMap<>();
                    error.put("success", false);
                    error.put("error", e.getMessage());
                    return Mono.just(ResponseEntity.badRequest().body(error));
                });
    }

    /**
     * Save API key for non-OAuth connectors
     * POST /oauth/{connectorId}/apikey
     */
    @PostMapping("/{connectorId}/apikey")
    public Mono<ResponseEntity<Map<String, Object>>> saveApiKey(
            @PathVariable String connectorId,
            @RequestParam String userId,
            @RequestBody Map<String, String> body) {

        String apiKey = body.get("apiKey");
        if (apiKey == null || apiKey.isEmpty()) {
            return Mono.just(ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", "API key is required")));
        }

        return oauthService.saveApiKey(connectorId, userId, apiKey)
                .map(connection -> {
                    Map<String, Object> response = new HashMap<>();
                    response.put("success", true);
                    response.put("connectorId", connectorId);
                    response.put("message", "API key saved successfully");
                    return ResponseEntity.ok(response);
                });
    }
}
