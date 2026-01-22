// Copyright (c) 2025 CloudContactAI LLC
// Licensed under the MIT License. See LICENSE in the project root for license information.

package com.cloudcontactai.ccai.mms;

import com.cloudcontactai.ccai.config.CCAIConfig;
import com.cloudcontactai.ccai.exception.CCAIApiException;
import com.cloudcontactai.ccai.sms.SMSResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Collections;
import java.util.List;

@Service
public class MMSService {

    private static final Logger logger = LoggerFactory.getLogger(MMSService.class);

    private final CCAIConfig config;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public MMSService(CCAIConfig config, RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.config = config;
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    public SignedUploadUrlResponse getSignedUploadUrl(String fileName, String contentType) throws CCAIApiException {
        String url = "https://files.cloudcontactai.com/upload/url";
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(config.getApiKey());
        
        String fileBasePath = config.getClientId() + "/campaign";
        
        SignedUploadUrlRequest request = new SignedUploadUrlRequest(fileName, contentType);
        request.setFileBasePath(fileBasePath);
        request.setPublicFile(true);
        
        HttpEntity<SignedUploadUrlRequest> entity = new HttpEntity<>(request, headers);
        
        try {
            ResponseEntity<SignedUploadUrlResponse> response = restTemplate.exchange(
                url, HttpMethod.POST, entity, SignedUploadUrlResponse.class);
            
            SignedUploadUrlResponse responseData = response.getBody();
            if (responseData != null) {
                // Override fileKey to match .NET implementation
                responseData.setFileKey(config.getClientId() + "/campaign/" + fileName);
            }
            return responseData;
        } catch (Exception e) {
            throw new CCAIApiException("Failed to get signed upload URL: " + e.getMessage(), e);
        }
    }

    public boolean uploadImageToSignedUrl(String signedUrl, String imagePath, String contentType) throws CCAIApiException {
        try {
            byte[] fileContent;
            
            // Check if it's a resource path (from JAR) or file system path
            if (imagePath.startsWith("file:") && imagePath.contains("!")) {
                // It's inside a JAR, read as resource
                String resourcePath = imagePath.substring(imagePath.lastIndexOf("!") + 2);
                try (var inputStream = getClass().getClassLoader().getResourceAsStream(resourcePath)) {
                    if (inputStream == null) {
                        throw new CCAIApiException("Resource not found: " + resourcePath);
                    }
                    fileContent = inputStream.readAllBytes();
                }
            } else {
                // It's a regular file
                File file = new File(imagePath);
                fileContent = Files.readAllBytes(file.toPath());
            }
            
            // Use HttpURLConnection to avoid URL encoding issues with pre-signed URLs
            java.net.URL url = new java.net.URL(signedUrl);
            java.net.HttpURLConnection connection = (java.net.HttpURLConnection) url.openConnection();
            connection.setDoOutput(true);
            connection.setRequestMethod("PUT");
            connection.setRequestProperty("Content-Type", contentType);
            connection.setRequestProperty("Content-Length", String.valueOf(fileContent.length));
            
            try (var outputStream = connection.getOutputStream()) {
                outputStream.write(fileContent);
            }
            
            int responseCode = connection.getResponseCode();
            connection.disconnect();
            
            return responseCode >= 200 && responseCode < 300;
        } catch (IOException e) {
            throw new CCAIApiException("Failed to read image file: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new CCAIApiException("Failed to upload image: " + e.getMessage(), e);
        }
    }

    public SMSResponse send(String fileKey, List<Account> accounts, String message, String title) throws CCAIApiException {
        String url = config.getEffectiveBaseUrl() + "/clients/" + config.getClientId() + "/campaigns/direct";
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(config.getApiKey());
        headers.set("ForceNewCampaign", "true");
        
        MMSRequest request = new MMSRequest(fileKey, accounts, message, title, config.getClientId());
        HttpEntity<MMSRequest> entity = new HttpEntity<>(request, headers);
        
        try {
            ResponseEntity<SMSResponse> response = restTemplate.exchange(
                url, HttpMethod.POST, entity, SMSResponse.class);
            return response.getBody();
        } catch (Exception e) {
            throw new CCAIApiException("Failed to send MMS: " + e.getMessage(), e);
        }
    }

    public SMSResponse sendSingle(String pictureFileKey, String firstName, String lastName, 
                                   String phone, String message, String title) throws CCAIApiException {
        Account account = new Account(firstName, lastName, phone);
        return send(pictureFileKey, Collections.singletonList(account), message, title);
    }

    public SMSResponse sendWithImage(String imagePath, String contentType, List<Account> accounts,
                                      String message, String title) throws CCAIApiException {
        String fileName = new File(imagePath).getName();
        SignedUploadUrlResponse uploadResponse = getSignedUploadUrl(fileName, contentType);
        
        boolean uploaded = uploadImageToSignedUrl(uploadResponse.getSignedS3Url(), imagePath, contentType);
        if (!uploaded) {
            throw new CCAIApiException("Failed to upload image");
        }
        
        return send(uploadResponse.getFileKey(), accounts, message, title);
    }
}
