// Copyright (c) 2025 CloudContactAI LLC
// Licensed under the MIT License. See LICENSE in the project root for license information.

package com.cloudcontactai.ccai.examples;

import com.cloudcontactai.ccai.client.CCAIClient;
import com.cloudcontactai.ccai.exception.CCAIApiException;
import com.cloudcontactai.ccai.sms.SMSResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

/**
 * Basic example of sending SMS messages using the CCAI Java client
 */
public class BasicSMSExample {

    private static final Logger logger = LoggerFactory.getLogger(BasicSMSExample.class);

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
            // Example 1: Send SMS to a single phone number
            logger.info("Sending SMS to single phone number...");
            SMSResponse response1 = client.getSmsService().sendSMS(
                    "+1234567890",
                    "Hello from CCAI Java! This is a test message."
            );
            logger.info("SMS Response: {}", response1);

            // Example 2: Send SMS to multiple phone numbers
            logger.info("Sending SMS to multiple phone numbers...");
            List<String> phoneNumbers = Arrays.asList("+1234567890", "+0987654321");
            SMSResponse response2 = client.getSmsService().sendSMS(
                    phoneNumbers,
                    "Hello everyone! This is a bulk SMS message from CCAI Java."
            );
            logger.info("Bulk SMS Response: {}", response2);

            // Example 3: Send SMS asynchronously
            logger.info("Sending SMS asynchronously...");
            client.getSmsService().sendSMSAsync(
                    "+1234567890",
                    "This is an async SMS message!"
            ).thenAccept(response -> {
                logger.info("Async SMS Response: {}", response);
            }).exceptionally(throwable -> {
                logger.error("Async SMS failed", throwable);
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
