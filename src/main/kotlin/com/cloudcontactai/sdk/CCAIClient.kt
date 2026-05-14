package com.cloudcontactai.sdk

import com.cloudcontactai.sdk.common.ApiClient
import com.cloudcontactai.sdk.common.CCAIConfig
import com.cloudcontactai.sdk.contact.ContactService
import com.cloudcontactai.sdk.contactvalidator.ContactValidatorService
import com.cloudcontactai.sdk.sms.SMSService
import com.cloudcontactai.sdk.email.EmailService
import com.cloudcontactai.sdk.webhook.WebhookService
import com.cloudcontactai.sdk.mms.MMSService
import com.cloudcontactai.sdk.brands.BrandService
import com.cloudcontactai.sdk.campaigns.CampaignService

class CCAIClient(private val config: CCAIConfig) {
    private val apiClient = ApiClient(config)

    val sms = SMSService(config, apiClient)
    val email = EmailService(config, apiClient)
    val webhook = WebhookService(config, apiClient)
    val mms = MMSService(config, apiClient)
    val contact = ContactService(config, apiClient)
    val brands = BrandService(config, apiClient)
    val campaigns = CampaignService(config, apiClient)
    val contactValidator = ContactValidatorService(config, apiClient)

    fun close() {
        // Cleanup resources if needed
    }
}
