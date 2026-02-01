package com.explore.automateflow.auth.connector;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Central registry for all connector plugins.
 * Manages connector definitions and routes execution to the right executor.
 */
@Service
public class ConnectorRegistry {

    private static final Logger logger = LoggerFactory.getLogger(ConnectorRegistry.class);

    private final ConnectorDefinitionRepository definitionRepository;
    private final Map<String, ConnectorExecutor> executors = new HashMap<>();

    public ConnectorRegistry(
            ConnectorDefinitionRepository definitionRepository,
            List<ConnectorExecutor> executorList) {
        this.definitionRepository = definitionRepository;

        // Register all executor beans
        for (ConnectorExecutor executor : executorList) {
            executors.put(executor.getConnectorId(), executor);
            logger.info("Registered connector executor: {}", executor.getConnectorId());
        }
    }

    @PostConstruct
    public void init() {
        logger.info("ConnectorRegistry initialized with {} executors", executors.size());
    }

    /**
     * Get all available connector definitions
     */
    public Flux<ConnectorDefinition> getAllConnectors() {
        return definitionRepository.findAll();
    }

    /**
     * Get a specific connector definition
     */
    public Mono<ConnectorDefinition> getConnector(String connectorId) {
        return definitionRepository.findById(connectorId);
    }

    /**
     * Get connectors by category
     */
    public Flux<ConnectorDefinition> getConnectorsByCategory(String category) {
        return definitionRepository.findByCategory(category);
    }

    /**
     * Get the executor for a connector
     */
    public ConnectorExecutor getExecutor(String connectorId) {
        return executors.get(connectorId);
    }

    /**
     * Execute an action on a connector
     */
    public Mono<Map<String, Object>> executeAction(
            String connectorId,
            String actionId,
            UserConnection connection,
            Map<String, Object> inputs) {

        ConnectorExecutor executor = executors.get(connectorId);
        if (executor == null) {
            return Mono.error(new RuntimeException("No executor found for connector: " + connectorId));
        }

        return executor.executeAction(actionId, connection, inputs);
    }

    /**
     * Register a new connector definition
     */
    public Mono<ConnectorDefinition> registerConnector(ConnectorDefinition definition) {
        return definitionRepository.save(definition)
                .doOnSuccess(d -> logger.info("Registered connector definition: {}", d.getId()));
    }
}
