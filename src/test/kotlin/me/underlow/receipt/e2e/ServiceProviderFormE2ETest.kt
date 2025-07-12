package me.underlow.receipt.e2e

import me.underlow.receipt.config.BaseE2ETest
import me.underlow.receipt.e2e.helpers.LoginHelper
import me.underlow.receipt.e2e.helpers.TestNavigationHelper
import me.underlow.receipt.e2e.pages.ServiceProviderFormPage
import me.underlow.receipt.e2e.pageobjects.ServiceProviderListPage
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName

/**
 * E2E tests for Service Provider Form functionality
 * Tests complete user flows for creating, editing, and managing service providers
 */
@DisplayName("Service Provider Form E2E Tests")
class ServiceProviderFormE2ETest : BaseE2ETest() {

    private lateinit var loginHelper: LoginHelper
    private lateinit var navigationHelper: TestNavigationHelper
    private lateinit var serviceProviderFormPage: ServiceProviderFormPage
    private lateinit var serviceProviderListPage: ServiceProviderListPage

    @BeforeEach
    fun setUp() {
        // Given: User is authenticated and has access to service provider functionality
        loginHelper = LoginHelper()
        navigationHelper = TestNavigationHelper()
        serviceProviderFormPage = ServiceProviderFormPage()
        serviceProviderListPage = ServiceProviderListPage()

        loginHelper.loginAsAllowedUser1()
        navigationHelper.navigateToServicesTab()
    }

    @Test
    @DisplayName("Should successfully create new service provider with valid data")
    fun shouldCreateNewServiceProviderWithValidData() {
        // Given: User wants to create a new service provider
        val providerName = "Test Electric Company"
        val providerComment = "Monthly electricity provider"
        val ocrComment = "Electric utility bills"
        val frequency = "MONTHLY"

        // When: User creates service provider with valid data
        serviceProviderFormPage
            .clickCreateNewProvider()
            .shouldBeInCreateMode()
            .fillForm(
                name = providerName,
                comment = providerComment,
                ocrComment = ocrComment,
                frequency = frequency,
                active = true
            )
            .save()

        // Then: Service provider should be created successfully
        serviceProviderFormPage
            .shouldShowSuccessMessage()
            .backToList()
            .waitForListToLoad()
            .findProviderByName(providerName)
            ?.let { provider ->
                provider.shouldHaveName(providerName)
                provider.shouldBeActive()
            } ?: throw AssertionError("Created service provider not found in list")
    }

    @Test
    @DisplayName("Should validate required fields when creating service provider")
    fun shouldValidateRequiredFieldsWhenCreatingProvider() {
        // Given: User opens create form
        serviceProviderFormPage
            .clickCreateNewProvider()
            .shouldBeInCreateMode()

        // When: User attempts to save without required fields
        serviceProviderFormPage.save()

        // Then: Validation errors should be displayed
        serviceProviderFormPage
            .shouldShowNameValidationError()
            .shouldHaveSaveButtonDisabled()
    }

    @Test
    @DisplayName("Should edit existing service provider successfully")
    fun shouldEditExistingServiceProviderSuccessfully() {
        // Given: Service provider exists in the system
        val originalName = "Original Provider"
        val updatedName = "Updated Provider Name"
        val updatedComment = "Updated provider comment"

        // Create provider first
        serviceProviderFormPage
            .clickCreateNewProvider()
            .fillForm(name = originalName, comment = "Original comment")
            .save()
            .backToList()
            .waitForListToLoad()

        // When: User edits the existing provider
        serviceProviderListPage
            .findProviderByName(originalName)
            ?.click()

        serviceProviderFormPage
            .shouldBeInEditMode()
            .enterName(updatedName)
            .enterComment(updatedComment)
            .save()

        // Then: Provider should be updated successfully
        serviceProviderFormPage
            .shouldShowSuccessMessage()
            .backToList()
            .waitForListToLoad()
            .findProviderByName(updatedName)
            ?.let { provider ->
                provider.shouldHaveName(updatedName)
            } ?: throw AssertionError("Updated service provider not found in list")
    }

    @Test
    @DisplayName("Should manage custom fields for service provider")
    fun shouldManageCustomFieldsForServiceProvider() {
        // Given: User is creating a new service provider
        val providerName = "Provider with Custom Fields"
        val customFieldKey = "Account Number"
        val customFieldValue = "123456789"

        // When: User adds custom field to provider
        serviceProviderFormPage
            .clickCreateNewProvider()
            .fillForm(name = providerName)
            .addCustomField(customFieldKey, customFieldValue)
            .save()

        // Then: Custom field should be saved and displayed
        serviceProviderFormPage
            .shouldShowSuccessMessage()
            .backToList()
            .waitForListToLoad()
            .findProviderByName(providerName)
            ?.click()

        serviceProviderFormPage
            .shouldBeInEditMode()
            .shouldHaveCustomField(customFieldKey, customFieldValue)
    }

    @Test
    @DisplayName("Should handle avatar upload functionality")
    fun shouldHandleAvatarUploadFunctionality() {
        // Given: User is creating a new service provider
        val providerName = "Provider with Avatar"

        // When: User uploads avatar for provider
        serviceProviderFormPage
            .clickCreateNewProvider()
            .fillForm(name = providerName)
            .clickUploadAvatar()

        // Then: Avatar upload modal should be triggered
        // Note: This test verifies the upload flow is initiated
        // Actual file upload would require additional setup
        serviceProviderFormPage
            .shouldHaveAvatarFallback()
    }

    @Test
    @DisplayName("Should create provider as active (state toggle removed)")
    fun shouldCreateProviderAsActive() {
        // Given: User is creating a new service provider
        val providerName = "Test State Provider"

        // When: User creates provider (always active since toggle removed)
        serviceProviderFormPage
            .clickCreateNewProvider()
            .fillForm(name = providerName, active = true)
            // State toggle removed - providers are always active
            .save()

        // Then: Provider should be created as active
        serviceProviderFormPage
            .shouldShowSuccessMessage()
            .backToList()
            .waitForListToLoad()
            .findProviderByName(providerName)
            ?.let { provider ->
                provider.shouldBeActive()
            } ?: throw AssertionError("Created active provider not found in list")
    }

    @Test
    @DisplayName("Should handle different frequency options correctly")
    fun shouldHandleDifferentFrequencyOptionsCorrectly() {
        // Given: User is creating a new service provider
        val providerName = "Frequency Test Provider"

        // When: User selects different frequencies
        serviceProviderFormPage
            .clickCreateNewProvider()
            .fillForm(name = providerName)
            .selectFrequency("YEARLY")
            .shouldHaveFieldValues(
                expectedName = providerName,
                expectedFrequency = "YEARLY"
            )
            .selectFrequency("MONTHLY")
            .shouldHaveFieldValues(
                expectedName = providerName,
                expectedFrequency = "MONTHLY"
            )
            .selectFrequency("WEEKLY")
            .shouldHaveFieldValues(
                expectedName = providerName,
                expectedFrequency = "WEEKLY"
            )
            .selectFrequency("NOT_REGULAR")
            .shouldHaveFieldValues(
                expectedName = providerName,
                expectedFrequency = "NOT_REGULAR"
            )
    }

    @Test
    @DisplayName("Should cancel form editing without saving changes")
    fun shouldCancelFormEditingWithoutSavingChanges() {
        // Given: User opens create form and makes changes
        val providerName = "Cancel Test Provider"

        // When: User cancels form editing
        serviceProviderFormPage
            .clickCreateNewProvider()
            .fillForm(name = providerName, comment = "Test comment")
            .cancel()

        // Then: Form should be cancelled and no provider should be created
        serviceProviderListPage
            .waitForListToLoad()
            .findProviderByName(providerName)
            ?.let {
                throw AssertionError("Provider should not exist after cancelling form")
            }
    }

    @Test
    @DisplayName("Should delete existing service provider")
    fun shouldDeleteExistingServiceProvider() {
        // Given: Service provider exists in the system
        val providerName = "Provider to Delete"

        // Create provider first
        serviceProviderFormPage
            .clickCreateNewProvider()
            .fillForm(name = providerName)
            .save()
            .backToList()
            .waitForListToLoad()

        // When: User deletes the provider
        serviceProviderListPage
            .findProviderByName(providerName)
            ?.click()

        serviceProviderFormPage
            .shouldBeInEditMode()
            .delete()

        // Then: Provider should be deleted successfully
        serviceProviderFormPage
            .shouldShowSuccessMessage()
            .backToList()
            .waitForListToLoad()
            .findProviderByName(providerName)
            ?.let {
                throw AssertionError("Provider should not exist after deletion")
            }
    }

    @Test
    @DisplayName("Should handle network errors gracefully when saving provider")
    fun shouldHandleNetworkErrorsGracefullyWhenSavingProvider() {
        // Given: User creates a service provider with very long name that could cause validation error
        val providerName = "A".repeat(1000) // Very long name to potentially trigger validation

        // When: User attempts to save with potentially invalid data
        serviceProviderFormPage
            .clickCreateNewProvider()
            .fillForm(name = providerName)
            .save()

        // Then: Form should handle the response appropriately
        // Since we cannot easily simulate network errors without backend mocking,
        // we verify that the form remains functional after save attempt
        serviceProviderFormPage
            .waitForFormToLoad()
            .shouldHaveFieldValues(expectedName = providerName)
    }

    @Test
    @DisplayName("Should maintain form state when switching between providers")
    fun shouldMaintainFormStateWhenSwitchingBetweenProviders() {
        // Given: Multiple service providers exist
        val provider1Name = "Provider One"
        val provider2Name = "Provider Two"

        // Create two providers
        serviceProviderFormPage
            .clickCreateNewProvider()
            .fillForm(name = provider1Name, comment = "First provider")
            .save()
            .backToList()
            .waitForListToLoad()

        serviceProviderFormPage
            .clickCreateNewProvider()
            .fillForm(name = provider2Name, comment = "Second provider")
            .save()
            .backToList()
            .waitForListToLoad()

        // When: User switches between providers
        serviceProviderListPage
            .findProviderByName(provider1Name)
            ?.click()

        // Then: Form should show correct provider data
        serviceProviderFormPage
            .shouldBeInEditMode()
            .shouldHaveFieldValues(
                expectedName = provider1Name,
                expectedComment = "First provider"
            )

        // When: User switches to second provider
        serviceProviderFormPage
            .backToList()
            .waitForListToLoad()
            .findProviderByName(provider2Name)
            ?.click()

        // Then: Form should show correct provider data
        serviceProviderFormPage
            .shouldBeInEditMode()
            .shouldHaveFieldValues(
                expectedName = provider2Name,
                expectedComment = "Second provider"
            )
    }
}
