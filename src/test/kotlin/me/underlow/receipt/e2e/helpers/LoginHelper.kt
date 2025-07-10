package me.underlow.receipt.e2e.helpers

import com.codeborne.selenide.Condition
import com.codeborne.selenide.Selenide
import com.codeborne.selenide.Selenide.`$`
import me.underlow.receipt.config.TestSecurityConfiguration

/**
 * Helper class for handling login operations in e2e tests.
 * Provides reliable login functionality with different selector strategies.
 * Uses defensive programming to handle different login form implementations.
 */
class LoginHelper {

    companion object {
        private const val DEFAULT_TIMEOUT = 10000L
        private const val LOGIN_URL = "/login"
        private const val DASHBOARD_URL = "/dashboard"
    }

    /**
     * Performs login with test user (default allowed user 1)
     */
    fun loginAsTestUser() {
        loginAsAllowedUser1()
    }

    /**
     * Performs login with allowed user 1 and navigates to dashboard
     */
    fun loginAsAllowedUser1() {
        performLogin(
            TestSecurityConfiguration.ALLOWED_EMAIL_1,
            TestSecurityConfiguration.TEST_PASSWORD
        )
    }

    /**
     * Performs login with allowed user 2 and navigates to dashboard
     */
    fun loginAsAllowedUser2() {
        performLogin(
            TestSecurityConfiguration.ALLOWED_EMAIL_2,
            TestSecurityConfiguration.TEST_PASSWORD
        )
    }

    /**
     * Performs login with not allowed user (for testing access restrictions)
     */
    fun loginAsNotAllowedUser() {
        performLogin(
            TestSecurityConfiguration.NOT_ALLOWED_EMAIL,
            TestSecurityConfiguration.TEST_PASSWORD
        )
    }

    /**
     * Performs login with custom credentials
     */
    fun performLogin(username: String, password: String) {
        // Navigate to login page
        Selenide.open(LOGIN_URL)
        
        // Wait for login page to load
        waitForLoginPageToLoad()
        
        // Fill login form using multiple selector strategies
        fillUsernameField(username)
        fillPasswordField(password)
        
        // Submit form
        submitLoginForm()
        
        // Wait for login to complete
        waitForLoginToComplete()
    }

    /**
     * Waits for login page to load completely
     */
    private fun waitForLoginPageToLoad() {
        // Wait for the login form to be present
        val loginFormPresent = `$`("form").isDisplayed() ||
                `$`("input[type='email']").exists() ||
                `$`("input[type='text']").exists() ||
                `$`("input[name='username']").exists() ||
                `$`("[data-test-id='username-input']").exists()
        
        if (!loginFormPresent) {
            // Wait a bit more for the page to load
            Thread.sleep(1000)
        }
    }

    /**
     * Fills username field using multiple selector strategies
     */
    private fun fillUsernameField(username: String) {
        val usernameField = when {
            `$`("[data-test-id='username-input']").exists() -> `$`("[data-test-id='username-input']")
            `$`("input[name='username']").exists() -> `$`("input[name='username']")
            `$`("input[type='email']").exists() -> `$`("input[type='email']")
            `$`("input[type='text']").exists() -> `$`("input[type='text']")
            `$`("#username").exists() -> `$`("#username")
            `$`("#email").exists() -> `$`("#email")
            else -> throw RuntimeException("Could not find username input field")
        }
        
        usernameField.shouldBe(Condition.visible).setValue(username)
    }

    /**
     * Fills password field using multiple selector strategies
     */
    private fun fillPasswordField(password: String) {
        val passwordField = when {
            `$`("[data-test-id='password-input']").exists() -> `$`("[data-test-id='password-input']")
            `$`("input[name='password']").exists() -> `$`("input[name='password']")
            `$`("input[type='password']").exists() -> `$`("input[type='password']")
            `$`("#password").exists() -> `$`("#password")
            else -> throw RuntimeException("Could not find password input field")
        }
        
        passwordField.shouldBe(Condition.visible).setValue(password)
    }

    /**
     * Submits login form using multiple selector strategies
     */
    private fun submitLoginForm() {
        val submitButton = when {
            `$`("[data-test-id='login-button']").exists() -> `$`("[data-test-id='login-button']")
            `$`("button[type='submit']").exists() -> `$`("button[type='submit']")
            `$`("input[type='submit']").exists() -> `$`("input[type='submit']")
            `$`("#login-button").exists() -> `$`("#login-button")
            `$`(".login-button").exists() -> `$`(".login-button")
            else -> throw RuntimeException("Could not find login submit button")
        }
        
        submitButton.shouldBe(Condition.visible).click()
    }

    /**
     * Waits for login to complete and verifies successful login
     */
    private fun waitForLoginToComplete() {
        // Wait for redirect from login page
        Thread.sleep(1000)
        
        // Check if we're on dashboard or no longer on login page
        val currentUrl = Selenide.webdriver().`object`().currentUrl
        val isLoggedIn = !currentUrl.contains("/login") || 
                        `$`(".dashboard-layout").exists() ||
                        `$`(".navigation-panel").exists() ||
                        `$`("nav").exists()
        
        if (!isLoggedIn) {
            // Check for login error messages
            val errorMessage = when {
                `$`(".alert-danger").exists() -> `$`(".alert-danger").text()
                `$`(".error-message").exists() -> `$`(".error-message").text()
                `$`("[data-test-id='error-message']").exists() -> `$`("[data-test-id='error-message']").text()
                else -> "Unknown login error"
            }
            throw RuntimeException("Login failed: $errorMessage")
        }
    }

    /**
     * Checks if user is already logged in
     */
    fun isLoggedIn(): Boolean {
        val currentUrl = Selenide.webdriver().`object`().currentUrl
        return !currentUrl.contains("/login") && 
               (`$`(".dashboard-layout").exists() || 
                `$`(".navigation-panel").exists() ||
                `$`("nav").exists())
    }

    /**
     * Performs logout operation
     */
    fun logout() {
        // Look for logout button in various locations
        val logoutButton = when {
            `$`("form[action='/logout'] button[type='submit']").exists() -> 
                `$`("form[action='/logout'] button[type='submit']")
            `$`("button[type='submit']").exists() && 
                `$`("button[type='submit']").text().contains("Logout") -> 
                `$`("button[type='submit']")
            `$`("a[href='/logout']").exists() -> `$`("a[href='/logout']")
            `$`("[data-test-id='logout-button']").exists() -> `$`("[data-test-id='logout-button']")
            else -> null
        }
        
        logoutButton?.click()
        
        // Wait for logout to complete
        Thread.sleep(1000)
    }

    /**
     * Clears browser state for clean test isolation
     */
    fun clearBrowserState() {
        try {
            Selenide.clearBrowserCookies()
            Selenide.clearBrowserLocalStorage()
        } catch (e: Exception) {
            // Ignore errors during cleanup
        }
    }

    /**
     * Waits for page to load completely
     */
    fun waitForPageLoad() {
        // Wait for DOM to be ready
        val readyState = Selenide.executeJavaScript<String>("return document.readyState")
        if (readyState != "complete") {
            Thread.sleep(500)
        }
    }
}