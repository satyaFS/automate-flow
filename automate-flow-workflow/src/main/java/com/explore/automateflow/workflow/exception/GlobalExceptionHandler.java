package com.explore.automateflow.workflow.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.server.ServerWebExchange;

import com.explore.automateflow.workflow.dto.ErrorResponse;

import reactor.core.publisher.Mono;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(WebClientResponseException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleWebClientResponseException(
            WebClientResponseException ex, ServerWebExchange exchange) {
        logger.error("WebClient error: {} - {}", ex.getStatusCode(), ex.getMessage());

        ErrorResponse error = ErrorResponse.of(
                ex.getStatusCode().value(),
                ex.getStatusText(),
                ex.getMessage(),
                exchange.getRequest().getPath().value());

        return Mono.just(ResponseEntity.status(ex.getStatusCode()).body(error));
    }

    @ExceptionHandler(java.util.concurrent.TimeoutException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleTimeoutException(
            java.util.concurrent.TimeoutException ex, ServerWebExchange exchange) {
        logger.error("Timeout error: {}", ex.getMessage());

        ErrorResponse error = ErrorResponse.of(
                HttpStatus.GATEWAY_TIMEOUT.value(),
                "Gateway Timeout",
                "Request timed out: " + ex.getMessage(),
                exchange.getRequest().getPath().value());

        return Mono.just(ResponseEntity.status(HttpStatus.GATEWAY_TIMEOUT).body(error));
    }

    @ExceptionHandler(WorkflowExecutionException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleWorkflowExecutionException(
            WorkflowExecutionException ex, ServerWebExchange exchange) {
        logger.error("Workflow execution error: {}", ex.getMessage());

        ErrorResponse error = ErrorResponse.of(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Workflow Execution Failed",
                ex.getMessage(),
                exchange.getRequest().getPath().value());

        return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error));
    }

    @ExceptionHandler(Exception.class)
    public Mono<ResponseEntity<ErrorResponse>> handleGenericException(
            Exception ex, ServerWebExchange exchange) {
        logger.error("Unexpected error", ex);

        ErrorResponse error = ErrorResponse.of(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Internal Server Error",
                ex.getMessage(),
                exchange.getRequest().getPath().value());

        return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error));
    }
}
