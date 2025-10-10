// Copyright (c) 2025 CloudContactAI LLC
// Licensed under the MIT License. See LICENSE in the project root for license information.

package com.cloudcontactai.ccai.sms;

import com.cloudcontactai.ccai.config.CCAIConfig;
import com.cloudcontactai.ccai.exception.CCAIApiException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SMSServiceTest {

    @Mock
    private RestTemplate restTemplate;

    private SMSService smsService;
    private CCAIConfig config;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        config = new CCAIConfig("test-client-id", "test-api-key");
        config.setDebugMode(true);
        
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        
        smsService = new SMSService(config, restTemplate, objectMapper);
    }

    @Test
    void testSendSMSSingleNumber() throws CCAIApiException {
        // Arrange
        String phoneNumber = "+1234567890";
        String message = "Test message";
        
        SMSResponse expectedResponse = new SMSResponse();
        expectedResponse.setSuccess(true);
        expectedResponse.setMessage("SMS sent successfully");
        expectedResponse.setCampaignId("campaign-123");
        expectedResponse.setSentCount(1);
        expectedResponse.setFailedCount(0);
        expectedResponse.setTimestamp(LocalDateTime.now());
        expectedResponse.setCost(0.05);

        ResponseEntity<SMSResponse> responseEntity = new ResponseEntity<>(expectedResponse, HttpStatus.OK);

        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(SMSResponse.class)
        )).thenReturn(responseEntity);

        // Act
        SMSResponse actualResponse = smsService.sendSMS(phoneNumber, message);

        // Assert
        assertNotNull(actualResponse);
        assertTrue(actualResponse.isSuccess());
        assertEquals("SMS sent successfully", actualResponse.getMessage());
        assertEquals("campaign-123", actualResponse.getCampaignId());
        assertEquals(1, actualResponse.getSentCount());
        assertEquals(0, actualResponse.getFailedCount());
        assertEquals(0.05, actualResponse.getCost());
    }

    @Test
    void testSendSMSMultipleNumbers() throws CCAIApiException {
        // Arrange
        List<String> phoneNumbers = Arrays.asList("+1234567890", "+0987654321");
        String message = "Bulk test message";
        
        SMSResponse expectedResponse = new SMSResponse();
        expectedResponse.setSuccess(true);
        expectedResponse.setMessage("Bulk SMS sent successfully");
        expectedResponse.setCampaignId("bulk-campaign-456");
        expectedResponse.setSentCount(2);
        expectedResponse.setFailedCount(0);
        expectedResponse.setTimestamp(LocalDateTime.now());
        expectedResponse.setCost(0.10);

        ResponseEntity<SMSResponse> responseEntity = new ResponseEntity<>(expectedResponse, HttpStatus.OK);

        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(SMSResponse.class)
        )).thenReturn(responseEntity);

        // Act
        SMSResponse actualResponse = smsService.sendSMS(phoneNumbers, message);

        // Assert
        assertNotNull(actualResponse);
        assertTrue(actualResponse.isSuccess());
        assertEquals("Bulk SMS sent successfully", actualResponse.getMessage());
        assertEquals("bulk-campaign-456", actualResponse.getCampaignId());
        assertEquals(2, actualResponse.getSentCount());
        assertEquals(0, actualResponse.getFailedCount());
        assertEquals(0.10, actualResponse.getCost());
    }

    @Test
    void testSendSMSWithRequest() throws CCAIApiException {
        // Arrange
        SMSRequest request = new SMSRequest();
        request.setPhoneNumbers(Arrays.asList("+1234567890"));
        request.setMessage("Custom request message");
        request.setCampaignId("custom-campaign");
        
        SMSResponse expectedResponse = new SMSResponse();
        expectedResponse.setSuccess(true);
        expectedResponse.setMessage("Custom SMS sent successfully");
        expectedResponse.setCampaignId("custom-campaign");
        expectedResponse.setSentCount(1);
        expectedResponse.setFailedCount(0);
        expectedResponse.setTimestamp(LocalDateTime.now());

        ResponseEntity<SMSResponse> responseEntity = new ResponseEntity<>(expectedResponse, HttpStatus.OK);

        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(SMSResponse.class)
        )).thenReturn(responseEntity);

        // Act
        SMSResponse actualResponse = smsService.sendSMS(request);

        // Assert
        assertNotNull(actualResponse);
        assertTrue(actualResponse.isSuccess());
        assertEquals("Custom SMS sent successfully", actualResponse.getMessage());
        assertEquals("custom-campaign", actualResponse.getCampaignId());
        assertEquals(1, actualResponse.getSentCount());
        assertEquals(0, actualResponse.getFailedCount());
    }

    @Test
    void testSendSMSAsync() throws Exception {
        // Arrange
        String phoneNumber = "+1234567890";
        String message = "Async test message";
        
        SMSResponse expectedResponse = new SMSResponse();
        expectedResponse.setSuccess(true);
        expectedResponse.setMessage("Async SMS sent successfully");
        expectedResponse.setCampaignId("async-campaign-789");
        expectedResponse.setSentCount(1);
        expectedResponse.setFailedCount(0);
        expectedResponse.setTimestamp(LocalDateTime.now());

        ResponseEntity<SMSResponse> responseEntity = new ResponseEntity<>(expectedResponse, HttpStatus.OK);

        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(SMSResponse.class)
        )).thenReturn(responseEntity);

        // Act
        SMSResponse actualResponse = smsService.sendSMSAsync(phoneNumber, message).get();

        // Assert
        assertNotNull(actualResponse);
        assertTrue(actualResponse.isSuccess());
        assertEquals("Async SMS sent successfully", actualResponse.getMessage());
        assertEquals("async-campaign-789", actualResponse.getCampaignId());
        assertEquals(1, actualResponse.getSentCount());
        assertEquals(0, actualResponse.getFailedCount());
    }

    @Test
    void testSendSMSWithInvalidConfig() {
        // Arrange
        CCAIConfig invalidConfig = new CCAIConfig();
        invalidConfig.setClientId("");
        invalidConfig.setApiKey("");
        
        SMSService invalidSmsService = new SMSService(invalidConfig, restTemplate, objectMapper);

        // Act & Assert
        assertThrows(CCAIApiException.class, () -> {
            invalidSmsService.sendSMS("+1234567890", "Test message");
        });
    }
}
