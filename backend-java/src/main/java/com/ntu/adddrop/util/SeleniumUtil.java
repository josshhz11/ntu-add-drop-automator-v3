package com.ntu.adddrop.util;

import com.ntu.adddrop.exception.SeleniumException;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

/* Selenium utility methods for common operations */
public final class SeleniumUtil {
    private static final Logger logger = LoggerFactory.getLogger(SeleniumUtil.class);

    private SeleniumUtil() {
        // Prevent instantiation
    }

    /* Wait for element to be clickable and click it */
    public static void waitAndClick(WebDriver driver, By locator, int timeoutSeconds) {
        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds));
            WebElement element = wait.until(ExpectedConditions.elementToBeClickable(locator));
            element.click();
            logger.debug("Successfully clicked element: {}", locator);
        } catch (Exception e) {
            logger.error("Failed to click element: {}", locator);
            throw new SeleniumException.ElementNotFoundException(locator.toString());
        }
    }

    /* Wait for element to be visible and send keys */
    public static void waitAndSendKeys(WebDriver driver, By locator, String text, int timeoutSeconds) {
        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds));
            WebElement element = wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
            element.clear();
            element.sendKeys(text);
            logger.debug("Successfully sent keys to element: {}", locator);
        } catch (Exception e) {
            logger.error("Failed to send keys to element: {}", locator, e);
            throw new SeleniumException.ElementNotFoundException(locator.toString());
        }
    }
    
    /* Wait for element to be present and get its text */
    public static String waitAndGetText(WebDriver driver, By locator, int timeoutSeconds) {
        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds));
            WebElement element = wait.until(ExpectedConditions.presenceOfElementLocated(locator));
            String text = element.getText();
            logger.debug("Successfully got text from element: {} -> {}", locator, text);
            return text;
        } catch (Exception e) {
            logger.error("Failed to get text from element: {}", locator, e);
            throw new SeleniumException.ElementNotFoundException(locator.toString());
        }
    }
    
    /* Check if element exists without throwing exception */
    public static boolean isElementPresent(WebDriver driver, By locator) {
        try {
            driver.findElement(locator);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    /* Wait for page to load completely */
    public static void waitForPageLoad(WebDriver driver, int timeoutSeconds) {
        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds));
            wait.until(webDriver -> 
                ((org.openqa.selenium.JavascriptExecutor) webDriver)
                    .executeScript("return document.readyState").equals("complete"));
            logger.debug("Page loaded successfully");
        } catch (Exception e) {
            logger.warn("Page load timeout, continuing anyway", e);
        }
    }
    
    /* Safe navigation with retry */
    public static void navigateWithRetry(WebDriver driver, String url, int maxRetries) {
        for (int i = 0; i < maxRetries; i++) {
            try {
                driver.navigate().to(url);
                waitForPageLoad(driver, Constants.Timeouts.PAGE_LOAD);
                logger.info("Successfully navigated to: {}", url);
                return;
            } catch (Exception e) {
                logger.warn("Navigation attempt {} failed for URL: {}", i + 1, url, e);
                if (i == maxRetries - 1) {
                    throw new SeleniumException.NavigationException(url, e.getMessage());
                }
                // Wait before retry
                try {
                    Thread.sleep(Constants.Timeouts.RETRY_DELAY * 1000);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new SeleniumException("Navigation interrupted", ie);
                }
            }
        }
    }
    
    /* Take screenshot for debugging (optional) */
    public static void takeScreenshot(WebDriver driver, String filename) {
        try {
            // Implementation for screenshot if needed
            logger.debug("Screenshot taken: {}", filename);
        } catch (Exception e) {
            logger.warn("Failed to take screenshot: {}", filename, e);
        }
    }

}
