package com.cloudcontactai.sdk.examples;

import com.cloudcontactai.sdk.CCAIClient;
import com.cloudcontactai.sdk.common.CCAIConfig;
import com.cloudcontactai.sdk.sms.Account;
import com.cloudcontactai.sdk.sms.SMSResponse;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

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
                "Test Campaign"
            );
            System.out.println("SMS sent! ID: " + response1.getResponseId());

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
                null  // optional sender phone
            );
            System.out.println("Bulk SMS sent! ID: " + response2.getResponseId());

            // Example 3: Send SMS to multiple recipients with custom fields
            System.out.println("\nSending SMS whit custom fields...");
            List<Account> accounts = Arrays.asList(
                    new Account("John", "Doe", "+15551234567", Map.of("role", "teacher", "room", "Second floor, 3B room")),
                    new Account("Jane", "Smith", "+15559876543", Map.of("role", "teacher", "room", "First floor, 1A room"))
            );

            SMSResponse response3 = client.getSms().send(
                    accounts,
                    "Hello dear ${role}, your classroom is on ${room}.",
                    "Custom fields sample"
            );
            System.out.println("Bulk SMS with custom fields sent! ID: " + response3.getResponseId());

            // Example 4: Send SMS with custom message data (this one is past to webhook events)
            System.out.println("Sending SMS to with custom message data...");
            SMSResponse response3 = client.getSms().sendSingle(
                    "John",
                    "Doe",
                    "+15551234567",
                    "Hello from CCAI Java SDK! This is a test message with custom data.",
                    "Test Campaign",
                    "{\"myAppCustomId\": \"3c1344d771eb48f99de6846746b2d4a0\"}"
            );
            System.out.println("SMS custom message data sent! ID: " + response1.getResponseId());
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            client.close();
        }
    }
}
