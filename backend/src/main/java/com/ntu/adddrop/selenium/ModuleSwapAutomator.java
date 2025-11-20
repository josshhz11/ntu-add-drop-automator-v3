package com.ntu.adddrop.selenium;

import com.ntu.adddrop.model.SessionData.ModuleStatus;
import com.ntu.adddrop.util.Constants;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;

/* Handles NTU course swap automation - implements FastAPI attempt_swap logic */
@Component
public class ModuleSwapAutomator {
    
    private static final Logger logger = LoggerFactory.getLogger(ModuleSwapAutomator.class);
    
    /**
     * Attempt to swap a module - follows FastAPI attempt_swap function
     * @param driver WebDriver instance
     * @param sessionId Session identifier for error tracking
     * @param oldIndex Current module index
     * @param newIndex Desired new module index
     * @return SwapResult with success status and message
     */
    public SwapResult attemptSwap(WebDriver driver, String sessionId, String oldIndex, String newIndex) {
        try {
            logger.info("Attempting swap for session: {} - {} -> {}", sessionId, oldIndex, newIndex);
            
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(Constants.Timeouts.ELEMENT_WAIT));
            
            // Step 1: Wait for course table to be present
            wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(Constants.Selectors.COURSE_TABLE)));
            
            // Step 2: Find and click the radio button for old_index
            String radioButtonXpath = String.format(Constants.Selectors.RADIO_BUTTON_TEMPLATE, oldIndex);
            try {
                wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(radioButtonXpath)));
                WebElement radioButton = driver.findElement(By.xpath(radioButtonXpath));
                radioButton.click();
                logger.debug("Selected radio button for old index: {}", oldIndex);
            } catch (Exception e) {
                String errorMsg = "Old index " + oldIndex + " not found. Swap cannot proceed.";
                logger.error(errorMsg);
                return new SwapResult(false, errorMsg);
            }
            
            // Step 3: Select "Change Index" from dropdown
            Select dropdown = new Select(driver.findElement(By.name(Constants.Selectors.DROPDOWN_OPTIONS)));
            dropdown.selectByValue("C");
            logger.debug("Selected 'Change Index' option");
            
            // Step 4: Hide header and click Go button
            try {
                WebElement header = driver.findElement(By.className(Constants.Selectors.HEADER_HIDE));
                ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("arguments[0].style.visibility = 'hidden';", header);
            } catch (Exception e) {
                // Header hiding is optional, continue if it fails
                logger.debug("Could not hide header, continuing...");
            }
            
            WebElement goButton = driver.findElement(By.xpath(Constants.Selectors.GO_BUTTON));
            goButton.click();
            logger.debug("Clicked Go button");
            
            // Step 5: Check for portal closed alert
            if (checkForPortalClosedAlert(driver)) {
                return new SwapResult(false, Constants.ErrorMessages.PORTAL_CLOSED);
            }
            
            // Step 6: Wait for swap page to load
            wait.until(ExpectedConditions.presenceOfElementLocated(By.name(Constants.Selectors.SWAP_PAGE_INDICATOR)));
            logger.debug("Swap page loaded successfully");
            
            // Step 7: Check if new index exists and has vacancies
            SwapResult vacancyCheck = checkNewIndexVacancy(driver, newIndex);
            if (!vacancyCheck.isSuccess()) {
                // Click back to timetable before returning
                clickBackToTimetable(driver);
                return vacancyCheck;
            }
            
            // Step 8: Click OK to proceed with swap
            WebElement okButton = driver.findElement(By.xpath(Constants.Selectors.OK_BUTTON));
            okButton.click();
            logger.debug("Clicked OK button to proceed with swap");
            
            // Step 9: Check for module clash alert
            if (checkForModuleClashAlert(driver)) {
                clickBackToTimetable(driver);
                return new SwapResult(false, "Module clash detected with existing modules");
            }
            
            // Step 10: Wait for confirm swap page
            wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(Constants.Selectors.CONFIRM_FORM)));
            logger.debug("Confirm swap page loaded");
            
            // Step 11: Click confirm swap button
            WebElement confirmButton = driver.findElement(By.xpath(Constants.Selectors.CONFIRM_SWAP_BUTTON));
            confirmButton.click();
            logger.debug("Clicked confirm swap button");
            
            // Step 12: Wait for success alert and accept it
            wait.until(ExpectedConditions.alertIsPresent());
            Alert alert = driver.switchTo().alert();
            String alertText = alert.getText();
            logger.info("Swap success alert: {}", alertText);
            alert.accept();
            
            logger.info("Successfully swapped {} -> {} for session: {}", oldIndex, newIndex, sessionId);
            return new SwapResult(true, "Successfully swapped " + oldIndex + " → " + newIndex);
            
        } catch (Exception e) {
            logger.error("Swap attempt failed for {} -> {}: {}", oldIndex, newIndex, e.getMessage(), e);
            return new SwapResult(false, "Error during swap attempt: " + e.getMessage());
        }
    }
    
    /* Process a module with multiple new index options */
    public ModuleStatus performModuleSwap(WebDriver driver, String sessionId, ModuleStatus moduleStatus) {
        logger.info("Processing module swap for session: {} - Old Index: {}", sessionId, moduleStatus.getOldIndex());
        
        List<String> failedIndexes = new java.util.ArrayList<>();
        
        // Try each new index in order of preference
        for (String newIndex : moduleStatus.getNewIndexes()) {
            logger.info("Attempting to swap to index: {}", newIndex);
            
            SwapResult result = attemptSwap(driver, sessionId, moduleStatus.getOldIndex(), newIndex);
            
            if (result.isSuccess()) {
                // Success! Update module status and return
                moduleStatus.setSwapped(true);
                moduleStatus.setMessage(result.getMessage());
                return moduleStatus;
            } else {
                // Failed, add to failed list and try next
                failedIndexes.add(newIndex);
                logger.warn("Failed to swap to {}: {}", newIndex, result.getMessage());
            }
        }
        
        // All indexes failed
        String failureMessage = "Indexes " + String.join(", ", failedIndexes) + " have no vacancies.";
        moduleStatus.setMessage(failureMessage);
        return moduleStatus;
    }
    
    /* Check for portal closed alert */
    private boolean checkForPortalClosedAlert(WebDriver driver) {
        try {
            WebDriverWait shortWait = new WebDriverWait(driver, Duration.ofSeconds(Constants.Timeouts.ALERT_WAIT));
            shortWait.until(ExpectedConditions.alertIsPresent());
            
            Alert alert = driver.switchTo().alert();
            String alertText = alert.getText();
            alert.accept();
            
            logger.warn("Portal closed alert detected: {}", alertText);
            return true;
            
        } catch (Exception e) {
            // No alert found, continue
            return false;
        }
    }
    
    /* Check if new index has vacancies */
    private SwapResult checkNewIndexVacancy(WebDriver driver, String newIndex) {
        try {
            WebElement dropdownElement = driver.findElement(By.name(Constants.Selectors.NEW_INDEX_DROPDOWN));
            List<WebElement> options = dropdownElement.findElements(By.xpath(".//option[@value='" + newIndex + "']"));
            
            if (options.isEmpty()) {
                String errorMsg = "New Index " + newIndex + " was not found in the dropdown options. Swap cannot proceed.";
                logger.warn(errorMsg);
                return new SwapResult(false, errorMsg);
            }
            
            // Parse vacancies from option text (e.g., "01172 / 9 / 1")
            String optionText = options.get(0).getText();
            try {
                String[] parts = optionText.split(" / ");
                if (parts.length >= 2) {
                    int vacancies = Integer.parseInt(parts[1].trim());
                    logger.debug("Vacancies for index {}: {}", newIndex, vacancies);
                    
                    if (vacancies <= 0) {
                        String errorMsg = "Index " + newIndex + " has no vacancies. Swap cannot proceed.";
                        logger.warn(errorMsg);
                        return new SwapResult(false, errorMsg);
                    }
                } else {
                    logger.warn("Could not parse vacancy information from: {}", optionText);
                }
            } catch (NumberFormatException e) {
                logger.warn("Failed to parse vacancies for index {}: {}", newIndex, e.getMessage());
            }
            
            // Select the new index
            Select selectDropdown = new Select(dropdownElement);
            selectDropdown.selectByValue(newIndex);
            logger.debug("Selected new index: {}", newIndex);
            
            return new SwapResult(true, "Index available");
            
        } catch (Exception e) {
            String errorMsg = "Unexpected error while checking new index " + newIndex + ": " + e.getMessage();
            logger.error(errorMsg, e);
            return new SwapResult(false, errorMsg);
        }
    }
    
    /* Check for module clash alert */
    private boolean checkForModuleClashAlert(WebDriver driver) {
        try {
            WebDriverWait shortWait = new WebDriverWait(driver, Duration.ofSeconds(Constants.Timeouts.ALERT_WAIT));
            shortWait.until(ExpectedConditions.alertIsPresent());
            
            Alert alert = driver.switchTo().alert();
            String alertText = alert.getText();
            alert.accept();
            
            logger.warn("Module clash alert detected: {}", alertText);
            return true;
            
        } catch (Exception e) {
            // No alert found, continue
            return false;
        }
    }
    
    /* Click back to timetable button */
    private void clickBackToTimetable(WebDriver driver) {
        try {
            WebElement backButton = driver.findElement(By.xpath(Constants.Selectors.BACK_TO_TIMETABLE));
            backButton.click();
            logger.debug("Clicked back to timetable");
        } catch (Exception e) {
            logger.warn("Could not click back to timetable: {}", e.getMessage());
        }
    }
    
    /* Result class for swap operations */
    public static class SwapResult {
        private final boolean success;
        private final String message;
        
        public SwapResult(boolean success, String message) {
            this.success = success;
            this.message = message;
        }
        
        public boolean isSuccess() {
            return success;
        }
        
        public String getMessage() {
            return message;
        }
    }
}