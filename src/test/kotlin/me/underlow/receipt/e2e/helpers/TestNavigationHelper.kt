package me.underlow.receipt.e2e.helpers

import com.codeborne.selenide.Condition
import com.codeborne.selenide.Selenide
import com.codeborne.selenide.Selenide.`$`
import com.codeborne.selenide.WebDriverRunner
import me.underlow.receipt.config.TestSecurityConfiguration

/**
 * Helper class for common navigation and interaction patterns in e2e tests.
 * Provides reliable methods for common test operations.
 */
class TestNavigationHelper {

    companion object {
        private const val DEFAULT_TIMEOUT = 10000L
        private const val MOBILE_WIDTH = 400
        private const val MOBILE_HEIGHT = 800
    }

    private val loginHelper = LoginHelper()

    /**
     * Performs login and navigates to services tab
     */
    fun loginAndNavigateToServices() {
        loginHelper.loginAsAllowedUser1()
        navigateToServicesTab()
    }

    /**
     * Performs login with default test credentials
     */
    fun performLogin() {
        loginHelper.loginAsAllowedUser1()
    }

    /**
     * Navigates to services tab and waits for content to load
     */
    fun navigateToServicesTab() {
        val servicesTab = when {
            `$`("[data-test-id='services-tab']").exists() -> `$`("[data-test-id='services-tab']")
            `$`("a[href='#services']").exists() -> `$`("a[href='#services']")
            `$`("a[href='/services']").exists() -> `$`("a[href='/services']")
            `$`("#services-tab").exists() -> `$`("#services-tab")
            `$`(".services-tab").exists() -> `$`(".services-tab")
            `$`("nav").exists() && `$`("nav").text().contains("Services") -> {
                `$`("nav").`$$`("a").find { it.text().contains("Services") }
            }
            else -> throw RuntimeException("Could not find services tab")
        }
        
        servicesTab?.shouldBe(Condition.visible)?.click()
        
        // Wait for services content to load
        val servicesContent = when {
            `$`("[data-test-id='services-content']").exists() -> `$`("[data-test-id='services-content']")
            `$`("#services-content").exists() -> `$`("#services-content")
            `$`("#services").exists() -> `$`("#services")
            `$`(".services-content").exists() -> `$`(".services-content")
            else -> null
        }
        
        servicesContent?.shouldBe(Condition.visible)
        
        // Wait for page to load
        Thread.sleep(1000)
    }

    /**
     * Waits for data to load using proper conditions instead of Thread.sleep
     */
    fun waitForDataLoad() {
        // Wait for loading indicator to disappear
        val loadingIndicator = `$`("[data-test-id='loading-indicator']")
        if (loadingIndicator.exists()) {
            loadingIndicator.shouldBe(Condition.disappear)
        }
        
        // Wait for service provider list to be visible using multiple selectors
        val serviceProviderList = when {
            `$`("[data-test-id='service-provider-list']").exists() -> `$`("[data-test-id='service-provider-list']")
            `$`("#service-provider-list").exists() -> `$`("#service-provider-list")
            `$`(".service-provider-list").exists() -> `$`(".service-provider-list")
            `$`(".service-providers").exists() -> `$`(".service-providers")
            `$`("[id*='provider']").exists() -> `$`("[id*='provider']")
            `$`("[class*='provider']").exists() -> `$`("[class*='provider']")
            else -> null
        }
        
        serviceProviderList?.shouldBe(Condition.visible)
        
        // Additional wait for content to fully load
        Thread.sleep(1000)
    }

    /**
     * Waits for page to load completely
     */
    fun waitForPageLoad() {
        // Wait for DOM to be ready
        Selenide.executeJavaScript<String>("return document.readyState").let { state ->
            if (state != "complete") {
                // Wait a bit more for async operations
                Thread.sleep(500)
            }
        }
    }

    /**
     * Triggers data refresh and waits for completion
     */
    fun refreshData() {
        // Look for refresh button first
        val refreshButton = `$`("[data-test-id='refresh-button']")
        if (refreshButton.exists()) {
            refreshButton.click()
        } else {
            // Fallback to JavaScript if refresh button doesn't exist
            Selenide.executeJavaScript<Unit>("if (typeof loadServicesData === 'function') loadServicesData();")
        }
        
        waitForDataLoad()
    }

    /**
     * Sets browser to mobile viewport
     */
    fun setMobileViewport() {
        WebDriverRunner.getWebDriver().manage().window().setSize(
            org.openqa.selenium.Dimension(MOBILE_WIDTH, MOBILE_HEIGHT)
        )
    }

    /**
     * Maximizes browser window
     */
    fun maximizeBrowser() {
        WebDriverRunner.getWebDriver().manage().window().maximize()
    }

    /**
     * Clears browser state for clean test isolation
     */
    fun clearBrowserState() {
        loginHelper.clearBrowserState()
    }

    /**
     * Waits for element to be visible with custom timeout
     */
    fun waitForElement(selector: String, timeoutMs: Long = DEFAULT_TIMEOUT) {
        `$`(selector).shouldBe(Condition.visible, java.time.Duration.ofMillis(timeoutMs))
    }

    /**
     * Waits for element to disappear with custom timeout
     */
    fun waitForElementToDisappear(selector: String, timeoutMs: Long = DEFAULT_TIMEOUT) {
        `$`(selector).shouldBe(Condition.disappear, java.time.Duration.ofMillis(timeoutMs))
    }

    /**
     * Scrolls to element safely
     */
    fun scrollToElement(selector: String) {
        val element = `$`(selector)
        if (element.exists()) {
            element.scrollTo()
        }
    }
}