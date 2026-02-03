package com.cloudcontactai.sdk.common

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class CCAIConfigEnhancedTest {
    
    @Test
    fun `should create config with debug mode`() {
        val config = CCAIConfig(
            clientId = "test-client",
            apiKey = "test-key",
            debugMode = true
        )
        
        assertTrue(config.debugMode)
    }
    
    @Test
    fun `should create config with custom max retries`() {
        val config = CCAIConfig(
            clientId = "test-client",
            apiKey = "test-key",
            maxRetries = 5
        )
        
        assertEquals(5, config.maxRetries)
    }
    
    @Test
    fun `should create config with custom timeout`() {
        val config = CCAIConfig(
            clientId = "test-client",
            apiKey = "test-key",
            timeoutMs = 60000
        )
        
        assertEquals(60000, config.timeoutMs)
    }
    
    @Test
    fun `should throw exception for negative max retries`() {
        val exception = assertThrows<IllegalArgumentException> {
            CCAIConfig(
                clientId = "test-client",
                apiKey = "test-key",
                maxRetries = -1
            )
        }
        
        assertEquals("Max retries must be non-negative", exception.message)
    }
    
    @Test
    fun `should throw exception for zero or negative timeout`() {
        val exception = assertThrows<IllegalArgumentException> {
            CCAIConfig(
                clientId = "test-client",
                apiKey = "test-key",
                timeoutMs = 0
            )
        }
        
        assertEquals("Timeout must be positive", exception.message)
    }
    
    @Test
    fun `should use default values for optional parameters`() {
        val config = CCAIConfig(
            clientId = "test-client",
            apiKey = "test-key"
        )
        
        assertFalse(config.debugMode)
        assertEquals(3, config.maxRetries)
        assertEquals(30000, config.timeoutMs)
    }
}
