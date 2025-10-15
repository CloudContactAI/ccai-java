// Copyright (c) 2025 CloudContactAI LLC
// Licensed under the MIT License. See LICENSE in the project root for license information.

package com.cloudcontactai.sdk

import com.cloudcontactai.sdk.common.CCAIConfig
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertNotNull

class CCAIClientTest {
    
    @Test
    fun `should create client with valid config`() {
        val config = CCAIConfig(
            clientId = "test-client-id",
            apiKey = "test-api-key"
        )
        
        val client = CCAIClient(config)
        
        assertNotNull(client.sms)
        assertNotNull(client.email)
        assertNotNull(client.webhook)
        
        client.close()
    }
    
    @Test
    fun `should throw exception for blank client id`() {
        assertThrows<IllegalArgumentException> {
            CCAIConfig(
                clientId = "",
                apiKey = "test-api-key"
            )
        }
    }
    
    @Test
    fun `should throw exception for blank api key`() {
        assertThrows<IllegalArgumentException> {
            CCAIConfig(
                clientId = "test-client-id",
                apiKey = ""
            )
        }
    }
}
