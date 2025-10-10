// Copyright (c) 2025 CloudContactAI LLC
// Licensed under the MIT License. See LICENSE in the project root for license information.

package com.cloudcontactai.ccai.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Configuration properties for the CCAI client
 */
@Component
@ConfigurationProperties(prefix = "ccai")
public class CCAIConfig {
    
    /**
     * Client ID for authentication
     */
    private String clientId;
    
    /**
     * API key for authentication
     */
    private String apiKey;
    
    /**
     * Base URL for the SMS/MMS API
     */
    private String baseUrl = getEnvOrDefault("CCAI_BASE_URL", "https://core.cloudcontactai.com/api");
    
    /**
     * Base URL for the Email API
     */
    private String emailBaseUrl = getEnvOrDefault("CCAI_EMAIL_BASE_URL", "https://email-campaigns.cloudcontactai.com");
    
    /**
     * Base URL for the Auth API
     */
    private String authBaseUrl = getEnvOrDefault("CCAI_AUTH_BASE_URL", "https://auth.cloudcontactai.com");
    
    /**
     * Whether to use test environment URLs
     */
    private boolean useTestEnvironment = false;
    
    /**
     * HTTP timeout in milliseconds
     */
    private int timeoutMs = 30000;
    
    /**
     * Maximum number of retry attempts
     */
    private int maxRetries = 3;
    
    /**
     * Whether to enable debug logging
     */
    private boolean debugMode = false;

    // Constructors
    public CCAIConfig() {}

    public CCAIConfig(String clientId, String apiKey) {
        this.clientId = clientId;
        this.apiKey = apiKey;
    }

    // Getters and Setters
    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getEmailBaseUrl() {
        return emailBaseUrl;
    }

    public void setEmailBaseUrl(String emailBaseUrl) {
        this.emailBaseUrl = emailBaseUrl;
    }

    public String getAuthBaseUrl() {
        return authBaseUrl;
    }

    public void setAuthBaseUrl(String authBaseUrl) {
        this.authBaseUrl = authBaseUrl;
    }

    public boolean isUseTestEnvironment() {
        return useTestEnvironment;
    }

    public void setUseTestEnvironment(boolean useTestEnvironment) {
        this.useTestEnvironment = useTestEnvironment;
    }

    public int getTimeoutMs() {
        return timeoutMs;
    }

    public void setTimeoutMs(int timeoutMs) {
        this.timeoutMs = timeoutMs;
    }

    public int getMaxRetries() {
        return maxRetries;
    }

    public void setMaxRetries(int maxRetries) {
        this.maxRetries = maxRetries;
    }

    public boolean isDebugMode() {
        return debugMode;
    }

    public void setDebugMode(boolean debugMode) {
        this.debugMode = debugMode;
    }

    /**
     * Validates the configuration
     */
    public void validate() {
        if (clientId == null || clientId.trim().isEmpty()) {
            throw new IllegalArgumentException("Client ID is required");
        }
        if (apiKey == null || apiKey.trim().isEmpty()) {
            throw new IllegalArgumentException("API Key is required");
        }
    }

    /**
     * Gets environment variable or default value
     */
    private String getEnvOrDefault(String envVar, String defaultValue) {
        String value = System.getenv(envVar);
        return value != null ? value : defaultValue;
    }

    @Override
    public String toString() {
        return "CCAIConfig{" +
                "clientId='" + (clientId != null ? "***" : null) + '\'' +
                ", apiKey='" + (apiKey != null ? "***" : null) + '\'' +
                ", baseUrl='" + baseUrl + '\'' +
                ", emailBaseUrl='" + emailBaseUrl + '\'' +
                ", authBaseUrl='" + authBaseUrl + '\'' +
                ", useTestEnvironment=" + useTestEnvironment +
                ", timeoutMs=" + timeoutMs +
                ", maxRetries=" + maxRetries +
                ", debugMode=" + debugMode +
                '}';
    }
}
