// Copyright (c) 2025 CloudContactAI LLC
// Licensed under the MIT License. See LICENSE in the project root for license information.

package com.cloudcontactai.ccai.sms;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.Map;

/**
 * Request object for sending SMS messages
 */
public class SMSRequest {

    @NotNull
    @NotEmpty
    @JsonProperty("phone_numbers")
    private List<String> phoneNumbers;

    @NotBlank
    @JsonProperty("message")
    private String message;

    @JsonProperty("custom_data")
    private Map<String, Object> customData;

    @JsonProperty("campaign_id")
    private String campaignId;

    @JsonProperty("account_id")
    private String accountId;

    // Constructors
    public SMSRequest() {}

    public SMSRequest(List<String> phoneNumbers, String message) {
        this.phoneNumbers = phoneNumbers;
        this.message = message;
    }

    // Getters and Setters
    public List<String> getPhoneNumbers() {
        return phoneNumbers;
    }

    public void setPhoneNumbers(List<String> phoneNumbers) {
        this.phoneNumbers = phoneNumbers;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Map<String, Object> getCustomData() {
        return customData;
    }

    public void setCustomData(Map<String, Object> customData) {
        this.customData = customData;
    }

    public String getCampaignId() {
        return campaignId;
    }

    public void setCampaignId(String campaignId) {
        this.campaignId = campaignId;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    @Override
    public String toString() {
        return "SMSRequest{" +
                "phoneNumbers=" + phoneNumbers +
                ", message='" + message + '\'' +
                ", customData=" + customData +
                ", campaignId='" + campaignId + '\'' +
                ", accountId='" + accountId + '\'' +
                '}';
    }
}
