package com.ntu.adddrop.selenium;

import com.ntu.adddrop.exception.SeleniumException;
import com.ntu.adddrop.util.Constants;
import com.ntu.adddrop.util.SeleniumUtil;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * Handles NTU login automation - follows FastAPI login_to_portal implementation
 */
@Component
public class NTULoginAutomator {
    
    private static final Logger logger = LoggerFactory.getLogger(NTULoginAutomator.class);
    
    /**
     * Perform login to NTU system - follows FastAPI login_to_portal logic
     * @param driver WebDriver instance
     * @param username NTU username
     * @param password NTU password
     * @return true if login successful
     */
    public boolean performLogin(WebDriver driver, String username, String password) {
        try {
            logger.info("Starting NTU login process for user: {}", username);
            
            // Step 1: Navigate to login page
            logger.debug("Navigating to NTU login page");
            driver.get(Constants.URLs.NTU_LOGIN);
            
            // Step 2: Enter username and click OK
            logger.debug("Entering username");
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(Constants.Timeouts.ELEMENT_WAIT));
            
            wait.until(ExpectedConditions.presenceOfElementLocated(By.id(Constants.Selectors.USERNAME_INPUT)));
            driver.findElement(By.id(Constants.Selectors.USERNAME_INPUT)).sendKeys(username);
            
            driver.findElement(By.xpath(Constants.Selectors.LOGIN_BUTTON)).click();
            
            // Step 3: Wait for password field and enter password
            logger.debug("Entering password");
            wait.until(ExpectedConditions.presenceOfElementLocated(By.id(Constants.Selectors.PASSWORD_INPUT)));
            driver.findElement(By.id(Constants.Selectors.PASSWORD_INPUT)).sendKeys(password);
            
            driver.findElement(By.xpath(Constants.Selectors.LOGIN_BUTTON)).click();
            
            // Step 4: Wait for redirect and check which page we land on
            logger.debug("Waiting for login redirect");
            wait.until(ExpectedConditions.or(
                ExpectedConditions.urlToBe(Constants.URLs.COURSE_PLANNER),
                ExpectedConditions.urlToBe(Constants.URLs.COURSE_TIMETABLE)
            ));
            
            String currentUrl = driver.getCurrentUrl();
            logger.debug("Login redirected to: {}", currentUrl);
            
            // Step 5: Handle different redirect scenarios
            if (Constants.URLs.COURSE_TIMETABLE.equals(currentUrl)) {
                // If redirected to timetable, click "Plan/ Registration" button
                logger.debug("On timetable page, looking for Plan/Registration button");
                try {
                    wait.until(ExpectedConditions.elementToBeClickable(By.xpath(Constants.Selectors.PLAN_REGISTRATION_BUTTON)));
                    driver.findElement(By.xpath(Constants.Selectors.PLAN_REGISTRATION_BUTTON)).click();
                    logger.debug("Clicked Plan/Registration button");
                } catch (Exception e) {
                    logger.error("Failed to find Plan/Registration button");
                    throw new SeleniumException.LoginFailedException(Constants.ErrorMessages.PLAN_BUTTON_NOT_FOUND);
                }
            }
            
            // Step 6: Wait for the main course table to appear
            logger.debug("Waiting for course table to load");
            wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(Constants.Selectors.COURSE_TABLE)));
            
            logger.info("NTU login successful for user: {}", username);
            return true;
            
        } catch (Exception e) {
            logger.error("Login failed for user: {} - Error: {}", username, e.getMessage(), e);
            throw new SeleniumException.LoginFailedException(Constants.ErrorMessages.LOGIN_FAILED);
        }
    }
    
    /**
     * Check if user is still logged in by verifying page elements
     * @param driver WebDriver instance
     * @return true if still logged in
     */
    public boolean isLoggedIn(WebDriver driver) {
        try {
            String currentUrl = driver.getCurrentUrl();
            
            // If back at login page, session expired
            if (currentUrl.contains("ldap_login.login")) {
                logger.warn("Session appears to be expired - back at login page");
                return false;
            }
            
            // Check if course table is still present
            return SeleniumUtil.isElementPresent(driver, By.xpath(Constants.Selectors.COURSE_TABLE));
            
        } catch (Exception e) {
            logger.warn("Error checking login status: {}", e.getMessage());
            return false;
        }
    }
}