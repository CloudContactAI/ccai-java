# CCAI Java SDK

A Kotlin/Java client library for interacting with the [CloudContactAI](https://cloudcontactai.com) API.

## Features

- Send SMS messages to single or multiple recipients
- Send MMS messages with images to single or multiple recipients
- Send Email campaigns to single or multiple recipients
- Brand registration and management for TCR verification
- Campaign registration and management for TCR carrier vetting
- Manage webhooks for event notifications
- Webhook signature validation for security
- Variable substitution in messages
- Test environment support
- Comprehensive error handling
- Spring Boot integration support

## Requirements

- Java 17 or higher
- Kotlin 1.9.0 or higher (if using Kotlin)

## Installation

### Maven

Add this dependency to your `pom.xml`:

```xml
<dependency>
    <groupId>com.cloudcontactai</groupId>
    <artifactId>ccai-java-sdk</artifactId>
    <version>1.0.9</version>
</dependency>
```

### Gradle

Add this dependency to your `build.gradle`:

```gradle
implementation 'com.cloudcontactai:ccai-java-sdk:1.0.9'
```

## Configuration

Set environment variables or pass configuration directly:

```bash
export CCAI_CLIENT_ID=1231
export CCAI_API_KEY=your-api-key-here
export CCAI_USE_TEST_ENVIRONMENT=false
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

#### MMS Usage

##### Image Recommendations

For optimal MMS delivery and performance:

**Dimensions:**
- Recommended: 640px × 1138px (9:16 aspect ratio)
- Alternative: 1080px × 1920px (9:16 aspect ratio)
- Format: Portrait or square orientation preferred

**File Size:**
- Target: ~200 KB (optimal for speed and deliverability)
- Maximum: 1 MB
- Use image compression tools to reduce file size while maintaining quality

**Supported Formats:**
- JPEG (recommended)
- PNG
- GIF

**Best Practice:** Keep images under 500 KB with 640×1138px dimensions for optimal compatibility and performance.

##### Code Examples

```kotlin
import com.cloudcontactai.sdk.mms.Account
import com.cloudcontactai.sdk.mms.SignedUploadUrlRequest
import java.io.File

// ── Option A: All-in-one (recommended) ─────────────────────────────────────
val mmsAccounts = listOf(
    Account(firstName = "John", lastName = "Doe", phone = "+15551234567")
)

val imageFile = File("path/to/image.jpg")
val mmsResponse = ccai.mms.sendWithImage(
    accounts = mmsAccounts,
    message = "Check out this image!",
    title = "MMS Campaign",
    imageFile = imageFile
    // optional: senderPhone = "+15559990000"
)
val responseId = mmsResponse.campaignId ?: mmsResponse.id
println("MMS sent with ID: ${responseId}")

// ── Option B: Manual workflow (step-by-step) ────────────────────────────────

// Step 1 — Get a pre-signed S3 upload URL
val uploadRequest = SignedUploadUrlRequest(fileName = "image.jpg", fileType = "image/jpeg")
val uploadResponse = ccai.mms.getSignedUploadUrl(uploadRequest)

// Step 2 — Upload the image to S3
ccai.mms.uploadImageToSignedUrl(uploadResponse.signedS3Url, imageFile, "image/jpeg")

// Step 3 — (Optional) Confirm the file is available
val stored = ccai.mms.checkFileUploaded(uploadResponse.fileKey!!)
println("File URL: ${stored.storedUrl}")

// Step 4a — Send to multiple recipients using the uploaded fileKey
val bulkResponse = ccai.mms.send(
    accounts = mmsAccounts,
    message = "Hello ${firstName}!",
    title = "MMS Campaign",
    pictureFileKey = uploadResponse.fileKey!!
    // optional: senderPhone = "+15559990000"
)

// Step 4b — Send to a single recipient
val singleResponse = ccai.mms.sendSingle(
    firstName = "John",
    lastName = "Doe",
    phone = "+15551234567",
    message = "Hello ${firstName}!",
    title = "MMS Campaign",
    pictureFileKey = uploadResponse.fileKey!!
    // optional: senderPhone = "+15559990000"
)
```

#### Webhook Management

```kotlin
import com.cloudcontactai.sdk.webhook.WebhookRequest
import com.cloudcontactai.sdk.webhook.WebhookUpdateRequest

// Create a webhook (auto-generated secret)
val webhook = ccai.webhook.create(WebhookRequest("https://your-app.com/webhooks/ccai"))
println("Webhook created with ID: ${webhook.id}")
println("URL: ${webhook.url}")
println("Secret Key: ${webhook.secretKey}")

// Create a webhook with custom secret
val customWebhook = ccai.webhook.create(
    WebhookRequest("https://your-app.com/webhooks/ccai", "my-custom-secret-32chars12345")
)
println("Webhook created with custom secret!")

// Get all webhooks
val allWebhooks = ccai.webhook.getAll()
allWebhooks.forEach { wh ->
    println("Webhook ID: ${wh.id}, URL: ${wh.url}")
}

// Get a specific webhook by ID
val webhookDetails = ccai.webhook.get(webhook.id)
println("Current webhook URL: ${webhookDetails.url}")
println("Method: ${webhookDetails.method}")
println("Secret Key: ${webhookDetails.secretKey}")

// Update webhook
val updated = ccai.webhook.update(
    WebhookUpdateRequest(webhook.id, "https://your-app.com/webhooks/ccai-updated", "my-custom-secret-32chars12345")
)
println("Webhook updated to: ${updated.url}")

// Delete a webhook
ccai.webhook.delete(webhook.id)

// Validate CloudContactAI webhook signature (using eventHash)
val payload = """
{
    "eventType": "sms.sent",
    "data": {
        "id": 12345,
        "MessageStatus": "sent",
        "To": "+15551234567",
        "Message": "Hello World"
    },
    "eventHash": "abc123def456ghi789jkl012mno345pq"
}
"""
val signature = request.getHeader("X-CCAI-Signature")
val event = ccai.webhook.parseWebhookEvent(payload)

val isValid = ccai.webhook.validateSignature(
    signature,
    webhook.secretKey!!,
    config.clientId.toLong(),
    event.eventHash
)

if (isValid) {
    println("Event type: ${event.eventType}")
    println("Event hash: ${event.eventHash}")
    println("Data: ${event.data}")
}
```

#### Brand Registration

```kotlin
import com.cloudcontactai.sdk.brands.BrandRequest

// Create a brand
val brand = ccai.brands.create(BrandRequest(
    legalCompanyName = "Collect.org Inc.",
    dba = "Collect",
    entityType = "NON_PROFIT",
    taxId = "123456789",
    taxIdCountry = "US",
    country = "US",
    verticalType = "NON_PROFIT",
    websiteUrl = "https://www.collect.org",
    street = "123 Main Street",
    city = "San Francisco",
    state = "CA",
    postalCode = "94105",
    contactFirstName = "Jane",
    contactLastName = "Doe",
    contactEmail = "jane@collect.org",
    contactPhone = "+14155551234"
))
println("Brand created with ID: ${brand.id}")

// Get a brand by ID
val fetched = ccai.brands.get(brand.id)
println("Website match score: ${fetched.websiteMatchScore ?: "pending"}")

// List all brands for the account
val brands = ccai.brands.list()
println("Found ${brands.size} brand(s)")

// Update a brand (partial update)
val updated = ccai.brands.update(brand.id, BrandRequest(
    street = "456 Oak Avenue",
    city = "Los Angeles"
))

// Delete a brand
ccai.brands.delete(brand.id)
```

**Entity Types:** `PRIVATE_PROFIT`, `PUBLIC_PROFIT`, `NON_PROFIT`, `GOVERNMENT`, `SOLE_PROPRIETOR`

> Note: `PUBLIC_PROFIT` entities require `stockSymbol` and `stockExchange` fields.

**Vertical Types:** `AUTOMOTIVE`, `AGRICULTURE`, `BANKING`, `COMMUNICATION`, `CONSTRUCTION`, `EDUCATION`, `ENERGY`, `ENTERTAINMENT`, `GOVERNMENT`, `HEALTHCARE`, `HOSPITALITY`, `INSURANCE`, `LEGAL`, `MANUFACTURING`, `NON_PROFIT`, `PROFESSIONAL`, `REAL_ESTATE`, `RETAIL`, `TECHNOLOGY`, `TRANSPORTATION`

#### Campaign Registration

```kotlin
import com.cloudcontactai.sdk.campaigns.CampaignRequest

// Create a campaign
val campaign = ccai.campaigns.create(CampaignRequest(
    brandId = 1,
    useCase = "MIXED",
    subUseCases = listOf("CUSTOMER_CARE", "TWO_FACTOR_AUTHENTICATION", "ACCOUNT_NOTIFICATION"),
    description = "Security codes and support messaging.",
    messageFlow = "Users opt-in via signup form at https://example.com/signup",
    hasEmbeddedLinks = true,
    hasEmbeddedPhone = false,
    isAgeGated = false,
    isDirectLending = false,
    optInKeywords = listOf("START"),
    optInMessage = "Welcome! Reply STOP to cancel.",
    optInProofUrl = "https://example.com/opt-in-proof.png",
    helpKeywords = listOf("HELP"),
    helpMessage = "For HELP email support@example.com.",
    optOutKeywords = listOf("STOP"),
    optOutMessage = "STOP received. You are unsubscribed.",
    sampleMessages = listOf(
        "Your code is 554321. Reply STOP to cancel.",
        "Your ticket has been updated. Reply HELP for info."
    )
))
println("Campaign created with ID: ${campaign.id}")

// Get a campaign by ID
val fetched = ccai.campaigns.get(campaign.id)

// List all campaigns for the account
val campaigns = ccai.campaigns.list()
println("Found ${campaigns.size} campaign(s)")

// Update a campaign (partial update)
val updated = ccai.campaigns.update(campaign.id, CampaignRequest(
    description = "Updated description."
))

// Delete a campaign
ccai.campaigns.delete(campaign.id)
```

**Use Cases:** `TWO_FACTOR_AUTHENTICATION`, `ACCOUNT_NOTIFICATION`, `CUSTOMER_CARE`, `DELIVERY_NOTIFICATION`, `FRAUD_ALERT`, `HIGHER_EDUCATION`, `LOW_VOLUME_MIXED`, `MARKETING`, `MIXED`, `POLLING_VOTING`, `PUBLIC_SERVICE_ANNOUNCEMENT`, `SECURITY_ALERT`

> Note: `MIXED` and `LOW_VOLUME_MIXED` campaigns require 2–3 `subUseCases`.

**Sub-Use Cases:** `TWO_FACTOR_AUTHENTICATION`, `ACCOUNT_NOTIFICATION`, `CUSTOMER_CARE`, `DELIVERY_NOTIFICATION`, `FRAUD_ALERT`, `MARKETING`, `POLLING_VOTING`

#### Contact Management

```kotlin
import com.cloudcontactai.sdk.contact.ContactService

// Opt a contact out of text messages (by phone number)
ccai.contact.setDoNotText(phone = "+15551234567", doNotText = true)

// Opt a contact back in (by phone number)
ccai.contact.setDoNotText(phone = "+15551234567", doNotText = false)

// Opt out by contactId
ccai.contact.setDoNotText(contactId = "contact-abc-123", doNotText = true)
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
    false  // useTestEnvironment
);

CCAIClient ccai = new CCAIClient(config);

// Send SMS
SMSResponse response = ccai.getSms().sendSingle(
    "John",
    "Doe", 
    "+15551234567",
    "Hello John, this is a test message!",
    "Test Campaign",
    null  // optional sender phone
);

System.out.println("Message sent with ID: " + response.getId());

ccai.close();
```

## Configuration Options

The `CCAIConfig` class supports the following options:

- `clientId`: Your CCAI client ID (required)
- `apiKey`: Your CCAI API key (required)
- `useTestEnvironment`: Whether to use test environment URLs (default: false)
- `debugMode`: Enable debug logging (default: false)
- `maxRetries`: Maximum retry attempts for failed requests (default: 3)
- `timeoutMs`: Request timeout in milliseconds (default: 30000)

The SDK automatically configures the following URLs based on `useTestEnvironment`:
- `baseUrl`: SMS/MMS API endpoint
- `emailBaseUrl`: Email API endpoint
- `authBaseUrl`: Authentication API endpoint
- `filesBaseUrl`: File upload API endpoint (for MMS)

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
