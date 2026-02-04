package com.cloudcontactai.sdk.examples;

import com.cloudcontactai.sdk.CCAIClient;
import com.cloudcontactai.sdk.common.CCAIConfig;
import com.cloudcontactai.sdk.mms.Account;
import com.cloudcontactai.sdk.mms.MMSResponse;

import java.io.File;
import java.util.Arrays;
import java.util.List;

/**
 * Basic example of sending MMS messages with images using the CCAI Java SDK
 */
public class BasicMMSExample {

    public static void main(String[] args) {
        // Load configuration from environment variables
        String clientId = System.getenv("CCAI_CLIENT_ID");
        String apiKey = System.getenv("CCAI_API_KEY");

        if (clientId == null || apiKey == null) {
            System.err.println("Please set CCAI_CLIENT_ID and CCAI_API_KEY environment variables");
            System.exit(1);
        }

        // Create CCAI client
        CCAIConfig config = new CCAIConfig(clientId, apiKey, false);
        CCAIClient client = new CCAIClient(config);

        try {
            // Send MMS with automatic image upload (easiest method)
            System.out.println("Sending MMS with automatic image upload...");
            File imageFile = new File("path/to/your/image.jpg");
            
            if (!imageFile.exists()) {
                System.out.println("Image file not found. Please provide a valid image path.");
                System.out.println("Recommended: 640x1138px, under 500KB");
                return;
            }
            
            List<Account> accounts = Arrays.asList(
                new Account("John", "Doe", "+15551234567")
            );
            
            MMSResponse response = client.getMms().sendWithImage(
                accounts,
                "Check out this image!",
                "MMS Campaign",
                imageFile,
                null  // optional sender phone
            );
            
            String responseId = response.getCampaignId() != null ? response.getCampaignId() : response.getId();
            System.out.println("MMS sent! Response ID: " + responseId);
            if (response.getSentCount() != null) {
                System.out.println("Sent Count: " + response.getSentCount());
            }

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            client.close();
        }
    }
}
