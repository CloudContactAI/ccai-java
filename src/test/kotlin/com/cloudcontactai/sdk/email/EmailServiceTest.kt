package com.cloudcontactai.sdk.email

import com.cloudcontactai.sdk.CCAIClient
import com.cloudcontactai.sdk.common.CCAIConfig
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class EmailServiceTest {
    
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
        
        val emailBaseUrlField = CCAIConfig::class.java.getDeclaredField("emailBaseUrl")
        emailBaseUrlField.isAccessible = true
        emailBaseUrlField.set(config, mockServer.url("/").toString().trimEnd('/'))
        
        client = CCAIClient(config)
    }
    
    @AfterEach
    fun tearDown() {
        mockServer.shutdown()
        client.close()
    }
    
    @Test
    fun `should send single email`() {
        val responseJson = """
            {
                "id": "email-123",
                "campaignId": "campaign-123",
                "status": "sent",
                "message": "Email sent successfully"
            }
        """.trimIndent()
        
        mockServer.enqueue(MockResponse()
            .setResponseCode(200)
            .setBody(responseJson)
            .addHeader("Content-Type", "application/json"))
        
        val response = client.email.sendSingle(
            firstName = "John",
            lastName = "Doe",
            email = "john@example.com",
            subject = "Test Subject",
            htmlContent = "<h1>Test</h1>"
        )
        
        assertEquals("email-123", response.id)
        assertEquals("sent", response.status)
    }
    
    @Test
    fun `should send bulk email`() {
        val responseJson = """
            {
                "id": "email-456",
                "campaignId": "campaign-456",
                "status": "sent",
                "message": "Emails sent successfully"
            }
        """.trimIndent()
        
        mockServer.enqueue(MockResponse()
            .setResponseCode(200)
            .setBody(responseJson)
            .addHeader("Content-Type", "application/json"))
        
        val accounts = listOf(
            EmailAccount("John", "Doe", "john@example.com"),
            EmailAccount("Jane", "Smith", "jane@example.com")
        )
        
        val response = client.email.send(
            accounts = accounts,
            subject = "Bulk Subject",
            htmlContent = "<h1>Bulk Email</h1>"
        )
        
        assertEquals("email-456", response.id)
        assertEquals("sent", response.status)
    }
    
    @Test
    fun `should get campaign status`() {
        val responseJson = """
            {
                "id": "campaign-123",
                "status": "completed",
                "totalEmails": 10,
                "sentEmails": 10,
                "failedEmails": 0
            }
        """.trimIndent()
        
        mockServer.enqueue(MockResponse()
            .setResponseCode(200)
            .setBody(responseJson)
            .addHeader("Content-Type", "application/json"))
        
        val status = client.email.getCampaignStatus("campaign-123")
        
        assertEquals("campaign-123", status.id)
        assertEquals("completed", status.status)
        assertEquals(10, status.sentEmails)
    }
}
