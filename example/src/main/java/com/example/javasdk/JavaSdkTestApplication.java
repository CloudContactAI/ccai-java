package com.example.javasdk;

import com.cloudcontactai.sdk.CCAIClient;
import com.cloudcontactai.sdk.common.CCAIConfig;
import com.cloudcontactai.sdk.sms.SMSResponse;
import com.cloudcontactai.sdk.mms.MMSResponse;
import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

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
            if ("sms".equals(args[0])) {
                runSmsTest();
                return;
            } else if ("mms".equals(args[0])) {
                runMmsTest();
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
            System.out.println("=== WEBHOOK RECEIVED ===");
            System.out.println("Payload: " + payload);
            System.out.println("Signature: " + signature);
            System.out.println("All Headers: " + headers);
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
    
    private static void runMmsTest() {
        System.out.println("Testing CCAI Java SDK MMS...");
        
        String clientId = System.getProperty("CCAI_CLIENT_ID", System.getenv("CCAI_CLIENT_ID"));
        String apiKey = System.getProperty("CCAI_API_KEY", System.getenv("CCAI_API_KEY"));
        
        if (clientId == null || apiKey == null) {
            System.err.println("Please set CCAI_CLIENT_ID and CCAI_API_KEY environment variables");
            return;
        }
        
        CCAIConfig config = new CCAIConfig(clientId, apiKey, true);
        CCAIClient client = new CCAIClient(config);
        
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
            com.cloudcontactai.sdk.mms.Account account = new com.cloudcontactai.sdk.mms.Account("John", "Doe", "+14158906431");
            accounts.add(account);
            
            MMSResponse response = client.getMms().sendWithImage(
                accounts,
                "Testing with my CloudContactAI logo2",
                "Test my MMS Campaign2",
                tempFile,
                null
            );
            
            String status = (response != null && (response.getCampaignId() != null || response.getId() != null)) ? "PASS" : "FAIL";
            String responseId = response != null ? (response.getCampaignId() != null ? response.getCampaignId() : response.getId()) : "N/A";
            System.out.println(String.format("MMS Test Result: %s | Name: %s %s | Phone: %s | Response ID: %s", 
                status, account.getFirstName(), account.getLastName(), account.getPhone(), responseId));
        } catch (Exception e) {
            System.out.println(String.format("MMS Test Result: FAIL | Error: %s", e.getMessage()));
            e.printStackTrace();
        }
    }
    
    private static void runSmsTest() {
        System.out.println("Testing CCAI Java SDK...");
        
        String clientId = System.getProperty("CCAI_CLIENT_ID", System.getenv("CCAI_CLIENT_ID"));
        String apiKey = System.getProperty("CCAI_API_KEY", System.getenv("CCAI_API_KEY"));
        
        if (clientId == null || apiKey == null) {
            System.err.println("Please set CCAI_CLIENT_ID and CCAI_API_KEY environment variables");
            return;
        }
        
        CCAIConfig config = new CCAIConfig(clientId, apiKey, true);
        CCAIClient client = new CCAIClient(config);
        
        String firstName = "John";
        String lastName = "Doe";
        String phone = "+14158906431";
        
        try {
            SMSResponse response = client.getSms().sendSingle(firstName, lastName, phone, "Test SMS from Java SDK", "Test SMS Campaign", null);
            String status = (response != null && response.getId() != null) ? "PASS" : "FAIL";
            System.out.println(String.format("SMS Test Result: %s | Name: %s %s | Phone: %s | Response ID: %s", 
                status, firstName, lastName, phone, 
                response != null ? response.getId() : "N/A"));
        } catch (Exception e) {
            System.out.println(String.format("SMS Test Result: FAIL | Name: %s %s | Phone: %s | Error: %s", 
                firstName, lastName, phone, e.getMessage()));
        }
    }
}
