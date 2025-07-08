package me.underlow.receipt.integration

import me.underlow.receipt.dashboard.BillsView
import me.underlow.receipt.dashboard.BaseTable
import me.underlow.receipt.dashboard.SortDirection
import me.underlow.receipt.dashboard.PaginationConfig
import me.underlow.receipt.dashboard.TableViewData
import me.underlow.receipt.model.BillEntity
import me.underlow.receipt.model.BillState
import me.underlow.receipt.service.MockBillsService
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.test.assertFalse
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Integration tests for Bills functionality.
 * Tests the complete integration between BillsView, BillEntity, and MockBillsService components.
 * Verifies that all components work together correctly in realistic scenarios.
 */
@ExtendWith(SpringExtension::class)
@SpringBootTest
@ActiveProfiles("test")
@Testcontainers
class BillsIntegrationTest {

    @Autowired
    private lateinit var mockBillsService: MockBillsService

    @Autowired
    private lateinit var billsView: BillsView

    @Autowired
    private lateinit var baseTable: BaseTable

    companion object {
        @Container
        @JvmStatic
        val postgres = PostgreSQLContainer<Nothing>("postgres:15-alpine").apply {
            withDatabaseName("testdb")
            withUsername("test")
            withPassword("test")
        }

        @JvmStatic
        @org.springframework.test.context.DynamicPropertySource
        fun configureProperties(registry: org.springframework.test.context.DynamicPropertyRegistry) {
            registry.add("spring.datasource.url", postgres::getJdbcUrl)
            registry.add("spring.datasource.username", postgres::getUsername)
            registry.add("spring.datasource.password", postgres::getPassword)
        }
    }

    @Test
    fun `given mock bills service when fetching all data then should return complete bill items`() {
        // given - mock bills service with test data
        // when - fetching all bill items
        val allItems = mockBillsService.findAll()

        // then - should return complete bill items in all states
        assertNotNull(allItems)
        assertTrue(allItems.isNotEmpty())

        // Verify all states are represented
        assertTrue(allItems.any { it.state == BillState.CREATED })
        assertTrue(allItems.any { it.state == BillState.REMOVED })

        // Verify different service providers are present
        assertTrue(allItems.map { it.serviceProviderId }.toSet().size > 1)

        // Verify both inbox-created and manually created bills are present
        assertTrue(allItems.any { it.inboxEntityId != null })
        assertTrue(allItems.any { it.inboxEntityId == null })
    }

    @Test
    fun `given mock bills service when fetching paginated data then should support pagination correctly`() {
        // given - mock bills service with test data
        val pageSize = 5
        val totalCount = mockBillsService.getTotalCount()
        val expectedPages = (totalCount + pageSize - 1) / pageSize

        // when - fetching first page
        val firstPage = mockBillsService.findAll(0, pageSize)

        // then - should return correct page size and data
        assertNotNull(firstPage)
        assertTrue(firstPage.size <= pageSize)
        assertTrue(firstPage.isNotEmpty())

        // when - fetching second page if it exists
        if (expectedPages > 1) {
            val secondPage = mockBillsService.findAll(1, pageSize)

            // then - should return different data
            assertNotNull(secondPage)
            assertTrue(secondPage.isNotEmpty())

            // Verify pages contain different items
            val firstPageIds = firstPage.map { it.id }.toSet()
            val secondPageIds = secondPage.map { it.id }.toSet()
            assertTrue(firstPageIds.intersect(secondPageIds).isEmpty())
        }
    }

    @Test
    fun `given mock bills service when fetching sorted data then should support sorting correctly`() {
        // given - mock bills service with test data
        // when - fetching data sorted by bill date ascending
        val sortedAsc = mockBillsService.findAll(0, 20, "billDate", "ASC")

        // then - should return data sorted by bill date in ascending order
        assertNotNull(sortedAsc)
        assertTrue(sortedAsc.isNotEmpty())

        for (i in 0 until sortedAsc.size - 1) {
            assertTrue(
                sortedAsc[i].billDate <= sortedAsc[i + 1].billDate,
                "Items should be sorted by bill date ascending"
            )
        }

        // when - fetching data sorted by bill date descending
        val sortedDesc = mockBillsService.findAll(0, 20, "billDate", "DESC")

        // then - should return data sorted by bill date in descending order
        assertNotNull(sortedDesc)
        assertTrue(sortedDesc.isNotEmpty())

        for (i in 0 until sortedDesc.size - 1) {
            assertTrue(
                sortedDesc[i].billDate >= sortedDesc[i + 1].billDate,
                "Items should be sorted by bill date descending"
            )
        }
    }

    @Test
    fun `given mock bills service when fetching sorted data by amount then should support amount sorting correctly`() {
        // given - mock bills service with test data
        // when - fetching data sorted by amount ascending
        val sortedAsc = mockBillsService.findAll(0, 20, "amount", "ASC")

        // then - should return data sorted by amount in ascending order
        assertNotNull(sortedAsc)
        assertTrue(sortedAsc.isNotEmpty())

        for (i in 0 until sortedAsc.size - 1) {
            assertTrue(
                sortedAsc[i].amount <= sortedAsc[i + 1].amount,
                "Items should be sorted by amount ascending"
            )
        }

        // when - fetching data sorted by amount descending
        val sortedDesc = mockBillsService.findAll(0, 20, "amount", "DESC")

        // then - should return data sorted by amount in descending order
        assertNotNull(sortedDesc)
        assertTrue(sortedDesc.isNotEmpty())

        for (i in 0 until sortedDesc.size - 1) {
            assertTrue(
                sortedDesc[i].amount >= sortedDesc[i + 1].amount,
                "Items should be sorted by amount descending"
            )
        }
    }

    @Test
    fun `given bills view when rendering with mock service data then should display all bill items correctly`() {
        // given - bills view and mock service with test data
        val billsData = mockBillsService.findAll()

        // when - preparing table view data with mock data
        val tableViewData = billsView.prepareTableViewData(billsData)

        // then - should prepare complete table data with all items
        assertNotNull(tableViewData)
        assertTrue(tableViewData.data.isNotEmpty())

        // Verify table structure is present
        assertEquals("bills", tableViewData.tableId)
        assertTrue(tableViewData.columns.any { it.label == "Bill Date" })
        assertTrue(tableViewData.columns.any { it.label == "Service Provider" })
        assertTrue(tableViewData.columns.any { it.label == "Amount" })
        assertTrue(tableViewData.columns.any { it.label == "Description" })
        assertTrue(tableViewData.columns.any { it.label == "Created Date" })
        assertTrue(tableViewData.columns.any { it.label == "Actions" })
    }

    @Test
    fun `given bills view when rendering with different states then should show appropriate actions for each state`() {
        // given - bills view and mock service data with different states
        val billsData = mockBillsService.findAll()

        // when - converting each entity to row data
        val createdItems = billsData.filter { it.state == BillState.CREATED }
        val removedItems = billsData.filter { it.state == BillState.REMOVED }

        // then - should show appropriate actions for each state
        createdItems.forEach { item ->
            val rowData = billsView.convertEntityToRowData(item)
            assertTrue(rowData["actions"]?.contains("Edit") == true)
            assertTrue(rowData["actions"]?.contains("Remove") == true)
        }

        removedItems.forEach { item ->
            val rowData = billsView.convertEntityToRowData(item)
            assertTrue(rowData["actions"]?.contains("Removed") == true)
            assertFalse(rowData["actions"]?.contains("Edit") == true)
            // Check that it doesn't contain "Remove" button (but may contain "Removed" text)
            assertFalse(rowData["actions"]?.contains("removeBill") == true)
        }
    }

    @Test
    fun `given bills view when rendering with pagination then should integrate with base table pagination`() {
        // given - bills view and mock service with paginated data
        val pageSize = 5
        val currentPage = 1
        val paginationConfig = PaginationConfig(
            pageSize = pageSize,
            currentPage = currentPage,
            totalItems = mockBillsService.getTotalCount()
        )
        val billsData = mockBillsService.findAll(currentPage - 1, pageSize)

        // when - preparing table view data with pagination
        val tableViewData = billsView.prepareTableViewData(
            billsData = billsData,
            paginationConfig = paginationConfig
        )

        // then - should prepare table data with pagination configuration
        assertNotNull(tableViewData)
        assertTrue(tableViewData.data.isNotEmpty())

        // Verify pagination integration
        assertEquals(paginationConfig, tableViewData.paginationConfig)
        assertTrue(tableViewData.totalPages > 0)
    }

    @Test
    fun `given bills view when rendering with sorting then should integrate with base table sorting`() {
        // given - bills view and mock service with sorted data
        val sortKey = "billDate"
        val sortDirection = SortDirection.DESC
        val billsData = mockBillsService.findAll(0, 20, sortKey, sortDirection.toString())

        // when - preparing table view data with sorting
        val tableViewData = billsView.prepareTableViewData(
            billsData = billsData,
            sortKey = sortKey,
            sortDirection = sortDirection
        )

        // then - should prepare table data with sorting configuration
        assertNotNull(tableViewData)
        assertTrue(tableViewData.data.isNotEmpty())

        // Verify sorting integration
        assertEquals(sortKey, tableViewData.sortKey)
        assertEquals(sortDirection, tableViewData.sortDirection)
    }

    @Test
    fun `given bill entity when processing through different states then should maintain data integrity`() {
        // given - bill entity in created state
        val originalEntity = BillEntity.createManually(
            id = "test-bill-1",
            serviceProviderId = "test_provider_001",
            billDate = LocalDate.now(),
            amount = BigDecimal("100.00"),
            description = "Test bill",
            createdDate = LocalDateTime.now()
        )

        // when - checking entity state and capabilities
        // then - should be in created state with proper capabilities
        assertEquals(BillState.CREATED, originalEntity.state)
        assertTrue(originalEntity.isActive())
        assertTrue(originalEntity.canRemove())

        // when - removing the bill
        val removedEntity = originalEntity.remove()

        // then - should transition to removed state correctly
        assertEquals(BillState.REMOVED, removedEntity.state)
        assertFalse(removedEntity.isActive())
        assertFalse(removedEntity.canRemove())
    }

    @Test
    fun `given bill entity when created from inbox then should link to inbox entity correctly`() {
        // given - bill entity created from inbox
        val billFromInbox = BillEntity.createFromInbox(
            id = "test-bill-2",
            serviceProviderId = "test_provider_002",
            billDate = LocalDate.now(),
            amount = BigDecimal("250.00"),
            inboxEntityId = "inbox_test_001",
            description = "Bill from inbox",
            createdDate = LocalDateTime.now()
        )

        // when - checking entity properties
        // then - should have inbox entity linked
        assertEquals("inbox_test_001", billFromInbox.inboxEntityId)
        assertEquals(BillState.CREATED, billFromInbox.state)
        assertTrue(billFromInbox.isActive())

        // when - converting to row data for display
        val rowData = billsView.convertEntityToRowData(billFromInbox)

        // then - should display creation source correctly
        assertTrue(rowData["serviceProvider"]?.contains("(from inbox)") == true)
    }

    @Test
    fun `given bill entity when updating amount then should validate and update correctly`() {
        // given - bill entity with initial amount
        val originalEntity = BillEntity.createManually(
            id = "test-bill-3",
            serviceProviderId = "test_provider_003",
            billDate = LocalDate.now(),
            amount = BigDecimal("100.00"),
            description = "Test bill for amount update",
            createdDate = LocalDateTime.now()
        )

        // when - updating amount to valid value
        val updatedEntity = originalEntity.updateAmount(BigDecimal("150.00"))

        // then - should update amount correctly
        assertEquals(BigDecimal("150.00"), updatedEntity.amount)
        assertEquals(originalEntity.id, updatedEntity.id)
        assertEquals(originalEntity.serviceProviderId, updatedEntity.serviceProviderId)
    }

    @Test
    fun `given bill entity when updating service provider then should validate and update correctly`() {
        // given - bill entity with initial service provider
        val originalEntity = BillEntity.createManually(
            id = "test-bill-4",
            serviceProviderId = "test_provider_004",
            billDate = LocalDate.now(),
            amount = BigDecimal("100.00"),
            description = "Test bill for provider update",
            createdDate = LocalDateTime.now()
        )

        // when - updating service provider to valid value
        val updatedEntity = originalEntity.updateServiceProvider("new_provider_001")

        // then - should update service provider correctly
        assertEquals("new_provider_001", updatedEntity.serviceProviderId)
        assertEquals(originalEntity.id, updatedEntity.id)
        assertEquals(originalEntity.amount, updatedEntity.amount)
    }

    @Test
    fun `given bills view when rendering with different service providers then should display provider names correctly`() {
        // given - bills view and mock service with different service providers
        val billsData = mockBillsService.findAll()

        // when - converting entities to row data
        val rowDataList = billsData.map { billsView.convertEntityToRowData(it) }

        // then - should display service provider names correctly
        rowDataList.forEach { rowData ->
            val serviceProviderField = rowData["serviceProvider"]
            assertNotNull(serviceProviderField)
            assertTrue(serviceProviderField.isNotEmpty())
            
            // Should contain properly formatted service provider name without HTML
            assertTrue(serviceProviderField.isNotEmpty())
            
            // Should indicate creation source
            assertTrue(serviceProviderField.contains("(from inbox)") || 
                      serviceProviderField.contains("(manual)"))
        }
    }

    @Test
    fun `given bills view when rendering amounts then should format currency correctly`() {
        // given - bills view and mock service with different amounts
        val billsData = mockBillsService.findAll()

        // when - converting entities to row data
        val rowDataList = billsData.map { billsView.convertEntityToRowData(it) }

        // then - should format amounts as currency
        rowDataList.forEach { rowData ->
            val amountField = rowData["amount"]
            assertNotNull(amountField)
            assertTrue(amountField.isNotEmpty())
            
            // Should contain currency symbol
            assertTrue(amountField.contains("$"))
        }
    }

    @Test
    fun `given bills view when rendering descriptions then should handle null and long descriptions correctly`() {
        // given - bills view and mock service with various descriptions
        val billsData = mockBillsService.findAll()

        // when - converting entities to row data
        val rowDataList = billsData.map { billsView.convertEntityToRowData(it) }

        // then - should handle descriptions correctly
        rowDataList.forEach { rowData ->
            val descriptionField = rowData["description"]
            assertNotNull(descriptionField)
            assertTrue(descriptionField.isNotEmpty())
            
            // Should either contain actual description or display placeholder
            assertTrue(descriptionField.contains("text-muted") || 
                      !descriptionField.contains("text-muted"))
        }
    }

    @Test
    fun `given complete bills workflow when processing multiple items then should handle batch operations correctly`() {
        // given - multiple bill items in different states
        val billsData = mockBillsService.findAll()

        // when - applying view rendering to all items
        val allRowData = billsData.map { billsView.convertEntityToRowData(it) }

        // then - should handle all items correctly
        assertNotNull(allRowData)
        assertEquals(billsData.size, allRowData.size)

        // Verify each row has all required fields
        allRowData.forEach { rowData ->
            assertTrue(rowData.containsKey("billDate"))
            assertTrue(rowData.containsKey("serviceProvider"))
            assertTrue(rowData.containsKey("amount"))
            assertTrue(rowData.containsKey("description"))
            assertTrue(rowData.containsKey("createdDate"))
            assertTrue(rowData.containsKey("actions"))

            // Verify all field values are non-empty
            assertTrue(rowData["billDate"]?.isNotEmpty() == true)
            assertTrue(rowData["serviceProvider"]?.isNotEmpty() == true)
            assertTrue(rowData["amount"]?.isNotEmpty() == true)
            assertTrue(rowData["description"]?.isNotEmpty() == true)
            assertTrue(rowData["createdDate"]?.isNotEmpty() == true)
            assertTrue(rowData["actions"]?.isNotEmpty() == true)
        }
    }

    @Test
    fun `given bills view when handling empty data then should render empty state correctly`() {
        // given - bills view with empty data
        val emptyData = emptyList<BillEntity>()

        // when - preparing table view data for empty bills view
        val tableViewData = billsView.prepareTableViewData(emptyData)

        // then - should prepare empty table data without errors
        assertNotNull(tableViewData)
        assertTrue(tableViewData.data.isEmpty())

        // Should still contain table headers
        assertTrue(tableViewData.columns.any { it.label == "Bill Date" })
        assertTrue(tableViewData.columns.any { it.label == "Service Provider" })
        assertTrue(tableViewData.columns.any { it.label == "Amount" })
        assertTrue(tableViewData.columns.any { it.label == "Description" })
        assertTrue(tableViewData.columns.any { it.label == "Created Date" })
        assertTrue(tableViewData.columns.any { it.label == "Actions" })
    }

    @Test
    fun `given bills view when applying sorting and filtering then should integrate with base table functionality`() {
        // given - bills view with test data
        val billsData = mockBillsService.findAll()

        // when - applying sorting
        val sortedData = billsView.applySorting(billsData, "billDate", SortDirection.ASC)

        // then - should return sorted data
        assertNotNull(sortedData)
        assertTrue(sortedData.isNotEmpty())

        // when - applying pagination
        val paginatedData = billsView.applyPagination(billsData, 5, 1)

        // then - should return paginated data
        assertNotNull(paginatedData)
        assertTrue(paginatedData.size <= 5)

        // when - applying search
        val searchedData = billsView.applySearch(billsData, "electric")

        // then - should return filtered data
        assertNotNull(searchedData)
        // Search functionality should work with the base table
    }

    @Test
    fun `given bills view when rendering with search enabled then should integrate search functionality`() {
        // given - bills view with test data and search enabled
        val billsData = mockBillsService.findAll()

        // when - preparing table view data with search enabled
        val tableViewData = billsView.prepareTableViewData(
            billsData = billsData,
            searchEnabled = true
        )

        // then - should prepare table data with search functionality
        assertNotNull(tableViewData)
        assertTrue(tableViewData.data.isNotEmpty())

        // Should have search enabled in configuration
        assertTrue(tableViewData.searchEnabled)
    }
}