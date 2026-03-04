// Copyright (c) 2025 CloudContactAI LLC
// Licensed under the MIT License. See LICENSE in the project root for license information.

package com.cloudcontactai.sdk.common

/**
 * Configuration for the CCAI client
 */
data class CCAIConfig @JvmOverloads constructor(
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
    val useTestEnvironment: Boolean = false,

    /**
     * Enable debug mode for detailed logging
     */
    val debugMode: Boolean = false,

    /**
     * Maximum number of retry attempts for failed requests
     */
    val maxRetries: Int = 3,

    /**
     * Request timeout in milliseconds
     */
    val timeoutMs: Long = 30000
) {
    /**
     * Base URL for the SMS/MMS API
     */
    val baseUrl: String = if (useTestEnvironment) {
        System.getenv("CCAI_BASE_URL")?.replace("core.cloudcontactai.com", "core-test-cloudcontactai.allcode.com")
            ?: "https://core-test-cloudcontactai.allcode.com/api"
    } else {
        System.getenv("CCAI_BASE_URL") ?: "https://core.cloudcontactai.com/api"
    }
    
    /**
     * Base URL for the Email API
     */
    val emailBaseUrl: String = if (useTestEnvironment) {
        System.getenv("CCAI_EMAIL_BASE_URL")?.replace("email-campaigns.cloudcontactai.com", "email-campaigns-test-cloudcontactai.allcode.com")
            ?: "https://email-campaigns-test-cloudcontactai.allcode.com/api/v1"
    } else {
        System.getenv("CCAI_EMAIL_BASE_URL") ?: "https://email-campaigns.cloudcontactai.com/api/v1"
    }
    
    /**
     * Base URL for the Auth API
     */
    val authBaseUrl: String = if (useTestEnvironment) {
        System.getenv("CCAI_AUTH_BASE_URL")?.replace("auth.cloudcontactai.com", "auth-test-cloudcontactai.allcode.com")
            ?: "https://auth-test-cloudcontactai.allcode.com"
    } else {
        System.getenv("CCAI_AUTH_BASE_URL") ?: "https://auth.cloudcontactai.com"
    }
    
    /**
     * Base URL for the Files API (MMS uploads)
     */
    val filesBaseUrl: String = if (useTestEnvironment) {
        System.getenv("CCAI_FILES_BASE_URL")?.replace("files.cloudcontactai.com", "files-test-cloudcontactai.allcode.com")
            ?: "https://files-test-cloudcontactai.allcode.com"
    } else {
        System.getenv("CCAI_FILES_BASE_URL") ?: "https://files.cloudcontactai.com"
    }
    
    init {
        require(clientId.isNotBlank()) { "Client ID cannot be blank" }
        require(apiKey.isNotBlank()) { "API key cannot be blank" }
        require(maxRetries >= 0) { "Max retries must be non-negative" }
        require(timeoutMs > 0) { "Timeout must be positive" }
    }
}
