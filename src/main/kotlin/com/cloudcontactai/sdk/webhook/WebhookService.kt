package com.cloudcontactai.sdk.webhook

import com.cloudcontactai.sdk.common.ApiClient
import com.cloudcontactai.sdk.common.CCAIConfig
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper

class WebhookService(private val config: CCAIConfig, private val apiClient: ApiClient) {
    private val objectMapper = jacksonObjectMapper()
    
    init {
        require(config.clientId.isNotBlank()) { "Client ID cannot be null or empty" }
    }
    
    fun create(request: WebhookRequest): WebhookResponse {
        return apiClient.request(
            method = "POST",
            endpoint = "/v1/client/${config.clientId}/integration",
            data = listOf(request),
            responseClass = Array<WebhookResponse>::class.java
        ).first()
    }

    fun get(): WebhookResponse? {
        val webhooks = getAll()
        return webhooks.firstOrNull()
    }

    fun getAll(): List<WebhookResponse> {
        return apiClient.request(
            method = "GET",
            endpoint = "/v1/client/${config.clientId}/integration",
            responseClass = Array<WebhookResponse>::class.java
        ).toList()
    }

    fun update(request: WebhookRequest): WebhookResponse {
        return create(request)
    }

    fun parseWebhookEvent(payload: String): WebhookEvent {
        return objectMapper.readValue(payload, WebhookEvent::class.java)
    }

}
