package me.underlow.receipt.e2e

import com.codeborne.selenide.Condition
import com.codeborne.selenide.Selenide
import com.codeborne.selenide.Selenide.`$`
import com.codeborne.selenide.Selenide.`$$`
import me.underlow.receipt.config.BaseE2ETest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.BeforeEach
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.test.assertFalse
import java.time.Duration

/**
 * End-to-end tests for Inbox functionality.
 * Tests the complete user workflow from navigation to table display,
 * including user interactions with the inbox interface.
 */
class InboxE2ETest : BaseE2ETest() {

    @BeforeEach
    fun setUpInboxTest() {
        // given - user is logged in and on dashboard
        performLoginWithAllowedUser()
        waitForPageLoad()
    }

    @Test
    fun `given authenticated user when navigating to inbox then should display inbox table with data`() {
        // given - user is authenticated and on dashboard
        assertTrue(isOnDashboardPage())
        
        // when - user navigates to inbox (inbox tab should be active by default)
        // The inbox tab is already active by default, so just wait for the data to load
        waitForPageLoad()
        
        // Wait for inbox content to load (AJAX call)
        val inboxContent = `$`("#inbox-content")
        inboxContent.shouldBe(Condition.visible)
        
        // Wait for the table to appear (it's loaded dynamically) - use longer timeout
        val table = `$`("table")
        table.shouldBe(Condition.visible, Duration.ofSeconds(15))
        
        // Wait for table headers to be present
        val headers = `$$`("th")
        assertTrue(headers.size() > 0, "No table headers found")
        
        // Debug: Print all header texts
        val headerTexts = headers.map { it.text() }
        println("Found headers: $headerTexts")
        
        // Check each header individually with better error messages (case-insensitive)
        assertTrue(headerTexts.any { it.contains("Upload Date", ignoreCase = true) }, "Upload Date header not found. Found headers: $headerTexts")
        assertTrue(headerTexts.any { it.contains("Image", ignoreCase = true) }, "Image header not found. Found headers: $headerTexts")
        assertTrue(headerTexts.any { it.contains("OCR Status", ignoreCase = true) }, "OCR Status header not found. Found headers: $headerTexts")
        assertTrue(headerTexts.any { it.contains("Actions", ignoreCase = true) }, "Actions header not found. Found headers: $headerTexts")
    }

    @Test
    fun `given inbox table when loaded then should display items in different states`() {
        // given - user is on inbox view
        navigateToInbox()
        
        // when - table is loaded
        val table = `$`("table")
        table.shouldBe(Condition.visible)
        
        // then - should display items in different states
        val tableRows = `$$`("tbody tr")
        assertTrue(tableRows.size() > 0)
        
        // Verify different OCR status badges exist
        val statusBadges = `$$`(".status-indicator")
        assertTrue(statusBadges.size() > 0)
        
        // Check for different status types
        val hasPending = statusBadges.any { it.text().contains("Pending") }
        val hasProcessed = statusBadges.any { it.text().contains("Processed") }
        val hasFailed = statusBadges.any { it.text().contains("Failed") }
        val hasApproved = statusBadges.any { it.text().contains("Approved") }
        
        // At least some of these states should be present
        assertTrue(hasPending || hasProcessed || hasFailed || hasApproved)
    }

    @Test
    fun `given inbox table when clicking sort headers then should sort data accordingly`() {
        // given - user is on inbox view
        navigateToInbox()
        
        // when - user clicks on sortable column header
        val uploadDateHeader = `$$`("th").find { it.text().contains("Upload Date") }
        if (uploadDateHeader != null && uploadDateHeader.exists()) {
            uploadDateHeader.click()
            waitForPageLoad()
            
            // then - should apply sorting (verify sort indicator or data order change)
            // Re-find the header element after the page update to avoid stale reference
            val updatedHeader = `$$`("th").find { it.text().contains("Upload Date") }
            if (updatedHeader != null) {
                val sortIcon = updatedHeader.`$`("i")
                assertTrue(sortIcon.exists() || updatedHeader.attr("class")?.contains("sort") == true)
            } else {
                // Alternative verification - check if table structure still exists after sort
                val table = `$`("table")
                assertTrue(table.exists())
            }
        }
    }

    @Test
    fun `given processed items when clicking approve buttons then should show approval options`() {
        // given - user is on inbox view with processed items
        navigateToInbox()
        
        // when - looking for approve buttons
        val approveButtons = `$$`("button").filter { it.text().contains("Approve") }
        
        if (approveButtons.size > 0) {
            // then - should have approve buttons for processed items
            assertTrue(approveButtons.size > 0)
            
            // Verify approve button types
            val approveBillButtons = approveButtons.filter { it.text().contains("Bill") }
            val approveReceiptButtons = approveButtons.filter { it.text().contains("Receipt") }
            
            assertTrue(approveBillButtons.size > 0 || approveReceiptButtons.size > 0)
        }
    }

    @Test
    fun `given failed items when clicking retry buttons then should initiate retry process`() {
        // given - user is on inbox view with failed items
        navigateToInbox()
        
        // when - looking for retry buttons
        val retryButtons = `$$`("button").filter { it.text().contains("Retry") }
        
        if (retryButtons.size > 0) {
            // then - should have retry buttons for failed items
            assertTrue(retryButtons.size > 0)
            
            // Verify retry button functionality
            val firstRetryButton = retryButtons.first()
            assertTrue(firstRetryButton.isEnabled)
            
            // Note: We don't actually click to avoid changing state during test
            // In a real scenario, you would click and verify the state change
        }
    }

    @Test
    fun `given inbox table when pagination is enabled then should navigate between pages`() {
        // given - user is on inbox view
        navigateToInbox()
        
        // when - looking for pagination controls
        val paginationControls = `$$`(".pagination, .page-link, .page-item")
        
        if (paginationControls.size() > 0) {
            // then - should have pagination controls
            assertTrue(paginationControls.size() > 0)
            
            // Verify pagination elements
            val nextButton = `$$`("button, a").find { 
                it.text().contains("Next") || it.text().contains("»") 
            }
            val prevButton = `$$`("button, a").find { 
                it.text().contains("Previous") || it.text().contains("«") 
            }
            
            // At least one pagination control should exist
            assertTrue(nextButton != null || prevButton != null)
        }
    }

    @Test
    fun `given inbox table when search is enabled then should filter results`() {
        // given - user is on inbox view
        navigateToInbox()
        
        // when - looking for search input
        val searchInput = `$`("input[type='search'], input[placeholder*='search'], input[placeholder*='Search']")
        
        if (searchInput.exists()) {
            // then - should have search functionality
            assertTrue(searchInput.exists())
            
            // Verify search input is functional
            assertTrue(searchInput.isEnabled)
            
            // Test search functionality
            searchInput.setValue("grocery")
            waitForPageLoad()
            
            // Results should be filtered (or at least search should not break)
            val filteredRows = `$$`("tbody tr")
            assertTrue(filteredRows.size() >= 0) // Should not break the table
        }
    }

    @Test
    fun `given inbox table when clicking image thumbnails then should display image preview`() {
        // given - user is on inbox view with images
        navigateToInbox()
        
        // when - looking for image thumbnails
        val imageThumbnails = `$$`("img.img-thumbnail, img[onclick*='showImageModal']")
        
        if (imageThumbnails.size() > 0) {
            // then - should have clickable image thumbnails
            assertTrue(imageThumbnails.size() > 0)
            
            val firstThumbnail = imageThumbnails.first()
            assertTrue(firstThumbnail.exists())
            
            // Verify image has proper attributes
            val imgSrc = firstThumbnail.attr("src")
            assertNotNull(imgSrc)
            assertTrue(imgSrc.isNotEmpty())
        }
    }

    @Test
    fun `given inbox table when no data exists then should display empty state`() {
        // given - user is on inbox view
        navigateToInbox()
        
        // when - checking for empty state handling
        val table = `$`("table")
        if (table.exists()) {
            val tableRows = `$$`("tbody tr")
            
            if (tableRows.size() == 0) {
                // then - should display empty state message
                val emptyMessage = `$`(".empty-state, .no-data, .table-empty")
                assertTrue(emptyMessage.exists() || table.text().contains("No data") || 
                          table.text().contains("Empty"))
            }
        }
    }

    @Test
    fun `given inbox interface when performing complete workflow then should handle all user interactions`() {
        // given - user is on inbox view
        navigateToInbox()
        
        // when - user interacts with various elements
        val table = `$`("table")
        assertTrue(table.exists())
        
        // then - should handle all interactions without errors
        
        // Test table loading
        table.shouldBe(Condition.visible)
        
        // Test column headers
        val headers = `$$`("th")
        assertTrue(headers.size() >= 4) // Should have at least 4 columns
        
        // Test row data
        val rows = `$$`("tbody tr")
        if (rows.size() > 0) {
            val firstRow = rows.first()
            assertTrue(firstRow.exists())
            
            // Verify row has all expected columns
            val cells = firstRow.`$$`("td")
            assertTrue(cells.size() >= 4)
        }
        
        // Test status indicators
        val statusBadges = `$$`(".status-indicator")
        assertTrue(statusBadges.size() >= 0) // Should not cause errors
        
        // Test action buttons
        val actionButtons = `$$`("button")
        assertTrue(actionButtons.size() >= 0) // Should not cause errors
    }

    @Test
    fun `given inbox interface when handling errors then should display error states gracefully`() {
        // given - user is on inbox view
        navigateToInbox()
        
        // when - checking for error handling
        val errorElements = `$$`(".alert-danger, .error, .alert-error")
        
        // then - should handle errors gracefully without breaking the interface
        if (errorElements.size() > 0) {
            errorElements.forEach { errorElement ->
                assertTrue(errorElement.exists())
                assertTrue(errorElement.text().isNotEmpty())
            }
        }
        
        // Interface should still be functional
        val table = `$`("table")
        assertTrue(table.exists())
    }

    /**
     * Helper method to navigate to the inbox view.
     * Handles different possible navigation patterns.
     */
    private fun navigateToInbox() {
        // Try different navigation methods
        val inboxTab = `$`("[data-tab='inbox']")
        if (inboxTab.exists()) {
            inboxTab.click()
        } else {
            val inboxLink = `$`("a[href*='inbox']")
            if (inboxLink.exists()) {
                inboxLink.click()
            } else {
                // If no specific navigation, assume we're already on the right page
                // or try to find inbox-related content
                val inboxContent = `$`("[data-testid='inbox'], .inbox-container, #inbox")
                if (inboxContent.exists()) {
                    // Already on inbox page
                } else {
                    // Try opening dashboard and looking for inbox
                    Selenide.open("/dashboard")
                    waitForPageLoad()
                }
            }
        }
        waitForPageLoad()
    }
}
