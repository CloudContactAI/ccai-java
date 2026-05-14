package com.cloudcontactai.sdk.contactvalidator

data class EmailValidationResult(
    val contactField: String,
    val type: String,
    val status: String,
    val metadata: Map<String, Any?> = emptyMap()
)

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
