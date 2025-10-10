// Copyright (c) 2025 CloudContactAI LLC
// Licensed under the MIT License. See LICENSE in the project root for license information.

package com.cloudcontactai.ccai.exception;

/**
 * Exception thrown when API calls fail
 */
public class CCAIApiException extends CCAIException {

    public CCAIApiException(String message) {
        super(message);
    }

    public CCAIApiException(String message, Throwable cause) {
        super(message, cause);
    }

    public CCAIApiException(String message, int statusCode, String errorCode) {
        super(message, statusCode, errorCode);
    }

    public CCAIApiException(String message, int statusCode, String errorCode, Throwable cause) {
        super(message, statusCode, errorCode, cause);
    }
}
