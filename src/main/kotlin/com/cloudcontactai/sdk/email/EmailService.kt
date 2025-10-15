package com.cloudcontactai.sdk.email

import com.cloudcontactai.sdk.common.ApiClient
import com.cloudcontactai.sdk.common.CCAIConfig

class EmailService(private val config: CCAIConfig, private val apiClient: ApiClient) {
    
    fun sendSingle(
        firstName: String,
        lastName: String,
        email: String,
        subject: String,
        htmlContent: String,
        textContent: String? = null,
        senderEmail: String = "noreply@cloudcontactai.com",
        replyEmail: String = "noreply@cloudcontactai.com",
        senderName: String = "CloudContactAI"
    ): EmailResponse {
        val account = EmailAccount(
            firstName = firstName,
            lastName = lastName,
            email = email
        )
        
        return send(
            accounts = listOf(account),
            subject = subject,
            htmlContent = htmlContent,
            senderEmail = senderEmail,
            replyEmail = replyEmail,
            senderName = senderName
        )
    }
    
    fun send(
        accounts: List<EmailAccount>,
        subject: String,
        htmlContent: String,
        senderEmail: String = "noreply@cloudcontactai.com",
        replyEmail: String = "noreply@cloudcontactai.com",
        senderName: String = "CloudContactAI"
    ): EmailResponse {
        val campaign = EmailCampaign(
            subject = subject,
            title = subject,
            message = htmlContent,
            senderEmail = senderEmail,
            replyEmail = replyEmail,
            senderName = senderName,
            accounts = accounts
        )
        
        val headers = mapOf(
            "AccountId" to config.clientId,
            "ClientId" to config.clientId
        )
        
        return apiClient.request(
            method = "POST",
            endpoint = "/campaigns",
            data = campaign,
            baseUrl = config.emailBaseUrl,
            headers = headers,
            responseClass = EmailResponse::class.java
        )
    }
    
    fun getCampaignStatus(campaignId: String): EmailCampaignStatus {
        val headers = mapOf(
            "AccountId" to config.clientId,
            "ClientId" to config.clientId
        )
        
        return apiClient.request(
            method = "GET",
            endpoint = "/campaigns/$campaignId/status",
            baseUrl = config.emailBaseUrl,
            headers = headers,
            responseClass = EmailCampaignStatus::class.java
        )
    }
}
