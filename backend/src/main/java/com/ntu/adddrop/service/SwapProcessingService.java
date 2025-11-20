package com.ntu.adddrop.service;

import com.ntu.adddrop.exception.SwapProcessingException;
import com.ntu.adddrop.model.SessionData;
import com.ntu.adddrop.model.SessionData.ModuleStatus;
import com.ntu.adddrop.util.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service for managing swap processing and async operations
 * Implements FastAPI async handling and session management
 */
@Service
public class SwapProcessingService {
    
    private static final Logger logger = LoggerFactory.getLogger(SwapProcessingService.class);
    
    @Autowired
    private SessionService sessionService;
    
    @Autowired
    private SeleniumService seleniumService;
    
    // Track running swap processes
    private final Map<String, CompletableFuture<Void>> runningSwaps = new ConcurrentHashMap<>();
    
    /**
     * Initialize swap data in session and start processing
     * Implements FastAPI initialize_swap_in_session + thread.start()
     */
    public void startSwapProcess(String sessionId, List<SwapItem> swapItems) {
        try {
            logger.info("Starting swap process for session: {} with {} modules", sessionId, swapItems.size());
            
            // Convert SwapItems to ModuleStatus objects (matches your FastAPI structure)
            List<ModuleStatus> modules = new ArrayList<>();
            for (SwapItem item : swapItems) {
                ModuleStatus moduleStatus = new ModuleStatus();
                moduleStatus.setOldIndex(item.getOldIndex());
                moduleStatus.setNewIndexes(item.getNewIndexes());
                moduleStatus.setSwapped(false);
                moduleStatus.setMessage("Pending...");
                modules.add(moduleStatus);
            }
            
            // Initialize swap data in session (matches your initialize_swap_in_session)
            sessionService.updateOverallSwapStatus(sessionId, Constants.SwapStatus.PROCESSING, 
                "Your swap request is being processed");
            sessionService.updateModules(sessionId, modules);
            
            // Start async swap process (matches your threading.Thread)
            CompletableFuture<Void> swapFuture = seleniumService.performSwapsAsync(sessionId)
                .whenComplete((result, throwable) -> {
                    // Clean up when done
                    runningSwaps.remove(sessionId);
                    if (throwable != null) {
                        logger.error("Swap process failed for session: {}: {}", sessionId, throwable.getMessage());
                        sessionService.updateOverallSwapStatus(sessionId, Constants.SwapStatus.ERROR, 
                            "Swap process failed: " + throwable.getMessage());
                    }
                });
            
            // Track the running swap
            runningSwaps.put(sessionId, swapFuture);
            
            logger.info("Swap process started successfully for session: {}", sessionId);
            
        } catch (Exception e) {
            logger.error("Failed to start swap process for session: {}: {}", sessionId, e.getMessage(), e);
            throw new SwapProcessingException(sessionId, "Failed to start swap process: " + e.getMessage(), e);
        }
    }
    
    /* Stop swap process for a session */
    public void stopSwapProcess(String sessionId) {
        try {
            logger.info("Stopping swap process for session: {}", sessionId);
            
            // Cancel running swap
            CompletableFuture<Void> runningSwap = runningSwaps.get(sessionId);
            if (runningSwap != null) {
                runningSwap.cancel(true);
                runningSwaps.remove(sessionId);
            }
            
            // Update status and cleanup
            seleniumService.stopSwap(sessionId);
            
            logger.info("Swap process stopped for session: {}", sessionId);
            
        } catch (Exception e) {
            logger.error("Error stopping swap process for session: {}: {}", sessionId, e.getMessage());
            throw new SwapProcessingException(sessionId, "Failed to stop swap process: " + e.getMessage(), e);
        }
    }
    
    /* Get swap status for a session */
    public SwapStatusResponse getSwapStatus(String sessionId) {
        try {
            SessionData sessionData = sessionService.getSecureSession(sessionId);
            
            SwapStatusResponse response = new SwapStatusResponse();
            response.setStatus(sessionData.getSwapStatus());
            response.setMessage(sessionData.getSwapMessage());
            response.setDetails(sessionData.getModules());
            response.setStartedAt(sessionData.getSwapStartedAt());
            
            return response;
            
        } catch (Exception e) {
            logger.error("Error getting swap status for session: {}: {}", sessionId, e.getMessage());
            throw new SwapProcessingException(sessionId, "Failed to get swap status: " + e.getMessage(), e);
        }
    }
    
    /* Check if swap is currently running */
    public boolean isSwapRunning(String sessionId) {
        return runningSwaps.containsKey(sessionId) || seleniumService.isSwapRunning(sessionId);
    }
    
    /* Get count of active swaps */
    public int getActiveSwapCount() {
        return runningSwaps.size();
    }
    
    /* SwapItem class for input data */
    public static class SwapItem {
        private String oldIndex;
        private List<String> newIndexes;
        
        public SwapItem() {}
        
        public SwapItem(String oldIndex, List<String> newIndexes) {
            this.oldIndex = oldIndex;
            this.newIndexes = newIndexes;
        }
        
        // Getters and setters
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
    }
    
    /* SwapStatusResponse class for output data */
    public static class SwapStatusResponse {
        private String status;
        private String message;
        private List<ModuleStatus> details;
        private Long startedAt;
        
        // Getters and setters
        public String getStatus() {
            return status;
        }
        
        public void setStatus(String status) {
            this.status = status;
        }
        
        public String getMessage() {
            return message;
        }
        
        public void setMessage(String message) {
            this.message = message;
        }
        
        public List<ModuleStatus> getDetails() {
            return details;
        }
        
        public void setDetails(List<ModuleStatus> details) {
            this.details = details;
        }
        
        public Long getStartedAt() {
            return startedAt;
        }
        
        public void setStartedAt(Long startedAt) {
            this.startedAt = startedAt;
        }
    }
}