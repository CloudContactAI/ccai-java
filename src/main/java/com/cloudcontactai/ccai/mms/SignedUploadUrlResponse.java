// Copyright (c) 2025 CloudContactAI LLC
// Licensed under the MIT License. See LICENSE in the project root for license information.

package com.cloudcontactai.ccai.mms;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SignedUploadUrlResponse {
    
    @JsonProperty("signedS3Url")
    private String signedS3Url;
    
    @JsonProperty("fileKey")
    private String fileKey;

    public SignedUploadUrlResponse() {}

    public SignedUploadUrlResponse(String signedS3Url, String fileKey) {
        this.signedS3Url = signedS3Url;
        this.fileKey = fileKey;
    }

    public String getSignedS3Url() {
        return signedS3Url;
    }

    public void setSignedS3Url(String signedS3Url) {
        this.signedS3Url = signedS3Url;
    }

    public String getFileKey() {
        return fileKey;
    }

    public void setFileKey(String fileKey) {
        this.fileKey = fileKey;
    }
}
