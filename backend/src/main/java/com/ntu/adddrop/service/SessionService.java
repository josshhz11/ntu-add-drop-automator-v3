package com.ntu.adddrop.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ntu.adddrop.model.SessionData;
import com.ntu.adddrop.model.SessionData.ModuleStatus;
import com.ntu.adddrop.security.EncryptionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;
import java.util.Base64;
import java.util.List;

@Service
public class SessionService {
    
    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private ObjectMapper objectMapper;
    
    @Autowired
    private EncryptionService encryptionService;

    private static final Duration SESSION_TTL = Duration.ofHours(2);
    private static final SecureRandom secureRandom = new SecureRandom();

    /**
     * FastAPI's create_secure_session function
     */
    public String createSecureSession(String username, String encryptedPassword) {
        String sessionId = generateSessionId();

        SessionData sessionData = new SessionData();
        sessionData.setUsername(username);
        sessionData.setEncryptedPassword(encryptedPassword);
        sessionData.setAuthenticated(true);
        sessionData.setCreatedAt(System.currentTimeMillis() / 1000);
        sessionData.setExpiresAt(System.currentTimeMillis() / 1000 + 7200);
        sessionData.setSwapStatus("Idle");
        sessionData.setSwapMessage(null);
        sessionData.setSwapStartedAt(null);

        try {
            String sessionJson = objectMapper.writeValueAsString(sessionData);
            redisTemplate.opsForValue().set(
                "session:" + sessionId,
                sessionJson,
                SESSION_TTL
            );
            return sessionId;
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize session data", e);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create session", e);
        }
    }

    public SessionData getSecureSession(String sessionId) {
        try {
            String sessionJson = redisTemplate.opsForValue().get("session:" + sessionId);
            if (sessionJson == null) {
                throw new SecurityException("Session expired or invalid");
            }

            SessionData sessionData = objectMapper.readValue(sessionJson, SessionData.class);

            if (System.currentTimeMillis() / 1000 > sessionData.getExpiresAt()) {
                redisTemplate.delete("session:" + sessionId);
                throw new SecurityException("Session expired");
            }

            return sessionData;
        } catch (SecurityException e) {
            throw e; // Re-throw security exceptions as-is
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to deserialize session data", e);
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve session", e);
        }
    }

    public void updateSessionData(String sessionId, SessionData updates) {
        try {
            SessionData sessionData = getSecureSession(sessionId);

            if (updates.getSwapStatus() != null) {
                sessionData.setSwapStatus(updates.getSwapStatus());
            }
            if (updates.getSwapMessage() != null) {
                sessionData.setSwapMessage(updates.getSwapMessage());
            }
            if (updates.getSwapStartedAt() != null) {
                sessionData.setSwapStartedAt(updates.getSwapStartedAt());
            }
            if (updates.getModules() != null) {
                sessionData.setModules(updates.getModules());
            }

            String sessionJson = objectMapper.writeValueAsString(sessionData);

            Long ttl = redisTemplate.getExpire("session:" + sessionId);
            if (ttl != null && ttl > 0) {
                redisTemplate.opsForValue().set(
                    "session:" + sessionId,
                    sessionJson,
                    Duration.ofSeconds(ttl)
                );
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize session updates", e);
        } catch (Exception e) {
            throw new RuntimeException("Failed to update session", e);
        }
    }

    /**
     * Update overall swap status - matches FastAPI update_overall_swap_status
     */
    public void updateOverallSwapStatus(String sessionId, String status, String message) {
        SessionData updates = new SessionData();
        updates.setSwapStatus(status);
        updates.setSwapMessage(message);
        updateSessionData(sessionId, updates);
    }
    
    /**
     * Initialize modules in session - matches FastAPI initialize_swap_in_session
     */
    public void updateModules(String sessionId, List<ModuleStatus> modules) {
        SessionData updates = new SessionData();
        updates.setModules(modules);
        updates.setSwapStartedAt(System.currentTimeMillis() / 1000);
        updateSessionData(sessionId, updates);
    }
    
    /**
     * Update specific module status - matches FastAPI update_module_status
     */
    public void updateModuleStatus(String sessionId, int moduleIdx, ModuleStatus updatedModule) {
        try {
            SessionData sessionData = getSecureSession(sessionId);
            List<ModuleStatus> modules = sessionData.getModules();
            
            if (moduleIdx >= 0 && moduleIdx < modules.size()) {
                modules.set(moduleIdx, updatedModule);
                
                SessionData updates = new SessionData();
                updates.setModules(modules);
                updateSessionData(sessionId, updates);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to update module status", e);
        }
    }
    
    /**
     * Get decrypted credentials - matches FastAPI get_decrypted_credentials
     */
    public String[] getDecryptedCredentials(String sessionId) {
        SessionData sessionData = getSecureSession(sessionId);
        String username = sessionData.getUsername();
        String encryptedPassword = sessionData.getEncryptedPassword();
        String password = encryptionService.decrypt(encryptedPassword);
        
        return new String[]{username, password};
    }

    public void cleanupSession(String sessionId) {
        redisTemplate.delete("session:" + sessionId);
    }

    private String generateSessionId() {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}