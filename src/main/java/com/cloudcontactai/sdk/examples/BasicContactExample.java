package com.cloudcontactai.sdk.examples;

import com.cloudcontactai.sdk.CCAIClient;
import com.cloudcontactai.sdk.common.CCAIConfig;
import com.cloudcontactai.sdk.contact.ContactDoNotTextResponse;

/**
 * Basic example of managing contact do not text status using the CCAI Java SDK
 */
public class BasicContactExample {

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
            // Example 1: Mark contact as do not text by contactId
            System.out.println("Marking contact as do not text by contactId...");
            ContactDoNotTextResponse response1 = client.getContact().setDoNotText(
                "12345",  // contactId
                null,     // phone (not needed when using contactId)
                true      // doNotText = true
            );
            System.out.println("Contact marked as do not text! ContactId: " + response1.getContactId() + 
                             ", Phone: " + response1.getPhone() + 
                             ", DoNotText: " + response1.getDoNotText());

            // Example 2: Mark contact as do not text by phone number
            System.out.println("\nMarking contact as do not text by phone...");
            ContactDoNotTextResponse response2 = client.getContact().setDoNotText(
                null,            // contactId (not needed when using phone)
                "+15551234567",  // phone
                true             // doNotText = true
            );
            System.out.println("Contact marked as do not text! ContactId: " + response2.getContactId() + 
                             ", Phone: " + response2.getPhone() + 
                             ", DoNotText: " + response2.getDoNotText());

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            client.close();
        }
    }
}