package com.ntu.adddrop.controller;

import com.ntu.adddrop.model.LoginRequest;
import com.ntu.adddrop.model.SessionData;
import com.ntu.adddrop.security.EncryptionService;
import com.ntu.adddrop.service.SessionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = {
    "http://localhost:3000",
    "https://ntu-add-drop-automator.vercel.app",
    "https://ntu-add-drop-automator*.vercel.app"
})
public class AuthController {
    
    @Autowired
    private SessionService sessionService;

    @Autowired
    private EncryptionService encryptionService;

    /* Login endpoint: /api/login */
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(
        @Valid @RequestBody LoginRequest loginRequest,
        HttpServletRequest request) {
            
        try {
            // Encrypt the password
            String encryptedPassword = encryptionService.encrypt(loginRequest.getPassword());

            // Create secure session
            String sessionId = sessionService.createSecureSession(
                loginRequest.getUsername(),
                encryptedPassword
            );

            // Store some key session information in HTTP session
            request.getSession().setAttribute("sessionId", sessionId);
            request.getSession().setAttribute("authenticated", true);
            request.getSession().setAttribute("numModules", loginRequest.getNumModules());

            // Return 200 OK success response
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Login successful",
                "session_id", sessionId,
                "num_modules", loginRequest.getNumModules()
            ));
        } catch (Exception e) {
            // Return 500 error response
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", "Login failed: " + e.getMessage()
            ));
        }
    }

    /* Logout endpoint: /api/logout/{session_id} */
    @PostMapping("/logout/{sessionId}")
    public ResponseEntity<Map<String, Object>> logout(
        @PathVariable String sessionId,
        HttpServletRequest request) {
        
        try {
            // Clean up session from Redis
            sessionService.cleanupSession(sessionId);

            // Invalidate HTTP session
            request.getSession().invalidate();

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Logged out successfully"
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", "Logout failed: " + e.getMessage()
            ));
        }
    }

    /* Check session status - additional endpoint for frontend */
    @GetMapping("/session-status/{sessionId}")
    public ResponseEntity<Map<String, Object>> getSessionStatus(@PathVariable String sessionId) {
        try {
            SessionData sessionData = sessionService.getSecureSession(sessionId);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "authenticated", sessionData.isAuthenticated(),
                "username", sessionData.getUsername(),
                "swap_status", sessionData.getSwapStatus(),
                "created_at", sessionData.getCreatedAt(),
                "expires_at", sessionData.getExpiresAt()
            ));
        } catch (SecurityException e) {
            return ResponseEntity.status(401).body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", "Failed to get session status: " + e.getMessage()
            ));
        }
    }
}
