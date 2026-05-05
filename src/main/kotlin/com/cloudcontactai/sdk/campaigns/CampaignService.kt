package com.cloudcontactai.sdk.campaigns

import com.cloudcontactai.sdk.common.ApiClient
import com.cloudcontactai.sdk.common.CCAIConfig
import com.cloudcontactai.sdk.common.CCAIException

class CampaignService(private val config: CCAIConfig, private val apiClient: ApiClient) {

    companion object {
        private val CAMPAIGN_USE_CASES = setOf(
            "TWO_FACTOR_AUTHENTICATION", "ACCOUNT_NOTIFICATION", "CUSTOMER_CARE", "DELIVERY_NOTIFICATION",
            "FRAUD_ALERT", "HIGHER_EDUCATION", "LOW_VOLUME_MIXED", "MARKETING", "MIXED",
            "POLLING_VOTING", "PUBLIC_SERVICE_ANNOUNCEMENT", "SECURITY_ALERT"
        )
        private val CAMPAIGN_SUB_USE_CASES = setOf(
            "TWO_FACTOR_AUTHENTICATION", "ACCOUNT_NOTIFICATION", "CUSTOMER_CARE", "DELIVERY_NOTIFICATION",
            "FRAUD_ALERT", "MARKETING", "POLLING_VOTING"
        )
        private val MIXED_USE_CASES = setOf("MIXED", "LOW_VOLUME_MIXED")
    }

    fun create(data: CampaignRequest): CampaignResponse {
        validate(data, isCreate = true)
        return apiClient.request(
            method = "POST",
            endpoint = "/v1/campaigns",
            data = data,
            baseUrl = config.complianceBaseUrl,
            responseClass = CampaignResponse::class.java
        )
    }

    fun get(id: Long): CampaignResponse {
        return apiClient.request(
            method = "GET",
            endpoint = "/v1/campaigns/$id",
            baseUrl = config.complianceBaseUrl,
            responseClass = CampaignResponse::class.java
        )
    }

    fun list(): Array<CampaignResponse> {
        return apiClient.request(
            method = "GET",
            endpoint = "/v1/campaigns",
            baseUrl = config.complianceBaseUrl,
            responseClass = Array<CampaignResponse>::class.java
        )
    }

    fun update(id: Long, data: CampaignRequest): CampaignResponse {
        validate(data, isCreate = false)
        return apiClient.request(
            method = "PATCH",
            endpoint = "/v1/campaigns/$id",
            data = data,
            baseUrl = config.complianceBaseUrl,
            responseClass = CampaignResponse::class.java
        )
    }

    fun delete(id: Long) {
        apiClient.requestNoContent(
            method = "DELETE",
            endpoint = "/v1/campaigns/$id",
            baseUrl = config.complianceBaseUrl
        )
    }

    private fun validate(data: CampaignRequest, isCreate: Boolean) {
        val errors = mutableListOf<String>()

        if (isCreate) {
            if (data.brandId == null) errors.add("brandId is required")
            if (data.useCase.isNullOrBlank()) errors.add("useCase is required")
            if (data.description.isNullOrBlank()) errors.add("description is required")
            if (data.messageFlow.isNullOrBlank()) errors.add("messageFlow is required")
            if (data.hasEmbeddedLinks == null) errors.add("hasEmbeddedLinks is required")
            if (data.hasEmbeddedPhone == null) errors.add("hasEmbeddedPhone is required")
            if (data.isAgeGated == null) errors.add("isAgeGated is required")
            if (data.isDirectLending == null) errors.add("isDirectLending is required")
            if (data.optInKeywords.isNullOrEmpty()) errors.add("optInKeywords is required")
            if (data.optInMessage.isNullOrBlank()) errors.add("optInMessage is required")
            if (data.optInProofUrl.isNullOrBlank()) errors.add("optInProofUrl is required")
            if (data.helpKeywords.isNullOrEmpty()) errors.add("helpKeywords is required")
            if (data.helpMessage.isNullOrBlank()) errors.add("helpMessage is required")
            if (data.optOutKeywords.isNullOrEmpty()) errors.add("optOutKeywords is required")
            if (data.optOutMessage.isNullOrBlank()) errors.add("optOutMessage is required")
            if (data.sampleMessages.isNullOrEmpty()) errors.add("sampleMessages is required")
        }

        data.useCase?.let { if (it !in CAMPAIGN_USE_CASES) errors.add("Invalid use case") }

        // MIXED/LOW_VOLUME_MIXED sub-use case validation
        val useCase = data.useCase
        val subUseCases = data.subUseCases
        if (useCase != null && useCase in MIXED_USE_CASES) {
            if (subUseCases == null || subUseCases.size < 2 || subUseCases.size > 3) {
                errors.add("MIXED/LOW_VOLUME_MIXED requires 2-3 sub use cases")
            } else {
                subUseCases.forEach { if (it !in CAMPAIGN_SUB_USE_CASES) errors.add("Invalid sub use case: $it") }
            }
        } else if (useCase != null && !subUseCases.isNullOrEmpty()) {
            errors.add("subUseCases should be empty for non-MIXED use cases")
        }

        // sampleMessages count validation
        if (data.sampleMessages != null) {
            val msgs = data.sampleMessages!!
            if (msgs.size < 2 || msgs.size > 5) {
                errors.add("sampleMessages must contain 2-5 items")
            } else {
                val optOutKws = data.optOutKeywords ?: emptyList()
                val helpKws = data.helpKeywords ?: emptyList()

                val hasOptOut = msgs.any { msg ->
                    msg.contains("Reply STOP") || optOutKws.any { kw -> msg.contains("Reply $kw") }
                }
                if (!hasOptOut) errors.add("At least one sample must contain 'Reply STOP' or 'Reply {optOutKeyword}'")

                val hasHelp = msgs.any { msg ->
                    msg.contains("Reply HELP") || helpKws.any { kw -> msg.contains("Reply $kw") }
                }
                if (!hasHelp) errors.add("At least one sample must contain 'Reply HELP' or 'Reply {helpKeyword}'")
            }
        }

        // optOutMessage must contain STOP or an opt-out keyword
        if (data.optOutMessage != null) {
            val msg = data.optOutMessage!!
            val optOutKws = data.optOutKeywords ?: emptyList()
            val hasKeyword = optOutKws.any { kw -> msg.contains(kw) }
            if (!msg.contains("STOP") && !hasKeyword) {
                errors.add("optOutMessage must contain 'STOP' or at least one optOutKeyword")
            }
        }

        // helpMessage must contain HELP or a help keyword
        if (data.helpMessage != null) {
            val msg = data.helpMessage!!
            val helpKws = data.helpKeywords ?: emptyList()
            val hasKeyword = helpKws.any { kw -> msg.contains(kw) }
            if (!msg.contains("HELP") && !hasKeyword) {
                errors.add("helpMessage must contain 'HELP' or at least one helpKeyword")
            }
        }

        data.optInProofUrl?.let {
            if (!it.startsWith("http://") && !it.startsWith("https://")) errors.add("Opt-in proof URL must start with http:// or https://")
        }
        data.termsLink?.let {
            if (!it.startsWith("http://") && !it.startsWith("https://")) errors.add("Terms link must start with http:// or https://")
        }
        data.privacyLink?.let {
            if (!it.startsWith("http://") && !it.startsWith("https://")) errors.add("Privacy link must start with http:// or https://")
        }

        if (errors.isNotEmpty()) {
            throw CCAIException("Validation failed: ${errors.joinToString(", ")}")
        }
    }
}
