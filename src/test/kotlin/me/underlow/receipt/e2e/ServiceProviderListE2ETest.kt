package me.underlow.receipt.e2e

import me.underlow.receipt.config.BaseE2ETest
import me.underlow.receipt.e2e.helpers.TestDataHelper
import me.underlow.receipt.e2e.helpers.TestNavigationHelper
import me.underlow.receipt.e2e.pageobjects.ServiceProviderListPage
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

/**
 * E2E tests for Service Provider List functionality.
 * Tests complete user journeys for viewing and managing service providers.
 *
 * Each test follows the given-when-then pattern with clear business logic.
 */
class ServiceProviderListE2ETest : BaseE2ETest() {

    private lateinit var navigationHelper: TestNavigationHelper
    private lateinit var serviceProviderListPage: ServiceProviderListPage
    private lateinit var testDataHelper: TestDataHelper

    @BeforeEach
    fun setUpServiceProviderListTests() {
        // Given: Clean test environment with authenticated user
        navigationHelper = TestNavigationHelper()
        serviceProviderListPage = ServiceProviderListPage()
        testDataHelper = TestDataHelper()

        navigationHelper.loginAndNavigateToServices()
    }

    @AfterEach
    fun tearDownServiceProviderListTests() {
        // Clean up test data and browser state
        testDataHelper.cleanupTestData()
        navigationHelper.clearBrowserState()
    }

    @Test
    fun shouldDisplayEmptyServiceProviderListWhenNoProvidersExist() {
        // Given: Database contains no service providers
        testDataHelper.clearAllServiceProviders()

        // When: User navigates to services tab
        navigationHelper.refreshServicesData()

        // Then: Service provider list should be visible and empty
        serviceProviderListPage.waitForListToLoad()
            .shouldBeEmpty()
            .shouldDisplayEmptyState()
    }

    @Test
    fun shouldDisplayServiceProviderListWhenProvidersExist() {
        // Given: Database contains service providers
        val testProvider = testDataHelper.createTestServiceProvider("Test Provider", "ACTIVE")

        // When: User navigates to services tab
        navigationHelper.refreshServicesData()

        // Then: Service provider list should display existing providers
        serviceProviderListPage.waitForListToLoad()
            .shouldHaveProviderCount(1)
            .shouldContainProvider(testProvider.name)
    }

    @Test
    fun shouldNavigateToCreateFormWhenCreateButtonClicked() {
        // Given: User is viewing service providers list
        serviceProviderListPage.waitForListToLoad()

        // When: User clicks create new service provider button
        serviceProviderListPage.clickCreateButton()

        // Then: Service provider creation form should be displayed
        serviceProviderListPage.shouldShowCreateForm()
            .shouldHaveFormField("provider-name")
            .shouldHaveFormField("provider-type")
            .shouldHaveSubmitButton()
    }

    @Test
    fun shouldSelectServiceProviderWhenItemClicked() {
        // Given: Service provider list contains multiple providers
        val provider1 = testDataHelper.createTestServiceProvider("Provider 1", "ACTIVE")
        val provider2 = testDataHelper.createTestServiceProvider("Provider 2", "ACTIVE")
        navigationHelper.refreshServicesData()

        // When: User clicks on a specific service provider
        serviceProviderListPage.waitForListToLoad()
            .clickProvider(provider1.name)

        // Then: Selected provider should be highlighted
        serviceProviderListPage.shouldHaveSelectedProvider(provider1.name)
            .shouldNotHaveSelectedProvider(provider2.name)
    }

    @Test
    fun shouldDisplayProviderDetailsWhenProviderSelected() {
        // Given: Service provider list contains a provider with details
        val provider = testDataHelper.createTestServiceProvider(
            name = "Test Provider",
            state = "ACTIVE",
            description = "Test provider description"
        )
        navigationHelper.refreshServicesData()

        // When: User selects a service provider
        serviceProviderListPage.waitForListToLoad()
            .clickProvider(provider.name)

        // Then: Provider details should be displayed in details panel
        serviceProviderListPage.shouldShowProviderDetails()
            .shouldDisplayProviderName(provider.name)
            .shouldDisplayProviderState(provider.state)
            .shouldDisplayProviderDescription(provider.description ?: "Test provider description")
    }

    @Test
    fun shouldFilterProvidersByStateWhenFilterApplied() {
        // Given: Service provider list contains providers with different states
        val activeProvider = testDataHelper.createTestServiceProvider("Active Provider", "ACTIVE")
        val inactiveProvider = testDataHelper.createTestServiceProvider("Inactive Provider", "INACTIVE")
        navigationHelper.refreshServicesData()

        // When: User applies filter to show only active providers
        serviceProviderListPage.waitForListToLoad()
            .applyStateFilter("ACTIVE")

        // Then: Only active providers should be visible
        serviceProviderListPage.shouldContainProvider(activeProvider.name)
            .shouldNotContainProvider(inactiveProvider.name)
    }

    @Test
    fun shouldDisplayErrorMessageWhenServiceProviderLoadingFails() {
        // Given: Backend service is unavailable
        testDataHelper.simulateServiceUnavailable()

        // When: User attempts to load service providers
        navigationHelper.refreshServicesData()

        // Then: Error message should be displayed with retry option
        serviceProviderListPage.shouldShowErrorMessage()
            .shouldDisplayRetryButton()
            .shouldHaveErrorMessage("Unable to load service providers")
    }

    @Test
    fun shouldRetryLoadingWhenRetryButtonClicked() {
        // Given: Service provider loading initially fails
        testDataHelper.simulateServiceUnavailable()
        navigationHelper.refreshServicesData()
        serviceProviderListPage.shouldShowErrorMessage()

        // When: Service becomes available and user clicks retry
        testDataHelper.restoreServiceAvailability()
        serviceProviderListPage.clickRetryButton()

        // Then: Service providers should load successfully
        serviceProviderListPage.waitForListToLoad()
            .shouldNotShowErrorMessage()
            .shouldDisplayProviderList()
    }

    @Test
    fun shouldMaintainFunctionalityOnMobileViewport() {
        // Given: Service provider list is displayed on desktop
        val provider = testDataHelper.createTestServiceProvider("Mobile Test Provider", "ACTIVE")
        navigationHelper.refreshServicesData()
        serviceProviderListPage.waitForListToLoad()

        // When: User switches to mobile viewport
        navigationHelper.setMobileViewport()

        // Then: All functionality should remain accessible
        serviceProviderListPage.shouldBeResponsive()
            .shouldHaveAccessibleCreateButton()
            .shouldHaveAccessibleProviderList()
            .shouldAllowProviderSelection(provider.name)
    }
}
