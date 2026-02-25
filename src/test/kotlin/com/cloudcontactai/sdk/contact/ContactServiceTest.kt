package com.cloudcontactai.sdk.contact

import com.cloudcontactai.sdk.CCAIClient
import com.cloudcontactai.sdk.common.CCAIConfig
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class ContactServiceTest {
    
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
        
        val contactBaseUrlField = CCAIConfig::class.java.getDeclaredField("contactBaseUrl")
        contactBaseUrlField.isAccessible = true
        contactBaseUrlField.set(config, mockServer.url("/").toString().trimEnd('/'))
        
        client = CCAIClient(config)
    }
    
    @AfterEach
    fun tearDown() {
        mockServer.shutdown()
        client.close()
    }
    
    @Test
    fun `should set do not text by contact ID`() {
        val responseJson = """
            {
                "contactId": "613b086b5d7d4dee0723f7f6",
                "phone": "+15551234567",
                "doNotText": true
            }
        """.trimIndent()
        
        mockServer.enqueue(MockResponse()
            .setResponseCode(200)
            .setBody(responseJson)
            .addHeader("Content-Type", "application/json"))
        
        val response = client.contact.setDoNotText(
            contactId = "613b086b5d7d4dee0723f7f6",
            doNotText = true
        )
        
        assertEquals("613b086b5d7d4dee0723f7f6", response.contactId)
        assertEquals("+15551234567", response.phone)
        assertTrue(response.doNotText)
    }
    
    @Test
    fun `should set do not text by phone`() {
        val responseJson = """
            {
                "contactId": "613b086b5d7d4dee0723f7f6",
                "phone": "+12345678901",
                "doNotText": true
            }
        """.trimIndent()
        
        mockServer.enqueue(MockResponse()
            .setResponseCode(200)
            .setBody(responseJson)
            .addHeader("Content-Type", "application/json"))
        
        val response = client.contact.setDoNotText(
            phone = "+12345678901",
            doNotText = true
        )
        
        assertEquals("613b086b5d7d4dee0723f7f6", response.contactId)
        assertEquals("+12345678901", response.phone)
        assertTrue(response.doNotText)
    }
    
    @Test
    fun `should remove do not text flag`() {
        val responseJson = """
            {
                "contactId": "613b086b5d7d4dee0723f7f6",
                "phone": "+15551234567",
                "doNotText": false
            }
        """.trimIndent()
        
        mockServer.enqueue(MockResponse()
            .setResponseCode(200)
            .setBody(responseJson)
            .addHeader("Content-Type", "application/json"))
        
        val response = client.contact.setDoNotText(
            contactId = "613b086b5d7d4dee0723f7f6",
            doNotText = false
        )
        
        assertEquals("613b086b5d7d4dee0723f7f6", response.contactId)
        assertEquals("+15551234567", response.phone)
        assertFalse(response.doNotText)
    }
}