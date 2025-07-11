package me.underlow.receipt.e2e

import me.underlow.receipt.config.BaseE2ETest
import me.underlow.receipt.e2e.helpers.UploadHelper
import me.underlow.receipt.e2e.pages.InboxPage
import me.underlow.receipt.e2e.pages.UploadModalPage
import me.underlow.receipt.e2e.pages.DashboardPage
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Disabled
import org.springframework.test.context.ActiveProfiles

/**
 * End-to-end tests for complete upload workflow.
 * Tests the entire user journey from file selection through upload completion
 * and verification in the inbox, covering all supported image formats and scenarios.
 *
 * Each test follows the given-when-then pattern and uses business logic terms.
 * Tests are isolated and leave the system in a clean state.
 */
@ActiveProfiles("test")
@Disabled("rework after upload is implemented")
class CompleteUploadE2ETest : BaseE2ETest() {

    private lateinit var uploadHelper: UploadHelper
    private lateinit var dashboardPage: DashboardPage
    private lateinit var uploadModalPage: UploadModalPage
    private lateinit var inboxPage: InboxPage

    @BeforeEach
    fun setUpCompleteUploadTest() {
        // Given - user is logged in and on dashboard
        performLoginWithAllowedUser()
        waitForPageLoad()

        // Initialize page objects and helpers
        uploadHelper = UploadHelper()
        dashboardPage = DashboardPage()
        uploadModalPage = UploadModalPage()
        inboxPage = InboxPage()

        // Verify dashboard is displayed
        dashboardPage.shouldBeDisplayed()
    }

    @AfterEach
    fun cleanupAfterTest() {
        // Clean up any test files created during the test
        uploadHelper.cleanupTestFiles()

        // Ensure modal is closed if still open
        try {
            uploadModalPage.closeModal()
        } catch (e: Exception) {
            // Ignore if modal is already closed
        }
    }

    @Test
    fun shouldUploadJpegImageAndDisplayInInbox() {
        // Given - user has opened upload modal
        uploadModalPage.openModal()
            .shouldBeVisible()

        // And - user has a valid JPEG image file
        val jpegFile = uploadHelper.createTestJpegFile()

        // When - user uploads the JPEG image
        uploadModalPage.uploadFile(jpegFile)
            .shouldShowCropperImage()
            .shouldEnableConfirmButton()
            .confirmUpload()

        // Then - upload modal should close
        uploadModalPage.shouldBeClosed()

        // And - uploaded image should appear in inbox
        inboxPage.navigateToInbox()
            .shouldBeDisplayed()
            .shouldContainUploadedItem()
            .shouldShowImageThumbnail()
    }

    @Test
    fun shouldUploadPngImageAndDisplayInInbox() {
        // Given - user has opened upload modal
        uploadModalPage.openModal()
            .shouldBeVisible()

        // And - user has a valid PNG image file
        val pngFile = uploadHelper.createTestPngFile()

        // When - user uploads the PNG image
        uploadModalPage.uploadFile(pngFile)
            .shouldShowCropperImage()
            .shouldEnableConfirmButton()
            .confirmUpload()

        // Then - upload modal should close
        uploadModalPage.shouldBeClosed()

        // And - uploaded image should appear in inbox
        inboxPage.navigateToInbox()
            .shouldBeDisplayed()
            .shouldContainUploadedItem()
            .shouldShowImageThumbnail()
    }

    @Test
    fun shouldUploadGifImageAndDisplayInInbox() {
        // Given - user has opened upload modal
        uploadModalPage.openModal()
            .shouldBeVisible()

        // And - user has a valid GIF image file
        val gifFile = uploadHelper.createTestGifFile()

        // When - user uploads the GIF image
        uploadModalPage.uploadFile(gifFile)
            .shouldShowCropperImage()
            .shouldEnableConfirmButton()
            .confirmUpload()

        // Then - upload modal should close
        uploadModalPage.shouldBeClosed()

        // And - uploaded image should appear in inbox
        inboxPage.navigateToInbox()
            .shouldBeDisplayed()
            .shouldContainUploadedItem()
            .shouldShowImageThumbnail()
    }

    @Test
    fun shouldUploadWebpImageAndDisplayInInbox() {
        // Given - user has opened upload modal
        uploadModalPage.openModal()
            .shouldBeVisible()

        // And - user has a valid WebP image file
        val webpFile = uploadHelper.createTestWebPFile()

        // When - user uploads the WebP image
        uploadModalPage.uploadFile(webpFile)
            .shouldShowCropperImage()
            .shouldEnableConfirmButton()
            .confirmUpload()

        // Then - upload modal should close
        uploadModalPage.shouldBeClosed()

        // And - uploaded image should appear in inbox
        inboxPage.navigateToInbox()
            .shouldBeDisplayed()
            .shouldContainUploadedItem()
            .shouldShowImageThumbnail()
    }

    @Test
    fun shouldUploadAndCropImageBeforeDisplayingInInbox() {
        // Given - user has opened upload modal
        uploadModalPage.openModal()
            .shouldBeVisible()

        // And - user has a valid image file
        val imageFile = uploadHelper.createTestJpegFile()

        // When - user uploads and crops the image
        uploadModalPage.uploadFile(imageFile)
            .shouldShowCropperImage()
            .cropImage()
            .shouldEnableConfirmButton()
            .confirmUpload()

        // Then - upload modal should close
        uploadModalPage.shouldBeClosed()

        // And - cropped image should appear in inbox
        inboxPage.navigateToInbox()
            .shouldBeDisplayed()
            .shouldContainUploadedItem()
            .shouldShowImageThumbnail()
    }

    @Test
    fun shouldUploadAndRotateImageBeforeDisplayingInInbox() {
        // Given - user has opened upload modal
        uploadModalPage.openModal()
            .shouldBeVisible()

        // And - user has a valid image file
        val imageFile = uploadHelper.createTestJpegFile()

        // When - user uploads and rotates the image
        uploadModalPage.uploadFile(imageFile)
            .shouldShowCropperImage()
            .rotateImage()
            .shouldEnableConfirmButton()
            .confirmUpload()

        // Then - upload modal should close
        uploadModalPage.shouldBeClosed()

        // And - rotated image should appear in inbox
        inboxPage.navigateToInbox()
            .shouldBeDisplayed()
            .shouldContainUploadedItem()
            .shouldShowImageThumbnail()
    }

    @Test
    fun shouldShowProgressDuringLargeFileUpload() {
        // Given - user has opened upload modal
        uploadModalPage.openModal()
            .shouldBeVisible()

        // And - user has a large image file
        val largeFile = uploadHelper.createLargeTestFile()

        // When - user uploads the large image
        uploadModalPage.uploadFile(largeFile)
            .shouldShowCropperImage()
            .shouldEnableConfirmButton()

        // And - starts the upload process
        uploadModalPage.confirmUpload()

        // Then - upload progress should be visible
        uploadModalPage.shouldShowProgress()
            .shouldShowProgressUpdates()

        // And - upload should eventually complete
        uploadModalPage.shouldBeClosed()

        // And - uploaded image should appear in inbox
        inboxPage.navigateToInbox()
            .shouldBeDisplayed()
            .shouldContainUploadedItem()
    }

    @Test
    fun shouldUploadLargeImageNearSizeLimit() {
        // Given - user has opened upload modal
        uploadModalPage.openModal()
            .shouldBeVisible()

        // And - user has a large image file near the size limit
        val largeFile = uploadHelper.createLargeTestFile("large-image.jpg", UploadHelper.LARGE_FILE_SIZE)

        // When - user uploads the large image
        uploadModalPage.uploadFile(largeFile)
            .shouldShowCropperImage()
            .shouldEnableConfirmButton()
            .confirmUpload()

        // Then - upload modal should close
        uploadModalPage.shouldBeClosed()

        // And - large image should be processed successfully
        inboxPage.navigateToInbox()
            .shouldBeDisplayed()
            .shouldContainUploadedItem()
            .shouldShowImageThumbnail()
    }

    @Test
    fun shouldUploadImageViaDragAndDrop() {
        // Given - user is on inbox page
        inboxPage.navigateToInbox()
            .shouldBeDisplayed()

        // And - user has a valid image file
        val imageFile = uploadHelper.createTestJpegFile()

        // When - user drags and drops image onto inbox
        uploadModalPage.dragAndDropFile(imageFile)
            .shouldBeVisible()
            .shouldShowCropperImage()
            .shouldEnableConfirmButton()
            .confirmUpload()

        // Then - upload modal should close
        uploadModalPage.shouldBeClosed()

        // And - uploaded image should appear in inbox
        inboxPage.shouldContainUploadedItem()
            .shouldShowImageThumbnail()
    }

    @Test
    fun shouldUploadMultipleImagesSequentially() {
        // Given - user uploads first image
        val firstImage = uploadHelper.createTestJpegFile("image1.jpg")
        uploadModalPage.openModal()
            .uploadFile(firstImage)
            .confirmUpload()
            .shouldBeClosed()

        // And - first image appears in inbox
        inboxPage.navigateToInbox()
            .shouldContainUploadedItem()

        // When - user uploads second image
        val secondImage = uploadHelper.createTestPngFile("image2.png")
        uploadModalPage.openModal()
            .uploadFile(secondImage)
            .confirmUpload()
            .shouldBeClosed()

        // Then - both images should appear in inbox
        inboxPage.navigateToInbox()
            .shouldContainAtLeastItems(2)

        // When - user uploads third image
        val thirdImage = uploadHelper.createTestGifFile("image3.gif")
        uploadModalPage.openModal()
            .uploadFile(thirdImage)
            .confirmUpload()
            .shouldBeClosed()

        // Then - all three images should appear in inbox
        inboxPage.navigateToInbox()
            .shouldContainAtLeastItems(3)
    }

    @Test
    fun shouldShowSuccessMessageAfterSuccessfulUpload() {
        // Given - user is on dashboard page
        dashboardPage.open()
            .shouldBeDisplayed()

        // And - user has opened upload modal
        uploadModalPage.openModal()
            .shouldBeVisible()

        // And - user has a valid image file
        val imageFile = uploadHelper.createTestJpegFile()

        // When - user uploads the image
        uploadModalPage.uploadFile(imageFile)
            .shouldShowCropperImage()
            .shouldEnableConfirmButton()
            .confirmUpload()

        // Then - success message should be shown
        uploadModalPage.shouldShowSuccessMessage()

        // And - upload process should complete and modal should close
        uploadModalPage.simulateUploadCompletion()

        // And - uploaded image should appear in inbox with correct metadata
        inboxPage.navigateToInbox()
            .shouldBeDisplayed()
            .shouldContainUploadedItem()
            .shouldShowImageThumbnail()
            .shouldShowUploadTimestamp()
            .shouldShowFileSize()
            .shouldShowMetadata()
    }

    @Test
    fun shouldProcessImageWithAllSecurityValidations() {
        // Given - user has opened upload modal
        uploadModalPage.openModal()
            .shouldBeVisible()

        // And - user has a secure image file that passes all validations
        val secureImageFile = uploadHelper.createSecureTestFile()

        // When - user uploads the secure image
        uploadModalPage.uploadFile(secureImageFile)
            .shouldShowCropperImage()
            .shouldEnableConfirmButton()
            .confirmUpload()

        // Then - upload modal should close without errors
        uploadModalPage.shouldBeClosed()
            .shouldNotShowErrorMessage()

        // And - image should pass all security checks and appear in inbox
        inboxPage.navigateToInbox()
            .shouldBeDisplayed()
            .shouldContainUploadedItem()
            .shouldShowImageThumbnail()
            .shouldNotShowErrorMessage()
    }

    @Test
    fun shouldCancelUploadProcess() {
        // Given - user has opened upload modal
        uploadModalPage.openModal()
            .shouldBeVisible()

        // And - user has selected an image file
        val imageFile = uploadHelper.createTestJpegFile()
        uploadModalPage.uploadFile(imageFile)
            .shouldShowCropperImage()
            .shouldEnableConfirmButton()

        // When - user cancels the upload
        uploadModalPage.cancelUpload()

        // Then - upload modal should close
        uploadModalPage.shouldBeClosed()

        // And - no new image should appear in inbox
        val initialCount = inboxPage.navigateToInbox().getItemCount()
        inboxPage.refreshInbox()
        val finalCount = inboxPage.getItemCount()

        assert(finalCount == initialCount) {
            "No new items should be added to inbox after canceling upload"
        }
    }

    @Test
    fun shouldResetImageTransformations() {
        // Given - user has opened upload modal
        uploadModalPage.openModal()
            .shouldBeVisible()

        // And - user has uploaded and transformed an image
        val imageFile = uploadHelper.createTestJpegFile()
        uploadModalPage.uploadFile(imageFile)
            .shouldShowCropperImage()
            .cropImage()
            .rotateImage()

        // When - user resets the image transformations
        uploadModalPage.resetImage()

        // Then - image should be back to original state
        uploadModalPage.shouldShowCropperImage()
            .shouldEnableConfirmButton()

        // And - user can still upload the reset image
        uploadModalPage.confirmUpload()
            .shouldBeClosed()

        // And - uploaded image should appear in inbox
        inboxPage.navigateToInbox()
            .shouldBeDisplayed()
            .shouldContainUploadedItem()
    }
}
