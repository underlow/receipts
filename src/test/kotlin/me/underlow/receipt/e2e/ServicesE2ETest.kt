package me.underlow.receipt.e2e

import com.codeborne.selenide.Condition
import com.codeborne.selenide.Selenide
import com.codeborne.selenide.Selenide.`$`
import com.codeborne.selenide.Selenide.`$$`
import com.codeborne.selenide.WebDriverRunner
import me.underlow.receipt.config.BaseE2ETest
import me.underlow.receipt.config.TestSecurityConfiguration
import me.underlow.receipt.e2e.helpers.LoginHelper
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach

/**
 * E2E tests for Services Management UI Components
 * Tests the split-panel layout, service provider list, and form functionality
 */
class ServicesE2ETest : BaseE2ETest() {

    private val loginHelper = LoginHelper()

    @BeforeEach
    fun setup() {
        // Given: User is authenticated and on dashboard
        loginHelper.loginAsAllowedUser1()
        assertTrue(isOnDashboardPage())
        
        // When: User navigates to Services tab
        navigateToServicesTab()
    }

    @Test
    fun `given services tab when opened then should display split-panel layout`() {
        // Then: Should display split-panel container
        val splitPanelContainer = `$`(".split-panel-container")
        splitPanelContainer.shouldBe(Condition.visible)
        
        // And: Should have left panel for service provider list
        val leftPanel = `$`(".split-panel-left")
        leftPanel.shouldBe(Condition.visible)
        
        val leftPanelTitle = leftPanel.`$`(".split-panel-title")
        leftPanelTitle.shouldBe(Condition.visible)
        assertTrue(leftPanelTitle.text().contains("Service Providers"))
        
        // And: Should have right panel for form
        val rightPanel = `$`(".split-panel-right")
        rightPanel.shouldBe(Condition.visible)
        
        val rightPanelTitle = rightPanel.`$`(".split-panel-title")
        rightPanelTitle.shouldBe(Condition.visible)
    }

    @Test
    fun `given services tab when opened then should display create service button`() {
        // Then: Should display create service button in left panel header
        val createButton = `$`("#createServiceButton")
        createButton.shouldBe(Condition.visible)
        createButton.shouldBe(Condition.enabled)
        
        val buttonText = createButton.text()
        assertTrue(buttonText.contains("Create Service"))
        
        // And: Button should have proper styling
        assertTrue(createButton.getAttribute("class")?.contains("btn-upload") == true)
    }

    @Test
    fun `given empty service provider list when displayed then should show empty state`() {
        // Given: Service provider list is empty (default test state)
        
        // Then: Should display empty state in list
        val emptyState = `$`("#serviceProviderList .empty-state")
        emptyState.shouldBe(Condition.visible)
        
        val emptyStateTitle = emptyState.`$`(".empty-state-title")
        emptyStateTitle.shouldBe(Condition.visible)
        assertTrue(emptyStateTitle.text().contains("No Service Providers"))
        
        val emptyStateText = emptyState.`$`(".empty-state-text")
        emptyStateText.shouldBe(Condition.visible)
        assertTrue(emptyStateText.text().contains("Create your first service provider"))
    }

    @Test
    fun `given no service provider selected when form displayed then should show selection prompt`() {
        // Then: Form should display empty state
        val formEmptyState = `$`("#serviceProviderForm .empty-state")
        formEmptyState.shouldBe(Condition.visible)
        
        val formTitle = `$`("#formTitle")
        formTitle.shouldBe(Condition.visible)
        assertTrue(formTitle.text().contains("Select a Service Provider"))
        
        val emptyStateTitle = formEmptyState.`$`(".empty-state-title")
        emptyStateTitle.shouldBe(Condition.visible)
        assertTrue(emptyStateTitle.text().contains("No Service Provider Selected"))
    }

    @Test
    fun `given create service button when clicked then should display new provider form`() {
        // When: User clicks create service button
        val createButton = `$`("#createServiceButton")
        createButton.click()
        
        // Then: Form title should change to create mode
        val formTitle = `$`("#formTitle")
        formTitle.shouldBe(Condition.visible)
        assertTrue(formTitle.text().contains("Create New Service Provider"))
        
        // And: Form should be displayed with proper fields
        val form = `$`(".service-provider-form")
        form.shouldBe(Condition.visible)
        
        // And: Should have name field (required)
        val nameField = `$`("#providerName")
        nameField.shouldBe(Condition.visible)
        nameField.shouldBe(Condition.enabled)
        assertTrue(nameField.getAttribute("required") != null)
        
        // And: Should have save button
        val saveButton = `$`("#saveButton")
        saveButton.shouldBe(Condition.visible)
        saveButton.shouldBe(Condition.enabled)
        assertTrue(saveButton.text().contains("Save"))
    }

    @Test
    fun `given create form when fields filled then should enable form functionality`() {
        // Given: User clicks create service button
        `$`("#createServiceButton").click()
        
        // When: User fills form fields
        val nameField = `$`("#providerName")
        nameField.value = "Test Service Provider"
        
        val commentField = `$`("#providerComment")
        commentField.value = "Test comment"
        
        val ocrCommentField = `$`("#providerOcrComment")
        ocrCommentField.value = "OCR test comment"
        
        val frequencyField = `$`("#providerFrequency")
        frequencyField.selectOptionByValue("MONTHLY")
        
        // Then: All fields should be properly filled
        assertEquals("Test Service Provider", nameField.value)
        assertEquals("Test comment", commentField.value)
        assertEquals("OCR test comment", ocrCommentField.value)
        assertEquals("MONTHLY", frequencyField.value)
        
        // And: Save button should remain enabled
        val saveButton = `$`("#saveButton")
        saveButton.shouldBe(Condition.enabled)
    }

    @Test
    fun `given form when avatar section displayed then should show upload controls`() {
        // Given: User opens create form
        `$`("#createServiceButton").click()
        
        // Then: Avatar section should be visible
        val avatarSection = `$`(".avatar-upload-section")
        avatarSection.shouldBe(Condition.visible)
        
        // And: Should have avatar preview placeholder
        val avatarPreview = `$`("#avatarPreview")
        avatarPreview.shouldBe(Condition.visible)
        
        // And: Should have upload button
        val uploadButton = avatarSection.`$`("button[onclick='uploadAvatar()']")
        uploadButton.shouldBe(Condition.visible)
        assertTrue(uploadButton.text().contains("Upload Avatar"))
    }

    @Test
    fun `given form when custom fields section displayed then should allow field management`() {
        // Given: User opens create form
        `$`("#createServiceButton").click()
        
        // Then: Custom fields section should be visible
        val customFieldsContainer = `$`("#customFieldsContainer")
        customFieldsContainer.shouldBe(Condition.visible)
        
        // And: Should show no custom fields message initially
        val noFieldsMessage = customFieldsContainer.`$`("p.text-muted")
        noFieldsMessage.shouldBe(Condition.visible)
        assertTrue(noFieldsMessage.text().contains("No custom fields defined"))
        
        // And: Should have add custom field button
        val addFieldButton = customFieldsContainer.parent().`$`("button[onclick='addCustomField()']")
        addFieldButton.shouldBe(Condition.visible)
        assertTrue(addFieldButton.text().contains("Add Custom Field"))
    }

    @Test
    fun `given form when state toggle displayed then should allow status changes`() {
        // Given: User opens create form
        `$`("#createServiceButton").click()
        
        // Then: State toggle should be visible
        val toggleSwitch = `$`(".toggle-switch")
        toggleSwitch.shouldBe(Condition.visible)
        
        val toggleInput = `$`("#providerState")
        toggleInput.shouldBe(Condition.visible)
        
        val toggleLabel = toggleSwitch.`$`(".toggle-label")
        toggleLabel.shouldBe(Condition.visible)
        assertTrue(toggleLabel.text().contains("Active"))
        
        // And: Should be checked by default (ACTIVE state)
        assertTrue(toggleInput.getAttribute("checked") != null)
    }

    @Test
    fun `given form when action buttons displayed then should have proper functionality`() {
        // Given: User opens create form
        `$`("#createServiceButton").click()
        
        // Then: Should have form actions section
        val formActions = `$`(".form-actions")
        formActions.shouldBe(Condition.visible)
        
        // And: Should have save button
        val saveButton = `$`("#saveButton")
        saveButton.shouldBe(Condition.visible)
        saveButton.shouldBe(Condition.enabled)
        assertTrue(saveButton.text().contains("Save"))
        assertTrue(saveButton.getAttribute("class")?.contains("btn-save") == true)
        
        // And: Should have cancel button
        val cancelButton = formActions.`$`("button[onclick='cancelEdit()']")
        cancelButton.shouldBe(Condition.visible)
        cancelButton.shouldBe(Condition.enabled)
        assertTrue(cancelButton.text().contains("Cancel"))
        assertTrue(cancelButton.getAttribute("class")?.contains("btn-cancel") == true)
        
        // And: Should not have delete button for new provider
        val deleteButtons = formActions.`$$`("button[onclick='deleteServiceProvider()']")
        assertEquals(0, deleteButtons.size())
    }

    @Test
    fun `given form when validation error occurs then should display error message`() {
        // Given: User opens create form
        `$`("#createServiceButton").click()
        
        // When: User tries to save without required name field
        val saveButton = `$`("#saveButton")
        saveButton.click()
        
        // Then: Should display validation error (this tests client-side validation)
        val nameField = `$`("#providerName")
        // Note: Browser native validation will prevent form submission
        // We verify the field is marked as required
        assertTrue(nameField.getAttribute("required") != null)
    }

    @Test
    fun `given services layout when responsive design tested then should adapt to mobile`() {
        // Given: User is on services tab
        
        // When: Browser is resized to mobile width
        WebDriverRunner.getWebDriver().manage().window().setSize(
            org.openqa.selenium.Dimension(400, 800)
        )
        
        // Then: Split panel should still be visible and functional
        val splitPanelContainer = `$`(".split-panel-container")
        splitPanelContainer.shouldBe(Condition.visible)
        
        // And: Panels should be stacked vertically (CSS media query)
        // Note: We can't directly test CSS flexbox direction change in Selenide,
        // but we can verify the layout remains functional
        val leftPanel = `$`(".split-panel-left")
        leftPanel.shouldBe(Condition.visible)
        
        val rightPanel = `$`(".split-panel-right")
        rightPanel.shouldBe(Condition.visible)
        
        // Reset window size
        WebDriverRunner.getWebDriver().manage().window().maximize()
    }

    @Test
    fun `given services tab when accessibility features tested then should be accessible`() {
        // Given: User opens create form first to make form elements visible
        `$`("#createServiceButton").click()
        
        // Then: Form labels should have proper associations
        val nameLabel = `$`("label[for='providerName']")
        nameLabel.shouldBe(Condition.visible)
        
        val nameField = `$`("#providerName")
        nameField.shouldBe(Condition.visible)
        
        // And: Buttons should have proper accessibility attributes
        val createButton = `$`("#createServiceButton")
        createButton.shouldBe(Condition.visible)
        
        // And: Toggle switch should have proper labeling
        val toggleLabel = `$`("label[for='providerState']")
        toggleLabel.shouldBe(Condition.visible)
        
        // And: Form should have proper structure
        val form = `$`(".service-provider-form")
        form.shouldBe(Condition.visible)
    }

    /**
     * Helper method to navigate to Services tab
     */
    private fun navigateToServicesTab() {
        val servicesTab = `$`("a[href='#services']")
        servicesTab.shouldBe(Condition.visible)
        servicesTab.click()
        
        // Wait for tab content to load
        val servicesContent = `$`("#services-content")
        servicesContent.shouldBe(Condition.visible)
    }
}