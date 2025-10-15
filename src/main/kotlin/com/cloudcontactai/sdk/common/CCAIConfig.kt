// Copyright (c) 2025 CloudContactAI LLC
// Licensed under the MIT License. See LICENSE in the project root for license information.

package com.cloudcontactai.sdk.common

/**
 * Configuration for the CCAI client
 */
data class CCAIConfig(
    /**
     * Client ID for authentication
     */
    val clientId: String,
    
    /**
     * API key for authentication
     */
    val apiKey: String,
    
    /**
     * Whether to use test environment URLs
     */
    val useTestEnvironment: Boolean = false
) {
    /**
     * Base URL for the SMS/MMS API
     */
    val baseUrl: String = if (useTestEnvironment) {
        System.getenv("CCAI_BASE_URL")?.replace("core.cloudcontactai.com", "core-test.cloudcontactai.com") 
            ?: "https://core-test.cloudcontactai.com/api"
    } else {
        System.getenv("CCAI_BASE_URL") ?: "https://core.cloudcontactai.com/api"
    }
    
    /**
     * Base URL for the Email API
     */
    val emailBaseUrl: String = if (useTestEnvironment) {
        System.getenv("CCAI_EMAIL_BASE_URL")?.replace("email-campaigns.cloudcontactai.com", "email-campaigns-test.cloudcontactai.com")
            ?: "https://email-campaigns-test.cloudcontactai.com/api/v1"
    } else {
        System.getenv("CCAI_EMAIL_BASE_URL") ?: "https://email-campaigns.cloudcontactai.com/api/v1"
    }
    
    /**
     * Base URL for the Auth API
     */
    val authBaseUrl: String = if (useTestEnvironment) {
        System.getenv("CCAI_AUTH_BASE_URL")?.replace("auth.cloudcontactai.com", "auth-test.cloudcontactai.com")
            ?: "https://auth-test.cloudcontactai.com"
    } else {
        System.getenv("CCAI_AUTH_BASE_URL") ?: "https://auth.cloudcontactai.com"
    }
    
    init {
        require(clientId.isNotBlank()) { "Client ID cannot be blank" }
        require(apiKey.isNotBlank()) { "API key cannot be blank" }
    }
}
