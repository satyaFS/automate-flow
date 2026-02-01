package com.explore.automateflow.auth.controller;

import com.explore.automateflow.auth.connector.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

/**
 * REST API for connector management.
 * Designed for UI workflow builder consumption.
 */
@RestController
@RequestMapping("/api/connectors")
@CrossOrigin(origins = "*")
public class ConnectorController {

    private final ConnectorRegistry registry;
    private final UserConnectionRepository connectionRepository;

    public ConnectorController(ConnectorRegistry registry, UserConnectionRepository connectionRepository) {
        this.registry = registry;
        this.connectionRepository = connectionRepository;
    }

    /**
     * List all available connectors
     * UI uses this to show available integrations
     */
    @GetMapping
    public Flux<ConnectorDefinition> listConnectors() {
        return registry.getAllConnectors();
    }

    /**
     * Get connector details including triggers and actions
     */
    @GetMapping("/{connectorId}")
    public Mono<ResponseEntity<ConnectorDefinition>> getConnector(@PathVariable String connectorId) {
        return registry.getConnector(connectorId)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    /**
     * Get connectors by category
     */
    @GetMapping("/category/{category}")
    public Flux<ConnectorDefinition> getByCategory(@PathVariable String category) {
        return registry.getConnectorsByCategory(category);
    }

    /**
     * List user's connections
     */
    @GetMapping("/connections")
    public Flux<UserConnection> getUserConnections(@RequestParam String userId) {
        return connectionRepository.findByUserId(userId);
    }

    /**
     * Get specific connection
     */
    @GetMapping("/connections/{connectorId}")
    public Mono<ResponseEntity<UserConnection>> getConnection(
            @PathVariable String connectorId,
            @RequestParam String userId) {
        return connectionRepository.findByUserIdAndConnectorId(userId, connectorId)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    /**
     * Execute a connector action (for testing or direct use)
     * For API-key based connectors (Gemini, Telegram), can work without stored
     * connection
     */
    @PostMapping("/{connectorId}/actions/{actionId}")
    public Mono<ResponseEntity<Map<String, Object>>> executeAction(
            @PathVariable String connectorId,
            @PathVariable String actionId,
            @RequestParam String userId,
            @RequestBody Map<String, Object> inputs) {

        return connectionRepository.findByUserIdAndConnectorId(userId, connectorId)
                .flatMap(connection -> registry.executeAction(connectorId, actionId, connection, inputs))
                .switchIfEmpty(
                        // For API-key based connectors, allow execution with null connection (uses
                        // default key)
                        registry.executeAction(connectorId, actionId, null, inputs))
                .map(ResponseEntity::ok)
                .onErrorResume(e -> {
                    Map<String, Object> error = new HashMap<>();
                    error.put("success", false);
                    error.put("error", e.getMessage());
                    return Mono.just(ResponseEntity.badRequest().body(error));
                });
    }

    /**
     * Validate a connection is still working
     */
    @PostMapping("/connections/{connectorId}/validate")
    public Mono<ResponseEntity<Map<String, Object>>> validateConnection(
            @PathVariable String connectorId,
            @RequestParam String userId) {

        return connectionRepository.findByUserIdAndConnectorId(userId, connectorId)
                .flatMap(connection -> {
                    ConnectorExecutor executor = registry.getExecutor(connectorId);
                    if (executor == null) {
                        Map<String, Object> errorResult = new HashMap<>();
                        errorResult.put("valid", false);
                        errorResult.put("error", "No executor found");
                        return Mono.just(errorResult);
                    }
                    return executor.validateConnection(connection)
                            .map(valid -> {
                                Map<String, Object> result = new HashMap<>();
                                result.put("valid", valid);
                                return result;
                            });
                })
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    /**
     * Register a new connector definition (admin use)
     */
    @PostMapping
    public Mono<ConnectorDefinition> registerConnector(@RequestBody ConnectorDefinition definition) {
        return registry.registerConnector(definition);
    }
}
