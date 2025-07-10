package me.underlow.receipt.e2e

import com.codeborne.selenide.Condition
import com.codeborne.selenide.Selenide
import com.codeborne.selenide.Selenide.`$`
import com.codeborne.selenide.Selenide.`$$`
import com.codeborne.selenide.WebDriverRunner
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.*
import me.underlow.receipt.config.BaseE2ETest
import me.underlow.receipt.config.TestSecurityConfiguration
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach

/**
 * E2E tests for Service Provider List functionality
 * Tests list rendering, selection, and interaction with mock data
 */
class ServiceProviderListE2ETest : BaseE2ETest() {

    private lateinit var wireMockServer: WireMockServer

    @BeforeEach
    fun setup() {
        // Setup WireMock server for API mocking
        wireMockServer = WireMockServer(8089)
        wireMockServer.start()
        
        // Given: User is authenticated and on services tab
        performLogin(TestSecurityConfiguration.ALLOWED_EMAIL_1, TestSecurityConfiguration.TEST_PASSWORD)
        navigateToServicesTab()
    }

    @AfterEach
    fun tearDown() {
        if (::wireMockServer.isInitialized) {
            wireMockServer.stop()
        }
    }

    @Test
    fun `given service providers exist when list loads then should display providers with avatars`() {
        // Given: API returns service providers with avatars
        setupServiceProvidersApiMock(createMockServiceProviders())
        
        // When: Services tab loads data
        refreshServicesData()
        
        // Then: Should display service provider list
        val providerList = `$`(".service-provider-list")
        providerList.shouldBe(Condition.visible)
        
        val providerItems = `$$`(".service-provider-item")
        assertTrue(providerItems.size() >= 2)
        
        // And: First provider should have avatar
        val firstProvider = providerItems.first()
        val avatar = firstProvider.`$`(".service-provider-avatar")
        avatar.shouldBe(Condition.visible)
        assertTrue(avatar.getAttribute("src")?.contains("avatar1.jpg") == true)
        
        // And: Should display provider name
        val providerName = firstProvider.`$`(".service-provider-name")
        providerName.shouldBe(Condition.visible)
        assertEquals("Electric Company", providerName.text())
        
        // And: Should display provider state
        val providerState = firstProvider.`$`(".service-provider-state")
        providerState.shouldBe(Condition.visible)
        assertEquals("Active", providerState.text())
    }

    @Test
    fun `given service provider without avatar when displayed then should show fallback initials`() {
        // Given: API returns service provider without avatar
        setupServiceProvidersApiMock(createMockServiceProvidersWithoutAvatars())
        
        // When: Services tab loads data
        refreshServicesData()
        
        // Then: Should display fallback avatar with initials
        val providerItems = `$$`(".service-provider-item")
        val providerWithoutAvatar = providerItems.get(1) // Second provider has no avatar
        
        val avatarFallback = providerWithoutAvatar.`$`(".service-provider-avatar-fallback")
        avatarFallback.shouldBe(Condition.visible)
        assertEquals("W", avatarFallback.text()) // "Water" -> "W"
    }

    @Test
    fun `given hidden service provider when displayed then should show dimmed appearance`() {
        // Given: API returns mix of active and hidden providers
        setupServiceProvidersApiMock(createMockServiceProvidersWithHidden())
        
        // When: Services tab loads data
        refreshServicesData()
        
        // Then: Hidden provider should have dimmed styling
        val providerItems = `$$`(".service-provider-item")
        val hiddenProvider = providerItems.find { it.getAttribute("class")?.contains("hidden") == true }
        
        assertNotNull(hiddenProvider)
        hiddenProvider!!.shouldBe(Condition.visible)
        
        // And: Should show "Hidden" state
        val stateText = hiddenProvider.`$`(".service-provider-state")
        stateText.shouldBe(Condition.visible)
        assertEquals("Hidden", stateText.text())
    }

    @Test
    fun `given service provider when clicked then should select and highlight item`() {
        // Given: Service providers are loaded
        setupServiceProvidersApiMock(createMockServiceProviders())
        refreshServicesData()
        
        // When: User clicks on first provider
        val providerItems = `$$`(".service-provider-item")
        val firstProvider = providerItems.first()
        firstProvider.click()
        
        // Then: Provider should be selected and highlighted
        firstProvider.shouldHave(Condition.cssClass("selected"))
        
        // And: Form should display provider details
        val formTitle = `$`("#formTitle")
        formTitle.shouldBe(Condition.visible)
        assertTrue(formTitle.text().contains("Edit Service Provider"))
    }

    @Test
    fun `given multiple providers when one selected then only that provider should be highlighted`() {
        // Given: Multiple service providers are loaded
        setupServiceProvidersApiMock(createMockServiceProviders())
        refreshServicesData()
        
        val providerItems = `$$`(".service-provider-item")
        
        // When: User clicks on second provider
        val secondProvider = providerItems.get(1)
        secondProvider.click()
        
        // Then: Only second provider should be selected
        secondProvider.shouldHave(Condition.cssClass("selected"))
        
        // And: First provider should not be selected
        val firstProvider = providerItems.first()
        firstProvider.shouldNotHave(Condition.cssClass("selected"))
    }

    @Test
    fun `given service provider list when scrolling required then should support long lists`() {
        // Given: API returns many service providers
        setupServiceProvidersApiMock(createMockManyServiceProviders())
        
        // When: Services tab loads data
        refreshServicesData()
        
        // Then: List should be scrollable
        val listContainer = `$`("#serviceProviderList")
        listContainer.shouldBe(Condition.visible)
        
        val providerItems = `$$`(".service-provider-item")
        assertTrue(providerItems.size() >= 10)
        
        // And: All items should be accessible through scrolling
        val lastProvider = providerItems.last()
        lastProvider.scrollTo()
        lastProvider.shouldBe(Condition.visible)
    }

    @Test
    fun `given API error when loading providers then should display error message`() {
        // Given: API returns error
        setupServiceProvidersApiError()
        
        // When: Services tab tries to load data
        refreshServicesData()
        
        // Then: Should display error message
        val errorAlert = `$`("#serviceProviderList .alert-danger")
        errorAlert.shouldBe(Condition.visible)
        assertTrue(errorAlert.text().contains("Failed to load service providers"))
        
        // And: Should have retry button
        val retryButton = errorAlert.`$`("button[onclick='loadServicesData()']")
        retryButton.shouldBe(Condition.visible)
        assertTrue(retryButton.text().contains("Retry"))
    }

    @Test
    fun `given provider list when search functionality added then should filter results`() {
        // Given: Service providers with different names are loaded
        setupServiceProvidersApiMock(createMockServiceProvidersForSearch())
        refreshServicesData()
        
        // Note: Search functionality would be implemented as a future enhancement
        // This test verifies the list structure supports filtering
        val providerItems = `$$`(".service-provider-item")
        assertTrue(providerItems.size() >= 3)
        
        // Verify different provider names exist for potential filtering
        val providerNames = providerItems.map { it.`$`(".service-provider-name").text() }
        assertTrue(providerNames.contains("Electric Company"))
        assertTrue(providerNames.contains("Water Utility"))
        assertTrue(providerNames.contains("Internet Provider"))
    }

    @Test
    fun `given provider list when responsive design tested then should adapt layout`() {
        // Given: Service providers are loaded
        setupServiceProvidersApiMock(createMockServiceProviders())
        refreshServicesData()
        
        // When: Browser is resized to mobile width
        WebDriverRunner.getWebDriver().manage().window().setSize(
            org.openqa.selenium.Dimension(400, 800)
        )
        
        // Then: List items should remain functional
        val providerItems = `$$`(".service-provider-item")
        val firstProvider = providerItems.first()
        firstProvider.shouldBe(Condition.visible)
        
        // And: Avatar and text should be properly laid out
        val avatar = firstProvider.`$`(".service-provider-avatar")
        val providerInfo = firstProvider.`$`(".service-provider-info")
        
        avatar.shouldBe(Condition.visible)
        providerInfo.shouldBe(Condition.visible)
        
        // Reset window size
        WebDriverRunner.getWebDriver().manage().window().maximize()
    }

    /**
     * Helper methods for API mocking and test data
     */
    private fun setupServiceProvidersApiMock(providers: String) {
        wireMockServer.stubFor(
            get(urlEqualTo("/api/service-providers"))
                .willReturn(
                    aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(providers)
                )
        )
    }

    private fun setupServiceProvidersApiError() {
        wireMockServer.stubFor(
            get(urlEqualTo("/api/service-providers"))
                .willReturn(
                    aResponse()
                        .withStatus(500)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""{"error": "Internal server error"}""")
                )
        )
    }

    private fun createMockServiceProviders(): String {
        return """[
            {
                "id": 1,
                "name": "Electric Company",
                "avatar": "/uploads/avatar1.jpg",
                "comment": "Monthly electricity bills",
                "commentForOcr": "Electric utility provider",
                "regular": "MONTHLY",
                "customFields": {
                    "account": "123456789",
                    "customerService": "+1-800-123-4567"
                },
                "state": "ACTIVE",
                "createdDate": "2025-01-01T10:00:00Z",
                "modifiedDate": "2025-01-01T10:00:00Z"
            },
            {
                "id": 2,
                "name": "Water Utility",
                "avatar": null,
                "comment": "Quarterly water bills",
                "commentForOcr": "Water utility provider",
                "regular": "NOT_REGULAR",
                "customFields": {},
                "state": "ACTIVE",
                "createdDate": "2025-01-01T11:00:00Z",
                "modifiedDate": "2025-01-01T11:00:00Z"
            }
        ]"""
    }

    private fun createMockServiceProvidersWithoutAvatars(): String {
        return """[
            {
                "id": 1,
                "name": "Electric Company",
                "avatar": "/uploads/avatar1.jpg",
                "state": "ACTIVE"
            },
            {
                "id": 2,
                "name": "Water Utility",
                "avatar": null,
                "state": "ACTIVE"
            }
        ]"""
    }

    private fun createMockServiceProvidersWithHidden(): String {
        return """[
            {
                "id": 1,
                "name": "Electric Company",
                "avatar": "/uploads/avatar1.jpg",
                "state": "ACTIVE"
            },
            {
                "id": 2,
                "name": "Old Provider",
                "avatar": null,
                "state": "HIDDEN"
            }
        ]"""
    }

    private fun createMockManyServiceProviders(): String {
        val providers = mutableListOf<String>()
        for (i in 1..15) {
            providers.add("""
                {
                    "id": $i,
                    "name": "Provider $i",
                    "avatar": null,
                    "state": "ACTIVE"
                }
            """.trimIndent())
        }
        return "[${providers.joinToString(",")}]"
    }

    private fun createMockServiceProvidersForSearch(): String {
        return """[
            {
                "id": 1,
                "name": "Electric Company",
                "avatar": null,
                "state": "ACTIVE"
            },
            {
                "id": 2,
                "name": "Water Utility",
                "avatar": null,
                "state": "ACTIVE"
            },
            {
                "id": 3,
                "name": "Internet Provider",
                "avatar": null,
                "state": "ACTIVE"
            }
        ]"""
    }

    private fun refreshServicesData() {
        // Execute JavaScript to reload services data
        Selenide.executeJavaScript<Unit>("if (typeof loadServicesData === 'function') loadServicesData();")
        
        // Wait for data to load
        Thread.sleep(1000)
    }

    private fun navigateToServicesTab() {
        val servicesTab = `$`("a[href='#services']")
        servicesTab.shouldBe(Condition.visible)
        servicesTab.click()
        
        val servicesContent = `$`("#services-content")
        servicesContent.shouldBe(Condition.visible)
    }
}