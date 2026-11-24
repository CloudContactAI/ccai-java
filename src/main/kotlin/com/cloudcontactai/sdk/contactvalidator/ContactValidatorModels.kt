package com.cloudcontactai.sdk.contactvalidator

import com.fasterxml.jackson.annotation.JsonIgnore

/**
 * The six checks of an email validation, each with its own Amazon SES verdict:
 * "HIGH", "MEDIUM", "LOW" or "UNKNOWN" (a verdict SES did not return).
 */
data class EmailValidationChecks(
    /** The address follows RFC format rules (good value: HIGH) */
    val hasValidSyntax: String? = null,
    /** The domain exists and has DNS/MX records to receive mail (good value: HIGH) */
    val hasValidDnsRecords: String? = null,
    /** The mailbox exists and can receive messages (good value: HIGH) */
    val mailboxExists: String? = null,
    /** A role address such as admin@, support@ or info@ (good value: LOW) */
    val isRoleAddress: String? = null,
    /** A disposable or temporary email provider (good value: LOW) */
    val isDisposable: String? = null,
    /** The address looks randomly generated (good value: LOW) */
    val isRandomInput: String? = null
)

data class EmailValidationResult(
    val contactField: String,
    val type: String,
    val status: String,
    val metadata: Map<String, Any?> = emptyMap()
) {
    // The properties below are read from [metadata]. They are null when the response does not have them: the account
    // does not have Email Address Validation, or the address was not checked (see metadata["error"]).

    /** The overall verdict: "HIGH", "MEDIUM", "LOW" or "UNKNOWN" */
    val verdict: String?
        @JsonIgnore get() = metadata["verdict"] as? String

    /** The six checks, each with its own verdict */
    val checks: EmailValidationChecks?
        @JsonIgnore get() = (metadata["checks"] as? Map<*, *>)?.let { checks ->
            fun verdict(name: String) = checks[name] as? String
            EmailValidationChecks(
                hasValidSyntax = verdict("hasValidSyntax"),
                hasValidDnsRecords = verdict("hasValidDnsRecords"),
                mailboxExists = verdict("mailboxExists"),
                isRoleAddress = verdict("isRoleAddress"),
                isDisposable = verdict("isDisposable"),
                isRandomInput = verdict("isRandomInput")
            )
        }

    /** When Amazon SES checked the address (ISO 8601). Null when the address was rejected without a check. */
    val validatedAt: String?
        @JsonIgnore get() = metadata["validatedAt"] as? String

    /** True when the result is a saved check from the last 30 days, instead of a new one */
    val reused: Boolean?
        @JsonIgnore get() = metadata["reused"] as? Boolean
}

data class PhoneValidationResult(
    val contactField: String,
    val type: String,
    val status: String,
    val metadata: Map<String, Any?> = emptyMap()
)

data class ValidationSummary(
    val total: Int,
    val valid: Int,
    val invalid: Int,
    val risky: Int,
    val landline: Int = 0
)

data class BulkEmailValidationResult(
    val results: List<EmailValidationResult>,
    val summary: ValidationSummary
)

data class BulkPhoneValidationResult(
    val results: List<PhoneValidationResult>,
    val summary: ValidationSummary
)

data class PhoneInput(
    val phone: String,
    val countryCode: String? = null
)
