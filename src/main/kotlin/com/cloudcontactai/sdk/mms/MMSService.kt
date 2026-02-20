package com.cloudcontactai.sdk.mms

import com.cloudcontactai.sdk.common.ApiClient
import com.cloudcontactai.sdk.common.CCAIConfig
import com.cloudcontactai.sdk.common.CCAIException
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileInputStream
import java.security.MessageDigest

class MMSService(private val config: CCAIConfig, private val apiClient: ApiClient) {
    private val httpClient = OkHttpClient()
    private val objectMapper = jacksonObjectMapper()

    fun getSignedUploadUrl(request: SignedUploadUrlRequest): SignedUploadUrlResponse {
        val url = "${config.filesBaseUrl}/upload/url"
        
        // Use default fileBasePath if not provided
        val fileBasePath = request.fileBasePath ?: "${config.clientId}/campaign"
        
        // Construct the fileKey as clientId/campaign/filename
        val fileKey = "${config.clientId}/campaign/${request.fileName}"
        
        val requestData = SignedUploadUrlRequest(
            fileName = request.fileName,
            fileType = request.fileType,
            fileBasePath = fileBasePath,
            publicFile = request.publicFile
        )
        
        val jsonBody = objectMapper.writeValueAsString(requestData)
        val requestBody = jsonBody.toRequestBody("application/json".toMediaType())
        
        val httpRequest = Request.Builder()
            .url(url)
            .post(requestBody)
            .addHeader("Authorization", "Bearer ${config.apiKey}")
            .addHeader("Content-Type", "application/json")
            .build()

        httpClient.newCall(httpRequest).execute().use { response ->
            if (!response.isSuccessful) {
                throw CCAIException("HTTP ${response.code}: ${response.body?.string() ?: ""}")
            }
            
            val responseBody = response.body?.string() ?: throw CCAIException("Empty response body")
            val uploadResponse = objectMapper.readValue(responseBody, SignedUploadUrlResponse::class.java)
            
            // Override fileKey with our constructed one since API doesn't return it
            return uploadResponse.copy(fileKey = fileKey)
        }
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
            endpoint = "/clients/${config.clientId}/campaigns/direct",
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
        val md5Image = md5(imageFile)
        val extension = imageFile.extension.lowercase()
        val fileName = "${md5Image}.${extension}"
        val fileKey = "${config.clientId}/campaign/${fileName}"

        //Check if the same image has already been uploaded
        val storedUrlResponse = checkFileUploaded(fileKey)
        if(storedUrlResponse.storedUrl.isNotEmpty()){
            return send(accounts, message, title, fileKey, senderPhone)
        }

        val contentType = when (extension) {
            "jpg", "jpeg" -> "image/jpeg"
            "png" -> "image/png"
            "gif" -> "image/gif"
            else -> "image/jpeg"
        }
        val uploadRequest = SignedUploadUrlRequest(
            fileName = fileName,
            fileType = contentType,
            publicFile = true
        )
        val uploadResponse = getSignedUploadUrl(uploadRequest)
        uploadImageToSignedUrl(uploadResponse.signedS3Url, imageFile, contentType)
        return send(accounts, message, title, fileKey, senderPhone)
    }

    private fun md5(file: File): String {
        val digest = MessageDigest.getInstance("MD5")
        FileInputStream(file).use { fis ->
            val buffer = ByteArray(8192)
            var bytesRead: Int

            while (fis.read(buffer).also { bytesRead = it } != -1) {
                digest.update(buffer, 0, bytesRead)
            }
        }

        return digest.digest().joinToString("") { "%02x".format(it) }
    }

    fun checkFileUploaded(fileKey: String):StoredUrlResponse{
        return try {
            apiClient.request(
                method = "GET",
                endpoint = "/clients/${config.clientId}/storedUrl?fileKey=${fileKey}",
                responseClass = StoredUrlResponse::class.java
            )
        } catch (e: Exception) {
            e.printStackTrace()
            StoredUrlResponse("")
        }
    }
}
