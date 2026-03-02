package com.cloudcontactai.sdk.webhook

import com.cloudcontactai.sdk.common.ApiClient
import com.cloudcontactai.sdk.common.CCAIConfig
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import java.util.Base64
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

class WebhookService(private val config: CCAIConfig, private val apiClient: ApiClient) {
    private val objectMapper = jacksonObjectMapper()
    
    init {
        require(config.clientId.isNotBlank()) { "Client ID cannot be null or empty" }
    }
    
    fun create(request: WebhookRequest): WebhookResponse {
        return create(listOf(request)).first()
    }

    fun create(request: List<WebhookRequest>): List<WebhookResponse> {
        return apiClient.request(
            method = "POST",
            endpoint = "/v1/client/${config.clientId}/integration",
            data = request,
            responseClass = Array<WebhookResponse>::class.java
        ).toList()
    }

    fun get(webhookId: Long): WebhookResponse {
        return apiClient.request(
            method = "GET",
            endpoint = "/v1/client/${config.clientId}/integration/${webhookId}",
            responseClass = WebhookResponse::class.java
        )
    }

    fun getAll(): List<WebhookResponse> {
        return apiClient.request(
            method = "GET",
            endpoint = "/v1/client/${config.clientId}/integration",
            responseClass = Array<WebhookResponse>::class.java
        ).toList()
    }

    fun update(request: WebhookUpdateRequest): WebhookResponse {
        return create(request)
    }

    fun delete(webhookId: Long): WebhookResponse {
        return apiClient.request(
            method = "DELETE",
            endpoint = "/v1/client/${config.clientId}/integration/${webhookId}",
            responseClass = WebhookResponse::class.java
        )
    }

    fun parseWebhookEvent(payload: String): WebhookEvent {
        return objectMapper.readValue(payload, WebhookEvent::class.java)
    }

    fun validateSignature(signature: String, secretKey: String, clientId: Long, eventHash: String): Boolean {
        return try {
            val expectedSignature = generateSignature(secretKey, clientId, eventHash)
            constantTimeEquals(signature, expectedSignature)
        } catch (e: Exception) {
            false
        }
    }

    fun generateSignature(secretKey: String, clientId: Long, eventHash: String): String {
        val data = "$clientId:$eventHash"
        val hmac = Mac.getInstance("HmacSHA256")
        val secretKeySpec = SecretKeySpec(secretKey.toByteArray(), "HmacSHA256")
        hmac.init(secretKeySpec)
        val signatureBytes = hmac.doFinal(data.toByteArray())
        return Base64.getEncoder().encodeToString(signatureBytes)
    }

    private fun constantTimeEquals(a: String, b: String): Boolean {
        if (a.length != b.length) return false
        var result = 0
        for (i in a.indices) {
            result = result or (a[i].code xor b[i].code)
        }
        return result == 0
    }

}
