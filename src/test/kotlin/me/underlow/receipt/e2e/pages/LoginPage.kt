package me.underlow.receipt.e2e.pages

import com.codeborne.selenide.Condition
import com.codeborne.selenide.Selenide
import com.codeborne.selenide.Selenide.`$`

/**
 * Page Object for login page interactions.
 * Encapsulates login form elements and actions using reliable selectors.
 */
class LoginPage {
    
    // Page elements with fallback selector strategies
    private val usernameInput get() = when {
        `$`("[data-test-id='username-input']").exists() -> `$`("[data-test-id='username-input']")
        `$`("input[name='username']").exists() -> `$`("input[name='username']")
        `$`("input[type='email']").exists() -> `$`("input[type='email']")
        else -> `$`("input[type='text']")
    }
    
    private val passwordInput get() = when {
        `$`("[data-test-id='password-input']").exists() -> `$`("[data-test-id='password-input']")
        `$`("input[name='password']").exists() -> `$`("input[name='password']")
        else -> `$`("input[type='password']")
    }
    
    private val loginButton get() = when {
        `$`("[data-test-id='login-button']").exists() -> `$`("[data-test-id='login-button']")
        `$`("button[type='submit']").exists() -> `$`("button[type='submit']")
        else -> `$`("input[type='submit']")
    }
    
    private val errorMessage get() = when {
        `$`("[data-test-id='error-message']").exists() -> `$`("[data-test-id='error-message']")
        `$`(".alert-danger").exists() -> `$`(".alert-danger")
        else -> `$`(".error-message")
    }
    
    /**
     * Opens login page and waits for it to load
     */
    fun open(): LoginPage {
        Selenide.open("/login")
        waitForPageLoad()
        return this
    }
    
    /**
     * Enters username into the username field
     */
    fun enterUsername(username: String): LoginPage {
        usernameInput.shouldBe(Condition.visible).clear()
        usernameInput.setValue(username)
        return this
    }
    
    /**
     * Enters password into the password field
     */
    fun enterPassword(password: String): LoginPage {
        passwordInput.shouldBe(Condition.visible).clear()
        passwordInput.setValue(password)
        return this
    }
    
    /**
     * Clicks the login button
     */
    fun clickLogin(): LoginPage {
        loginButton.shouldBe(Condition.visible).click()
        return this
    }
    
    /**
     * Performs complete login flow with given credentials
     */
    fun loginWith(username: String, password: String): LoginPage {
        enterUsername(username)
        enterPassword(password)
        clickLogin()
        return this
    }
    
    /**
     * Verifies login form is visible and ready for input
     */
    fun shouldBeDisplayed(): LoginPage {
        usernameInput.shouldBe(Condition.visible)
        passwordInput.shouldBe(Condition.visible)
        loginButton.shouldBe(Condition.visible)
        return this
    }
    
    /**
     * Verifies error message is displayed with specific text
     */
    fun shouldShowErrorMessage(): LoginPage {
        errorMessage.shouldBe(Condition.visible)
        return this
    }
    
    /**
     * Verifies error message contains specific text
     */
    fun shouldShowErrorMessage(expectedText: String): LoginPage {
        errorMessage.shouldBe(Condition.visible)
        errorMessage.shouldHave(Condition.text(expectedText))
        return this
    }
    
    /**
     * Verifies user is still on login page (login failed)
     */
    fun shouldStillBeOnLoginPage(): LoginPage {
        usernameInput.shouldBe(Condition.visible)
        passwordInput.shouldBe(Condition.visible)
        return this
    }
    
    /**
     * Verifies page URL contains login path
     */
    fun shouldBeOnLoginUrl(): LoginPage {
        Selenide.webdriver().`object`().currentUrl.let { url ->
            assert(url.contains("/login")) { "Expected to be on login page, but was on: $url" }
        }
        return this
    }
    
    /**
     * Waits for login page to load completely
     */
    private fun waitForPageLoad() {
        // Wait for login form to be present
        usernameInput.shouldBe(Condition.visible)
        passwordInput.shouldBe(Condition.visible)
        loginButton.shouldBe(Condition.visible)
    }
}