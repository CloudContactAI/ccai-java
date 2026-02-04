package com.cloudcontactai.sdk.examples;

import com.cloudcontactai.sdk.CCAIClient;
import com.cloudcontactai.sdk.common.CCAIConfig;
import com.cloudcontactai.sdk.sms.Account;
import com.cloudcontactai.sdk.sms.SMSResponse;

import java.util.Arrays;
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

        // Create CCAI client with optional test environment
        CCAIConfig config = new CCAIConfig(
            clientId,
            apiKey,
            false  // useTestEnvironment - set to true for testing
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
                null  // optional variables map
            );
            System.out.println("SMS sent! ID: " + response1.getId());

            // Example 2: Send SMS to multiple recipients
            System.out.println("\nSending SMS to multiple recipients...");
            List<Account> accounts = Arrays.asList(
                new Account("John", "Doe", "+15551234567"),
                new Account("Jane", "Smith", "+15559876543")
            );
            
            SMSResponse response2 = client.getSms().send(
                accounts,
                "Hello everyone! This is a bulk SMS message.",
                "Bulk Campaign",
                null  // optional variables map
            );
            System.out.println("Bulk SMS sent! ID: " + response2.getId());

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            client.close();
        }
    }
}
