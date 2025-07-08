package me.underlow.receipt.dashboard

import me.underlow.receipt.model.ReceiptEntity
import me.underlow.receipt.model.ReceiptState
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Unit tests for ReceiptsView component.
 * Tests receipt-specific table rendering, column definitions, state display,
 * action buttons, and integration with BaseTable functionality.
 */
@ExtendWith(MockitoExtension::class)
class ReceiptsViewTest {

    @Mock
    private lateinit var baseTable: BaseTable

    @Test
    fun `given receipts view when get table columns then should return correct receipts columns`() {
        // given - receipts view component
        val receiptsView = ReceiptsView(baseTable)
        
        // when - getting table columns
        val columns = receiptsView.getTableColumns()
        
        // then - should return correct receipts columns
        assertNotNull(columns)
        assertEquals(7, columns.size)
        assertTrue(columns.any { it.key == "paymentDate" && it.label == "Payment Date" && it.sortable })
        assertTrue(columns.any { it.key == "merchantName" && it.label == "Merchant Name" && !it.sortable })
        assertTrue(columns.any { it.key == "amount" && it.label == "Amount" && it.sortable })
        assertTrue(columns.any { it.key == "paymentType" && it.label == "Payment Type" && !it.sortable })
        assertTrue(columns.any { it.key == "description" && it.label == "Description" && !it.sortable })
        assertTrue(columns.any { it.key == "createdDate" && it.label == "Created Date" && it.sortable })
        assertTrue(columns.any { it.key == "actions" && it.label == "Actions" && !it.sortable })
    }

    @Test
    fun `given receipts view when converting entity to row data then should include all required fields`() {
        // given - receipts view component and receipt entity
        val receiptsView = ReceiptsView(baseTable)
        val entity = ReceiptEntity(
            id = "1",
            paymentTypeId = "credit_card_001",
            paymentDate = LocalDate.of(2025, 1, 15),
            amount = BigDecimal("99.99"),
            inboxEntityId = "inbox123",
            state = ReceiptState.CREATED,
            createdDate = LocalDateTime.of(2025, 1, 1, 12, 0),
            description = "Grocery shopping",
            merchantName = "Walmart"
        )
        
        // when - converting entity to row data
        val rowData = receiptsView.convertEntityToRowData(entity)
        
        // then - should include all required fields
        assertNotNull(rowData)
        assertTrue(rowData.containsKey("paymentDate"))
        assertTrue(rowData.containsKey("merchantName"))
        assertTrue(rowData.containsKey("amount"))
        assertTrue(rowData.containsKey("paymentType"))
        assertTrue(rowData.containsKey("description"))
        assertTrue(rowData.containsKey("createdDate"))
        assertTrue(rowData.containsKey("actions"))
    }

    @Test
    fun `given receipts view when formatting amount then should return correctly formatted currency`() {
        // given - receipts view component
        val receiptsView = ReceiptsView(baseTable)
        
        // when - formatting different amounts
        val smallAmount = receiptsView.formatAmount(BigDecimal("1.50"))
        val largeAmount = receiptsView.formatAmount(BigDecimal("1234.56"))
        val wholeAmount = receiptsView.formatAmount(BigDecimal("100.00"))
        
        // then - should return correctly formatted currency
        assertNotNull(smallAmount)
        assertNotNull(largeAmount)
        assertNotNull(wholeAmount)
        assertTrue(smallAmount.contains("$1.50"))
        assertTrue(largeAmount.contains("$1,234.56"))
        assertTrue(wholeAmount.contains("$100.00"))
    }

    @Test
    fun `given receipts view when formatting payment type then should return payment type display name`() {
        // given - receipts view component
        val receiptsView = ReceiptsView(baseTable)
        
        // when - formatting payment type
        val paymentTypeDisplay = receiptsView.formatPaymentType("credit_card_001")
        
        // then - should return payment type display name
        assertNotNull(paymentTypeDisplay)
        assertTrue(paymentTypeDisplay.contains("Credit Card"))
    }

    @Test
    fun `given receipts view when formatting merchant name then should handle null and empty values`() {
        // given - receipts view component
        val receiptsView = ReceiptsView(baseTable)
        
        // when - formatting merchant names
        val validMerchant = receiptsView.formatMerchantName("Walmart")
        val nullMerchant = receiptsView.formatMerchantName(null)
        val emptyMerchant = receiptsView.formatMerchantName("")
        
        // then - should handle different merchant name scenarios
        assertNotNull(validMerchant)
        assertNotNull(nullMerchant)
        assertNotNull(emptyMerchant)
        assertTrue(validMerchant.contains("Walmart"))
        assertTrue(nullMerchant.contains("-"))
        assertTrue(emptyMerchant.contains("-"))
    }

    @Test
    fun `given receipts view when formatting description then should truncate long text`() {
        // given - receipts view component
        val receiptsView = ReceiptsView(baseTable)
        
        // when - formatting descriptions of different lengths
        val shortDescription = receiptsView.formatDescription("Grocery shopping")
        val longDescription = receiptsView.formatDescription("This is a very long description that should be truncated to prevent the table from becoming too wide and maintain readability for users")
        val nullDescription = receiptsView.formatDescription(null)
        
        // then - should handle descriptions appropriately
        assertNotNull(shortDescription)
        assertNotNull(longDescription)
        assertNotNull(nullDescription)
        assertTrue(shortDescription.contains("Grocery shopping"))
        assertTrue(longDescription.contains("This is a very long description"))
        assertTrue(nullDescription.contains("-"))
    }

    @Test
    fun `given receipts view when formatting actions then should return appropriate action buttons for receipt state`() {
        // given - receipts view component
        val receiptsView = ReceiptsView(baseTable)
        
        // when - formatting actions for different states
        val createdActions = receiptsView.formatActions(ReceiptState.CREATED, "1")
        val removedActions = receiptsView.formatActions(ReceiptState.REMOVED, "2")
        
        // then - should return appropriate action buttons
        assertNotNull(createdActions)
        assertNotNull(removedActions)
        assertTrue(createdActions.contains("Edit"))
        assertTrue(createdActions.contains("Remove"))
        assertTrue(removedActions.contains("Removed"))
    }

    @Test
    fun `given receipts view when converting created entity then should show edit and remove buttons`() {
        // given - receipts view component with created entity
        val receiptsView = ReceiptsView(baseTable)
        val createdEntity = ReceiptEntity(
            id = "1",
            paymentTypeId = "credit_card_001",
            paymentDate = LocalDate.of(2025, 1, 15),
            amount = BigDecimal("99.99"),
            state = ReceiptState.CREATED,
            createdDate = LocalDateTime.of(2025, 1, 1, 12, 0),
            merchantName = "Walmart"
        )
        
        // when - converting created entity to row data
        val rowData = receiptsView.convertEntityToRowData(createdEntity)
        
        // then - should show edit and remove buttons
        assertNotNull(rowData)
        assertTrue(rowData["actions"]?.contains("Edit") == true)
        assertTrue(rowData["actions"]?.contains("Remove") == true)
    }

    @Test
    fun `given receipts view when converting removed entity then should show removed status`() {
        // given - receipts view component with removed entity
        val receiptsView = ReceiptsView(baseTable)
        val removedEntity = ReceiptEntity(
            id = "1",
            paymentTypeId = "credit_card_001",
            paymentDate = LocalDate.of(2025, 1, 15),
            amount = BigDecimal("99.99"),
            state = ReceiptState.REMOVED,
            createdDate = LocalDateTime.of(2025, 1, 1, 12, 0),
            merchantName = "Walmart"
        )
        
        // when - converting removed entity to row data
        val rowData = receiptsView.convertEntityToRowData(removedEntity)
        
        // then - should show removed status
        assertNotNull(rowData)
        assertTrue(rowData["actions"]?.contains("Removed") == true)
    }

    @Test
    fun `given receipts view when converting entity created from inbox then should show linked inbox indicator`() {
        // given - receipts view component with entity created from inbox
        val receiptsView = ReceiptsView(baseTable)
        val inboxLinkedEntity = ReceiptEntity(
            id = "1",
            paymentTypeId = "credit_card_001",
            paymentDate = LocalDate.of(2025, 1, 15),
            amount = BigDecimal("99.99"),
            inboxEntityId = "inbox123",
            state = ReceiptState.CREATED,
            createdDate = LocalDateTime.of(2025, 1, 1, 12, 0),
            merchantName = "Walmart"
        )
        
        // when - converting entity to row data
        val rowData = receiptsView.convertEntityToRowData(inboxLinkedEntity)
        
        // then - should show linked inbox indicator
        assertNotNull(rowData)
        assertTrue(rowData["merchantName"]?.contains("inbox") == true)
    }

    @Test
    fun `given receipts view when converting entity created manually then should show manual creation indicator`() {
        // given - receipts view component with entity created manually
        val receiptsView = ReceiptsView(baseTable)
        val manualEntity = ReceiptEntity(
            id = "1",
            paymentTypeId = "credit_card_001",
            paymentDate = LocalDate.of(2025, 1, 15),
            amount = BigDecimal("99.99"),
            inboxEntityId = null,
            state = ReceiptState.CREATED,
            createdDate = LocalDateTime.of(2025, 1, 1, 12, 0),
            merchantName = "Walmart"
        )
        
        // when - converting entity to row data
        val rowData = receiptsView.convertEntityToRowData(manualEntity)
        
        // then - should show manual creation indicator
        assertNotNull(rowData)
        assertTrue(rowData["merchantName"]?.contains("manual") == true)
    }

    @Test
    fun `given receipts view when applying sorting then should use base table sorting`() {
        // given - receipts view component with mock BaseTable
        val receiptsView = ReceiptsView(baseTable)
        val receiptsData = listOf(
            ReceiptEntity(
                id = "1",
                paymentTypeId = "credit_card_001",
                paymentDate = LocalDate.of(2025, 1, 15),
                amount = BigDecimal("99.99"),
                state = ReceiptState.CREATED,
                createdDate = LocalDateTime.of(2025, 1, 1, 12, 0),
                merchantName = "Walmart"
            )
        )
        
        // Mock BaseTable behavior
        `when`(baseTable.applySorting(any(), any(), any())).thenReturn(
            listOf(mapOf("key" to "sorted_value"))
        )
        
        // when - applying sorting
        val sortedData = receiptsView.applySorting(receiptsData, "paymentDate", SortDirection.ASC)
        
        // then - should use base table sorting functionality
        assertNotNull(sortedData)
        assertTrue(sortedData.isNotEmpty())
    }

    @Test
    fun `given receipts view when applying pagination then should use base table pagination`() {
        // given - receipts view component with mock BaseTable
        val receiptsView = ReceiptsView(baseTable)
        val receiptsData = listOf(
            ReceiptEntity(
                id = "1",
                paymentTypeId = "credit_card_001",
                paymentDate = LocalDate.of(2025, 1, 15),
                amount = BigDecimal("99.99"),
                state = ReceiptState.CREATED,
                createdDate = LocalDateTime.of(2025, 1, 1, 12, 0),
                merchantName = "Walmart"
            )
        )
        
        // Mock BaseTable behavior
        `when`(baseTable.applyPagination(any(), any(), any())).thenReturn(
            listOf(mapOf("key" to "paginated_value"))
        )
        
        // when - applying pagination
        val paginatedData = receiptsView.applyPagination(receiptsData, 10, 1)
        
        // then - should use base table pagination functionality
        assertNotNull(paginatedData)
        assertTrue(paginatedData.isNotEmpty())
    }

    @Test
    fun `given receipts view when applying search then should use base table search`() {
        // given - receipts view component with mock BaseTable
        val receiptsView = ReceiptsView(baseTable)
        val receiptsData = listOf(
            ReceiptEntity(
                id = "1",
                paymentTypeId = "credit_card_001",
                paymentDate = LocalDate.of(2025, 1, 15),
                amount = BigDecimal("99.99"),
                state = ReceiptState.CREATED,
                createdDate = LocalDateTime.of(2025, 1, 1, 12, 0),
                merchantName = "Walmart"
            )
        )
        
        // Mock BaseTable behavior
        `when`(baseTable.applySearch(any(), any())).thenReturn(
            listOf(mapOf("key" to "filtered_value"))
        )
        
        // when - applying search
        val filteredData = receiptsView.applySearch(receiptsData, "search_term")
        
        // then - should use base table search functionality
        assertNotNull(filteredData)
        assertTrue(filteredData.isNotEmpty())
    }

    @Test
    fun `given receipts view when preparing table view data then should use base table preparation`() {
        // given - receipts view component with mock BaseTable
        val receiptsView = ReceiptsView(baseTable)
        val receiptsData = listOf(
            ReceiptEntity(
                id = "1",
                paymentTypeId = "credit_card_001",
                paymentDate = LocalDate.of(2025, 1, 15),
                amount = BigDecimal("99.99"),
                state = ReceiptState.CREATED,
                createdDate = LocalDateTime.of(2025, 1, 1, 12, 0),
                merchantName = "Walmart"
            )
        )
        
        val expectedTableViewData = TableViewData(
            columns = receiptsView.getTableColumns(),
            data = listOf(mapOf("key" to "value")),
            tableId = "receipts"
        )
        
        // Mock BaseTable behavior with specific arguments
        `when`(baseTable.prepareTableViewData(
            columns = receiptsView.getTableColumns(),
            data = listOf(mapOf(
                "id" to "1",
                "paymentDate" to "2025-01-15",
                "merchantName" to "Walmart (manual)",
                "amount" to "$99.99",
                "paymentType" to "Credit Card",
                "description" to "-",
                "createdDate" to "2025-01-01 12:00",
                "actions" to "Edit | Remove"
            )),
            tableId = "receipts",
            paginationConfig = null,
            searchEnabled = false,
            sortKey = null,
            sortDirection = SortDirection.ASC
        )).thenReturn(expectedTableViewData)
        
        // when - preparing table view data
        val tableViewData = receiptsView.prepareTableViewData(receiptsData)
        
        // then - should use base table preparation functionality
        assertNotNull(tableViewData)
        assertEquals("receipts", tableViewData.tableId)
    }
}