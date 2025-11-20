package com.ntu.adddrop.selenium;

import com.ntu.adddrop.exception.SeleniumException;
import com.ntu.adddrop.util.Constants;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Manages WebDriver instances with proper lifecycle management
 * Handles driver creation, cleanup, and pooling
 */
@Component
public class WebDriverManager {
    private static final Logger logger = LoggerFactory.getLogger(WebDriverManager.class);

    @Autowired
    private ApplicationContext applicationContext;

    // Track active drivers for cleanup
    private final ConcurrentMap<String, WebDriver> activeDrivers = new ConcurrentHashMap<>();

    /* Create a new WebDriver instance for a session */
    public WebDriver createDriver(String sessionId) {
        try {
            logger.info("Creating WebDriver for session: {}", sessionId);

            // Get new WebDriver instance from Spring context (prototype scope)
            WebDriver driver = applicationContext.getBean(WebDriver.class);

            // Track the driver
            activeDrivers.put(sessionId, driver);

            logger.info("WebDriver created successfully for session: {}", sessionId);
            return driver;
        } catch (Exception e) {
            logger.error("Failed to create WebDriver for session: {}", sessionId, e);
            throw new SeleniumException(Constants.ErrorMessages.DRIVER_INIT_FAILED + ": " + e.getMessage(), e);
        }
    }

    /* Get existing WebDriver for a session */
    public WebDriver getDriver(String sessionId) {
        WebDriver driver = activeDrivers.get(sessionId);
        if (driver == null) {
            logger.warn("No WebDriver found for session: {}", sessionId);
            throw new SeleniumException("No WebDriver found for session: " + sessionId);
        }
        return driver;
    }

    /* Check if driver exists for session */
    public boolean hasDriver(String sessionId) {
        return activeDrivers.containsKey(sessionId);
    }

    /* Safely close and cleanup WebDriver for a session */
    public void closeDriver(String sessionId) {
        WebDriver driver = activeDrivers.remove(sessionId);
        if (driver != null) {
            try {
                logger.info("Closing WebDriver for session: {}", sessionId);
                driver.quit();
                logger.info("WebDriver closed successfully for session: {}", sessionId);
            } catch (Exception e) {
                logger.warn("Error closing WebDriver for session: {}", sessionId, e);
            }
        } else {
            logger.warn("No WebDriver to close for session: {}", sessionId);
        }
    }

    /* Close all active drivers (for application shutdown) */
    public void closeAllDrivers() {
        logger.info("Closing all active WebDrivers. Count: {}", activeDrivers.size());
        
        for (String sessionId: activeDrivers.keySet()) {
            closeDriver(sessionId);
        }

        activeDrivers.clear();
        logger.info("All WebDrivers closed");
    }

    /* Get count of active drivers */
    public int getActiveDriverCount() {
        return activeDrivers.size();
    }

    /* Force cleanup of inactive sessions */
    public void cleanupInactiveDrivers() {
        logger.debug("Checking for inactive WebDrivers to cleanup");

        activeDrivers.entrySet().removeIf(entry -> {
            String sessionId = entry.getKey();
            WebDriver driver = entry.getValue();

            try {
                // Test if driver is still responsive
                driver.getTitle();
                return false; // Keep active driver
            } catch (Exception e) {
                logger.warn("Removing inactive WebDriver for session: {}", sessionId);
                try {
                    driver.quit();
                } catch (Exception closeEx) {
                    logger.debug("Error closing inactive driver: {}", closeEx.getMessage());
                }
                return true; // Remove inactive driver
            }
        });
    }
}
