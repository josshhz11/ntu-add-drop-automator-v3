package com.ntu.adddrop.model;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// FastAPI's Pydantic Model LoginRequest
public class LoginRequest {
    
    @NotBlank(message = "Username is required")
    @Size(min = 1, max = 50, message = "Username must be between 1 and 50 characters")
    private String username;

    @NotBlank(message = "Password is required")
    @Size(min = 1, max = 100, message = "Password must be between 1 and 100 characters")
    private String password;

    @Min(value = 1, message = "Number of modules must be at least 1")
    @Max(value = 6, message = "Number of modules cannot exceed 6")
    private int numModules;

    // Constructors
    public LoginRequest() {}

    public LoginRequest(String username, String password, int numModules) {
        this.username = username;
        this.password = password;
        this.numModules = numModules;
    }

    // Getters and Setters
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getNumModules() {
        return numModules;
    }

    public void setNumModules(int numModules) {
        this.numModules = numModules;
    }

    @Override
    public String toString() {
        return "LoginRequest{username='" + username + "', numModules=" + numModules + "}";
    }
}
