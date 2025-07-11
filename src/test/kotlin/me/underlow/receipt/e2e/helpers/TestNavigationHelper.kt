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
            `$`("a[href='#services']").exists() -> `$`("a[href='#services']")
            `$`("[data-test-id='services-tab']").exists() -> `$`("[data-test-id='services-tab']")
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
            `$`("#services").exists() -> `$`("#services")
            `$`("#services-content").exists() -> `$`("#services-content")
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
            `$`("#serviceProviderList").exists() -> `$`("#serviceProviderList")
            `$`(".service-provider-list").exists() -> `$`(".service-provider-list")
            `$`(".split-panel-left").exists() -> `$`(".split-panel-left")
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
        // Trigger data loading using the JavaScript function from dashboard.html
        Selenide.executeJavaScript<Unit>("if (typeof loadServicesData === 'function') loadServicesData();")
        
        waitForDataLoad()
    }

    /**
     * Triggers services data refresh and waits for completion
     */
    fun refreshServicesData() {
        // Trigger data loading using the JavaScript function from dashboard.html
        Selenide.executeJavaScript<Unit>("if (typeof loadServicesData === 'function') loadServicesData();")
        
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

    /**
     * Selects a service provider from the list by ID
     */
    fun selectServiceProvider(providerId: String) {
        // Wait for services data to load first
        waitForDataLoad()
        
        // Click on the service provider item in the list
        val providerItem = when {
            `$`("[onclick='selectServiceProvider($providerId)']").exists() -> `$`("[onclick='selectServiceProvider($providerId)']")
            `$`(".service-provider-item").exists() -> {
                `$`(".service-provider-item").`$$`("li").find { 
                    it.getAttribute("onclick")?.contains(providerId) == true
                }
            }
            else -> {
                // If not found, trigger JavaScript directly
                Selenide.executeJavaScript<Unit>("selectServiceProvider($providerId);")
                null
            }
        }
        
        providerItem?.shouldBe(Condition.visible)?.click()
        
        // Wait for the form to load
        Thread.sleep(500)
    }

    /**
     * Clicks on the avatar preview to open the upload modal
     */
    fun clickOnAvatarToOpenUpload() {
        // Wait for the form to be ready
        val avatarPreview = when {
            `$`("#avatarPreview").exists() -> `$`("#avatarPreview")
            `$`(".avatar-preview").exists() -> `$`(".avatar-preview")
            `$`(".avatar-preview-fallback").exists() -> `$`(".avatar-preview-fallback")
            `$`("[onclick*='uploadAvatar']").exists() -> `$`("[onclick*='uploadAvatar']")
            else -> throw RuntimeException("Could not find avatar preview element to click")
        }
        
        avatarPreview.shouldBe(Condition.visible).click()
        
        // Small wait for modal to open
        Thread.sleep(200)
    }
}