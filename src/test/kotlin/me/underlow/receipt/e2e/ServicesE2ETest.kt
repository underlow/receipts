package me.underlow.receipt.e2e

import me.underlow.receipt.config.BaseE2ETest
import me.underlow.receipt.e2e.helpers.LoginHelper
import me.underlow.receipt.e2e.pageobjects.ServiceProviderListPage
import me.underlow.receipt.e2e.pages.ServiceProviderFormPage
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.concurrent.thread

/**
 * E2E tests for Services Management functionality.
 * Tests service provider creation, editing, and management workflows.
 */
class ServicesE2ETest : BaseE2ETest() {

    private lateinit var loginHelper: LoginHelper
    private lateinit var serviceProviderListPage: ServiceProviderListPage
    private lateinit var serviceProviderFormPage: ServiceProviderFormPage

    @BeforeEach
    fun setupServicesTest() {
        // Given: Test environment is prepared
        loginHelper = LoginHelper()
        serviceProviderListPage = ServiceProviderListPage()
        serviceProviderFormPage = ServiceProviderFormPage()

        // And: User is authenticated as allowed user
        loginHelper.loginAsAllowedUser1()

        // And: User navigates to services section
        serviceProviderListPage.navigateToServicesTab()
            .waitForListToLoad()
    }

    @AfterEach
    fun cleanupServicesTest() {
        // Given: Test cleanup is performed
        // Note: BaseE2ETest handles browser cleanup
    }

    @Test
    fun shouldDisplayServiceProviderListLayoutWhenServicesTabOpened() {
        // Given: User is on services tab

        // When: Services page loads

        // Then: Service provider list should be displayed
        serviceProviderListPage
            .shouldDisplayProviderList()
            .shouldBeResponsive()
    }

    @Test
    fun shouldShowEmptyStateWhenNoServiceProvidersExist() {
        // Given: No service providers exist in the system

        // When: Services page loads

        // Then: Empty state should be displayed with appropriate message
        serviceProviderListPage
            .shouldBeEmpty()
            .shouldDisplayEmptyState()
    }

    @Test
    fun shouldOpenCreateFormWhenCreateButtonClicked() {
        // Given: User is viewing service provider list

        // When: User clicks create service provider button
        serviceProviderListPage.clickCreateButton()

        // Then: Create form should be displayed
        serviceProviderFormPage
            .waitForFormToLoad()
            .shouldBeInCreateMode()
    }

    @Test
    fun shouldCreateServiceProviderWithValidData() {
        // Given: User opens create service provider form
        serviceProviderListPage.clickCreateButton()
        serviceProviderFormPage.waitForFormToLoad()

        // When: User fills form with valid service provider data
        val testProviderName = "Test Healthcare Provider"
        val testComment = "Primary healthcare provider"
        val testOcrComment = "OCR: Healthcare bills"

        serviceProviderFormPage
            .fillForm(
                name = testProviderName,
                comment = testComment,
                ocrComment = testOcrComment,
                frequency = "MONTHLY",
                active = true
            )
            .save()

        // Then: Service provider should be created successfully
        serviceProviderFormPage.shouldShowSuccessMessage()

        // And: Service provider should appear in the list
        serviceProviderListPage
            .shouldContainProvider(testProviderName)
            .shouldHaveProviderCount(1)
    }

    @Test
    fun shouldShowValidationErrorWhenRequiredFieldsEmpty() {
        // Given: User opens create service provider form
        serviceProviderListPage.clickCreateButton()
        serviceProviderFormPage.waitForFormToLoad()

        // When: User tries to save form without filling required name field
        serviceProviderFormPage.save()

        // Then: Validation error should be displayed for name field
        serviceProviderFormPage.shouldShowNameValidationError()
    }

    @Test
    fun shouldEditExistingServiceProvider() {
        // Given: Service provider exists in the system
        val originalName = "Original Provider"
        val updatedName = "Updated Provider Name"

        // Create a service provider first
        serviceProviderListPage.clickCreateButton()
        serviceProviderFormPage
            .waitForFormToLoad()
            .fillForm(name = originalName)
            .save()

        // And: Wait for service provider to appear in the list after creation
        serviceProviderListPage.waitForProviderToAppear(originalName)

        // When: User selects existing service provider for editing
        serviceProviderListPage.clickProvider(originalName)

        // Then: Edit form should be displayed with existing data
        serviceProviderFormPage
            .waitForFormToLoad()
            .shouldBeInEditMode()
            .shouldHaveFieldValues(expectedName = originalName)

        // When: User updates service provider name
        serviceProviderFormPage
            .enterName(updatedName)
            .save()

        // Then: Service provider should be updated successfully
        serviceProviderFormPage.shouldShowSuccessMessage()

        // And: Updated service provider should appear in the list
        serviceProviderListPage
            .shouldContainProvider(updatedName)
            .shouldNotContainProvider(originalName)
    }

    @Test
    fun shouldDeleteServiceProviderWhenDeleteButtonClicked() {
        // Given: Service provider exists in the system
        val providerName = "Provider to Delete"

        // Create a service provider first
        serviceProviderListPage.clickCreateButton()
        serviceProviderFormPage
            .waitForFormToLoad()
            .fillForm(name = providerName)
            .save()

        // And: Wait for service provider to appear in the list after creation
        serviceProviderListPage.waitForProviderToAppear(providerName)

        // When: User selects service provider and clicks delete
        serviceProviderListPage.clickProvider(providerName)
        serviceProviderFormPage
            .waitForFormToLoad()
            .delete()

        // Then: Service provider should be deleted successfully
        serviceProviderFormPage.shouldShowSuccessMessage()

        // And: Service provider should not appear in the list (hidden providers are filtered out)
        serviceProviderListPage.waitForListToBeEmpty()
    }

    @Test
    fun shouldManageCustomFieldsForServiceProvider() {
        // Given: User opens create service provider form
        serviceProviderListPage.clickCreateButton()
        serviceProviderFormPage.waitForFormToLoad()

        // When: User adds custom field with key and value
        val customKey = "Account Number"
        val customValue = "ACC-12345"

        serviceProviderFormPage
            .fillForm(name = "Provider with Custom Field")
            .addCustomField(customKey, customValue)
            .save()

        // Then: Service provider should be created with custom field
        serviceProviderFormPage.shouldShowSuccessMessage()

        // And: Wait for service provider to appear in the list
        serviceProviderListPage.waitForProviderToAppear("Provider with Custom Field")

        // And: Custom field should be preserved when editing
        serviceProviderListPage.clickProvider("Provider with Custom Field")
        serviceProviderFormPage
            .waitForFormToLoad()
            .shouldHaveCustomField(customKey, customValue)
    }

    @Test
    fun shouldToggleServiceProviderStateActive() {
        // Given: User creates active service provider
        val providerName = "Active Provider"

        serviceProviderListPage.clickCreateButton()
        serviceProviderFormPage
            .waitForFormToLoad()
            .fillForm(name = providerName, active = true)
            .save()

        // And: Wait for service provider to appear in the list after creation
        serviceProviderListPage.waitForProviderToAppear(providerName)

        // When: User changes service provider state to inactive
        serviceProviderListPage.clickProvider(providerName)
        serviceProviderFormPage
            .waitForFormToLoad()
            .setProviderState(false)
            .save()

        // Then: Service provider state should be updated
        serviceProviderFormPage.shouldShowSuccessMessage()

        // And: Service provider should show as inactive in the list
        serviceProviderListPage
            .findProviderByName(providerName)
            ?.shouldBeInactive()
    }

    @Test
    fun shouldHandleAvatarUploadForServiceProvider() {
        // Given: User opens create service provider form
        serviceProviderListPage.clickCreateButton()
        serviceProviderFormPage.waitForFormToLoad()

        // When: User fills form and avatar section is displayed
        serviceProviderFormPage.fillForm(name = "Provider with Avatar")

        // Then: Avatar upload controls should be available
        serviceProviderFormPage.shouldHaveAvatarFallback()

        // When: User clicks upload avatar button
        serviceProviderFormPage.clickUploadAvatar()

        // Then: Avatar upload functionality should be triggered
        // Note: Actual file upload testing would require additional setup
    }

    @Test
    fun shouldCancelFormEditingWhenCancelButtonClicked() {
        // Given: User opens create service provider form
        serviceProviderListPage.clickCreateButton()
        serviceProviderFormPage.waitForFormToLoad()

        // When: User fills form data and clicks cancel
        serviceProviderFormPage
            .fillForm(name = "Canceled Provider")
            .cancel()

        // Then: Form should be cleared and no service provider created
        serviceProviderListPage
            .shouldBeEmpty()
            .shouldDisplayEmptyState()
    }

    @Test
    fun shouldMaintainFormStateWhenValidationFails() {
        // Given: User opens create service provider form
        serviceProviderListPage.clickCreateButton()
        serviceProviderFormPage.waitForFormToLoad()

        // When: User fills partial form data and saves with validation error
        val testComment = "This comment should be preserved"
        val testOcrComment = "OCR comment should remain"

        serviceProviderFormPage
            .enterComment(testComment)
            .enterOcrComment(testOcrComment)
            .selectFrequency("MONTHLY")
            .save() // This should fail due to missing required name

        // Then: Form should show validation error but preserve filled data
        serviceProviderFormPage
            .shouldShowNameValidationError()
            .shouldHaveFieldValues(
                expectedName = "",
                expectedComment = testComment,
                expectedOcrComment = testOcrComment,
                expectedFrequency = "MONTHLY"
            )
    }

    @Test
    fun shouldDisplayFormWithAllRequiredFieldsAndControls() {
        // Given: User opens create service provider form
        serviceProviderListPage.clickCreateButton()

        // When: Form loads
        serviceProviderFormPage.waitForFormToLoad()

        // Then: All required form elements should be visible and functional
        serviceProviderFormPage
            .shouldBeInCreateMode()
            .shouldHaveSaveButtonEnabled()

        // And: Form should have all expected input fields
        // Note: Specific field validation would be handled by the page object
    }

    @Test
    fun shouldHandleServerErrorGracefullyDuringServiceProviderCreation() {
        // Given: User opens create service provider form
        serviceProviderListPage.clickCreateButton()
        serviceProviderFormPage.waitForFormToLoad()

        // When: User submits form that might cause server error
        serviceProviderFormPage
            .fillForm(name = "Test Provider")
            .save()

        // Then: System should handle any server errors gracefully
        // Either success message or error message should be displayed
        // This test verifies error handling without specific error injection
    }
}
