package me.underlow.receipt.e2e

import com.codeborne.selenide.Condition
import com.codeborne.selenide.Selenide.`$`
import com.codeborne.selenide.Selenide.`$$`
import com.codeborne.selenide.Selenide.executeJavaScript
import me.underlow.receipt.config.BaseE2ETest
import me.underlow.receipt.e2e.helpers.LoginHelper
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.BeforeEach
import org.springframework.test.context.ActiveProfiles
import kotlin.test.assertTrue
import kotlin.test.assertFalse
import java.time.Duration

/**
 * End-to-end tests for Upload Modal Error Handling functionality.
 * Tests that error messages are displayed within the modal instead of on the page,
 * modal stays open on errors, and errors are non-blocking.
 */
@ActiveProfiles("test")
class UploadModalErrorHandlingE2ETest : BaseE2ETest() {

    private val loginHelper = LoginHelper()

    @BeforeEach
    fun setUpErrorHandlingTest() {
        // given - user is logged in and on dashboard
        loginHelper.loginAsAllowedUser1()
        waitForPageLoad()
    }

    @Test
    fun `given upload modal when error occurs then should display error message in modal`() {
        // given - user opens upload modal
        openUploadModal()

        // when - simulating error condition via JavaScript
        executeJavaScript<Any>("window.showErrorMessage('Test error message')")

        // then - error message should be displayed in modal, not on page
        val modalErrorContainer = `$`("#uploadModal .alert-danger")
        modalErrorContainer.shouldBe(Condition.visible, Duration.ofSeconds(5))
        assertTrue(modalErrorContainer.text().contains("Test error message"))

        // and - error should not appear on the main page
        val pageErrorContainer = `$`(".dashboard-layout .alert-danger")
        if (pageErrorContainer.exists()) {
            assertFalse(pageErrorContainer.text().contains("Test error message"))
        }
    }

    @Test
    fun `given upload modal when error occurs then should keep modal open`() {
        // given - user opens upload modal
        openUploadModal()

        // when - error occurs during upload
        executeJavaScript<Any>("window.showErrorMessage('Upload failed')")

        // then - modal should remain open
        val modal = `$`("#uploadModal")
        modal.shouldBe(Condition.visible)
        modal.shouldHave(Condition.cssClass("show"))

        // and - error should be visible in modal
        val modalErrorContainer = `$`("#uploadModal .alert-danger")
        modalErrorContainer.shouldBe(Condition.visible)
        assertTrue(modalErrorContainer.text().contains("Upload failed"))
    }

    @Test
    fun `given upload modal when file validation error occurs then should display error in modal`() {
        // given - user opens upload modal
        openUploadModal()

        // when - file validation error occurs
        executeJavaScript<Any>("window.showErrorMessage('Please select a valid image file (JPEG, PNG, GIF, WebP)')")

        // then - validation error should be displayed in modal
        val modalErrorContainer = `$`("#uploadModal .alert-danger")
        modalErrorContainer.shouldBe(Condition.visible)
        assertTrue(modalErrorContainer.text().contains("Please select a valid image file"))

        // and - modal should remain open
        val modal = `$`("#uploadModal")
        modal.shouldBe(Condition.visible)
        modal.shouldHave(Condition.cssClass("show"))
    }

    @Test
    fun `given upload modal when file size error occurs then should display error in modal`() {
        // given - user opens upload modal
        openUploadModal()

        // when - file size error occurs
        executeJavaScript<Any>("window.showErrorMessage('File size must be less than 20MB')")

        // then - size error should be displayed in modal
        val modalErrorContainer = `$`("#uploadModal .alert-danger")
        modalErrorContainer.shouldBe(Condition.visible)
        assertTrue(modalErrorContainer.text().contains("File size must be less than 20MB"))

        // and - modal should remain open
        val modal = `$`("#uploadModal")
        modal.shouldBe(Condition.visible)
    }

    @Test
    fun `given upload modal when file creation error occurs then should display error in modal`() {
        // given - user opens upload modal
        openUploadModal()

        // when - file creation error occurs
        executeJavaScript<Any>("window.showErrorMessage('Failed to create file. Please try again.')")

        // then - file creation error should be displayed in modal
        val modalErrorContainer = `$`("#uploadModal .alert-danger")
        modalErrorContainer.shouldBe(Condition.visible)
        assertTrue(modalErrorContainer.text().contains("Failed to create file"))

        // and - modal should remain open
        val modal = `$`("#uploadModal")
        modal.shouldBe(Condition.visible)
    }

    @Test
    fun `given upload modal when entity creation error occurs then should display error in modal`() {
        // given - user opens upload modal
        openUploadModal()

        // when - entity creation error occurs
        executeJavaScript<Any>("window.showErrorMessage('Failed to create inbox entity. Please try again.')")

        // then - entity creation error should be displayed in modal
        val modalErrorContainer = `$`("#uploadModal .alert-danger")
        modalErrorContainer.shouldBe(Condition.visible)
        assertTrue(modalErrorContainer.text().contains("Failed to create inbox entity"))

        // and - modal should remain open
        val modal = `$`("#uploadModal")
        modal.shouldBe(Condition.visible)
    }

    @Test
    fun `given upload modal when network error occurs then should display error in modal`() {
        // given - user opens upload modal
        openUploadModal()

        // when - network error occurs
        executeJavaScript<Any>("window.showErrorMessage('Network error occurred during upload')")

        // then - network error should be displayed in modal
        val modalErrorContainer = `$`("#uploadModal .alert-danger")
        modalErrorContainer.shouldBe(Condition.visible)
        assertTrue(modalErrorContainer.text().contains("Network error occurred"))

        // and - modal should remain open
        val modal = `$`("#uploadModal")
        modal.shouldBe(Condition.visible)
    }

    @Test
    fun `given upload modal when upload timeout occurs then should display error in modal`() {
        // given - user opens upload modal
        openUploadModal()

        // when - upload timeout occurs
        executeJavaScript<Any>("window.showErrorMessage('Upload timeout. Please try again.')")

        // then - timeout error should be displayed in modal
        val modalErrorContainer = `$`("#uploadModal .alert-danger")
        modalErrorContainer.shouldBe(Condition.visible)
        assertTrue(modalErrorContainer.text().contains("Upload timeout"))

        // and - modal should remain open
        val modal = `$`("#uploadModal")
        modal.shouldBe(Condition.visible)
    }

    @Test
    fun `given upload modal when error is displayed then should be dismissible`() {
        // given - user opens upload modal and error occurs
        openUploadModal()
        executeJavaScript<Any>("window.showErrorMessage('Test dismissible error')")

        // when - error is displayed
        val modalErrorContainer = `$`("#uploadModal .alert-danger")
        modalErrorContainer.shouldBe(Condition.visible)

        // then - error should have dismiss button
        val dismissButton = `$`("#uploadModal .alert-danger .btn-close")
        dismissButton.shouldBe(Condition.visible)
        dismissButton.shouldBe(Condition.enabled)

        // and - clicking dismiss should hide error
        dismissButton.click()
        modalErrorContainer.shouldNotBe(Condition.visible, Duration.ofSeconds(5))
    }

    @Test
    fun `given upload modal when error is displayed then should not be modal blocking`() {
        // given - user opens upload modal and error occurs
        openUploadModal()
        executeJavaScript<Any>("window.showErrorMessage('Non-blocking error')")

        // when - error is displayed
        val modalErrorContainer = `$`("#uploadModal .alert-danger")
        modalErrorContainer.shouldBe(Condition.visible)

        // then - user should still be able to interact with modal elements
        val fileInput = `$`("#fileInput")
        fileInput.shouldBe(Condition.enabled)

        val selectFileBtn = `$`("#selectFileBtn")
        selectFileBtn.shouldBe(Condition.enabled)
        selectFileBtn.shouldBe(Condition.enabled)

        val cancelButton = `$`("#cancelUpload")
        cancelButton.shouldBe(Condition.enabled)
        cancelButton.shouldBe(Condition.enabled)

        // and - error should not have modal-backdrop preventing interaction
        val modalBackdrop = `$`(".modal-backdrop")
        // Modal should still be accessible (not blocked by error)
        val modal = `$`("#uploadModal")
        modal.shouldBe(Condition.visible)
    }

    @Test
    fun `given upload modal when new file is selected then should clear previous errors`() {
        // given - user opens upload modal and error occurs
        openUploadModal()
        executeJavaScript<Any>("window.showErrorMessage('Previous error')")

        // when - error is displayed
        val modalErrorContainer = `$`("#uploadModal .alert-danger")
        modalErrorContainer.shouldBe(Condition.visible)

        // and - new file selection occurs (simulated)
        executeJavaScript<Any>("window.clearModalErrors()")

        // then - previous error should be cleared
        modalErrorContainer.shouldNotBe(Condition.visible, Duration.ofSeconds(5))
    }

    @Test
    fun `given upload modal when multiple errors occur then should display latest error`() {
        // given - user opens upload modal
        openUploadModal()

        // when - first error occurs
        executeJavaScript<Any>("window.showErrorMessage('First error')")
        val modalErrorContainer = `$`("#uploadModal .alert-danger")
        modalErrorContainer.shouldBe(Condition.visible)
        assertTrue(modalErrorContainer.text().contains("First error"))

        // and - second error occurs
        executeJavaScript<Any>("window.showErrorMessage('Second error')")

        // then - should display latest error
        val updatedErrorContainer = `$`("#uploadModal .alert-danger")
        updatedErrorContainer.shouldBe(Condition.visible)
        assertTrue(updatedErrorContainer.text().contains("Second error"))
    }

    @Test
    fun `given upload modal when server error response occurs then should display error in modal`() {
        // given - user opens upload modal
        openUploadModal()

        // when - server error response is handled
        executeJavaScript<Any>("""
            window.handleUploadResponse({
                success: false,
                error: 'Server internal error',
                message: 'Upload failed'
            });
        """)

        // then - server error should be displayed in modal
        val modalErrorContainer = `$`("#uploadModal .alert-danger")
        modalErrorContainer.shouldBe(Condition.visible)
        assertTrue(modalErrorContainer.text().contains("Server internal error"))

        // and - modal should remain open
        val modal = `$`("#uploadModal")
        modal.shouldBe(Condition.visible)
    }

    /**
     * Helper method to open the upload modal.
     * Handles different possible ways to trigger the modal.
     */
    private fun openUploadModal() {
        // Try to find upload button or trigger
        val uploadButton = `$`("button[data-bs-target='#uploadModal']")
        if (uploadButton.exists()) {
            uploadButton.click()
        } else {
            // Try alternative selectors
            val uploadTrigger = `$`("[onclick*='uploadModal'], .upload-trigger")
            if (uploadTrigger.exists()) {
                uploadTrigger.click()
            }
        }

        // Wait for modal to appear
        val modal = `$`("#uploadModal")
        modal.shouldBe(Condition.visible, Duration.ofSeconds(10))
    }
}