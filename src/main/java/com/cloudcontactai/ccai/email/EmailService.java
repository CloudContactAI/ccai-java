// Copyright (c) 2025 CloudContactAI LLC
// Licensed under the MIT License. See LICENSE in the project root for license information.

package com.cloudcontactai.ccai.email;

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
 * Service for sending email messages via the CCAI API
 */
@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    private final CCAIConfig config;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public EmailService(CCAIConfig config, RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.config = config;
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    /**
     * Sends an email to a single recipient
     *
     * @param toEmail The recipient email address
     * @param subject The email subject
     * @param htmlContent The HTML content of the email
     * @return EmailResponse containing the result
     * @throws CCAIApiException if the API call fails
     */
    public EmailResponse sendEmail(String toEmail, String subject, String htmlContent) throws CCAIApiException {
        return sendEmail(Collections.singletonList(toEmail), subject, htmlContent);
    }

    /**
     * Sends an email to multiple recipients
     *
     * @param toEmails List of recipient email addresses
     * @param subject The email subject
     * @param htmlContent The HTML content of the email
     * @return EmailResponse containing the result
     * @throws CCAIApiException if the API call fails
     */
    public EmailResponse sendEmail(List<String> toEmails, String subject, String htmlContent) throws CCAIApiException {
        EmailRequest request = new EmailRequest(toEmails, subject, htmlContent);
        return sendEmail(request);
    }

    /**
     * Sends an email using a complete EmailRequest object
     *
     * @param request The email request containing all parameters
     * @return EmailResponse containing the result
     * @throws CCAIApiException if the API call fails
     */
    public EmailResponse sendEmail(EmailRequest request) throws CCAIApiException {
        try {
            config.validate();

            String url = config.getEmailBaseUrl() + "/api/email/send";
            HttpHeaders headers = createHeaders();
            HttpEntity<EmailRequest> entity = new HttpEntity<>(request, headers);

            if (config.isDebugMode()) {
                logger.debug("Sending email request to: {}", url);
                logger.debug("Request payload: {}", objectMapper.writeValueAsString(request));
            }

            ResponseEntity<EmailResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    EmailResponse.class
            );

            EmailResponse emailResponse = response.getBody();
            if (emailResponse == null) {
                throw new CCAIApiException("Empty response from Email API");
            }

            if (config.isDebugMode()) {
                logger.debug("Email response: {}", objectMapper.writeValueAsString(emailResponse));
            }

            return emailResponse;

        } catch (RestClientException e) {
            logger.error("Failed to send email", e);
            throw new CCAIApiException("Failed to send email: " + e.getMessage(), e);
        } catch (Exception e) {
            logger.error("Unexpected error sending email", e);
            throw new CCAIApiException("Unexpected error: " + e.getMessage(), e);
        }
    }

    /**
     * Sends an email asynchronously
     *
     * @param toEmails List of recipient email addresses
     * @param subject The email subject
     * @param htmlContent The HTML content of the email
     * @return CompletableFuture containing the EmailResponse
     */
    public CompletableFuture<EmailResponse> sendEmailAsync(List<String> toEmails, String subject, String htmlContent) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return sendEmail(toEmails, subject, htmlContent);
            } catch (CCAIApiException e) {
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * Sends an email asynchronously to a single recipient
     *
     * @param toEmail The recipient email address
     * @param subject The email subject
     * @param htmlContent The HTML content of the email
     * @return CompletableFuture containing the EmailResponse
     */
    public CompletableFuture<EmailResponse> sendEmailAsync(String toEmail, String subject, String htmlContent) {
        return sendEmailAsync(Collections.singletonList(toEmail), subject, htmlContent);
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
