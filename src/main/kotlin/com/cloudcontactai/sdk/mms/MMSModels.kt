package com.cloudcontactai.sdk.mms

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

data class Account(
    val firstName: String,
    val lastName: String,
    val phone: String
)

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
    val id: String? = null
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
