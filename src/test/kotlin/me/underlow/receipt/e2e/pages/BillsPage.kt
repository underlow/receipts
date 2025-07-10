package me.underlow.receipt.e2e.pages

import com.codeborne.selenide.Condition
import com.codeborne.selenide.ElementsCollection
import com.codeborne.selenide.SelenideElement
import com.codeborne.selenide.Selenide.`$`
import com.codeborne.selenide.Selenide.`$$`
import java.time.Duration

/**
 * Page Object for bills management functionality.
 * Encapsulates all bills-related elements and actions using reliable selectors.
 */
class BillsPage {
    
    // Main bills container
    private val billsContainer get() = when {
        `$`("[data-test-id='bills-container']").exists() -> `$`("[data-test-id='bills-container']")
        `$`("#bills").exists() -> `$`("#bills")
        else -> `$`(".bills-container")
    }
    
    // Bills table elements
    private val billsTable get() = when {
        `$`("[data-test-id='bills-table']").exists() -> `$`("[data-test-id='bills-table']")
        billsContainer.`$`("table").exists() -> billsContainer.`$`("table")
        else -> `$`("table")
    }
    
    private val tableHeaders get() = when {
        `$$`("[data-test-id='bill-header']").size() > 0 -> `$$`("[data-test-id='bill-header']")
        billsTable.`$$`("th").size() > 0 -> billsTable.`$$`("th")
        else -> `$$`("th")
    }
    
    private val tableRows get() = when {
        `$$`("[data-test-id='bill-row']").size() > 0 -> `$$`("[data-test-id='bill-row']")
        billsTable.`$$`("tbody tr").size() > 0 -> billsTable.`$$`("tbody tr")
        else -> `$$`("tbody tr")
    }
    
    // Navigation elements
    private val billsTab get() = when {
        `$`("[data-test-id='bills-tab']").exists() -> `$`("[data-test-id='bills-tab']")
        `$`("a[href='#bills'][data-bs-toggle='tab']").exists() -> `$`("a[href='#bills'][data-bs-toggle='tab']")
        else -> `$`("a[href*='bills']")
    }
    
    // Search and filter elements
    private val searchInput get() = when {
        `$`("[data-test-id='bills-search']").exists() -> `$`("[data-test-id='bills-search']")
        `$`("#bills-search").exists() -> `$`("#bills-search")
        else -> billsContainer.`$`("input[type='search']")
    }
    
    // Action buttons
    private val editButtons get() = when {
        `$$`("[data-test-id='edit-bill-button']").size() > 0 -> `$$`("[data-test-id='edit-bill-button']")
        else -> `$$`("button").filter { it.text().contains("Edit") }
    }
    
    private val deleteButtons get() = when {
        `$$`("[data-test-id='delete-bill-button']").size() > 0 -> `$$`("[data-test-id='delete-bill-button']")
        else -> `$$`("button").filter { it.text().contains("Remove") || it.text().contains("Delete") }
    }
    
    // Pagination elements
    private val paginationContainer get() = when {
        `$`("[data-test-id='bills-pagination']").exists() -> `$`("[data-test-id='bills-pagination']")
        `$`(".pagination").exists() -> `$`(".pagination")
        else -> `$`(".page-navigation")
    }
    
    private val nextPageButton get() = when {
        `$`("[data-test-id='next-page-button']").exists() -> `$`("[data-test-id='next-page-button']")
        else -> `$$`("button, a").find { it.text().contains("Next") || it.text().contains("»") }
    }
    
    private val previousPageButton get() = when {
        `$`("[data-test-id='previous-page-button']").exists() -> `$`("[data-test-id='previous-page-button']")
        else -> `$$`("button, a").find { it.text().contains("Previous") || it.text().contains("«") }
    }
    
    // Empty state elements
    private val emptyStateMessage get() = when {
        `$`("[data-test-id='bills-empty-state']").exists() -> `$`("[data-test-id='bills-empty-state']")
        `$`(".empty-state").exists() -> `$`(".empty-state")
        else -> `$`(".no-data")
    }
    
    // Error handling elements
    private val errorMessages get() = when {
        `$$`("[data-test-id='error-message']").size() > 0 -> `$$`("[data-test-id='error-message']")
        else -> `$$`(".alert-danger, .error, .alert-error")
    }
    
    /**
     * Navigates to bills tab and waits for content to load
     */
    fun navigateToBills(): BillsPage {
        billsTab.shouldBe(Condition.visible).click()
        waitForBillsToLoad()
        return this
    }
    
    /**
     * Verifies bills page is displayed with required elements
     */
    fun shouldBeDisplayed(): BillsPage {
        billsContainer.shouldBe(Condition.visible)
        billsTable.shouldBe(Condition.visible)
        return this
    }
    
    /**
     * Verifies bills table has expected headers
     */
    fun shouldHaveExpectedHeaders(): BillsPage {
        val expectedHeaders = listOf("BILL DATE", "SERVICE PROVIDER", "AMOUNT", "DESCRIPTION", "CREATED DATE", "ACTIONS")
        
        tableHeaders.shouldHave(Condition.sizeGreaterThanOrEqual(expectedHeaders.size))
        
        expectedHeaders.forEach { expectedHeader ->
            tableHeaders.find { it.text().contains(expectedHeader, ignoreCase = true) }
                ?.shouldBe(Condition.visible)
                ?: throw AssertionError("Header '$expectedHeader' not found in bills table")
        }
        return this
    }
    
    /**
     * Verifies bills table contains data rows
     */
    fun shouldHaveDataRows(): BillsPage {
        tableRows.shouldHave(Condition.sizeGreaterThan(0))
        return this
    }
    
    /**
     * Verifies bills table displays properly formatted currency amounts
     */
    fun shouldDisplayFormattedCurrency(): BillsPage {
        val amountCells = getCellsContainingText("$")
        amountCells.shouldHave(Condition.sizeGreaterThan(0))
        
        amountCells.forEach { cell ->
            cell.text() shouldMatch Regex(".*\\$[0-9,]+\\.[0-9]{2}.*")
        }
        return this
    }
    
    /**
     * Verifies bills table displays service provider information
     */
    fun shouldDisplayServiceProviders(): BillsPage {
        val providerPatterns = listOf("Company", "Provider", "Electric", "Gas", "Water", "Internet", "Phone", "Mobile")
        val hasProviders = getCellsContainingAnyText(providerPatterns).size() > 0
        
        assert(hasProviders) { "No service provider information found in bills table" }
        return this
    }
    
    /**
     * Searches for bills using the search input
     */
    fun searchForBills(searchTerm: String): BillsPage {
        searchInput.shouldBe(Condition.visible, Condition.enabled)
            .setValue(searchTerm)
        waitForSearchResults()
        return this
    }
    
    /**
     * Sorts bills by clicking on a specific column header
     */
    fun sortByColumn(columnName: String): BillsPage {
        val header = tableHeaders.find { it.text().contains(columnName, ignoreCase = true) }
        header?.shouldBe(Condition.visible, Condition.enabled)?.click()
        waitForSortToComplete()
        return this
    }
    
    /**
     * Verifies sorting is applied by checking for sort indicators
     */
    fun shouldHaveSortIndicator(columnName: String): BillsPage {
        val header = tableHeaders.find { it.text().contains(columnName, ignoreCase = true) }
        header?.let { h ->
            val hasSortIcon = h.`$`("i").exists() || h.attr("class")?.contains("sort") == true
            assert(hasSortIcon) { "Sort indicator not found for column '$columnName'" }
        }
        return this
    }
    
    /**
     * Clicks the edit button for the first available bill
     */
    fun clickEditButton(): BillsPage {
        editButtons.first().shouldBe(Condition.visible, Condition.enabled).click()
        return this
    }
    
    /**
     * Clicks the delete button for the first available bill
     */
    fun clickDeleteButton(): BillsPage {
        deleteButtons.first().shouldBe(Condition.visible, Condition.enabled).click()
        return this
    }
    
    /**
     * Verifies edit buttons are present and enabled
     */
    fun shouldHaveEditButtons(): BillsPage {
        editButtons.shouldHave(Condition.sizeGreaterThan(0))
        editButtons.forEach { button ->
            button.shouldBe(Condition.visible, Condition.enabled)
        }
        return this
    }
    
    /**
     * Verifies delete buttons are present and enabled
     */
    fun shouldHaveDeleteButtons(): BillsPage {
        deleteButtons.shouldHave(Condition.sizeGreaterThan(0))
        deleteButtons.forEach { button ->
            button.shouldBe(Condition.visible, Condition.enabled)
        }
        return this
    }
    
    /**
     * Navigates to next page using pagination
     */
    fun goToNextPage(): BillsPage {
        nextPageButton?.shouldBe(Condition.visible, Condition.enabled)?.click()
        waitForPageToLoad()
        return this
    }
    
    /**
     * Navigates to previous page using pagination
     */
    fun goToPreviousPage(): BillsPage {
        previousPageButton?.shouldBe(Condition.visible, Condition.enabled)?.click()
        waitForPageToLoad()
        return this
    }
    
    /**
     * Verifies pagination controls are present
     */
    fun shouldHavePaginationControls(): BillsPage {
        paginationContainer.shouldBe(Condition.visible)
        assert(nextPageButton != null || previousPageButton != null) { 
            "No pagination controls found" 
        }
        return this
    }
    
    /**
     * Verifies empty state is displayed when no bills exist
     */
    fun shouldShowEmptyState(): BillsPage {
        emptyStateMessage.shouldBe(Condition.visible)
        return this
    }
    
    /**
     * Verifies no error messages are displayed
     */
    fun shouldNotHaveErrors(): BillsPage {
        errorMessages.filter { it.isDisplayed }.shouldHave(Condition.size(0))
        return this
    }
    
    /**
     * Verifies accessibility attributes are present
     */
    fun shouldHaveAccessibilityAttributes(): BillsPage {
        // Table should have proper accessibility attributes
        val tableRole = billsTable.attr("role")
        val tableAriaLabel = billsTable.attr("aria-label")
        assert(tableRole == "table" || !tableAriaLabel.isNullOrEmpty()) {
            "Table should have proper accessibility attributes"
        }
        
        // Headers should have scope attributes
        tableHeaders.forEach { header ->
            if (header.text().trim().isNotEmpty()) {
                val scope = header.attr("scope")
                assert(scope == "col" || scope == "row") {
                    "Header '${header.text()}' should have scope attribute"
                }
            }
        }
        
        return this
    }
    
    /**
     * Gets the number of bills currently displayed
     */
    fun getBillsCount(): Int = tableRows.size()
    
    /**
     * Gets all cells containing the specified text
     */
    private fun getCellsContainingText(text: String): ElementsCollection {
        return billsTable.`$$`("td").filter { it.text().contains(text) }
    }
    
    /**
     * Gets all cells containing any of the specified text patterns
     */
    private fun getCellsContainingAnyText(patterns: List<String>): ElementsCollection {
        return billsTable.`$$`("td").filter { cell ->
            patterns.any { pattern -> cell.text().contains(pattern, ignoreCase = true) }
        }
    }
    
    /**
     * Waits for bills content to load
     */
    private fun waitForBillsToLoad() {
        billsContainer.shouldBe(Condition.visible, Duration.ofSeconds(10))
        billsTable.shouldBe(Condition.visible, Duration.ofSeconds(10))
    }
    
    /**
     * Waits for search results to update
     */
    private fun waitForSearchResults() {
        // Wait for potential loading indicators to disappear
        Thread.sleep(1000)
    }
    
    /**
     * Waits for sort operation to complete
     */
    private fun waitForSortToComplete() {
        // Wait for sort operation to complete
        Thread.sleep(1000)
    }
    
    /**
     * Waits for page navigation to complete
     */
    private fun waitForPageToLoad() {
        billsTable.shouldBe(Condition.visible, Duration.ofSeconds(10))
        Thread.sleep(500)
    }
}

private infix fun String.shouldMatch(regex: Regex) {
    assert(regex.matches(this)) { "String '$this' does not match pattern ${regex.pattern}" }
}