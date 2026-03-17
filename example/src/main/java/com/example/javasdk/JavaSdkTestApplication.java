package com.example.javasdk;

import com.cloudcontactai.sdk.CCAIClient;
import com.cloudcontactai.sdk.common.CCAIConfig;
import com.cloudcontactai.sdk.contact.ContactDoNotTextResponse;
import com.cloudcontactai.sdk.sms.SMSResponse;
import com.cloudcontactai.sdk.mms.MMSResponse;
import com.cloudcontactai.sdk.webhook.WebhookRequest;
import com.cloudcontactai.sdk.webhook.WebhookResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@SpringBootApplication
@RestController
public class JavaSdkTestApplication implements CommandLineRunner {

    public static void main(String[] args) {
        try {
            Dotenv dotenv = Dotenv.configure()
                .directory(".")
                .ignoreIfMissing()
                .load();
            dotenv.entries().forEach(e -> {
                System.setProperty(e.getKey(), e.getValue());
            });
        } catch (Exception e) {
            System.err.println("Warning: Could not load .env file: " + e.getMessage());
        }
        
        if (args.length > 0) {
            String clientId = System.getProperty("CCAI_CLIENT_ID", System.getenv("CCAI_CLIENT_ID"));
            String apiKey = System.getProperty("CCAI_API_KEY", System.getenv("CCAI_API_KEY"));

            if (clientId == null || apiKey == null) {
                System.err.println("Please set CCAI_CLIENT_ID and CCAI_API_KEY environment variables");
                return;
            }

            CCAIConfig config = new CCAIConfig(clientId, apiKey, true);
            CCAIClient client = new CCAIClient(config);

            if ("sms".equals(args[0])) {
                runSmsTest(client);
                return;
            } else if ("mms".equals(args[0])) {
                runMmsTest(client);
                return;
            }else if("webhook".equals(args[0])){
                runWebhookConfigurationTest(client);
                return;
            } else if("contact".equals(args[0])){
                runDoNotTextContactTest(client);
                return;
            }
        }
        SpringApplication.run(JavaSdkTestApplication.class, args);
    }

    @PostMapping("/webhook")
    public ResponseEntity<String> handleWebhook(
            @RequestBody String payload,
            @RequestHeader(value = "X-CCAI-Signature", required = false) String signature,
            @RequestHeader Map<String, String> headers) {
        
        try {
            String clientId = System.getProperty("CCAI_CLIENT_ID", System.getenv("CCAI_CLIENT_ID"));
            String apiKey = System.getProperty("CCAI_API_KEY", System.getenv("CCAI_API_KEY"));

            if (clientId == null || apiKey == null) {
                throw new Exception("Please set CCAI_CLIENT_ID and CCAI_API_KEY environment variables");
            }

            CCAIConfig config = new CCAIConfig(clientId, apiKey, true);
            CCAIClient client = new CCAIClient(config);

            System.out.println("=== WEBHOOK RECEIVED ===");
            System.out.println("Payload: " + payload);
            System.out.println("Signature: " + signature);
            System.out.println("All Headers: " + headers);

            String secretKey = "your-webhook-secret-key";

            Map<String, Object> payloadMap = new ObjectMapper().readValue(payload, Map.class);
            String eventHash = payloadMap.get("eventHash").toString();

            boolean isValid = client.getWebhook().validateSignature(
                    signature,
                    secretKey,
                    Long.parseLong(clientId),
                    eventHash
            );
            System.out.println("Signature valid: " + isValid);
            System.out.println("========================");
            
            return ResponseEntity.ok("Webhook processed");
            
        } catch (Exception e) {
            System.err.println("Webhook error: " + e.getMessage());
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("Webhook server started. Use 'sms' or 'mms' argument to test messaging.");
    }
    
    private static void runMmsTest(CCAIClient client) {
        System.out.println("Testing CCAI Java SDK MMS...");
        
        try {
            // Get the image from resources as InputStream and write to temp file
            java.io.InputStream imageStream = JavaSdkTestApplication.class.getClassLoader()
                .getResourceAsStream("CloudContactAI.png");
            
            if (imageStream == null) {
                throw new RuntimeException("CloudContactAI.png not found in resources");
            }
            
            // Create temp file
            java.io.File tempFile = java.io.File.createTempFile("mms-image-", ".png");
            tempFile.deleteOnExit();
            
            // Copy stream to temp file
            java.nio.file.Files.copy(imageStream, tempFile.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            imageStream.close();
            
            // Upload and send MMS with the image
            List<com.cloudcontactai.sdk.mms.Account> accounts = new java.util.ArrayList<>();
            com.cloudcontactai.sdk.mms.Account account = new com.cloudcontactai.sdk.mms.Account("John", "Doe", "+14158906431", "{customId: \"3c1344d771eb48f99de6846746b2d4a0\"}");
            accounts.add(account);
            
            MMSResponse response = client.getMms().sendWithImage(
                accounts,
                "Testing with my CloudContactAI logo2",
                "Test my MMS Campaign2",
                tempFile,
                null
            );

            String responseId = response.getResponseId();
            String campaignId = response.getId();
            String status = responseId != null ? "PASS" : "FAIL";
            System.out.printf("MMS Test Result: %s | Name: %s %s | Phone: %s | Response ID: %s | Campaign ID: %s%n",
                    status, account.getFirstName(), account.getLastName(), account.getPhone(), responseId, campaignId);
        } catch (Exception e) {
            System.out.printf("MMS Test Result: FAIL | Error: %s%n", e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static void runSmsTest(CCAIClient client) {
        System.out.println("Testing SMS CCAI Java SDK...");

        String firstName = "John";
        String lastName = "Doe";
        String phone = "+14158906431";
        String customData = "{\"appCustomId\": \"3c1344d771eb48f99de6846746b2d4a0\"}";

        try {
            SMSResponse response = client.getSms().sendSingle(
                    firstName, lastName, phone,
                    "Test 2.6 SMS from Java SDK", "Test SMS Campaign",
                    customData, null
            );
            String responseId = response.getResponseId();
            String campaignId = response.getId();
            String status = responseId != null ? "PASS" : "FAIL";
            System.out.printf("SMS Test Result: %s | Name: %s %s | Phone: %s | Response ID: %s | Campaign ID: %s%n",
                    status, firstName, lastName, phone,
                    responseId != null ? responseId : "N/A", campaignId);
        } catch (Exception e) {
            System.out.printf("SMS Test Result: FAIL | Name: %s %s | Phone: %s | Error: %s%n",
                    firstName, lastName, phone, e.getMessage());
        }
    }

    private static void runWebhookConfigurationTest(CCAIClient client) {
        System.out.println("Testing Webhook CCAI Java SDK...");

        try {
            WebhookRequest webhookRequest = new WebhookRequest("https://your-app.com/webhooks/ccai");

            WebhookResponse webhook = client.getWebhook().create(webhookRequest);
            System.out.println("Webhook created! ID: " + webhook.getId());
            System.out.println("URL: " + webhook.getUrl());
            System.out.println("Secret Key: " + webhook.getSecretKey());
            System.out.println("IMPORTANT: Save this secret key securely!");

            System.out.println("Creating multiple webhooks...");
            String customSecret = "my-custom-secret-key-32chars12";
            List<WebhookRequest> listWebhookRequest = Arrays.asList(
                    new WebhookRequest("https://your-new-app.com/webhooks/ccai", customSecret),
                    new WebhookRequest("https://your-second-app.com/webhooks/ccai")
            );

            List<WebhookResponse> webhooks = client.getWebhook().create(listWebhookRequest);
            System.out.println("First Webhook created! ID: " + webhooks.get(0).getId());
            System.out.println("Second Webhook created! ID: " + webhooks.get(1).getId());
        } catch (Exception e) {
            System.out.printf("Webhook Test Result: FAIL %s", e.getMessage());
        }
    }

    private static void runDoNotTextContactTest(CCAIClient client) {
        try{
            System.out.println("Testing Do Not Text Contact CCAI Java SDK...");
            ContactDoNotTextResponse response = client.getContact().setDoNotText(
                    null,   // contactId (not needed when using phone)
                    "+19162371883", // phone
                    true
            );
            System.out.println("Contact marked as do not text! ContactId: " + response.getContactId() +
                    ", Phone: " + response.getPhone() +
                    ", DoNotText: " + response.getDoNotText());
        }catch (Exception e){
            System.out.printf("Do not text Test Result: FAIL %s", e.getMessage());
        }
    }
}
