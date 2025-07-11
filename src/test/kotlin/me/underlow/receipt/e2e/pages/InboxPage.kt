package me.underlow.receipt.e2e.pages

import com.codeborne.selenide.Condition
import com.codeborne.selenide.ElementsCollection
import com.codeborne.selenide.Selenide.`$`
import com.codeborne.selenide.Selenide.`$$`
import com.codeborne.selenide.SelenideElement
import com.codeborne.selenide.CollectionCondition
import com.codeborne.selenide.Selenide
import java.time.Duration

/**
 * Page Object for inbox page interactions.
 * Encapsulates inbox elements and actions using data-test-id selectors.
 */
class InboxPage {

    // Inbox page elements using data-test-id selectors
    private val inboxTab get() = `$`("[data-test-id='inbox-tab']")
    private val inboxTable get() = `$`("[data-test-id='inbox-table']")
    private val inboxTableContainer get() = `$`("[data-test-id='inbox-table-container']")
    private val inboxTableBody get() = `$`("[data-test-id='inbox-table-body']")
    private val inboxRows get() = `$$`("[data-test-id='inbox-row']")
    private val emptyInboxMessage get() = `$`("[data-test-id='empty-inbox-message']")
    private val loadingIndicator get() = `$`("[data-test-id='loading-indicator']")
    private val refreshButton get() = `$`("[data-test-id='refresh-button']")
    private val searchInput get() = `$`("[data-test-id='search-input']")
    private val searchButton get() = `$`("[data-test-id='search-button']")
    private val clearSearchButton get() = `$`("[data-test-id='clear-search-button']")
    private val sortByDateButton get() = `$`("[data-test-id='sort-by-date-button']")
    private val sortByNameButton get() = `$`("[data-test-id='sort-by-name-button']")
    private val errorMessage get() = `$`("[data-test-id='error-message']")

    // Drag and drop elements
    private val dropZone get() = `$`("[data-test-id='inbox-drop-zone']")
    private val dropOverlay get() = `$`("[data-test-id='drop-overlay']")
    private val dropMessage get() = `$`("[data-test-id='drop-message']")
    private val uploadIcon get() = `$`("[data-test-id='upload-icon']")

    /**
     * Navigates to the inbox tab
     */
    fun navigateToInbox(): InboxPage {
        inboxTab.shouldBe(Condition.visible).click()
        waitForInboxToLoad()
        return this
    }

    /**
     * Verifies the inbox page is displayed
     */
    fun shouldBeDisplayed(): InboxPage {
        inboxTable.shouldBe(Condition.visible)
        return this
    }

    /**
     * Verifies the inbox contains at least one uploaded item
     */
    fun shouldContainUploadedItem(): InboxPage {
        inboxRows.shouldHave(CollectionCondition.size(1))
        return this
    }

    /**
     * Verifies the inbox contains the expected number of items
     */
    fun shouldContainItems(expectedCount: Int): InboxPage {
        inboxRows.shouldHave(CollectionCondition.size(expectedCount))
        return this
    }

    /**
     * Verifies the inbox contains at least the minimum number of items
     */
    fun shouldContainAtLeastItems(minimumCount: Int): InboxPage {
        val actualCount = inboxRows.size()
        assert(actualCount >= minimumCount) {
            "Expected at least $minimumCount items in inbox, but found $actualCount"
        }
        return this
    }

    /**
     * Verifies the inbox is empty
     */
    fun shouldBeEmpty(): InboxPage {
        inboxRows.shouldHave(CollectionCondition.size(0))
        emptyInboxMessage.shouldBe(Condition.visible)
        return this
    }

    /**
     * Verifies the first item in inbox contains image thumbnail
     */
    fun shouldShowImageThumbnail(): InboxPage {
        val firstRow = getFirstRow()
        val thumbnail = firstRow.`$`("[data-test-id='image-thumbnail']")
        thumbnail.shouldBe(Condition.visible)
        return this
    }

    /**
     * Verifies the first item in inbox contains specific filename
     */
    fun shouldShowFileName(expectedFileName: String): InboxPage {
        val firstRow = getFirstRow()
        val fileName = firstRow.`$`("[data-test-id='file-name']")
        fileName.shouldBe(Condition.visible)
        fileName.shouldHave(Condition.text(expectedFileName))
        return this
    }

    /**
     * Verifies the first item in inbox contains upload timestamp
     */
    fun shouldShowUploadTimestamp(): InboxPage {
        val firstRow = getFirstRow()
        val timestamp = firstRow.`$`("[data-test-id='upload-timestamp']")
        timestamp.shouldBe(Condition.visible)
        return this
    }

    /**
     * Verifies the first item in inbox contains file size information
     */
    fun shouldShowFileSize(): InboxPage {
        val firstRow = getFirstRow()
        val fileSize = firstRow.`$`("[data-test-id='file-size']")
        fileSize.shouldBe(Condition.visible)
        return this
    }

    /**
     * Verifies the first item in inbox contains metadata
     */
    fun shouldShowMetadata(): InboxPage {
        val firstRow = getFirstRow()
        val metadata = firstRow.`$$`("[data-test-id*='metadata']")
        metadata.shouldHave(CollectionCondition.size(1))
        return this
    }

    /**
     * Clicks on the first item in inbox to view details
     */
    fun clickFirstItem(): InboxPage {
        val firstRow = getFirstRow()
        firstRow.click()
        return this
    }

    /**
     * Refreshes the inbox by clicking refresh button
     */
    fun refreshInbox(): InboxPage {
        refreshButton.shouldBe(Condition.visible).click()
        waitForInboxToLoad()
        return this
    }

    /**
     * Searches for items in inbox using search input
     */
    fun searchForItems(searchTerm: String): InboxPage {
        searchInput.shouldBe(Condition.visible).setValue(searchTerm)
        searchButton.click()
        waitForSearchResults()
        return this
    }

    /**
     * Clears search results
     */
    fun clearSearch(): InboxPage {
        clearSearchButton.shouldBe(Condition.visible).click()
        waitForInboxToLoad()
        return this
    }

    /**
     * Sorts inbox items by date
     */
    fun sortByDate(): InboxPage {
        sortByDateButton.shouldBe(Condition.visible).click()
        waitForSortToApply()
        return this
    }

    /**
     * Sorts inbox items by name
     */
    fun sortByName(): InboxPage {
        sortByNameButton.shouldBe(Condition.visible).click()
        waitForSortToApply()
        return this
    }

    /**
     * Verifies no error message is displayed
     */
    fun shouldNotShowErrorMessage(): InboxPage {
        errorMessage.shouldNotBe(Condition.visible)
        return this
    }

    /**
     * Verifies error message is displayed
     */
    fun shouldShowErrorMessage(): InboxPage {
        errorMessage.shouldBe(Condition.visible)
        return this
    }

    /**
     * Verifies error message contains specific text
     */
    fun shouldShowErrorMessage(expectedText: String): InboxPage {
        errorMessage.shouldBe(Condition.visible)
        errorMessage.shouldHave(Condition.text(expectedText))
        return this
    }

    /**
     * Gets the first row from inbox table
     */
    fun getFirstRow(): SelenideElement {
        val rows = inboxRows
        assert(rows.size() > 0) { "No items found in inbox" }
        return rows.first()
    }

    /**
     * Gets all rows from inbox table
     */
    fun getAllRows(): ElementsCollection {
        return inboxRows
    }

    /**
     * Gets the current number of items in inbox
     */
    fun getItemCount(): Int {
        return inboxRows.size()
    }

    /**
     * Waits for inbox to load completely
     */
    private fun waitForInboxToLoad() {
        // Wait for loading indicator to disappear
        if (loadingIndicator.exists()) {
            loadingIndicator.shouldNotBe(Condition.visible, Duration.ofSeconds(10))
        }

        // Wait for table to be visible
        inboxTable.shouldBe(Condition.visible, Duration.ofSeconds(10))

        // Wait for table body to be present
        inboxTableBody.shouldBe(Condition.visible, Duration.ofSeconds(5))
    }

    /**
     * Waits for search results to load
     */
    private fun waitForSearchResults() {
        // Wait for search to complete
        Thread.sleep(1000)

        // Wait for results to update
        inboxTable.shouldBe(Condition.visible)
    }

    /**
     * Waits for sort to apply
     */
    private fun waitForSortToApply() {
        // Wait for sort to complete
        Thread.sleep(1000)

        // Wait for table to update
        inboxTable.shouldBe(Condition.visible)
    }

    /**
     * Verifies drop zone is properly configured for drag and drop
     */
    fun shouldHaveDropZone(): InboxPage {
        dropZone.shouldBe(Condition.visible)
        dropZone.shouldHave(Condition.cssClass("drop-zone"))
        return this
    }

    /**
     * Verifies drop overlay is initially hidden
     */
    fun shouldHaveHiddenDropOverlay(): InboxPage {
        dropOverlay.shouldBe(Condition.exist)
        dropOverlay.shouldNotBe(Condition.visible)
        return this
    }

    /**
     * Simulates dragging a file over the inbox drop zone
     */
    fun dragFileOverDropZone(fileName: String, mimeType: String = "image/jpeg"): InboxPage {
        Selenide.executeJavaScript<Unit>("""
            var element = arguments[0];
            var fileName = arguments[1];
            var mimeType = arguments[2];
            
            var dataTransfer = new DataTransfer();
            var file = new File(['test file content'], fileName, {
                type: mimeType,
                lastModified: Date.now()
            });
            dataTransfer.items.add(file);
            
            var dragEnterEvent = new DragEvent('dragenter', {
                bubbles: true,
                cancelable: true,
                dataTransfer: dataTransfer
            });
            var dragOverEvent = new DragEvent('dragover', {
                bubbles: true,
                cancelable: true,
                dataTransfer: dataTransfer
            });
            
            element.dispatchEvent(dragEnterEvent);
            element.dispatchEvent(dragOverEvent);
        """, dropZone, fileName, mimeType)
        return this
    }

    /**
     * Simulates dragging a file away from the inbox drop zone
     */
    fun dragFileAwayFromDropZone(): InboxPage {
        Selenide.executeJavaScript<Unit>("""
            var element = arguments[0];
            var dragLeaveEvent = new DragEvent('dragleave', {
                bubbles: true,
                cancelable: true,
                dataTransfer: new DataTransfer()
            });
            element.dispatchEvent(dragLeaveEvent);
        """, dropZone)
        return this
    }

    /**
     * Simulates dropping a file on the inbox drop zone
     */
    fun dropFileOnDropZone(fileName: String, mimeType: String = "image/jpeg"): InboxPage {
        Selenide.executeJavaScript<Unit>("""
            var element = arguments[0];
            var fileName = arguments[1];
            var mimeType = arguments[2];
            
            var dataTransfer = new DataTransfer();
            var file = new File(['test file content'], fileName, {
                type: mimeType,
                lastModified: Date.now()
            });
            dataTransfer.items.add(file);
            
            var dropEvent = new DragEvent('drop', {
                bubbles: true,
                cancelable: true,
                dataTransfer: dataTransfer
            });
            
            element.dispatchEvent(dropEvent);
        """, dropZone, fileName, mimeType)
        return this
    }

    /**
     * Simulates dropping multiple files on the inbox drop zone
     */
    fun dropMultipleFilesOnDropZone(fileNames: List<String>, mimeTypes: List<String> = listOf("image/jpeg")): InboxPage {
        val defaultMimeType = "image/jpeg"
        Selenide.executeJavaScript<Unit>("""
            var element = arguments[0];
            var fileNames = arguments[1];
            var mimeTypes = arguments[2];
            var defaultMimeType = arguments[3];
            
            var dataTransfer = new DataTransfer();
            
            for (var i = 0; i < fileNames.length; i++) {
                var mimeType = i < mimeTypes.length ? mimeTypes[i] : defaultMimeType;
                var file = new File(['test file content'], fileNames[i], {
                    type: mimeType,
                    lastModified: Date.now()
                });
                dataTransfer.items.add(file);
            }
            
            var dropEvent = new DragEvent('drop', {
                bubbles: true,
                cancelable: true,
                dataTransfer: dataTransfer
            });
            
            element.dispatchEvent(dropEvent);
        """, dropZone, fileNames.toTypedArray(), mimeTypes.toTypedArray(), defaultMimeType)
        return this
    }

    /**
     * Verifies drop zone shows drag-over styling
     */
    fun shouldShowDragOverStyling(): InboxPage {
        dropZone.shouldHave(Condition.cssClass("drag-over"))
        return this
    }

    /**
     * Verifies drop zone does not show drag-over styling
     */
    fun shouldNotShowDragOverStyling(): InboxPage {
        dropZone.shouldNotHave(Condition.cssClass("drag-over"))
        return this
    }

    /**
     * Verifies drop overlay becomes visible
     */
    fun shouldShowDropOverlay(): InboxPage {
        dropOverlay.shouldBe(Condition.visible)
        return this
    }

    /**
     * Verifies drop overlay is hidden
     */
    fun shouldHideDropOverlay(): InboxPage {
        dropOverlay.shouldNotBe(Condition.visible)
        return this
    }

    /**
     * Verifies drop overlay shows upload icon and message
     */
    fun shouldShowDropOverlayContent(): InboxPage {
        dropOverlay.shouldBe(Condition.visible)
        uploadIcon.shouldBe(Condition.visible)
        dropMessage.shouldBe(Condition.visible)
        dropMessage.shouldHave(Condition.text("Drop images here to upload"))
        return this
    }
}
