package com.cloudcontactai.sdk.mms

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

data class MMSResponse(
    val success: Boolean,
    val message: String,
    val campaignId: String,
    val sentCount: Int,
    val failedCount: Int,
    val failedNumbers: List<String>?,
    val timestamp: String,
    val cost: Double?,
    val errorCode: String?
)

data class SignedUploadUrlRequest(
    val fileName: String,
    val fileType: String,
    val fileBasePath: String? = null,
    val publicFile: Boolean = false
)

data class SignedUploadUrlResponse(
    val signedS3Url: String,
    val fileKey: String
)
