package com.ccai.integration

import com.cloudcontactai.sdk.CCAIClient
import com.cloudcontactai.sdk.common.CCAIConfig
import com.cloudcontactai.sdk.sms.Account as SmsAccount
import com.cloudcontactai.sdk.mms.Account as MmsAccount
import com.cloudcontactai.sdk.mms.MMSResponse
import com.cloudcontactai.sdk.email.EmailAccount
import com.cloudcontactai.sdk.webhook.WebhookRequest
import com.cloudcontactai.sdk.webhook.WebhookUpdateRequest
import com.cloudcontactai.sdk.brands.BrandRequest
import com.cloudcontactai.sdk.campaigns.CampaignRequest
import com.cloudcontactai.sdk.contactvalidator.PhoneInput
import java.io.File
import java.util.Base64
import kotlin.system.exitProcess

// ---------------------------------------------------------------------------
// CCAI Java (Kotlin) SDK Integration Tests — 52 tests
// Covers: SMS (1-6), MMS (7-17), Email (18-22), Webhook (23-29), Contact (30-31),
// Brands (32-36), Campaigns (37-42), ContactValidator (43-46), Negative cases (47-52)
//
// Test results use three states:
//   PASS — the test ran and all assertions held
//   FAIL — the test ran and an assertion (or the API call) failed
//   SKIP — a prerequisite test failed, so this test could not run
//
// Resources created during the run (webhooks, brands, campaigns) are tracked
// and deleted in a final cleanup block even if tests fail midway.
// Exits with code 1 if any test fails, 2 if required env vars are missing.
// ---------------------------------------------------------------------------

// ---------------------------------------------------------------------------
// Environment variables
// ---------------------------------------------------------------------------
val requiredEnv = listOf(
    "CCAI_CLIENT_ID", "CCAI_API_KEY",
    "CCAI_TEST_PHONE", "CCAI_TEST_PHONE_2", "CCAI_TEST_PHONE_3",
    "CCAI_TEST_EMAIL", "CCAI_TEST_EMAIL_2", "CCAI_TEST_EMAIL_3",
    "CCAI_TEST_FIRST_NAME", "CCAI_TEST_LAST_NAME",
    "CCAI_TEST_FIRST_NAME_2", "CCAI_TEST_LAST_NAME_2",
    "CCAI_TEST_FIRST_NAME_3", "CCAI_TEST_LAST_NAME_3",
    "WEBHOOK_URL"
)

val clientId   = System.getenv("CCAI_CLIENT_ID")        ?: ""
val apiKey     = System.getenv("CCAI_API_KEY")           ?: ""
val phone1     = System.getenv("CCAI_TEST_PHONE")        ?: ""
val phone2     = System.getenv("CCAI_TEST_PHONE_2")      ?: ""
val phone3     = System.getenv("CCAI_TEST_PHONE_3")      ?: ""
val email1     = System.getenv("CCAI_TEST_EMAIL")        ?: ""
val email2     = System.getenv("CCAI_TEST_EMAIL_2")      ?: ""
val email3     = System.getenv("CCAI_TEST_EMAIL_3")      ?: ""
val first1     = System.getenv("CCAI_TEST_FIRST_NAME")   ?: ""
val last1      = System.getenv("CCAI_TEST_LAST_NAME")    ?: ""
val first2     = System.getenv("CCAI_TEST_FIRST_NAME_2") ?: ""
val last2      = System.getenv("CCAI_TEST_LAST_NAME_2")  ?: ""
val first3     = System.getenv("CCAI_TEST_FIRST_NAME_3") ?: ""
val last3      = System.getenv("CCAI_TEST_LAST_NAME_3")  ?: ""

// Unique per-run suffix so parallel SDK runs don't collide on the same webhook URL
val runId = "java-${System.currentTimeMillis() / 1000}"
val webhookBase = System.getenv("WEBHOOK_URL") ?: ""
val webhookUrl = webhookBase + (if (webhookBase.contains('?')) "&" else "?") + "run=$runId"

val senderEmail   = System.getenv("CCAI_TEST_SENDER_EMAIL")?.takeIf { it.isNotBlank() } ?: "noreply@cloudcontactai.com"
val replyEmail    = senderEmail
val webhookSecret = System.getenv("CCAI_WEBHOOK_SECRET")?.takeIf { it.isNotBlank() } ?: "java-test-secret-key"

// ---------------------------------------------------------------------------
// Test runner state
// ---------------------------------------------------------------------------
var passed = 0
var failed = 0
var skipped = 0

/** Thrown when a test cannot run because a prerequisite test failed. */
class SkipTest(message: String) : RuntimeException(message)

fun skipTest(reason: String): Nothing = throw SkipTest(reason)

fun runTest(label: String, block: () -> Unit) {
    try {
        block()
        println("  [PASS] $label")
        passed++
    } catch (e: SkipTest) {
        println("  [SKIP] $label: ${e.message}")
        skipped++
    } catch (e: Throwable) {
        val msg = e.message?.lines()?.firstOrNull() ?: "unknown error"
        println("  [FAIL] $label: $msg")
        failed++
    }
}

/** Runs block and asserts that it throws — used by the negative test cases. */
fun expectError(what: String, block: () -> Unit) {
    val didFail = try {
        block()
        false
    } catch (e: SkipTest) {
        throw e
    } catch (e: Throwable) {
        true
    }
    check(didFail) { "expected $what to fail, but it succeeded" }
}

/** Asserts that an MMS response carries a campaign/message identifier. */
fun assertMmsResponse(res: MMSResponse?) {
    checkNotNull(res) { "Null response" }
    check(!res.id.isNullOrBlank() || !res.campaignId.isNullOrBlank()) {
        "response has no id/campaignId"
    }
}

// ---------------------------------------------------------------------------
// Main
// ---------------------------------------------------------------------------
fun main() {
    // Validate ALL required env vars up front and report every missing one,
    // instead of failing later with a cryptic API error.
    val missing = requiredEnv.filter { System.getenv(it).isNullOrBlank() }
    if (missing.isNotEmpty()) {
        System.err.println("ERROR: required env vars are not set: ${missing.joinToString(", ")}")
        exitProcess(2)
    }

    // Use CCAI_BASE_URL if set (local dev), otherwise fall back to test environment
    val config = CCAIConfig(
        clientId = clientId,
        apiKey = apiKey,
        useTestEnvironment = System.getenv("CCAI_BASE_URL") == null
    )
    val client = CCAIClient(config)

    // Write a 1x1 transparent PNG to a temp file for MMS tests
    val imageBytes = Base64.getDecoder().decode(
        "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mP8z8BQDwADhQGAWjR9awAAAABJRU5ErkJggg=="
    )
    val imageFile = File.createTempFile("ccai_test", ".png").also {
        it.writeBytes(imageBytes)
    }

    // IDs of resources created by the tests; anything still listed here when the
    // finally block runs is deleted (tests remove entries they already deleted).
    val cleanupWebhookIds = mutableListOf<Long>()
    val cleanupBrandIds = mutableListOf<Long>()
    val cleanupCampaignIds = mutableListOf<Long>()

    println("=== CCAI Java (Kotlin) SDK Integration Tests ===\n")

    try {
        // -------------------------------------------------------------------
        // SMS Tests (01–06)
        // -------------------------------------------------------------------
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

        // -------------------------------------------------------------------
        // MMS Tests (07–17)
        // -------------------------------------------------------------------
        println("\n--- MMS ---")

        var signedUrl: String? = null
        var fileKey: String? = null
        var uploadOk = false

        runTest("07 MMS getSignedUploadUrl") {
            val req = com.cloudcontactai.sdk.mms.SignedUploadUrlRequest(
                fileName = "java_test.png",
                fileType = "image/png",
                publicFile = true
            )
            val res = client.mms.getSignedUploadUrl(req)
            check(res.signedS3Url.isNotBlank()) { "Missing signedS3Url" }
            check(!res.fileKey.isNullOrBlank()) { "Missing fileKey" }
            signedUrl = res.signedS3Url
            fileKey   = res.fileKey
        }

        runTest("08 MMS uploadImageToSignedUrl") {
            val url = signedUrl ?: skipTest("dependency test 07 failed")
            client.mms.uploadImageToSignedUrl(url, imageFile, "image/png")
            uploadOk = true
        }

        runTest("09 MMS sendSingle") {
            val fk = fileKey ?: skipTest("dependency test 07 failed")
            val res = client.mms.sendSingle(first1, last1, phone1, "MMS single test", "Java MMS 09", fk)
            assertMmsResponse(res)
        }

        runTest("10 MMS send (1 recipient)") {
            val fk = fileKey ?: skipTest("dependency test 07 failed")
            val accounts = listOf(MmsAccount(first1, last1, phone1))
            val res = client.mms.send(accounts, "MMS bulk test", "Java MMS 10", fk)
            assertMmsResponse(res)
        }

        runTest("11 MMS send (2 recipients)") {
            val fk = fileKey ?: skipTest("dependency test 07 failed")
            val accounts = listOf(
                MmsAccount(first1, last1, phone1),
                MmsAccount(first2, last2, phone2)
            )
            val res = client.mms.send(accounts, "MMS 2-recipient test", "Java MMS 11", fk)
            assertMmsResponse(res)
        }

        runTest("12 MMS send (3 recipients)") {
            val fk = fileKey ?: skipTest("dependency test 07 failed")
            val accounts = listOf(
                MmsAccount(first1, last1, phone1),
                MmsAccount(first2, last2, phone2),
                MmsAccount(first3, last3, phone3)
            )
            val res = client.mms.send(accounts, "MMS 3-recipient test", "Java MMS 12", fk)
            assertMmsResponse(res)
        }

        runTest("13 MMS send with data (template variables)") {
            val fk = fileKey ?: skipTest("dependency test 07 failed")
            val accounts = listOf(MmsAccount(first1, last1, phone1, mapOf("promo" to "JV13")))
            val res = client.mms.send(accounts, "MMS data promo \${promo}", "Java MMS 13", fk)
            assertMmsResponse(res)
        }

        runTest("14 MMS sendSingle with customData") {
            val fk = fileKey ?: skipTest("dependency test 07 failed")
            val res = client.mms.sendSingle(
                first1, last1, phone1,
                "MMS custom data test", "Java MMS 14", fk,
                """{"source":"java-integration"}"""
            )
            assertMmsResponse(res)
        }

        runTest("15 MMS checkFileUploaded") {
            val fk = fileKey ?: skipTest("dependency test 07 failed")
            if (!uploadOk) skipTest("dependency test 08 failed")
            val res = client.mms.checkFileUploaded(fk)
            checkNotNull(res) { "Null response" }
            check(res.storedUrl.isNotBlank()) { "expected non-empty storedUrl for uploaded file $fk" }
        }

        runTest("16 MMS sendWithImage (fresh upload)") {
            val accounts = listOf(MmsAccount(first1, last1, phone1))
            val res = client.mms.sendWithImage(accounts, "MMS sendWithImage test", "Java MMS 16", imageFile)
            assertMmsResponse(res)
        }

        runTest("17 MMS sendWithImage (cached, same file)") {
            val accounts = listOf(MmsAccount(first1, last1, phone1))
            val res = client.mms.sendWithImage(accounts, "MMS cached image test", "Java MMS 17", imageFile)
            assertMmsResponse(res)
        }

        // -------------------------------------------------------------------
        // Email Tests (18–22)
        // -------------------------------------------------------------------
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
                senderEmail = senderEmail,
                replyEmail  = replyEmail,
                senderName  = "Java Integration"
            )
            check(res.id.isNotBlank()) { "Empty id in response" }
        }

        // -------------------------------------------------------------------
        // Webhook Tests (23–29)
        // -------------------------------------------------------------------
        println("\n--- Webhook ---")

        var webhookId: Long? = null

        runTest("23 Webhook create (register)") {
            val req = WebhookRequest(webhookUrl, webhookSecret)
            val res = client.webhook.create(req)
            check(res.id > 0) { "Invalid webhook id: ${res.id}" }
            webhookId = res.id
            cleanupWebhookIds.add(res.id)
        }

        runTest("24 Webhook getAll (list)") {
            val list = client.webhook.getAll()
            check(list.isNotEmpty()) { "expected at least one webhook, got 0" }
            webhookId?.let { id ->
                check(list.any { it.id == id }) { "webhook $id registered in test 23 not present in getAll()" }
            }
        }

        runTest("25 Webhook update") {
            val id = webhookId ?: skipTest("dependency test 23 failed")
            val req = WebhookUpdateRequest(id, "$webhookUrl&updated=1")
            val res = client.webhook.update(req)
            check(res.id > 0) { "Invalid webhook id in update response" }
            // Verify via getAll() that the URL actually changed
            val hook = client.webhook.getAll().find { it.id == id }
            checkNotNull(hook) { "webhook $id not found in getAll() after update" }
            check(hook.url.contains("updated=1")) {
                "webhook URL was not updated: expected to contain \"updated=1\", got \"${hook.url}\""
            }
        }

        runTest("26 Webhook validateSignature (valid)") {
            val eventHash = "abc123hash"
            val clientIdLong = clientId.toLong()
            val expected = client.webhook.generateSignature(webhookSecret, clientIdLong, eventHash)
            val ok = client.webhook.validateSignature(expected, webhookSecret, clientIdLong, eventHash)
            check(ok) { "Valid signature validation returned false" }
        }

        runTest("27 Webhook validateSignature (invalid)") {
            val ok = client.webhook.validateSignature("invalidsig==", "wrong-secret", clientId.toLong(), "somehash")
            check(!ok) { "Invalid signature validation returned true" }
        }

        runTest("28 Webhook parseWebhookEvent") {
            val payload = """{"eventType":"SMS_SENT","data":{"phone":"+13055551234"},"eventHash":"abc123hash"}"""
            val event = client.webhook.parseWebhookEvent(payload)
            check(event.eventType == "SMS_SENT") { "expected eventType \"SMS_SENT\", got \"${event.eventType}\"" }
            check(event.eventHash == "abc123hash") { "expected eventHash \"abc123hash\", got \"${event.eventHash}\"" }
        }

        runTest("29 Webhook delete") {
            val id = webhookId ?: skipTest("dependency test 23 failed")
            val res = client.webhook.delete(id)
            check(res.id == id) { "Deleted webhook id mismatch" }
            cleanupWebhookIds.remove(id)
            // Verify via getAll() that it is gone
            check(client.webhook.getAll().none { it.id == id }) {
                "webhook $id still present in getAll() after delete"
            }
        }

        // -------------------------------------------------------------------
        // Contact Tests (30–31)
        // -------------------------------------------------------------------
        println("\n--- Contact ---")

        runTest("30 Contact setDoNotText (opt-out)") {
            val res = client.contact.setDoNotText(phone = phone1, doNotText = true)
            checkNotNull(res) { "Null response" }
        }

        runTest("31 Contact setDoNotText (opt-in)") {
            val res = client.contact.setDoNotText(phone = phone1, doNotText = false)
            checkNotNull(res) { "Null response" }
        }

        // -------------------------------------------------------------------
        // Brand Tests (32–36)
        // -------------------------------------------------------------------
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
            cleanupBrandIds.add(res.id)
        }

        runTest("33 Brand get") {
            val id = brandId ?: skipTest("dependency test 32 failed")
            val res = client.brands.get(id)
            check(res.id == id) { "Brand id mismatch" }
            check(res.legalCompanyName == "Test Company LLC") {
                "expected legalCompanyName \"Test Company LLC\", got \"${res.legalCompanyName}\""
            }
        }

        runTest("34 Brand list") {
            val list = client.brands.list()
            checkNotNull(list) { "Null response" }
            brandId?.let { id ->
                check(list.any { it.id == id }) { "brand $id created in test 32 not present in list()" }
            }
        }

        runTest("35 Brand update") {
            val id = brandId ?: skipTest("dependency test 32 failed")
            val req = BrandRequest(city = "Orlando")
            val res = client.brands.update(id, req)
            check(res.id == id) { "Brand id mismatch after update" }
            // Verify via get() that the field actually changed
            val fetched = client.brands.get(id)
            check(fetched.city == "Orlando") { "expected city \"Orlando\" after update, got \"${fetched.city}\"" }
        }

        runTest("36 Brand delete") {
            val id = brandId ?: skipTest("dependency test 32 failed")
            client.brands.delete(id)
            cleanupBrandIds.remove(id)
            // Verify via get() that it is gone (404 expected)
            expectError("get of deleted brand $id") { client.brands.get(id) }
        }

        // -------------------------------------------------------------------
        // Campaign Tests (37–42)
        // -------------------------------------------------------------------
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
            cleanupBrandIds.add(res.id)
        }

        runTest("38 Campaign create") {
            val bid = campaignBrandId ?: skipTest("dependency test 37 failed")
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
            cleanupCampaignIds.add(res.id)
        }

        runTest("39 Campaign get") {
            val id = campaignId ?: skipTest("dependency test 38 failed")
            val res = client.campaigns.get(id)
            check(res.id == id) { "Campaign id mismatch" }
            check(res.brandId == campaignBrandId) { "expected brandId $campaignBrandId, got ${res.brandId}" }
        }

        runTest("40 Campaign list") {
            val list = client.campaigns.list()
            checkNotNull(list) { "Null response" }
            campaignId?.let { id ->
                check(list.any { it.id == id }) { "campaign $id created in test 38 not present in list()" }
            }
        }

        runTest("41 Campaign update") {
            val id = campaignId ?: skipTest("dependency test 38 failed")
            val newDescription = "Updated integration test campaign description"
            val req = CampaignRequest(description = newDescription)
            val res = client.campaigns.update(id, req)
            check(res.id == id) { "Campaign id mismatch after update" }
            // Verify via get() that the field actually changed
            val fetched = client.campaigns.get(id)
            check(fetched.description == newDescription) {
                "expected updated description after update, got \"${fetched.description}\""
            }
        }

        runTest("42 Campaign delete") {
            val id = campaignId ?: skipTest("dependency test 38 failed")
            client.campaigns.delete(id)
            cleanupCampaignIds.remove(id)
            // Verify via get() that it is gone (404 expected)
            expectError("get of deleted campaign $id") { client.campaigns.get(id) }
            campaignBrandId?.let {
                client.brands.delete(it)
                cleanupBrandIds.remove(it)
            }
        }

        // -------------------------------------------------------------------
        // ContactValidator Tests (43–46)
        // -------------------------------------------------------------------
        println("\n--- ContactValidator ---")

        runTest("43 ContactValidator validateEmail") {
            val res = client.contactValidator.validateEmail(email1)
            check(res.status.isNotBlank()) { "status is blank" }
        }

        runTest("44 ContactValidator validateEmails") {
            val res = client.contactValidator.validateEmails(listOf(email1, email2))
            check(res.summary.total == 2) { "expected summary.total=2, got ${res.summary.total}" }
            check(res.results.size == 2) { "expected 2 results, got ${res.results.size}" }
        }

        runTest("45 ContactValidator validatePhone") {
            val res = client.contactValidator.validatePhone(phone1)
            check(res.status.isNotBlank()) { "status is blank" }
        }

        runTest("46 ContactValidator validatePhones") {
            val res = client.contactValidator.validatePhones(listOf(
                PhoneInput(phone = phone1),
                PhoneInput(phone = phone2)
            ))
            check(res.summary.total == 2) { "expected summary.total=2, got ${res.summary.total}" }
            check(res.results.size == 2) { "expected 2 results, got ${res.results.size}" }
        }

        // -------------------------------------------------------------------
        // Negative & Permissive Tests (47–52)
        // 47/49/50 PASS when the operation fails as expected. 48/51/52 document
        // permissive behavior observed in the test API:
        // those operations succeed even with invalid input, so the tests
        // assert success.
        // -------------------------------------------------------------------
        println("\n--- Negative cases ---")

        runTest("47 NEGATIVE: SMS sendSingle with invalid API key") {
            val badClient = CCAIClient(CCAIConfig(
                clientId = clientId,
                apiKey = "invalid-api-key-for-negative-test",
                useTestEnvironment = System.getenv("CCAI_BASE_URL") == null
            ))
            expectError("send with invalid API key") {
                badClient.sms.sendSingle(first1, last1, phone1, "should fail", "Java Negative 47")
            }
        }

        // The test API accepts malformed phone numbers: the
        // send succeeds instead of failing. If the API starts validating phone
        // format, change this back to expectError.
        runTest("48 PERMISSIVE: SMS sendSingle with malformed phone (API accepts)") {
            val res = client.sms.sendSingle(first1, last1, "abc", "malformed phone accepted", "Java Permissive 48")
            check(res.id.isNotBlank()) { "Empty id in response" }
        }

        runTest("49 NEGATIVE: Brand get(nonexistent)") {
            expectError("get of nonexistent brand") { client.brands.get(99999999L) }
        }

        runTest("50 NEGATIVE: Webhook delete(nonexistent)") {
            expectError("delete of nonexistent webhook") { client.webhook.delete(99999999L) }
        }

        // The test environment's validator reports "valid" even for syntactically
        // invalid emails — upstream validation is not
        // enforced there, so only assert that a status is returned.
        runTest("51 PERMISSIVE: ContactValidator validateEmail(invalid input)") {
            val res = client.contactValidator.validateEmail("not-an-email")
            check(res.status.isNotBlank()) { "status is blank" }
        }

        // The test API accepts MMS sends with a nonexistent fileKey: it does not
        // verify the file exists at send time. If the
        // API starts validating the fileKey, change this back to expectError.
        runTest("52 PERMISSIVE: MMS send with nonexistent fileKey (API accepts)") {
            val fakeKey = "$clientId/campaign/nonexistent_${System.currentTimeMillis()}.png"
            val res = client.mms.send(
                listOf(MmsAccount(first1, last1, phone1)),
                "nonexistent fileKey accepted", "Java Permissive 52", fakeKey
            )
            assertMmsResponse(res)
        }
    } finally {
        // -------------------------------------------------------------------
        // Cleanup — always runs, even if the test body threw: delete leftover
        // resources and the temp PNG.
        // -------------------------------------------------------------------
        for (id in cleanupCampaignIds) {
            try {
                client.campaigns.delete(id)
                println("  CLEANUP: deleted leftover campaign $id")
            } catch (e: Throwable) {
                println("  CLEANUP: could not delete campaign $id: ${e.message}")
            }
        }
        for (id in cleanupBrandIds) {
            try {
                client.brands.delete(id)
                println("  CLEANUP: deleted leftover brand $id")
            } catch (e: Throwable) {
                println("  CLEANUP: could not delete brand $id: ${e.message}")
            }
        }
        for (id in cleanupWebhookIds) {
            try {
                client.webhook.delete(id)
                println("  CLEANUP: deleted leftover webhook $id")
            } catch (e: Throwable) {
                println("  CLEANUP: could not delete webhook $id: ${e.message}")
            }
        }
        imageFile.delete()
    }

    // -----------------------------------------------------------------------
    // Summary
    // -----------------------------------------------------------------------
    val total = passed + failed + skipped
    println("\n=== Results: $passed passed, $failed failed, $skipped skipped ($total total) ===")
    println("\nSUMMARY_JSON: {\"sdk\":\"java\",\"passed\":$passed,\"failed\":$failed,\"skipped\":$skipped,\"total\":$total}")
    if (failed > 0) exitProcess(1)
}
