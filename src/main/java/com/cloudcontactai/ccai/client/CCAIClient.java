// Copyright (c) 2025 CloudContactAI LLC
// Licensed under the MIT License. See LICENSE in the project root for license information.

package com.cloudcontactai.ccai.client;

import com.cloudcontactai.ccai.config.CCAIConfig;
import com.cloudcontactai.ccai.email.EmailService;
import com.cloudcontactai.ccai.sms.SMSService;
import com.cloudcontactai.ccai.webhook.WebhookService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

/**
 * Main client for interacting with the CloudContactAI API
 */
@Component
public class CCAIClient {

    private final CCAIConfig config;
    private final SMSService smsService;
    private final EmailService emailService;
    private final WebhookService webhookService;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    /**
     * Creates a new CCAI client with the provided configuration
     *
     * @param config The CCAI configuration
     */
    public CCAIClient(CCAIConfig config) {
        this.config = config;
        this.objectMapper = createObjectMapper();
        this.restTemplate = createRestTemplate();
        this.smsService = new SMSService(config, restTemplate, objectMapper);
        this.emailService = new EmailService(config, restTemplate, objectMapper);
        this.webhookService = new WebhookService(objectMapper);
        
        // Validate configuration on initialization
        config.validate();
    }

    /**
     * Creates a new CCAI client with client ID and API key
     *
     * @param clientId The client ID
     * @param apiKey The API key
     */
    public CCAIClient(String clientId, String apiKey) {
        this(new CCAIConfig(clientId, apiKey));
    }

    /**
     * Gets the SMS service for sending SMS messages
     *
     * @return SMSService instance
     */
    public SMSService getSmsService() {
        return smsService;
    }

    /**
     * Gets the Email service for sending email messages
     *
     * @return EmailService instance
     */
    public EmailService getEmailService() {
        return emailService;
    }

    /**
     * Gets the Webhook service for handling webhook events
     *
     * @return WebhookService instance
     */
    public WebhookService getWebhookService() {
        return webhookService;
    }

    /**
     * Gets the configuration used by this client
     *
     * @return CCAIConfig instance
     */
    public CCAIConfig getConfig() {
        return config;
    }

    /**
     * Gets the RestTemplate used for HTTP requests
     *
     * @return RestTemplate instance
     */
    public RestTemplate getRestTemplate() {
        return restTemplate;
    }

    /**
     * Gets the ObjectMapper used for JSON serialization/deserialization
     *
     * @return ObjectMapper instance
     */
    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    /**
     * Creates and configures the RestTemplate
     */
    private RestTemplate createRestTemplate() {
        RestTemplateBuilder builder = new RestTemplateBuilder()
                .setConnectTimeout(Duration.ofMillis(config.getTimeoutMs()))
                .setReadTimeout(Duration.ofMillis(config.getTimeoutMs()));

        // Configure request factory for timeout settings
        ClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        ((SimpleClientHttpRequestFactory) factory).setConnectTimeout(config.getTimeoutMs());
        ((SimpleClientHttpRequestFactory) factory).setReadTimeout(config.getTimeoutMs());

        return builder
                .requestFactory(() -> factory)
                .build();
    }

    /**
     * Creates and configures the ObjectMapper
     */
    private ObjectMapper createObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        return mapper;
    }

    /**
     * Builder class for creating CCAIClient instances
     */
    public static class Builder {
        private String clientId;
        private String apiKey;
        private String baseUrl;
        private String emailBaseUrl;
        private String authBaseUrl;
        private boolean useTestEnvironment = false;
        private int timeoutMs = 30000;
        private int maxRetries = 3;
        private boolean debugMode = false;

        public Builder clientId(String clientId) {
            this.clientId = clientId;
            return this;
        }

        public Builder apiKey(String apiKey) {
            this.apiKey = apiKey;
            return this;
        }

        public Builder baseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
            return this;
        }

        public Builder emailBaseUrl(String emailBaseUrl) {
            this.emailBaseUrl = emailBaseUrl;
            return this;
        }

        public Builder authBaseUrl(String authBaseUrl) {
            this.authBaseUrl = authBaseUrl;
            return this;
        }

        public Builder useTestEnvironment(boolean useTestEnvironment) {
            this.useTestEnvironment = useTestEnvironment;
            return this;
        }

        public Builder timeoutMs(int timeoutMs) {
            this.timeoutMs = timeoutMs;
            return this;
        }

        public Builder maxRetries(int maxRetries) {
            this.maxRetries = maxRetries;
            return this;
        }

        public Builder debugMode(boolean debugMode) {
            this.debugMode = debugMode;
            return this;
        }

        public CCAIClient build() {
            CCAIConfig config = new CCAIConfig();
            config.setClientId(clientId);
            config.setApiKey(apiKey);
            
            if (baseUrl != null) config.setBaseUrl(baseUrl);
            if (emailBaseUrl != null) config.setEmailBaseUrl(emailBaseUrl);
            if (authBaseUrl != null) config.setAuthBaseUrl(authBaseUrl);
            
            config.setUseTestEnvironment(useTestEnvironment);
            config.setTimeoutMs(timeoutMs);
            config.setMaxRetries(maxRetries);
            config.setDebugMode(debugMode);

            return new CCAIClient(config);
        }
    }

    /**
     * Creates a new builder for constructing CCAIClient instances
     *
     * @return Builder instance
     */
    public static Builder builder() {
        return new Builder();
    }
}
