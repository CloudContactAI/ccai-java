package com.cloudcontactai.sdk.sms

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class Account(
    @JsonProperty("firstName") val firstName: String,
    @JsonProperty("lastName") val lastName: String,
    @JsonProperty("phone") val phone: String,
    @JsonProperty("customFields") val customFields: Map<String, String> = emptyMap()
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class SMSCampaign(
    @JsonProperty("accounts") val accounts: List<Account>,
    @JsonProperty("message") val message: String,
    @JsonProperty("title") val title: String,
    @JsonProperty("senderPhone") val senderPhone: String? = null
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class SMSResponse(
    @JsonProperty("id") val id: String,
    @JsonProperty("campaignId") val campaignId: String? = null,
    @JsonProperty("status") val status: String? = null,
    @JsonProperty("message") val message: String? = null,
    @JsonProperty("responseId") val responseId: String? = null
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class SMSCampaignStatus(
    @JsonProperty("id") val id: String,
    @JsonProperty("status") val status: String,
    @JsonProperty("totalMessages") val totalMessages: Int,
    @JsonProperty("sentMessages") val sentMessages: Int,
    @JsonProperty("failedMessages") val failedMessages: Int = 0
)
