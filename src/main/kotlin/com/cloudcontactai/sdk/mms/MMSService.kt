package com.cloudcontactai.sdk.mms

import com.cloudcontactai.sdk.common.ApiClient
import com.cloudcontactai.sdk.common.CCAIConfig
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

class MMSService(private val config: CCAIConfig, private val apiClient: ApiClient) {
    private val httpClient = OkHttpClient()

    fun getSignedUploadUrl(request: SignedUploadUrlRequest): SignedUploadUrlResponse {
        return apiClient.request(
            method = "POST",
            endpoint = "/clients/${config.clientId}/files/signed-upload-url",
            data = request,
            responseClass = SignedUploadUrlResponse::class.java
        )
    }

    fun uploadImageToSignedUrl(signedUrl: String, imageFile: File, contentType: String = "image/jpeg") {
        val mediaType = contentType.toMediaType()
        val requestBody = imageFile.readBytes().toRequestBody(mediaType)
        
        val request = Request.Builder()
            .url(signedUrl)
            .put(requestBody)
            .addHeader("Content-Type", contentType)
            .build()

        httpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw RuntimeException("Failed to upload image: ${response.code} ${response.message}")
            }
        }
    }

    fun send(
        accounts: List<Account>,
        message: String,
        title: String,
        pictureFileKey: String,
        senderPhone: String? = null
    ): MMSResponse {
        val campaign = MMSCampaign(
            accounts = accounts,
            message = message,
            title = title,
            pictureFileKey = pictureFileKey,
            senderPhone = senderPhone
        )

        return apiClient.request(
            method = "POST",
            endpoint = "/clients/${config.clientId}/campaigns/mms",
            data = campaign,
            responseClass = MMSResponse::class.java
        )
    }

    fun sendSingle(
        firstName: String,
        lastName: String,
        phone: String,
        message: String,
        title: String,
        pictureFileKey: String,
        senderPhone: String? = null
    ): MMSResponse {
        val account = Account(firstName, lastName, phone)
        return send(listOf(account), message, title, pictureFileKey, senderPhone)
    }

    fun sendWithImage(
        accounts: List<Account>,
        message: String,
        title: String,
        imageFile: File,
        senderPhone: String? = null
    ): MMSResponse {
        val extension = imageFile.extension.lowercase()
        val contentType = when (extension) {
            "jpg", "jpeg" -> "image/jpeg"
            "png" -> "image/png"
            "gif" -> "image/gif"
            else -> "image/jpeg"
        }

        val uploadRequest = SignedUploadUrlRequest(
            fileName = imageFile.name,
            fileType = contentType
        )

        val uploadResponse = getSignedUploadUrl(uploadRequest)
        uploadImageToSignedUrl(uploadResponse.signedS3Url, imageFile, contentType)

        return send(accounts, message, title, uploadResponse.fileKey, senderPhone)
    }
}
