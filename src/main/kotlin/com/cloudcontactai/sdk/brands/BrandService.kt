package com.cloudcontactai.sdk.brands

import com.cloudcontactai.sdk.common.ApiClient
import com.cloudcontactai.sdk.common.CCAIConfig
import com.cloudcontactai.sdk.common.CCAIException

class BrandService(private val config: CCAIConfig, private val apiClient: ApiClient) {

    companion object {
        private val ENTITY_TYPES = setOf("PRIVATE_PROFIT", "PUBLIC_PROFIT", "NON_PROFIT", "GOVERNMENT", "SOLE_PROPRIETOR")
        private val VERTICAL_TYPES = setOf(
            "AUTOMOTIVE", "AGRICULTURE", "BANKING", "COMMUNICATION", "CONSTRUCTION", "EDUCATION",
            "ENERGY", "ENTERTAINMENT", "GOVERNMENT", "HEALTHCARE", "HOSPITALITY", "INSURANCE",
            "LEGAL", "MANUFACTURING", "NON_PROFIT", "PROFESSIONAL", "REAL_ESTATE", "RETAIL",
            "TECHNOLOGY", "TRANSPORTATION"
        )
        private val TAX_ID_COUNTRIES = setOf("US", "CA", "GB", "AU")
        private val STOCK_EXCHANGES = setOf("NASDAQ", "NYSE", "AMEX", "TSX", "LON", "JPX", "HKEX", "OTHER")
    }

    fun create(data: BrandRequest): BrandResponse {
        validate(data, isCreate = true)
        return apiClient.request(
            method = "POST",
            endpoint = "/v1/brands",
            data = data,
            baseUrl = config.complianceBaseUrl,
            responseClass = BrandResponse::class.java
        )
    }

    fun get(id: Long): BrandResponse {
        return apiClient.request(
            method = "GET",
            endpoint = "/v1/brands/$id",
            baseUrl = config.complianceBaseUrl,
            responseClass = BrandResponse::class.java
        )
    }

    fun list(): Array<BrandResponse> {
        return apiClient.request(
            method = "GET",
            endpoint = "/v1/brands",
            baseUrl = config.complianceBaseUrl,
            responseClass = Array<BrandResponse>::class.java
        )
    }

    fun update(id: Long, data: BrandRequest): BrandResponse {
        validate(data, isCreate = false)
        return apiClient.request(
            method = "PATCH",
            endpoint = "/v1/brands/$id",
            data = data,
            baseUrl = config.complianceBaseUrl,
            responseClass = BrandResponse::class.java
        )
    }

    fun delete(id: Long) {
        apiClient.requestNoContent(
            method = "DELETE",
            endpoint = "/v1/brands/$id",
            baseUrl = config.complianceBaseUrl
        )
    }

    private fun validate(data: BrandRequest, isCreate: Boolean) {
        val errors = mutableListOf<String>()

        if (isCreate) {
            if (data.legalCompanyName.isNullOrBlank()) errors.add("legalCompanyName is required")
            if (data.entityType.isNullOrBlank()) errors.add("entityType is required")
            if (data.taxId.isNullOrBlank()) errors.add("taxId is required")
            if (data.taxIdCountry.isNullOrBlank()) errors.add("taxIdCountry is required")
            if (data.country.isNullOrBlank()) errors.add("country is required")
            if (data.verticalType.isNullOrBlank()) errors.add("verticalType is required")
            if (data.websiteUrl.isNullOrBlank()) errors.add("websiteUrl is required")
            if (data.street.isNullOrBlank()) errors.add("street is required")
            if (data.city.isNullOrBlank()) errors.add("city is required")
            if (data.state.isNullOrBlank()) errors.add("state is required")
            if (data.postalCode.isNullOrBlank()) errors.add("postalCode is required")
            if (data.contactFirstName.isNullOrBlank()) errors.add("contactFirstName is required")
            if (data.contactLastName.isNullOrBlank()) errors.add("contactLastName is required")
            if (data.contactEmail.isNullOrBlank()) errors.add("contactEmail is required")
            if (data.contactPhone.isNullOrBlank()) errors.add("contactPhone is required")
        }

        data.entityType?.let { if (it !in ENTITY_TYPES) errors.add("Invalid entity type") }
        data.verticalType?.let { if (it !in VERTICAL_TYPES) errors.add("Invalid vertical type") }
        data.taxIdCountry?.let { if (it !in TAX_ID_COUNTRIES) errors.add("Invalid tax ID country") }
        data.stockExchange?.let { if (it !in STOCK_EXCHANGES) errors.add("Invalid stock exchange") }

        data.websiteUrl?.let {
            if (!it.startsWith("http://") && !it.startsWith("https://")) errors.add("Website URL must start with http:// or https://")
        }

        data.contactEmail?.let {
            if (!it.matches(Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$"))) errors.add("Invalid email format")
        }

        if (data.taxId != null && data.taxIdCountry != null && data.taxIdCountry in setOf("US", "CA")) {
            if (!data.taxId.matches(Regex("^\\d{9}$"))) errors.add("Tax ID must be exactly 9 digits for ${data.taxIdCountry}")
        }

        if (data.entityType == "PUBLIC_PROFIT") {
            if (data.stockSymbol.isNullOrBlank()) errors.add("Stock symbol is required for PUBLIC_PROFIT entities")
            if (data.stockExchange.isNullOrBlank()) errors.add("Stock exchange is required for PUBLIC_PROFIT entities")
        }

        if (errors.isNotEmpty()) {
            throw CCAIException("Validation failed: ${errors.joinToString(", ")}")
        }
    }
}
