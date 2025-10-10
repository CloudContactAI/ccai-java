// Copyright (c) 2025 CloudContactAI LLC
// Licensed under the MIT License. See LICENSE in the project root for license information.

package com.cloudcontactai.ccai.email;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.Map;

/**
 * Request object for sending email messages
 */
public class EmailRequest {

    @NotNull
    @NotEmpty
    @JsonProperty("to_emails")
    private List<@Email String> toEmails;

    @NotBlank
    @JsonProperty("subject")
    private String subject;

    @NotBlank
    @JsonProperty("html_content")
    private String htmlContent;

    @JsonProperty("text_content")
    private String textContent;

    @Email
    @JsonProperty("from_email")
    private String fromEmail;

    @JsonProperty("from_name")
    private String fromName;

    @JsonProperty("reply_to")
    private String replyTo;

    @JsonProperty("custom_data")
    private Map<String, Object> customData;

    @JsonProperty("campaign_id")
    private String campaignId;

    @JsonProperty("account_id")
    private String accountId;

    @JsonProperty("template_id")
    private String templateId;

    @JsonProperty("variables")
    private Map<String, String> variables;

    // Constructors
    public EmailRequest() {}

    public EmailRequest(List<String> toEmails, String subject, String htmlContent) {
        this.toEmails = toEmails;
        this.subject = subject;
        this.htmlContent = htmlContent;
    }

    // Getters and Setters
    public List<String> getToEmails() {
        return toEmails;
    }

    public void setToEmails(List<String> toEmails) {
        this.toEmails = toEmails;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getHtmlContent() {
        return htmlContent;
    }

    public void setHtmlContent(String htmlContent) {
        this.htmlContent = htmlContent;
    }

    public String getTextContent() {
        return textContent;
    }

    public void setTextContent(String textContent) {
        this.textContent = textContent;
    }

    public String getFromEmail() {
        return fromEmail;
    }

    public void setFromEmail(String fromEmail) {
        this.fromEmail = fromEmail;
    }

    public String getFromName() {
        return fromName;
    }

    public void setFromName(String fromName) {
        this.fromName = fromName;
    }

    public String getReplyTo() {
        return replyTo;
    }

    public void setReplyTo(String replyTo) {
        this.replyTo = replyTo;
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

    public String getTemplateId() {
        return templateId;
    }

    public void setTemplateId(String templateId) {
        this.templateId = templateId;
    }

    public Map<String, String> getVariables() {
        return variables;
    }

    public void setVariables(Map<String, String> variables) {
        this.variables = variables;
    }

    @Override
    public String toString() {
        return "EmailRequest{" +
                "toEmails=" + toEmails +
                ", subject='" + subject + '\'' +
                ", htmlContent='" + (htmlContent != null ? htmlContent.substring(0, Math.min(50, htmlContent.length())) + "..." : null) + '\'' +
                ", textContent='" + (textContent != null ? textContent.substring(0, Math.min(50, textContent.length())) + "..." : null) + '\'' +
                ", fromEmail='" + fromEmail + '\'' +
                ", fromName='" + fromName + '\'' +
                ", replyTo='" + replyTo + '\'' +
                ", customData=" + customData +
                ", campaignId='" + campaignId + '\'' +
                ", accountId='" + accountId + '\'' +
                ", templateId='" + templateId + '\'' +
                ", variables=" + variables +
                '}';
    }
}
