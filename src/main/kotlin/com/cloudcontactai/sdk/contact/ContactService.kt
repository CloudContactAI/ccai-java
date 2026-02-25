package com.cloudcontactai.sdk.contact

import com.cloudcontactai.sdk.common.ApiClient
import com.cloudcontactai.sdk.common.CCAIConfig

class ContactService(private val config: CCAIConfig, private val apiClient: ApiClient) {

    fun setDoNotText(
        contactId: String? = null,
        phone: String? = null,
        doNotText: Boolean
    ): ContactDoNotTextResponse{
        val requestData = ContactDoNotTextRequest(
            config.clientId,
            contactId,
            phone,
            doNotText
        )

        return apiClient.request(
            method = "PUT",
            endpoint = "/contacts/do-not-text",
            data = requestData,
            baseUrl = config.contactBaseUrl,
            responseClass = ContactDoNotTextResponse::class.java
        )
    }
}