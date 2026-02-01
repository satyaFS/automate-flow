package com.explore.automateflow.auth.oauth;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Slack OAuth client configuration from properties.
 */
@Configuration
@ConfigurationProperties(prefix = "oauth.slack")
@Getter
@Setter
public class SlackOAuthClientConfig {
    private String clientId;
    private String clientSecret;
    private String redirectUri;
    private String scopes;
}
