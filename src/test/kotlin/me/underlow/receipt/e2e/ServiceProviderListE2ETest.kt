package me.underlow.receipt.e2e

import com.codeborne.selenide.Condition
import com.github.tomakehurst.wiremock.WireMockServer
import me.underlow.receipt.config.BaseE2ETest
import me.underlow.receipt.e2e.helpers.ServiceProviderApiMockHelper
import me.underlow.receipt.e2e.helpers.TestNavigationHelper
import me.underlow.receipt.e2e.pageobjects.ServiceProviderListPage
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

/**
 * E2E tests for Service Provider List functionality.
 * Tests the complete user journey for viewing and interacting with service providers.
 * 
 * Each test follows the given-when-then pattern and has a single responsibility.
 */
class ServiceProviderListE2ETest : BaseE2ETest() {

    private lateinit var wireMockServer: WireMockServer
    private lateinit var apiMockHelper: ServiceProviderApiMockHelper
    private lateinit var navigationHelper: TestNavigationHelper
    private lateinit var serviceProviderListPage: ServiceProviderListPage

    @BeforeEach
    fun setUpServiceProviderListTests() {
        // Given: Fresh test environment with mocked API
        wireMockServer = WireMockServer(8089)
        wireMockServer.start()
        
        apiMockHelper = ServiceProviderApiMockHelper(wireMockServer)
        navigationHelper = TestNavigationHelper()
        serviceProviderListPage = ServiceProviderListPage()
        
        // And: User is authenticated and on services tab
        navigationHelper.loginAndNavigateToServices()
    }

    @AfterEach
    fun tearDownServiceProviderListTests() {
        if (::wireMockServer.isInitialized) {
            wireMockServer.stop()
        }
        navigationHelper.clearBrowserState()
    }

    @Test
    fun shouldDisplayServiceProvidersWithAvatarsWhenDataLoaded() {
        // Given: API returns service providers with avatars
        apiMockHelper.setupSuccessfulResponse()
        
        // When: Service provider list loads
        navigationHelper.refreshData()
        
        // Then: Service provider list should be visible
        serviceProviderListPage.waitForListToLoad()
        
        // And: Should display expected number of providers
        assert(serviceProviderListPage.getProviderCount() >= 2) { 
            "Expected at least 2 providers, but found ${serviceProviderListPage.getProviderCount()}" 
        }
        
        // And: First provider should have avatar and correct information
        val firstProvider = serviceProviderListPage.getFirstProvider()
        assert(firstProvider.isVisible()) { "First provider should be visible" }
        assert(firstProvider.hasAvatar()) { "First provider should have avatar" }
        assert(firstProvider.getAvatarSrc()?.contains("avatar1.jpg") == true) { 
            "Avatar should contain 'avatar1.jpg'" 
        }
        assert(firstProvider.getName() == "Electric Company") { 
            "Provider name should be 'Electric Company'" 
        }
        assert(firstProvider.getState() == "Active") { 
            "Provider state should be 'Active'" 
        }
    }

    @Test
    fun shouldDisplayFallbackAvatarWhenProviderHasNoAvatar() {
        // Given: API returns service providers where some have no avatar
        apiMockHelper.setupProvidersWithMixedAvatars()
        
        // When: Service provider list loads
        navigationHelper.refreshData()
        
        // Then: Provider without avatar should show fallback with initials
        serviceProviderListPage.waitForListToLoad()
        
        val providerWithoutAvatar = serviceProviderListPage.getProviderItem(1)
        assert(providerWithoutAvatar.hasAvatarFallback()) { 
            "Provider without avatar should show fallback" 
        }
        assert(providerWithoutAvatar.getAvatarFallbackText() == "W") { 
            "Fallback should show 'W' for 'Water'" 
        }
    }

    @Test
    fun shouldDisplayHiddenProviderWithDimmedAppearance() {
        // Given: API returns mix of active and hidden providers
        apiMockHelper.setupProvidersWithMixedStates()
        
        // When: Service provider list loads
        navigationHelper.refreshData()
        
        // Then: Hidden provider should be displayed with dimmed styling
        serviceProviderListPage.waitForListToLoad()
        
        val hiddenProvider = serviceProviderListPage.findProviderByState("Hidden")
        assert(hiddenProvider != null) { "Hidden provider should be found" }
        assert(hiddenProvider!!.isVisible()) { "Hidden provider should be visible" }
        assert(hiddenProvider.isHidden()) { "Hidden provider should have dimmed styling" }
        assert(hiddenProvider.getState() == "Hidden") { "State should be 'Hidden'" }
    }

    @Test
    fun shouldSelectProviderWhenClicked() {
        // Given: Service providers are loaded
        apiMockHelper.setupSuccessfulResponse()
        navigationHelper.refreshData()
        serviceProviderListPage.waitForListToLoad()
        
        // When: User clicks on first provider
        val firstProvider = serviceProviderListPage.getFirstProvider()
        firstProvider.click()
        
        // Then: Provider should be selected and highlighted
        firstProvider.waitForSelection()
        assert(firstProvider.isSelected()) { "First provider should be selected" }
        
        // And: Form should display provider details
        assert(serviceProviderListPage.isFormTitleDisplayed()) { 
            "Form title should be displayed" 
        }
        assert(serviceProviderListPage.getFormTitle().contains("Edit Service Provider")) { 
            "Form title should contain 'Edit Service Provider'" 
        }
    }

    @Test
    fun shouldSelectOnlyOneProviderAtATime() {
        // Given: Multiple service providers are loaded
        apiMockHelper.setupSuccessfulResponse()
        navigationHelper.refreshData()
        serviceProviderListPage.waitForListToLoad()
        
        val firstProvider = serviceProviderListPage.getProviderItem(0)
        val secondProvider = serviceProviderListPage.getProviderItem(1)
        
        // When: User clicks on second provider after first is selected
        firstProvider.click()
        firstProvider.waitForSelection()
        secondProvider.click()
        
        // Then: Only second provider should be selected
        secondProvider.waitForSelection()
        assert(secondProvider.isSelected()) { "Second provider should be selected" }
        
        // And: First provider should not be selected
        firstProvider.waitForDeselection()
        assert(!firstProvider.isSelected()) { "First provider should not be selected" }
    }

    @Test
    fun shouldSupportScrollingWithManyProviders() {
        // Given: API returns many service providers
        apiMockHelper.setupManyProviders(15)
        
        // When: Service provider list loads
        navigationHelper.refreshData()
        
        // Then: List should display all providers
        serviceProviderListPage.waitForListToLoad()
        assert(serviceProviderListPage.getProviderCount() >= 10) { 
            "Should have at least 10 providers for scrolling test" 
        }
        
        // And: Should be able to scroll to last provider
        serviceProviderListPage.scrollToLastProvider()
        val lastProvider = serviceProviderListPage.getProviderItem(
            serviceProviderListPage.getProviderCount() - 1
        )
        assert(lastProvider.isVisible()) { "Last provider should be visible after scrolling" }
    }

    @Test
    fun shouldDisplayErrorMessageWhenApiFailsToLoad() {
        // Given: API returns error response
        apiMockHelper.setupServerError()
        
        // When: Service provider list tries to load
        navigationHelper.refreshData()
        
        // Then: Should display error message
        assert(serviceProviderListPage.isErrorDisplayed()) { 
            "Error message should be displayed" 
        }
        assert(serviceProviderListPage.getErrorMessage().contains("Failed to load service providers")) { 
            "Error message should contain expected text" 
        }
        
        // And: Should provide retry functionality
        serviceProviderListPage.clickRetry()
        // Verify retry button works (would need additional API setup for full test)
    }

    @Test
    fun shouldSupportResponsiveDesignOnMobileViewport() {
        // Given: Service providers are loaded
        apiMockHelper.setupSuccessfulResponse()
        navigationHelper.refreshData()
        serviceProviderListPage.waitForListToLoad()
        
        // When: Browser viewport is set to mobile size
        navigationHelper.setMobileViewport()
        
        // Then: List items should remain functional and visible
        val firstProvider = serviceProviderListPage.getFirstProvider()
        assert(firstProvider.isVisible()) { "Provider should be visible on mobile" }
        
        // And: Should be able to interact with providers
        firstProvider.click()
        firstProvider.waitForSelection()
        assert(firstProvider.isSelected()) { "Provider should be selectable on mobile" }
        
        // Cleanup: Reset to normal viewport
        navigationHelper.maximizeBrowser()
    }

    @Test
    fun shouldProvideProvidersForPotentialSearchFunctionality() {
        // Given: Service providers with different names are loaded
        apiMockHelper.setupProvidersForSearch()
        
        // When: Service provider list loads
        navigationHelper.refreshData()
        
        // Then: Should display providers with searchable names
        serviceProviderListPage.waitForListToLoad()
        assert(serviceProviderListPage.getProviderCount() >= 3) { 
            "Should have at least 3 providers for search testing" 
        }
        
        // And: Should have providers with different names for filtering
        val electricProvider = serviceProviderListPage.findProviderByName("Electric Company")
        val waterProvider = serviceProviderListPage.findProviderByName("Water Utility")
        val internetProvider = serviceProviderListPage.findProviderByName("Internet Provider")
        
        assert(electricProvider != null) { "Electric Company provider should exist" }
        assert(waterProvider != null) { "Water Utility provider should exist" }
        assert(internetProvider != null) { "Internet Provider should exist" }
    }
}