package com.cloudcontactai.sdk.contact

data class ContactDoNotTextRequest(
    val clientId: String,
    var contactId: String? = null,
    var phone: String? = null,
    var doNotText: Boolean? = null
)

data class ContactDoNotTextResponse(
    val contactId: String,
    val phone: String,
    val doNotText: Boolean
)