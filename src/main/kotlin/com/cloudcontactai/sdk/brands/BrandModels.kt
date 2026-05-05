package com.cloudcontactai.sdk.brands

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

data class BrandRequest(
    val legalCompanyName: String? = null,
    val dba: String? = null,
    val entityType: String? = null,
    val taxId: String? = null,
    val taxIdCountry: String? = null,
    val country: String? = null,
    val verticalType: String? = null,
    val websiteUrl: String? = null,
    val stockSymbol: String? = null,
    val stockExchange: String? = null,
    val street: String? = null,
    val city: String? = null,
    val state: String? = null,
    val postalCode: String? = null,
    val contactFirstName: String? = null,
    val contactLastName: String? = null,
    val contactEmail: String? = null,
    val contactPhone: String? = null,
    val websiteMatch: Boolean = false
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class BrandResponse(
    val id: Long = 0,
    val accountId: Long = 0,
    val legalCompanyName: String = "",
    val dba: String? = null,
    val entityType: String = "",
    val taxId: String = "",
    val taxIdCountry: String = "",
    val country: String = "",
    val verticalType: String = "",
    val websiteUrl: String = "",
    val stockSymbol: String? = null,
    val stockExchange: String? = null,
    val street: String = "",
    val city: String = "",
    val state: String = "",
    val postalCode: String = "",
    val contactFirstName: String = "",
    val contactLastName: String = "",
    val contactEmail: String = "",
    val contactPhone: String = "",
    val websiteMatchScore: Int? = null,
    val createdAt: String = "",
    val updatedAt: String = ""
)
