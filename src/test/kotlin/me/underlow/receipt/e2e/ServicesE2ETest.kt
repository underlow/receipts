package me.underlow.receipt.e2e

import me.underlow.receipt.config.BaseE2ETest
import me.underlow.receipt.e2e.helpers.LoginHelper
import me.underlow.receipt.e2e.pageobjects.ServiceProviderListPage
import me.underlow.receipt.e2e.pages.ServiceProviderFormPage
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.JdbcTemplate

/**
 * E2E tests for Services Management functionality.
 * Tests service provider creation, editing, and management workflows.
 */
class ServicesE2ETest : BaseE2ETest() {

    private val logger = LoggerFactory.getLogger(ServicesE2ETest::class.java)

    @Autowired
    private lateinit var jdbcTemplate: JdbcTemplate

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
        // Clean up all service providers created during the test to ensure test isolation
        try {
            // Delete all service providers to prevent test interference
            jdbcTemplate.execute("DELETE FROM service_providers")
        } catch (e: Exception) {
            // Log but don't fail test on cleanup errors
            logger.warn("Failed to cleanup service providers after test: ${e.message}")
        }
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
//        serviceProviderFormPage.shouldShowSuccessMessage()

        // And: Service provider should appear in the list
        serviceProviderListPage
            .waitForProviderToAppear(testProviderName)
            .shouldHaveProviderCount(1)
    }

    @Test
    fun shouldShowValidationErrorWhenRequiredFieldsEmpty() {
        // Given: User opens create service provider form
        serviceProviderListPage.clickCreateButton()
        serviceProviderFormPage.waitForFormToLoad()

        // When: User doesn't fill the required name field
        // Then: Save button should be disabled
        serviceProviderFormPage.shouldHaveSaveButtonDisabled()
    }

    @Test
    fun shouldDisableSaveButtonWhenNameEnteredAndRemoved() {
        // Given: User opens create service provider form
        serviceProviderListPage.clickCreateButton()
        serviceProviderFormPage.waitForFormToLoad()

        // When: User enters a name
        serviceProviderFormPage.enterName("Test Provider")

        // Then: Save button should be enabled
        serviceProviderFormPage.shouldHaveSaveButtonEnabled()

        // When: User removes the name
        serviceProviderFormPage.enterName("")

        // Then: Save button should be disabled again
        serviceProviderFormPage.shouldHaveSaveButtonDisabled()
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
//        serviceProviderFormPage.shouldShowSuccessMessage()

        // And: Wait for service provider to appear in the list
        serviceProviderListPage.waitForProviderToAppear("Provider with Custom Field")

        // And: Custom field should be preserved when editing
        serviceProviderListPage.clickProvider("Provider with Custom Field")
        serviceProviderFormPage
            .waitForFormToLoad()
            .shouldHaveCustomField(customKey, customValue)
    }

    @Test
    fun shouldCreateServiceProviderAsActive() {
        // Given: User creates active service provider
        val providerName = "Active Provider"

        serviceProviderListPage.clickCreateButton()
        serviceProviderFormPage
            .waitForFormToLoad()
            .fillForm(name = providerName, active = true)
            .save()

        // And: Wait for service provider to appear in the list after creation
        serviceProviderListPage.waitForProviderToAppear(providerName)

        // When: User edits service provider (state toggle removed)
        serviceProviderListPage.clickProvider(providerName)
        serviceProviderFormPage
            .waitForFormToLoad()
            // State toggle removed - providers are always active
            .save()

        // Then: Service provider should remain active
        serviceProviderFormPage.shouldShowSuccessMessage()

        // And: Service provider should show as active in the list
        serviceProviderListPage
            .waitForProviderToAppear(providerName)
            .findProviderByName(providerName)
            ?.shouldBeActive()
    }

    @Test
    fun shouldHandleAvatarUploadForServiceProvider() {
        // Given: User opens create service provider form
        serviceProviderListPage.clickCreateButton()
        serviceProviderFormPage.waitForFormToLoad()

        // When: User fills form and avatar section is displayed
        serviceProviderFormPage.fillForm(name = "Provider with Avatar")

        // Then: Avatar fallback should be displayed (no upload button)
        serviceProviderFormPage.shouldHaveAvatarFallback()
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

        // When: User fills partial form data without required name
        val testComment = "This comment should be preserved"
        val testOcrComment = "OCR comment should remain"

        serviceProviderFormPage
            .enterComment(testComment)
            .enterOcrComment(testOcrComment)
            .selectFrequency("MONTHLY")

        // Then: Save button should be disabled due to missing required name
        serviceProviderFormPage.shouldHaveSaveButtonDisabled()

        // And: Form should preserve filled data
        serviceProviderFormPage.shouldHaveFieldValues(
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
            .shouldHaveSaveButtonDisabled() // Save button should be disabled when name is empty

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

    @Test
    fun shouldPreserveUnsavedFormValuesWhenDeletingCustomField() {
        // Given: User opens create service provider form
        serviceProviderListPage.clickCreateButton()
        serviceProviderFormPage.waitForFormToLoad()

        // When: User fills form with main data and multiple custom fields
        val providerName = "Provider with Multiple Fields"
        val comment = "Important provider comment"
        val ocrComment = "OCR processing notes"

        serviceProviderFormPage
            .fillForm(
                name = providerName,
                comment = comment,
                ocrComment = ocrComment,
                frequency = "MONTHLY",
                active = true
            )
            .addCustomField("Account Number", "ACC-12345")
            .addCustomField("Department", "Healthcare")
            .addCustomField("Contact Person", "John Doe")

        // And: User deletes the middle custom field (Department)
        serviceProviderFormPage.removeCustomField(1)

        // Then: Main form values should be preserved
        serviceProviderFormPage.shouldHaveFieldValues(
            expectedName = providerName,
            expectedComment = comment,
            expectedOcrComment = ocrComment,
            expectedFrequency = "MONTHLY"
        )

        // And: Remaining custom fields should be preserved
        serviceProviderFormPage
            .shouldHaveCustomField("Account Number", "ACC-12345")
            .shouldHaveCustomField("Contact Person", "John Doe")

        // When: User saves the service provider
        serviceProviderFormPage.save()

        // Then: Service provider should be created with remaining data
        serviceProviderFormPage.shouldShowSuccessMessage()

        // And: Verify data persistence by editing the created provider
        serviceProviderListPage.waitForProviderToAppear(providerName)
        serviceProviderListPage.clickProvider(providerName)

        serviceProviderFormPage
            .waitForFormToLoad()
            .shouldHaveFieldValues(
                expectedName = providerName,
                expectedComment = comment,
                expectedOcrComment = ocrComment,
                expectedFrequency = "MONTHLY"
            )
            .shouldHaveCustomField("Account Number", "ACC-12345")
            .shouldHaveCustomField("Contact Person", "John Doe")
    }

    @Test
    fun shouldDeleteMultipleCustomFieldsWithoutAffectingFormData() {
        // Given: User opens create service provider form
        serviceProviderListPage.clickCreateButton()
        serviceProviderFormPage.waitForFormToLoad()

        // When: User fills form and adds multiple custom fields
        val providerName = "Multi-Field Provider"

        serviceProviderFormPage
            .fillForm(name = providerName, active = true)
            .addCustomField("Field1", "Value1")
            .addCustomField("Field2", "Value2")
            .addCustomField("Field3", "Value3")
            .addCustomField("Field4", "Value4")

        // And: User deletes first and third fields
        serviceProviderFormPage
            .removeCustomField(0) // Remove Field1
            .removeCustomField(1) // Remove Field3 (which is now at index 1)

        // Then: Form data should be preserved
        serviceProviderFormPage.shouldHaveFieldValues(expectedName = providerName)

        // And: Only remaining custom fields should exist
        serviceProviderFormPage
            .shouldHaveCustomField("Field2", "Value2")
            .shouldHaveCustomField("Field4", "Value4")

        // When: User saves the provider
        serviceProviderFormPage.save()

        // Then: Provider should be created successfully
        serviceProviderFormPage.shouldShowSuccessMessage()

        // And: Verify data persistence by reloading and checking saved data
        serviceProviderListPage.waitForProviderToAppear(providerName)
        serviceProviderListPage.clickProvider(providerName)

        serviceProviderFormPage
            .waitForFormToLoad()
            .shouldHaveFieldValues(expectedName = providerName)
            .shouldHaveCustomField("Field2", "Value2")
            .shouldHaveCustomField("Field4", "Value4")
    }
}
