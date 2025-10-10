// Copyright (c) 2025 CloudContactAI LLC
// Licensed under the MIT License. See LICENSE in the project root for license information.

package com.cloudcontactai.ccai.exception;

/**
 * Base exception for all CCAI-related errors
 */
public class CCAIException extends Exception {
    
    private final int statusCode;
    private final String errorCode;

    public CCAIException(String message) {
        super(message);
        this.statusCode = 0;
        this.errorCode = null;
    }

    public CCAIException(String message, Throwable cause) {
        super(message, cause);
        this.statusCode = 0;
        this.errorCode = null;
    }

    public CCAIException(String message, int statusCode, String errorCode) {
        super(message);
        this.statusCode = statusCode;
        this.errorCode = errorCode;
    }

    public CCAIException(String message, int statusCode, String errorCode, Throwable cause) {
        super(message, cause);
        this.statusCode = statusCode;
        this.errorCode = errorCode;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getErrorCode() {
        return errorCode;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(super.toString());
        if (statusCode > 0) {
            sb.append(" (Status: ").append(statusCode).append(")");
        }
        if (errorCode != null) {
            sb.append(" (Error Code: ").append(errorCode).append(")");
        }
        return sb.toString();
    }
}
