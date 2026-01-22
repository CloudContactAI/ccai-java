// Copyright (c) 2025 CloudContactAI LLC
// Licensed under the MIT License. See LICENSE in the project root for license information.

package com.cloudcontactai.ccai.mms;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SignedUploadUrlRequest {
    
    @JsonProperty("fileName")
    private String fileName;
    
    @JsonProperty("fileType")
    private String fileType;
    
    @JsonProperty("fileBasePath")
    private String fileBasePath;
    
    @JsonProperty("publicFile")
    private Boolean publicFile;

    public SignedUploadUrlRequest() {}

    public SignedUploadUrlRequest(String fileName, String fileType) {
        this.fileName = fileName;
        this.fileType = fileType;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }
    
    public String getFileBasePath() {
        return fileBasePath;
    }

    public void setFileBasePath(String fileBasePath) {
        this.fileBasePath = fileBasePath;
    }
    
    public Boolean getPublicFile() {
        return publicFile;
    }

    public void setPublicFile(Boolean publicFile) {
        this.publicFile = publicFile;
    }
}
