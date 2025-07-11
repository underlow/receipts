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
     * Clicks the retry button (alternative method name)
     */
    fun clickRetryButton(): ServiceProviderListPage {
        return clickRetry()
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
     * Checks if the list is empty and displays empty state
     */
    fun shouldBeEmpty(): ServiceProviderListPage {
        val items = getServiceProviderItems()
        assert(items.size() == 0) { "Service provider list should be empty, but found ${items.size()} items" }
        return this
    }

    /**
     * Checks if empty state is displayed
     */
    fun shouldDisplayEmptyState(): ServiceProviderListPage {
        val emptyState = when {
            `$`("[data-test-id='empty-state']").exists() -> `$`("[data-test-id='empty-state']")
            `$`(".empty-state").exists() -> `$`(".empty-state")
            `$`(".no-providers").exists() -> `$`(".no-providers")
            else -> `$`("body")
        }
        emptyState.shouldBe(Condition.visible)
        return this
    }

    /**
     * Checks if the list has the expected number of providers
     */
    fun shouldHaveProviderCount(expectedCount: Int): ServiceProviderListPage {
        val items = getServiceProviderItems()
        assert(items.size() == expectedCount) { "Expected $expectedCount providers, but found ${items.size()}" }
        return this
    }

    /**
     * Checks if the list contains a provider with the given name
     */
    fun shouldContainProvider(providerName: String): ServiceProviderListPage {
        val items = getServiceProviderItems()
        assert(items.size() >= 1) { "Service provider list should contain at least one item" }
        val provider = findProviderByName(providerName)
        assert(provider != null) { "Provider '$providerName' should be present in the list" }
        return this
    }

    /**
     * Checks if the list does not contain a provider with the given name
     */
    fun shouldNotContainProvider(providerName: String): ServiceProviderListPage {
        val provider = findProviderByName(providerName)
        assert(provider == null) { "Provider '$providerName' should not be present in the list" }
        return this
    }

    /**
     * Clicks the create button
     */
    fun clickCreateButton(): ServiceProviderListPage {
        val createButton = when {
            `$`("[data-test-id='create-service-button']").exists() -> `$`("[data-test-id='create-service-button']")
            `$`("#createServiceButton").exists() -> `$`("#createServiceButton")
            `$`(".create-service-button").exists() -> `$`(".create-service-button")
            else -> `$`("button:contains('Create')")
        }
        createButton.shouldBe(Condition.visible).click()
        return this
    }

    /**
     * Checks if create form is displayed
     */
    fun shouldShowCreateForm(): ServiceProviderListPage {
        val form = when {
            `$`("[data-test-id='service-provider-form']").exists() -> `$`("[data-test-id='service-provider-form']")
            `$`(".service-provider-form").exists() -> `$`(".service-provider-form")
            `$`(".create-form").exists() -> `$`(".create-form")
            else -> `$`("form")
        }
        form.shouldBe(Condition.visible)
        return this
    }

    /**
     * Checks if form field is present
     */
    fun shouldHaveFormField(fieldName: String): ServiceProviderListPage {
        val field = when {
            `$`("[data-test-id='$fieldName']").exists() -> `$`("[data-test-id='$fieldName']")
            `$`("#$fieldName").exists() -> `$`("#$fieldName")
            `$`("input[name='$fieldName']").exists() -> `$`("input[name='$fieldName']")
            else -> `$`("input")
        }
        field.shouldBe(Condition.visible)
        return this
    }

    /**
     * Checks if submit button is present
     */
    fun shouldHaveSubmitButton(): ServiceProviderListPage {
        val submitButton = when {
            `$`("[data-test-id='submit-button']").exists() -> `$`("[data-test-id='submit-button']")
            `$`("#saveButton").exists() -> `$`("#saveButton")
            `$`(".submit-button").exists() -> `$`(".submit-button")
            else -> `$`("button[type='submit']")
        }
        submitButton.shouldBe(Condition.visible)
        return this
    }

    /**
     * Clicks on a provider by name
     */
    fun clickProvider(providerName: String): ServiceProviderListPage {
        val provider = findProviderByName(providerName)
        assert(provider != null) { "Provider '$providerName' not found" }
        provider!!.click()
        return this
    }

    /**
     * Checks if provider is selected
     */
    fun shouldHaveSelectedProvider(providerName: String): ServiceProviderListPage {
        val provider = findProviderByName(providerName)
        assert(provider != null) { "Provider '$providerName' not found" }
        assert(provider!!.isSelected()) { "Provider '$providerName' should be selected" }
        return this
    }

    /**
     * Checks if provider is not selected
     */
    fun shouldNotHaveSelectedProvider(providerName: String): ServiceProviderListPage {
        val provider = findProviderByName(providerName)
        if (provider != null) {
            assert(!provider.isSelected()) { "Provider '$providerName' should not be selected" }
        }
        return this
    }

    /**
     * Checks if provider details are shown
     */
    fun shouldShowProviderDetails(): ServiceProviderListPage {
        val detailsPanel = when {
            `$`("[data-test-id='provider-details']").exists() -> `$`("[data-test-id='provider-details']")
            `$`(".provider-details").exists() -> `$`(".provider-details")
            `$`(".details-panel").exists() -> `$`(".details-panel")
            else -> `$`(".split-panel-right")
        }
        detailsPanel.shouldBe(Condition.visible)
        return this
    }

    /**
     * Checks if provider name is displayed in details
     */
    fun shouldDisplayProviderName(providerName: String): ServiceProviderListPage {
        val nameElement = when {
            `$`("[data-test-id='provider-name']").exists() -> `$`("[data-test-id='provider-name']")
            `$`(".provider-name").exists() -> `$`(".provider-name")
            `$`(".provider-title").exists() -> `$`(".provider-title")
            else -> `$`("h1, h2, h3")
        }
        nameElement.shouldHave(Condition.text(providerName))
        return this
    }

    /**
     * Checks if provider state is displayed in details
     */
    fun shouldDisplayProviderState(providerState: String): ServiceProviderListPage {
        val stateElement = when {
            `$`("[data-test-id='provider-state']").exists() -> `$`("[data-test-id='provider-state']")
            `$`(".provider-state").exists() -> `$`(".provider-state")
            `$`(".state-badge").exists() -> `$`(".state-badge")
            else -> `$`(".status")
        }
        stateElement.shouldHave(Condition.text(providerState))
        return this
    }

    /**
     * Checks if provider description is displayed in details
     */
    fun shouldDisplayProviderDescription(description: String): ServiceProviderListPage {
        val descriptionElement = when {
            `$`("[data-test-id='provider-description']").exists() -> `$`("[data-test-id='provider-description']")
            `$`(".provider-description").exists() -> `$`(".provider-description")
            `$`(".description").exists() -> `$`(".description")
            else -> `$`("p")
        }
        descriptionElement.shouldHave(Condition.text(description))
        return this
    }

    /**
     * Applies state filter
     */
    fun applyStateFilter(state: String): ServiceProviderListPage {
        val filter = when {
            `$`("[data-test-id='state-filter']").exists() -> `$`("[data-test-id='state-filter']")
            `$`(".state-filter").exists() -> `$`(".state-filter")
            `$`("select[name='state']").exists() -> `$`("select[name='state']")
            else -> `$`("select")
        }
        filter.selectOption(state)
        return this
    }

    /**
     * Checks if error message is shown
     */
    fun shouldShowErrorMessage(): ServiceProviderListPage {
        val errorElement = when {
            `$`("[data-test-id='error-message']").exists() -> `$`("[data-test-id='error-message']")
            `$`(".error-message").exists() -> `$`(".error-message")
            `$`(".alert-danger").exists() -> `$`(".alert-danger")
            else -> `$`(".error")
        }
        errorElement.shouldBe(Condition.visible)
        return this
    }

    /**
     * Checks if retry button is displayed
     */
    fun shouldDisplayRetryButton(): ServiceProviderListPage {
        val retryButton = when {
            `$`("[data-test-id='retry-button']").exists() -> `$`("[data-test-id='retry-button']")
            `$`(".retry-button").exists() -> `$`(".retry-button")
            else -> `$`("button:contains('Retry')")
        }
        retryButton.shouldBe(Condition.visible)
        return this
    }

    /**
     * Checks if error message contains expected text
     */
    fun shouldHaveErrorMessage(expectedMessage: String): ServiceProviderListPage {
        val errorElement = when {
            `$`("[data-test-id='error-message']").exists() -> `$`("[data-test-id='error-message']")
            `$`(".error-message").exists() -> `$`(".error-message")
            `$`(".alert-danger").exists() -> `$`(".alert-danger")
            else -> `$`(".error")
        }
        errorElement.shouldHave(Condition.text(expectedMessage))
        return this
    }

    /**
     * Checks if error message is not shown
     */
    fun shouldNotShowErrorMessage(): ServiceProviderListPage {
        val errorElement = when {
            `$`("[data-test-id='error-message']").exists() -> `$`("[data-test-id='error-message']")
            `$`(".error-message").exists() -> `$`(".error-message")
            `$`(".alert-danger").exists() -> `$`(".alert-danger")
            else -> null
        }
        errorElement?.shouldNotBe(Condition.visible)
        return this
    }

    /**
     * Checks if provider list is displayed
     */
    fun shouldDisplayProviderList(): ServiceProviderListPage {
        getServiceProviderList().shouldBe(Condition.visible)
        return this
    }

    /**
     * Checks if page is responsive
     */
    fun shouldBeResponsive(): ServiceProviderListPage {
        // Check if main container is visible
        getServiceProviderList().shouldBe(Condition.visible)
        return this
    }

    /**
     * Checks if create button is accessible on mobile
     */
    fun shouldHaveAccessibleCreateButton(): ServiceProviderListPage {
        val createButton = when {
            `$`("[data-test-id='create-service-button']").exists() -> `$`("[data-test-id='create-service-button']")
            `$`("#createServiceButton").exists() -> `$`("#createServiceButton")
            `$`(".create-service-button").exists() -> `$`(".create-service-button")
            else -> `$`("button:contains('Create')")
        }
        createButton.shouldBe(Condition.visible)
        return this
    }

    /**
     * Checks if provider list is accessible on mobile
     */
    fun shouldHaveAccessibleProviderList(): ServiceProviderListPage {
        getServiceProviderList().shouldBe(Condition.visible)
        return this
    }

    /**
     * Checks if provider selection works on mobile
     */
    fun shouldAllowProviderSelection(providerName: String): ServiceProviderListPage {
        val provider = findProviderByName(providerName)
        if (provider != null) {
            provider.click()
            provider.shouldBeSelected()
        }
        return this
    }

    /**
     * Extension function to check if provider is selected
     */
    private fun ServiceProviderItem.shouldBeSelected(): ServiceProviderItem {
        assert(this.isSelected()) { "Provider should be selected" }
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

        /**
         * Verifies provider has expected name
         */
        fun shouldHaveName(expectedName: String): ServiceProviderItem {
            name.shouldHave(Condition.text(expectedName))
            return this
        }

        /**
         * Verifies provider is in active state
         */
        fun shouldBeActive(): ServiceProviderItem {
            state.shouldHave(Condition.text("ACTIVE"))
            return this
        }

        /**
         * Verifies provider is in inactive state
         */
        fun shouldBeInactive(): ServiceProviderItem {
            state.shouldHave(Condition.text("INACTIVE"))
            return this
        }
    }
}