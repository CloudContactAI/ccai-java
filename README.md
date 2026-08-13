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
- Validate email addresses (valid/invalid/risky) and phone numbers (valid/invalid/landline)
- Variable substitution in messages
- Test environment support
- Comprehensive error handling
- Spring Boot integration support

## Requirements

- Java 11 or higher
- Kotlin 1.9.0 or higher (if using Kotlin)

## Installation

### Maven

Add this dependency to your `pom.xml`:

```xml
<dependency>
    <groupId>com.cloudcontactai</groupId>
    <artifactId>ccai-java-sdk</artifactId>
    <version>1.2.0</version>
</dependency>
```

### Gradle

Add this dependency to your `build.gradle`:

```gradle
implementation 'com.cloudcontactai:ccai-java-sdk:1.2.0'
```

## Configuration

The SDK does not read environment variables on its own — `CCAIConfig` always requires `clientId` and `apiKey` to be passed explicitly to its constructor. The usual pattern is to set environment variables and read them yourself before constructing the config:

```bash
export CCAI_CLIENT_ID=1231
export CCAI_API_KEY=your-api-key-here
export CCAI_USE_TEST_ENVIRONMENT=false
```

```kotlin
val config = CCAIConfig(
    clientId = System.getenv("CCAI_CLIENT_ID") ?: throw IllegalArgumentException("CCAI_CLIENT_ID not found"),
    apiKey = System.getenv("CCAI_API_KEY") ?: throw IllegalArgumentException("CCAI_API_KEY not found"),
    useTestEnvironment = System.getenv("CCAI_USE_TEST_ENVIRONMENT")?.toBoolean() ?: false
)
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

#### SMS — Template-Controlled Accounts

If an account has been configured to enforce template-only messaging, all campaigns must reference a pre-approved template ID. Sending a free-text message to such an account will result in a `422` error.

```kotlin
// Send to multiple recipients using a template
val response = ccai.sms.sendWithTemplate(
    accounts = accounts,
    templateId = 12345L,   // the ID of the approved template
    title = "My Campaign"
)

// Send to a single recipient using a template
val response = ccai.sms.sendSingleWithTemplate(
    firstName = "John",
    lastName = "Doe",
    phone = "+15551234567",
    templateId = 12345L,
    title = "My Campaign"
)

println("Campaign sent with ID: ${response.id}")

ccai.close()
```

The message body is resolved server-side from the template. Variable substitution (e.g. `${firstName}`) is applied automatically using the recipient's account data.

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

#### Contact Validator

Validate email addresses and phone numbers.

> Bulk endpoints accept up to 50 contacts per request and are processed server-side in chunks.

```kotlin
import com.cloudcontactai.sdk.contactvalidator.PhoneInput

// Validate a single email
val emailResult = ccai.contactValidator.validateEmail("user@example.com")
println(emailResult.status) // "valid" | "invalid" | "risky"
println(emailResult.metadata["safe_to_send"]) // true | false

// Validate multiple emails (up to 50, processed server-side in chunks)
val bulkEmails = ccai.contactValidator.validateEmails(listOf(
    "user@example.com",
    "bad@invalid.xyz"
))
println(bulkEmails.summary) // ValidationSummary(total=2, valid=1, invalid=1, risky=0, landline=0)

// Validate a single phone number
val phoneResult = ccai.contactValidator.validatePhone("+15551234567", countryCode = "US")
println(phoneResult.status) // "valid" | "invalid" | "landline"
println(phoneResult.metadata["carrier_type"]) // "mobile" | "landline" | "voip"

// Validate multiple phone numbers (up to 50, processed server-side in chunks)
val bulkPhones = ccai.contactValidator.validatePhones(listOf(
    PhoneInput(phone = "+15551234567"),
    PhoneInput(phone = "+15559876543", countryCode = "US")
))
println(bulkPhones.summary) // ValidationSummary(total=2, valid=1, invalid=0, risky=0, landline=1)
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
    termsLink = "https://example.com/terms",
    privacyLink = "https://example.com/privacy",
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

> `termsLink` and `privacyLink` are optional fields on `CampaignRequest`/`CampaignResponse`.

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

From Java, call one of the overloads declared on each service with a matching argument list — parameters with default values in Kotlin are not available as omittable arguments through Java.

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

// Send SMS (5-arg overload — no senderPhone)
SMSResponse response = ccai.getSms().sendSingle(
    "John",
    "Doe", 
    "+15551234567",
    "Hello John, this is a test message!",
    "Test Campaign"
);

System.out.println("Message sent with ID: " + response.getId());

ccai.close();
```

#### Contact Validator (Java)

`validatePhone` takes both arguments (pass `null` for `countryCode` if you don't have one), and `PhoneInput`'s constructor takes both `phone` and `countryCode` (`null` allowed for `countryCode`).

```java
import com.cloudcontactai.sdk.contactvalidator.EmailValidationResult;
import com.cloudcontactai.sdk.contactvalidator.PhoneValidationResult;
import com.cloudcontactai.sdk.contactvalidator.BulkPhoneValidationResult;
import com.cloudcontactai.sdk.contactvalidator.PhoneInput;

import java.util.Arrays;
import java.util.List;

// Validate a single email
EmailValidationResult emailResult = ccai.getContactValidator().validateEmail("user@example.com");
System.out.println(emailResult.getStatus()); // "valid" | "invalid" | "risky"

// Validate a single phone number
PhoneValidationResult phoneResult = ccai.getContactValidator().validatePhone("+15551234567", "US");
System.out.println(phoneResult.getStatus()); // "valid" | "invalid" | "landline"

// Validate multiple phone numbers (up to 50, processed server-side in chunks)
List<PhoneInput> phones = Arrays.asList(
    new PhoneInput("+15551234567", null),
    new PhoneInput("+15559876543", "US")
);
BulkPhoneValidationResult bulkPhones = ccai.getContactValidator().validatePhones(phones);
System.out.println(bulkPhones.getSummary());
```

#### Brand Registration (Java)

`BrandRequest` fields are passed positionally (use `null` for fields you don't want to set), in this order:
`legalCompanyName, dba, entityType, taxId, taxIdCountry, country, verticalType, websiteUrl, stockSymbol, stockExchange, street, city, state, postalCode, contactFirstName, contactLastName, contactEmail, contactPhone, websiteMatch`.

```java
import com.cloudcontactai.sdk.brands.BrandRequest;
import com.cloudcontactai.sdk.brands.BrandResponse;

// Create a brand
BrandRequest request = new BrandRequest(
    "Collect.org Inc.", "Collect", "NON_PROFIT",
    "123456789", "US", "US", "NON_PROFIT",
    "https://www.collect.org", null, null,
    "123 Main Street", "San Francisco", "CA", "94105",
    "Jane", "Doe", "jane@collect.org", "+14155551234", false
);
BrandResponse brand = ccai.getBrands().create(request);
System.out.println("Brand created with ID: " + brand.getId());

// Get a brand by ID
BrandResponse fetched = ccai.getBrands().get(brand.getId());
System.out.println("Website match score: " + fetched.getWebsiteMatchScore());

// List all brands for the account
BrandResponse[] brands = ccai.getBrands().list();
System.out.println("Found " + brands.length + " brand(s)");

// Update a brand (partial update — unset fields must be passed as null)
BrandRequest updateRequest = new BrandRequest(
    null, null, null, null, null, null, null, null, null, null,
    "456 Oak Avenue", "Los Angeles", null, null,
    null, null, null, null, false
);
BrandResponse updated = ccai.getBrands().update(brand.getId(), updateRequest);

// Delete a brand
ccai.getBrands().delete(brand.getId());
```

**Entity Types:** `PRIVATE_PROFIT`, `PUBLIC_PROFIT`, `NON_PROFIT`, `GOVERNMENT`, `SOLE_PROPRIETOR`

> Note: `PUBLIC_PROFIT` entities require `stockSymbol` and `stockExchange` fields.

#### Campaign Registration (Java)

`CampaignRequest` fields are passed positionally, in this order:
`brandId, useCase, subUseCases, description, messageFlow, termsLink, privacyLink, hasEmbeddedLinks, hasEmbeddedPhone, isAgeGated, isDirectLending, optInKeywords, optInMessage, optInProofUrl, helpKeywords, helpMessage, optOutKeywords, optOutMessage, sampleMessages`.

```java
import com.cloudcontactai.sdk.campaigns.CampaignRequest;
import com.cloudcontactai.sdk.campaigns.CampaignResponse;

import java.util.Arrays;

// Create a campaign (assumes brand ID 1 exists)
CampaignResponse campaign = ccai.getCampaigns().create(new CampaignRequest(
    1L,                          // brandId
    "MIXED",                     // useCase
    Arrays.asList("CUSTOMER_CARE", "TWO_FACTOR_AUTHENTICATION", "ACCOUNT_NOTIFICATION"),
    "Security codes and support messaging.",
    "Users opt-in via signup form at https://example.com/signup",
    "https://example.com/terms",     // termsLink
    "https://example.com/privacy",   // privacyLink
    true,                         // hasEmbeddedLinks
    false,                        // hasEmbeddedPhone
    false,                        // isAgeGated
    false,                        // isDirectLending
    Arrays.asList("START"),
    "Welcome! Reply STOP to cancel.",
    "https://example.com/opt-in-proof.png",
    Arrays.asList("HELP"),
    "For HELP email support@example.com.",
    Arrays.asList("STOP"),
    "STOP received. You are unsubscribed.",
    Arrays.asList(
        "Your code is 554321. Reply STOP to cancel.",
        "Your ticket has been updated. Reply HELP for info."
    )
));
System.out.println("Campaign created with ID: " + campaign.getId());

// Get a campaign by ID
CampaignResponse fetched = ccai.getCampaigns().get(campaign.getId());

// List all campaigns for the account
CampaignResponse[] campaigns = ccai.getCampaigns().list();
System.out.println("Found " + campaigns.length + " campaign(s)");

// Delete a campaign
ccai.getCampaigns().delete(campaign.getId());
```

**Use Cases:** `TWO_FACTOR_AUTHENTICATION`, `ACCOUNT_NOTIFICATION`, `CUSTOMER_CARE`, `DELIVERY_NOTIFICATION`, `FRAUD_ALERT`, `HIGHER_EDUCATION`, `LOW_VOLUME_MIXED`, `MARKETING`, `MIXED`, `POLLING_VOTING`, `PUBLIC_SERVICE_ANNOUNCEMENT`, `SECURITY_ALERT`

> Note: `MIXED` and `LOW_VOLUME_MIXED` campaigns require 2–3 `subUseCases`.

## Configuration Options

The `CCAIConfig` class supports the following options:

- `clientId`: Your CCAI client ID (required)
- `apiKey`: Your CCAI API key (required)
- `useTestEnvironment`: Whether to use test environment URLs (default: false)

The SDK automatically configures the following URLs based on `useTestEnvironment` (each overridable via its own environment variable — see `CCAIConfig`):
- `baseUrl` (`CCAI_BASE_URL`): SMS/MMS campaign API endpoint
- `emailBaseUrl` (`CCAI_EMAIL_BASE_URL`): Email API endpoint
- `authBaseUrl` (`CCAI_AUTH_BASE_URL`): Authentication API endpoint
- `filesBaseUrl` (`CCAI_FILES_BASE_URL`): File upload API endpoint (for MMS)
- `complianceBaseUrl` (`CCAI_COMPLIANCE_BASE_URL`): Brand/Campaign registration API endpoint

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
git clone https://github.com/cloudcontactai/ccai-java.git
cd ccai-java
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
