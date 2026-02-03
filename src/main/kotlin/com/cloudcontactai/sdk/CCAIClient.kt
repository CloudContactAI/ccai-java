package com.cloudcontactai.sdk

import com.cloudcontactai.sdk.common.ApiClient
import com.cloudcontactai.sdk.common.CCAIConfig
import com.cloudcontactai.sdk.sms.SMSService
import com.cloudcontactai.sdk.email.EmailService
import com.cloudcontactai.sdk.webhook.WebhookService
import com.cloudcontactai.sdk.mms.MMSService

class CCAIClient(private val config: CCAIConfig) {
    private val apiClient = ApiClient(config)
    
    val sms = SMSService(config, apiClient)
    val email = EmailService(config, apiClient)
    val webhook = WebhookService(config, apiClient)
    val mms = MMSService(config, apiClient)
    
    fun close() {
        // Cleanup resources if needed
    }
}
