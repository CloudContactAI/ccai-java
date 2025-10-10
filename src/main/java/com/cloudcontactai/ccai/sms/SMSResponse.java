// Copyright (c) 2025 CloudContactAI LLC
// Licensed under the MIT License. See LICENSE in the project root for license information.

package com.cloudcontactai.ccai.sms;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Response object for SMS operations
 */
public class SMSResponse {

    @JsonProperty("success")
    private boolean success;

    @JsonProperty("message")
    private String message;

    @JsonProperty("campaign_id")
    private String campaignId;

    @JsonProperty("sent_count")
    private int sentCount;

    @JsonProperty("failed_count")
    private int failedCount;

    @JsonProperty("failed_numbers")
    private List<String> failedNumbers;

    @JsonProperty("timestamp")
    private LocalDateTime timestamp;

    @JsonProperty("cost")
    private Double cost;

    @JsonProperty("error_code")
    private String errorCode;

    // Constructors
    public SMSResponse() {}

    // Getters and Setters
    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getCampaignId() {
        return campaignId;
    }

    public void setCampaignId(String campaignId) {
        this.campaignId = campaignId;
    }

    public int getSentCount() {
        return sentCount;
    }

    public void setSentCount(int sentCount) {
        this.sentCount = sentCount;
    }

    public int getFailedCount() {
        return failedCount;
    }

    public void setFailedCount(int failedCount) {
        this.failedCount = failedCount;
    }

    public List<String> getFailedNumbers() {
        return failedNumbers;
    }

    public void setFailedNumbers(List<String> failedNumbers) {
        this.failedNumbers = failedNumbers;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public Double getCost() {
        return cost;
    }

    public void setCost(Double cost) {
        this.cost = cost;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    @Override
    public String toString() {
        return "SMSResponse{" +
                "success=" + success +
                ", message='" + message + '\'' +
                ", campaignId='" + campaignId + '\'' +
                ", sentCount=" + sentCount +
                ", failedCount=" + failedCount +
                ", failedNumbers=" + failedNumbers +
                ", timestamp=" + timestamp +
                ", cost=" + cost +
                ", errorCode='" + errorCode + '\'' +
                '}';
    }
}
