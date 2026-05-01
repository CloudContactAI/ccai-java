package com.ccai.integration

import com.cloudcontactai.sdk.CCAIClient
import com.cloudcontactai.sdk.common.CCAIConfig
import com.cloudcontactai.sdk.sms.Account as SmsAccount
import com.cloudcontactai.sdk.mms.Account as MmsAccount
import com.cloudcontactai.sdk.email.EmailAccount
import com.cloudcontactai.sdk.webhook.WebhookRequest
import com.cloudcontactai.sdk.webhook.WebhookUpdateRequest
import com.cloudcontactai.sdk.brands.BrandRequest
import com.cloudcontactai.sdk.campaigns.CampaignRequest
import java.io.File
import java.util.Base64
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import kotlin.system.exitProcess

// ---------------------------------------------------------------------------
// Environment variables
// ---------------------------------------------------------------------------
val clientId   = System.getenv("CCAI_CLIENT_ID")        ?: ""
val apiKey     = System.getenv("CCAI_API_KEY")           ?: ""
val phone1     = System.getenv("CCAI_TEST_PHONE")        ?: ""
val phone2     = System.getenv("CCAI_TEST_PHONE_2")      ?: ""
val phone3     = System.getenv("CCAI_TEST_PHONE_3")      ?: ""
val email1     = System.getenv("CCAI_TEST_EMAIL")        ?: ""
val email2     = System.getenv("CCAI_TEST_EMAIL_2")      ?: ""
val email3     = System.getenv("CCAI_TEST_EMAIL_3")      ?: ""
val first1     = System.getenv("CCAI_TEST_FIRST_NAME")   ?: "Docker"
val last1      = System.getenv("CCAI_TEST_LAST_NAME")    ?: "Test"
val first2     = System.getenv("CCAI_TEST_FIRST_NAME_2") ?: "Docker2"
val last2      = System.getenv("CCAI_TEST_LAST_NAME_2")  ?: "Test2"
val first3     = System.getenv("CCAI_TEST_FIRST_NAME_3") ?: "Docker3"
val last3      = System.getenv("CCAI_TEST_LAST_NAME_3")  ?: "Test3"
val webhookUrl = System.getenv("WEBHOOK_URL")            ?: "https://webhook.site/java-docker-test"

// ---------------------------------------------------------------------------
// Test runner state
// ---------------------------------------------------------------------------
var passed = 0
var failed = 0

fun runTest(label: String, block: () -> Unit) {
    try {
        block()
        println("  [PASS] $label")
        passed++
    } catch (e: Throwable) {
        val msg = e.message?.lines()?.firstOrNull() ?: "unknown error"
        println("  [FAIL] $label: $msg")
        failed++
    }
}

// ---------------------------------------------------------------------------
// Main
// ---------------------------------------------------------------------------
fun main() {
    if (clientId.isBlank() || apiKey.isBlank()) {
        System.err.println("ERROR: CCAI_CLIENT_ID and CCAI_API_KEY environment variables are required.")
        exitProcess(1)
    }

    val config = CCAIConfig(
        clientId = clientId,
        apiKey = apiKey,
        useTestEnvironment = true
    )
    val client = CCAIClient(config)

    // Write a 1x1 transparent PNG to a temp file for MMS tests
    val imageBytes = Base64.getDecoder().decode(
        "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mP8z8BQDwADhQGAWjR9awAAAABJRU5ErkJggg=="
    )
    val imageFile = File.createTempFile("ccai_test", ".png").also {
        it.writeBytes(imageBytes)
        it.deleteOnExit()
    }

    println("=== CCAI Java (Kotlin) SDK Integration Tests ===\n")

    // -----------------------------------------------------------------------
    // SMS Tests (01–06)
    // -----------------------------------------------------------------------
    println("--- SMS ---")

    runTest("01 SMS sendSingle") {
        val res = client.sms.sendSingle(first1, last1, phone1, "Hello \${firstName}!", "Java Test 01")
        check(res.id.isNotBlank()) { "Empty id in response" }
    }

    runTest("02 SMS send (1 recipient)") {
        val accounts = listOf(SmsAccount(first1, last1, phone1))
        val res = client.sms.send(accounts, "Bulk test \${firstName}", "Java Test 02")
        check(res.id.isNotBlank()) { "Empty id in response" }
    }

    runTest("03 SMS send (2 recipients)") {
        val accounts = listOf(
            SmsAccount(first1, last1, phone1),
            SmsAccount(first2, last2, phone2)
        )
        val res = client.sms.send(accounts, "Multi-recipient \${firstName}", "Java Test 03")
        check(res.id.isNotBlank()) { "Empty id in response" }
    }

    runTest("04 SMS send (3 recipients)") {
        val accounts = listOf(
            SmsAccount(first1, last1, phone1),
            SmsAccount(first2, last2, phone2),
            SmsAccount(first3, last3, phone3)
        )
        val res = client.sms.send(accounts, "Triple-recipient \${firstName}", "Java Test 04")
        check(res.id.isNotBlank()) { "Empty id in response" }
    }

    runTest("05 SMS send with data (template variables)") {
        val accounts = listOf(
            SmsAccount(first1, last1, phone1, customFields = mapOf("city" to "Miami", "code" to "JV5"))
        )
        val res = client.sms.send(accounts, "Hello \${firstName}, code \${code} from \${city}", "Java Test 05")
        check(res.id.isNotBlank()) { "Empty id in response" }
    }

    runTest("06 SMS sendSingle with customData") {
        val res = client.sms.sendSingle(
            first1, last1, phone1,
            "Custom data test", "Java Test 06",
            """{"source":"java-integration"}"""
        )
        check(res.id.isNotBlank()) { "Empty id in response" }
    }

    // -----------------------------------------------------------------------
    // MMS Tests (07–17)
    // -----------------------------------------------------------------------
    println("\n--- MMS ---")

    var signedUrl: String? = null
    var fileKey: String? = null

    runTest("07 MMS getSignedUploadUrl") {
        val req = com.cloudcontactai.sdk.mms.SignedUploadUrlRequest(
            fileName = "java_test.png",
            fileType = "image/png",
            publicFile = true
        )
        val res = client.mms.getSignedUploadUrl(req)
        check(res.signedS3Url.isNotBlank()) { "Missing signedS3Url" }
        signedUrl = res.signedS3Url
        fileKey   = res.fileKey
    }

    runTest("08 MMS uploadImageToSignedUrl") {
        val url = signedUrl ?: error("Dependency test 07 failed — skipping")
        client.mms.uploadImageToSignedUrl(url, imageFile, "image/png")
    }

    runTest("09 MMS sendSingle") {
        val fk = fileKey ?: error("Dependency test 07 failed — skipping")
        val res = client.mms.sendSingle(first1, last1, phone1, "MMS single test", "Java MMS 09", fk)
        checkNotNull(res) { "Null response" }
    }

    runTest("10 MMS send (1 recipient)") {
        val fk = fileKey ?: error("Dependency test 07 failed — skipping")
        val accounts = listOf(MmsAccount(first1, last1, phone1))
        val res = client.mms.send(accounts, "MMS bulk test", "Java MMS 10", fk)
        checkNotNull(res) { "Null response" }
    }

    runTest("11 MMS send (2 recipients)") {
        val fk = fileKey ?: error("Dependency test 07 failed — skipping")
        val accounts = listOf(
            MmsAccount(first1, last1, phone1),
            MmsAccount(first2, last2, phone2)
        )
        val res = client.mms.send(accounts, "MMS 2-recipient test", "Java MMS 11", fk)
        checkNotNull(res) { "Null response" }
    }

    runTest("12 MMS send (3 recipients)") {
        val fk = fileKey ?: error("Dependency test 07 failed — skipping")
        val accounts = listOf(
            MmsAccount(first1, last1, phone1),
            MmsAccount(first2, last2, phone2),
            MmsAccount(first3, last3, phone3)
        )
        val res = client.mms.send(accounts, "MMS 3-recipient test", "Java MMS 12", fk)
        checkNotNull(res) { "Null response" }
    }

    runTest("13 MMS send with data (template variables)") {
        val fk = fileKey ?: error("Dependency test 07 failed — skipping")
        val accounts = listOf(MmsAccount(first1, last1, phone1, mapOf("promo" to "JV13")))
        val res = client.mms.send(accounts, "MMS data promo \${promo}", "Java MMS 13", fk)
        checkNotNull(res) { "Null response" }
    }

    runTest("14 MMS sendSingle with customData") {
        val fk = fileKey ?: error("Dependency test 07 failed — skipping")
        val res = client.mms.sendSingle(
            first1, last1, phone1,
            "MMS custom data test", "Java MMS 14", fk,
            """{"source":"java-integration"}"""
        )
        checkNotNull(res) { "Null response" }
    }

    runTest("15 MMS checkFileUploaded") {
        val fk = fileKey ?: error("Dependency test 07 failed — skipping")
        val res = client.mms.checkFileUploaded(fk)
        checkNotNull(res) { "Null response" }
    }

    runTest("16 MMS sendWithImage (fresh upload)") {
        val accounts = listOf(MmsAccount(first1, last1, phone1))
        val res = client.mms.sendWithImage(accounts, "MMS sendWithImage test", "Java MMS 16", imageFile)
        checkNotNull(res) { "Null response" }
    }

    runTest("17 MMS sendWithImage (cached, same file)") {
        val accounts = listOf(MmsAccount(first1, last1, phone1))
        val res = client.mms.sendWithImage(accounts, "MMS cached image test", "Java MMS 17", imageFile)
        checkNotNull(res) { "Null response" }
    }

    // -----------------------------------------------------------------------
    // Email Tests (18–22)
    // -----------------------------------------------------------------------
    println("\n--- Email ---")

    runTest("18 Email sendSingle") {
        val res = client.email.sendSingle(
            first1, last1, email1,
            "Java Integration Test 18",
            "<p>Hello \${firstName}!</p>"
        )
        check(res.id.isNotBlank()) { "Empty id in response" }
    }

    runTest("19 Email send (1 recipient)") {
        val accounts = listOf(EmailAccount(first1, last1, email1))
        val res = client.email.send(accounts, "Java Integration Test 19", "<p>Hello \${firstName}!</p>")
        check(res.id.isNotBlank()) { "Empty id in response" }
    }

    runTest("20 Email send (2 recipients)") {
        val accounts = listOf(
            EmailAccount(first1, last1, email1),
            EmailAccount(first2, last2, email2)
        )
        val res = client.email.send(accounts, "Java Integration Test 20", "<p>Hello \${firstName}!</p>")
        check(res.id.isNotBlank()) { "Empty id in response" }
    }

    runTest("21 Email send (3 recipients)") {
        val accounts = listOf(
            EmailAccount(first1, last1, email1),
            EmailAccount(first2, last2, email2),
            EmailAccount(first3, last3, email3)
        )
        val res = client.email.send(accounts, "Java Integration Test 21", "<p>Hello \${firstName}!</p>")
        check(res.id.isNotBlank()) { "Empty id in response" }
    }

    runTest("22 Email send (full campaign — 3 recipients with data)") {
        val accounts = listOf(
            EmailAccount(first1, last1, email1, customFields = mapOf("plan" to "premium")),
            EmailAccount(first2, last2, email2, customFields = mapOf("plan" to "standard")),
            EmailAccount(first3, last3, email3, customFields = mapOf("plan" to "basic"))
        )
        val res = client.email.send(
            accounts,
            "Java Integration Test 22",
            "<h1>Campaign Test</h1><p>Hello \${firstName}, your plan is \${plan}.</p>",
            senderEmail = "noreply@cloudcontactai.com",
            replyEmail  = "noreply@cloudcontactai.com",
            senderName  = "Java Integration"
        )
        check(res.id.isNotBlank()) { "Empty id in response" }
    }

    // -----------------------------------------------------------------------
    // Webhook Tests (23–29)
    // -----------------------------------------------------------------------
    println("\n--- Webhook ---")

    var webhookId: Long? = null

    runTest("23 Webhook create (register)") {
        val req = WebhookRequest(webhookUrl, "java-test-secret-key")
        val res = client.webhook.create(req)
        check(res.id > 0) { "Invalid webhook id: ${res.id}" }
        webhookId = res.id
    }

    runTest("24 Webhook getAll (list)") {
        val list = client.webhook.getAll()
        check(list.isNotEmpty() || list.isEmpty()) { "Expected a list" } // just verify it doesn't throw
    }

    runTest("25 Webhook update") {
        val id = webhookId ?: error("Dependency test 23 failed — skipping")
        val req = WebhookUpdateRequest(id, "$webhookUrl/updated")
        val res = client.webhook.update(req)
        check(res.id > 0) { "Invalid webhook id in update response" }
    }

    runTest("26 Webhook validateSignature (valid)") {
        val secret    = "java-test-secret-key"
        val eventHash = "abc123hash"
        val clientIdLong = clientId.toLong()
        val expected  = client.webhook.generateSignature(secret, clientIdLong, eventHash)
        val ok = client.webhook.validateSignature(expected, secret, clientIdLong, eventHash)
        check(ok) { "Valid signature validation returned false" }
    }

    runTest("27 Webhook validateSignature (invalid)") {
        val ok = client.webhook.validateSignature("invalidsig==", "wrong-secret", clientId.toLong(), "somehash")
        check(!ok) { "Invalid signature validation returned true" }
    }

    runTest("28 Webhook parseWebhookEvent") {
        val payload = """{"eventType":"SMS_SENT","data":{"phone":"+13055551234"},"eventHash":"abc123hash"}"""
        val event = client.webhook.parseWebhookEvent(payload)
        check(event.eventType.isNotBlank()) { "Missing eventType in parsed event" }
    }

    runTest("29 Webhook delete") {
        val id = webhookId ?: error("Dependency test 23 failed — skipping")
        val res = client.webhook.delete(id)
        check(res.id == id) { "Deleted webhook id mismatch" }
    }

    // -----------------------------------------------------------------------
    // Contact Tests (30–31)
    // -----------------------------------------------------------------------
    println("\n--- Contact ---")

    runTest("30 Contact setDoNotText (opt-out)") {
        val res = client.contact.setDoNotText(phone = phone1, doNotText = true)
        checkNotNull(res) { "Null response" }
    }

    runTest("31 Contact setDoNotText (opt-in)") {
        val res = client.contact.setDoNotText(phone = phone1, doNotText = false)
        checkNotNull(res) { "Null response" }
    }

    // -----------------------------------------------------------------------
    // Brand Tests (32–36)
    // -----------------------------------------------------------------------
    println("\n--- Brands ---")

    var brandId: Long? = null

    runTest("32 Brand create") {
        val req = BrandRequest(
            legalCompanyName  = "Test Company LLC",
            entityType        = "PRIVATE_PROFIT",
            taxId             = "123456789",
            taxIdCountry      = "US",
            country           = "US",
            verticalType      = "TECHNOLOGY",
            websiteUrl        = "https://example.com",
            street            = "123 Main St",
            city              = "Miami",
            state             = "FL",
            postalCode        = "33101",
            contactFirstName  = first1,
            contactLastName   = last1,
            contactEmail      = email1,
            contactPhone      = phone1
        )
        val res = client.brands.create(req)
        check(res.id > 0) { "Invalid brand id: ${res.id}" }
        brandId = res.id
    }

    runTest("33 Brand get") {
        val id = brandId ?: error("Dependency test 32 failed — skipping")
        val res = client.brands.get(id)
        check(res.id == id) { "Brand id mismatch" }
    }

    runTest("34 Brand list") {
        val list = client.brands.list()
        checkNotNull(list) { "Null response" }
    }

    runTest("35 Brand update") {
        val id = brandId ?: error("Dependency test 32 failed — skipping")
        val req = BrandRequest(city = "Orlando")
        val res = client.brands.update(id, req)
        check(res.id == id) { "Brand id mismatch after update" }
    }

    runTest("36 Brand delete") {
        val id = brandId ?: error("Dependency test 32 failed — skipping")
        client.brands.delete(id)
    }

    // -----------------------------------------------------------------------
    // Campaign Tests (37–42)
    // -----------------------------------------------------------------------
    println("\n--- Campaigns ---")

    var campaignBrandId: Long? = null
    var campaignId: Long? = null

    runTest("37 Campaign setup — create brand") {
        val req = BrandRequest(
            legalCompanyName  = "Campaign Test LLC",
            entityType        = "PRIVATE_PROFIT",
            taxId             = "987654321",
            taxIdCountry      = "US",
            country           = "US",
            verticalType      = "TECHNOLOGY",
            websiteUrl        = "https://example.com",
            street            = "456 Test Ave",
            city              = "Miami",
            state             = "FL",
            postalCode        = "33101",
            contactFirstName  = first1,
            contactLastName   = last1,
            contactEmail      = email1,
            contactPhone      = phone1
        )
        val res = client.brands.create(req)
        check(res.id > 0) { "Invalid brand id: ${res.id}" }
        campaignBrandId = res.id
    }

    runTest("38 Campaign create") {
        val bid = campaignBrandId ?: error("Dependency test 37 failed — skipping")
        val req = CampaignRequest(
            brandId           = bid,
            useCase           = "MARKETING",
            description       = "Integration test campaign for automated testing",
            messageFlow       = "Customers opt-in via website form at https://example.com/sms-signup",
            hasEmbeddedLinks  = false,
            hasEmbeddedPhone  = false,
            isAgeGated        = false,
            isDirectLending   = false,
            optInKeywords     = listOf("START", "YES"),
            optInMessage      = "You have opted in to receive messages. Reply STOP to unsubscribe.",
            optInProofUrl     = "https://example.com/opt-in-proof",
            helpKeywords      = listOf("HELP", "INFO"),
            helpMessage       = "For help reply HELP or call 1-800-555-0000.",
            optOutKeywords    = listOf("STOP", "END"),
            optOutMessage     = "You have been unsubscribed. Reply START to opt back in. STOP",
            sampleMessages    = listOf(
                "Hello \${firstName}, this is a test message. Reply STOP to unsubscribe.",
                "Reminder: your appointment is tomorrow. Reply HELP for assistance."
            )
        )
        val res = client.campaigns.create(req)
        check(res.id > 0) { "Invalid campaign id: ${res.id}" }
        campaignId = res.id
    }

    runTest("39 Campaign get") {
        val id = campaignId ?: error("Dependency test 38 failed — skipping")
        val res = client.campaigns.get(id)
        check(res.id == id) { "Campaign id mismatch" }
    }

    runTest("40 Campaign list") {
        val list = client.campaigns.list()
        checkNotNull(list) { "Null response" }
    }

    runTest("41 Campaign update") {
        val id = campaignId ?: error("Dependency test 38 failed — skipping")
        val req = CampaignRequest(description = "Updated integration test campaign description")
        val res = client.campaigns.update(id, req)
        check(res.id == id) { "Campaign id mismatch after update" }
    }

    runTest("42 Campaign delete") {
        val id = campaignId ?: error("Dependency test 38 failed — skipping")
        client.campaigns.delete(id)
        campaignBrandId?.let { client.brands.delete(it) }
    }

    // -----------------------------------------------------------------------
    // Summary
    // -----------------------------------------------------------------------
    val total = passed + failed
    println("\n=== Results: $passed/$total passed ===")
    if (failed > 0) exitProcess(1)
}
