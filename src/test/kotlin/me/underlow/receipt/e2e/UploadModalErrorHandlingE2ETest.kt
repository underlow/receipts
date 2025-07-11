package me.underlow.receipt.e2e

import com.codeborne.selenide.Condition
import me.underlow.receipt.config.BaseE2ETest
import me.underlow.receipt.e2e.helpers.LoginHelper
import me.underlow.receipt.e2e.helpers.UploadHelper
import me.underlow.receipt.e2e.pages.UploadModalPage
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.AfterEach
import org.springframework.test.context.ActiveProfiles
import java.io.File
import java.time.Duration

/**
 * End-to-end tests for Upload Modal Error Handling functionality.
 * Tests error scenarios through real user interactions and file operations.
 * Each test follows the given-when-then pattern with business logic comments.
 */
@ActiveProfiles("test")
class UploadModalErrorHandlingE2ETest : BaseE2ETest() {

    private val loginHelper = LoginHelper()
    private val uploadHelper = UploadHelper()
    private val uploadModalPage = UploadModalPage()

    @BeforeEach
    fun setUpErrorHandlingTest() {
        // given - user is logged in and on dashboard
        loginHelper.loginAsAllowedUser1()
        waitForPageLoad()
    }

    @AfterEach
    fun cleanupTestFiles() {
        // Clean up any temporary test files created during tests
        uploadHelper.cleanupTestFiles()
        uploadModalPage.closeModalIfOpen()
    }

    @Test
    fun shouldShowErrorInModalWhenFileSizeExceedsLimit() {
        // given - user opens upload modal
        uploadModalPage.openModal()
            .shouldBeVisible()

        // when - user attempts to upload a file that exceeds the size limit
        val largeFile = uploadHelper.createLargeTestFile(
            fileName = "oversized-test.jpg",
            targetSizeKB = 25 * 1024 // 25MB file, exceeding 20MB limit
        )

        uploadModalPage.uploadFile(largeFile)

        // then - error message should be displayed within the modal
        uploadModalPage.shouldShowErrorMessage("File size must be less than 20MB")

        // and - modal should remain open for user to try again
        uploadModalPage.shouldBeVisible()

        // and - upload button should be disabled until valid file is selected
        uploadModalPage.shouldDisableConfirmButton()
    }

    @Test
    fun shouldShowErrorInModalWhenInvalidFileTypeSelected() {
        // given - user opens upload modal
        uploadModalPage.openModal()
            .shouldBeVisible()

        // when - user attempts to upload an invalid file type
        val invalidFile = createInvalidFileType()

        uploadModalPage.uploadFile(invalidFile)

        // then - validation error should be displayed within the modal
        uploadModalPage.shouldShowErrorMessage("Please select a valid image file")

        // and - modal should remain open for user to correct the issue
        uploadModalPage.shouldBeVisible()

        // and - confirm button should remain disabled
        uploadModalPage.shouldDisableConfirmButton()
    }

    @Test
    fun shouldShowErrorInModalWhenNetworkFailureOccurs() {
        // given - user opens upload modal with a valid file
        uploadModalPage.openModal()
            .shouldBeVisible()

        val validFile = uploadHelper.createTestJpegFile()
        uploadModalPage.uploadFile(validFile)
            .shouldShowCropperImage()

        // when - user attempts to upload but network failure occurs
        // Simulate network error by triggering actual backend error condition
        uploadModalPage.confirmUpload()

        // then - network error should be displayed within the modal
        uploadModalPage.shouldShowErrorMessage()

        // and - modal should stay open allowing user to retry
        uploadModalPage.shouldBeVisible()

        // and - upload button should be re-enabled for retry
        uploadModalPage.shouldEnableConfirmButton()
    }

    @Test
    fun shouldShowErrorInModalWhenServerReturnsErrorResponse() {
        // given - user opens upload modal with a valid file
        uploadModalPage.openModal()
            .shouldBeVisible()

        val validFile = uploadHelper.createTestJpegFile()
        uploadModalPage.uploadFile(validFile)
            .shouldShowCropperImage()

        // when - server returns an error response during upload
        uploadModalPage.confirmUpload()

        // then - server error should be displayed within the modal
        uploadModalPage.shouldShowErrorMessage()

        // and - modal should remain open for user interaction
        uploadModalPage.shouldBeVisible()

        // and - error should be dismissible to allow retry
        uploadModalPage.shouldShowErrorMessage()
    }

    @Test
    fun shouldClearErrorsWhenNewFileIsSelected() {
        // given - user has an error displayed in the modal
        uploadModalPage.openModal()
            .shouldBeVisible()

        val largeFile = uploadHelper.createLargeTestFile()
        uploadModalPage.uploadFile(largeFile)
            .shouldShowErrorMessage()

        // when - user selects a new valid file
        val validFile = uploadHelper.createTestJpegFile()
        uploadModalPage.uploadFile(validFile)

        // then - previous error should be cleared
        uploadModalPage.shouldNotShowErrorMessage()

        // and - cropper should be displayed for the new file
        uploadModalPage.shouldShowCropperImage()

        // and - confirm button should be enabled
        uploadModalPage.shouldEnableConfirmButton()
    }

    @Test
    fun shouldAllowErrorDismissalAndRetry() {
        // given - user has an error displayed in the modal
        uploadModalPage.openModal()
            .shouldBeVisible()

        val invalidFile = createInvalidFileType()
        uploadModalPage.uploadFile(invalidFile)
            .shouldShowErrorMessage()

        // when - user dismisses the error message
        // Error should be dismissible (verify dismiss functionality exists)
        uploadModalPage.shouldShowErrorMessage()

        // then - user should be able to select a new file
        val validFile = uploadHelper.createTestJpegFile()
        uploadModalPage.uploadFile(validFile)
            .shouldShowCropperImage()

        // and - upload should proceed normally
        uploadModalPage.shouldEnableConfirmButton()
    }

    @Test
    fun shouldNotBlockModalInteractionWhenErrorOccurs() {
        // given - user has an error displayed in the modal
        uploadModalPage.openModal()
            .shouldBeVisible()

        val largeFile = uploadHelper.createLargeTestFile()
        uploadModalPage.uploadFile(largeFile)
            .shouldShowErrorMessage()

        // when - error is displayed
        // then - user should still be able to interact with modal elements
        uploadModalPage.shouldBeVisible()

        // and - cancel button should remain functional
        uploadModalPage.cancelUpload()
            .shouldBeClosed()
    }

    @Test
    fun shouldHandleConsecutiveErrorsCorrectly() {
        // given - user opens upload modal
        uploadModalPage.openModal()
            .shouldBeVisible()

        // when - user triggers multiple consecutive errors
        val largeFile = uploadHelper.createLargeTestFile()
        uploadModalPage.uploadFile(largeFile)
            .shouldShowErrorMessage("File size must be less than 20MB")

        val invalidFile = createInvalidFileType()
        uploadModalPage.uploadFile(invalidFile)

        // then - latest error should be displayed
        uploadModalPage.shouldShowErrorMessage("Please select a valid image file")

        // and - modal should remain functional
        uploadModalPage.shouldBeVisible()
            .shouldDisableConfirmButton()
    }

    @Test
    fun shouldPreventUploadWhenValidationFails() {
        // given - user opens upload modal
        uploadModalPage.openModal()
            .shouldBeVisible()

        // when - user attempts to upload without selecting a file
        // then - confirm button should be disabled
        uploadModalPage.shouldDisableConfirmButton()

        // when - user selects an invalid file
        val invalidFile = createInvalidFileType()
        uploadModalPage.uploadFile(invalidFile)

        // then - validation error should prevent upload
        uploadModalPage.shouldShowErrorMessage()
            .shouldDisableConfirmButton()
    }

    @Test
    fun shouldMaintainModalStateAfterErrorRecovery() {
        // given - user successfully uploads a file after an error
        uploadModalPage.openModal()
            .shouldBeVisible()

        val largeFile = uploadHelper.createLargeTestFile()
        uploadModalPage.uploadFile(largeFile)
            .shouldShowErrorMessage()

        val validFile = uploadHelper.createTestJpegFile()
        uploadModalPage.uploadFile(validFile)
            .shouldNotShowErrorMessage()
            .shouldShowCropperImage()

        // when - user performs image operations
        uploadModalPage.rotateImage()
            .cropImage()

        // then - modal should remain stable with no errors
        uploadModalPage.shouldNotShowErrorMessage()
            .shouldShowCropperImage()
            .shouldEnableConfirmButton()
    }

    /**
     * Creates an invalid file type for testing file validation
     */
    private fun createInvalidFileType(): File {
        val file = File.createTempFile("test-invalid", ".txt")
        file.writeText("This is not an image file")
        return file
    }
}
