package com.explore.automateflow.auth.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import lombok.Getter;

@Configuration
@Getter
public class SlackOAuthConfig {

    @Value("${oauth.slack.client-id}")
    private String clientId;

    @Value("${oauth.slack.client-secret}")
    private String clientSecret;

    @Value("${oauth.slack.redirect-uri}")
    private String redirectUri;

    @Value("${oauth.slack.scopes}")
    private String scopes;

    public String getAuthorizationUrl(String state) {
        return String.format(
                "https://slack.com/oauth/v2/authorize?client_id=%s&scope=%s&redirect_uri=%s&state=%s",
                clientId, scopes, redirectUri, state);
    }
}
