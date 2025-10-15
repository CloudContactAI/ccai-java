# CCAI Java SDK

A Kotlin/Java client library for interacting with the [CloudContactAI](https://cloudcontactai.com) API.

## Features

- Send SMS messages to single or multiple recipients
- Send MMS messages with images
- Send Email campaigns to single or multiple recipients
- Upload images to S3 with signed URLs
- Variable substitution in messages
- Manage webhooks for event notifications
- Async/await support with Kotlin coroutines
- Progress tracking
- Comprehensive error handling
- Full test coverage

## Requirements

- Java 11 or higher
- Kotlin 1.7.0 or higher (if using Kotlin)

## Installation

### Maven

Add this dependency to your `pom.xml`:

```xml
<dependency>
    <groupId>com.cloudcontactai</groupId>
    <artifactId>ccai-java-sdk</artifactId>
    <version>1.0.0</version>
</dependency>
```

### Gradle

Add this dependency to your `build.gradle`:

```gradle
implementation 'com.cloudcontactai:ccai-java-sdk:1.0.0'
```

## Configuration

Set environment variables or pass configuration directly:

```bash
export CCAI_CLIENT_ID=1231
export CCAI_API_KEY=your-api-key-here
```

## Usage

### Kotlin Usage

#### SMS Basic Usage

```kotlin
import com.cloudcontactai.ccai.CCAIClient
import com.cloudcontactai.ccai.common.CCAIConfig
import com.cloudcontactai.ccai.sms.Account
import kotlinx.coroutines.runBlocking

// Initialize the client
val config = CCAIConfig(
    clientId = System.getenv("CCAI_CLIENT_ID") ?: throw IllegalArgumentException("CCAI_CLIENT_ID not found"),
    apiKey = System.getenv("CCAI_API_KEY") ?: throw IllegalArgumentException("CCAI_API_KEY not found")
)

val ccai = CCAIClient(config)

runBlocking {
    // Send a single SMS
    val response = ccai.sms.sendSingle(
        firstName = "John",
        lastName = "Doe",
        phone = "+15551234567",
        message = "Hello \${FirstName}, this is a test message!",
        title = "Test Campaign"
    )
    
    println("Message sent with ID: ${response.id}")
    
    // Send to multiple recipients
    val accounts = listOf(
        Account(
            firstName = "John",
            lastName = "Doe",
            phone = "+15551234567"
        ),
        Account(
            firstName = "Jane",
            lastName = "Smith",
            phone = "+15559876543"
        )
    )
    
    val campaignResponse = ccai.sms.send(
        accounts = accounts,
        message = "Hello \${FirstName} \${LastName}, this is a test message!",
        title = "Bulk Test Campaign"
    )
    
    println("Campaign sent with ID: ${campaignResponse.campaignId}")
}

ccai.close()
```

#### Email Usage

```kotlin
import com.cloudcontactai.ccai.email.EmailAccount

runBlocking {
    // Send a single email
    val response = ccai.email.sendSingle(
        firstName = "John",
        lastName = "Doe",
        email = "john.doe@example.com",
        subject = "Welcome \${FirstName}!",
        htmlContent = "<h1>Hello \${FirstName} \${LastName}!</h1><p>Welcome to our service.</p>",
        textContent = "Hello \${FirstName} \${LastName}! Welcome to our service."
    )
    
    println("Email sent with ID: ${response.id}")
    
    // Send email campaign
    val emailAccounts = listOf(
        EmailAccount(
            firstName = "John",
            lastName = "Doe",
            email = "john.doe@example.com"
        ),
        EmailAccount(
            firstName = "Jane",
            lastName = "Smith",
            email = "jane.smith@example.com"
        )
    )
    
    val campaignResponse = ccai.email.send(
        accounts = emailAccounts,
        subject = "Newsletter for \${FirstName}",
        htmlContent = "<h1>Hello \${FirstName}!</h1><p>Here's your newsletter.</p>"
    )
    
    println("Email campaign sent with ID: ${campaignResponse.campaignId}")
}
```

#### Webhook Management

```kotlin
import com.cloudcontactai.ccai.webhook.WebhookRequest

runBlocking {
    // Create a webhook
    val webhookRequest = WebhookRequest(
        url = "https://your-app.com/webhooks/ccai",
        events = listOf("sms.sent", "sms.delivered", "email.opened"),
        isActive = true
    )
    
    val webhook = ccai.webhook.create(webhookRequest)
    println("Webhook created with ID: ${webhook.id}")
    
    // Get all webhooks
    val webhooks = ccai.webhook.getAll()
    println("Total webhooks: ${webhooks.size}")
    
    // Test webhook
    val testResult = ccai.webhook.test(webhook.id)
    println("Webhook test successful: ${testResult.success}")
}
```

### Java Usage

#### SMS Basic Usage

```java
import com.cloudcontactai.ccai.CCAIClient;
import com.cloudcontactai.ccai.common.CCAIConfig;
import com.cloudcontactai.ccai.sms.Account;
import com.cloudcontactai.ccai.sms.SMSResponse;
import kotlinx.coroutines.BuildersKt;
import kotlinx.coroutines.Dispatchers;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

// Initialize the client
CCAIConfig config = new CCAIConfig(
    System.getenv("CCAI_CLIENT_ID"),
    System.getenv("CCAI_API_KEY"),
    "https://core.cloudcontactai.com/api", // baseUrl
    "https://email-campaigns.cloudcontactai.com", // emailBaseUrl
    "https://auth.cloudcontactai.com", // authBaseUrl
    false // useTestEnvironment
);

CCAIClient ccai = new CCAIClient(config);

// Send SMS using CompletableFuture for Java compatibility
CompletableFuture<SMSResponse> future = CompletableFuture.supplyAsync(() -> {
    try {
        return BuildersKt.runBlocking(Dispatchers.getIO(), (scope, continuation) -> 
            ccai.getSms().sendSingle(
                "John",
                "Doe", 
                "+15551234567",
                "Hello ${FirstName}, this is a test message!",
                "Test Campaign",
                continuation
            )
        );
    } catch (Exception e) {
        throw new RuntimeException(e);
    }
});

SMSResponse response = future.get();
System.out.println("Message sent with ID: " + response.getId());

ccai.close();
```

## Configuration Options

The `CCAIConfig` class supports the following options:

- `clientId`: Your CCAI client ID (required)
- `apiKey`: Your CCAI API key (required)
- `baseUrl`: Base URL for SMS/MMS API (default: from CCAI_BASE_URL env var or production URL)
- `emailBaseUrl`: Base URL for Email API (default: from CCAI_EMAIL_BASE_URL env var or production URL)
- `authBaseUrl`: Base URL for Auth API (default: from CCAI_AUTH_BASE_URL env var or production URL)
- `useTestEnvironment`: Whether to use test environment URLs (default: false)

## Error Handling

The SDK throws `CCAIException` for API errors:

```kotlin
try {
    val response = ccai.sms.sendSingle(
        firstName = "John",
        lastName = "Doe",
        phone = "invalid-phone",
        message = "Test message",
        title = "Test"
    )
} catch (e: CCAIException) {
    println("API Error: ${e.message}")
}
```

## Building from Source

```bash
git clone https://github.com/cloudcontactai/ccai-java-sdk.git
cd ccai-java-sdk
mvn clean install
```

## Testing

```bash
mvn test
```

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Support

For support, email support@cloudcontactai.com or visit [https://cloudcontactai.com](https://cloudcontactai.com).
