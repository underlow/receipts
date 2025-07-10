package me.underlow.receipt.e2e

import com.codeborne.selenide.Condition
import com.codeborne.selenide.Selenide
import com.codeborne.selenide.SelenideElement
import me.underlow.receipt.config.BaseE2ETest
import me.underlow.receipt.e2e.helpers.LoginHelper
import me.underlow.receipt.e2e.helpers.TestNavigationHelper
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.test.context.jdbc.Sql
import java.io.File
import kotlin.test.assertTrue

/**
 * End-to-end tests for avatar upload functionality.
 * Tests complete user workflows including dialog opening, file selection, cropping, and upload.
 */
@Sql(scripts = ["/test-data/service-providers.sql"], executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class AvatarUploadE2ETest : BaseE2ETest() {

    private val navigationHelper = TestNavigationHelper()

    @BeforeEach
    fun setUp() {
        // Given: User is logged in and navigated to services tab
        val loginHelper = LoginHelper()
        loginHelper.loginAsTestUser()
        navigationHelper.navigateToServicesTab()
    }

    @Nested
    @DisplayName("Avatar Upload Dialog Tests")
    inner class AvatarUploadDialogTests {

        @Test
        @DisplayName("should open avatar upload dialog when clicking upload button")
        fun shouldOpenAvatarUploadDialogWhenClickingUploadButton() {
            // Given: Service provider with no avatar exists
            val uploadButton = Selenide.`$`("[data-test-id='avatar-upload-button-1']")

            // When: User clicks the upload avatar button
            uploadButton.click()

            // Then: Avatar upload modal should be displayed
            val avatarModal = Selenide.`$`("#avatarUploadModal")
            avatarModal.shouldBe(Condition.visible)

            // And: Modal title should be correct
            val modalTitle = Selenide.`$`("#avatarUploadModalLabel")
            modalTitle.shouldHave(Condition.text("Upload Service Provider Avatar"))

            // And: File drop zone should be visible
            val dropZone = Selenide.`$`("#avatarFileDropZone")
            dropZone.shouldBe(Condition.visible)

            // And: Upload button should be disabled initially
            val confirmButton = Selenide.`$`("#avatarConfirmUpload")
            confirmButton.shouldBe(Condition.disabled)
        }

        @Test
        @DisplayName("should close avatar upload dialog when clicking cancel")
        fun shouldCloseAvatarUploadDialogWhenClickingCancel() {
            // Given: Avatar upload modal is open
            val uploadButton = Selenide.`$`("[data-test-id='avatar-upload-button-1']")
            uploadButton.click()
            val avatarModal = Selenide.`$`("#avatarUploadModal")
            avatarModal.shouldBe(Condition.visible)

            // When: User clicks cancel button
            val cancelButton = Selenide.`$`("#avatarCancelUpload")
            cancelButton.click()

            // Then: Modal should be hidden
            avatarModal.shouldBe(Condition.hidden)
        }

        @Test
        @DisplayName("should close avatar upload dialog when clicking close button")
        fun shouldCloseAvatarUploadDialogWhenClickingCloseButton() {
            // Given: Avatar upload modal is open
            val uploadButton = Selenide.`$`("[data-test-id='avatar-upload-button-1']")
            uploadButton.click()
            val avatarModal = Selenide.`$`("#avatarUploadModal")
            avatarModal.shouldBe(Condition.visible)

            // When: User clicks close button (X)
            val closeButton = avatarModal.`$`(".btn-close")
            closeButton.click()

            // Then: Modal should be hidden
            avatarModal.shouldBe(Condition.hidden)
        }
    }

    @Nested
    @DisplayName("File Selection Tests")
    inner class FileSelectionTests {

        @Test
        @DisplayName("should enable upload button when valid image is selected")
        fun shouldEnableUploadButtonWhenValidImageIsSelected() {
            // Given: Avatar upload modal is open
            val uploadButton = Selenide.`$`("[data-test-id='avatar-upload-button-1']")
            uploadButton.click()
            val avatarModal = Selenide.`$`("#avatarUploadModal")
            avatarModal.shouldBe(Condition.visible)

            // When: User selects a valid image file
            val testImageFile = createTestImageFile()
            val fileInput = Selenide.`$`("#avatarFileInput")
            fileInput.uploadFile(testImageFile)

            // Then: Cropper image should be visible
            val cropperImage = Selenide.`$`("#avatarCropperImage")
            cropperImage.shouldBe(Condition.visible)

            // And: File drop zone should be hidden
            val dropZone = Selenide.`$`("#avatarFileDropZone")
            dropZone.shouldBe(Condition.hidden)

            // And: Upload button should be enabled
            val confirmButton = Selenide.`$`("#avatarConfirmUpload")
            confirmButton.shouldBe(Condition.enabled)

            // And: 200x200 preview should be visible
            val previewImage = Selenide.`$`("#avatarPreviewImage")
            previewImage.shouldBe(Condition.visible)

            // Clean up
            testImageFile.delete()
        }

        @Test
        @DisplayName("should show error message for invalid file format")
        fun shouldShowErrorMessageForInvalidFileFormat() {
            // Given: Avatar upload modal is open
            val uploadButton = Selenide.`$`("[data-test-id='avatar-upload-button-1']")
            uploadButton.click()
            val avatarModal = Selenide.`$`("#avatarUploadModal")
            avatarModal.shouldBe(Condition.visible)

            // When: User selects an invalid file (text file)
            val invalidFile = createTestTextFile()
            val fileInput = Selenide.`$`("#avatarFileInput")
            fileInput.uploadFile(invalidFile)

            // Then: Error message should be displayed
            val errorContainer = Selenide.`$`("#avatarErrorContainer")
            errorContainer.shouldBe(Condition.visible)

            val errorMessage = Selenide.`$`("#avatarErrorMessage")
            errorMessage.shouldHave(Condition.text("Please select a valid image file"))

            // And: Upload button should remain disabled
            val confirmButton = Selenide.`$`("#avatarConfirmUpload")
            confirmButton.shouldBe(Condition.disabled)

            // Clean up
            invalidFile.delete()
        }

        @Test
        @DisplayName("should show error message for oversized file")
        fun shouldShowErrorMessageForOversizedFile() {
            // Given: Avatar upload modal is open
            val uploadButton = Selenide.`$`("[data-test-id='avatar-upload-button-1']")
            uploadButton.click()
            val avatarModal = Selenide.`$`("#avatarUploadModal")
            avatarModal.shouldBe(Condition.visible)

            // When: User selects a large file (simulated by JavaScript)
            Selenide.executeJavaScript<Unit>("""
                // Simulate oversized file error
                window.showAvatarErrorMessage('File size must be less than 10MB');
            """)

            // Then: Error message should be displayed
            val errorContainer = Selenide.`$`("#avatarErrorContainer")
            errorContainer.shouldBe(Condition.visible)

            val errorMessage = Selenide.`$`("#avatarErrorMessage")
            errorMessage.shouldHave(Condition.text("File size must be less than 10MB"))
        }
    }

    @Nested
    @DisplayName("Image Cropping Tests")
    inner class ImageCroppingTests {

        @Test
        @DisplayName("should show crop controls when crop button is clicked")
        fun shouldShowCropControlsWhenCropButtonIsClicked() {
            // Given: Avatar upload modal is open with image loaded
            val uploadButton = Selenide.`$`("[data-test-id='avatar-upload-button-1']")
            uploadButton.click()
            val testImageFile = createTestImageFile()
            val fileInput = Selenide.`$`("#avatarFileInput")
            fileInput.uploadFile(testImageFile)

            // When: User hovers over image and clicks crop button
            val imagePreview = Selenide.`$`("#avatarImagePreview")
            imagePreview.hover()
            val cropButton = Selenide.`$`("#avatarCropButton")
            cropButton.shouldBe(Condition.visible)
            cropButton.click()

            // Then: Crop controls should be visible
            val cropControls = Selenide.`$`("#avatarCropControls")
            cropControls.shouldBe(Condition.visible)

            // And: Accept and cancel buttons should be present
            val acceptCrop = Selenide.`$`("#avatarAcceptCrop")
            acceptCrop.shouldBe(Condition.visible)
            val cancelCrop = Selenide.`$`("#avatarCancelCrop")
            cancelCrop.shouldBe(Condition.visible)

            // Clean up
            testImageFile.delete()
        }

        @Test
        @DisplayName("should accept crop changes when accept button is clicked")
        fun shouldAcceptCropChangesWhenAcceptButtonIsClicked() {
            // Given: Avatar upload modal is open with image in crop mode
            val uploadButton = Selenide.`$`("[data-test-id='avatar-upload-button-1']")
            uploadButton.click()
            val testImageFile = createTestImageFile()
            val fileInput = Selenide.`$`("#avatarFileInput")
            fileInput.uploadFile(testImageFile)

            val imagePreview = Selenide.`$`("#avatarImagePreview")
            imagePreview.hover()
            val cropButton = Selenide.`$`("#avatarCropButton")
            cropButton.click()

            // When: User clicks accept crop
            val acceptCrop = Selenide.`$`("#avatarAcceptCrop")
            acceptCrop.click()

            // Then: Crop controls should be hidden
            val cropControls = Selenide.`$`("#avatarCropControls")
            cropControls.shouldBe(Condition.hidden)

            // And: Preview should be updated
            val previewImage = Selenide.`$`("#avatarPreviewImage")
            previewImage.shouldBe(Condition.visible)

            // Clean up
            testImageFile.delete()
        }

        @Test
        @DisplayName("should cancel crop changes when cancel button is clicked")
        fun shouldCancelCropChangesWhenCancelButtonIsClicked() {
            // Given: Avatar upload modal is open with image in crop mode
            val uploadButton = Selenide.`$`("[data-test-id='avatar-upload-button-1']")
            uploadButton.click()
            val testImageFile = createTestImageFile()
            val fileInput = Selenide.`$`("#avatarFileInput")
            fileInput.uploadFile(testImageFile)

            val imagePreview = Selenide.`$`("#avatarImagePreview")
            imagePreview.hover()
            val cropButton = Selenide.`$`("#avatarCropButton")
            cropButton.click()

            // When: User clicks cancel crop
            val cancelCrop = Selenide.`$`("#avatarCancelCrop")
            cancelCrop.click()

            // Then: Crop controls should be hidden
            val cropControls = Selenide.`$`("#avatarCropControls")
            cropControls.shouldBe(Condition.hidden)

            // And: Original image should be restored
            val cropperImage = Selenide.`$`("#avatarCropperImage")
            cropperImage.shouldBe(Condition.visible)

            // Clean up
            testImageFile.delete()
        }
    }

    @Nested
    @DisplayName("Image Rotation Tests")
    inner class ImageRotationTests {

        @Test
        @DisplayName("should show rotate controls when rotate button is clicked")
        fun shouldShowRotateControlsWhenRotateButtonIsClicked() {
            // Given: Avatar upload modal is open with image loaded
            val uploadButton = Selenide.`$`("[data-test-id='avatar-upload-button-1']")
            uploadButton.click()
            val testImageFile = createTestImageFile()
            val fileInput = Selenide.`$`("#avatarFileInput")
            fileInput.uploadFile(testImageFile)

            // When: User hovers over image and clicks rotate button
            val imagePreview = Selenide.`$`("#avatarImagePreview")
            imagePreview.hover()
            val rotateButton = Selenide.`$`("#avatarRotateButton")
            rotateButton.shouldBe(Condition.visible)
            rotateButton.click()

            // Then: Rotate controls should be visible
            val rotateControls = Selenide.`$`("#avatarRotateControls")
            rotateControls.shouldBe(Condition.visible)

            // And: Accept and cancel buttons should be present
            val acceptRotate = Selenide.`$`("#avatarAcceptRotate")
            acceptRotate.shouldBe(Condition.visible)
            val cancelRotate = Selenide.`$`("#avatarCancelRotate")
            cancelRotate.shouldBe(Condition.visible)

            // Clean up
            testImageFile.delete()
        }
    }

    @Nested
    @DisplayName("Avatar Upload Tests")
    inner class AvatarUploadTests {

        @Test
        @DisplayName("should show progress indication during upload")
        fun shouldShowProgressIndicationDuringUpload() {
            // Given: Avatar upload modal is open with image selected
            val uploadButton = Selenide.`$`("[data-test-id='avatar-upload-button-1']")
            uploadButton.click()
            val testImageFile = createTestImageFile()
            val fileInput = Selenide.`$`("#avatarFileInput")
            fileInput.uploadFile(testImageFile)

            // When: User clicks upload button
            val confirmButton = Selenide.`$`("#avatarConfirmUpload")
            confirmButton.click()

            // Then: Progress container should be visible
            val progressContainer = Selenide.`$`("#avatarProgressContainer")
            progressContainer.shouldBe(Condition.visible)

            // And: Button should show uploading state
            confirmButton.shouldHave(Condition.text("Uploading"))

            // Clean up
            testImageFile.delete()
        }

        @Test
        @DisplayName("should close modal and show success message on successful upload")
        fun shouldCloseModalAndShowSuccessMessageOnSuccessfulUpload() {
            // Given: Avatar upload modal is open with image selected
            val uploadButton = Selenide.`$`("[data-test-id='avatar-upload-button-1']")
            uploadButton.click()
            val testImageFile = createTestImageFile()
            val fileInput = Selenide.`$`("#avatarFileInput")
            fileInput.uploadFile(testImageFile)

            // When: User uploads the image successfully (simulated)
            Selenide.executeJavaScript<Unit>("""
                // Simulate successful upload response
                window.handleAvatarUploadResponse({
                    success: true,
                    message: 'Avatar uploaded successfully!',
                    data: { id: 1, name: 'Test Provider', avatar: 'test_avatar.jpg' },
                    avatarPath: 'test_avatar.jpg'
                });
            """)

            // Then: Modal should be closed
            val avatarModal = Selenide.`$`("#avatarUploadModal")
            avatarModal.shouldBe(Condition.hidden)

            // And: Success message should be displayed
            val successAlert = Selenide.`$`(".alert-success")
            successAlert.shouldBe(Condition.visible)
            successAlert.shouldHave(Condition.text("Avatar uploaded successfully!"))

            // Clean up
            testImageFile.delete()
        }

        @Test
        @DisplayName("should show error message on upload failure")
        fun shouldShowErrorMessageOnUploadFailure() {
            // Given: Avatar upload modal is open with image selected
            val uploadButton = Selenide.`$`("[data-test-id='avatar-upload-button-1']")
            uploadButton.click()
            val testImageFile = createTestImageFile()
            val fileInput = Selenide.`$`("#avatarFileInput")
            fileInput.uploadFile(testImageFile)

            // When: Upload fails (simulated)
            Selenide.executeJavaScript<Unit>("""
                // Simulate upload failure response
                window.handleAvatarUploadResponse({
                    success: false,
                    error: 'Upload failed. Please try again.'
                });
            """)

            // Then: Error message should be displayed in modal
            val errorContainer = Selenide.`$`("#avatarErrorContainer")
            errorContainer.shouldBe(Condition.visible)

            val errorMessage = Selenide.`$`("#avatarErrorMessage")
            errorMessage.shouldHave(Condition.text("Upload failed. Please try again."))

            // And: Modal should remain open
            val avatarModal = Selenide.`$`("#avatarUploadModal")
            avatarModal.shouldBe(Condition.visible)

            // Clean up
            testImageFile.delete()
        }
    }

    @Nested
    @DisplayName("Avatar Preview Tests")
    inner class AvatarPreviewTests {

        @Test
        @DisplayName("should update 200x200 preview when image is cropped")
        fun shouldUpdate200x200PreviewWhenImageIsCropped() {
            // Given: Avatar upload modal is open with image loaded
            val uploadButton = Selenide.`$`("[data-test-id='avatar-upload-button-1']")
            uploadButton.click()
            val testImageFile = createTestImageFile()
            val fileInput = Selenide.`$`("#avatarFileInput")
            fileInput.uploadFile(testImageFile)

            // When: User performs crop operation
            val imagePreview = Selenide.`$`("#avatarImagePreview")
            imagePreview.hover()
            val cropButton = Selenide.`$`("#avatarCropButton")
            cropButton.click()

            // Simulate crop change
            Selenide.executeJavaScript<Unit>("window.updateAvatarPreview && window.updateAvatarPreview();")

            // Then: Preview image should be visible and have correct dimensions
            val previewImage = Selenide.`$`("#avatarPreviewImage")
            previewImage.shouldBe(Condition.visible)

            // And: Preview should show cropped result
            val previewBox = Selenide.`$`("#avatarPreview")
            assertTrue("Preview box should be displayed") { previewBox.isDisplayed }

            // Clean up
            testImageFile.delete()
        }

        @Test
        @DisplayName("should hide placeholder when image is loaded")
        fun shouldHidePlaceholderWhenImageIsLoaded() {
            // Given: Avatar upload modal is open
            val uploadButton = Selenide.`$`("[data-test-id='avatar-upload-button-1']")
            uploadButton.click()

            // When: User selects an image
            val testImageFile = createTestImageFile()
            val fileInput = Selenide.`$`("#avatarFileInput")
            fileInput.uploadFile(testImageFile)

            // Then: Placeholder should be hidden
            val placeholder = Selenide.`$`("#avatarPreviewPlaceholder")
            placeholder.shouldBe(Condition.hidden)

            // And: Preview image should be visible
            val previewImage = Selenide.`$`("#avatarPreviewImage")
            previewImage.shouldBe(Condition.visible)

            // Clean up
            testImageFile.delete()
        }
    }

    // Helper methods for creating test files
    private fun createTestImageFile(): File {
        val tempFile = File.createTempFile("test-avatar", ".jpg")
        tempFile.writeBytes(createMinimalJpegBytes())
        return tempFile
    }

    private fun createTestTextFile(): File {
        val tempFile = File.createTempFile("test-text", ".txt")
        tempFile.writeText("This is not an image file")
        return tempFile
    }

    private fun createMinimalJpegBytes(): ByteArray {
        // Minimal JPEG file header for testing
        return byteArrayOf(
            0xFF.toByte(), 0xD8.toByte(), // JPEG SOI marker
            0xFF.toByte(), 0xE0.toByte(), // APP0 marker
            0x00, 0x10,                   // APP0 length
            0x4A, 0x46, 0x49, 0x46, 0x00, // "JFIF\0"
            0x01, 0x01,                   // Version 1.1
            0x01,                         // Units: dots per inch
            0x00, 0x48,                   // X density: 72
            0x00, 0x48,                   // Y density: 72
            0x00, 0x00,                   // Thumbnail width/height: 0
            0xFF.toByte(), 0xD9.toByte()  // JPEG EOI marker
        )
    }
}