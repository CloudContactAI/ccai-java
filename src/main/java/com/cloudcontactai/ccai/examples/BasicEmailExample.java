// Copyright (c) 2025 CloudContactAI LLC
// Licensed under the MIT License. See LICENSE in the project root for license information.

package com.cloudcontactai.ccai.examples;

import com.cloudcontactai.ccai.client.CCAIClient;
import com.cloudcontactai.ccai.email.EmailRequest;
import com.cloudcontactai.ccai.email.EmailResponse;
import com.cloudcontactai.ccai.exception.CCAIApiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Basic example of sending email messages using the CCAI Java client
 */
public class BasicEmailExample {

    private static final Logger logger = LoggerFactory.getLogger(BasicEmailExample.class);

    public static void main(String[] args) {
        // Load configuration from environment variables
        String clientId = System.getenv("CCAI_CLIENT_ID");
        String apiKey = System.getenv("CCAI_API_KEY");

        if (clientId == null || apiKey == null) {
            logger.error("Please set CCAI_CLIENT_ID and CCAI_API_KEY environment variables");
            System.exit(1);
        }

        // Create CCAI client
        CCAIClient client = CCAIClient.builder()
                .clientId(clientId)
                .apiKey(apiKey)
                .debugMode(true)
                .build();

        try {
            // Example 1: Send simple email to a single recipient
            logger.info("Sending email to single recipient...");
            EmailResponse response1 = client.getEmailService().sendEmail(
                    "test@example.com",
                    "Test Email from CCAI Java",
                    "<h1>Hello from CCAI Java!</h1><p>This is a test email message.</p>"
            );
            logger.info("Email Response: {}", response1);

            // Example 2: Send email to multiple recipients
            logger.info("Sending email to multiple recipients...");
            List<String> recipients = Arrays.asList("test1@example.com", "test2@example.com");
            EmailResponse response2 = client.getEmailService().sendEmail(
                    recipients,
                    "Bulk Email from CCAI Java",
                    "<h1>Hello everyone!</h1><p>This is a bulk email message from CCAI Java.</p>"
            );
            logger.info("Bulk Email Response: {}", response2);

            // Example 3: Send email with full configuration
            logger.info("Sending email with full configuration...");
            EmailRequest request = new EmailRequest();
            request.setToEmails(Arrays.asList("test@example.com"));
            request.setSubject("Advanced Email from CCAI Java");
            request.setHtmlContent("<h1>Advanced Email</h1><p>Hello {{name}}, this email has custom data!</p>");
            request.setTextContent("Hello {{name}}, this email has custom data!");
            request.setFromEmail("noreply@yourcompany.com");
            request.setFromName("Your Company");
            request.setReplyTo("support@yourcompany.com");

            // Add custom data
            Map<String, Object> customData = new HashMap<>();
            customData.put("campaign_type", "welcome");
            customData.put("user_segment", "premium");
            request.setCustomData(customData);

            // Add variables for template substitution
            Map<String, String> variables = new HashMap<>();
            variables.put("name", "John Doe");
            request.setVariables(variables);

            EmailResponse response3 = client.getEmailService().sendEmail(request);
            logger.info("Advanced Email Response: {}", response3);

            // Example 4: Send email asynchronously
            logger.info("Sending email asynchronously...");
            client.getEmailService().sendEmailAsync(
                    "test@example.com",
                    "Async Email from CCAI Java",
                    "<h1>Async Email</h1><p>This email was sent asynchronously!</p>"
            ).thenAccept(response -> {
                logger.info("Async Email Response: {}", response);
            }).exceptionally(throwable -> {
                logger.error("Async email failed", throwable);
                return null;
            });

            // Wait a bit for async operation to complete
            Thread.sleep(2000);

        } catch (CCAIApiException e) {
            logger.error("CCAI API Error: {}", e.getMessage(), e);
        } catch (Exception e) {
            logger.error("Unexpected error", e);
        }
    }
}
