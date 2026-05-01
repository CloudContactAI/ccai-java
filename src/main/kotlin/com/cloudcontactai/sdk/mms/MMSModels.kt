package com.cloudcontactai.sdk.mms

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

data class Account(
    val firstName: String,
    val lastName: String,
    val phone: String
){
    @JsonProperty("data")
    var customFields: Map<String, String> = emptyMap()

    @JsonProperty("messageData")
    var customData: String? = null

    constructor(firstName: String, lastName: String, phone: String, customFields: Map<String, String>) : this(firstName, lastName, phone) {
        this.customFields = customFields
    }

    constructor(firstName: String, lastName: String, phone: String, customData: String) : this(firstName, lastName, phone) {
        this.customData = customData
    }

    constructor(firstName: String, lastName: String, phone: String, customFields: Map<String, String>, customData: String) : this(firstName, lastName, phone) {
        this.customFields = customFields
        this.customData = customData
    }
}

data class MMSCampaign(
    val accounts: List<Account>,
    val message: String,
    val title: String,
    val pictureFileKey: String,
    val senderPhone: String? = null
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class MMSResponse(
    val success: Boolean? = null,
    val message: String? = null,
    val campaignId: String? = null,
    val sentCount: Int? = null,
    val failedCount: Int? = null,
    val failedNumbers: List<String>? = null,
    val timestamp: String? = null,
    val cost: Double? = null,
    val errorCode: String? = null,
    val id: String? = null,
    val responseId: String? = null
)

data class SignedUploadUrlRequest(
    val fileName: String,
    val fileType: String,
    val fileBasePath: String? = null,
    val publicFile: Boolean = false
)

data class SignedUploadUrlResponse(
    val signedS3Url: String,
    val fileKey: String? = null
)

data class StoredUrlResponse(
    val storedUrl: String
)
