package com.explore.automateflow.auth.oauth;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Gmail OAuth client configuration from properties.
 */
@Configuration
@ConfigurationProperties(prefix = "oauth.gmail")
@Getter
@Setter
public class GmailOAuthClientConfig {
    private String clientId;
    private String clientSecret;
    private String redirectUri;
    private String authUri;
    private String tokenUri;
}
