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
        CCAIConfig config = new CCAIConfig(clientId, apiKey, true);
        CCAIClient client = new CCAIClient(config);

        try {
            // Example 1: Create a webhook
            System.out.println("Creating webhook...");
            WebhookRequest webhookRequest = new WebhookRequest("https://your-app.com/webhooks/ccai");

            WebhookResponse webhook = client.getWebhook().create(webhookRequest);
            System.out.println("Webhook created! ID: " + webhook.getId());
            System.out.println("URL: " + webhook.getUrl());

            // Example 2: Get the webhook
            System.out.println("\nGetting webhook details...");
            WebhookResponse webhookDetails = client.getWebhook().get();
            if (webhookDetails != null) {
                System.out.println("Webhook: " + webhookDetails.getUrl());
                System.out.println("Method: " + webhookDetails.getMethod());
            } else {
                System.out.println("No webhook configured");
            }

            // Example 3: Update webhook
            System.out.println("\nUpdating webhook...");
            WebhookRequest updateRequest = new WebhookRequest("https://your-app.com/webhooks/ccai-updated");
            WebhookResponse updated = client.getWebhook().update(updateRequest);
            System.out.println("Webhook updated! New URL: " + updated.getUrl());

            // Example 4: Parse webhook event
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
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            client.close();
        }
    }
}
