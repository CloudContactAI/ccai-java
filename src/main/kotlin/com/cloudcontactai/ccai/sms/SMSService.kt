package com.cloudcontactai.ccai.sms

import com.cloudcontactai.ccai.common.ApiClient
import com.cloudcontactai.ccai.common.CCAIConfig

class SMSService(private val config: CCAIConfig, private val apiClient: ApiClient) {
    
    fun sendSingle(
        firstName: String,
        lastName: String,
        phone: String,
        message: String,
        title: String,
        senderPhone: String? = null
    ): SMSResponse {
        val account = Account(
            firstName = firstName,
            lastName = lastName,
            phone = phone
        )
        
        return send(listOf(account), message, title, senderPhone)
    }
    
    fun send(
        accounts: List<Account>,
        message: String,
        title: String,
        senderPhone: String? = null
    ): SMSResponse {
        val campaign = SMSCampaign(
            accounts = accounts,
            message = message,
            title = title,
            senderPhone = senderPhone
        )
        
        val headers = mapOf("ForceNewCampaign" to "false")
        
        return apiClient.request(
            method = "POST",
            endpoint = "/clients/${config.clientId}/campaigns/direct",
            data = campaign,
            headers = headers,
            responseClass = SMSResponse::class.java
        )
    }
    
    fun getCampaignStatus(campaignId: String): SMSCampaignStatus {
        return apiClient.request(
            method = "GET",
            endpoint = "/campaigns/$campaignId/status",
            responseClass = SMSCampaignStatus::class.java
        )
    }
}
