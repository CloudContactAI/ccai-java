package com.example.javasdk;

import com.cloudcontactai.sdk.CCAIClient;
import com.cloudcontactai.sdk.common.CCAIConfig;
import com.cloudcontactai.sdk.sms.SMSResponse;
import com.cloudcontactai.sdk.sms.Account;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Arrays;
import java.util.HashMap;

@SpringBootApplication
public class JavaSdkTestApplication implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(JavaSdkTestApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("Testing CCAI Java SDK...");
        
        // Initialize configuration
        CCAIConfig config = new CCAIConfig(
            "ccai-client-id",  // Client ID - Provide your actual client ID here
            "ccai-api-key",  // provide your API Key here
            true); // Use sandbox/test environment;
        
        // Create client
        CCAIClient client = new CCAIClient(config);
        
        System.out.println("Client initialized successfully!");
        System.out.println("Base URL: " + config.getBaseUrl());
        System.out.println("Email Base URL: " + config.getEmailBaseUrl());
        
        try {
            // Test single SMS
            SMSResponse response = client.getSms().sendSingle(
                "John",
                "Doe", 
                "+14158906431",
                "Testing some more message from Java SDK",
                "Test Campaign",
                "+13056486693"
            );
            System.out.println("SMS sent successfully! Response ID: " + response.getId());
        } catch (Exception e) {
            System.out.println("SMS sending failed (expected in test): " + e.getMessage());
        }
        
        try {
            // Test bulk SMS
            Account account = new Account("Jane", "Smith", "+14158906431", new HashMap<>());
            SMSResponse response = client.getSms().send(
                Arrays.asList(account),
                "This is greatt testing.",
                "Bulk Test Campaign",
                "+13056486693"
            );
            System.out.println("Bulk SMS sent successfully! Response ID: " + response.getId());
        } catch (Exception e) {
            System.out.println("Bulk SMS sending failed (expected in test): " + e.toString());
        }
        
        System.out.println("SDK test completed!");
    }
}
