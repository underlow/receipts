package me.underlow.receipt.e2e.pages

import com.codeborne.selenide.Condition
import com.codeborne.selenide.Selenide
import com.codeborne.selenide.Selenide.`$`

/**
 * Page Object for profile page interactions.
 * Encapsulates profile page elements and actions using reliable selectors.
 */
class ProfilePage {
    
    // Page elements with fallback selector strategies
    private val pageTitle get() = when {
        `$`("[data-test-id='page-title']").exists() -> `$`("[data-test-id='page-title']")
        `$`("h1").exists() -> `$`("h1")
        else -> `$`(".page-title")
    }
    
    /**
     * Opens profile page and waits for it to load
     */
    fun open(): ProfilePage {
        Selenide.open("/profile")
        waitForPageLoad()
        return this
    }
    
    /**
     * Verifies profile page is displayed with correct title
     */
    fun shouldBeDisplayed(): ProfilePage {
        pageTitle.shouldBe(Condition.visible)
        pageTitle.shouldHave(Condition.text("User Profile"))
        return this
    }
    
    /**
     * Verifies page URL contains profile path
     */
    fun shouldBeOnProfileUrl(): ProfilePage {
        Selenide.webdriver().`object`().currentUrl.let { url ->
            assert(url.contains("/profile")) { "Expected to be on profile page, but was on: $url" }
        }
        return this
    }
    
    /**
     * Waits for profile page to load completely
     */
    private fun waitForPageLoad() {
        pageTitle.shouldBe(Condition.visible)
    }
}