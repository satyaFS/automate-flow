package com.explore.automateflow.auth.config;

import com.explore.automateflow.auth.connector.ConnectorDefinition;
import com.explore.automateflow.auth.connector.ConnectorDefinition.*;
import com.explore.automateflow.auth.connector.ConnectorDefinitionRepository;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Initializes built-in connector definitions on startup.
 */
@Component
public class ConnectorInitializer {

        private static final Logger logger = LoggerFactory.getLogger(ConnectorInitializer.class);

        private final ConnectorDefinitionRepository repository;

        public ConnectorInitializer(ConnectorDefinitionRepository repository) {
                this.repository = repository;
        }

        @PostConstruct
        public void init() {
                initSlackConnector();
                initGeminiConnector();
                initTelegramConnector();
                initOutlookConnector();
                initGmailConnector();
        }

        private void initSlackConnector() {
                repository.findById("slack")
                                .switchIfEmpty(repository.save(ConnectorDefinition.builder()
                                                .id("slack")
                                                .name("Slack")
                                                .description("Send messages to Slack channels")
                                                .icon("slack")
                                                .category("Communication")
                                                .authType(AuthType.OAUTH2)
                                                .oauth(OAuthConfig.builder()
                                                                .authUrl("https://slack.com/oauth/v2/authorize")
                                                                .tokenUrl("https://slack.com/api/oauth.v2.access")
                                                                .scopes(List.of("chat:write", "channels:read"))
                                                                .build())
                                                .triggers(List.of())
                                                .actions(List.of(
                                                                ActionDefinition.builder()
                                                                                .id("send_message")
                                                                                .name("Send Message")
                                                                                .description("Send a message to a Slack channel")
                                                                                .inputSchema(Map.of(
                                                                                                "channel",
                                                                                                FieldSchema.builder()
                                                                                                                .type("string")
                                                                                                                .label("Channel")
                                                                                                                .required(true)
                                                                                                                .build(),
                                                                                                "text",
                                                                                                FieldSchema.builder()
                                                                                                                .type("string")
                                                                                                                .label("Message")
                                                                                                                .required(true)
                                                                                                                .build()))
                                                                                .build()))
                                                .build()))
                                .subscribe(c -> logger.info("Slack connector initialized"));
        }

        private void initGeminiConnector() {
                repository.findById("gemini")
                                .switchIfEmpty(repository.save(ConnectorDefinition.builder()
                                                .id("gemini")
                                                .name("Google Gemini AI")
                                                .description("AI-powered text analysis and generation")
                                                .icon("gemini")
                                                .category("AI")
                                                .authType(AuthType.API_KEY)
                                                .apiKeyHeader("X-API-Key")
                                                .triggers(List.of())
                                                .actions(List.of(
                                                                ActionDefinition.builder()
                                                                                .id("analyze_email")
                                                                                .name("Analyze Email")
                                                                                .description("Analyze if an email is a hiring/recruiter email")
                                                                                .inputSchema(Map.of(
                                                                                                "subject",
                                                                                                FieldSchema.builder()
                                                                                                                .type("string")
                                                                                                                .label("Subject")
                                                                                                                .required(true)
                                                                                                                .build(),
                                                                                                "body",
                                                                                                FieldSchema.builder()
                                                                                                                .type("string")
                                                                                                                .label("Email Body")
                                                                                                                .required(true)
                                                                                                                .build(),
                                                                                                "from",
                                                                                                FieldSchema.builder()
                                                                                                                .type("string")
                                                                                                                .label("From")
                                                                                                                .required(false)
                                                                                                                .build()))
                                                                                .outputSchema(Map.of(
                                                                                                "isHiringEmail",
                                                                                                FieldSchema.builder()
                                                                                                                .type("boolean")
                                                                                                                .label("Is Hiring Email")
                                                                                                                .build(),
                                                                                                "analysis",
                                                                                                FieldSchema.builder()
                                                                                                                .type("string")
                                                                                                                .label("Analysis")
                                                                                                                .build()))
                                                                                .build(),
                                                                ActionDefinition.builder()
                                                                                .id("draft_response")
                                                                                .name("Draft Email Response")
                                                                                .description("Generate a professional email response")
                                                                                .inputSchema(Map.of(
                                                                                                "originalEmail",
                                                                                                FieldSchema.builder()
                                                                                                                .type("string")
                                                                                                                .label("Original Email")
                                                                                                                .required(true)
                                                                                                                .build(),
                                                                                                "responseType",
                                                                                                FieldSchema.builder()
                                                                                                                .type("string")
                                                                                                                .label("Response Type")
                                                                                                                .enumValues(List.of(
                                                                                                                                "interested",
                                                                                                                                "not_interested",
                                                                                                                                "need_more_info"))
                                                                                                                .build()))
                                                                                .outputSchema(Map.of(
                                                                                                "draftedResponse",
                                                                                                FieldSchema.builder()
                                                                                                                .type("string")
                                                                                                                .label("Drafted Response")
                                                                                                                .build()))
                                                                                .build()))
                                                .build()))
                                .subscribe(c -> logger.info("Gemini connector initialized"));
        }

        private void initTelegramConnector() {
                repository.findById("telegram")
                                .switchIfEmpty(repository.save(ConnectorDefinition.builder()
                                                .id("telegram")
                                                .name("Telegram")
                                                .description("Send messages via Telegram bot")
                                                .icon("telegram")
                                                .category("Communication")
                                                .authType(AuthType.API_KEY)
                                                .apiKeyHeader("Bot-Token")
                                                .triggers(List.of())
                                                .actions(List.of(
                                                                ActionDefinition.builder()
                                                                                .id("send_message")
                                                                                .name("Send Message")
                                                                                .description("Send a text message")
                                                                                .inputSchema(Map.of(
                                                                                                "chatId",
                                                                                                FieldSchema.builder()
                                                                                                                .type("string")
                                                                                                                .label("Chat ID")
                                                                                                                .required(true)
                                                                                                                .build(),
                                                                                                "text",
                                                                                                FieldSchema.builder()
                                                                                                                .type("string")
                                                                                                                .label("Message")
                                                                                                                .required(true)
                                                                                                                .build()))
                                                                                .build(),
                                                                ActionDefinition.builder()
                                                                                .id("send_approval_request")
                                                                                .name("Send Approval Request")
                                                                                .description("Send a message with Approve/Reject buttons")
                                                                                .inputSchema(Map.of(
                                                                                                "chatId",
                                                                                                FieldSchema.builder()
                                                                                                                .type("string")
                                                                                                                .label("Chat ID")
                                                                                                                .required(true)
                                                                                                                .build(),
                                                                                                "text",
                                                                                                FieldSchema.builder()
                                                                                                                .type("string")
                                                                                                                .label("Message")
                                                                                                                .required(true)
                                                                                                                .build(),
                                                                                                "callbackData",
                                                                                                FieldSchema.builder()
                                                                                                                .type("string")
                                                                                                                .label("Callback ID")
                                                                                                                .required(true)
                                                                                                                .build()))
                                                                                .build()))
                                                .build()))
                                .subscribe(c -> logger.info("Telegram connector initialized"));
        }

        private void initOutlookConnector() {
                repository.findById("outlook")
                                .switchIfEmpty(repository.save(ConnectorDefinition.builder()
                                                .id("outlook")
                                                .name("Microsoft Outlook")
                                                .description("Read and send emails via Outlook")
                                                .icon("outlook")
                                                .category("Email")
                                                .authType(AuthType.OAUTH2)
                                                .oauth(OAuthConfig.builder()
                                                                .authUrl("https://login.microsoftonline.com/common/oauth2/v2.0/authorize")
                                                                .tokenUrl("https://login.microsoftonline.com/common/oauth2/v2.0/token")
                                                                .scopes(List.of("Mail.Read", "Mail.Send",
                                                                                "offline_access"))
                                                                .build())
                                                .triggers(List.of(
                                                                TriggerDefinition.builder()
                                                                                .id("new_email")
                                                                                .name("New Email Received")
                                                                                .description("Triggers when a new email arrives")
                                                                                .outputSchema(Map.of(
                                                                                                "subject",
                                                                                                FieldSchema.builder()
                                                                                                                .type("string")
                                                                                                                .label("Subject")
                                                                                                                .build(),
                                                                                                "body",
                                                                                                FieldSchema.builder()
                                                                                                                .type("string")
                                                                                                                .label("Body")
                                                                                                                .build(),
                                                                                                "from",
                                                                                                FieldSchema.builder()
                                                                                                                .type("string")
                                                                                                                .label("From")
                                                                                                                .build()))
                                                                                .build()))
                                                .actions(List.of(
                                                                ActionDefinition.builder()
                                                                                .id("send_email")
                                                                                .name("Send Email")
                                                                                .description("Send an email")
                                                                                .inputSchema(Map.of(
                                                                                                "to",
                                                                                                FieldSchema.builder()
                                                                                                                .type("string")
                                                                                                                .label("To")
                                                                                                                .required(true)
                                                                                                                .build(),
                                                                                                "subject",
                                                                                                FieldSchema.builder()
                                                                                                                .type("string")
                                                                                                                .label("Subject")
                                                                                                                .required(true)
                                                                                                                .build(),
                                                                                                "body",
                                                                                                FieldSchema.builder()
                                                                                                                .type("string")
                                                                                                                .label("Body")
                                                                                                                .required(true)
                                                                                                                .build()))
                                                                                .build()))
                                                .build()))
                                .subscribe(c -> logger.info("Outlook connector initialized"));
        }

        private void initGmailConnector() {
                repository.findById("gmail")
                                .switchIfEmpty(repository.save(ConnectorDefinition.builder()
                                                .id("gmail")
                                                .name("Gmail")
                                                .description("Read and send emails via Gmail")
                                                .icon("gmail")
                                                .category("Email")
                                                .authType(AuthType.OAUTH2)
                                                .oauth(OAuthConfig.builder()
                                                                .authUrl("https://accounts.google.com/o/oauth2/auth")
                                                                .tokenUrl("https://oauth2.googleapis.com/token")
                                                                .scopes(List.of(
                                                                                "https://www.googleapis.com/auth/gmail.readonly",
                                                                                "https://www.googleapis.com/auth/gmail.send"))
                                                                .build())
                                                .triggers(List.of(
                                                                TriggerDefinition.builder()
                                                                                .id("new_email")
                                                                                .name("New Email Received")
                                                                                .description("Triggers when a new email arrives in inbox")
                                                                                .outputSchema(Map.of(
                                                                                                "id",
                                                                                                FieldSchema.builder()
                                                                                                                .type("string")
                                                                                                                .label("Message ID")
                                                                                                                .build(),
                                                                                                "subject",
                                                                                                FieldSchema.builder()
                                                                                                                .type("string")
                                                                                                                .label("Subject")
                                                                                                                .build(),
                                                                                                "body",
                                                                                                FieldSchema.builder()
                                                                                                                .type("string")
                                                                                                                .label("Body")
                                                                                                                .build(),
                                                                                                "from",
                                                                                                FieldSchema.builder()
                                                                                                                .type("string")
                                                                                                                .label("From")
                                                                                                                .build()))
                                                                                .build()))
                                                .actions(List.of(
                                                                ActionDefinition.builder()
                                                                                .id("list_emails")
                                                                                .name("List Emails")
                                                                                .description("List recent emails")
                                                                                .inputSchema(Map.of(
                                                                                                "maxResults",
                                                                                                FieldSchema.builder()
                                                                                                                .type("number")
                                                                                                                .label("Max Results")
                                                                                                                .defaultValue("10")
                                                                                                                .build(),
                                                                                                "query",
                                                                                                FieldSchema.builder()
                                                                                                                .type("string")
                                                                                                                .label("Search Query")
                                                                                                                .build()))
                                                                                .build(),
                                                                ActionDefinition.builder()
                                                                                .id("get_email")
                                                                                .name("Get Email")
                                                                                .description("Get a single email by ID")
                                                                                .inputSchema(Map.of(
                                                                                                "messageId",
                                                                                                FieldSchema.builder()
                                                                                                                .type("string")
                                                                                                                .label("Message ID")
                                                                                                                .required(true)
                                                                                                                .build()))
                                                                                .build(),
                                                                ActionDefinition.builder()
                                                                                .id("send_email")
                                                                                .name("Send Email")
                                                                                .description("Send an email")
                                                                                .inputSchema(Map.of(
                                                                                                "to",
                                                                                                FieldSchema.builder()
                                                                                                                .type("string")
                                                                                                                .label("To")
                                                                                                                .required(true)
                                                                                                                .build(),
                                                                                                "subject",
                                                                                                FieldSchema.builder()
                                                                                                                .type("string")
                                                                                                                .label("Subject")
                                                                                                                .required(true)
                                                                                                                .build(),
                                                                                                "body",
                                                                                                FieldSchema.builder()
                                                                                                                .type("string")
                                                                                                                .label("Body")
                                                                                                                .required(true)
                                                                                                                .build()))
                                                                                .build()))
                                                .build()))
                                .subscribe(c -> logger.info("Gmail connector initialized"));
        }
}
