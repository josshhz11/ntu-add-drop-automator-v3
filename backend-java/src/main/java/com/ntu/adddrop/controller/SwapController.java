package com.ntu.adddrop.controller;

import com.ntu.adddrop.service.SwapProcessingService;
import com.ntu.adddrop.service.SwapProcessingService.SwapItem;
import com.ntu.adddrop.service.SwapProcessingService.SwapStatusResponse;
import com.ntu.adddrop.service.SessionService;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = {
    "http://localhost:3000",
    "https://ntu-add-drop-automator.vercel.app",
    "https://ntu-add-drop-automator-*.vercel.app"
})
public class SwapController {
    
    private static final Logger logger = LoggerFactory.getLogger(SwapController.class);
    
    @Autowired
    private SwapProcessingService swapProcessingService;
    
    @Autowired
    private SessionService sessionService;
    
    /**
     * Submit swap request - matches your FastAPI /api/submit-swap
     */
    @PostMapping("/submit-swap")
    public ResponseEntity<Map<String, Object>> submitSwap(
            @RequestBody Map<String, Object> requestData,
            HttpServletRequest request) {
        
        try {
            logger.info("Received swap submission request");
            
            // Get session ID from request session (matches your FastAPI logic)
            String sessionId = (String) request.getSession().getAttribute("sessionId");
            if (sessionId == null) {
                return ResponseEntity.status(401).body(Map.of(
                    "success", false,
                    "message", "No active session"
                ));
            }
            
            // Validate session exists in Redis
            sessionService.getSecureSession(sessionId);
            
            // Parse module data (matches your FastAPI parsing)
            Integer numModules = (Integer) requestData.get("num_modules");
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> moduleData = (List<Map<String, Object>>) requestData.get("modules");
            
            if (numModules == null || numModules <= 0 || moduleData == null || moduleData.size() != numModules) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Invalid module data"
                ));
            }
            
            // Parse modules into SwapItems (matches your FastAPI swap_items logic)
            List<SwapItem> swapItems = new ArrayList<>();
            for (int i = 0; i < moduleData.size(); i++) {
                Map<String, Object> module = moduleData.get(i);
                
                String oldIndex = (String) module.get("old_index");
                String newIndexesRaw = (String) module.get("new_indexes");
                
                if (oldIndex == null || newIndexesRaw == null || oldIndex.trim().isEmpty() || newIndexesRaw.trim().isEmpty()) {
                    return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "Missing or invalid data for module " + (i + 1)
                    ));
                }
                
                // Parse comma-separated new indexes
                List<String> newIndexList = Arrays.stream(newIndexesRaw.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .toList();
                
                if (newIndexList.isEmpty()) {
                    return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "No valid new indexes provided for module " + (i + 1)
                    ));
                }
                
                swapItems.add(new SwapItem(oldIndex, newIndexList));
            }
            
            // Start swap process (matches your FastAPI thread.start())
            swapProcessingService.startSwapProcess(sessionId, swapItems);
            
            logger.info("Swap process started successfully for session: {}", sessionId);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "session_id", sessionId,
                "message", "Swap process started successfully"
            ));
            
        } catch (Exception e) {
            logger.error("Error submitting swap request: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", "Swap initiation failed: " + e.getMessage()
            ));
        }
    }
    
    /**
     * Get swap status - matches your FastAPI /api/swap-status/{session_id}
     */
    @GetMapping("/swap-status/{sessionId}")
    public ResponseEntity<SwapStatusResponse> getSwapStatus(@PathVariable String sessionId) {
        try {
            logger.debug("Getting swap status for session: {}", sessionId);
            
            SwapStatusResponse status = swapProcessingService.getSwapStatus(sessionId);
            return ResponseEntity.ok(status);
            
        } catch (Exception e) {
            logger.error("Error getting swap status for session: {}: {}", sessionId, e.getMessage());
            return ResponseEntity.status(500).body(null);
        }
    }
    
    /**
     * Stop swap - matches your FastAPI /api/stop-swap/{session_id}
     */
    @PostMapping("/stop-swap/{sessionId}")
    public ResponseEntity<Map<String, Object>> stopSwap(
            @PathVariable String sessionId,
            HttpServletRequest request) {
        
        try {
            logger.info("Stopping swap for session: {}", sessionId);
            
            // Stop the swap process
            swapProcessingService.stopSwapProcess(sessionId);
            
            // Clean up session (matches your FastAPI cleanup)
            sessionService.cleanupSession(sessionId);
            request.getSession().invalidate();
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Swap successfully stopped"
            ));
            
        } catch (Exception e) {
            logger.error("Error stopping swap for session: {}: {}", sessionId, e.getMessage());
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", "Error stopping swap: " + e.getMessage()
            ));
        }
    }
}