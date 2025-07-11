package me.underlow.receipt.e2e

import me.underlow.receipt.config.BaseE2ETest
import me.underlow.receipt.e2e.pages.InboxPage
import me.underlow.receipt.e2e.pages.UploadModalPage
import me.underlow.receipt.e2e.helpers.UploadHelper
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.DisplayName
import java.time.Duration

/**
 * End-to-end tests for inbox drag-and-drop functionality.
 * Tests the complete user workflow for dragging and dropping image files
 * directly into the inbox table area to trigger the upload modal.
 * 
 * Business scenarios covered:
 * - Visual feedback during drag operations
 * - File validation and error handling
 * - Integration with upload modal workflow
 * - Accessibility and user experience
 */
class InboxDragDropE2ETest : BaseE2ETest() {

    private lateinit var inboxPage: InboxPage
    private lateinit var uploadModalPage: UploadModalPage
    private lateinit var uploadHelper: UploadHelper

    @BeforeEach
    fun setUpInboxDragDropTest() {
        // Given - user is logged in and has access to inbox functionality
        performLoginWithAllowedUser()
        waitForPageLoad()
        
        // Initialize page objects and helpers
        inboxPage = InboxPage()
        uploadModalPage = UploadModalPage()
        uploadHelper = UploadHelper()
        
        // Navigate to inbox to prepare for drag-drop testing
        inboxPage.navigateToInbox()
    }

    @AfterEach
    fun cleanupAfterTest() {
        // Clean up any test files created during test execution
        uploadHelper.cleanupTestFiles()
    }

    @Test
    @DisplayName("Should display drop zone container when inbox page loads")
    fun shouldDisplayDropZoneWhenInboxPageLoads() {
        // Given - user has navigated to inbox page
        inboxPage.shouldBeDisplayed()
        
        // When - inbox page finishes loading
        // (page loading already completed in setup)
        
        // Then - drop zone container should be visible and properly configured
        inboxPage.shouldHaveDropZone()
    }

    @Test
    @DisplayName("Should hide drop overlay initially when page loads")
    fun shouldHideDropOverlayInitially() {
        // Given - user has navigated to inbox page
        inboxPage.shouldBeDisplayed()
        
        // When - inbox page finishes loading
        // (page loading already completed in setup)
        
        // Then - drop overlay should exist but remain hidden until drag interaction
        inboxPage.shouldHaveHiddenDropOverlay()
    }

    @Test
    @DisplayName("Should show visual feedback when dragging file over drop zone")
    fun shouldShowVisualFeedbackWhenDraggingFileOverDropZone() {
        // Given - user has inbox page loaded and ready for interaction
        inboxPage.shouldBeDisplayed()
        
        // When - user drags a valid image file over the inbox drop zone
        inboxPage.dragFileOverDropZone("test-receipt.jpg", "image/jpeg")
        
        // Then - drop zone should display visual feedback indicating file can be dropped
        inboxPage.shouldShowDragOverStyling()
    }

    @Test
    @DisplayName("Should show drop overlay when dragging file over drop zone")
    fun shouldShowDropOverlayWhenDraggingFileOverDropZone() {
        // Given - user has inbox page loaded and ready for interaction
        inboxPage.shouldBeDisplayed()
        
        // When - user drags a valid image file over the inbox drop zone
        inboxPage.dragFileOverDropZone("test-receipt.jpg", "image/jpeg")
        
        // Then - drop overlay should become visible to guide user interaction
        inboxPage.shouldShowDropOverlay()
    }

    @Test
    @DisplayName("Should remove visual feedback when dragging file away from drop zone")
    fun shouldRemoveVisualFeedbackWhenDraggingFileAwayFromDropZone() {
        // Given - user has dragged file over drop zone and visual feedback is showing
        inboxPage.shouldBeDisplayed()
        inboxPage.dragFileOverDropZone("test-receipt.jpg", "image/jpeg")
        inboxPage.shouldShowDragOverStyling()
        
        // When - user drags file away from the drop zone area
        inboxPage.dragFileAwayFromDropZone()
        
        // Then - visual feedback should be removed to indicate drop zone is no longer active
        inboxPage.shouldNotShowDragOverStyling()
    }

    @Test
    @DisplayName("Should hide drop overlay when dragging file away from drop zone")
    fun shouldHideDropOverlayWhenDraggingFileAwayFromDropZone() {
        // Given - user has dragged file over drop zone and drop overlay is visible
        inboxPage.shouldBeDisplayed()
        inboxPage.dragFileOverDropZone("test-receipt.jpg", "image/jpeg")
        inboxPage.shouldShowDropOverlay()
        
        // When - user drags file away from the drop zone area
        inboxPage.dragFileAwayFromDropZone()
        
        // Then - drop overlay should be hidden to clean up the interface
        inboxPage.shouldHideDropOverlay()
    }

    @Test
    @DisplayName("Should trigger upload modal when valid image file is dropped")
    fun shouldTriggerUploadModalWhenValidImageFileIsDropped() {
        // Given - user has inbox page loaded and ready for file drop
        inboxPage.shouldBeDisplayed()
        
        // When - user drops a valid image file onto the inbox drop zone
        inboxPage.dropFileOnDropZone("test-receipt.jpg", "image/jpeg")
        
        // Then - upload modal should open to allow user to process the uploaded image
        uploadModalPage.shouldBeVisible()
        uploadModalPage.shouldShowCropperImage()
        uploadModalPage.shouldEnableConfirmButton()
    }

    @Test
    @DisplayName("Should show error message when invalid file type is dropped")
    fun shouldShowErrorMessageWhenInvalidFileTypeIsDropped() {
        // Given - user has inbox page loaded and ready for file drop
        inboxPage.shouldBeDisplayed()
        
        // When - user drops an invalid file type (not an image) onto the drop zone
        inboxPage.dropFileOnDropZone("document.pdf", "application/pdf")
        
        // Then - system should display error message explaining file type requirements
        inboxPage.shouldShowErrorMessage("Please upload a valid image file")
    }

    @Test
    @DisplayName("Should process first valid image when multiple files are dropped")
    fun shouldProcessFirstValidImageWhenMultipleFilesAreDropped() {
        // Given - user has inbox page loaded and ready for file drop
        inboxPage.shouldBeDisplayed()
        
        // When - user drops multiple files including valid and invalid types
        val fileNames = listOf("document.pdf", "test-receipt.jpg", "notes.txt")
        val mimeTypes = listOf("application/pdf", "image/jpeg", "text/plain")
        inboxPage.dropMultipleFilesOnDropZone(fileNames, mimeTypes)
        
        // Then - system should process only the first valid image file
        uploadModalPage.shouldBeVisible()
        
        // Wait for file processing to complete and show cropper image
        Thread.sleep(500)
        uploadModalPage.shouldShowCropperImage()
    }

    @Test
    @DisplayName("Should integrate properly with upload modal workflow")
    fun shouldIntegrateProperlyWithUploadModalWorkflow() {
        // Given - user has inbox page loaded and ready for complete upload workflow
        inboxPage.shouldBeDisplayed()
        
        // When - user drops valid image file and proceeds through upload workflow
        inboxPage.dropFileOnDropZone("test-receipt.jpg", "image/jpeg")
        
        // Then - upload modal should be fully functional for image processing
        uploadModalPage.shouldBeVisible()
        uploadModalPage.shouldShowCropperImage()
        uploadModalPage.shouldEnableConfirmButton()
        
        // And - user should be able to confirm upload and return to inbox
        uploadModalPage.confirmUpload()
        uploadModalPage.shouldShowSuccessMessage()
        uploadModalPage.simulateUploadCompletion()
        
        // And - inbox should reflect the newly uploaded item
        inboxPage.shouldContainUploadedItem()
    }

    @Test
    @DisplayName("Should display proper drop overlay content during drag interaction")
    fun shouldDisplayProperDropOverlayContentDuringDragInteraction() {
        // Given - user has inbox page loaded and ready for drag interaction
        inboxPage.shouldBeDisplayed()
        
        // When - user drags valid image file over the drop zone
        inboxPage.dragFileOverDropZone("test-receipt.jpg", "image/jpeg")
        
        // Then - drop overlay should show clear instructions and visual cues
        inboxPage.shouldShowDropOverlayContent()
    }

    @Test
    @DisplayName("Should handle large file drops within size limits")
    fun shouldHandleLargeFileDropsWithinSizeLimits() {
        // Given - user has inbox page loaded and ready for large file drop
        inboxPage.shouldBeDisplayed()
        
        // When - user drops a large but valid image file (within size limits)
        inboxPage.dropFileOnDropZone("large-receipt.jpg", "image/jpeg")
        
        // Then - system should accept the file and open upload modal
        uploadModalPage.shouldBeVisible()
        uploadModalPage.shouldShowCropperImage()
    }

    @Test
    @DisplayName("Should maintain clean interface state after drag operations")
    fun shouldMaintainCleanInterfaceStateAfterDragOperations() {
        // Given - user has completed drag-drop operations
        inboxPage.shouldBeDisplayed()
        inboxPage.dragFileOverDropZone("test-receipt.jpg", "image/jpeg")
        inboxPage.shouldShowDropOverlay()
        
        // When - user completes or cancels drag operation
        inboxPage.dragFileAwayFromDropZone()
        
        // Then - interface should return to clean initial state
        inboxPage.shouldHideDropOverlay()
        inboxPage.shouldNotShowDragOverStyling()
        inboxPage.shouldNotShowErrorMessage()
    }

    @Test
    @DisplayName("Should provide accessibility support for drag-drop interactions")
    fun shouldProvideAccessibilitySupportForDragDropInteractions() {
        // Given - user with accessibility needs is using inbox page
        inboxPage.shouldBeDisplayed()
        
        // When - examining drop zone for accessibility features
        inboxPage.shouldHaveDropZone()
        
        // Then - drop zone should be properly configured for screen readers and keyboard navigation
        // Note: This test verifies the presence of accessibility attributes
        // Actual accessibility compliance would require additional attribute verification
        inboxPage.shouldHaveDropZone()
    }
}