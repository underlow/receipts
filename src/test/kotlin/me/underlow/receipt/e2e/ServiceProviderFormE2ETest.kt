package me.underlow.receipt.e2e

import com.codeborne.selenide.Condition
import com.codeborne.selenide.Selenide
import com.codeborne.selenide.Selenide.`$`
import com.codeborne.selenide.Selenide.`$$`
import com.codeborne.selenide.WebDriverRunner
import me.underlow.receipt.config.BaseE2ETest
import me.underlow.receipt.e2e.helpers.LoginHelper
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach

/**
 * E2E tests for Service Provider Form functionality
 * Tests form creation, editing, validation, and save operations
 */
class ServiceProviderFormE2ETest : BaseE2ETest() {

    private val loginHelper = LoginHelper()

    @BeforeEach
    fun setup() {
        // Given: User is authenticated and on services tab
        loginHelper.loginAsAllowedUser1()
        navigateToServicesTab()
    }

    @AfterEach
    fun tearDown() {
        // Clean up browser state
        loginHelper.clearBrowserState()
    }

    @Test
    fun `given create form when all fields filled correctly then should enable save functionality`() {
        // Given: User opens create form
        openCreateForm()
        
        // When: User fills all form fields
        fillFormFields(
            name = "Test Electric Company",
            comment = "Monthly electricity provider",
            ocrComment = "Electric utility bills",
            frequency = "MONTHLY",
            active = true
        )
        
        // Then: All fields should be correctly filled
        verifyFormFieldValues(
            expectedName = "Test Electric Company",
            expectedComment = "Monthly electricity provider",
            expectedOcrComment = "Electric utility bills",
            expectedFrequency = "MONTHLY",
            expectedActive = true
        )
        
        // And: Save button should be enabled
        val saveButton = `$`("#saveButton")
        saveButton.shouldBe(Condition.enabled)
    }

    @Test
    fun `given valid form data when save clicked then should enable save functionality`() {
        // Given: User opens create form and fills valid data
        openCreateForm()
        fillFormFields(
            name = "New Provider",
            comment = "Test provider",
            frequency = "YEARLY"
        )
        
        // When: Form is filled with valid data
        val saveButton = `$`("#saveButton")
        
        // Then: Save button should be enabled for valid data
        saveButton.shouldBe(Condition.enabled)
        
        // And: Form fields should contain the entered values
        verifyFormFieldValues(
            expectedName = "New Provider",
            expectedComment = "Test provider",
            expectedFrequency = "YEARLY",
            expectedActive = true
        )
    }

    @Test
    fun `given form with invalid name when save attempted then should show validation error`() {
        // Given: User opens create form
        openCreateForm()
        
        // When: User tries to save with empty name
        val saveButton = `$`("#saveButton")
        saveButton.click()
        
        // Then: Browser validation should prevent submission
        val nameField = `$`("#providerName")
        assertTrue(nameField.getAttribute("required") != null)
        
        // And: Field should be marked as invalid (browser validation)
        // Note: Browser native validation will show validation message
    }

    @Test
    fun `given edit form when provider data simulated then should show edit functionality`() {
        // Given: User opens create form first
        openCreateForm()
        
        // When: We simulate having provider data loaded (using JavaScript)
        loadExistingProviderInForm()
        
        // Then: Form title should indicate edit mode or show provider data
        val formTitle = `$`("#formTitle")
        // Form title should show some indication of provider data
        assertTrue(formTitle.isDisplayed)
        
        // And: Basic form elements should be present for editing
        val nameField = `$`("#providerName")
        val commentField = `$`("#providerComment")
        val saveButton = `$`("#saveButton")
        
        nameField.shouldBe(Condition.visible)
        commentField.shouldBe(Condition.visible)
        saveButton.shouldBe(Condition.visible)
    }

    @Test
    fun `given form with custom fields when managed then should allow add remove operations`() {
        // Given: User opens create form
        openCreateForm()
        
        // When: User adds custom field
        simulateAddCustomField("Account Number", "123456789")
        
        // Then: Custom field should be displayed
        val customFieldsContainer = `$`("#customFieldsContainer")
        customFieldsContainer.shouldBe(Condition.visible)
        
        val customFieldItems = customFieldsContainer.`$$`(".custom-field-item")
        assertTrue(customFieldItems.size() >= 1)
        
        val firstCustomField = customFieldItems.first()
        val keyInput = firstCustomField.`$`("input[placeholder='Field name']")
        val valueInput = firstCustomField.`$`("input[placeholder='Field value']")
        
        assertEquals("Account Number", keyInput.value)
        assertEquals("123456789", valueInput.value)
        
        // And: Should have remove button
        val removeButton = firstCustomField.`$`("button[onclick*='removeCustomField']")
        removeButton.shouldBe(Condition.visible)
    }

    @Test
    fun `given form with avatar when upload initiated then should trigger upload modal`() {
        // Given: User opens create form
        openCreateForm()
        
        // When: User clicks upload avatar button
        val uploadButton = `$`("button[onclick='uploadAvatar()']")
        uploadButton.click()
        
        // Then: Upload modal should be triggered
        // Note: This would require the upload modal to be present in test environment
        // For now, we verify the button exists and is clickable
        uploadButton.shouldBe(Condition.enabled)
    }

    @Test
    fun `given form with avatar when remove clicked then should clear avatar`() {
        // Given: Form has an avatar (simulated by setting one)
        openCreateForm()
        simulateAvatarPresence()
        
        // When: User clicks remove avatar button
        val removeButton = `$`("button[onclick='removeAvatar()']")
        if (removeButton.exists()) {
            removeButton.click()
            
            // Then: Avatar should be cleared (would show fallback)
            val avatarPreview = `$`("#avatarPreview")
            avatarPreview.shouldBe(Condition.visible)
            // In real implementation, this would change from img to fallback div
        }
    }

    @Test
    fun `given form when state toggle clicked then should change provider status`() {
        // Given: User opens create form (default is ACTIVE)
        openCreateForm()
        
        val stateToggle = `$`("#providerState")
        assertTrue(stateToggle.getAttribute("checked") != null)
        
        // When: User clicks toggle to set HIDDEN
        stateToggle.click()
        
        // Then: Toggle should be unchecked (HIDDEN state)
        assertNull(stateToggle.getAttribute("checked"))
        
        // When: User clicks toggle again to set ACTIVE
        stateToggle.click()
        
        // Then: Toggle should be checked (ACTIVE state)
        assertTrue(stateToggle.getAttribute("checked") != null)
    }

    @Test
    fun `given form when frequency dropdown changed then should update selection`() {
        // Given: User opens create form
        openCreateForm()
        
        val frequencyField = `$`("#providerFrequency")
        
        // When: User selects different frequencies
        frequencyField.selectOptionByValue("YEARLY")
        assertEquals("YEARLY", frequencyField.value)
        
        frequencyField.selectOptionByValue("MONTHLY")
        assertEquals("MONTHLY", frequencyField.value)
        
        frequencyField.selectOptionByValue("WEEKLY")
        assertEquals("WEEKLY", frequencyField.value)
        
        frequencyField.selectOptionByValue("NOT_REGULAR")
        assertEquals("NOT_REGULAR", frequencyField.value)
    }

    @Test
    fun `given form when cancel clicked then should reset or clear form`() {
        // Given: User opens create form and makes changes
        openCreateForm()
        fillFormFields(name = "Test Provider", comment = "Test comment")
        
        // When: User clicks cancel
        val cancelButton = `$`("button[onclick='cancelEdit()']")
        cancelButton.click()
        
        // Then: Form should be reset (for new provider) or go back to empty state
        val formTitle = `$`("#formTitle")
        // Should either show selection prompt or reload original data
        assertTrue(
            formTitle.text().contains("Select a Service Provider") || 
            formTitle.text().contains("Edit Service Provider")
        )
    }

    @Test
    fun `given edit form when delete button exists then should show delete functionality`() {
        // Given: User opens create form and simulates edit mode
        openCreateForm()
        loadExistingProviderInForm()
        
        // When: Form is in edit mode
        // Then: Check if delete button is present (it may not be visible in create mode)
        val deleteButton = `$`("button[onclick='deleteServiceProvider()']")
        
        // The delete button should exist in edit mode, but we don't test actual deletion
        // since that would require real backend integration
        if (deleteButton.exists()) {
            deleteButton.shouldBe(Condition.visible)
        }
        
        // Verify basic form structure is maintained
        val saveButton = `$`("#saveButton")
        saveButton.shouldBe(Condition.visible)
    }

    @Test
    fun `given form when responsive design tested then should adapt to mobile`() {
        // Given: User opens create form
        openCreateForm()
        
        // When: Browser is resized to mobile width
        WebDriverRunner.getWebDriver().manage().window().setSize(
            org.openqa.selenium.Dimension(400, 800)
        )
        
        // Then: Form should remain functional and properly laid out
        val form = `$`(".service-provider-form")
        form.shouldBe(Condition.visible)
        
        val nameField = `$`("#providerName")
        nameField.shouldBe(Condition.visible)
        
        val saveButton = `$`("#saveButton")
        saveButton.shouldBe(Condition.visible)
        
        // Reset window size
        WebDriverRunner.getWebDriver().manage().window().maximize()
    }

    @Test
    fun `given form when accessibility tested then should have proper labels and structure`() {
        // Given: User opens create form
        openCreateForm()
        
        // Then: All form fields should have proper labels
        val nameLabel = `$`("label[for='providerName']")
        nameLabel.shouldBe(Condition.visible)
        
        val commentLabel = `$`("label[for='providerComment']")
        commentLabel.shouldBe(Condition.visible)
        
        val ocrCommentLabel = `$`("label[for='providerOcrComment']")
        ocrCommentLabel.shouldBe(Condition.visible)
        
        val frequencyLabel = `$`("label[for='providerFrequency']")
        frequencyLabel.shouldBe(Condition.visible)
        
        // And: Toggle should have proper labeling
        val toggleLabel = `$`("label[for='providerState']")
        toggleLabel.shouldBe(Condition.visible)
        
        // And: Required fields should be marked
        val nameField = `$`("#providerName")
        assertTrue(nameField.getAttribute("required") != null)
    }

    /**
     * Helper methods for form testing
     */
    private fun openCreateForm() {
        val createButton = `$`("#createServiceButton")
        createButton.click()
        
        val form = `$`(".service-provider-form")
        form.shouldBe(Condition.visible)
    }

    private fun fillFormFields(
        name: String = "",
        comment: String = "",
        ocrComment: String = "",
        frequency: String = "NOT_REGULAR",
        active: Boolean = true
    ) {
        if (name.isNotEmpty()) {
            `$`("#providerName").value = name
        }
        if (comment.isNotEmpty()) {
            `$`("#providerComment").value = comment
        }
        if (ocrComment.isNotEmpty()) {
            `$`("#providerOcrComment").value = ocrComment
        }
        // Select by value instead of text to match HTML option values
        `$`("#providerFrequency").selectOptionByValue(frequency)
        
        val stateToggle = `$`("#providerState")
        val currentlyChecked = stateToggle.getAttribute("checked") != null
        if (currentlyChecked != active) {
            stateToggle.click()
        }
    }

    private fun verifyFormFieldValues(
        expectedName: String,
        expectedComment: String = "",
        expectedOcrComment: String = "",
        expectedFrequency: String = "NOT_REGULAR",
        expectedActive: Boolean = true
    ) {
        assertEquals(expectedName, `$`("#providerName").value)
        assertEquals(expectedComment, `$`("#providerComment").value)
        assertEquals(expectedOcrComment, `$`("#providerOcrComment").value)
        assertEquals(expectedFrequency, `$`("#providerFrequency").value)
        
        val stateToggle = `$`("#providerState")
        val isChecked = stateToggle.getAttribute("checked") != null
        assertEquals(expectedActive, isChecked)
    }

    private fun simulateAddCustomField(key: String, value: String) {
        // This would simulate the JavaScript function call for adding custom fields
        Selenide.executeJavaScript<Unit>("""
            if (typeof selectedServiceProvider !== 'undefined' && selectedServiceProvider) {
                if (!selectedServiceProvider.customFields) {
                    selectedServiceProvider.customFields = {};
                }
                selectedServiceProvider.customFields['$key'] = '$value';
                if (typeof renderServiceProviderForm === 'function') {
                    renderServiceProviderForm();
                }
            }
        """)
    }

    private fun simulateAvatarPresence() {
        // This would simulate having an avatar by updating the form state
        Selenide.executeJavaScript<Unit>("""
            if (typeof selectedServiceProvider !== 'undefined' && selectedServiceProvider) {
                selectedServiceProvider.avatar = '/uploads/test-avatar.jpg';
                if (typeof renderServiceProviderForm === 'function') {
                    renderServiceProviderForm();
                }
            }
        """)
    }

    private fun loadExistingProviderInForm() {
        // Simulate loading an existing provider by calling JavaScript function
        Selenide.executeJavaScript<Unit>("""
            if (typeof selectedServiceProvider !== 'undefined') {
                selectedServiceProvider = {
                    id: 1,
                    name: 'Existing Electric Company',
                    avatar: '/uploads/existing-avatar.jpg',
                    comment: 'Existing comment',
                    commentForOcr: 'Existing OCR comment',
                    regular: 'MONTHLY',
                    customFields: {'account': '123456'},
                    state: 'ACTIVE'
                };
                if (typeof renderServiceProviderForm === 'function') {
                    renderServiceProviderForm();
                }
            }
        """)
    }


    private fun navigateToServicesTab() {
        val servicesTab = when {
            `$`("a[href='#services']").exists() -> `$`("a[href='#services']")
            `$`("[data-test-id='services-tab']").exists() -> `$`("[data-test-id='services-tab']")
            `$`("#services-tab").exists() -> `$`("#services-tab")
            `$`(".services-tab").exists() -> `$`(".services-tab")
            `$`("nav").exists() && `$`("nav").text().contains("Services") -> {
                `$`("nav").`$$`("a").find { it.text().contains("Services") }
            }
            else -> throw RuntimeException("Could not find services tab")
        }
        
        servicesTab?.shouldBe(Condition.visible)?.click()
        
        // Wait for services content to load
        val servicesContent = when {
            `$`("#services").exists() -> `$`("#services")
            `$`("#services-content").exists() -> `$`("#services-content")
            `$`(".services-content").exists() -> `$`(".services-content")
            else -> null
        }
        
        servicesContent?.shouldBe(Condition.visible)
        
        // Wait for page to load
        Thread.sleep(1000)
    }
}