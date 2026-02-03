package com.example.javasdk;

import com.cloudcontactai.ccai.client.CCAIClient;
import com.cloudcontactai.ccai.config.CCAIConfig;
import com.cloudcontactai.ccai.sms.SMSResponse;
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
        
        CCAIConfig config = new CCAIConfig(clientId, apiKey);
        config.setUseTestEnvironment(true);
        CCAIClient client = new CCAIClient(config);
        
        try {
            // Get the image from resources
            String imagePath = JavaSdkTestApplication.class.getClassLoader()
                .getResource("CloudContactAI.png").getPath();
            
            // Upload and send MMS with the image
            List<com.cloudcontactai.ccai.mms.Account> accounts = new java.util.ArrayList<>();
            accounts.add(new com.cloudcontactai.ccai.mms.Account("John", "Doe", "+14158906431"));
            
            SMSResponse response = client.getMmsService().sendWithImage(
                imagePath,
                "image/png",
                accounts,
                "Test MMS with CloudContactAI logo",
                "Test MMS Campaign"
            );
            System.out.println("MMS sent successfully");
        } catch (Exception e) {
            System.out.println("MMS failed: " + e.getMessage());
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
        
        CCAIConfig config = new CCAIConfig(clientId, apiKey);
        config.setUseTestEnvironment(true);
        CCAIClient client = new CCAIClient(config);
        
        try {
            client.getSmsService().sendSMS("+14158906431", "Test SMS from Java SDK");
            System.out.println("SMS sent successfully");
        } catch (Exception e) {
            System.out.println("SMS failed: " + e.getMessage());
        }
    }
}
