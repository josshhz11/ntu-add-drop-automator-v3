package com.ntu.adddrop.exception;

/* Custom exceptions for swap-processing errors */
public class SwapProcessingException extends RuntimeException {
    
    private final String errorCode;
    private final String sessionId;

    public SwapProcessingException(String sessionId, String message) {
        super(message);
        this.sessionId = sessionId;
        this.errorCode = "SWAP_PROCESSING_ERROR";
    }

    public SwapProcessingException(String sessionId, String message, Throwable cause) {
        super(message, cause);
        this.sessionId = sessionId;
        this.errorCode = "SWAP_PROCESSING_ERROR";
    }

    public SwapProcessingException(String errorCode, String sessionId, String message) {
        super(message);
        this.sessionId = sessionId;
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getSessionId() {
        return sessionId;
    }

    // Specific swap error types
    public static class ModuleNotAvailableException extends SwapProcessingException {
        public ModuleNotAvailableException(String sessionId, String moduleIndex) {
            super("MODULE_NOT_AVAILABLE", sessionId, "Module with index " + moduleIndex + " is not available");
        }
    }

    public static class SwapTimeoutException extends SwapProcessingException {
        public SwapTimeoutException(String sessionId, String operation) {
            super("SWAP_TIMEOUT", sessionId, "Swap operations timed out: " + operation);
        }
    }
    
    public static class InvalidModuleException extends SwapProcessingException {
        public InvalidModuleException(String sessionId, String moduleIndex) {
            super("INVALID_MODULE", sessionId, "Invalid module index " + moduleIndex);
        }
    }
}
