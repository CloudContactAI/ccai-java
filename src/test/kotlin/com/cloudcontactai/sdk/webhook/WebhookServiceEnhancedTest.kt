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
    fun `should parse webhook event`() {
        val payload = """
            {
                "eventType": "sms.sent",
                "timestamp": "2026-02-02T22:00:00Z",
                "campaignId": "campaign-123",
                "messageId": "msg-456",
                "phoneNumber": "+15551234567",
                "status": "sent",
                "cost": 0.01
            }
        """.trimIndent()
        
        val event = webhookService.parseWebhookEvent(payload)
        
        assertEquals("sms.sent", event.eventType)
        assertEquals("campaign-123", event.campaignId)
        assertEquals("msg-456", event.messageId)
        assertEquals("+15551234567", event.phoneNumber)
        assertEquals("sent", event.status)
        assertEquals(0.01, event.cost)
    }
}
