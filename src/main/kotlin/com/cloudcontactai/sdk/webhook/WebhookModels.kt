package com.cloudcontactai.sdk.webhook

import com.fasterxml.jackson.annotation.JsonProperty

class WebhookRequest {
    @JsonProperty("url")
    var url: String = ""
    
    @JsonProperty("method")
    var method: String = "POST"
    
    @JsonProperty("integrationType")
    var integrationType: String = "DEFAULT"
    
    constructor()
    
    constructor(url: String) {
        this.url = url
        this.method = "POST"
        this.integrationType = "ALL"
    }
    
    constructor(url: String, method: String) {
        this.url = url
        this.method = method
        this.integrationType = "ALL"
    }
    
    constructor(url: String, method: String, integrationType: String) {
        this.url = url
        this.method = method
        this.integrationType = integrationType
    }
}

data class WebhookResponse(
    @JsonProperty("id") val id: Long,
    @JsonProperty("url") val url: String,
    @JsonProperty("method") val method: String,
    @JsonProperty("integrationType") val integrationType: String
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
