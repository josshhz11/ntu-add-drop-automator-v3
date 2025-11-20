package com.ntu.adddrop.service;

import com.ntu.adddrop.model.SessionData;
import com.ntu.adddrop.model.SessionData.ModuleStatus;
import com.ntu.adddrop.selenium.NTULoginAutomator;
import com.ntu.adddrop.selenium.ModuleSwapAutomator;
import com.ntu.adddrop.selenium.WebDriverManager;
import com.ntu.adddrop.util.Constants;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Core Selenium service that orchestrates the entire swap process
 * Implements FastAPI perform_swaps() function in Java
 */
@Service
public class SeleniumService {
    
    private static final Logger logger = LoggerFactory.getLogger(SeleniumService.class);

    @Autowired
    private WebDriverManager webDriverManager;

    @Autowired
    private NTULoginAutomator ntuLoginAutomator;

    @Autowired
    private ModuleSwapAutomator moduleSwapAutomator;

    @Autowired
    private SessionService sessionService;

    /**
     * Main method that performs all swaps for a session - async version of perform_swaps()
     * @param sessionId Unique Session Identifier
     * @return CompletableFuture that completes when swaps are done
     */
    public CompletableFuture<Void> performSwapsAsync(String sessionId) {
        return CompletableFuture.runAsync(() -> {
            performSwaps(sessionId);
        });
    }

    /**
     * Core swap logic - direct port of your FastAPI perform_swaps() function
     * @param sessionId Unique Session Identifier
     */
    public void performSwaps(String sessionId) {
        WebDriver driver = null;

        try {
            logger.info("Starting swap process for session: {}", sessionId);

            // Get decrypted credentials (matches your get_decrypted_credentials)
            String[] credentials = sessionService.getDecryptedCredentials(sessionId);
            String username = credentials[0];
            String password = credentials[1];

            // Create WebDriver
            driver = webDriverManager.createDriver(sessionId);

            // Update status: Logging in (matches your FastAPI)
            sessionService.updateOverallSwapStatus(sessionId, Constants.SwapStatus.PROCESSING, "Logging into NTU portal...");
            
            // Login to portal
            boolean loginSuccess = ntuLoginAutomator.performLogin(driver, username, password);
            if (!loginSuccess) {
                logger.error("Login failed for session: {}", sessionId);
                return; // Login automator already updated status
            }
            
            logger.info("Login successful for session: {}", sessionId);
            
            // Start main swap loop (matches your FastAPI while True loop)
            long startTime = System.currentTimeMillis();
            long timeoutMs = 2 * 60 * 60 * 1000; // 2 hours in milliseconds
            
            while (true) {
                // Check if session still exists (user might have stopped it)
                try {
                    SessionData currentSession = sessionService.getSecureSession(sessionId);
                    if (Constants.SwapStatus.STOPPED.equals(currentSession.getSwapStatus())) {
                        logger.info("Swap stopped by user for session: {}", sessionId);
                        break;
                    }
                } catch (Exception e) {
                    logger.warn("Session expired or deleted: {}", sessionId);
                    break; // Session expired or deleted
                }
                
                // Get current module statuses
                SessionData currentSessionData = sessionService.getSecureSession(sessionId);
                List<ModuleStatus> modules = currentSessionData.getModules();
                
                // Attempt swaps for each module (matches your FastAPI logic)
                for (int idx = 0; idx < modules.size(); idx++) {
                    ModuleStatus module = modules.get(idx);
                    
                    if (!module.isSwapped()) {
                        logger.info("Processing module {} for session: {}", module.getOldIndex(), sessionId);
                        
                        // Try each new index in order of preference
                        boolean swapSuccessful = false;
                        List<String> failedIndexes = new java.util.ArrayList<>();
                        
                        for (String newIndex : module.getNewIndexes()) {
                            try {
                                logger.info("Attempting swap: {} -> {} for session: {}", 
                                    module.getOldIndex(), newIndex, sessionId);
                                
                                ModuleSwapAutomator.SwapResult result = moduleSwapAutomator.attemptSwap(
                                    driver, sessionId, module.getOldIndex(), newIndex);
                                
                                if (result.isSuccess()) {
                                    // Success! Update module status
                                    module.setSwapped(true);
                                    module.setMessage("Successfully swapped " + module.getOldIndex() + " → " + newIndex);
                                    sessionService.updateModuleStatus(sessionId, idx, module);
                                    swapSuccessful = true;
                                    logger.info("Swap successful: {} -> {} for session: {}", 
                                        module.getOldIndex(), newIndex, sessionId);
                                    break;
                                } else {
                                    logger.warn("Swap failed: {} -> {}: {} for session: {}", 
                                        module.getOldIndex(), newIndex, result.getMessage(), sessionId);
                                    failedIndexes.add(newIndex);
                                }
                                
                            } catch (Exception e) {
                                logger.error("Error during swap attempt {} -> {} for session: {}: {}", 
                                    module.getOldIndex(), newIndex, sessionId, e.getMessage(), e);
                                
                                failedIndexes.add(newIndex);
                                
                                // Handle WebDriver errors (matches your FastAPI WebDriverException handling)
                                if (e.getMessage().contains("WebDriver") || e.getMessage().contains("Session")) {
                                    logger.warn("WebDriver error detected, recreating driver for session: {}", sessionId);
                                    try {
                                        webDriverManager.closeDriver(sessionId);
                                        driver = webDriverManager.createDriver(sessionId);
                                        ntuLoginAutomator.performLogin(driver, username, password);
                                    } catch (Exception driverError) {
                                        logger.error("Failed to recreate driver for session: {}: {}", sessionId, driverError.getMessage());
                                        sessionService.updateOverallSwapStatus(sessionId, Constants.SwapStatus.ERROR, 
                                            "WebDriver error: " + driverError.getMessage());
                                        return;
                                    }
                                }
                            }
                        }
                        
                        // If no swap was successful, update with failure message (matches your FastAPI logic)
                        if (!swapSuccessful) {
                            String failureMsg = "Indexes " + String.join(", ", failedIndexes) + " have no vacancies.";
                            module.setMessage(failureMsg);
                            sessionService.updateModuleStatus(sessionId, idx, module);
                        }
                    }
                }
                
                // Check if all modules are swapped (matches your FastAPI all_swapped check)
                SessionData updatedSession = sessionService.getSecureSession(sessionId);
                boolean allSwapped = updatedSession.getModules().stream().allMatch(ModuleStatus::isSwapped);
                
                if (allSwapped) {
                    sessionService.updateOverallSwapStatus(sessionId, Constants.SwapStatus.COMPLETED, 
                        "All modules have been successfully swapped.");
                    logger.info("All swaps completed for session: {}", sessionId);
                    break;
                }
                
                // Check timeout (matches your FastAPI 2-hour timeout)
                if (System.currentTimeMillis() - startTime >= timeoutMs) {
                    sessionService.updateOverallSwapStatus(sessionId, Constants.SwapStatus.TIMED_OUT, 
                        "Time limit reached before completing the swap.");
                    logger.warn("Swap timed out for session: {}", sessionId);
                    break;
                }
                
                // Wait 5 minutes before next attempt (matches your FastAPI sleep)
                try {
                    logger.debug("Waiting 5 minutes before next swap attempt for session: {}", sessionId);
                    Thread.sleep(5 * 60 * 1000); // 5 minutes
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    logger.info("Swap process interrupted for session: {}", sessionId);
                    break;
                }
            }
            
        } catch (Exception e) {
            logger.error("Error in swap process for session: {}: {}", sessionId, e.getMessage(), e);
            sessionService.updateOverallSwapStatus(sessionId, Constants.SwapStatus.ERROR, 
                "An error occurred: " + e.getMessage());
        } finally {
            // Clean up WebDriver (matches your FastAPI finally block)
            if (driver != null) {
                try {
                    webDriverManager.closeDriver(sessionId);
                    logger.info("WebDriver cleaned up for session: {}", sessionId);
                } catch (Exception e) {
                    logger.warn("Error cleaning up WebDriver for session: {}: {}", sessionId, e.getMessage());
                }
            }
        }
    }
    
    /* Check if swap is currently running for a session */
    public boolean isSwapRunning(String sessionId) {
        try {
            SessionData sessionData = sessionService.getSecureSession(sessionId);
            String status = sessionData.getSwapStatus();
            return Constants.SwapStatus.PROCESSING.equals(status);
        } catch (Exception e) {
            return false;
        }
    }
    
    /* Stop swap process for a session */
    public void stopSwap(String sessionId) {
        try {
            sessionService.updateOverallSwapStatus(sessionId, Constants.SwapStatus.STOPPED, "Swap stopped by user");
            webDriverManager.closeDriver(sessionId);
            logger.info("Swap stopped for session: {}", sessionId);
        } catch (Exception e) {
            logger.warn("Error stopping swap for session: {}: {}", sessionId, e.getMessage());
        }
    }
}