package com.cloudcontactai.sdk.contactvalidator

import com.cloudcontactai.sdk.CCAIClient
import com.cloudcontactai.sdk.common.CCAIConfig
import com.cloudcontactai.sdk.common.CCAIException
import com.fasterxml.jackson.databind.ObjectMapper
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

/**
 * Email validation with Amazon SES. The responses in src/test/resources/contact-validator were recorded from the
 * contact-validator API.
 */
class EmailValidationTest {

    private lateinit var mockServer: MockWebServer
    private lateinit var client: CCAIClient

    @BeforeEach
    fun setup() {
        mockServer = MockWebServer()
        mockServer.start()

        val config = CCAIConfig(clientId = "test-client", apiKey = "test-key")
        val baseUrlField = CCAIConfig::class.java.getDeclaredField("baseUrl")
        baseUrlField.isAccessible = true
        baseUrlField.set(config, mockServer.url("/").toString().trimEnd('/'))

        client = CCAIClient(config)
    }

    @AfterEach
    fun tearDown() {
        mockServer.shutdown()
        client.close()
    }

    private fun recorded(name: String): String =
        javaClass.getResource("/contact-validator/$name.json")!!.readText()

    private fun respond(code: Int, body: String) {
        mockServer.enqueue(
            MockResponse().setResponseCode(code).setBody(body).addHeader("Content-Type", "application/json")
        )
    }

    @Test
    fun `posts the address to the email endpoint`() {
        respond(200, recorded("email-valid"))

        client.contactValidator.validateEmail("jane@example.com")

        val request = mockServer.takeRequest()
        assertEquals("POST", request.method)
        assertEquals("/v1/contact-validator/email", request.path)
        assertEquals(mapOf("email" to "jane@example.com"), ObjectMapper().readValue(request.body.readUtf8(), Map::class.java))
    }

    @Test
    fun `returns the verdict, the six checks, the date and whether the result was reused`() {
        respond(200, recorded("email-valid"))

        val result = client.contactValidator.validateEmail("jane@example.com")

        assertEquals("valid", result.status)
        assertEquals(true, result.metadata["safe_to_send"])
        assertEquals("valid", result.metadata["ai_verdict"])
        assertEquals("HIGH", result.verdict)
        assertEquals(
            EmailValidationChecks(
                hasValidSyntax = "HIGH", hasValidDnsRecords = "HIGH", mailboxExists = "HIGH",
                isRoleAddress = "LOW", isDisposable = "LOW", isRandomInput = "LOW"
            ),
            result.checks
        )
        assertEquals("2026-11-13T15:02:11Z", result.validatedAt)
        assertEquals(false, result.reused)
    }

    @Test
    fun `the fields are also in the metadata`() {
        respond(200, recorded("email-valid"))

        val metadata = client.contactValidator.validateEmail("jane@example.com").metadata

        assertEquals("HIGH", metadata["verdict"])
        assertEquals("2026-11-13T15:02:11Z", metadata["validatedAt"])
        assertEquals(false, metadata["reused"])
        assertEquals("HIGH", (metadata["checks"] as Map<*, *>)["mailboxExists"])
    }

    @Test
    fun `says when the result is a saved check`() {
        respond(200, recorded("email-reused"))
        assertEquals(true, client.contactValidator.validateEmail("jane@example.com").reused)
    }

    @Test
    fun `a disposable address is risky`() {
        respond(200, recorded("email-risky-disposable"))

        val result = client.contactValidator.validateEmail("temp@mailinator.com")

        assertEquals("risky", result.status)
        assertEquals(false, result.metadata["safe_to_send"])
        assertEquals("HIGH", result.checks?.isDisposable)
    }

    @Test
    fun `a LOW address is invalid`() {
        respond(200, recorded("email-invalid-low"))

        val result = client.contactValidator.validateEmail("nobody@no-such-domain-example.xyz")

        assertEquals("invalid", result.status)
        assertEquals("LOW", result.verdict)
        assertEquals("LOW", result.checks?.hasValidDnsRecords)
    }

    @Test
    fun `an address rejected before asking SES has UNKNOWN checks, no date and the reason`() {
        respond(200, recorded("email-rejected-format"))

        val result = client.contactValidator.validateEmail("not-an-email")

        assertEquals("invalid", result.status)
        assertEquals("UNKNOWN", result.verdict)
        assertEquals(EmailValidationChecks("UNKNOWN", "UNKNOWN", "UNKNOWN", "UNKNOWN", "UNKNOWN", "UNKNOWN"), result.checks)
        assertNull(result.validatedAt)
        assertEquals("Invalid email format", result.metadata["error"])
    }

    @Test
    fun `a response without the new fields is read as before`() {
        respond(200, recorded("email-legacy"))

        val result = client.contactValidator.validateEmail("jane@example.com")

        assertEquals("valid", result.status)
        assertEquals(true, result.metadata["safe_to_send"])
        assertEquals("Mode: Sandbox", result.metadata["ai_verdict"])
        assertNull(result.verdict)
        assertNull(result.checks)
        assertNull(result.validatedAt)
        assertNull(result.reused)
    }

    @Test
    fun `the result is still built and compared by its four constructor fields`() {
        val a = EmailValidationResult("jane@example.com", "email", "valid", mapOf("verdict" to "HIGH"))
        val b = EmailValidationResult("jane@example.com", "email", "valid", mapOf("verdict" to "HIGH"))
        assertEquals(a, b)
        assertEquals("HIGH", a.verdict)
        assertEquals(EmailValidationResult("x", "email", "valid"), EmailValidationResult("x", "email", "valid", emptyMap()))
    }

    @Test
    fun `fails with the 403 of an account that does not have Email Address Validation`() {
        respond(403, recorded("error-403"))

        val error = assertThrows(CCAIException::class.java) { client.contactValidator.validateEmail("jane@example.com") }

        assertTrue(error.message!!.startsWith("HTTP 403"))
        assertTrue(error.message!!.contains("Email validation is not enabled for this account"))
    }

    @Test
    fun `fails with the 502 of an SES refusal, with the message from AWS`() {
        respond(502, recorded("error-502"))

        val error = assertThrows(CCAIException::class.java) { client.contactValidator.validateEmail("jane@example.com") }

        assertTrue(error.message!!.startsWith("HTTP 502"))
        assertTrue(error.message!!.contains("ses:GetEmailAddressInsights"))
    }

    @Test
    fun `posts the addresses to the bulk endpoint and returns the new fields on every result`() {
        respond(200, recorded("emails-bulk"))
        val emails = listOf("jane@example.com", "temp@mailinator.com", "bad@example.com")

        val bulk = client.contactValidator.validateEmails(emails)

        val request = mockServer.takeRequest()
        assertEquals("/v1/contact-validator/emails", request.path)
        assertEquals(mapOf("emails" to emails), ObjectMapper().readValue(request.body.readUtf8(), Map::class.java))
        assertEquals(ValidationSummary(total = 3, valid = 1, invalid = 1, risky = 1), bulk.summary)
        val (reused, risky, failed) = bulk.results
        assertEquals(true, reused.reused)
        assertEquals("HIGH", reused.verdict)
        assertEquals("MEDIUM", risky.verdict)
        assertEquals("HIGH", risky.checks?.isDisposable)
        assertEquals("2026-11-13T15:05:40Z", risky.validatedAt)
        // An address SES failed to check comes back invalid with the reason, and none of the new fields
        assertEquals("invalid", failed.status)
        assertEquals("The domain is not allowed", failed.metadata["error"])
        assertNull(failed.verdict)
        assertNull(failed.checks)
    }

    @Test
    fun `the bulk endpoint fails with the 403 too`() {
        respond(403, recorded("error-403"))
        val error = assertThrows(CCAIException::class.java) { client.contactValidator.validateEmails(listOf("jane@example.com")) }
        assertTrue(error.message!!.startsWith("HTTP 403"))
    }
}
