package com.cloudcontactai.sdk.sms

import com.cloudcontactai.sdk.CCAIClient
import com.cloudcontactai.sdk.common.CCAIConfig
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class SMSServiceTest {
    
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
    fun `should send single SMS`() {
        val responseJson = """
            {
                "id": "msg-123",
                "campaignId": "campaign-123",
                "status": "sent",
                "message": "SMS sent successfully",
                "responseId": "resp-123"
            }
        """.trimIndent()
        
        mockServer.enqueue(MockResponse()
            .setResponseCode(200)
            .setBody(responseJson)
            .addHeader("Content-Type", "application/json"))
        
        val response = client.sms.sendSingle(
            firstName = "John",
            lastName = "Doe",
            phone = "+15551234567",
            message = "Test message",
            title = "Test Campaign"
        )
        
        assertEquals("campaign-123", response.campaignId)
        assertEquals("msg-123", response.id)
        assertEquals("sent", response.status)
        assertEquals("resp-123", response.responseId)
    }
    
    @Test
    fun `should send bulk SMS`() {
        val responseJson = """
            {
                "id": "msg-456",
                "campaignId": "campaign-456",
                "status": "sent",
                "message": "Bulk SMS sent successfully",
                "responseId": "resp-456"
            }
        """.trimIndent()
        
        mockServer.enqueue(MockResponse()
            .setResponseCode(200)
            .setBody(responseJson)
            .addHeader("Content-Type", "application/json"))
        
        val accounts = listOf(
            Account("John", "Doe", "+15551234567"),
            Account("Jane", "Smith", "+15559876543")
        )
        
        val response = client.sms.send(
            accounts = accounts,
            message = "Bulk message",
            title = "Bulk Campaign"
        )
        
        assertEquals("campaign-456", response.campaignId)
        assertEquals("msg-456", response.id)
        assertEquals("resp-456", response.responseId)
    }
    
    @Test
    fun `should get campaign status`() {
        val responseJson = """
            {
                "id": "campaign-123",
                "status": "completed",
                "totalMessages": 10,
                "sentMessages": 10,
                "failedMessages": 0
            }
        """.trimIndent()
        
        mockServer.enqueue(MockResponse()
            .setResponseCode(200)
            .setBody(responseJson)
            .addHeader("Content-Type", "application/json"))
        
        val status = client.sms.getCampaignStatus("campaign-123")
        
        assertEquals("campaign-123", status.id)
        assertEquals("completed", status.status)
        assertEquals(10, status.sentMessages)
    }
}
