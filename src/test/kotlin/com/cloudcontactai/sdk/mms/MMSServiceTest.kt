package com.cloudcontactai.sdk.mms

import com.cloudcontactai.sdk.CCAIClient
import com.cloudcontactai.sdk.common.CCAIConfig
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

class MMSServiceTest {
    
    private lateinit var mockServer: MockWebServer
    private lateinit var client: CCAIClient
    
    @BeforeEach
    fun setup() {
        mockServer = MockWebServer()
        mockServer.start()
        
        val config = CCAIConfig(
            clientId = "test-client",
            apiKey = "test-key"
        )
        
        val baseUrlField = CCAIConfig::class.java.getDeclaredField("baseUrl")
        baseUrlField.isAccessible = true
        baseUrlField.set(config, mockServer.url("/").toString().trimEnd('/'))
        
        client = CCAIClient(config)
    }
    
    @AfterEach
    fun tearDown() {
        mockServer.shutdown()
        client.close()
    }
    
    @Test
    @Disabled("Requires mocking files API endpoint separately")
    fun `should get signed upload URL`() {
        val responseJson = """
            {
                "signedS3Url": "https://s3.amazonaws.com/test-bucket/test-file?signature=abc123",
                "fileKey": "uploads/test-file.jpg"
            }
        """.trimIndent()
        
        mockServer.enqueue(MockResponse()
            .setResponseCode(200)
            .setBody(responseJson)
            .addHeader("Content-Type", "application/json"))
        
        val request = SignedUploadUrlRequest(
            fileName = "test-file.jpg",
            fileType = "image/jpeg"
        )
        
        val response = client.mms.getSignedUploadUrl(request)
        
        assertEquals("uploads/test-file.jpg", response.fileKey)
        assertTrue(response.signedS3Url.contains("s3.amazonaws.com"))
    }
    
    @Test
    fun `should send MMS with picture file key`() {
        val responseJson = """
            {
                "success": true,
                "message": "MMS sent",
                "campaignId": "mms-campaign-123",
                "sentCount": 1,
                "failedCount": 0,
                "failedNumbers": null,
                "timestamp": "2026-02-02T22:00:00Z",
                "cost": 0.05,
                "errorCode": null
            }
        """.trimIndent()
        
        mockServer.enqueue(MockResponse()
            .setResponseCode(200)
            .setBody(responseJson)
            .addHeader("Content-Type", "application/json"))
        
        val accounts = listOf(
            Account("John", "Doe", "+15551234567")
        )
        
        val response = client.mms.send(
            accounts = accounts,
            message = "Check this out!",
            title = "MMS Test",
            pictureFileKey = "uploads/test.jpg"
        )
        
        assertEquals("mms-campaign-123", response.campaignId)
        assertEquals(1, response.sentCount)
        assertTrue(response.success == true)
    }
    
    @Test
    fun `should send single MMS`() {
        val responseJson = """
            {
                "success": true,
                "message": "MMS sent",
                "campaignId": "mms-single-123",
                "sentCount": 1,
                "failedCount": 0,
                "failedNumbers": null,
                "timestamp": "2026-02-02T22:00:00Z",
                "cost": 0.05,
                "errorCode": null
            }
        """.trimIndent()
        
        mockServer.enqueue(MockResponse()
            .setResponseCode(200)
            .setBody(responseJson)
            .addHeader("Content-Type", "application/json"))
        
        val response = client.mms.sendSingle(
            firstName = "Jane",
            lastName = "Smith",
            phone = "+15559876543",
            message = "Hello!",
            title = "Single MMS",
            pictureFileKey = "uploads/image.jpg"
        )
        
        assertEquals("mms-single-123", response.campaignId)
        assertTrue(response.success == true)
    }
}
