# CCAI Java SDK

A Kotlin/Java client library for interacting with the [CloudContactAI](https://cloudcontactai.com) API.

## Features

- Send SMS messages to single or multiple recipients
- Send Email campaigns to single or multiple recipients
- Manage webhooks for event notifications
- Variable substitution in messages
- Comprehensive error handling
- Spring Boot integration support

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

### Spring Boot Integration

#### Configuration Bean

```kotlin
@Configuration
class CCAIConfiguration {
    
    @Bean
    fun ccaiConfig(
        @Value("\${ccai.client-id}") clientId: String,
        @Value("\${ccai.api-key}") apiKey: String,
        @Value("\${ccai.use-test-environment:false}") useTestEnvironment: Boolean
    ): CCAIConfig {
        return CCAIConfig(
            clientId = clientId,
            apiKey = apiKey,
            useTestEnvironment = useTestEnvironment
        )
    }
    
    @Bean
    fun ccaiClient(config: CCAIConfig): CCAIClient {
        return CCAIClient(config)
    }
}
```

#### Service Bean

```kotlin
@Service
class NotificationService(private val ccaiClient: CCAIClient) {
    
    fun sendWelcomeSMS(firstName: String, lastName: String, phone: String) {
        val response = ccaiClient.sms.sendSingle(
            firstName = firstName,
            lastName = lastName,
            phone = phone,
            message = "Welcome ${firstName}! Thanks for joining our service.",
            title = "Welcome SMS"
        )
        println("SMS sent with ID: ${response.id}")
    }
    
    fun sendWelcomeEmail(firstName: String, lastName: String, email: String) {
        val response = ccaiClient.email.sendSingle(
            firstName = firstName,
            lastName = lastName,
            email = email,
            subject = "Welcome ${firstName}!",
            htmlContent = """
                <html>
                    <body>
                        <h1>Welcome ${firstName} ${lastName}!</h1>
                        <p>Thank you for joining our service.</p>
                    </body>
                </html>
            """.trimIndent()
        )
        println("Email sent with ID: ${response.id}")
    }
}
```

#### Application Properties

```properties
ccai.client-id=${CCAI_CLIENT_ID}
ccai.api-key=${CCAI_API_KEY}
ccai.use-test-environment=false
```

### Kotlin Usage

#### SMS Basic Usage

```kotlin
import com.cloudcontactai.sdk.CCAIClient
import com.cloudcontactai.sdk.common.CCAIConfig
import com.cloudcontactai.sdk.sms.Account

// Initialize the client
val config = CCAIConfig(
    clientId = System.getenv("CCAI_CLIENT_ID") ?: throw IllegalArgumentException("CCAI_CLIENT_ID not found"),
    apiKey = System.getenv("CCAI_API_KEY") ?: throw IllegalArgumentException("CCAI_API_KEY not found")
)

val ccai = CCAIClient(config)

// Send a single SMS
val response = ccai.sms.sendSingle(
    firstName = "John",
    lastName = "Doe",
    phone = "+15551234567",
    message = "Hello John, this is a test message!",
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
    message = "Hello from our service!",
    title = "Bulk Test Campaign"
)

println("Campaign sent with ID: ${campaignResponse.id}")

ccai.close()
```

#### Email Usage

```kotlin
import com.cloudcontactai.sdk.email.EmailAccount

// Send a single email
val response = ccai.email.sendSingle(
    firstName = "John",
    lastName = "Doe",
    email = "john.doe@example.com",
    subject = "Welcome John!",
    htmlContent = "<h1>Hello John Doe!</h1><p>Welcome to our service.</p>"
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
    subject = "Newsletter",
    htmlContent = "<h1>Hello!</h1><p>Here's your newsletter.</p>"
)

println("Email campaign sent with ID: ${campaignResponse.id}")
```

#### Webhook Management

```kotlin
import com.cloudcontactai.sdk.webhook.WebhookRequest

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
```

### Java Usage

```java
import com.cloudcontactai.sdk.CCAIClient;
import com.cloudcontactai.sdk.common.CCAIConfig;
import com.cloudcontactai.sdk.sms.SMSResponse;

// Initialize the client
CCAIConfig config = new CCAIConfig(
    System.getenv("CCAI_CLIENT_ID"),
    System.getenv("CCAI_API_KEY"),
    "https://core.cloudcontactai.com/api",
    "https://email-campaigns.cloudcontactai.com",
    "https://auth.cloudcontactai.com",
    false
);

CCAIClient ccai = new CCAIClient(config);

// Send SMS
SMSResponse response = ccai.getSms().sendSingle(
    "John",
    "Doe", 
    "+15551234567",
    "Hello John, this is a test message!",
    "Test Campaign",
    null
);

System.out.println("Message sent with ID: " + response.getId());

ccai.close();
```

## Configuration Options

The `CCAIConfig` class supports the following options:

- `clientId`: Your CCAI client ID (required)
- `apiKey`: Your CCAI API key (required)
- `baseUrl`: Base URL for SMS API (default: production URL)
- `emailBaseUrl`: Base URL for Email API (default: production URL)
- `authBaseUrl`: Base URL for Auth API (default: production URL)
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
