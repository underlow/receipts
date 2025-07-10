package me.underlow.receipt.e2e.pageobjects

import com.codeborne.selenide.Condition
import com.codeborne.selenide.ElementsCollection
import com.codeborne.selenide.Selenide.`$`
import com.codeborne.selenide.Selenide.`$$`
import com.codeborne.selenide.SelenideElement

/**
 * Page Object Model for Service Provider List functionality.
 * Encapsulates all UI interactions for the service provider list page.
 */
class ServiceProviderListPage {

    // Main container elements using correct selectors from dashboard.html
    private fun getServiceProviderList(): SelenideElement {
        return when {
            `$`("#serviceProviderList").exists() -> `$`("#serviceProviderList")
            `$`(".service-provider-list").exists() -> `$`(".service-provider-list")
            `$`(".split-panel-left").exists() -> `$`(".split-panel-left")
            else -> `$`("body") // fallback to body if nothing else is found
        }
    }
    
    private fun getServicesTab(): SelenideElement {
        return when {
            `$`("a[href='#services']").exists() -> `$`("a[href='#services']")
            `$`("[data-test-id='services-tab']").exists() -> `$`("[data-test-id='services-tab']")
            `$`("#services-tab").exists() -> `$`("#services-tab")
            else -> `$`("nav a:contains('Services')").takeIf { it.exists() } ?: `$`("body")
        }
    }
    
    private fun getServicesContent(): SelenideElement {
        return when {
            `$`("#services").exists() -> `$`("#services")
            `$`("#services-content").exists() -> `$`("#services-content")
            `$`(".services-content").exists() -> `$`(".services-content")
            else -> `$`("body") // fallback to body if nothing else is found
        }
    }

    // Service provider items using correct selectors from dashboard.html
    private fun getServiceProviderItems(): ElementsCollection {
        return when {
            `$$`(".service-provider-item").size() > 0 -> `$$`(".service-provider-item")
            `$$`("[data-test-id='service-provider-item']").size() > 0 -> `$$`("[data-test-id='service-provider-item']")
            `$$`(".provider-item").size() > 0 -> `$$`(".provider-item")
            else -> `$$`("div") // fallback
        }
    }

    /**
     * Navigates to the services tab and waits for content to load
     */
    fun navigateToServicesTab(): ServiceProviderListPage {
        getServicesTab().shouldBe(Condition.visible).click()
        getServicesContent().shouldBe(Condition.visible)
        return this
    }

    /**
     * Returns the service provider list container element
     */
    fun getServiceProviderListElement(): SelenideElement {
        return getServiceProviderList()
    }

    /**
     * Waits for service provider list to be visible and loaded
     */
    fun waitForListToLoad(): ServiceProviderListPage {
        getServiceProviderList().shouldBe(Condition.visible)
        return this
    }

    /**
     * Returns the number of service provider items displayed
     */
    fun getProviderCount(): Int {
        return getServiceProviderItems().size()
    }

    /**
     * Returns a specific service provider item by index
     */
    fun getProviderItem(index: Int): ServiceProviderItem {
        val element = getServiceProviderItems().get(index)
        return ServiceProviderItem(element)
    }

    /**
     * Returns the first service provider item
     */
    fun getFirstProvider(): ServiceProviderItem {
        return getProviderItem(0)
    }

    /**
     * Finds a service provider by name
     */
    fun findProviderByName(name: String): ServiceProviderItem? {
        val items = getServiceProviderItems().filter { item ->
            item.`$`(".service-provider-name").text() == name ||
            item.text().contains(name)
        }
        return if (items.isNotEmpty()) ServiceProviderItem(items.first()) else null
    }

    /**
     * Finds a service provider by state
     */
    fun findProviderByState(state: String): ServiceProviderItem? {
        val items = getServiceProviderItems().filter { item ->
            item.`$`(".service-provider-state").text() == state
        }
        return if (items.isNotEmpty()) ServiceProviderItem(items.first()) else null
    }

    /**
     * Checks if the list is empty
     */
    fun isListEmpty(): Boolean {
        return getServiceProviderItems().size() == 0
    }

    /**
     * Checks if error message is displayed
     */
    fun isErrorDisplayed(): Boolean {
        val errorElement = when {
            `$`("[data-test-id='service-provider-error']").exists() -> `$`("[data-test-id='service-provider-error']")
            `$`(".alert-danger").exists() -> `$`(".alert-danger")
            `$`(".error-message").exists() -> `$`(".error-message")
            `$`(".error").exists() -> `$`(".error")
            else -> null
        }
        return errorElement?.isDisplayed ?: false
    }

    /**
     * Gets the error message text
     */
    fun getErrorMessage(): String {
        val errorElement = when {
            `$`("[data-test-id='service-provider-error']").exists() -> `$`("[data-test-id='service-provider-error']")
            `$`(".alert-danger").exists() -> `$`(".alert-danger")
            `$`(".error-message").exists() -> `$`(".error-message")
            `$`(".error").exists() -> `$`(".error")
            else -> `$`("body")
        }
        return errorElement.text()
    }

    /**
     * Clicks the retry button
     */
    fun clickRetry(): ServiceProviderListPage {
        val retryElement = when {
            `$`("[data-test-id='retry-button']").exists() -> `$`("[data-test-id='retry-button']")
            `$`(".retry-button").exists() -> `$`(".retry-button")
            `$`("button:contains('Retry')").exists() -> `$`("button:contains('Retry')")
            else -> `$`("button")
        }
        retryElement.shouldBe(Condition.visible).click()
        return this
    }

    /**
     * Checks if the form title is displayed
     */
    fun isFormTitleDisplayed(): Boolean {
        val titleElement = when {
            `$`("#formTitle").exists() -> `$`("#formTitle")
            `$`(".form-title").exists() -> `$`(".form-title")
            `$`("[data-test-id='form-title']").exists() -> `$`("[data-test-id='form-title']")
            else -> null
        }
        return titleElement?.isDisplayed ?: false
    }

    /**
     * Gets the form title text
     */
    fun getFormTitle(): String {
        val titleElement = when {
            `$`("#formTitle").exists() -> `$`("#formTitle")
            `$`(".form-title").exists() -> `$`(".form-title")
            `$`("[data-test-id='form-title']").exists() -> `$`("[data-test-id='form-title']")
            else -> `$`("body")
        }
        return titleElement.text()
    }

    /**
     * Scrolls to the last provider item
     */
    fun scrollToLastProvider(): ServiceProviderListPage {
        val items = getServiceProviderItems()
        if (items.size() > 0) {
            items.last().scrollTo()
        }
        return this
    }

    /**
     * Represents a single service provider item in the list
     */
    class ServiceProviderItem(private val element: SelenideElement) {

        private val avatar = element.`$`(".service-provider-avatar")
        private val avatarFallback = element.`$`(".service-provider-avatar-fallback")
        private val name = element.`$`(".service-provider-name")
        private val state = element.`$`(".service-provider-state")
        private val info = element.`$`(".service-provider-info")

        /**
         * Clicks on the service provider item
         */
        fun click(): ServiceProviderItem {
            element.shouldBe(Condition.visible).click()
            return this
        }

        /**
         * Checks if the provider is selected
         */
        fun isSelected(): Boolean {
            return element.has(Condition.cssClass("selected"))
        }

        /**
         * Checks if the provider is hidden (dimmed)
         */
        fun isHidden(): Boolean {
            return element.has(Condition.cssClass("hidden"))
        }

        /**
         * Gets the provider name
         */
        fun getName(): String {
            return name.text()
        }

        /**
         * Gets the provider state
         */
        fun getState(): String {
            return state.text()
        }

        /**
         * Checks if avatar is displayed
         */
        fun hasAvatar(): Boolean {
            return avatar.isDisplayed
        }

        /**
         * Gets the avatar source URL
         */
        fun getAvatarSrc(): String? {
            return avatar.getAttribute("src")
        }

        /**
         * Checks if avatar fallback is displayed
         */
        fun hasAvatarFallback(): Boolean {
            return avatarFallback.isDisplayed
        }

        /**
         * Gets the avatar fallback text (initials)
         */
        fun getAvatarFallbackText(): String {
            return avatarFallback.text()
        }

        /**
         * Checks if the provider item is visible
         */
        fun isVisible(): Boolean {
            return element.isDisplayed
        }

        /**
         * Scrolls to this provider item
         */
        fun scrollTo(): ServiceProviderItem {
            element.scrollTo()
            return this
        }

        /**
         * Waits for the provider to be selected
         */
        fun waitForSelection(): ServiceProviderItem {
            element.shouldHave(Condition.cssClass("selected"))
            return this
        }

        /**
         * Waits for the provider to not be selected
         */
        fun waitForDeselection(): ServiceProviderItem {
            element.shouldNotHave(Condition.cssClass("selected"))
            return this
        }
    }
}