// Copyright (c) 2025 CloudContactAI LLC
// Licensed under the MIT License. See LICENSE in the project root for license information.

package com.cloudcontactai.ccai.examples;

import com.cloudcontactai.ccai.client.CCAIClient;
import com.cloudcontactai.ccai.webhook.WebhookEvent;
import com.cloudcontactai.ccai.webhook.WebhookService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Example Spring Boot application that handles CCAI webhooks
 */
@SpringBootApplication
@RestController
public class WebhookExample {

    private static final Logger logger = LoggerFactory.getLogger(WebhookExample.class);

    private final WebhookService webhookService;
    private final String webhookSecret;

    public WebhookExample() {
        // Initialize CCAI client
        String clientId = System.getenv("CCAI_CLIENT_ID");
        String apiKey = System.getenv("CCAI_API_KEY");
        this.webhookSecret = System.getenv("CCAI_WEBHOOK_SECRET");

        if (clientId == null || apiKey == null) {
            throw new IllegalStateException("Please set CCAI_CLIENT_ID and CCAI_API_KEY environment variables");
        }

        CCAIClient client = CCAIClient.builder()
                .clientId(clientId)
                .apiKey(apiKey)
                .debugMode(true)
                .build();

        this.webhookService = client.getWebhookService();
    }

    /**
     * Webhook endpoint for receiving CCAI events
     */
    @PostMapping("/webhook/ccai")
    public ResponseEntity<String> handleWebhook(
            @RequestBody String payload,
            @RequestHeader(value = "X-CCAI-Signature", required = false) String signature) {

        try {
            logger.info("Received webhook payload: {}", payload);

            // Validate webhook signature if secret is configured
            if (webhookSecret != null && !webhookSecret.isEmpty()) {
                if (signature == null || !webhookService.validateWebhookSignature(payload, signature, webhookSecret)) {
                    logger.warn("Invalid webhook signature");
                    return ResponseEntity.status(401).body("Invalid signature");
                }
                logger.info("Webhook signature validated successfully");
            }

            // Parse webhook event
            WebhookEvent event = webhookService.parseWebhookEvent(payload);
            logger.info("Parsed webhook event: {}", event);

            // Handle the event
            webhookService.handleWebhookEvent(event);

            return ResponseEntity.ok("Webhook processed successfully");

        } catch (Exception e) {
            logger.error("Error processing webhook", e);
            return ResponseEntity.status(500).body("Error processing webhook: " + e.getMessage());
        }
    }

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("OK");
    }

    /**
     * Test endpoint to simulate webhook events
     */
    @PostMapping("/test/webhook")
    public ResponseEntity<String> testWebhook() {
        try {
            // Create a test webhook event
            String testPayload = """
                {
                    "event_type": "sms.delivered",
                    "timestamp": "2025-01-09T12:00:00Z",
                    "campaign_id": "test-campaign-123",
                    "message_id": "msg-456",
                    "phone_number": "+1234567890",
                    "status": "delivered",
                    "delivery_status": "delivered",
                    "cost": 0.05,
                    "account_id": "acc-789",
                    "custom_data": {
                        "user_id": "user-123",
                        "campaign_type": "promotional"
                    }
                }
                """;

            WebhookEvent event = webhookService.parseWebhookEvent(testPayload);
            webhookService.handleWebhookEvent(event);

            return ResponseEntity.ok("Test webhook processed successfully");

        } catch (Exception e) {
            logger.error("Error processing test webhook", e);
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        SpringApplication.run(WebhookExample.class, args);
        logger.info("Webhook server started. Listening for webhooks at /webhook/ccai");
        logger.info("Test endpoint available at /test/webhook");
        logger.info("Health check available at /health");
    }
}
