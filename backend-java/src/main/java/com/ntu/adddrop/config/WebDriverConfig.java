package com.ntu.adddrop.config;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import java.time.Duration;

@Configuration
public class WebDriverConfig {
    
    @Value("${app.selenium.chrome.binary-path}")
    private String chromeBinaryPath;

    @Value("${app.selenium.chrome.driver-path}")
    private String chromeDriverPath;

    @Value("${app.selenium.options.headless:false}")
    private boolean headless;

    @Value("${app.selenium.options.window-size:1920,1080}")
    private String windowSize;

    @Value("${app.selenium.timeouts.page-load:30}")
    private int pageLoadTimeout;

    @Value("${app.selenium.timeouts.implicit-wait:10}")
    private int implicitWaitTimeout;

    /* Configure Chrome options for Selenium WebDriver */
    @Bean
    public ChromeOptions chromeOptions() {
        ChromeOptions options = new ChromeOptions();

        // Basic Chrome options
        options.setBinary(chromeBinaryPath);
        options.addArguments("--window-size=" + windowSize);
        options.addArguments("--disable-gpu");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--disable-extensions");
        options.addArguments("--disable-background-timer-throttling");
        options.addArguments("--disable-backgrounding-occluded-windows");
        options.addArguments("--disable-renderer-background-throttling");

        // Headless mode for production
        if (headless) {
            options.addArguments("--headless");
        }
        
        // Performance optimizations
        options.addArguments("--disable-images");
        options.addArguments("--disable-javascript");
        options.addArguments("--disable-plugins");
        options.addArguments("--disable-popup-blocking");
        
        return options;
    }

    /**
     * Create WebDriver bean with proper configuration
     * Scope: prototype - creates new instance each time
     */
    @Bean
    @Scope("prototype")
    public WebDriver webDriver() {
        // Set ChromeDriver path
        System.setProperty("webdriver.chrome.driver", chromeDriverPath);
        
        ChromeDriver driver = new ChromeDriver(chromeOptions());
        
        // Set timeouts
        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(pageLoadTimeout));
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(implicitWaitTimeout));
        
        return driver;
    }
}
