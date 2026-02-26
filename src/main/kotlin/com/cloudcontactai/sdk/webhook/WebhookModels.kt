package com.cloudcontactai.sdk.webhook

import com.fasterxml.jackson.annotation.JsonProperty

class WebhookRequest {
    @JsonProperty("url")
    var url: String = ""
    
    @JsonProperty("method")
    var method: String = "POST"
    
    @JsonProperty("integrationType")
    var integrationType: String = "DEFAULT"
    
    @JsonProperty("secretKey")
    var secretKey: String? = null
    
    constructor()
    
    constructor(url: String) {
        this.url = url
        this.method = "POST"
        this.integrationType = "ALL"
    }
    
    constructor(url: String, secretKey: String?) {
        this.url = url
        this.method = "POST"
        this.integrationType = "ALL"
        this.secretKey = secretKey
    }
    
    constructor(url: String, secretKey: String?, method: String) {
        this.url = url
        this.method = method
        this.integrationType = "ALL"
        this.secretKey = secretKey
    }
    
    constructor(url: String, secretKey: String?, method: String, integrationType: String) {
        this.url = url
        this.method = method
        this.integrationType = integrationType
        this.secretKey = secretKey
    }
}

data class WebhookResponse(
    @JsonProperty("id") val id: Long,
    @JsonProperty("url") val url: String,
    @JsonProperty("method") val method: String,
    @JsonProperty("integrationType") val integrationType: String,
    @JsonProperty("secretKey") val secretKey: String?
)

data class WebhookEvent(
    @JsonProperty("eventType") val eventType: String,
    @JsonProperty("data") val data: Map<String, Any?>,
    @JsonProperty("eventHash") val eventHash: String
)
