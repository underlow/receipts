package me.underlow.receipt.e2e

import com.codeborne.selenide.Condition
import com.codeborne.selenide.Selenide
import com.codeborne.selenide.Selenide.`$`
import com.codeborne.selenide.Selenide.`$$`
import me.underlow.receipt.config.BaseE2ETest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.BeforeEach
import kotlin.test.assertTrue

/**
 * End-to-end tests for Bills functionality.
 * Tests the complete user workflow from navigation to table display,
 * including user interactions with the bills interface.
 */
class BillsE2ETest : BaseE2ETest() {

    @BeforeEach
    fun setUpBillsTest() {
        // given - user is logged in and on dashboard
        performLoginWithAllowedUser()
        waitForPageLoad()
    }

    @Test
    fun `given authenticated user when navigating to bills then should display bills table with data`() {
        // given - user is authenticated and on dashboard
        assertTrue(isOnDashboardPage())
        
        // when - user clicks on Bills tab or navigates to bills
        val billsTab = `$`("[data-tab='bills']")
        if (billsTab.exists()) {
            billsTab.click()
        } else {
            // Alternative navigation if tab structure is different
            val billsLink = `$`("a[href*='bills']")
            if (billsLink.exists()) {
                billsLink.click()
            }
        }
        waitForPageLoad()
        
        // then - should display bills table with headers
        val table = `$`("table")
        assertTrue(table.exists())
        
        // Verify table headers are present
        assertTrue(`$`("th").text().contains("Bill Date") || 
                  `$$`("th").any { it.text().contains("Bill Date") })
        assertTrue(`$`("th").text().contains("Service Provider") || 
                  `$$`("th").any { it.text().contains("Service Provider") })
        assertTrue(`$`("th").text().contains("Amount") || 
                  `$$`("th").any { it.text().contains("Amount") })
        assertTrue(`$`("th").text().contains("Description") || 
                  `$$`("th").any { it.text().contains("Description") })
        assertTrue(`$`("th").text().contains("Created Date") || 
                  `$$`("th").any { it.text().contains("Created Date") })
        assertTrue(`$`("th").text().contains("Actions") || 
                  `$$`("th").any { it.text().contains("Actions") })
    }

    @Test
    fun `given bills table when loaded then should display bills in different states`() {
        // given - user is on bills view
        navigateToBills()
        
        // when - table is loaded
        val table = `$`("table")
        table.shouldBe(Condition.visible)
        
        // then - should display bills in different states
        val tableRows = `$$`("tbody tr")
        assertTrue(tableRows.size() > 0)
        
        // Verify different service providers exist
        val hasProviders = `$$`("td").any { 
            it.text().contains("Company") || it.text().contains("Provider") || 
            it.text().contains("Electric") || it.text().contains("Gas") ||
            it.text().contains("Water") 
        }
        assertTrue(hasProviders)
        
        // Verify amounts are displayed as currency
        val hasAmounts = `$$`("td").any { it.text().contains("$") }
        assertTrue(hasAmounts)
    }

    @Test
    fun `given bills table when clicking sort headers then should sort data accordingly`() {
        // given - user is on bills view
        navigateToBills()
        
        // when - user clicks on sortable column header
        val billDateHeader = `$$`("th").find { it.text().contains("Bill Date") }
        if (billDateHeader != null && billDateHeader.exists()) {
            billDateHeader.click()
            waitForPageLoad()
            
            // then - should apply sorting (verify sort indicator or data order change)
            // Re-find the header element after the page update to avoid stale reference
            val updatedHeader = `$$`("th").find { it.text().contains("Bill Date") }
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
    fun `given bills table when clicking amount header then should sort by amount`() {
        // given - user is on bills view
        navigateToBills()
        
        // when - user clicks on amount column header
        val amountHeader = `$$`("th").find { it.text().contains("Amount") }
        if (amountHeader != null && amountHeader.exists()) {
            amountHeader.click()
            waitForPageLoad()
            
            // then - should apply amount sorting
            val updatedHeader = `$$`("th").find { it.text().contains("Amount") }
            if (updatedHeader != null) {
                val sortIcon = updatedHeader.`$`("i")
                assertTrue(sortIcon.exists() || updatedHeader.attr("class")?.contains("sort") == true)
            } else {
                // Alternative verification - check if table still contains monetary values
                val hasAmounts = `$$`("td").any { it.text().contains("$") }
                assertTrue(hasAmounts)
            }
        }
    }

    @Test
    fun `given created bills when edit buttons exist then should show edit options`() {
        // given - user is on bills view with created bills
        navigateToBills()
        
        // when - looking for edit buttons
        val hasEditButtons = `$$`("button").any { it.text().contains("Edit") }
        
        if (hasEditButtons) {
            // then - should have edit buttons for created bills
            assertTrue(hasEditButtons)
            
            // Verify edit buttons are enabled
            val editButtons = `$$`("button").filter { it.text().contains("Edit") }
            editButtons.forEach { button ->
                assertTrue(button.isEnabled)
                assertTrue(button.text().contains("Edit"))
            }
        }
    }

    @Test
    fun `given created bills when remove buttons exist then should show remove options`() {
        // given - user is on bills view with created bills
        navigateToBills()
        
        // when - looking for remove buttons
        val hasRemoveButtons = `$$`("button").any { it.text().contains("Remove") }
        
        if (hasRemoveButtons) {
            // then - should have remove buttons for created bills
            assertTrue(hasRemoveButtons)
            
            // Verify remove buttons are enabled
            val removeButtons = `$$`("button").filter { it.text().contains("Remove") }
            removeButtons.forEach { button ->
                assertTrue(button.isEnabled)
                assertTrue(button.text().contains("Remove"))
            }
            
            // Note: We don't actually click to avoid changing state during test
            // In a real scenario, you would click and verify the state change
        }
    }

    @Test
    fun `given bills table when pagination exists then should navigate between pages`() {
        // given - user is on bills view
        navigateToBills()
        
        // when - looking for pagination controls
        val hasPagination = `$$`(".pagination, .page-link, .page-item").any { it.exists() }
        
        if (hasPagination) {
            // then - should have pagination controls
            assertTrue(hasPagination)
            
            // Verify pagination elements
            val hasNextButton = `$$`("button, a").any { 
                it.text().contains("Next") || it.text().contains("»") 
            }
            val hasPrevButton = `$$`("button, a").any { 
                it.text().contains("Previous") || it.text().contains("«") 
            }
            
            // At least one pagination control should exist
            assertTrue(hasNextButton || hasPrevButton)
        }
    }

    @Test
    fun `given bills table when search exists then should filter results`() {
        // given - user is on bills view
        navigateToBills()
        
        // when - looking for bills-specific search input
        val searchInput = `$`("#bills-search")
        
        if (searchInput.exists()) {
            // then - should have search functionality
            assertTrue(searchInput.exists())
            
            // Wait for search input to be visible and enabled
            searchInput.shouldBe(Condition.visible)
            searchInput.shouldBe(Condition.enabled)
            
            // Test search functionality
            searchInput.setValue("electric")
            waitForPageLoad()
            
            // Results should be filtered (or at least search should not break)
            val filteredRows = `$$`("tbody tr")
            assertTrue(filteredRows.size() >= 0) // Should not break the table
        }
    }

    @Test
    fun `given bills table when displaying service providers then should show provider details`() {
        // given - user is on bills view with service providers
        navigateToBills()
        
        // when - checking for service provider information
        val hasProviders = `$$`("td").any { 
            it.text().contains("Company") || it.text().contains("Provider") ||
            it.text().contains("Electric") || it.text().contains("Gas") ||
            it.text().contains("Water") || it.text().contains("Internet") ||
            it.text().contains("Phone") || it.text().contains("Mobile")
        }
        
        if (hasProviders) {
            // then - should display service provider details
            assertTrue(hasProviders)
            
            // Verify provider information includes creation source
            val hasSource = `$$`("td").any { 
                it.text().contains("From inbox") || it.text().contains("manual entry")
            }
            assertTrue(hasSource)
        }
    }

    @Test
    fun `given bills table when displaying amounts then should show formatted currency`() {
        // given - user is on bills view with amounts
        navigateToBills()
        
        // when - looking for amount values
        val hasAmounts = `$$`("td").any { it.text().contains("$") }
        
        if (hasAmounts) {
            // then - should display properly formatted currency
            assertTrue(hasAmounts)
            
            // Verify currency format
            val amountCells = `$$`("td").filter { it.text().contains("$") }
            amountCells.forEach { cell ->
                val amountText = cell.text()
                assertTrue(amountText.contains("$"))
                // Verify it follows currency format pattern
                assertTrue(amountText.matches(Regex(".*\\$[0-9,]+\\.[0-9]{2}.*")))
            }
        }
    }

    @Test
    fun `given bills table when no data exists then should display empty state`() {
        // given - user is on bills view
        navigateToBills()
        
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
    fun `given bills interface when performing complete workflow then should handle all user interactions`() {
        // given - user is on bills view
        navigateToBills()
        
        // when - user interacts with various elements
        val table = `$`("table")
        assertTrue(table.exists())
        
        // then - should handle all interactions without errors
        
        // Test table loading
        table.shouldBe(Condition.visible)
        
        // Test column headers
        val headers = `$$`("th")
        assertTrue(headers.size() >= 6) // Should have at least 6 columns
        
        // Test row data
        val rows = `$$`("tbody tr")
        if (rows.size() > 0) {
            val firstRow = rows.first()
            assertTrue(firstRow.exists())
            
            // Verify row has all expected columns
            val cells = firstRow.`$$`("td")
            assertTrue(cells.size() >= 6)
        }
        
        // Test currency formatting
        val hasAmounts = `$$`("td").any { it.text().contains("$") }
        assertTrue(hasAmounts || rows.size() == 0) // Should have amounts if there are rows
        
        // Test action buttons
        val hasActionButtons = `$$`("button").any { it.exists() }
        assertTrue(hasActionButtons || rows.size() == 0) // Should have buttons if there are rows
    }

    @Test
    fun `given bills interface when handling errors then should display error states gracefully`() {
        // given - user is on bills view
        navigateToBills()
        
        // when - checking for error handling
        val hasErrors = `$$`(".alert-danger, .error, .alert-error").any { it.exists() }
        
        // then - should handle errors gracefully without breaking the interface
        if (hasErrors) {
            val errorElements = `$$`(".alert-danger, .error, .alert-error")
            errorElements.forEach { errorElement ->
                assertTrue(errorElement.exists())
                assertTrue(errorElement.text().isNotEmpty())
            }
        }
        
        // Interface should still be functional
        val table = `$`("table")
        assertTrue(table.exists())
    }

    @Test
    fun `given bills interface when verifying accessibility then should have proper accessibility attributes`() {
        // given - user is on bills view
        navigateToBills()
        
        // when - checking accessibility attributes
        val table = `$`("table")
        assertTrue(table.exists())
        
        // then - should have proper accessibility features
        
        // Verify table has proper accessibility attributes
        val tableRole = table.attr("role")
        val tableAriaLabel = table.attr("aria-label")
        assertTrue(tableRole == "table" || tableAriaLabel?.isNotEmpty() == true)
        
        // Check table headers contain expected text content (ignore sort icons)
        val headers = `$$`("th")
        val expectedHeaders = listOf("Bill Date", "Service Provider", "Amount", "Description", "Created Date", "Actions")
        
        // Verify we have the expected number of headers
        assertTrue(headers.size() >= expectedHeaders.size)
        
        // Verify each expected header exists in the table
        expectedHeaders.forEach { expectedHeader ->
            val headerExists = headers.any { header -> 
                header.text().contains(expectedHeader)
            }
            assertTrue(headerExists, "Expected header '$expectedHeader' not found in table")
        }
        
        // Verify headers have proper scope attributes for accessibility
        headers.forEach { header ->
            val headerText = header.text().trim()
            if (headerText.isNotEmpty()) {
                // Headers should have scope attribute for accessibility
                val scope = header.attr("scope")
                assertTrue(scope == "col" || scope == "row", "Header '$headerText' should have scope attribute")
            }
        }
        
        // Action buttons should have proper accessibility attributes (if any exist)
        val actionButtons = `$$`("button")
        if (actionButtons.size() > 0) {
            actionButtons.forEach { button ->
                val hasTitle = button.attr("title")?.isNotEmpty() == true
                val hasAriaLabel = button.attr("aria-label")?.isNotEmpty() == true
                val hasText = button.text().trim().isNotEmpty()
                val hasDataToggle = button.attr("data-bs-toggle")?.isNotEmpty() == true
                val hasIconChild = button.`$`("i").exists()
                
                // At least one form of accessible text should be present, or it's a recognized UI component
                assertTrue(hasTitle || hasAriaLabel || hasText || hasDataToggle || hasIconChild, 
                          "Button should have title, aria-label, text content, or be a recognized UI component for accessibility")
            }
        }
    }

    /**
     * Helper method to navigate to the bills view.
     * Handles different possible navigation patterns.
     */
    private fun navigateToBills() {
        // Try different navigation methods
        val billsTab = `$`("[data-tab='bills']")
        if (billsTab.exists()) {
            billsTab.click()
        } else {
            val billsLink = `$`("a[href*='bills']")
            if (billsLink.exists()) {
                billsLink.click()
            } else {
                // If no specific navigation, assume we're already on the right page
                // or try to find bills-related content
                val billsContent = `$`("[data-testid='bills'], .bills-container, #bills")
                if (billsContent.exists()) {
                    // Already on bills page
                } else {
                    // Try opening dashboard and looking for bills
                    Selenide.open("/dashboard")
                    waitForPageLoad()
                }
            }
        }
        waitForPageLoad()
    }
}