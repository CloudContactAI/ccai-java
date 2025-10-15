package com.cloudcontactai.ccai.webhook

import com.cloudcontactai.ccai.common.ApiClient
import com.cloudcontactai.ccai.common.CCAIConfig

class WebhookService(private val config: CCAIConfig, private val apiClient: ApiClient) {
    
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
}
