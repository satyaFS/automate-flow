package com.explore.automateflow.workflow.config;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import reactor.util.retry.Retry;
import reactor.util.retry.RetryBackoffSpec;

@Configuration
public class RetryConfig {

    @Value("${workflow.retry.max-attempts:3}")
    private int maxAttempts;

    @Value("${workflow.retry.initial-backoff-seconds:1}")
    private int initialBackoffSeconds;

    @Value("${workflow.retry.max-backoff-seconds:10}")
    private int maxBackoffSeconds;

    public RetryBackoffSpec getRetrySpec() {
        return Retry.backoff(maxAttempts, Duration.ofSeconds(initialBackoffSeconds))
                .maxBackoff(Duration.ofSeconds(maxBackoffSeconds));
    }

    public int getMaxAttempts() {
        return maxAttempts;
    }
}
