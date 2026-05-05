package com.cloudcontactai.sdk.campaigns

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

data class CampaignRequest(
    val brandId: Long? = null,
    val useCase: String? = null,
    val subUseCases: List<String>? = null,
    val description: String? = null,
    val messageFlow: String? = null,
    val termsLink: String? = null,
    val privacyLink: String? = null,
    val hasEmbeddedLinks: Boolean? = null,
    val hasEmbeddedPhone: Boolean? = null,
    val isAgeGated: Boolean? = null,
    val isDirectLending: Boolean? = null,
    val optInKeywords: List<String>? = null,
    val optInMessage: String? = null,
    val optInProofUrl: String? = null,
    val helpKeywords: List<String>? = null,
    val helpMessage: String? = null,
    val optOutKeywords: List<String>? = null,
    val optOutMessage: String? = null,
    val sampleMessages: List<String>? = null
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class CampaignResponse(
    val id: Long = 0,
    val accountId: Long = 0,
    val brandId: Long = 0,
    val useCase: String = "",
    val subUseCases: List<String> = emptyList(),
    val description: String = "",
    val messageFlow: String = "",
    val termsLink: String? = null,
    val privacyLink: String? = null,
    val hasEmbeddedLinks: Boolean = false,
    val hasEmbeddedPhone: Boolean = false,
    val isAgeGated: Boolean = false,
    val isDirectLending: Boolean = false,
    val optInKeywords: List<String> = emptyList(),
    val optInMessage: String = "",
    val optInProofUrl: String = "",
    val helpKeywords: List<String> = emptyList(),
    val helpMessage: String = "",
    val optOutKeywords: List<String> = emptyList(),
    val optOutMessage: String = "",
    val sampleMessages: List<String> = emptyList(),
    val monthlyFee: Double = 20.00,
    val createdAt: String = "",
    val updatedAt: String = ""
)
