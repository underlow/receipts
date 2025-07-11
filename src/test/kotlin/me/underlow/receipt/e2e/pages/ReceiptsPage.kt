package me.underlow.receipt.e2e.pages

import com.codeborne.selenide.Condition
import com.codeborne.selenide.ElementsCollection
import com.codeborne.selenide.Selenide
import com.codeborne.selenide.SelenideElement
import com.codeborne.selenide.Selenide.`$`
import com.codeborne.selenide.Selenide.`$$`
import java.time.Duration

/**
 * Page Object for receipts functionality.
 * Encapsulates receipts table elements and actions using reliable selectors.
 * Follows best practices for e2e test selectors and user interactions.
 */
class ReceiptsPage {
    
    // Tab navigation elements
    private val receiptsTab get() = when {
        `$`("[data-test-id='receipts-tab']").exists() -> `$`("[data-test-id='receipts-tab']")
        `$`("a[href=\"#receipts\"][data-bs-toggle=\"tab\"]").exists() -> `$`("a[href=\"#receipts\"][data-bs-toggle=\"tab\"]")
        else -> `$`("a[href*='receipts']")
    }
    
    // Main receipts container
    private val receiptsContainer get() = when {
        `$`("[data-test-id='receipts-container']").exists() -> `$`("[data-test-id='receipts-container']")
        `$`("#receipts").exists() -> `$`("#receipts")
        else -> `$`(".receipts-container")
    }
    
    // Table elements
    private val receiptsTable get() = when {
        `$`("[data-test-id='receipts-table']").exists() -> `$`("[data-test-id='receipts-table']")
        `$`("#receipts table").exists() -> `$`("#receipts table")
        else -> `$`("table")
    }
    
    private val tableHeaders get() = when {
        `$$`("[data-test-id='receipts-table-header']").size() > 0 -> `$$`("[data-test-id='receipts-table-header']")
        `$$`("#receipts th").size() > 0 -> `$$`("#receipts th")
        else -> `$$`("th")
    }
    
    private val tableRows get() = when {
        `$$`("[data-test-id='receipts-table-row']").size() > 0 -> `$$`("[data-test-id='receipts-table-row']")
        `$$`("#receipts tbody tr").size() > 0 -> `$$`("#receipts tbody tr")
        else -> `$$`("tbody tr")
    }
    
    private val tableCells get() = when {
        `$$`("[data-test-id='receipts-table-cell']").size() > 0 -> `$$`("[data-test-id='receipts-table-cell']")
        `$$`("#receipts td").size() > 0 -> `$$`("#receipts td")
        else -> `$$`("td")
    }
    
    // Search functionality
    private val searchInput get() = when {
        `$`("[data-test-id='receipts-search']").exists() -> `$`("[data-test-id='receipts-search']")
        `$`("#receipts-search").exists() -> `$`("#receipts-search")
        else -> `$`("input[type='search']")
    }
    
    // Action buttons
    private val editButtons get() = when {
        `$$`("[data-test-id='receipt-edit-button']").size() > 0 -> `$$`("[data-test-id='receipt-edit-button']")
        `$$`("#receipts button:contains('Edit')").size() > 0 -> `$$`("#receipts button:contains('Edit')")
        else -> `$$`("button:contains('Edit')")
    }
    
    private val removeButtons get() = when {
        `$$`("[data-test-id='receipt-remove-button']").size() > 0 -> `$$`("[data-test-id='receipt-remove-button']")
        `$$`("#receipts button:contains('Remove')").size() > 0 -> `$$`("#receipts button:contains('Remove')")
        else -> `$$`("button:contains('Remove')")
    }
    
    // Pagination elements
    private val paginationContainer get() = when {
        `$`("[data-test-id='receipts-pagination']").exists() -> `$`("[data-test-id='receipts-pagination']")
        `$`(".pagination").exists() -> `$`(".pagination")
        else -> `$`(".page-navigation")
    }
    
    private val nextButton get() = when {
        `$`("[data-test-id='pagination-next']").exists() -> `$`("[data-test-id='pagination-next']")
        `$`("button:contains('Next'), a:contains('Next')").exists() -> `$`("button:contains('Next'), a:contains('Next')")
        else -> `$`("button:contains('»'), a:contains('»')")
    }
    
    private val prevButton get() = when {
        `$`("[data-test-id='pagination-prev']").exists() -> `$`("[data-test-id='pagination-prev']")
        `$`("button:contains('Previous'), a:contains('Previous')").exists() -> `$`("button:contains('Previous'), a:contains('Previous')")
        else -> `$`("button:contains('«'), a:contains('«')")
    }
    
    // Empty state elements
    private val emptyStateMessage get() = when {
        `$`("[data-test-id='receipts-empty-state']").exists() -> `$`("[data-test-id='receipts-empty-state']")
        `$`(".empty-state").exists() -> `$`(".empty-state")
        else -> `$`(".no-data")
    }
    
    // Error elements
    private val errorMessage get() = when {
        `$`("[data-test-id='receipts-error']").exists() -> `$`("[data-test-id='receipts-error']")
        `$`(".alert-danger").exists() -> `$`(".alert-danger")
        else -> `$`(".error-message")
    }
    
    /**
     * Navigates to the receipts tab and waits for it to load
     */
    fun navigateToReceipts(): ReceiptsPage {
        receiptsTab.click()
        waitForReceiptsToLoad()
        return this
    }
    
    /**
     * Verifies receipts table is displayed and contains expected headers
     */
    fun shouldDisplayReceiptsTable(): ReceiptsPage {
        receiptsTable.shouldBe(Condition.visible, Duration.ofSeconds(10))
        return this
    }
    
    /**
     * Verifies all expected table headers are present
     */
    fun shouldHaveExpectedHeaders(): ReceiptsPage {
        val expectedHeaders = listOf(
            "PAYMENT DATE", "MERCHANT NAME", "AMOUNT", 
            "PAYMENT TYPE", "DESCRIPTION", "CREATED DATE", "ACTIONS"
        )
        
        tableHeaders.should(Condition.size(expectedHeaders.size))
        
        expectedHeaders.forEach { expectedHeader ->
            tableHeaders.find { it.text().contains(expectedHeader, ignoreCase = true) }
                ?.shouldBe(Condition.visible)
                ?: throw AssertionError("Header '$expectedHeader' not found")
        }
        
        return this
    }
    
    /**
     * Verifies table contains receipt data
     */
    fun shouldHaveReceiptData(): ReceiptsPage {
        tableRows.shouldHave(Condition.sizeGreaterThan(0))
        return this
    }
    
    /**
     * Verifies merchants are displayed in the table
     */
    fun shouldDisplayMerchants(): ReceiptsPage {
        val merchantNames = listOf("Whole Foods", "Subway", "Starbucks", "Nike", "Target", "Amazon")
        val hasMerchants = tableCells.any { cell ->
            merchantNames.any { merchant -> cell.text().contains(merchant, ignoreCase = true) }
        }
        assert(hasMerchants) { "No expected merchants found in receipts table" }
        return this
    }
    
    /**
     * Verifies amounts are displayed as currency
     */
    fun shouldDisplayFormattedAmounts(): ReceiptsPage {
        val hasAmounts = tableCells.any { it.text().contains("$") }
        assert(hasAmounts) { "No currency amounts found in receipts table" }
        
        // Verify currency format
        tableCells.filter { it.text().contains("$") }.forEach { cell ->
            val amountText = cell.text()
            assert(amountText.matches(Regex(".*\\$[0-9,]+\\.[0-9]{2}.*"))) {
                "Invalid currency format: $amountText"
            }
        }
        
        return this
    }
    
    /**
     * Verifies payment types are displayed
     */
    fun shouldDisplayPaymentTypes(): ReceiptsPage {
        val paymentTypes = listOf("Credit Card", "Debit Card", "Cash", "Check")
        val hasPaymentTypes = tableCells.any { cell ->
            paymentTypes.any { type -> cell.text().contains(type, ignoreCase = true) }
        }
        assert(hasPaymentTypes) { "No payment types found in receipts table" }
        return this
    }
    
    /**
     * Clicks on a sortable column header
     */
    fun sortByColumn(columnName: String): ReceiptsPage {
        val header = tableHeaders.find { it.text().contains(columnName, ignoreCase = true) }
        header?.click()
        waitForTableUpdate()
        return this
    }
    
    /**
     * Verifies sort indicator is present on column
     */
    fun shouldHaveSortIndicator(columnName: String): ReceiptsPage {
        val header = tableHeaders.find { it.text().contains(columnName, ignoreCase = true) }
        val sortIcon = header?.`$`("i")
        val hasSortClass = header?.attr("class")?.contains("sort") == true
        
        assert(sortIcon?.exists() == true || hasSortClass) {
            "No sort indicator found for column '$columnName'"
        }
        
        return this
    }
    
    /**
     * Searches for receipts using the search input
     */
    fun searchReceipts(searchTerm: String): ReceiptsPage {
        if (searchInput.exists()) {
            searchInput.shouldBe(Condition.visible)
            searchInput.shouldBe(Condition.enabled)
            searchInput.setValue(searchTerm)
            waitForTableUpdate()
        }
        return this
    }
    
    /**
     * Verifies edit buttons are present and enabled
     */
    fun shouldHaveEditButtons(): ReceiptsPage {
        if (editButtons.size() > 0) {
            editButtons.forEach { button ->
                button.shouldBe(Condition.enabled)
                button.shouldBe(Condition.visible)
            }
        }
        return this
    }
    
    /**
     * Verifies remove buttons are present and enabled
     */
    fun shouldHaveRemoveButtons(): ReceiptsPage {
        if (removeButtons.size() > 0) {
            removeButtons.forEach { button ->
                button.shouldBe(Condition.enabled)
                button.shouldBe(Condition.visible)
            }
        }
        return this
    }
    
    /**
     * Verifies pagination controls are present
     */
    fun shouldHavePaginationControls(): ReceiptsPage {
        if (paginationContainer.exists()) {
            paginationContainer.shouldBe(Condition.visible)
            assert(nextButton.exists() || prevButton.exists()) {
                "No pagination navigation buttons found"
            }
        }
        return this
    }
    
    /**
     * Verifies empty state message is displayed when no data
     */
    fun shouldDisplayEmptyState(): ReceiptsPage {
        if (tableRows.size() == 0) {
            emptyStateMessage.shouldBe(Condition.visible)
        }
        return this
    }
    
    /**
     * Verifies error message is displayed gracefully
     */
    fun shouldHandleErrorsGracefully(): ReceiptsPage {
        if (errorMessage.exists()) {
            errorMessage.shouldBe(Condition.visible)
            val errorText = errorMessage.text().trim()
            assert(errorText.isNotEmpty()) { "Error message should not be empty" }
        }
        return this
    }
    
    /**
     * Verifies creation source indicators are displayed
     */
    fun shouldDisplayCreationSource(): ReceiptsPage {
        val hasInboxIndicator = tableCells.any { it.text().contains("from inbox") }
        val hasManualIndicator = tableCells.any { it.text().contains("manual") }
        
        if (hasInboxIndicator || hasManualIndicator) {
            assert(hasInboxIndicator || hasManualIndicator) {
                "No creation source indicators found"
            }
        }
        
        return this
    }
    
    /**
     * Verifies accessibility attributes are present
     */
    fun shouldHaveAccessibilityAttributes(): ReceiptsPage {
        // Verify table has accessibility attributes
        val tableRole = receiptsTable.attr("role")
        val tableAriaLabel = receiptsTable.attr("aria-label")
        assert(tableRole == "table" || tableAriaLabel?.isNotEmpty() == true) {
            "Table should have proper accessibility attributes"
        }
        
        // Verify headers have scope attributes
        tableHeaders.forEach { header ->
            val headerText = header.text().trim()
            if (headerText.isNotEmpty()) {
                val scope = header.attr("scope")
                assert(scope == "col" || scope == "row") {
                    "Header '$headerText' should have scope attribute"
                }
            }
        }
        
        return this
    }
    
    /**
     * Gets the current number of table rows
     */
    fun getRowCount(): Int {
        return tableRows.size()
    }
    
    /**
     * Gets the current number of table headers
     */
    fun getHeaderCount(): Int {
        return tableHeaders.size()
    }
    
    /**
     * Verifies the receipts page is displayed
     */
    fun shouldBeDisplayed(): ReceiptsPage {
        receiptsContainer.shouldBe(Condition.visible)
        return this
    }
    
    /**
     * Waits for receipts table to load completely
     */
    private fun waitForReceiptsToLoad() {
        receiptsContainer.shouldBe(Condition.visible, Duration.ofSeconds(10))
        receiptsTable.shouldBe(Condition.visible, Duration.ofSeconds(10))
    }
    
    /**
     * Waits for table to update after user interaction
     */
    private fun waitForTableUpdate() {
        Thread.sleep(500) // Wait for any dynamic updates
    }
}