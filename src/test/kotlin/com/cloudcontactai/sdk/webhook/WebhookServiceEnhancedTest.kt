package com.cloudcontactai.sdk.webhook

import com.cloudcontactai.sdk.CCAIClient
import com.cloudcontactai.sdk.common.CCAIConfig
import com.cloudcontactai.sdk.common.ApiClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class WebhookServiceEnhancedTest {
    
    private lateinit var config: CCAIConfig
    private lateinit var apiClient: ApiClient
    private lateinit var webhookService: WebhookService
    private lateinit var mockServer: MockWebServer
    private lateinit var client: CCAIClient
    
    @BeforeEach
    fun setup() {
        mockServer = MockWebServer()
        mockServer.start()
        
        config = CCAIConfig(
            clientId = "test-client",
            apiKey = "test-key"
        )
        
        val baseUrlField = CCAIConfig::class.java.getDeclaredField("baseUrl")
        baseUrlField.isAccessible = true
        baseUrlField.set(config, mockServer.url("/").toString().trimEnd('/'))
        
        apiClient = ApiClient(config)
        webhookService = WebhookService(config, apiClient)
        client = CCAIClient(config)
    }
    
    @AfterEach
    fun tearDown() {
        mockServer.shutdown()
        client.close()
    }
    
    @Test
    fun `should parse webhook event with new structure`() {
        val payload = """
            {
                "eventType": "sms.sent",
                "data": {
                    "id": 12345,
                    "MessageStatus": "sent",
                    "To": "+15551234567",
                    "Message": "Hello World",
                    "CustomData": "order-123",
                    "CampaignId": 789
                },
                "eventHash": "abc123def456ghi789jkl012mno345pq"
            }
        """.trimIndent()
        
        val event = webhookService.parseWebhookEvent(payload)
        
        assertEquals("sms.sent", event.eventType)
        assertEquals("abc123def456ghi789jkl012mno345pq", event.eventHash)
        assertNotNull(event.data)
        assertEquals(12345, (event.data["id"] as Number).toInt())
        assertEquals("sent", event.data["MessageStatus"])
        assertEquals("+15551234567", event.data["To"])
    }
    
    @Test
    fun `should validate signature with eventHash correctly`() {
        val clientId = 12345L
        val eventHash = "abc123def456ghi789jkl012mno345pq"
        val secret = "my-secret-key"
        
        val signature = webhookService.generateSignature(secret, clientId, eventHash)
        val isValid = webhookService.validateSignature(signature, secret, clientId, eventHash)
        
        assertTrue(isValid)
    }
    
    @Test
    fun `should reject invalid signature with eventHash`() {
        val clientId = 12345L
        val eventHash = "abc123def456ghi789jkl012mno345pq"
        val secret = "my-secret-key"
        val invalidSignature = "invalid-signature-12345"
        
        val isValid = webhookService.validateSignature(invalidSignature, secret, clientId, eventHash)
        
        assertFalse(isValid)
    }
    
    @Test
    fun `should generate consistent signatures`() {
        val clientId = 12345L
        val eventHash = "test-hash-123"
        val secret = "secret-key"
        
        val signature1 = webhookService.generateSignature(secret, clientId, eventHash)
        val signature2 = webhookService.generateSignature(secret, clientId, eventHash)
        
        assertEquals(signature1, signature2)
    }
    
    @Test
    fun `should create webhook`() {
        val responseJson = """
            [{
                "id": 123,
                "url": "https://example.com/webhook",
                "method": "POST",
                "integrationType": "ALL",
                "secretKey": "secret-key-123"
            }]
        """.trimIndent()
        
        mockServer.enqueue(MockResponse()
            .setResponseCode(200)
            .setBody(responseJson)
            .addHeader("Content-Type", "application/json"))
        
        val request = WebhookRequest("https://example.com/webhook")
        val response = client.webhook.create(request)
        
        assertEquals(123L, response.id)
        assertEquals("https://example.com/webhook", response.url)
        assertEquals("POST", response.method)
        assertEquals("ALL", response.integrationType)
        assertEquals("secret-key-123", response.secretKey)
    }
    
    @Test
    fun `should update webhook`() {
        val responseJson = """
            [{
                "id": 123,
                "url": "https://example.com/webhook-updated",
                "method": "POST",
                "integrationType": "ALL",
                "secretKey": "new-secret-key"
            }]
        """.trimIndent()
        
        mockServer.enqueue(MockResponse()
            .setResponseCode(200)
            .setBody(responseJson)
            .addHeader("Content-Type", "application/json"))
        
        val request = WebhookUpdateRequest(123L, "https://example.com/webhook-updated", "new-secret-key")
        val response = client.webhook.update(request)
        
        assertEquals(123L, response.id)
        assertEquals("https://example.com/webhook-updated", response.url)
        assertEquals("ALL", response.integrationType)
        assertEquals("new-secret-key", response.secretKey)
    }
    
    @Test
    fun `should delete webhook`() {
        val responseJson = """
            {
                "id": 123,
                "url": "https://example.com/webhook",
                "method": "POST",
                "integrationType": "ALL",
                "secretKey": null
            }
        """.trimIndent()
        
        mockServer.enqueue(MockResponse()
            .setResponseCode(200)
            .setBody(responseJson)
            .addHeader("Content-Type", "application/json"))
        
        val response = client.webhook.delete(123L)
        
        assertEquals(123L, response.id)
        assertEquals("https://example.com/webhook", response.url)
        assertNull(response.secretKey)
    }
}
