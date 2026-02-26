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
            // Example 1: Create a webhook (auto-generated secret)
            System.out.println("Creating webhook...");
            WebhookRequest webhookRequest = new WebhookRequest("https://your-app.com/webhooks/ccai");

            WebhookResponse webhook = client.getWebhook().create(webhookRequest);
            System.out.println("Webhook created! ID: " + webhook.getId());
            System.out.println("URL: " + webhook.getUrl());
            System.out.println("Secret Key: " + webhook.getSecretKey());
            System.out.println("IMPORTANT: Save this secret key securely!");

            // Example 2: Create a webhook with custom secret
            System.out.println("\nCreating webhook with custom secret...");
            String customSecret = "my-custom-secret-key-32chars12";
            WebhookRequest customWebhookRequest = new WebhookRequest(
                "https://your-app.com/webhooks/ccai",
                customSecret
            );
            WebhookResponse customWebhook = client.getWebhook().create(customWebhookRequest);
            System.out.println("Webhook created with custom secret!");

            // Example 3: Get the webhook
            System.out.println("\nGetting webhook details...");
            WebhookResponse webhookDetails = client.getWebhook().get();
            if (webhookDetails != null) {
                System.out.println("Webhook: " + webhookDetails.getUrl());
                System.out.println("Method: " + webhookDetails.getMethod());
                System.out.println("Secret Key: " + webhookDetails.getSecretKey());
            } else {
                System.out.println("No webhook configured");
            }

            // Example 4: Update webhook
            System.out.println("\nUpdating webhook...");
            WebhookRequest updateRequest = new WebhookRequest(
                "https://your-app.com/webhooks/ccai-updated",
                customSecret
            );
            WebhookResponse updated = client.getWebhook().update(updateRequest);
            System.out.println("Webhook updated! New URL: " + updated.getUrl());

            // Example 5: Validate webhook signature
            System.out.println("\nValidating webhook signature...");
            String signatureFromHeader = "signature-from-X-CCAI-Signature-header";
            String secretKey = webhook.getSecretKey();
            String eventHash = "abc123def456ghi789jkl012mno345pq"; // From webhook payload
            long clientIdLong = Long.parseLong(clientId);
            
            boolean isValid = client.getWebhook().validateSignature(
                signatureFromHeader,
                secretKey,
                clientIdLong,
                eventHash
            );
            System.out.println("Signature valid: " + isValid);

            // Example 5: Parse webhook event
            System.out.println("\nParsing webhook event...");
            String eventPayload = """
                {
                    "eventType": "sms.sent",
                    "data": {
                        "id": 12345,
                        "MessageStatus": "sent",
                        "To": "+15551234567",
                        "Message": "Hello World",
                        "CustomData": "order-123",
                        "ClientExternalId": "ext-456",
                        "CampaignId": 789,
                        "CampaignTitle": "Spring Sale"
                    },
                    "eventHash": "abc123def456ghi789jkl012mno345pq"
                }
                """;

            WebhookEvent event = client.getWebhook().parseWebhookEvent(eventPayload);
            System.out.println("Event Type: " + event.getEventType());
            System.out.println("Event Hash: " + event.getEventHash());
            System.out.println("Data: " + event.getData());
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            client.close();
        }
    }
}
