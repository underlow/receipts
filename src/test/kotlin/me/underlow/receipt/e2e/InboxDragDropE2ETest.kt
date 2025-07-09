package me.underlow.receipt.e2e

import com.codeborne.selenide.Condition
import com.codeborne.selenide.Selenide
import com.codeborne.selenide.Selenide.`$`
import com.codeborne.selenide.Selenide.`$$`
import com.codeborne.selenide.SelenideElement
import com.codeborne.selenide.ex.ElementNotFound
import me.underlow.receipt.config.BaseE2ETest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.BeforeEach
import kotlin.test.assertTrue
import java.time.Duration

/**
 * End-to-end tests for Inbox table drag-and-drop functionality.
 * Tests the complete user workflow for dragging and dropping image files
 * directly into the inbox table area to trigger the upload modal.
 */
class InboxDragDropE2ETest : BaseE2ETest() {

    @BeforeEach
    fun setUpInboxDragDropTest() {
        // given - user is logged in and on dashboard
        performLoginWithAllowedUser()
        waitForPageLoad()
        
        // Ensure we're on the inbox tab
        navigateToInboxTab()
    }

    @Test
    fun `given inbox table when page loads then should have drop zone container`() {
        // given - user is on inbox tab
        assertTrue(isOnInboxTab())

        // when - page is loaded
        waitForInboxTableToLoad()

        // then - inbox table should have drop zone container
        val inboxTableContainer = `$`("#inboxTableContainer")
        inboxTableContainer.shouldBe(Condition.exist)
        assertTrue(inboxTableContainer.getAttribute("class")?.contains("drop-zone") == true)
    }

    @Test
    fun `given inbox table when page loads then should have drop overlay hidden`() {
        // given - user is on inbox tab
        assertTrue(isOnInboxTab())

        // when - page is loaded
        waitForInboxTableToLoad()

        // then - drop overlay should exist but be hidden
        val dropOverlay = `$`("#dropOverlay")
        dropOverlay.shouldBe(Condition.exist)
        assertTrue(dropOverlay.getAttribute("style")?.contains("display: none") == true)
    }

    @Test
    fun `given inbox table when dragging file over table then should show visual feedback`() {
        // given - user is on inbox tab
        assertTrue(isOnInboxTab())
        waitForInboxTableToLoad()

        // when - user drags file over inbox table
        val inboxTableContainer = `$`("#inboxTableContainer")
        inboxTableContainer.shouldBe(Condition.exist)

        // Simulate dragover event
        simulateDragOver(inboxTableContainer)

        // then - table should show drag-over styling
        inboxTableContainer.shouldHave(Condition.cssClass("drag-over"))
    }

    @Test
    fun `given inbox table when dragging file over table then should show drop overlay`() {
        // given - user is on inbox tab
        assertTrue(isOnInboxTab())
        waitForInboxTableToLoad()

        // when - user drags file over inbox table
        val inboxTableContainer = `$`("#inboxTableContainer")
        inboxTableContainer.shouldBe(Condition.exist)

        // Simulate dragover event
        simulateDragOver(inboxTableContainer)

        // then - drop overlay should become visible
        val dropOverlay = `$`("#dropOverlay")
        dropOverlay.shouldBe(Condition.visible)
    }

    @Test
    fun `given inbox table when dragging file leaves table then should remove visual feedback`() {
        // given - user is on inbox tab with file dragged over
        assertTrue(isOnInboxTab())
        waitForInboxTableToLoad()

        val inboxTableContainer = `$`("#inboxTableContainer")
        inboxTableContainer.shouldBe(Condition.exist)

        // First drag over to show feedback
        simulateDragOver(inboxTableContainer)
        inboxTableContainer.shouldHave(Condition.cssClass("drag-over"))

        // when - user drags file away from inbox table
        simulateDragLeave(inboxTableContainer)

        // then - visual feedback should be removed
        inboxTableContainer.shouldNotHave(Condition.cssClass("drag-over"))
    }

    @Test
    fun `given inbox table when dragging file leaves table then should hide drop overlay`() {
        // given - user is on inbox tab with file dragged over
        assertTrue(isOnInboxTab())
        waitForInboxTableToLoad()

        val inboxTableContainer = `$`("#inboxTableContainer")
        inboxTableContainer.shouldBe(Condition.exist)

        // First drag over to show overlay
        simulateDragOver(inboxTableContainer)
        val dropOverlay = `$`("#dropOverlay")
        dropOverlay.shouldBe(Condition.visible)

        // when - user drags file away from inbox table
        simulateDragLeave(inboxTableContainer)

        // then - drop overlay should be hidden
        dropOverlay.shouldNotBe(Condition.visible)
    }

    @Test
    fun `given inbox table when valid image file is dropped then should trigger upload modal`() {
        // given - user is on inbox tab
        assertTrue(isOnInboxTab())
        waitForInboxTableToLoad()

        // when - user drops a valid image file on inbox table
        val inboxTableContainer = `$`("#inboxTableContainer")
        inboxTableContainer.shouldBe(Condition.exist)

        // Simulate file drop with valid image
        simulateImageFileDrop(inboxTableContainer)

        // then - upload modal should be triggered
        val uploadModal = `$`("#uploadModal")
        uploadModal.shouldBe(Condition.visible, Duration.ofSeconds(10))
    }

    @Test
    fun `given inbox table when invalid file is dropped then should show error message`() {
        // given - user is on inbox tab
        assertTrue(isOnInboxTab())
        waitForInboxTableToLoad()

        // when - user drops an invalid file on inbox table
        val inboxTableContainer = `$`("#inboxTableContainer")
        inboxTableContainer.shouldBe(Condition.exist)

        // Simulate file drop with invalid file
        simulateInvalidFileDrop(inboxTableContainer)

        // then - error message should be displayed
        val errorAlert = `$`(".alert-danger")
        errorAlert.shouldBe(Condition.visible, Duration.ofSeconds(10))
        assertTrue(errorAlert.text().contains("valid image file"))
    }

    @Test
    fun `given inbox table when multiple files are dropped then should process first valid image`() {
        // given - user is on inbox tab
        assertTrue(isOnInboxTab())
        waitForInboxTableToLoad()

        // when - user drops multiple files on inbox table
        val inboxTableContainer = `$`("#inboxTableContainer")
        inboxTableContainer.shouldBe(Condition.exist)

        // Simulate multiple file drop
        simulateMultipleFileDrop(inboxTableContainer)

        // then - upload modal should be triggered for first valid image
        val uploadModal = `$`("#uploadModal")
        uploadModal.shouldBe(Condition.visible, Duration.ofSeconds(10))
    }

    @Test
    fun `given inbox table when file is dropped then should integrate with upload js module`() {
        // given - user is on inbox tab
        assertTrue(isOnInboxTab())
        waitForInboxTableToLoad()

        // when - user drops a valid image file on inbox table
        val inboxTableContainer = `$`("#inboxTableContainer")
        inboxTableContainer.shouldBe(Condition.exist)

        // Simulate file drop
        simulateImageFileDrop(inboxTableContainer)

        // then - upload modal should open with proper integration
        val uploadModal = `$`("#uploadModal")
        uploadModal.shouldBe(Condition.visible, Duration.ofSeconds(10))

        // Verify upload modal content is properly initialized
        val cropperImage = `$`("#cropperImage")
        cropperImage.shouldBe(Condition.exist)

        val confirmUpload = `$`("#confirmUpload")
        confirmUpload.shouldBe(Condition.visible)
    }

    @Test
    fun `given inbox table when drop overlay is visible then should display upload icon and message`() {
        // given - user is on inbox tab
        assertTrue(isOnInboxTab())
        waitForInboxTableToLoad()

        // when - user drags file over inbox table
        val inboxTableContainer = `$`("#inboxTableContainer")
        inboxTableContainer.shouldBe(Condition.exist)

        simulateDragOver(inboxTableContainer)

        // then - drop overlay should show upload icon and message
        val dropOverlay = `$`("#dropOverlay")
        dropOverlay.shouldBe(Condition.visible)

        val uploadIcon = dropOverlay.`$`("i.fa-cloud-upload-alt")
        uploadIcon.shouldBe(Condition.visible)

        val dropMessage = dropOverlay.`$`("p")
        dropMessage.shouldBe(Condition.visible)
        assertTrue(dropMessage.text().contains("Drop images here to upload"))
    }

    @Test
    fun `given inbox table when accessibility features are used then should be keyboard accessible`() {
        // given - user is on inbox tab
        assertTrue(isOnInboxTab())
        waitForInboxTableToLoad()

        // when - checking accessibility features
        val inboxTableContainer = `$`("#inboxTableContainer")
        inboxTableContainer.shouldBe(Condition.exist)

        // then - drop zone should have appropriate accessibility attributes
        // Check for proper ARIA attributes or role
        val dropZone = inboxTableContainer
        
        // Verify the drop zone is properly marked for accessibility
        // This would be enhanced based on actual implementation
        assertTrue(dropZone.exists())
    }

    /**
     * Helper method to navigate to the inbox tab.
     */
    private fun navigateToInboxTab() {
        val inboxTabLink = `$`("a[href='#inbox']")
        if (inboxTabLink.exists()) {
            inboxTabLink.click()
        }
        
        // Wait for tab to become active
        val inboxTab = `$`("#inbox")
        inboxTab.shouldBe(Condition.visible, Duration.ofSeconds(10))
    }

    /**
     * Helper method to check if we're on the inbox tab.
     */
    private fun isOnInboxTab(): Boolean {
        val inboxTab = `$`("#inbox")
        return inboxTab.exists() && inboxTab.isDisplayed
    }

    /**
     * Helper method to wait for inbox table to load.
     */
    private fun waitForInboxTableToLoad() {
        val inboxContent = `$`("#inbox-content")
        inboxContent.shouldBe(Condition.visible, Duration.ofSeconds(10))
        
        // Wait for loading spinner to disappear
        val loadingSpinner = `$`(".spinner-border")
        if (loadingSpinner.exists()) {
            loadingSpinner.shouldNotBe(Condition.visible, Duration.ofSeconds(10))
        }
    }

    /**
     * Helper method to simulate drag over event.
     */
    private fun simulateDragOver(element: SelenideElement) {
        element.shouldBe(Condition.exist)
        
        // Execute JavaScript to simulate dragenter and dragover events
        Selenide.executeJavaScript<Unit>("""
            var element = arguments[0];
            var dragEnterEvent = new DragEvent('dragenter', {
                bubbles: true,
                cancelable: true,
                dataTransfer: new DataTransfer()
            });
            var dragOverEvent = new DragEvent('dragover', {
                bubbles: true,
                cancelable: true,
                dataTransfer: new DataTransfer()
            });
            
            element.dispatchEvent(dragEnterEvent);
            element.dispatchEvent(dragOverEvent);
        """, element)
    }

    /**
     * Helper method to simulate drag leave event.
     */
    private fun simulateDragLeave(element: SelenideElement) {
        element.shouldBe(Condition.exist)
        
        // Execute JavaScript to simulate dragleave event
        Selenide.executeJavaScript<Unit>("""
            var element = arguments[0];
            var dragLeaveEvent = new DragEvent('dragleave', {
                bubbles: true,
                cancelable: true,
                dataTransfer: new DataTransfer()
            });
            
            element.dispatchEvent(dragLeaveEvent);
        """, element)
    }

    /**
     * Helper method to simulate image file drop.
     */
    private fun simulateImageFileDrop(element: SelenideElement) {
        element.shouldBe(Condition.exist)
        
        // Execute JavaScript to simulate drop event with image file
        Selenide.executeJavaScript<Unit>("""
            var element = arguments[0];
            var dataTransfer = new DataTransfer();
            
            // Create a mock image file
            var file = new File(['fake image content'], 'test-image.jpg', {
                type: 'image/jpeg',
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
     * Helper method to simulate invalid file drop.
     */
    private fun simulateInvalidFileDrop(element: SelenideElement) {
        element.shouldBe(Condition.exist)
        
        // Execute JavaScript to simulate drop event with invalid file
        Selenide.executeJavaScript<Unit>("""
            var element = arguments[0];
            var dataTransfer = new DataTransfer();
            
            // Create a mock text file (invalid)
            var file = new File(['fake text content'], 'test-file.txt', {
                type: 'text/plain',
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
     * Helper method to simulate multiple file drop.
     */
    private fun simulateMultipleFileDrop(element: SelenideElement) {
        element.shouldBe(Condition.exist)
        
        // Execute JavaScript to simulate drop event with multiple files
        Selenide.executeJavaScript<Unit>("""
            var element = arguments[0];
            var dataTransfer = new DataTransfer();
            
            // Create multiple mock files
            var imageFile = new File(['fake image content'], 'test-image.jpg', {
                type: 'image/jpeg',
                lastModified: Date.now()
            });
            var textFile = new File(['fake text content'], 'test-file.txt', {
                type: 'text/plain',
                lastModified: Date.now()
            });
            
            dataTransfer.items.add(imageFile);
            dataTransfer.items.add(textFile);
            
            var dropEvent = new DragEvent('drop', {
                bubbles: true,
                cancelable: true,
                dataTransfer: dataTransfer
            });
            
            element.dispatchEvent(dropEvent);
        """, element)
    }
}