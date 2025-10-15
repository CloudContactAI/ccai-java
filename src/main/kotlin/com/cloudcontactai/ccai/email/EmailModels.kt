package com.cloudcontactai.ccai.email

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class EmailAccount(
    @JsonProperty("firstName") val firstName: String,
    @JsonProperty("lastName") val lastName: String,
    @JsonProperty("email") val email: String,
    @JsonProperty("customAccountId") val customAccountId: String? = null,
    @JsonProperty("customFields") val customFields: Map<String, String> = emptyMap()
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class EmailCampaign(
    @JsonProperty("subject") val subject: String,
    @JsonProperty("title") val title: String,
    @JsonProperty("message") val message: String,
    @JsonProperty("senderEmail") val senderEmail: String,
    @JsonProperty("replyEmail") val replyEmail: String,
    @JsonProperty("senderName") val senderName: String,
    @JsonProperty("accounts") val accounts: List<EmailAccount>,
    @JsonProperty("campaignType") val campaignType: String = "EMAIL",
    @JsonProperty("addToList") val addToList: String = "noList",
    @JsonProperty("contactInput") val contactInput: String = "accounts",
    @JsonProperty("fromType") val fromType: String = "single",
    @JsonProperty("senders") val senders: List<Any> = emptyList()
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class EmailResponse(
    @JsonProperty("id") val id: String,
    @JsonProperty("campaignId") val campaignId: String? = null,
    @JsonProperty("status") val status: String,
    @JsonProperty("message") val message: String? = null
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class EmailCampaignStatus(
    @JsonProperty("id") val id: String,
    @JsonProperty("status") val status: String,
    @JsonProperty("totalEmails") val totalEmails: Int,
    @JsonProperty("sentEmails") val sentEmails: Int,
    @JsonProperty("failedEmails") val failedEmails: Int = 0
)
