package com.cloudcontactai.sdk.examples;

import com.cloudcontactai.sdk.CCAIClient;
import com.cloudcontactai.sdk.common.CCAIConfig;
import com.cloudcontactai.sdk.webhook.WebhookEvent;
import com.cloudcontactai.sdk.webhook.WebhookRequest;
import com.cloudcontactai.sdk.webhook.WebhookResponse;

import java.util.Arrays;
import java.util.List;

/**
 * Example of managing webhooks and validating webhook signatures
 */
public class WebhookExample {

    public static void main(String[] args) {
        // Load configuration from environment variables
        String clientId = System.getenv("CCAI_CLIENT_ID");
        String apiKey = System.getenv("CCAI_API_KEY");

        if (clientId == null || apiKey == null) {
            System.err.println("Please set CCAI_CLIENT_ID and CCAI_API_KEY environment variables");
            System.exit(1);
        }

        // Create CCAI client
        CCAIConfig config = new CCAIConfig(clientId, apiKey);
        CCAIClient client = new CCAIClient(config);

        try {
            // Example 1: Create a webhook
            System.out.println("Creating webhook...");
            WebhookRequest webhookRequest = new WebhookRequest(
                "https://your-app.com/webhooks/ccai",
                Arrays.asList("sms.sent", "sms.delivered", "email.opened", "email.clicked"),
                true,
                "your-webhook-secret-key"
            );
            
            WebhookResponse webhook = client.getWebhook().create(webhookRequest);
            System.out.println("Webhook created! ID: " + webhook.getId());
            System.out.println("URL: " + webhook.getUrl());

            // Example 2: List all webhooks
            System.out.println("\nListing all webhooks...");
            List<WebhookResponse> webhooks = client.getWebhook().getAll();
            System.out.println("Total webhooks: " + webhooks.size());
            for (WebhookResponse wh : webhooks) {
                System.out.println("  - " + wh.getId() + ": " + wh.getUrl() + " (active: " + wh.isActive() + ")");
            }

            // Example 3: Get specific webhook
            System.out.println("\nGetting webhook details...");
            WebhookResponse webhookDetails = client.getWebhook().get(webhook.getId());
            System.out.println("Webhook: " + webhookDetails.getUrl());
            System.out.println("Events: " + webhookDetails.getEvents());

            // Example 4: Update webhook
            System.out.println("\nUpdating webhook...");
            WebhookRequest updateRequest = new WebhookRequest(
                "https://your-app.com/webhooks/ccai-updated",
                Arrays.asList("sms.sent", "sms.delivered", "sms.failed"),
                true,
                "your-webhook-secret-key"
            );
            WebhookResponse updated = client.getWebhook().update(webhook.getId(), updateRequest);
            System.out.println("Webhook updated! New URL: " + updated.getUrl());

            // Example 5: Test webhook
            System.out.println("\nTesting webhook...");
            var testResult = client.getWebhook().test(webhook.getId());
            System.out.println("Test result: " + (testResult.isSuccess() ? "SUCCESS" : "FAILED"));

            // Example 6: Validate webhook signature (for incoming webhooks)
            System.out.println("\nValidating webhook signature...");
            String incomingPayload = "{\"eventType\":\"sms.sent\",\"messageId\":\"123\"}";
            String incomingSignature = "signature-from-header";  // From X-CCAI-Signature header
            String secret = "your-webhook-secret-key";
            
            boolean isValid = client.getWebhook().validateWebhookSignature(
                incomingPayload,
                incomingSignature,
                secret
            );
            System.out.println("Signature valid: " + isValid);

            // Example 7: Parse webhook event
            System.out.println("\nParsing webhook event...");
            String eventPayload = """
                {
                    "eventType": "sms.sent",
                    "timestamp": "2026-02-02T22:00:00Z",
                    "campaignId": "campaign-123",
                    "messageId": "msg-456",
                    "phoneNumber": "+15551234567",
                    "status": "sent",
                    "cost": 0.01
                }
                """;
            
            WebhookEvent event = client.getWebhook().parseWebhookEvent(eventPayload);
            System.out.println("Event Type: " + event.getEventType());
            System.out.println("Campaign ID: " + event.getCampaignId());
            System.out.println("Phone Number: " + event.getPhoneNumber());
            System.out.println("Status: " + event.getStatus());

            // Example 8: Delete webhook
            System.out.println("\nDeleting webhook...");
            var deleteResult = client.getWebhook().delete(webhook.getId());
            System.out.println("Delete result: " + (deleteResult.isSuccess() ? "SUCCESS" : "FAILED"));

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            client.close();
        }
    }
}
