package me.underlow.receipt.e2e

import me.underlow.receipt.config.BaseE2ETest
import me.underlow.receipt.e2e.helpers.LoginHelper
import me.underlow.receipt.e2e.helpers.TestNavigationHelper
import me.underlow.receipt.e2e.helpers.UploadHelper
import me.underlow.receipt.e2e.pages.AvatarUploadPage
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.test.context.jdbc.Sql
import java.io.File

/**
 * End-to-end tests for avatar upload functionality.
 * Tests complete user workflows including modal opening, file selection, cropping, and upload.
 * Tests full flow from frontend to backend without JavaScript shortcuts.
 */
@Sql(scripts = ["/test-data/service-providers.sql"], executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class AvatarUploadE2ETest : BaseE2ETest() {

    private val navigationHelper = TestNavigationHelper()
    private val uploadHelper = UploadHelper()
    private val avatarUploadPage = AvatarUploadPage()
    private val testFiles = mutableListOf<File>()

    @BeforeEach
    fun setUp() {
        // Given: User is logged in and navigated to services tab
        val loginHelper = LoginHelper()
        loginHelper.loginAsTestUser()
        navigationHelper.navigateToServicesTab()
    }

    @AfterEach
    fun tearDown() {
        // Clean up test files created during test execution
        testFiles.forEach { file ->
            try {
                if (file.exists()) {
                    file.delete()
                }
            } catch (e: Exception) {
                // Ignore cleanup errors
            }
        }
        testFiles.clear()
    }

    @Test
    @DisplayName("should open avatar upload modal when clicking upload button")
    fun shouldOpenAvatarUploadModalWhenClickingUploadButton() {
        // Given: Service provider with no avatar exists
        
        // When: User clicks the upload avatar button for service provider 1
        avatarUploadPage.openAvatarUploadModal("1")

        // Then: Avatar upload modal should be displayed with correct title
        avatarUploadPage
            .shouldBeVisible()
            .shouldHaveTitle("Upload Service Provider Avatar")
            .shouldDisableConfirmButton()
    }

    @Test
    @DisplayName("should close avatar upload modal when clicking cancel")
    fun shouldCloseAvatarUploadModalWhenClickingCancel() {
        // Given: Avatar upload modal is open
        avatarUploadPage.openAvatarUploadModal("1")
        avatarUploadPage.shouldBeVisible()

        // When: User clicks cancel button
        avatarUploadPage.cancelUpload()

        // Then: Modal should be closed
        avatarUploadPage.shouldBeClosed()
    }

    @Test
    @DisplayName("should close avatar upload modal when clicking close button")
    fun shouldCloseAvatarUploadModalWhenClickingCloseButton() {
        // Given: Avatar upload modal is open
        avatarUploadPage.openAvatarUploadModal("1")
        avatarUploadPage.shouldBeVisible()

        // When: User clicks close button (X)
        avatarUploadPage.closeModal()

        // Then: Modal should be closed
        avatarUploadPage.shouldBeClosed()
    }

    @Test
    @DisplayName("should enable upload button when valid image is selected")
    fun shouldEnableUploadButtonWhenValidImageIsSelected() {
        // Given: Avatar upload modal is open
        avatarUploadPage.openAvatarUploadModal("1")
        avatarUploadPage.shouldBeVisible()

        // When: User selects a valid image file
        val testImageFile = uploadHelper.createTestJpegFile("test-valid-image.jpg")
        testFiles.add(testImageFile)
        avatarUploadPage.selectFile(testImageFile)

        // Then: Cropper image should be visible and file drop zone hidden
        avatarUploadPage
            .shouldShowCropperImage()
            .shouldHideFileDropZone()
            .shouldEnableConfirmButton()
            .shouldShowPreviewImage()
    }

    @Test
    @DisplayName("should show error message for invalid file format")
    fun shouldShowErrorMessageForInvalidFileFormat() {
        // Given: Avatar upload modal is open
        avatarUploadPage.openAvatarUploadModal("1")
        avatarUploadPage.shouldBeVisible()

        // When: User selects an invalid file (text file)
        val invalidFile = createTestTextFile()
        testFiles.add(invalidFile)
        avatarUploadPage.selectFile(invalidFile)

        // Then: Error message should be displayed and upload button disabled
        avatarUploadPage
            .shouldShowErrorMessage("Please select a valid image file")
            .shouldDisableConfirmButton()
    }

    @Test
    @DisplayName("should show error message for oversized file")
    fun shouldShowErrorMessageForOversizedFile() {
        // Given: Avatar upload modal is open
        avatarUploadPage.openAvatarUploadModal("1")
        avatarUploadPage.shouldBeVisible()

        // When: User selects an oversized file (larger than 10MB)
        val largeFile = uploadHelper.createLargeTestFile("large-test-image.jpg", 11 * 1024 * 1024) // 11MB
        testFiles.add(largeFile)
        avatarUploadPage.selectFile(largeFile)

        // Then: Error message should be displayed about file size
        avatarUploadPage
            .shouldShowErrorMessage("File size must be less than 10MB")
            .shouldDisableConfirmButton()
    }

    @Test
    @DisplayName("should show crop controls when crop button is clicked")
    fun shouldShowCropControlsWhenCropButtonIsClicked() {
        // Given: Avatar upload modal is open with image loaded
        avatarUploadPage.openAvatarUploadModal("1")
        avatarUploadPage.shouldBeVisible()
        
        val testImageFile = uploadHelper.createTestJpegFile("test-crop-image.jpg")
        testFiles.add(testImageFile)
        avatarUploadPage.selectFile(testImageFile)
        avatarUploadPage.shouldShowCropperImage()

        // When: User hovers over image and clicks crop button
        avatarUploadPage.startCropping()

        // Then: Crop controls should be visible with accept and cancel buttons
        avatarUploadPage.shouldShowCropControls()
    }

    @Test
    @DisplayName("should accept crop changes when accept button is clicked")
    fun shouldAcceptCropChangesWhenAcceptButtonIsClicked() {
        // Given: Avatar upload modal is open with image in crop mode
        avatarUploadPage.openAvatarUploadModal("1")
        avatarUploadPage.shouldBeVisible()
        
        val testImageFile = uploadHelper.createTestJpegFile("test-crop-accept.jpg")
        testFiles.add(testImageFile)
        avatarUploadPage.selectFile(testImageFile)
        avatarUploadPage.shouldShowCropperImage()
        avatarUploadPage.startCropping()
        avatarUploadPage.shouldShowCropControls()

        // When: User clicks accept crop
        avatarUploadPage.acceptCrop()

        // Then: Crop controls should be hidden and preview updated
        avatarUploadPage
            .shouldHideCropControls()
            .shouldShowPreviewImage()
    }

    @Test
    @DisplayName("should cancel crop changes when cancel button is clicked")
    fun shouldCancelCropChangesWhenCancelButtonIsClicked() {
        // Given: Avatar upload modal is open with image in crop mode
        avatarUploadPage.openAvatarUploadModal("1")
        avatarUploadPage.shouldBeVisible()
        
        val testImageFile = uploadHelper.createTestJpegFile("test-crop-cancel.jpg")
        testFiles.add(testImageFile)
        avatarUploadPage.selectFile(testImageFile)
        avatarUploadPage.shouldShowCropperImage()
        avatarUploadPage.startCropping()
        avatarUploadPage.shouldShowCropControls()

        // When: User clicks cancel crop
        avatarUploadPage.cancelCrop()

        // Then: Crop controls should be hidden and original image restored
        avatarUploadPage
            .shouldHideCropControls()
            .shouldShowCropperImage()
    }

    @Test
    @DisplayName("should show rotate controls when rotate button is clicked")
    fun shouldShowRotateControlsWhenRotateButtonIsClicked() {
        // Given: Avatar upload modal is open with image loaded
        avatarUploadPage.openAvatarUploadModal("1")
        avatarUploadPage.shouldBeVisible()
        
        val testImageFile = uploadHelper.createTestJpegFile("test-rotate-image.jpg")
        testFiles.add(testImageFile)
        avatarUploadPage.selectFile(testImageFile)
        avatarUploadPage.shouldShowCropperImage()

        // When: User hovers over image and clicks rotate button
        avatarUploadPage.startRotation()

        // Then: Rotate controls should be visible with accept and cancel buttons
        avatarUploadPage.shouldShowRotateControls()
    }

    @Test
    @DisplayName("should show progress indication during upload")
    fun shouldShowProgressIndicationDuringUpload() {
        // Given: Avatar upload modal is open with image selected
        avatarUploadPage.openAvatarUploadModal("1")
        avatarUploadPage.shouldBeVisible()
        
        val testImageFile = uploadHelper.createTestJpegFile("test-upload-progress.jpg")
        testFiles.add(testImageFile)
        avatarUploadPage.selectFile(testImageFile)
        avatarUploadPage.shouldShowCropperImage()

        // When: User clicks upload button
        avatarUploadPage.confirmUpload()

        // Then: Progress indication should be visible
        avatarUploadPage
            .shouldShowProgress()
            .shouldShowUploadingState()
    }

    @Test
    @DisplayName("should close modal and show success message on successful upload")
    fun shouldCloseModalAndShowSuccessMessageOnSuccessfulUpload() {
        // Given: Avatar upload modal is open with image selected
        avatarUploadPage.openAvatarUploadModal("1")
        avatarUploadPage.shouldBeVisible()
        
        val testImageFile = uploadHelper.createTestJpegFile("test-upload-success.jpg")
        testFiles.add(testImageFile)
        avatarUploadPage.selectFile(testImageFile)
        avatarUploadPage.shouldShowCropperImage()

        // When: User uploads the image successfully
        avatarUploadPage.confirmUpload()

        // Then: Upload should complete successfully and show success message
        avatarUploadPage.shouldShowSuccessAlert("Avatar uploaded successfully!")
        
        // And: Modal should eventually close
        avatarUploadPage.shouldBeClosed()
    }

    @Test
    @DisplayName("should show error message on upload failure")
    fun shouldShowErrorMessageOnUploadFailure() {
        // Given: Avatar upload modal is open with corrupted image file
        avatarUploadPage.openAvatarUploadModal("1")
        avatarUploadPage.shouldBeVisible()
        
        // Create a file that appears to be an image but has invalid content
        val corruptedImageFile = uploadHelper.createTestJpegFile("test-corrupted-image.jpg")
        // Corrupt the file by writing invalid content while keeping JPEG extension
        corruptedImageFile.writeBytes(byteArrayOf(0x00, 0x01, 0x02, 0x03))
        testFiles.add(corruptedImageFile)
        
        avatarUploadPage.selectFile(corruptedImageFile)

        // When: Upload fails due to corrupted file
        avatarUploadPage.confirmUpload()

        // Then: Error message should be displayed and modal should remain open
        avatarUploadPage
            .shouldShowErrorMessage("Upload failed. Please try again.")
            .shouldBeVisible()
    }

    @Test
    @DisplayName("should update preview when image is cropped")
    fun shouldUpdatePreviewWhenImageIsCropped() {
        // Given: Avatar upload modal is open with image loaded
        avatarUploadPage.openAvatarUploadModal("1")
        avatarUploadPage.shouldBeVisible()
        
        val testImageFile = uploadHelper.createTestJpegFile("test-preview-crop.jpg")
        testFiles.add(testImageFile)
        avatarUploadPage.selectFile(testImageFile)
        avatarUploadPage.shouldShowCropperImage()

        // When: User performs crop operation
        avatarUploadPage.startCropping()
        avatarUploadPage.acceptCrop()

        // Then: Preview image should be visible and updated
        avatarUploadPage.shouldShowPreviewImage()
    }

    @Test
    @DisplayName("should hide placeholder when image is loaded")
    fun shouldHidePlaceholderWhenImageIsLoaded() {
        // Given: Avatar upload modal is open
        avatarUploadPage.openAvatarUploadModal("1")
        avatarUploadPage.shouldBeVisible()

        // When: User selects an image
        val testImageFile = uploadHelper.createTestJpegFile("test-placeholder.jpg")
        testFiles.add(testImageFile)
        avatarUploadPage.selectFile(testImageFile)

        // Then: Placeholder should be hidden and preview image visible
        avatarUploadPage
            .shouldHidePreviewPlaceholder()
            .shouldShowPreviewImage()
    }

    /**
     * Creates a test text file for invalid file format testing
     */
    private fun createTestTextFile(): File {
        val tempFile = File.createTempFile("test-text", ".txt")
        tempFile.writeText("This is not an image file")
        return tempFile
    }
}