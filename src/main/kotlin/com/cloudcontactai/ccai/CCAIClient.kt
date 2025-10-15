package com.cloudcontactai.ccai

import com.cloudcontactai.ccai.common.ApiClient
import com.cloudcontactai.ccai.common.CCAIConfig
import com.cloudcontactai.ccai.sms.SMSService
import com.cloudcontactai.ccai.email.EmailService
import com.cloudcontactai.ccai.webhook.WebhookService

class CCAIClient(private val config: CCAIConfig) {
    private val apiClient = ApiClient(config)
    
    val sms = SMSService(config, apiClient)
    val email = EmailService(config, apiClient)
    val webhook = WebhookService(config, apiClient)
    
    fun close() {
        // Cleanup resources if needed
    }
}
