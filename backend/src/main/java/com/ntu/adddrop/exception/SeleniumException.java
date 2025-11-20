package com.ntu.adddrop.exception;

/* Custom exception for Selenium-related errors */
public class SeleniumException extends RuntimeException{
    
    private final String errorCode;

    public SeleniumException(String message) {
        super(message);
        this.errorCode = "SELENIUM_ERROR";
    }
    
    public SeleniumException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "SELENIUM_ERROR";
    }
    
    public SeleniumException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
    
    public SeleniumException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }
    
    public String getErrorCode() {
        return errorCode;
    }

    // Specific error types
    public static class LoginFailedException extends SeleniumException {
        public LoginFailedException(String message) {
            super("LOGIN_FAILED", message);
        }
    }
    
    public static class ElementNotFoundException extends SeleniumException {
        public ElementNotFoundException(String element) {
            super("ELEMENT_NOT_FOUND", "Could not find element: " + element);
        }
    }
    
    public static class TimeoutException extends SeleniumException {
        public TimeoutException(String operation) {
            super("TIMEOUT", "Timeout occurred during: " + operation);
        }
    }
    
    public static class NavigationException extends SeleniumException {
        public NavigationException(String url, String message) {
            super("NAVIGATION_FAILED", "Failed to navigate to " + url + ": " + message);
        }
    }
}
