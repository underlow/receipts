package me.underlow.receipt.dashboard

import me.underlow.receipt.model.BillEntity
import me.underlow.receipt.model.BillState
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
 * Unit tests for BillsView component.
 * Tests bill-specific table rendering, column definitions, state display,
 * action buttons, and integration with BaseTable functionality.
 */
@ExtendWith(MockitoExtension::class)
class BillsViewTest {

    @Mock
    private lateinit var baseTable: BaseTable

    @Test
    fun `given bills view when get table columns then should return correct bills columns`() {
        // given - bills view component
        val billsView = BillsView(baseTable)
        
        // when - getting table columns
        val columns = billsView.getTableColumns()
        
        // then - should return correct bills columns
        assertNotNull(columns)
        assertEquals(6, columns.size)
        assertTrue(columns.any { it.key == "billDate" && it.label == "Bill Date" && it.sortable })
        assertTrue(columns.any { it.key == "serviceProvider" && it.label == "Service Provider" && !it.sortable })
        assertTrue(columns.any { it.key == "amount" && it.label == "Amount" && it.sortable })
        assertTrue(columns.any { it.key == "description" && it.label == "Description" && !it.sortable })
        assertTrue(columns.any { it.key == "createdDate" && it.label == "Created Date" && it.sortable })
        assertTrue(columns.any { it.key == "actions" && it.label == "Actions" && !it.sortable })
    }

    @Test
    fun `given bills view when converting entity to row data then should include all required fields`() {
        // given - bills view component and bill entity
        val billsView = BillsView(baseTable)
        val entity = BillEntity(
            id = "1",
            serviceProviderId = "provider123",
            billDate = LocalDate.of(2025, 1, 15),
            amount = BigDecimal("99.99"),
            inboxEntityId = "inbox123",
            state = BillState.CREATED,
            createdDate = LocalDateTime.of(2025, 1, 1, 12, 0),
            description = "Monthly electricity bill"
        )
        
        // when - converting entity to row data
        val rowData = billsView.convertEntityToRowData(entity)
        
        // then - should include all required fields
        assertNotNull(rowData)
        assertTrue(rowData.containsKey("billDate"))
        assertTrue(rowData.containsKey("serviceProvider"))
        assertTrue(rowData.containsKey("amount"))
        assertTrue(rowData.containsKey("description"))
        assertTrue(rowData.containsKey("createdDate"))
        assertTrue(rowData.containsKey("actions"))
    }

    @Test
    fun `given bills view when formatting amount then should return correctly formatted currency`() {
        // given - bills view component
        val billsView = BillsView(baseTable)
        
        // when - formatting different amounts
        val smallAmount = billsView.formatAmount(BigDecimal("1.50"))
        val largeAmount = billsView.formatAmount(BigDecimal("1234.56"))
        val wholeAmount = billsView.formatAmount(BigDecimal("100.00"))
        
        // then - should return correctly formatted currency
        assertNotNull(smallAmount)
        assertNotNull(largeAmount)
        assertNotNull(wholeAmount)
        assertTrue(smallAmount.contains("$1.50"))
        assertTrue(largeAmount.contains("$1,234.56"))
        assertTrue(wholeAmount.contains("$100.00"))
    }

    @Test
    fun `given bills view when formatting service provider then should return provider display name`() {
        // given - bills view component
        val billsView = BillsView(baseTable)
        
        // when - formatting service provider
        val providerDisplay = billsView.formatServiceProvider("electric_company_123")
        
        // then - should return provider display name
        assertNotNull(providerDisplay)
        assertTrue(providerDisplay.contains("Electric Company"))
    }

    @Test
    fun `given bills view when formatting description then should truncate long text`() {
        // given - bills view component
        val billsView = BillsView(baseTable)
        
        // when - formatting descriptions of different lengths
        val shortDescription = billsView.formatDescription("Short text")
        val longDescription = billsView.formatDescription("This is a very long description that should be truncated to prevent the table from becoming too wide and maintain readability")
        val nullDescription = billsView.formatDescription(null)
        
        // then - should handle descriptions appropriately
        assertNotNull(shortDescription)
        assertNotNull(longDescription)
        assertNotNull(nullDescription)
        assertTrue(shortDescription.contains("Short text"))
        assertTrue(longDescription.contains("This is a very long description"))
        assertTrue(nullDescription.contains("-"))
    }

    @Test
    fun `given bills view when formatting actions then should return appropriate action buttons for bill state`() {
        // given - bills view component
        val billsView = BillsView(baseTable)
        
        // when - formatting actions for different states
        val createdActions = billsView.formatActions(BillState.CREATED, "1")
        val removedActions = billsView.formatActions(BillState.REMOVED, "2")
        
        // then - should return appropriate action buttons
        assertNotNull(createdActions)
        assertNotNull(removedActions)
        assertTrue(createdActions.contains("Edit"))
        assertTrue(createdActions.contains("Remove"))
        assertTrue(removedActions.contains("Removed"))
    }

    @Test
    fun `given bills view when converting created entity then should show edit and remove buttons`() {
        // given - bills view component with created entity
        val billsView = BillsView(baseTable)
        val createdEntity = BillEntity(
            id = "1",
            serviceProviderId = "provider123",
            billDate = LocalDate.of(2025, 1, 15),
            amount = BigDecimal("99.99"),
            state = BillState.CREATED,
            createdDate = LocalDateTime.of(2025, 1, 1, 12, 0)
        )
        
        // when - converting created entity to row data
        val rowData = billsView.convertEntityToRowData(createdEntity)
        
        // then - should show edit and remove buttons
        assertNotNull(rowData)
        assertTrue(rowData["actions"]?.contains("Edit") == true)
        assertTrue(rowData["actions"]?.contains("Remove") == true)
    }

    @Test
    fun `given bills view when converting removed entity then should show removed status`() {
        // given - bills view component with removed entity
        val billsView = BillsView(baseTable)
        val removedEntity = BillEntity(
            id = "1",
            serviceProviderId = "provider123",
            billDate = LocalDate.of(2025, 1, 15),
            amount = BigDecimal("99.99"),
            state = BillState.REMOVED,
            createdDate = LocalDateTime.of(2025, 1, 1, 12, 0)
        )
        
        // when - converting removed entity to row data
        val rowData = billsView.convertEntityToRowData(removedEntity)
        
        // then - should show removed status
        assertNotNull(rowData)
        assertTrue(rowData["actions"]?.contains("Removed") == true)
    }

    @Test
    fun `given bills view when converting entity created from inbox then should show linked inbox indicator`() {
        // given - bills view component with entity created from inbox
        val billsView = BillsView(baseTable)
        val inboxLinkedEntity = BillEntity(
            id = "1",
            serviceProviderId = "provider123",
            billDate = LocalDate.of(2025, 1, 15),
            amount = BigDecimal("99.99"),
            inboxEntityId = "inbox123",
            state = BillState.CREATED,
            createdDate = LocalDateTime.of(2025, 1, 1, 12, 0)
        )
        
        // when - converting entity to row data
        val rowData = billsView.convertEntityToRowData(inboxLinkedEntity)
        
        // then - should show linked inbox indicator
        assertNotNull(rowData)
        assertTrue(rowData["serviceProvider"]?.contains("inbox") == true)
    }

    @Test
    fun `given bills view when converting entity created manually then should show manual creation indicator`() {
        // given - bills view component with entity created manually
        val billsView = BillsView(baseTable)
        val manualEntity = BillEntity(
            id = "1",
            serviceProviderId = "provider123",
            billDate = LocalDate.of(2025, 1, 15),
            amount = BigDecimal("99.99"),
            inboxEntityId = null,
            state = BillState.CREATED,
            createdDate = LocalDateTime.of(2025, 1, 1, 12, 0)
        )
        
        // when - converting entity to row data
        val rowData = billsView.convertEntityToRowData(manualEntity)
        
        // then - should show manual creation indicator
        assertNotNull(rowData)
        assertTrue(rowData["serviceProvider"]?.contains("manual") == true)
    }

    @Test
    fun `given bills view when applying sorting then should use base table sorting`() {
        // given - bills view component with mock BaseTable
        val billsView = BillsView(baseTable)
        val billsData = listOf(
            BillEntity(
                id = "1",
                serviceProviderId = "provider123",
                billDate = LocalDate.of(2025, 1, 15),
                amount = BigDecimal("99.99"),
                state = BillState.CREATED,
                createdDate = LocalDateTime.of(2025, 1, 1, 12, 0)
            )
        )
        
        // Mock BaseTable behavior
        `when`(baseTable.applySorting(any(), any(), any())).thenReturn(
            listOf(mapOf("key" to "sorted_value"))
        )
        
        // when - applying sorting
        val sortedData = billsView.applySorting(billsData, "billDate", SortDirection.ASC)
        
        // then - should use base table sorting functionality
        assertNotNull(sortedData)
        assertTrue(sortedData.isNotEmpty())
    }

    @Test
    fun `given bills view when applying pagination then should use base table pagination`() {
        // given - bills view component with mock BaseTable
        val billsView = BillsView(baseTable)
        val billsData = listOf(
            BillEntity(
                id = "1",
                serviceProviderId = "provider123",
                billDate = LocalDate.of(2025, 1, 15),
                amount = BigDecimal("99.99"),
                state = BillState.CREATED,
                createdDate = LocalDateTime.of(2025, 1, 1, 12, 0)
            )
        )
        
        // Mock BaseTable behavior
        `when`(baseTable.applyPagination(any(), any(), any())).thenReturn(
            listOf(mapOf("key" to "paginated_value"))
        )
        
        // when - applying pagination
        val paginatedData = billsView.applyPagination(billsData, 10, 1)
        
        // then - should use base table pagination functionality
        assertNotNull(paginatedData)
        assertTrue(paginatedData.isNotEmpty())
    }

    @Test
    fun `given bills view when applying search then should use base table search`() {
        // given - bills view component with mock BaseTable
        val billsView = BillsView(baseTable)
        val billsData = listOf(
            BillEntity(
                id = "1",
                serviceProviderId = "provider123",
                billDate = LocalDate.of(2025, 1, 15),
                amount = BigDecimal("99.99"),
                state = BillState.CREATED,
                createdDate = LocalDateTime.of(2025, 1, 1, 12, 0)
            )
        )
        
        // Mock BaseTable behavior
        `when`(baseTable.applySearch(any(), any())).thenReturn(
            listOf(mapOf("key" to "filtered_value"))
        )
        
        // when - applying search
        val filteredData = billsView.applySearch(billsData, "search_term")
        
        // then - should use base table search functionality
        assertNotNull(filteredData)
        assertTrue(filteredData.isNotEmpty())
    }
}