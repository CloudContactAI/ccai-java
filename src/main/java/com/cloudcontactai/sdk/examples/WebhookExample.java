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

            // Example 3: Create multiple webhooks (custom and auto-generated secret)
            System.out.println("Creating multiple webhooks...");
            List<WebhookRequest> listWebhookRequest = Arrays.asList(
                    new WebhookRequest("https://your-new-app.com/webhooks/ccai", customSecret),
                    new WebhookRequest("https://your-second-app.com/webhooks/ccai")
            );

            List<WebhookResponse> webhooks = client.getWebhook().create(listWebhookRequest);
            System.out.println("First Webhook created! ID: " + webhooks.get(0).getId());
            System.out.println("Second Webhook created! ID: " + webhooks.get(1).getId());

            // Example 4: Get the webhook by ID
            System.out.println("\nGetting webhook details...");
            Long webhookId = 105L; //your saved webhook ID
            WebhookResponse webhookDetails = client.getWebhook().get(webhookId);
            System.out.println("Webhook: " + webhookDetails.getUrl());
            System.out.println("Method: " + webhookDetails.getMethod());
            System.out.println("Secret Key: " + webhookDetails.getSecretKey());

            // Example 5: Get webhook list
            System.out.println("\nGetting all webhooks...");
            List<WebhookResponse> webhookListDetails = client.getWebhook().getAll();
            System.out.println("Webhook count: " + webhookListDetails.size());
            if (!webhookListDetails.isEmpty()) {
                System.out.println("First Method: " + webhookListDetails.get(0).getMethod());
                System.out.println("First Secret Key: " + webhookListDetails.get(0).getSecretKey());
            }

            // Example 6: Update webhook
            System.out.println("\nUpdating webhook...");
            long webhookId = 105; //your saved webhook ID
            WebhookUpdateRequest updateRequest = new WebhookUpdateRequest(
                webhookId,
                "https://your-app.com/webhooks/ccai-updated",
                customSecret
            );
            WebhookResponse updated = client.getWebhook().update(updateRequest);
            System.out.println("Webhook updated! New URL: " + updated.getUrl());

            // Example 7: Delete a webhook
            System.out.println("\nDeleting webhook...");
            Long webhookId = 105L; //your saved webhook ID
            WebhookResponse webhookDeleted = client.getWebhook().delete(webhookId);
            System.out.println("Webhook Url deleted: " + webhookDeleted.getUrl());

            // Example 8: Validate webhook signature
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

            // Example 9: Parse webhook event
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
