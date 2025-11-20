package com.ntu.adddrop.util;

/* Application constants */
public final class Constants {
    
    private Constants() {
        // Prevent instantiation
    }
    
    // NTU URLs
    public static final class URLs {
        public static final String NTU_LOGIN = "https://wish.wis.ntu.edu.sg/pls/webexe/ldap_login.login?w_url=https://wish.wis.ntu.edu.sg/pls/webexe/aus_stars_planner.main";
        public static final String COURSE_PLANNER = "https://wish.wis.ntu.edu.sg/pls/webexe/AUS_STARS_PLANNER.planner";
        public static final String COURSE_TIMETABLE = "https://wish.wis.ntu.edu.sg/pls/webexe/AUS_STARS_PLANNER.time_table";
    }
    
    // Selenium selectors - Updated to match actual NTU portal
    public static final class Selectors {
        // Login page
        public static final String USERNAME_INPUT = "UID";  // By.ID
        public static final String PASSWORD_INPUT = "PW";   // By.ID
        public static final String LOGIN_BUTTON = "//input[@value='OK']";  // By.XPATH
        
        // Course planner page
        public static final String PLAN_REGISTRATION_BUTTON = "//input[@value='Plan/ Registration']";
        public static final String COURSE_TABLE = "//table[@bordercolor='#E0E0E0']";
        
        // Module selection and swap
        public static final String RADIO_BUTTON_TEMPLATE = "//input[@type='radio' and @value='%s']";
        public static final String DROPDOWN_OPTIONS = "opt";  // By.NAME
        public static final String GO_BUTTON = "//input[@type='submit' and @value='Go']";
        public static final String HEADER_HIDE = "site-header__body";  // By.CLASS_NAME
        
        // Swap page
        public static final String SWAP_PAGE_INDICATOR = "AUS_STARS_MENU";  // By.NAME
        public static final String NEW_INDEX_DROPDOWN = "new_index_nmbr";  // By.NAME
        public static final String OK_BUTTON = "//input[@type='submit' and @value='OK']";
        public static final String BACK_TO_TIMETABLE = "//input[@type='submit' and @value='Back to Timetable']";
        
        // Confirm swap page
        public static final String CONFIRM_FORM = "//*[@id='top']/div/section[2]/div/div/form[1]";
        public static final String CONFIRM_SWAP_BUTTON = "//input[@type='submit' and @value='Confirm to Change Index Number']";
    }
    
    // Timeouts (in seconds)
    public static final class Timeouts {
        public static final int PAGE_LOAD = 30;
        public static final int ELEMENT_WAIT = 10;
        public static final int LOGIN_WAIT = 15;
        public static final int SWAP_OPERATION = 20;
        public static final int ALERT_WAIT = 5;
        public static final int RETRY_DELAY = 2;
        public static final int MAX_RETRIES = 3;
    }
    
    // Session constants
    public static final class Session {
        public static final long DEFAULT_EXPIRY_HOURS = 2;
        public static final String SESSION_KEY_PREFIX = "ntu_session:";
        public static final String SWAP_LOCK_PREFIX = "swap_lock:";
    }
    
    // Swap statuses
    public static final class SwapStatus {
        public static final String IDLE = "Idle";
        public static final String PROCESSING = "Processing";
        public static final String COMPLETED = "Completed";
        public static final String ERROR = "Error";
        public static final String STOPPED = "Stopped";
        public static final String TIMED_OUT = "Timed Out";
    }
    
    // Error messages
    public static final class ErrorMessages {
        public static final String LOGIN_FAILED = "Incorrect username/password. Please try again.";
        public static final String NAVIGATION_FAILED = "Failed to navigate to course registration page";
        public static final String MODULE_NOT_FOUND = "Module with specified index not found";
        public static final String SWAP_TIMEOUT = "Swap operation timed out";
        public static final String SESSION_EXPIRED = "Session has expired";
        public static final String DRIVER_INIT_FAILED = "Failed to initialize WebDriver";
        public static final String PORTAL_CLOSED = "Portal is closed now. Please try again from 10:30am - 10:00pm.";
        public static final String PLAN_BUTTON_NOT_FOUND = "Unable to find or click the 'Plan/ Registration' button.";
    }
}