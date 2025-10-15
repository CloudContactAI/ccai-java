package com.cloudcontactai.ccai.common

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody

class ApiClient(config: CCAIConfig) {
    private val client = OkHttpClient()
    private val objectMapper = ObjectMapper().registerModule(KotlinModule.Builder().build())
    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()
    private val baseUrl = config.baseUrl
    private val apiKey = config.apiKey

    fun <T> request(
        method: String,
        endpoint: String,
        data: Any? = null,
        baseUrl: String? = null,
        headers: Map<String, String> = emptyMap(),
        responseClass: Class<T>
    ): T {
        val url = "${baseUrl ?: this.baseUrl}$endpoint"
        
        val requestBuilder = Request.Builder()
            .url(url)
            .addHeader("Authorization", "Bearer $apiKey")
            .addHeader("Accept", "application/json")
        
        headers.forEach { (key, value) ->
            requestBuilder.addHeader(key, value)
        }
        
        when (method.uppercase()) {
            "GET" -> requestBuilder.get()
            "POST" -> {
                val body = if (data != null) {
                    objectMapper.writeValueAsString(data).toRequestBody(jsonMediaType)
                } else {
                    "".toRequestBody(jsonMediaType)
                }
                requestBuilder.post(body)
            }
            "PUT" -> {
                val body = if (data != null) {
                    objectMapper.writeValueAsString(data).toRequestBody(jsonMediaType)
                } else {
                    "".toRequestBody(jsonMediaType)
                }
                requestBuilder.put(body)
            }
            "DELETE" -> requestBuilder.delete()
        }
        
        val request = requestBuilder.build()
        
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw CCAIException("HTTP ${response.code}: ${response.message}")
            }
            
            val responseBody = response.body?.string() ?: ""
            return objectMapper.readValue(responseBody, responseClass)
        }
    }
}

inline fun <reified T> ApiClient.request(
    method: String,
    endpoint: String,
    data: Any? = null,
    baseUrl: String? = null,
    headers: Map<String, String> = emptyMap()
): T = request(method, endpoint, data, baseUrl, headers, T::class.java)

class CCAIException(message: String, cause: Throwable? = null) : Exception(message, cause)
