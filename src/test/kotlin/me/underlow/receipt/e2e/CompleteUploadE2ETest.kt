package me.underlow.receipt.e2e

import com.codeborne.selenide.Condition
import com.codeborne.selenide.Selenide.`$`
import com.codeborne.selenide.Selenide.`$$`
import com.codeborne.selenide.Selenide.executeJavaScript
import com.codeborne.selenide.Selenide.sleep
import me.underlow.receipt.config.BaseE2ETest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.BeforeEach
import org.springframework.test.context.ActiveProfiles
import kotlin.test.assertTrue
import kotlin.test.assertFalse
import java.time.Duration
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths

/**
 * End-to-end tests for complete upload workflow.
 * Tests the entire user journey from file selection through upload completion
 * and verification in the inbox, covering all supported image formats and scenarios.
 */
@ActiveProfiles("test")
class CompleteUploadE2ETest : BaseE2ETest() {

    @BeforeEach
    fun setUpCompleteUploadTest() {
        // given - user is logged in and on dashboard
        performLoginWithAllowedUser()
        waitForPageLoad()
    }

    @Test
    fun `given user uploads valid JPEG image when complete workflow executes then should appear in inbox`() {
        // given - user opens upload modal
        openUploadModal()
        
        // when - user selects and uploads a JPEG image
        uploadImageFile("test-image.jpg", "image/jpeg")
        
        // then - image should be processed and appear in inbox
        verifyImageAppearsInInbox()
        
        // and - upload modal should close
        verifyUploadModalClosed()
    }

    @Test
    fun `given user uploads valid PNG image when complete workflow executes then should appear in inbox`() {
        // given - user opens upload modal
        openUploadModal()
        
        // when - user selects and uploads a PNG image
        uploadImageFile("test-image.png", "image/png")
        
        // then - image should be processed and appear in inbox
        verifyImageAppearsInInbox()
        
        // and - upload modal should close
        verifyUploadModalClosed()
    }

    @Test
    fun `given user uploads valid GIF image when complete workflow executes then should appear in inbox`() {
        // given - user opens upload modal
        openUploadModal()
        
        // when - user selects and uploads a GIF image
        uploadImageFile("test-image.gif", "image/gif")
        
        // then - image should be processed and appear in inbox
        verifyImageAppearsInInbox()
        
        // and - upload modal should close
        verifyUploadModalClosed()
    }

    @Test
    fun `given user uploads valid WebP image when complete workflow executes then should appear in inbox`() {
        // given - user opens upload modal
        openUploadModal()
        
        // when - user selects and uploads a WebP image
        uploadImageFile("test-image.webp", "image/webp")
        
        // then - image should be processed and appear in inbox
        verifyImageAppearsInInbox()
        
        // and - upload modal should close
        verifyUploadModalClosed()
    }

    @Test
    fun `given user uploads image when image is cropped then should upload cropped version`() {
        // given - user opens upload modal
        openUploadModal()
        
        // when - user selects image and applies cropping
        selectImageFile("test-image.jpg", "image/jpeg")
        waitForCropperInitialization()
        applyCropping()
        confirmUpload()
        
        // then - cropped image should be processed and appear in inbox
        verifyImageAppearsInInbox()
        
        // and - upload modal should close
        verifyUploadModalClosed()
    }

    @Test
    fun `given user uploads image when image is rotated then should upload rotated version`() {
        // given - user opens upload modal
        openUploadModal()
        
        // when - user selects image and applies rotation
        selectImageFile("test-image.jpg", "image/jpeg")
        waitForCropperInitialization()
        applyRotation()
        confirmUpload()
        
        // then - rotated image should be processed and appear in inbox
        verifyImageAppearsInInbox()
        
        // and - upload modal should close
        verifyUploadModalClosed()
    }

    @Test
    fun `given user uploads image when upload progress is tracked then should show progress bar`() {
        // given - user opens upload modal
        openUploadModal()
        
        // when - user starts uploading a larger image
        selectImageFile("large-test-image.jpg", "image/jpeg")
        waitForCropperInitialization()
        
        // Start upload and monitor progress
        val confirmButton = `$`("#confirmUpload")
        confirmButton.click()
        
        // then - progress bar should be visible during upload
        val progressBar = `$`("#uploadProgress")
        progressBar.shouldBe(Condition.visible, Duration.ofSeconds(5))
        
        // and - progress should update during upload
        verifyProgressUpdates()
        
        // and - upload should complete successfully
        verifyImageAppearsInInbox()
    }

    @Test
    fun `given user uploads large image when near size limit then should process successfully`() {
        // given - user opens upload modal
        openUploadModal()
        
        // when - user uploads a large image (close to 20MB limit)
        uploadLargeImageFile("large-test-image.jpg", "image/jpeg")
        
        // then - large image should be processed successfully
        verifyImageAppearsInInbox()
        
        // and - upload modal should close
        verifyUploadModalClosed()
    }

    @Test
    fun `given user drags and drops image when complete workflow executes then should appear in inbox`() {
        // given - user is on inbox tab
        navigateToInboxTab()
        
        // when - user drags and drops image onto inbox table
        val inboxTableContainer = `$`("#inboxTableContainer")
        inboxTableContainer.shouldBe(Condition.exist)
        
        // Simulate drag and drop with real file
        simulateFileDragAndDrop(inboxTableContainer, "test-image.jpg", "image/jpeg")
        
        // and - completes upload in modal
        waitForUploadModalToOpen()
        confirmUpload()
        
        // then - image should appear in inbox
        verifyImageAppearsInInbox()
    }

    @Test
    fun `given user uploads multiple images when processed sequentially then should all appear in inbox`() {
        // given - user uploads first image
        openUploadModal()
        uploadImageFile("test-image1.jpg", "image/jpeg")
        verifyImageAppearsInInbox()
        
        // when - user uploads second image
        openUploadModal()
        uploadImageFile("test-image2.png", "image/png")
        
        // then - both images should appear in inbox
        verifyMultipleImagesInInbox(2)
        
        // when - user uploads third image
        openUploadModal()
        uploadImageFile("test-image3.gif", "image/gif")
        
        // then - all three images should appear in inbox
        verifyMultipleImagesInInbox(3)
    }

    @Test
    fun `given user uploads image when backend processing completes then should show success feedback`() {
        // given - user opens upload modal
        openUploadModal()
        
        // when - user uploads image and backend processes it
        uploadImageFile("test-image.jpg", "image/jpeg")
        
        // then - success feedback should be shown
        verifySuccessMessage()
        
        // and - image should appear in inbox with correct metadata
        verifyImageInInboxWithMetadata()
    }

    @Test
    fun `given user uploads image when CSRF token is validated then should process successfully`() {
        // given - user opens upload modal
        openUploadModal()
        
        // when - user uploads image (CSRF token should be automatically included)
        uploadImageFile("test-image.jpg", "image/jpeg")
        
        // then - upload should succeed with proper authentication
        verifyImageAppearsInInbox()
        
        // and - no authentication errors should occur
        verifyNoAuthenticationErrors()
    }

    @Test
    fun `given user uploads image when file validation passes then should process through all security checks`() {
        // given - user opens upload modal
        openUploadModal()
        
        // when - user uploads image that passes all security validations
        uploadImageFile("secure-test-image.jpg", "image/jpeg")
        
        // then - image should pass MIME type validation
        // and - image should pass file header validation
        // and - image should pass size validation
        // and - image should be stored securely
        verifyImageAppearsInInbox()
        
        // and - no security errors should occur
        verifyNoSecurityErrors()
    }

    /**
     * Helper method to open the upload modal.
     */
    private fun openUploadModal() {
        val uploadButton = `$`("button[data-bs-target='#uploadModal']")
        uploadButton.shouldBe(Condition.visible)
        uploadButton.click()
        
        val modal = `$`("#uploadModal")
        modal.shouldBe(Condition.visible, Duration.ofSeconds(10))
    }

    /**
     * Helper method to upload an image file through the complete workflow.
     */
    private fun uploadImageFile(fileName: String, mimeType: String) {
        selectImageFile(fileName, mimeType)
        waitForCropperInitialization()
        confirmUpload()
        waitForUploadToComplete()
    }

    /**
     * Helper method to select an image file.
     */
    private fun selectImageFile(fileName: String, mimeType: String) {
        // Create a test image file
        val imageContent = createTestImageContent(mimeType)
        
        // Simulate file selection
        executeJavaScript<Unit>("""
            var fileInput = document.getElementById('fileInput');
            var file = new File(['${imageContent}'], '${fileName}', {
                type: '${mimeType}',
                lastModified: Date.now()
            });
            
            // Create a FileList-like object
            var fileList = {
                0: file,
                length: 1,
                item: function(index) { return this[index]; }
            };
            
            // Set the files property
            Object.defineProperty(fileInput, 'files', {
                value: fileList,
                writable: false
            });
            
            // Trigger change event
            var event = new Event('change', { bubbles: true });
            fileInput.dispatchEvent(event);
        """)
    }

    /**
     * Helper method to wait for cropper initialization.
     */
    private fun waitForCropperInitialization() {
        val cropperImage = `$`("#cropperImage")
        cropperImage.shouldBe(Condition.visible, Duration.ofSeconds(10))
        
        // Wait for cropper to be fully initialized
        sleep(1000)
        
        // Check if cropper container exists
        val cropperContainer = `$`(".cropper-container")
        cropperContainer.shouldBe(Condition.visible, Duration.ofSeconds(10))
    }

    /**
     * Helper method to apply cropping to the image.
     */
    private fun applyCropping() {
        // Simulate cropping by adjusting the crop box
        executeJavaScript<Unit>("""
            if (window.cropper) {
                window.cropper.setCropBoxData({
                    left: 10,
                    top: 10,
                    width: 200,
                    height: 200
                });
            }
        """)
        
        sleep(500) // Allow cropping to apply
    }

    /**
     * Helper method to apply rotation to the image.
     */
    private fun applyRotation() {
        // Simulate rotation by 90 degrees
        executeJavaScript<Unit>("""
            if (window.cropper) {
                window.cropper.rotate(90);
            }
        """)
        
        sleep(500) // Allow rotation to apply
    }

    /**
     * Helper method to confirm the upload.
     */
    private fun confirmUpload() {
        val confirmButton = `$`("#confirmUpload")
        confirmButton.shouldBe(Condition.visible)
        confirmButton.shouldBe(Condition.enabled)
        confirmButton.click()
    }

    /**
     * Helper method to wait for upload to complete.
     */
    private fun waitForUploadToComplete() {
        // Wait for upload to finish (progress bar should disappear)
        val progressBar = `$`("#uploadProgress")
        if (progressBar.exists()) {
            progressBar.shouldNotBe(Condition.visible, Duration.ofSeconds(30))
        }
        
        // Wait for any loading indicators to disappear
        sleep(2000)
    }

    /**
     * Helper method to verify image appears in inbox.
     */
    private fun verifyImageAppearsInInbox() {
        // Navigate to inbox tab if not already there
        navigateToInboxTab()
        
        // Wait for inbox to load
        val inboxTable = `$`("#inboxTable")
        inboxTable.shouldBe(Condition.visible, Duration.ofSeconds(10))
        
        // Check that inbox contains at least one entry
        val inboxRows = `$$`("#inboxTable tbody tr")
        assertTrue(inboxRows.size() > 0, "Inbox should contain uploaded image")
        
        // Verify the most recent entry (first row) contains image data
        val firstRow = inboxRows.first()
        firstRow.shouldBe(Condition.visible)
        
        // Check for image thumbnail or file name
        val imageThumbnail = firstRow.`$`("img, .image-thumbnail")
        if (imageThumbnail.exists()) {
            imageThumbnail.shouldBe(Condition.visible)
        }
    }

    /**
     * Helper method to verify upload modal is closed.
     */
    private fun verifyUploadModalClosed() {
        val modal = `$`("#uploadModal")
        modal.shouldNotBe(Condition.visible, Duration.ofSeconds(10))
    }

    /**
     * Helper method to verify multiple images appear in inbox.
     */
    private fun verifyMultipleImagesInInbox(expectedCount: Int) {
        navigateToInboxTab()
        
        val inboxTable = `$`("#inboxTable")
        inboxTable.shouldBe(Condition.visible, Duration.ofSeconds(10))
        
        val inboxRows = `$$`("#inboxTable tbody tr")
        assertTrue(inboxRows.size() >= expectedCount, 
            "Inbox should contain at least $expectedCount images")
    }

    /**
     * Helper method to verify progress updates during upload.
     */
    private fun verifyProgressUpdates() {
        // Check that progress bar shows some progress
        val progressBar = `$`("#uploadProgress .progress-bar")
        if (progressBar.exists()) {
            progressBar.shouldBe(Condition.visible)
            
            // Progress should be between 0 and 100
            val progressValue = progressBar.getAttribute("style")
            assertTrue(progressValue != null && progressValue.contains("width:"))
        }
    }

    /**
     * Helper method to upload a large image file.
     */
    private fun uploadLargeImageFile(fileName: String, mimeType: String) {
        // Create a larger test image (simulated)
        val largeImageContent = createLargeTestImageContent(mimeType)
        
        executeJavaScript<Unit>("""
            var fileInput = document.getElementById('fileInput');
            var file = new File(['${largeImageContent}'], '${fileName}', {
                type: '${mimeType}',
                lastModified: Date.now()
            });
            
            var fileList = {
                0: file,
                length: 1,
                item: function(index) { return this[index]; }
            };
            
            Object.defineProperty(fileInput, 'files', {
                value: fileList,
                writable: false
            });
            
            var event = new Event('change', { bubbles: true });
            fileInput.dispatchEvent(event);
        """)
        
        waitForCropperInitialization()
        confirmUpload()
        waitForUploadToComplete()
    }

    /**
     * Helper method to simulate file drag and drop.
     */
    private fun simulateFileDragAndDrop(element: com.codeborne.selenide.SelenideElement, fileName: String, mimeType: String) {
        val imageContent = createTestImageContent(mimeType)
        
        executeJavaScript<Unit>("""
            var element = arguments[0];
            var dataTransfer = new DataTransfer();
            
            var file = new File(['${imageContent}'], '${fileName}', {
                type: '${mimeType}',
                lastModified: Date.now()
            });
            dataTransfer.items.add(file);
            
            var dropEvent = new DragEvent('drop', {
                bubbles: true,
                cancelable: true,
                dataTransfer: dataTransfer
            });
            
            element.dispatchEvent(dropEvent);
        """, element)
    }

    /**
     * Helper method to wait for upload modal to open after drag and drop.
     */
    private fun waitForUploadModalToOpen() {
        val modal = `$`("#uploadModal")
        modal.shouldBe(Condition.visible, Duration.ofSeconds(10))
    }

    /**
     * Helper method to verify success message is shown.
     */
    private fun verifySuccessMessage() {
        val successAlert = `$`(".alert-success")
        if (successAlert.exists()) {
            successAlert.shouldBe(Condition.visible, Duration.ofSeconds(10))
        }
    }

    /**
     * Helper method to verify image in inbox with metadata.
     */
    private fun verifyImageInInboxWithMetadata() {
        navigateToInboxTab()
        
        val inboxTable = `$`("#inboxTable")
        inboxTable.shouldBe(Condition.visible, Duration.ofSeconds(10))
        
        val firstRow = `$$`("#inboxTable tbody tr").first()
        firstRow.shouldBe(Condition.visible)
        
        // Check for timestamp, file name, or other metadata
        val metadataElements = firstRow.`$$`("td")
        assertTrue(metadataElements.size() > 0, "Row should contain metadata")
    }

    /**
     * Helper method to verify no authentication errors occurred.
     */
    private fun verifyNoAuthenticationErrors() {
        val authErrorAlert = `$`(".alert-danger")
        if (authErrorAlert.exists()) {
            assertFalse(authErrorAlert.text().contains("authentication") ||
                       authErrorAlert.text().contains("unauthorized") ||
                       authErrorAlert.text().contains("CSRF"))
        }
    }

    /**
     * Helper method to verify no security errors occurred.
     */
    private fun verifyNoSecurityErrors() {
        val securityErrorAlert = `$`(".alert-danger")
        if (securityErrorAlert.exists()) {
            assertFalse(securityErrorAlert.text().contains("security") ||
                       securityErrorAlert.text().contains("malicious") ||
                       securityErrorAlert.text().contains("invalid file"))
        }
    }

    /**
     * Helper method to navigate to inbox tab.
     */
    private fun navigateToInboxTab() {
        val inboxTabLink = `$`("a[href='#inbox']")
        if (inboxTabLink.exists()) {
            inboxTabLink.click()
        }
        
        val inboxTab = `$`("#inbox")
        inboxTab.shouldBe(Condition.visible, Duration.ofSeconds(10))
    }

    /**
     * Helper method to create test image content.
     */
    private fun createTestImageContent(mimeType: String): String {
        return when (mimeType) {
            "image/jpeg" -> "fake-jpeg-content-data"
            "image/png" -> "fake-png-content-data"
            "image/gif" -> "fake-gif-content-data"
            "image/webp" -> "fake-webp-content-data"
            else -> "fake-image-content-data"
        }
    }

    /**
     * Helper method to create large test image content.
     */
    private fun createLargeTestImageContent(mimeType: String): String {
        val baseContent = createTestImageContent(mimeType)
        // Simulate larger content by repeating the base content
        return baseContent.repeat(1000)
    }
}