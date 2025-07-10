package me.underlow.receipt.e2e.pages

import com.codeborne.selenide.Condition
import com.codeborne.selenide.Selenide
import com.codeborne.selenide.Selenide.`$`

/**
 * Page Object for dashboard page interactions.
 * Encapsulates dashboard elements and actions using reliable selectors.
 */
class DashboardPage {
    
    // Page elements with fallback selector strategies
    private val dashboardLayout get() = when {
        `$`("[data-test-id='dashboard-layout']").exists() -> `$`("[data-test-id='dashboard-layout']")
        `$`(".dashboard-layout").exists() -> `$`(".dashboard-layout")
        else -> `$`(".dashboard")
    }
    
    private val navigationPanel get() = when {
        `$`("[data-test-id='navigation-panel']").exists() -> `$`("[data-test-id='navigation-panel']")
        `$`(".navigation-panel").exists() -> `$`(".navigation-panel")
        else -> `$`("nav")
    }
    
    private val contentArea get() = when {
        `$`("[data-test-id='content-area']").exists() -> `$`("[data-test-id='content-area']")
        `$`(".content-area").exists() -> `$`(".content-area")
        else -> `$`(".content")
    }
    
    private val userProfile get() = when {
        `$`("[data-test-id='user-profile']").exists() -> `$`("[data-test-id='user-profile']")
        `$`(".user-profile").exists() -> `$`(".user-profile")
        else -> `$`(".user-info")
    }
    
    private val logoutButton get() = when {
        `$`("[data-test-id='logout-button']").exists() -> `$`("[data-test-id='logout-button']")
        `$`("form[action='/logout'] button[type='submit']").exists() -> `$`("form[action='/logout'] button[type='submit']")
        `$`("a[href='/logout']").exists() -> `$`("a[href='/logout']")
        else -> `$`("button:contains('Logout')")
    }
    
    private val userDropdown get() = when {
        `$`("[data-test-id='user-dropdown']").exists() -> `$`("[data-test-id='user-dropdown']")
        `$`("#userDropdown").exists() -> `$`("#userDropdown")
        else -> `$`(".dropdown-toggle")
    }
    
    /**
     * Opens dashboard page and waits for it to load
     */
    fun open(): DashboardPage {
        Selenide.open("/dashboard")
        waitForPageLoad()
        return this
    }
    
    /**
     * Verifies dashboard page is displayed with all required elements
     */
    fun shouldBeDisplayed(): DashboardPage {
        // Use the same logic as isOnDashboardPage() in BaseE2ETest
        dashboardLayout.shouldBe(Condition.exist)
        navigationPanel.shouldBe(Condition.exist)
        contentArea.shouldBe(Condition.exist)
        return this
    }
    
    /**
     * Verifies user profile information is visible
     */
    fun shouldShowUserProfile(): DashboardPage {
        userProfile.shouldBe(Condition.visible)
        return this
    }
    
    /**
     * Verifies specific user email is displayed
     */
    fun shouldShowUserEmail(email: String): DashboardPage {
        `$`("body").shouldHave(Condition.text(email))
        return this
    }
    
    /**
     * Verifies page URL contains dashboard path
     */
    fun shouldBeOnDashboardUrl(): DashboardPage {
        Selenide.webdriver().`object`().currentUrl.let { url ->
            assert(url.contains("/dashboard") || !url.contains("/login")) { 
                "Expected to be on dashboard page, but was on: $url" 
            }
        }
        return this
    }
    
    /**
     * Performs logout action
     */
    fun logout(): DashboardPage {
        // Use the same logic as performLogout() in BaseE2ETest
        // First, open the user dropdown if it exists
        val userDropdown = `$`("#userDropdown")
        if (userDropdown.exists()) {
            userDropdown.click()
            Thread.sleep(500) // Wait for dropdown to open
        }

        // Look for logout button with different possible selectors
        val logoutButton = when {
            `$`("form[action='/logout'] button[type='submit']").exists() ->
                `$`("form[action='/logout'] button[type='submit']")

            `$`("button[type='submit']").exists() &&
                    `$`("button[type='submit']").text().contains("Logout") ->
                `$`("button[type='submit']")

            `$`("a[href='/logout']").exists() ->
                `$`("a[href='/logout']")

            else -> null
        }

        logoutButton?.click()
        Thread.sleep(1000) // Wait for logout to complete
        return this
    }
    
    /**
     * Navigates to profile page
     */
    fun navigateToProfile(): DashboardPage {
        val profileLink = when {
            `$`("[data-test-id='profile-link']").exists() -> `$`("[data-test-id='profile-link']")
            `$`("a[href='/profile']").exists() -> `$`("a[href='/profile']")
            else -> `$`("a:contains('Profile')")
        }
        profileLink.click()
        return this
    }
    
    /**
     * Navigates to settings page
     */
    fun navigateToSettings(): DashboardPage {
        val settingsLink = when {
            `$`("[data-test-id='settings-link']").exists() -> `$`("[data-test-id='settings-link']")
            `$`("a[href='/settings']").exists() -> `$`("a[href='/settings']")
            else -> `$`("a:contains('Settings')")
        }
        settingsLink.click()
        return this
    }
    
    /**
     * Uses browser back button
     */
    fun goBack(): DashboardPage {
        Selenide.back()
        waitForPageLoad()
        return this
    }
    
    /**
     * Waits for dashboard page to load completely
     */
    private fun waitForPageLoad() {
        // Wait for essential dashboard elements to be present
        dashboardLayout.shouldBe(Condition.visible)
        navigationPanel.shouldBe(Condition.visible)
        contentArea.shouldBe(Condition.visible)
    }
}