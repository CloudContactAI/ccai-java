package com.cloudcontactai.sdk.webhook

import com.cloudcontactai.sdk.common.ApiClient
import com.cloudcontactai.sdk.common.CCAIConfig
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

class WebhookService(private val config: CCAIConfig, private val apiClient: ApiClient) {
    private val objectMapper = jacksonObjectMapper()
    
    fun create(request: WebhookRequest): WebhookResponse {
        return apiClient.request(
            method = "POST",
            endpoint = "/webhooks",
            data = request,
            responseClass = WebhookResponse::class.java
        )
    }
    
    fun getAll(): List<WebhookResponse> {
        return apiClient.request(
            method = "GET",
            endpoint = "/webhooks",
            responseClass = Array<WebhookResponse>::class.java
        ).toList()
    }
    
    fun get(id: String): WebhookResponse {
        return apiClient.request(
            method = "GET",
            endpoint = "/webhooks/$id",
            responseClass = WebhookResponse::class.java
        )
    }
    
    fun update(id: String, request: WebhookRequest): WebhookResponse {
        return apiClient.request(
            method = "PUT",
            endpoint = "/webhooks/$id",
            data = request,
            responseClass = WebhookResponse::class.java
        )
    }
    
    fun delete(id: String): WebhookDeleteResponse {
        return apiClient.request(
            method = "DELETE",
            endpoint = "/webhooks/$id",
            responseClass = WebhookDeleteResponse::class.java
        )
    }
    
    fun test(id: String): WebhookTestResponse {
        return apiClient.request(
            method = "POST",
            endpoint = "/webhooks/$id/test",
            responseClass = WebhookTestResponse::class.java
        )
    }

    fun parseWebhookEvent(payload: String): WebhookEvent {
        return objectMapper.readValue(payload, WebhookEvent::class.java)
    }

    fun validateWebhookSignature(payload: String, signature: String, secret: String): Boolean {
        val calculatedSignature = calculateHmacSha256(payload, secret)
        return constantTimeEquals(signature, calculatedSignature)
    }

    private fun calculateHmacSha256(data: String, secret: String): String {
        val mac = Mac.getInstance("HmacSHA256")
        val secretKeySpec = SecretKeySpec(secret.toByteArray(), "HmacSHA256")
        mac.init(secretKeySpec)
        val hash = mac.doFinal(data.toByteArray())
        return hash.joinToString("") { "%02x".format(it) }
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
