package com.example.javasdk;

import com.cloudcontactai.sdk.CCAIClient;
import com.cloudcontactai.sdk.common.CCAIConfig;
import com.cloudcontactai.sdk.sms.SMSResponse;
import com.cloudcontactai.sdk.sms.Account;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@SpringBootApplication
@RestController
public class JavaSdkTestApplication implements CommandLineRunner {

    public static void main(String[] args) {
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

    /*
     * Pass 'sms' to send SMS messages
     * The default behavior is to start the webhook server on the port specified in application.properties
     */
    @Override
    public void run(String... args) throws Exception {
        if (args.length > 0 && "sms".equals(args[0])) {
            runSmsTest();
        } else {
            System.out.println("Webhook server started. Use 'sms' argument to test SMS sending.");
        }
    }
    
    private void runSmsTest() throws Exception {
        System.out.println("Testing CCAI Java SDK...");
        
        CCAIConfig config = new CCAIConfig(
            "ccai_client_id",
            "ccai_api_key",
            true);
        
        CCAIClient client = new CCAIClient(config);
        
        try {
            SMSResponse response = client.getSms().sendSingle(
                "Johns", "Does", "+14158906431",
                "Test SMS", "Test Campaign", "+13056486693"
            );
            System.out.println("SMS sent: " + response.getId());
        } catch (Exception e) {
            System.out.println("SMS failed: " + e.getMessage());
        }
    }
}
