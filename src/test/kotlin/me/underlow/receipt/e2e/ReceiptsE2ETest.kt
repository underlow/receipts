package me.underlow.receipt.e2e

import me.underlow.receipt.config.BaseE2ETest
import me.underlow.receipt.e2e.helpers.LoginHelper
import me.underlow.receipt.e2e.pages.ReceiptsPage
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName

/**
 * End-to-end tests for Receipts functionality.
 * Tests the complete user workflow from navigation to table display,
 * including user interactions with the receipts interface.
 * 
 * Follows best practices:
 * - Uses Page Object pattern for better maintainability
 * - Single responsibility per test method
 * - Descriptive test names following business logic
 * - Proper given-when-then structure
 * - Clean test state management
 */
class ReceiptsE2ETest : BaseE2ETest() {

    private lateinit var receiptsPage: ReceiptsPage
    private val loginHelper = LoginHelper()

    @BeforeEach
    fun setUpReceiptsTest() {
        // given - user is logged in and on dashboard
        loginHelper.loginAsAllowedUser1()
        waitForPageLoad()
        receiptsPage = ReceiptsPage()
    }

    @Test
    @DisplayName("Should display receipts table with expected headers when navigating to receipts")
    fun shouldDisplayReceiptsTableWithHeaders() {
        // given - user is authenticated and on dashboard
        assert(isOnDashboardPage()) { "User should be on dashboard page" }

        // when - user navigates to receipts tab
        receiptsPage.navigateToReceipts()

        // then - should display receipts table with expected headers
        receiptsPage
            .shouldBeDisplayed()
            .shouldDisplayReceiptsTable()
            .shouldHaveExpectedHeaders()
    }

    @Test
    @DisplayName("Should display receipts data with merchants when table is loaded")
    fun shouldDisplayReceiptsWithMerchants() {
        // given - user is on receipts view
        receiptsPage.navigateToReceipts()

        // when - table is loaded
        receiptsPage.shouldDisplayReceiptsTable()

        // then - should display receipts with merchant data
        receiptsPage
            .shouldHaveReceiptData()
            .shouldDisplayMerchants()
    }

    @Test
    @DisplayName("Should display properly formatted currency amounts")
    fun shouldDisplayFormattedCurrencyAmounts() {
        // given - user is on receipts view with data
        receiptsPage.navigateToReceipts()

        // when - table contains receipt data
        receiptsPage.shouldDisplayReceiptsTable()

        // then - should display amounts as properly formatted currency
        receiptsPage.shouldDisplayFormattedAmounts()
    }

    @Test
    @DisplayName("Should display payment types in receipts table")
    fun shouldDisplayPaymentTypes() {
        // given - user is on receipts view with data
        receiptsPage.navigateToReceipts()

        // when - table contains receipt data
        receiptsPage.shouldDisplayReceiptsTable()

        // then - should display payment types
        receiptsPage.shouldDisplayPaymentTypes()
    }

    @Test
    @DisplayName("Should sort receipts by payment date when payment date header is clicked")
    fun shouldSortReceiptsByPaymentDate() {
        // given - user is on receipts view with data
        receiptsPage.navigateToReceipts()
        receiptsPage.shouldDisplayReceiptsTable()

        // when - user clicks on payment date column header
        receiptsPage.sortByColumn("Payment Date")

        // then - should apply sorting with sort indicator
        receiptsPage.shouldHaveSortIndicator("Payment Date")
    }

    @Test
    @DisplayName("Should sort receipts by amount when amount header is clicked")
    fun shouldSortReceiptsByAmount() {
        // given - user is on receipts view with data
        receiptsPage.navigateToReceipts()
        receiptsPage.shouldDisplayReceiptsTable()

        // when - user clicks on amount column header
        receiptsPage.sortByColumn("Amount")

        // then - should apply amount sorting with sort indicator
        receiptsPage.shouldHaveSortIndicator("Amount")
    }

    @Test
    @DisplayName("Should display edit buttons for receipts when receipts exist")
    fun shouldDisplayEditButtonsForReceipts() {
        // given - user is on receipts view with created receipts
        receiptsPage.navigateToReceipts()
        receiptsPage.shouldDisplayReceiptsTable()

        // when - checking for edit functionality
        // then - should have edit buttons that are enabled
        receiptsPage.shouldHaveEditButtons()
    }

    @Test
    @DisplayName("Should display remove buttons for receipts when receipts exist")
    fun shouldDisplayRemoveButtonsForReceipts() {
        // given - user is on receipts view with created receipts
        receiptsPage.navigateToReceipts()
        receiptsPage.shouldDisplayReceiptsTable()

        // when - checking for remove functionality
        // then - should have remove buttons that are enabled
        receiptsPage.shouldHaveRemoveButtons()
    }

    @Test
    @DisplayName("Should display pagination controls when receipts data spans multiple pages")
    fun shouldDisplayPaginationControls() {
        // given - user is on receipts view with data
        receiptsPage.navigateToReceipts()
        receiptsPage.shouldDisplayReceiptsTable()

        // when - checking for pagination functionality
        // then - should have pagination controls when applicable
        receiptsPage.shouldHavePaginationControls()
    }

    @Test
    @DisplayName("Should filter receipts when search term is entered")
    fun shouldFilterReceiptsWhenSearching() {
        // given - user is on receipts view with data
        receiptsPage.navigateToReceipts()
        receiptsPage.shouldDisplayReceiptsTable()

        // when - user searches for a specific term
        receiptsPage.searchReceipts("starbucks")

        // then - search should not break the table structure
        receiptsPage.shouldDisplayReceiptsTable()
    }

    @Test
    @DisplayName("Should display creation source indicators for receipts")
    fun shouldDisplayCreationSourceIndicators() {
        // given - user is on receipts view with receipts from different sources
        receiptsPage.navigateToReceipts()
        receiptsPage.shouldDisplayReceiptsTable()

        // when - checking for creation source information
        // then - should display creation source indicators
        receiptsPage.shouldDisplayCreationSource()
    }



    @Test
    @DisplayName("Should display empty state when no receipts exist")
    fun shouldDisplayEmptyStateWhenNoReceipts() {
        // given - user is on receipts view with no data
        receiptsPage.navigateToReceipts()
        receiptsPage.shouldDisplayReceiptsTable()

        // when - checking for empty state
        // then - should display empty state message when no data exists
        receiptsPage.shouldDisplayEmptyState()
    }

    @Test
    @DisplayName("Should handle complete receipts workflow without errors")
    fun shouldHandleCompleteReceiptsWorkflow() {
        // given - user is on receipts view
        receiptsPage.navigateToReceipts()

        // when - user interacts with various elements
        receiptsPage.shouldDisplayReceiptsTable()

        // then - should handle all interactions without errors
        receiptsPage
            .shouldHaveExpectedHeaders()
            .shouldHaveReceiptData()
            .shouldDisplayMerchants()
            .shouldDisplayFormattedAmounts()
            .shouldDisplayPaymentTypes()
            .shouldHaveEditButtons()
            .shouldHaveRemoveButtons()
    }

    @Test
    @DisplayName("Should handle error states gracefully without breaking interface")
    fun shouldHandleErrorStatesGracefully() {
        // given - user is on receipts view
        receiptsPage.navigateToReceipts()

        // when - checking for error handling
        receiptsPage.shouldDisplayReceiptsTable()

        // then - should handle errors gracefully without breaking the interface
        receiptsPage.shouldHandleErrorsGracefully()
    }

    @Test
    @DisplayName("Should have proper accessibility attributes for screen readers")
    fun shouldHaveProperAccessibilityAttributes() {
        // given - user is on receipts view
        receiptsPage.navigateToReceipts()

        // when - checking accessibility attributes
        receiptsPage.shouldDisplayReceiptsTable()

        // then - should have proper accessibility features
        receiptsPage.shouldHaveAccessibilityAttributes()
    }



}