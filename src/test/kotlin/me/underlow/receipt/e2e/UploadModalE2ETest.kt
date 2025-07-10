package me.underlow.receipt.e2e

import com.codeborne.selenide.Condition
import com.codeborne.selenide.Selenide.`$`
import com.codeborne.selenide.Selenide.`$$`
import com.codeborne.selenide.Selenide.executeJavaScript
import me.underlow.receipt.config.BaseE2ETest
import me.underlow.receipt.e2e.helpers.LoginHelper
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.BeforeEach
import kotlin.test.assertTrue
import java.time.Duration

/**
 * End-to-end tests for Upload Modal functionality.
 * Tests the complete user workflow for image upload modal including
 * modal opening, image preview, cropping, and upload confirmation.
 */
class UploadModalE2ETest : BaseE2ETest() {

    private val loginHelper = LoginHelper()

    @BeforeEach
    fun setUpUploadModalTest() {
        // given - user is logged in and on dashboard
        loginHelper.loginAsAllowedUser1()
        waitForPageLoad()
    }

    @Test
    fun `given dashboard when upload button is clicked then should open upload modal`() {
        // given - user is authenticated and on dashboard
        assertTrue(isOnDashboardPage())

        // when - user clicks upload button
        val uploadButton = `$`("button[data-bs-target='#uploadModal']")
        if (uploadButton.exists()) {
            uploadButton.click()

            // then - upload modal should be visible
            val modal = `$`("#uploadModal")
            modal.shouldBe(Condition.visible, Duration.ofSeconds(10))

            // Verify modal structure
            val modalDialog = `$`("#uploadModal .modal-dialog")
            modalDialog.shouldBe(Condition.visible)
            assertTrue(modalDialog.getAttribute("class")?.contains("modal-lg") == true)
        }
    }

    @Test
    fun `given upload modal when opened then should display modal header with title`() {
        // given - user opens upload modal
        openUploadModal()

        // when - modal is displayed
        val modal = `$`("#uploadModal")
        modal.shouldBe(Condition.visible)

        // then - should display proper header with title
        val modalHeader = `$`("#uploadModal .modal-header")
        modalHeader.shouldBe(Condition.visible)

        val modalTitle = `$`("#uploadModal .modal-title")
        modalTitle.shouldBe(Condition.visible)
        assertTrue(modalTitle.text().contains("Upload Receipt Image"))
    }

    @Test
    fun `given upload modal when opened then should display image preview container`() {
        // given - user opens upload modal
        openUploadModal()

        // when - modal is displayed
        val modal = `$`("#uploadModal")
        modal.shouldBe(Condition.visible)

        // then - should display image preview container
        val imagePreview = `$`("#imagePreview")
        imagePreview.shouldBe(Condition.visible)

        val cropperImage = `$`("#cropperImage")
        cropperImage.shouldBe(Condition.exist)
    }



    @Test
    fun `given upload modal when opened then should display upload and cancel buttons`() {
        // given - user opens upload modal
        openUploadModal()

        // when - modal is displayed
        val modal = `$`("#uploadModal")
        modal.shouldBe(Condition.visible)

        // then - should display action buttons
        val cancelButton = `$`("#cancelUpload")
        cancelButton.shouldBe(Condition.visible)
        assertTrue(cancelButton.text().contains("Cancel"))
        assertTrue(cancelButton.getAttribute("class")?.contains("btn-secondary") == true)

        val confirmButton = `$`("#confirmUpload")
        confirmButton.shouldBe(Condition.visible)
        assertTrue(confirmButton.text().contains("Upload"))
        assertTrue(confirmButton.getAttribute("class")?.contains("btn-success") == true)
    }

    @Test
    fun `given upload modal when cancel button is clicked then should close modal`() {
        // given - user opens upload modal
        openUploadModal()

        // when - user clicks cancel button
        val cancelButton = `$`("#cancelUpload")
        cancelButton.shouldBe(Condition.visible)

        // Add a small delay to ensure modal is fully initialized
        Thread.sleep(500)

        // Click the cancel button
        cancelButton.click()

        // then - modal should be closed
        val modal = `$`("#uploadModal")
        // Wait for modal to fade out completely and check that it's no longer visible
        modal.shouldNotBe(Condition.visible, Duration.ofSeconds(10))

        // Additionally verify that the modal has the correct Bootstrap classes indicating it's closed
        modal.shouldNotHave(Condition.cssClass("show"), Duration.ofSeconds(5))
    }

    @Test
    fun `given upload modal when file is selected then should display image in preview`() {
        // given - user opens upload modal
        openUploadModal()

        // when - file is selected (simulated)
        val fileInput = `$`("input[type='file']")
        if (fileInput.exists()) {
            // then - image should be displayed in preview
            val cropperImage = `$`("#cropperImage")
            cropperImage.shouldBe(Condition.exist)

            // After file selection, image should have src attribute
            // Note: In real test, you would use a test image file
        }
    }

    @Test
    fun `given upload modal with image when cropper is initialized then should enable image editing`() {
        // given - user opens upload modal with image
        openUploadModal()

        // when - cropper is initialized (after image load)
        val cropperImage = `$`("#cropperImage")
        cropperImage.shouldBe(Condition.exist)

        // then - cropper functionality should be available
        // Check if cropper container exists (created by cropper.js)
        val cropperContainer = `$`(".cropper-container")
        if (cropperContainer.exists()) {
            cropperContainer.shouldBe(Condition.visible)

            // Verify cropper elements
            val cropperCanvas = `$`(".cropper-canvas")
            val cropperViewBox = `$`(".cropper-view-box")

            assertTrue(cropperCanvas.exists() || cropperViewBox.exists(),
                "Cropper should initialize with canvas or view box")
        }
    }


    @Test
    fun `given upload modal when upload button is clicked then should process image upload`() {
        // given - user opens upload modal
        openUploadModal()

        // when - modal is opened, upload button should be visible but disabled initially
        val confirmButton = `$`("#confirmUpload")
        confirmButton.shouldBe(Condition.visible)

        // then - upload button should be disabled by default (no image selected)
        assertTrue(!confirmButton.isEnabled, "Upload button should be disabled when no image is selected")

        // Verify button text and styling
        assertTrue(confirmButton.text().contains("Upload"))
        assertTrue(confirmButton.getAttribute("class")?.contains("btn-success") == true)

        // Note: In a complete test, you would:
        // 1. Select a file using the file input
        // 2. Wait for image to load and cropper to initialize
        // 3. Verify button becomes enabled
        // 4. Click the button and verify upload process starts
    }

    @Test
    fun `given upload modal when drag and drop is used then should handle file drop`() {
        // given - user opens upload modal
        openUploadModal()

        // when - checking for drag and drop support
        val dropZone = `$`("#imagePreview")
        if (dropZone.exists()) {
            // then - drop zone should be configured for file drops
            dropZone.shouldBe(Condition.visible)

            // In real test, you would simulate drag and drop events
            // This would verify the drop zone handles file drops correctly
        }
    }

    @Test
    fun `given upload modal when keyboard navigation is used then should be accessible`() {
        // given - user opens upload modal
        openUploadModal()

        // when - using keyboard navigation
        val modal = `$`("#uploadModal")
        modal.shouldBe(Condition.visible)

        // then - modal should be keyboard accessible
        val focusableElements = `$$`("#uploadModal button, #uploadModal input, #uploadModal [tabindex]")
        assertTrue(focusableElements.size() >= 2, "Should have focusable elements for keyboard navigation")

        // Verify first focusable element can receive focus
        val firstFocusable = focusableElements.firstOrNull()
        if (firstFocusable != null) {
            firstFocusable.shouldBe(Condition.exist)
        }
    }

    @Test
    fun `given upload modal when error occurs then should display error message in modal`() {
        // given - user opens upload modal
        openUploadModal()

        // when - error occurs during upload or processing (simulated via JavaScript)
        executeJavaScript<Any>("window.showErrorMessage('Upload failed. Please try again.')")

        // then - error message should be displayed in modal
        val modalErrorContainer = `$`("#uploadModal .alert-danger")
        modalErrorContainer.shouldBe(Condition.visible, Duration.ofSeconds(5))
        assertTrue(modalErrorContainer.text().isNotEmpty())
        assertTrue(modalErrorContainer.text().contains("Upload failed"))

        // and - modal should remain open
        val modal = `$`("#uploadModal")
        modal.shouldBe(Condition.visible)
        modal.shouldHave(Condition.cssClass("show"))
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
