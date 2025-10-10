// Copyright (c) 2025 CloudContactAI LLC
// Licensed under the MIT License. See LICENSE in the project root for license information.

package com.cloudcontactai.ccai.webhook;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

/**
 * Service for handling webhook events from the CCAI API
 */
@Service
public class WebhookService {

    private static final Logger logger = LoggerFactory.getLogger(WebhookService.class);
    private static final String HMAC_SHA256 = "HmacSHA256";

    private final ObjectMapper objectMapper;

    public WebhookService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * Parses a webhook payload into a WebhookEvent object
     *
     * @param payload The JSON payload from the webhook
     * @return WebhookEvent object
     * @throws Exception if parsing fails
     */
    public WebhookEvent parseWebhookEvent(String payload) throws Exception {
        try {
            return objectMapper.readValue(payload, WebhookEvent.class);
        } catch (Exception e) {
            logger.error("Failed to parse webhook event: {}", payload, e);
            throw new Exception("Failed to parse webhook event", e);
        }
    }

    /**
     * Validates the webhook signature using HMAC-SHA256
     *
     * @param payload The raw webhook payload
     * @param signature The signature from the webhook headers
     * @param secret The webhook secret for validation
     * @return true if the signature is valid, false otherwise
     */
    public boolean validateWebhookSignature(String payload, String signature, String secret) {
        try {
            if (payload == null || signature == null || secret == null) {
                logger.warn("Missing required parameters for webhook validation");
                return false;
            }

            // Remove 'sha256=' prefix if present
            String cleanSignature = signature.startsWith("sha256=") ? signature.substring(7) : signature;

            // Calculate expected signature
            String expectedSignature = calculateHmacSha256(payload, secret);

            // Compare signatures using constant-time comparison
            return constantTimeEquals(cleanSignature, expectedSignature);

        } catch (Exception e) {
            logger.error("Error validating webhook signature", e);
            return false;
        }
    }

    /**
     * Calculates HMAC-SHA256 signature for the given payload and secret
     *
     * @param payload The payload to sign
     * @param secret The secret key
     * @return The calculated signature as a hex string
     * @throws NoSuchAlgorithmException if HMAC-SHA256 is not available
     * @throws InvalidKeyException if the secret key is invalid
     */
    private String calculateHmacSha256(String payload, String secret) throws NoSuchAlgorithmException, InvalidKeyException {
        Mac mac = Mac.getInstance(HMAC_SHA256);
        SecretKeySpec secretKeySpec = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), HMAC_SHA256);
        mac.init(secretKeySpec);
        
        byte[] hash = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
        return bytesToHex(hash);
    }

    /**
     * Converts byte array to hex string
     *
     * @param bytes The byte array
     * @return Hex string representation
     */
    private String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }

    /**
     * Constant-time string comparison to prevent timing attacks
     *
     * @param a First string
     * @param b Second string
     * @return true if strings are equal, false otherwise
     */
    private boolean constantTimeEquals(String a, String b) {
        if (a == null || b == null) {
            return a == b;
        }
        
        if (a.length() != b.length()) {
            return false;
        }
        
        int result = 0;
        for (int i = 0; i < a.length(); i++) {
            result |= a.charAt(i) ^ b.charAt(i);
        }
        
        return result == 0;
    }

    /**
     * Handles a webhook event based on its type
     *
     * @param event The webhook event to handle
     */
    public void handleWebhookEvent(WebhookEvent event) {
        if (event == null) {
            logger.warn("Received null webhook event");
            return;
        }

        logger.info("Processing webhook event: type={}, messageId={}, status={}", 
                   event.getEventType(), event.getMessageId(), event.getStatus());

        switch (event.getEventType()) {
            case "sms.sent":
                handleSmsSent(event);
                break;
            case "sms.delivered":
                handleSmsDelivered(event);
                break;
            case "sms.failed":
                handleSmsFailed(event);
                break;
            case "email.sent":
                handleEmailSent(event);
                break;
            case "email.delivered":
                handleEmailDelivered(event);
                break;
            case "email.failed":
                handleEmailFailed(event);
                break;
            case "contact.unsubscribed":
                handleContactUnsubscribed(event);
                break;
            default:
                logger.warn("Unknown webhook event type: {}", event.getEventType());
                handleUnknownEvent(event);
                break;
        }
    }

    private void handleSmsSent(WebhookEvent event) {
        logger.info("SMS sent: messageId={}, phoneNumber={}", event.getMessageId(), event.getPhoneNumber());
        // Add your SMS sent handling logic here
    }

    private void handleSmsDelivered(WebhookEvent event) {
        logger.info("SMS delivered: messageId={}, phoneNumber={}", event.getMessageId(), event.getPhoneNumber());
        // Add your SMS delivered handling logic here
    }

    private void handleSmsFailed(WebhookEvent event) {
        logger.warn("SMS failed: messageId={}, phoneNumber={}, error={}", 
                   event.getMessageId(), event.getPhoneNumber(), event.getErrorMessage());
        // Add your SMS failed handling logic here
    }

    private void handleEmailSent(WebhookEvent event) {
        logger.info("Email sent: messageId={}, email={}", event.getMessageId(), event.getEmail());
        // Add your email sent handling logic here
    }

    private void handleEmailDelivered(WebhookEvent event) {
        logger.info("Email delivered: messageId={}, email={}", event.getMessageId(), event.getEmail());
        // Add your email delivered handling logic here
    }

    private void handleEmailFailed(WebhookEvent event) {
        logger.warn("Email failed: messageId={}, email={}, error={}", 
                   event.getMessageId(), event.getEmail(), event.getErrorMessage());
        // Add your email failed handling logic here
    }

    private void handleContactUnsubscribed(WebhookEvent event) {
        logger.info("Contact unsubscribed: email={}, phoneNumber={}", event.getEmail(), event.getPhoneNumber());
        // Add your contact unsubscribed handling logic here
    }

    private void handleUnknownEvent(WebhookEvent event) {
        logger.info("Unknown event received: {}", event);
        // Add your unknown event handling logic here
    }
}
