# CCAI Java

A Java client library for interacting with the [CloudContactAI](https://cloudcontactai.com) API using Spring Boot.

## Features

- Send SMS messages to single or multiple recipients
- Send MMS messages with images
- Send Email campaigns to single or multiple recipients
- Upload images to S3 with signed URLs
- Variable substitution in messages
- Manage webhooks for event notifications
- Async/await support with CompletableFuture
- Progress tracking
- Comprehensive error handling
- Full test coverage
- Spring Boot integration

## Requirements

- Java 17 or higher
- Maven 3.6 or higher
- Spring Boot 3.2.0 or higher

## Installation

### Maven

Add the following dependency to your `pom.xml`:

```xml
<dependency>
    <groupId>com.cloudcontactai</groupId>
    <artifactId>ccai-java</artifactId>
    <version>1.0.0</version>
</dependency>
```

### Gradle

Add the following to your `build.gradle`:

```gradle
implementation 'com.cloudcontactai:ccai-java:1.0.0'
```

## Configuration

### Environment Variables

Create a `.env` file in your project root or set environment variables:

```bash
CCAI_CLIENT_ID=1231
CCAI_API_KEY=your-api-key-here
CCAI_BASE_URL=https://core.cloudcontactai.com/api
CCAI_EMAIL_BASE_URL=https://email-campaigns.cloudcontactai.com
CCAI_AUTH_BASE_URL=https://auth.cloudcontactai.com
```

### Application Properties

Add to your `application.properties`:

```properties
ccai.client-id=${CCAI_CLIENT_ID}
ccai.api-key=${CCAI_API_KEY}
ccai.base-url=${CCAI_BASE_URL:https://core.cloudcontactai.com/api}
ccai.email-base-url=${CCAI_EMAIL_BASE_URL:https://email-campaigns.cloudcontactai.com}
ccai.auth-base-url=${CCAI_AUTH_BASE_URL:https://auth.cloudcontactai.com}
ccai.debug-mode=false
ccai.timeout-ms=30000
ccai.max-retries=3
```

## Usage

### SMS Basic Usage

```java
import com.cloudcontactai.ccai.client.CCAIClient;
import com.cloudcontactai.ccai.sms.SMSResponse;
import com.cloudcontactai.ccai.exception.CCAIApiException;

// Initialize the client
CCAIClient client = CCAIClient.builder()
    .clientId("your-client-id")
    .apiKey("your-api-key")
    .debugMode(true)
    .build();

try {
    // Send SMS to a single number
    SMSResponse response = client.getSmsService().sendSMS(
        "+1234567890", 
        "Hello from CCAI Java!"
    );
    
    System.out.println("SMS sent successfully: " + response.getCampaignId());
    
} catch (CCAIApiException e) {
    System.err.println("Failed to send SMS: " + e.getMessage());
}
```

### SMS Bulk Usage

```java
import java.util.Arrays;
import java.util.List;

List<String> phoneNumbers = Arrays.asList("+1234567890", "+0987654321");

SMSResponse response = client.getSmsService().sendSMS(
    phoneNumbers,
    "Hello everyone from CCAI Java!"
);

System.out.println("Sent to " + response.getSentCount() + " numbers");
System.out.println("Failed: " + response.getFailedCount() + " numbers");
```

### SMS Advanced Usage

```java
import com.cloudcontactai.ccai.sms.SMSRequest;
import java.util.HashMap;
import java.util.Map;

SMSRequest request = new SMSRequest();
request.setPhoneNumbers(Arrays.asList("+1234567890"));
request.setMessage("Hello {{name}}, your order {{order_id}} is ready!");
request.setCampaignId("welcome-campaign");

// Add custom data
Map<String, Object> customData = new HashMap<>();
customData.put("user_id", "12345");
customData.put("order_id", "ORD-789");
request.setCustomData(customData);

SMSResponse response = client.getSmsService().sendSMS(request);
```

### SMS Async Usage

```java
import java.util.concurrent.CompletableFuture;

CompletableFuture<SMSResponse> future = client.getSmsService().sendSMSAsync(
    "+1234567890",
    "Async SMS message!"
);

future.thenAccept(response -> {
    System.out.println("Async SMS sent: " + response.getCampaignId());
}).exceptionally(throwable -> {
    System.err.println("Async SMS failed: " + throwable.getMessage());
    return null;
});
```

### Email Basic Usage

```java
import com.cloudcontactai.ccai.email.EmailResponse;

EmailResponse response = client.getEmailService().sendEmail(
    "recipient@example.com",
    "Hello from CCAI Java",
    "<h1>Hello!</h1><p>This is a test email from CCAI Java.</p>"
);

System.out.println("Email sent: " + response.getMessageId());
```

### Email Advanced Usage

```java
import com.cloudcontactai.ccai.email.EmailRequest;

EmailRequest request = new EmailRequest();
request.setToEmails(Arrays.asList("user@example.com"));
request.setSubject("Welcome to Our Service");
request.setHtmlContent("<h1>Welcome {{name}}!</h1><p>Thanks for joining us.</p>");
request.setTextContent("Welcome {{name}}! Thanks for joining us.");
request.setFromEmail("noreply@yourcompany.com");
request.setFromName("Your Company");
request.setReplyTo("support@yourcompany.com");

// Add variables for template substitution
Map<String, String> variables = new HashMap<>();
variables.put("name", "John Doe");
request.setVariables(variables);

EmailResponse response = client.getEmailService().sendEmail(request);
```

### Webhook Handling

```java
import com.cloudcontactai.ccai.webhook.WebhookEvent;
import com.cloudcontactai.ccai.webhook.WebhookService;
import org.springframework.web.bind.annotation.*;

@RestController
public class WebhookController {
    
    private final WebhookService webhookService;
    
    public WebhookController(CCAIClient client) {
        this.webhookService = client.getWebhookService();
    }
    
    @PostMapping("/webhook/ccai")
    public ResponseEntity<String> handleWebhook(
            @RequestBody String payload,
            @RequestHeader(value = "X-CCAI-Signature", required = false) String signature) {
        
        try {
            // Validate signature (optional but recommended)
            String webhookSecret = System.getenv("CCAI_WEBHOOK_SECRET");
            if (webhookSecret != null && !webhookService.validateWebhookSignature(payload, signature, webhookSecret)) {
                return ResponseEntity.status(401).body("Invalid signature");
            }
            
            // Parse and handle the event
            WebhookEvent event = webhookService.parseWebhookEvent(payload);
            webhookService.handleWebhookEvent(event);
            
            return ResponseEntity.ok("Webhook processed");
            
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }
}
```

## Spring Boot Integration

### Auto Configuration

The library provides auto-configuration for Spring Boot applications. Simply add the dependency and configure the properties:

```java
@SpringBootApplication
public class MyApplication {
    
    @Autowired
    private CCAIClient ccaiClient;
    
    public static void main(String[] args) {
        SpringApplication.run(MyApplication.class, args);
    }
    
    @EventListener(ApplicationReadyEvent.class)
    public void sendWelcomeSMS() throws CCAIApiException {
        SMSResponse response = ccaiClient.getSmsService().sendSMS(
            "+1234567890",
            "Application started successfully!"
        );
        System.out.println("Welcome SMS sent: " + response.getCampaignId());
    }
}
```

### Custom Configuration

```java
@Configuration
public class CCAIConfiguration {
    
    @Bean
    @Primary
    public CCAIClient customCCAIClient() {
        return CCAIClient.builder()
            .clientId(System.getenv("CCAI_CLIENT_ID"))
            .apiKey(System.getenv("CCAI_API_KEY"))
            .debugMode(true)
            .timeoutMs(60000)
            .maxRetries(5)
            .build();
    }
}
```

## Error Handling

```java
import com.cloudcontactai.ccai.exception.CCAIApiException;

try {
    SMSResponse response = client.getSmsService().sendSMS("+1234567890", "Test");
} catch (CCAIApiException e) {
    System.err.println("API Error: " + e.getMessage());
    System.err.println("Status Code: " + e.getStatusCode());
    System.err.println("Error Code: " + e.getErrorCode());
}
```

## Testing

Run the tests with Maven:

```bash
mvn test
```

Run with coverage:

```bash
mvn test jacoco:report
```

## Examples

The `src/main/java/com/cloudcontactai/ccai/examples` directory contains complete examples:

- `BasicSMSExample.java` - Basic SMS sending examples
- `BasicEmailExample.java` - Basic email sending examples  
- `WebhookExample.java` - Complete webhook handling server

To run the examples:

```bash
# Set environment variables
export CCAI_CLIENT_ID="your-client-id"
export CCAI_API_KEY="your-api-key"

# Run SMS example
mvn exec:java -Dexec.mainClass="com.cloudcontactai.ccai.examples.BasicSMSExample"

# Run email example
mvn exec:java -Dexec.mainClass="com.cloudcontactai.ccai.examples.BasicEmailExample"

# Run webhook server
mvn spring-boot:run -Dspring-boot.run.mainClass="com.cloudcontactai.ccai.examples.WebhookExample"
```

## Building

Build the project:

```bash
mvn clean compile
```

Package the JAR:

```bash
mvn clean package
```

Install to local repository:

```bash
mvn clean install
```

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests for your changes
5. Ensure all tests pass
6. Submit a pull request

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Support

For support, please contact [support@cloudcontactai.com](mailto:support@cloudcontactai.com) or visit our [documentation](https://docs.cloudcontactai.com).
