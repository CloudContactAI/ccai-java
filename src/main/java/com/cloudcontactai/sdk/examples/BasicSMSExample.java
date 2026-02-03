package com.cloudcontactai.sdk.examples;

import com.cloudcontactai.sdk.CCAIClient;
import com.cloudcontactai.sdk.common.CCAIConfig;
import com.cloudcontactai.sdk.sms.Account;
import com.cloudcontactai.sdk.sms.SMSResponse;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * Basic example of sending SMS messages using the CCAI Java SDK
 */
public class BasicSMSExample {

    public static void main(String[] args) {
        // Load configuration from environment variables
        String clientId = System.getenv("CCAI_CLIENT_ID");
        String apiKey = System.getenv("CCAI_API_KEY");

        if (clientId == null || apiKey == null) {
            System.err.println("Please set CCAI_CLIENT_ID and CCAI_API_KEY environment variables");
            System.exit(1);
        }

        // Create CCAI client with optional debug mode
        CCAIConfig config = new CCAIConfig(
            clientId,
            apiKey,
            false,  // useTestEnvironment
            true,   // debugMode
            3,      // maxRetries
            30000   // timeoutMs
        );
        
        CCAIClient client = new CCAIClient(config);

        try {
            // Example 1: Send SMS to a single recipient
            System.out.println("Sending SMS to single recipient...");
            SMSResponse response1 = client.getSms().sendSingle(
                "John",
                "Doe",
                "+15551234567",
                "Hello from CCAI Java SDK! This is a test message.",
                "Test Campaign",
                null  // optional sender phone
            );
            System.out.println("SMS sent! Campaign ID: " + response1.getCampaignId());
            System.out.println("Message ID: " + response1.getId());

            // Example 2: Send SMS to multiple recipients
            System.out.println("\nSending SMS to multiple recipients...");
            List<Account> accounts = Arrays.asList(
                new Account("John", "Doe", "+15551234567", new HashMap<>()),
                new Account("Jane", "Smith", "+15559876543", new HashMap<>())
            );
            
            SMSResponse response2 = client.getSms().send(
                accounts,
                "Hello everyone! This is a bulk SMS message.",
                "Bulk Campaign",
                null  // optional sender phone
            );
            System.out.println("Bulk SMS sent! Campaign ID: " + response2.getCampaignId());

            // Example 3: Get campaign status
            System.out.println("\nGetting campaign status...");
            var status = client.getSms().getCampaignStatus(response1.getCampaignId());
            System.out.println("Campaign Status: " + status.getStatus());
            System.out.println("Sent Messages: " + status.getSentMessages());

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            client.close();
        }
    }
}
