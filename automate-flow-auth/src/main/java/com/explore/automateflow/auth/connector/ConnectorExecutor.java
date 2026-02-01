package com.explore.automateflow.auth.connector;

import reactor.core.publisher.Mono;
import java.util.Map;

/**
 * Interface that all connector implementations must follow.
 * This is the "plugin contract" that makes adding new integrations easy.
 */
public interface ConnectorExecutor {

    /**
     * Get the connector ID this executor handles
     */
    String getConnectorId();

    /**
     * Execute an action with the given inputs
     * 
     * @param actionId   The action to execute (e.g., "send_message")
     * @param connection User's connection with credentials
     * @param inputs     Action input parameters
     * @return Action output as a Map
     */
    Mono<Map<String, Object>> executeAction(
            String actionId,
            UserConnection connection,
            Map<String, Object> inputs);

    /**
     * Validate that the connection is still valid
     */
    Mono<Boolean> validateConnection(UserConnection connection);

    /**
     * Refresh expired tokens (for OAuth connectors)
     */
    default Mono<UserConnection> refreshToken(UserConnection connection) {
        return Mono.just(connection);
    }
}
