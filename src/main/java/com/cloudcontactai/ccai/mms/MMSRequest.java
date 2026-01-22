// Copyright (c) 2025 CloudContactAI LLC
// Licensed under the MIT License. See LICENSE in the project root for license information.

package com.cloudcontactai.ccai.mms;

import com.cloudcontactai.ccai.sms.SMSRequest;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class MMSRequest {
    
    @JsonProperty("pictureFileKey")
    private String pictureFileKey;
    
    @JsonProperty("accounts")
    private List<Account> accounts;
    
    @JsonProperty("message")
    private String message;
    
    @JsonProperty("title")
    private String title;
    
    @JsonProperty("clientId")
    private String clientId;

    public MMSRequest() {}

    public MMSRequest(String pictureFileKey, List<Account> accounts, String message, String title, String clientId) {
        this.pictureFileKey = pictureFileKey;
        this.accounts = accounts;
        this.message = message;
        this.title = title;
        this.clientId = clientId;
    }

    public String getPictureFileKey() {
        return pictureFileKey;
    }

    public void setPictureFileKey(String pictureFileKey) {
        this.pictureFileKey = pictureFileKey;
    }

    public List<Account> getAccounts() {
        return accounts;
    }

    public void setAccounts(List<Account> accounts) {
        this.accounts = accounts;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }
}
