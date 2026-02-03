package com.cloudcontactai.sdk.webhook

import com.fasterxml.jackson.annotation.JsonProperty

data class WebhookRequest(
    @JsonProperty("url") val url: String,
    @JsonProperty("events") val events: List<String>,
    @JsonProperty("isActive") val isActive: Boolean = true,
    @JsonProperty("secret") val secret: String? = null
)

data class WebhookResponse(
    @JsonProperty("id") val id: String,
    @JsonProperty("url") val url: String,
    @JsonProperty("events") val events: List<String>,
    @JsonProperty("isActive") val isActive: Boolean,
    @JsonProperty("secret") val secret: String? = null,
    @JsonProperty("createdAt") val createdAt: String? = null,
    @JsonProperty("updatedAt") val updatedAt: String? = null
)

data class WebhookDeleteResponse(
    @JsonProperty("success") val success: Boolean,
    @JsonProperty("message") val message: String? = null
)

data class WebhookTestResponse(
    @JsonProperty("success") val success: Boolean,
    @JsonProperty("statusCode") val statusCode: Int? = null,
    @JsonProperty("response") val response: String? = null,
    @JsonProperty("error") val error: String? = null
)

data class WebhookEvent(
    @JsonProperty("eventType") val eventType: String,
    @JsonProperty("timestamp") val timestamp: String,
    @JsonProperty("campaignId") val campaignId: String?,
    @JsonProperty("messageId") val messageId: String?,
    @JsonProperty("phoneNumber") val phoneNumber: String?,
    @JsonProperty("email") val email: String?,
    @JsonProperty("status") val status: String?,
    @JsonProperty("errorCode") val errorCode: String?,
    @JsonProperty("errorMessage") val errorMessage: String?,
    @JsonProperty("customData") val customData: Map<String, Any>?,
    @JsonProperty("deliveryStatus") val deliveryStatus: String?,
    @JsonProperty("cost") val cost: Double?,
    @JsonProperty("accountId") val accountId: String?
)
