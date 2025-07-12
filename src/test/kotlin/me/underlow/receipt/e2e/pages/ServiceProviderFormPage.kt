package me.underlow.receipt.e2e.pages

import com.codeborne.selenide.Condition
import com.codeborne.selenide.Selenide.`$`
import com.codeborne.selenide.Selenide.`$$`
import com.codeborne.selenide.SelenideElement
import me.underlow.receipt.e2e.pageobjects.ServiceProviderListPage
import java.time.Duration

/**
 * Page Object Model for Service Provider Form functionality.
 * Encapsulates all UI interactions for creating and editing service providers.
 */
class ServiceProviderFormPage {

    // Form elements using data-test-id attributes
    private val formContainer = `$`("[data-test-id='service-provider-form']")
    private val formTitle = `$`("[data-test-id='form-title']")
    private val nameField = `$`("[data-test-id='provider-name']")
    private val commentField = `$`("[data-test-id='provider-comment']")
    private val ocrCommentField = `$`("[data-test-id='provider-ocr-comment']")
    private val frequencyField = `$`("[data-test-id='provider-frequency']")
    private val avatarPreview = `$`("[data-test-id='avatar-preview']")
    private val uploadAvatarButton = `$`("[data-test-id='upload-avatar-button']")
    private val removeAvatarButton = `$`("[data-test-id='remove-avatar-button']")
    private val customFieldsContainer = `$`("[data-test-id='custom-fields-container']")
    private val addCustomFieldButton = `$`("[data-test-id='add-custom-field-button']")
    private val saveButton = `$`("[data-test-id='save-button']")
    private val cancelButton = `$`("[data-test-id='cancel-button']")
    private val deleteButton = `$`("[data-test-id='delete-button']")
    private val createButton = `$`("[data-test-id='create-service-button']")

    // Error and validation elements
    private val errorMessage = `$`("[data-test-id='error-message']")
    private val nameValidationError = `$`("[data-test-id='name-validation-error']")
    private val successMessage = `$`("[data-test-id='success-message']")

    /**
     * Waits for the form to be visible and ready for interaction
     */
    fun waitForFormToLoad(): ServiceProviderFormPage {
        formContainer.shouldBe(Condition.visible)
        // Wait for form fields to be ready
        nameField.shouldBe(Condition.visible)
        saveButton.shouldBe(Condition.visible)
        return this
    }

    /**
     * Clicks create new service provider button
     */
    fun clickCreateNewProvider(): ServiceProviderFormPage {
        createButton.shouldBe(Condition.visible).click()
        waitForFormToLoad()
        return this
    }

    /**
     * Enters provider name
     */
    fun enterName(name: String): ServiceProviderFormPage {
        nameField.shouldBe(Condition.visible)
        nameField.shouldBe(Condition.enabled)
        nameField.clear()
        nameField.setValue(name)
        // Verify the value was actually set
        nameField.shouldHave(Condition.value(name))
        return this
    }

    /**
     * Enters provider comment
     */
    fun enterComment(comment: String): ServiceProviderFormPage {
        commentField.shouldBe(Condition.visible).clear()
        commentField.setValue(comment)
        return this
    }

    /**
     * Enters OCR comment
     */
    fun enterOcrComment(ocrComment: String): ServiceProviderFormPage {
        ocrCommentField.shouldBe(Condition.visible).clear()
        ocrCommentField.setValue(ocrComment)
        return this
    }

    /**
     * Selects frequency from dropdown
     */
    fun selectFrequency(frequency: String): ServiceProviderFormPage {
        frequencyField.shouldBe(Condition.visible).selectOptionByValue(frequency)
        return this
    }

    /**
     * Sets provider state (active/inactive)
     * Note: State toggle has been removed from UI - providers are always active
     */
    fun setProviderState(active: Boolean): ServiceProviderFormPage {
        // State toggle functionality removed - providers are always active
        return this
    }

    /**
     * Clicks upload avatar button
     */
    fun clickUploadAvatar(): ServiceProviderFormPage {
        uploadAvatarButton.shouldBe(Condition.visible).click()
        return this
    }

    /**
     * Clicks remove avatar button
     */
    fun clickRemoveAvatar(): ServiceProviderFormPage {
        removeAvatarButton.shouldBe(Condition.visible).click()
        return this
    }

    /**
     * Adds a custom field with key and value
     */
    fun addCustomField(key: String, value: String): ServiceProviderFormPage {
        // Scroll to the button to ensure it's in view and clickable
        addCustomFieldButton.scrollTo()
        addCustomFieldButton.shouldBe(Condition.visible)
        addCustomFieldButton.shouldBe(Condition.enabled)
        addCustomFieldButton.click()

        val customFields = customFieldsContainer.`$$`("[data-test-id='custom-field-item']")
        val lastField = customFields.last()

        lastField.`$`("[data-test-id='custom-field-key']").setValue(key)
        lastField.`$`("[data-test-id='custom-field-value']").setValue(value)

        return this
    }

    /**
     * Removes a custom field by index
     */
    fun removeCustomField(index: Int): ServiceProviderFormPage {
        val customFields = customFieldsContainer.`$$`("[data-test-id='custom-field-item']")
        if (index < customFields.size()) {
            customFields.get(index).`$`("[data-test-id='remove-custom-field-button']").click()
        }
        return this
    }

    /**
     * Saves the form
     */
    fun save(): ServiceProviderFormPage {
        saveButton.shouldBe(Condition.enabled).click()
        return this
    }

    /**
     * Cancels form editing
     */
    fun cancel(): ServiceProviderFormPage {
        cancelButton.shouldBe(Condition.visible).click()
        return this
    }

    /**
     * Deletes the service provider (only available in edit mode)
     */
    fun delete(): ServiceProviderFormPage {
        deleteButton.shouldBe(Condition.visible).click()
        return this
    }

    /**
     * Fills the complete form with provided data
     */
    fun fillForm(
        name: String,
        comment: String = "",
        ocrComment: String = "",
        frequency: String = "NOT_REGULAR",
        active: Boolean = true
    ): ServiceProviderFormPage {
        enterName(name)
        if (comment.isNotEmpty()) enterComment(comment)
        if (ocrComment.isNotEmpty()) enterOcrComment(ocrComment)
        selectFrequency(frequency)
        // Service providers are always active - state toggle removed
        return this
    }

    // Verification methods

    /**
     * Verifies the form is in create mode
     */
    fun shouldBeInCreateMode(): ServiceProviderFormPage {
        formTitle.shouldHave(Condition.text("Create Service Provider"))
        deleteButton.shouldNot(Condition.exist)
        return this
    }

    /**
     * Verifies the form is in edit mode
     */
    fun shouldBeInEditMode(): ServiceProviderFormPage {
        formTitle.shouldHave(Condition.text("Edit Service Provider"))
        deleteButton.shouldBe(Condition.visible)
        return this
    }

    /**
     * Verifies save button is enabled
     */
    fun shouldHaveSaveButtonEnabled(): ServiceProviderFormPage {
        saveButton.shouldBe(Condition.enabled)
        return this
    }

    /**
     * Verifies save button is disabled
     */
    fun shouldHaveSaveButtonDisabled(): ServiceProviderFormPage {
        saveButton.shouldBe(Condition.disabled)
        return this
    }

    /**
     * Verifies form field values
     */
    fun shouldHaveFieldValues(
        expectedName: String,
        expectedComment: String = "",
        expectedOcrComment: String = "",
        expectedFrequency: String = "NOT_REGULAR",
        expectedActive: Boolean = true
    ): ServiceProviderFormPage {
        nameField.shouldHave(Condition.value(expectedName))
        commentField.shouldHave(Condition.value(expectedComment))
        ocrCommentField.shouldHave(Condition.value(expectedOcrComment))
        frequencyField.shouldHave(Condition.value(expectedFrequency))

        // State toggle removed - providers are always active
        // Verification of state toggle is no longer applicable

        return this
    }

    /**
     * Verifies validation error is shown for name field
     */
    fun shouldShowNameValidationError(): ServiceProviderFormPage {
        nameValidationError.shouldBe(Condition.visible)
        return this
    }

    /**
     * Verifies success message is shown
     */
    fun shouldShowSuccessMessage(): ServiceProviderFormPage {
        successMessage.shouldBe(Condition.visible, Duration.ofSeconds(2))
        return this
    }

    /**
     * Verifies error message is shown
     */
    fun shouldShowErrorMessage(): ServiceProviderFormPage {
        errorMessage.shouldBe(Condition.visible)
        return this
    }

    /**
     * Verifies custom field exists with key and value
     */
    fun shouldHaveCustomField(key: String, value: String): ServiceProviderFormPage {
        val customFields = customFieldsContainer.`$$`("[data-test-id='custom-field-item']")
        val matchingField = customFields.find { field ->
            field.`$`("[data-test-id='custom-field-key']").value == key &&
            field.`$`("[data-test-id='custom-field-value']").value == value
        }
        assert(matchingField != null) { "Custom field with key '$key' and value '$value' not found" }
        return this
    }

    /**
     * Verifies avatar is displayed
     */
    fun shouldHaveAvatar(): ServiceProviderFormPage {
        avatarPreview.shouldBe(Condition.visible)
        avatarPreview.`$`("img").shouldBe(Condition.visible)
        return this
    }

    /**
     * Verifies avatar fallback is displayed
     */
    fun shouldHaveAvatarFallback(): ServiceProviderFormPage {
        avatarPreview.shouldBe(Condition.visible)
        avatarPreview.`$`(".avatar-fallback").shouldBe(Condition.visible)
        return this
    }

    /**
     * Verifies form is cleared/reset
     */
    fun shouldBeCleared(): ServiceProviderFormPage {
        nameField.shouldHave(Condition.value(""))
        commentField.shouldHave(Condition.value(""))
        ocrCommentField.shouldHave(Condition.value(""))
        frequencyField.shouldHave(Condition.value("NOT_REGULAR"))
        return this
    }

    /**
     * Returns to service provider list page
     */
    fun backToList(): ServiceProviderListPage {
        return ServiceProviderListPage()
    }
}
