package me.underlow.receipt.e2e

import me.underlow.receipt.config.BaseE2ETest
import me.underlow.receipt.e2e.helpers.LoginHelper
import me.underlow.receipt.e2e.pages.UploadModalPage
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.AfterEach
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption

/**
 * End-to-end tests for Upload Modal functionality following best practices.
 * 
 * Tests the complete user workflow for receipt image upload including:
 * - Modal opening and closing
 * - File selection and preview
 * - Image manipulation (crop, rotate)
 * - Upload process and error handling
 * - User experience and accessibility
 */
class UploadModalE2ETest : BaseE2ETest() {

    private val loginHelper = LoginHelper()
    private val uploadModalPage = UploadModalPage()
    private lateinit var testImageFile: File

    @BeforeEach
    fun setUp() {
        // Given - user is authenticated and on dashboard
        loginHelper.loginAsAllowedUser1()
        waitForPageLoad()
        
        // Given - test image file is prepared
        testImageFile = createTestImageFile()
    }

    @AfterEach
    fun tearDown() {
        // Clean up test files
        if (::testImageFile.isInitialized && testImageFile.exists()) {
            testImageFile.delete()
        }
        
        // Ensure modal is closed after each test
        uploadModalPage.closeModalIfOpen()
    }

    @Test
    fun shouldOpenUploadModalWhenUploadButtonClicked() {
        // Given - user is on dashboard with upload functionality available
        // (setup handled in @BeforeEach)
        
        // When - user clicks upload button
        uploadModalPage.openModal()
        
        // Then - upload modal should be visible with proper structure
        uploadModalPage.shouldBeVisible()
            .shouldShowModalHeader()
            .shouldShowFileDropZone()
            .shouldShowActionButtons()
    }

    @Test
    fun shouldCloseModalWhenCancelButtonClicked() {
        // Given - user has opened upload modal
        uploadModalPage.openModal()
            .shouldBeVisible()
        
        // When - user clicks cancel button
        uploadModalPage.cancelUpload()
        
        // Then - modal should be closed
        uploadModalPage.shouldBeClosed()
    }

    @Test
    fun shouldCloseModalWhenCloseButtonClicked() {
        // Given - user has opened upload modal
        uploadModalPage.openModal()
            .shouldBeVisible()
        
        // When - user clicks close button (X)
        uploadModalPage.closeModal()
        
        // Then - modal should be closed
        uploadModalPage.shouldBeClosed()
    }

    @Test
    fun shouldDisplayImagePreviewWhenFileSelected() {
        // Given - user has opened upload modal
        uploadModalPage.openModal()
            .shouldBeVisible()
        
        // When - user selects an image file
        uploadModalPage.uploadFile(testImageFile)
        
        // Then - image should be displayed in preview with cropper
        uploadModalPage.shouldShowCropperImage()
            .shouldEnableConfirmButton()
            .shouldShowImageControls()
    }

    @Test
    fun shouldHandleDragAndDropFileUpload() {
        // Given - user has opened upload modal
        uploadModalPage.openModal()
            .shouldBeVisible()
        
        // When - user drags and drops an image file
        uploadModalPage.dragAndDropFile(testImageFile)
        
        // Then - image should be displayed in preview with cropper
        uploadModalPage.shouldShowCropperImage()
            .shouldEnableConfirmButton()
            .shouldShowImageControls()
    }

    @Test
    fun shouldRotateImageWhenRotateButtonClicked() {
        // Given - user has uploaded an image to the modal
        uploadModalPage.openModal()
            .uploadFile(testImageFile)
            .shouldShowCropperImage()
        
        // When - user clicks rotate button
        uploadModalPage.rotateImage()
        
        // Then - image should be rotated and cropper should update
        uploadModalPage.shouldShowCropperImage()
            .shouldEnableConfirmButton()
    }

    @Test
    fun shouldCropImageWhenCropButtonClicked() {
        // Given - user has uploaded an image to the modal
        uploadModalPage.openModal()
            .uploadFile(testImageFile)
            .shouldShowCropperImage()
        
        // When - user clicks crop button
        uploadModalPage.cropImage()
        
        // Then - image should be cropped and preview should update
        uploadModalPage.shouldShowCropperImage()
            .shouldEnableConfirmButton()
    }

    @Test
    fun shouldResetImageWhenResetButtonClicked() {
        // Given - user has uploaded and modified an image
        uploadModalPage.openModal()
            .uploadFile(testImageFile)
            .shouldShowCropperImage()
            .rotateImage()
        
        // When - user clicks reset button
        uploadModalPage.resetImage()
        
        // Then - image should be reset to original state
        uploadModalPage.shouldShowCropperImage()
            .shouldEnableConfirmButton()
    }

    @Test
    fun shouldCompleteUploadProcessWhenConfirmButtonClicked() {
        // Given - user has uploaded and prepared an image
        uploadModalPage.openModal()
            .uploadFile(testImageFile)
            .shouldShowCropperImage()
            .shouldEnableConfirmButton()
        
        // When - user clicks confirm upload button
        uploadModalPage.confirmUpload()
        
        // Then - upload process should complete successfully
        uploadModalPage.shouldShowProgressDuringUpload()
            .shouldShowSuccessMessage()
            .shouldCloseAutomaticallyAfterSuccess()
    }

    @Test
    fun shouldDisplayErrorMessageWhenUploadFails() {
        // Given - user has uploaded an image (simulating upload failure scenario)
        uploadModalPage.openModal()
            .uploadFile(testImageFile)
            .shouldShowCropperImage()
        
        // When - upload fails (simulated through error injection)
        uploadModalPage.simulateUploadError("Network error occurred")
        
        // Then - error message should be displayed and modal should remain open
        uploadModalPage.shouldShowErrorMessage("Network error occurred")
            .shouldBeVisible()
            .shouldDisableConfirmButton()
    }

    @Test
    fun shouldValidateFileTypeAndShowErrorForInvalidFiles() {
        // Given - user has opened upload modal
        uploadModalPage.openModal()
            .shouldBeVisible()
        
        // When - user selects an invalid file type
        val invalidFile = createTestFile("test.txt", "text/plain")
        uploadModalPage.uploadFile(invalidFile)
        
        // Then - error message should be displayed
        uploadModalPage.shouldShowErrorMessage("Invalid file type")
            .shouldNotShowCropperImage()
            .shouldDisableConfirmButton()
        
        // Cleanup
        invalidFile.delete()
    }

    @Test
    fun shouldValidateFileSizeAndShowErrorForOversizedFiles() {
        // Given - user has opened upload modal
        uploadModalPage.openModal()
            .shouldBeVisible()
        
        // When - user selects a file that is too large (simulated)
        uploadModalPage.simulateOversizedFile()
        
        // Then - error message should be displayed
        uploadModalPage.shouldShowErrorMessage("File size too large")
            .shouldNotShowCropperImage()
            .shouldDisableConfirmButton()
    }

    @Test
    fun shouldBeAccessibleWithKeyboardNavigation() {
        // Given - user has opened upload modal
        uploadModalPage.openModal()
            .shouldBeVisible()
        
        // When - user navigates using keyboard
        uploadModalPage.testKeyboardNavigation()
        
        // Then - all interactive elements should be accessible
        uploadModalPage.shouldSupportKeyboardNavigation()
            .shouldHaveProperFocusOrder()
            .shouldSupportEscapeToClose()
    }

    @Test
    fun shouldMaintainModalStateWhenErrorOccurs() {
        // Given - user has uploaded an image
        uploadModalPage.openModal()
            .uploadFile(testImageFile)
            .shouldShowCropperImage()
        
        // When - error occurs during upload
        uploadModalPage.simulateUploadError("Server error")
        
        // Then - modal should remain open with error displayed
        uploadModalPage.shouldBeVisible()
            .shouldShowErrorMessage("Server error")
            .shouldShowCropperImage()
            .shouldDisableConfirmButton()
    }

    @Test
    fun shouldClearErrorWhenNewFileSelected() {
        // Given - user has an error state from previous upload attempt
        uploadModalPage.openModal()
            .uploadFile(createTestFile("invalid.txt", "text/plain"))
            .shouldShowErrorMessage("Invalid file type")
        
        // When - user selects a valid file
        uploadModalPage.uploadFile(testImageFile)
        
        // Then - error should be cleared and image should be displayed
        uploadModalPage.shouldNotShowErrorMessage()
            .shouldShowCropperImage()
            .shouldEnableConfirmButton()
    }

    /**
     * Creates a test image file for upload testing
     */
    private fun createTestImageFile(): File {
        val testFile = File.createTempFile("test_image", ".png")
        
        // Copy a minimal PNG image for testing
        val imageResource = this::class.java.getResourceAsStream("/test-images/sample.png")
        
        if (imageResource != null) {
            Files.copy(imageResource, testFile.toPath(), StandardCopyOption.REPLACE_EXISTING)
        } else {
            // Create a minimal valid PNG file if resource not available
            val minimalPng = byteArrayOf(
                0x89.toByte(), 0x50.toByte(), 0x4E.toByte(), 0x47.toByte(), 0x0D.toByte(), 0x0A.toByte(), 0x1A.toByte(), 0x0A.toByte(), // PNG signature
                0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x0D.toByte(), 0x49.toByte(), 0x48.toByte(), 0x44.toByte(), 0x52.toByte(), // IHDR chunk
                0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x01.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x01.toByte(), // 1x1 pixel
                0x01.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x37.toByte(), 0x6E.toByte(), 0xF9.toByte(), 0x24.toByte(), // IHDR data
                0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x0A.toByte(), 0x49.toByte(), 0x44.toByte(), 0x41.toByte(), 0x54.toByte(), // IDAT chunk
                0x78.toByte(), 0x9C.toByte(), 0x62.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x02.toByte(), 0x00.toByte(), 0x01.toByte(), // IDAT data
                0xE2.toByte(), 0x21.toByte(), 0xBC.toByte(), 0x33.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte(), // IDAT end
                0x49.toByte(), 0x45.toByte(), 0x4E.toByte(), 0x44.toByte(), 0xAE.toByte(), 0x42.toByte(), 0x60.toByte(), 0x82.toByte()  // IEND chunk
            )
            testFile.writeBytes(minimalPng)
        }
        
        return testFile
    }

    /**
     * Creates a test file with specified content and mime type
     */
    private fun createTestFile(filename: String, mimeType: String): File {
        val testFile = File.createTempFile("test_", filename)
        testFile.writeText("Test content for $mimeType file")
        return testFile
    }
}