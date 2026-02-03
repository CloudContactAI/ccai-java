package com.cloudcontactai.sdk.examples;

import com.cloudcontactai.sdk.CCAIClient;
import com.cloudcontactai.sdk.common.CCAIConfig;
import com.cloudcontactai.sdk.email.EmailAccount;
import com.cloudcontactai.sdk.email.EmailResponse;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * Basic example of sending Email campaigns using the CCAI Java SDK
 */
public class BasicEmailExample {

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
            // Example 1: Send email to a single recipient
            System.out.println("Sending email to single recipient...");
            EmailResponse response1 = client.getEmail().sendSingle(
                "John",
                "Doe",
                "john.doe@example.com",
                "Welcome to CCAI!",
                "<html><body><h1>Hello John!</h1><p>Welcome to our service.</p></body></html>",
                null  // optional text content
            );
            System.out.println("Email sent! Campaign ID: " + response1.getCampaignId());
            System.out.println("Message ID: " + response1.getId());

            // Example 2: Send email to multiple recipients
            System.out.println("\nSending email to multiple recipients...");
            List<EmailAccount> accounts = Arrays.asList(
                new EmailAccount("John", "Doe", "john.doe@example.com", null, new HashMap<>()),
                new EmailAccount("Jane", "Smith", "jane.smith@example.com", null, new HashMap<>())
            );
            
            String htmlContent = """
                <html>
                <body>
                    <h1>Newsletter</h1>
                    <p>Here's your monthly newsletter with the latest updates!</p>
                    <ul>
                        <li>Feature 1: New dashboard</li>
                        <li>Feature 2: Enhanced reporting</li>
                        <li>Feature 3: Mobile app</li>
                    </ul>
                </body>
                </html>
                """;
            
            EmailResponse response2 = client.getEmail().send(
                accounts,
                "Monthly Newsletter",
                htmlContent,
                null  // optional text content
            );
            System.out.println("Bulk email sent! Campaign ID: " + response2.getCampaignId());

            // Example 3: Get campaign status
            System.out.println("\nGetting campaign status...");
            var status = client.getEmail().getCampaignStatus(response1.getCampaignId());
            System.out.println("Campaign Status: " + status.getStatus());
            System.out.println("Sent Emails: " + status.getSentEmails());

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            client.close();
        }
    }
}
