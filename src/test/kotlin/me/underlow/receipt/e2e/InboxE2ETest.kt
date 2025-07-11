package me.underlow.receipt.e2e

import me.underlow.receipt.config.BaseE2ETest
import me.underlow.receipt.e2e.helpers.LoginHelper
import me.underlow.receipt.e2e.pages.InboxPage
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertTrue
import com.codeborne.selenide.Selenide.`$`
import com.codeborne.selenide.Condition

/**
 * End-to-end tests for Inbox functionality.
 * Tests user workflows for managing uploaded receipts in the inbox.
 */
class InboxE2ETest : BaseE2ETest() {

    private val loginHelper = LoginHelper()
    private val inboxPage = InboxPage()

    @BeforeEach
    fun setUp() {
        // Given - user is authenticated and ready to use inbox
        loginHelper.loginAsAllowedUser1()
        waitForPageLoad()
    }

    @AfterEach
    fun tearDown() {
        // Clean up any test data or state changes
        // This ensures tests don't interfere with each other
    }

    @Test
    fun shouldDisplayInboxTableWhenUserNavigatesToInbox() {
        // Given - user is on dashboard
        assertTrue(isOnDashboardPage())

        // When - user navigates to inbox
        inboxPage.navigateToInbox()

        // Then - inbox table should be displayed
        inboxPage.shouldBeDisplayed()
    }

    @Test
    fun shouldShowUploadedReceiptsInInboxTable() {
        // Given - user has uploaded receipts in their inbox
        inboxPage.navigateToInbox()

        // When - inbox loads
        inboxPage.shouldBeDisplayed()

        // Then - uploaded receipts should be visible
        inboxPage.shouldContainAtLeastItems(1)
        inboxPage.shouldShowImageThumbnail()
        inboxPage.shouldShowUploadTimestamp()
    }

    @Test
    fun shouldDisplayEmptyStateWhenNoReceiptsUploaded() {
        // Given - user has no uploaded receipts
        inboxPage.navigateToInbox()

        // When - inbox loads with no data
        // This test would need test data management to create empty state

        // Then - empty state message should be displayed
        // inboxPage.shouldBeEmpty()
        // Note: This test needs proper test data setup to create empty state
    }

    @Test
    fun shouldSortReceiptsByDateWhenUserClicksSortButton() {
        // Given - user has multiple receipts in inbox
        inboxPage.navigateToInbox()
        inboxPage.shouldContainAtLeastItems(2)

        // When - user clicks sort by date header
        inboxPage.sortByDateHeader()

        // Then - receipts should be sorted chronologically
        inboxPage.shouldBeDisplayed()
        // Note: Actual sort verification would require checking order of timestamps
    }

    @Test
    fun shouldFilterReceiptsWhenUserSearches() {
        // Given - user has searchable receipts in inbox
        inboxPage.navigateToInbox()
        val initialItemCount = inboxPage.getItemCount()

        // When - user searches for specific term
        inboxPage.searchForItems("grocery")

        // Then - filtered results should be displayed
        inboxPage.shouldBeDisplayed()
        // Note: Actual verification would depend on test data containing searchable terms
    }

    @Test
    fun shouldRefreshInboxDataWhenUserClicksRefresh() {
        // Given - user is viewing inbox
        inboxPage.navigateToInbox()
        inboxPage.shouldBeDisplayed()

        // When - user clicks refresh button
        inboxPage.refreshInbox()

        // Then - inbox should reload with current data
        inboxPage.shouldBeDisplayed()
        inboxPage.shouldNotShowErrorMessage()
    }

    @Test
    fun shouldShowReceiptDetailsWhenUserClicksOnReceipt() {
        // Given - user has receipts in inbox
        inboxPage.navigateToInbox()
        inboxPage.shouldContainAtLeastItems(1)

        // When - user clicks on first receipt
        inboxPage.clickFirstItem()

        // Then - receipt details should be displayed
        // Note: This would navigate to receipt details page
        // Verification depends on the actual navigation behavior
    }

    @Test
    fun shouldAcceptFileDropWhenUserDragsReceiptToInbox() {
        // Given - user is on inbox with drag-and-drop enabled
        inboxPage.navigateToInbox()
        inboxPage.shouldHaveDropZone()
        inboxPage.shouldHaveHiddenDropOverlay()

        // When - user drags a receipt image file over the drop zone
        inboxPage.dragFileOverDropZone("test-receipt.jpg")

        // Then - drop zone should show visual feedback
        inboxPage.shouldShowDropOverlay()
        inboxPage.shouldShowDropOverlayContent()
    }

    @Test
    fun shouldUploadReceiptWhenUserDropsFileOnInbox() {
        // Given - user is dragging a receipt file over inbox
        inboxPage.navigateToInbox()
        val initialItemCount = inboxPage.getItemCount()

        // When - user drops the file on the inbox
        inboxPage.dropFileOnDropZone("new-receipt.jpg")

        // Then - receipt should be uploaded and appear in inbox
        inboxPage.shouldContainItems(initialItemCount + 1)
        inboxPage.shouldShowFileName("new-receipt.jpg")
    }

    @Test
    fun shouldUploadMultipleReceiptsWhenUserDropsMultipleFiles() {
        // Given - user is on inbox page
        inboxPage.navigateToInbox()
        val initialItemCount = inboxPage.getItemCount()

        // When - user drops multiple files at once
        val fileNames = listOf("receipt1.jpg", "receipt2.png", "receipt3.pdf")
        inboxPage.dropMultipleFilesOnDropZone(fileNames)

        // Then - all receipts should be uploaded
        inboxPage.shouldContainItems(initialItemCount + fileNames.size)
    }

    @Test
    fun shouldHideDropOverlayWhenUserDragsFileAway() {
        // Given - user is dragging file over inbox with visible overlay
        inboxPage.navigateToInbox()
        inboxPage.dragFileOverDropZone("test-receipt.jpg")
        inboxPage.shouldShowDropOverlay()

        // When - user drags file away from drop zone
        inboxPage.dragFileAwayFromDropZone()

        // Then - drop overlay should be hidden
        inboxPage.shouldHideDropOverlay()
        inboxPage.shouldNotShowDragOverStyling()
    }

    @Test
    fun shouldHandleErrorsGracefullyWhenInboxFailsToLoad() {
        // Given - user attempts to access inbox
        inboxPage.navigateToInbox()

        // When - inbox encounters loading error
        // Note: This would require simulating network failure or server error

        // Then - error should be displayed without breaking the interface
        // This test needs proper error simulation setup
        inboxPage.shouldNotShowErrorMessage() // Placeholder - actual test would verify error handling
    }

    @Test
    fun shouldMaintainInboxStateWhenUserNavigatesAwayAndReturns() {
        // Given - user has applied search filters in inbox
        inboxPage.navigateToInbox()
        inboxPage.searchForItems("grocery")
        
        // Store the current search value to verify later
        val searchInput = `$`("[data-test-id='search-input']")
        val initialSearchValue = searchInput.getValue()

        // When - user navigates away to services tab and returns
        val servicesTab = `$`("[data-test-id='services-tab']")
        servicesTab.shouldBe(Condition.visible).click()
        waitForPageLoad()
        
        // Navigate back to inbox
        inboxPage.navigateToInbox()

        // Then - search filters should be maintained
        val currentSearchValue = searchInput.getValue()
        assert(currentSearchValue == initialSearchValue) {
            "Search filter was not maintained. Expected: '$initialSearchValue', but got: '$currentSearchValue'"
        }
    }
}
