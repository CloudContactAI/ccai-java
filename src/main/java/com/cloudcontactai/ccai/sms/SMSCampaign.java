// Copyright (c) 2025 CloudContactAI LLC
// Licensed under the MIT License. See LICENSE in the project root for license information.

package com.cloudcontactai.ccai.sms;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class SMSCampaign {
    
    @JsonProperty("accounts")
    private List<Account> accounts;
    
    @JsonProperty("message")
    private String message;
    
    @JsonProperty("title")
    private String title;
    
    @JsonProperty("senderPhone")
    private String senderPhone;

    public SMSCampaign() {}

    public SMSCampaign(List<Account> accounts, String message, String title, String senderPhone) {
        this.accounts = accounts;
        this.message = message;
        this.title = title;
        this.senderPhone = senderPhone;
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

    public String getSenderPhone() {
        return senderPhone;
    }

    public void setSenderPhone(String senderPhone) {
        this.senderPhone = senderPhone;
    }
    
    public static class Account {
        @JsonProperty("firstName")
        private String firstName;
        
        @JsonProperty("lastName")
        private String lastName;
        
        @JsonProperty("phone")
        private String phone;

        public Account() {}

        public Account(String firstName, String lastName, String phone) {
            this.firstName = firstName;
            this.lastName = lastName;
            this.phone = phone;
        }

        public String getFirstName() {
            return firstName;
        }

        public void setFirstName(String firstName) {
            this.firstName = firstName;
        }

        public String getLastName() {
            return lastName;
        }

        public void setLastName(String lastName) {
            this.lastName = lastName;
        }

        public String getPhone() {
            return phone;
        }

        public void setPhone(String phone) {
            this.phone = phone;
        }
    }
}
