package com.cloudcontactai.sdk.examples;

import com.cloudcontactai.sdk.CCAIClient;
import com.cloudcontactai.sdk.common.CCAIConfig;
import com.cloudcontactai.sdk.mms.Account;
import com.cloudcontactai.sdk.mms.MMSResponse;
import com.cloudcontactai.sdk.mms.SignedUploadUrlRequest;
import com.cloudcontactai.sdk.mms.SignedUploadUrlResponse;

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
        CCAIConfig config = new CCAIConfig(clientId, apiKey);
        CCAIClient client = new CCAIClient(config);

        try {
            // Example 1: Send MMS with automatic image upload (easiest method)
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
            
            MMSResponse response1 = client.getMms().sendWithImage(
                accounts,
                "Check out this image!",
                "MMS Campaign",
                imageFile,
                null  // optional sender phone
            );
            System.out.println("MMS sent! Campaign ID: " + response1.getCampaignId());
            System.out.println("Sent Count: " + response1.getSentCount());

            // Example 2: Manual upload process (advanced)
            System.out.println("\nManual MMS upload process...");
            
            // Step 1: Get signed upload URL
            SignedUploadUrlRequest uploadRequest = new SignedUploadUrlRequest(
                "my-image.jpg",
                "image/jpeg",
                null,
                false
            );
            SignedUploadUrlResponse uploadResponse = client.getMms().getSignedUploadUrl(uploadRequest);
            System.out.println("Got signed URL: " + uploadResponse.getFileKey());
            
            // Step 2: Upload image to S3
            client.getMms().uploadImageToSignedUrl(
                uploadResponse.getSignedS3Url(),
                imageFile,
                "image/jpeg"
            );
            System.out.println("Image uploaded successfully");
            
            // Step 3: Send MMS with the uploaded file key
            MMSResponse response2 = client.getMms().send(
                accounts,
                "Another MMS message!",
                "MMS Campaign 2",
                uploadResponse.getFileKey(),
                null
            );
            System.out.println("MMS sent! Campaign ID: " + response2.getCampaignId());

            // Example 3: Send single MMS
            System.out.println("\nSending single MMS...");
            MMSResponse response3 = client.getMms().sendSingle(
                "Jane",
                "Smith",
                "+15559876543",
                "Hello Jane! Check out this image.",
                "Single MMS",
                uploadResponse.getFileKey(),
                null
            );
            System.out.println("Single MMS sent! Campaign ID: " + response3.getCampaignId());

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            client.close();
        }
    }
}
