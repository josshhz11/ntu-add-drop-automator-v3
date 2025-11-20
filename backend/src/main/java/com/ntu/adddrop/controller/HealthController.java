package com.ntu.adddrop.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class HealthController {
    
    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @GetMapping("/")
    public ResponseEntity<Map<String, Object>> root() {
        return ResponseEntity.ok(Map.of(
            "message", "NTU Add-Drop Automator Java Backend API",
            "status", "running",
            "timestamp", LocalDateTime.now()
        ));
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        try {
            // Test Redis Connection
            redisTemplate.opsForValue().set("health-check", "ok");
            String value = redisTemplate.opsForValue().get("health-check");

            return ResponseEntity.ok(Map.of(
                "status", "healthy",
                "redis", "connected",
                "redis-test", value,
                "timestamp", LocalDateTime.now()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(503).body(Map.of(
                "status", "unhealthy",
                "redis", "disconnected: " + e.getMessage(),
                "timestamp", LocalDateTime.now()
            ));
        }
    }
}
