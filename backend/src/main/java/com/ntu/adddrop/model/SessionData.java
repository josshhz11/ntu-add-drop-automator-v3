package com.ntu.adddrop.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

// FastAPI's session_data stsructure
public class SessionData {
    
    private String username;

    @JsonProperty("encrypted_password")
    private String encryptedPassword;

    private boolean authenticated;

    @JsonProperty("created_at")
    private long createdAt;

    @JsonProperty("expires_at")
    private long expiresAt;

    // Swap status fields
    @JsonProperty("swap_status")
    private String swapStatus; // "Idle", "Processing", "Completed", "Error", "Stopped", "Timed Out"

    @JsonProperty("swap_message")
    private String swapMessage;

    @JsonProperty("swap_started_at")
    private Long swapStartedAt;

    // Module data
    private List<ModuleStatus> modules;

    // Nested class for module
    public static class ModuleStatus {
        @JsonProperty("old_index")
        private String oldIndex;

        @JsonProperty("new_indexes")
        private List<String> newIndexes;

        private boolean swapped;

        private String message;

        // Constructors
        public ModuleStatus() {}

        public ModuleStatus(String oldIndex, List<String> newIndexes, boolean swapped, String message) {
            this.oldIndex = oldIndex;
            this.newIndexes = newIndexes;
            this.swapped = swapped;
            this.message = message;
        }

        // Getters and Setters
        public String getOldIndex() {
            return oldIndex;
        }

        public void setOldIndex(String oldIndex) {
            this.oldIndex = oldIndex;
        }

        public List<String> getNewIndexes() {
            return newIndexes;
        }

        public void setNewIndexes(List<String> newIndexes) {
            this.newIndexes = newIndexes;
        }

        public boolean isSwapped() {
            return swapped;
        }

        public void setSwapped(boolean swapped) {
            this.swapped = swapped;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }

    // Constructors
    public SessionData() {}

    public SessionData(String username, String encryptedPassword, boolean authenticated) {
        this.username = username;
        this.encryptedPassword = encryptedPassword;
        this.authenticated = authenticated;
        this.createdAt = System.currentTimeMillis() / 1000;
        this.expiresAt = System.currentTimeMillis() / 1000 + 7200; // 2 hours
        this.swapStatus = "Idle";
    }

    // All Getters and Setters
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getEncryptedPassword() {
        return encryptedPassword;
    }
    
    public void setEncryptedPassword(String encryptedPassword) {
        this.encryptedPassword = encryptedPassword;
    }
    
    public boolean isAuthenticated() {
        return authenticated;
    }
    
    public void setAuthenticated(boolean authenticated) {
        this.authenticated = authenticated;
    }
    
    public long getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }
    
    public long getExpiresAt() {
        return expiresAt;
    }
    
    public void setExpiresAt(long expiresAt) {
        this.expiresAt = expiresAt;
    }
    
    public String getSwapStatus() {
        return swapStatus;
    }
    
    public void setSwapStatus(String swapStatus) {
        this.swapStatus = swapStatus;
    }
    
    public String getSwapMessage() {
        return swapMessage;
    }
    
    public void setSwapMessage(String swapMessage) {
        this.swapMessage = swapMessage;
    }
    
    public Long getSwapStartedAt() {
        return swapStartedAt;
    }
    
    public void setSwapStartedAt(Long swapStartedAt) {
        this.swapStartedAt = swapStartedAt;
    }
    
    public List<ModuleStatus> getModules() {
        return modules;
    }
    
    public void setModules(List<ModuleStatus> modules) {
        this.modules = modules;
    }
}
