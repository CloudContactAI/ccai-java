// Copyright (c) 2025 CloudContactAI LLC
// Licensed under the MIT License. See LICENSE in the project root for license information.

package com.cloudcontactai.ccai.sms;

import com.cloudcontactai.ccai.config.CCAIConfig;
import com.cloudcontactai.ccai.exception.CCAIApiException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Service for sending SMS messages via the CCAI API
 */
@Service
public class SMSService {

    private static final Logger logger = LoggerFactory.getLogger(SMSService.class);

    private final CCAIConfig config;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public SMSService(CCAIConfig config, RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.config = config;
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    /**
     * Sends an SMS message to a single phone number
     *
     * @param phoneNumber The phone number to send to
     * @param message The message content
     * @return SMSResponse containing the result
     * @throws CCAIApiException if the API call fails
     */
    public SMSResponse sendSMS(String phoneNumber, String message) throws CCAIApiException {
        return sendSMS(Collections.singletonList(phoneNumber), message);
    }

    /**
     * Sends an SMS message to multiple phone numbers
     *
     * @param phoneNumbers List of phone numbers to send to
     * @param message The message content
     * @return SMSResponse containing the result
     * @throws CCAIApiException if the API call fails
     */
    public SMSResponse sendSMS(List<String> phoneNumbers, String message) throws CCAIApiException {
        SMSRequest request = new SMSRequest(phoneNumbers, message);
        return sendSMS(request);
    }

    /**
     * Sends an SMS message using a complete SMSRequest object
     *
     * @param request The SMS request containing all parameters
     * @return SMSResponse containing the result
     * @throws CCAIApiException if the API call fails
     */
    public SMSResponse sendSMS(SMSRequest request) throws CCAIApiException {
        try {
            config.validate();

            String url = config.getBaseUrl() + "/sms/send";
            HttpHeaders headers = createHeaders();
            HttpEntity<SMSRequest> entity = new HttpEntity<>(request, headers);

            if (config.isDebugMode()) {
                logger.debug("Sending SMS request to: {}", url);
                logger.debug("Request payload: {}", objectMapper.writeValueAsString(request));
            }

            ResponseEntity<SMSResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    SMSResponse.class
            );

            SMSResponse smsResponse = response.getBody();
            if (smsResponse == null) {
                throw new CCAIApiException("Empty response from SMS API");
            }

            if (config.isDebugMode()) {
                logger.debug("SMS response: {}", objectMapper.writeValueAsString(smsResponse));
            }

            return smsResponse;

        } catch (RestClientException e) {
            logger.error("Failed to send SMS", e);
            throw new CCAIApiException("Failed to send SMS: " + e.getMessage(), e);
        } catch (Exception e) {
            logger.error("Unexpected error sending SMS", e);
            throw new CCAIApiException("Unexpected error: " + e.getMessage(), e);
        }
    }

    /**
     * Sends an SMS message asynchronously
     *
     * @param phoneNumbers List of phone numbers to send to
     * @param message The message content
     * @return CompletableFuture containing the SMSResponse
     */
    public CompletableFuture<SMSResponse> sendSMSAsync(List<String> phoneNumbers, String message) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return sendSMS(phoneNumbers, message);
            } catch (CCAIApiException e) {
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * Sends an SMS message asynchronously to a single phone number
     *
     * @param phoneNumber The phone number to send to
     * @param message The message content
     * @return CompletableFuture containing the SMSResponse
     */
    public CompletableFuture<SMSResponse> sendSMSAsync(String phoneNumber, String message) {
        return sendSMSAsync(Collections.singletonList(phoneNumber), message);
    }

    /**
     * Creates HTTP headers for API requests
     */
    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + config.getApiKey());
        headers.set("X-Client-ID", config.getClientId());
        headers.set("User-Agent", "CCAI-Java/1.0.0");
        return headers;
    }
}
