package me.underlow.receipt.e2e.pages

import com.codeborne.selenide.Condition
import com.codeborne.selenide.Selenide
import com.codeborne.selenide.Selenide.`$`

/**
 * Page Object for settings page interactions.
 * Encapsulates settings page elements and actions using reliable selectors.
 */
class SettingsPage {
    
    // Page elements with fallback selector strategies
    private val pageTitle get() = when {
        `$`("[data-test-id='page-title']").exists() -> `$`("[data-test-id='page-title']")
        `$`("h1").exists() -> `$`("h1")
        else -> `$`(".page-title")
    }
    
    /**
     * Opens settings page and waits for it to load
     */
    fun open(): SettingsPage {
        Selenide.open("/settings")
        waitForPageLoad()
        return this
    }
    
    /**
     * Verifies settings page is displayed with correct title
     */
    fun shouldBeDisplayed(): SettingsPage {
        pageTitle.shouldBe(Condition.visible)
        pageTitle.shouldHave(Condition.text("Settings"))
        return this
    }
    
    /**
     * Verifies page URL contains settings path
     */
    fun shouldBeOnSettingsUrl(): SettingsPage {
        Selenide.webdriver().`object`().currentUrl.let { url ->
            assert(url.contains("/settings")) { "Expected to be on settings page, but was on: $url" }
        }
        return this
    }
    
    /**
     * Waits for settings page to load completely
     */
    private fun waitForPageLoad() {
        pageTitle.shouldBe(Condition.visible)
    }
}