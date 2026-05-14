package com.cloudcontactai.sdk.contactvalidator

import com.cloudcontactai.sdk.common.ApiClient
import com.cloudcontactai.sdk.common.CCAIConfig

class ContactValidatorService(private val config: CCAIConfig, private val apiClient: ApiClient) {

    fun validateEmail(email: String): EmailValidationResult =
        apiClient.request(
            method = "POST",
            endpoint = "/v1/contact-validator/email",
            data = mapOf("email" to email),
            responseClass = EmailValidationResult::class.java
        )

    fun validateEmails(emails: List<String>): BulkEmailValidationResult =
        apiClient.request(
            method = "POST",
            endpoint = "/v1/contact-validator/emails",
            data = mapOf("emails" to emails),
            responseClass = BulkEmailValidationResult::class.java
        )

    fun validatePhone(phone: String, countryCode: String? = null): PhoneValidationResult =
        apiClient.request(
            method = "POST",
            endpoint = "/v1/contact-validator/phone",
            data = mapOf("phone" to phone, "countryCode" to countryCode),
            responseClass = PhoneValidationResult::class.java
        )

    fun validatePhones(phones: List<PhoneInput>): BulkPhoneValidationResult =
        apiClient.request(
            method = "POST",
            endpoint = "/v1/contact-validator/phones",
            data = mapOf("phones" to phones),
            responseClass = BulkPhoneValidationResult::class.java
        )
}
