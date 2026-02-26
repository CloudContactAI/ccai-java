package com.cloudcontactai.sdk.webhook

import com.cloudcontactai.sdk.common.CCAIConfig
import com.cloudcontactai.sdk.common.ApiClient
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class WebhookServiceEnhancedTest {
    
    private lateinit var config: CCAIConfig
    private lateinit var apiClient: ApiClient
    private lateinit var webhookService: WebhookService
    
    @BeforeEach
    fun setup() {
        config = CCAIConfig(
            clientId = "test-client",
            apiKey = "test-key"
        )
        apiClient = ApiClient(config)
        webhookService = WebhookService(config, apiClient)
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
}
