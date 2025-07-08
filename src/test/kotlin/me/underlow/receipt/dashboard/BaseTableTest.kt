package me.underlow.receipt.dashboard

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.test.assertFalse

/**
 * Unit tests for BaseTable component.
 * Tests table rendering, sorting, pagination, search functionality, and empty state handling.
 */
@ExtendWith(MockitoExtension::class)
class BaseTableTest {

    @Test
    fun `given base table when preparing view data then should return correct table view data`() {
        // given - base table component with sample data
        val baseTable = BaseTable()
        val columns = listOf(
            TableColumn("id", "ID", sortable = true),
            TableColumn("name", "Name", sortable = true),
            TableColumn("date", "Date", sortable = true)
        )
        val data = listOf(
            mapOf("id" to "1", "name" to "Item 1", "date" to "2025-01-01"),
            mapOf("id" to "2", "name" to "Item 2", "date" to "2025-01-02")
        )
        
        // when - preparing table view data
        val tableViewData = baseTable.prepareTableViewData(columns, data)
        
        // then - should return correct table view data
        assertNotNull(tableViewData)
        assertEquals(columns, tableViewData.columns)
        assertEquals(data, tableViewData.data)
        assertEquals("default", tableViewData.tableId)
        assertEquals(false, tableViewData.searchEnabled)
    }

    @Test
    fun `given base table when preparing view data with empty data then should return empty data in view data`() {
        // given - base table component with empty data
        val baseTable = BaseTable()
        val columns = listOf(
            TableColumn("id", "ID", sortable = true),
            TableColumn("name", "Name", sortable = true)
        )
        val data = emptyList<Map<String, String>>()
        
        // when - preparing table view data with empty data
        val tableViewData = baseTable.prepareTableViewData(columns, data)
        
        // then - should return empty data in view data
        assertNotNull(tableViewData)
        assertEquals(columns, tableViewData.columns)
        assertTrue(tableViewData.data.isEmpty())
    }

    @Test
    fun `given base table when preparing view data with sortable columns then should include sorting configuration`() {
        // given - base table component with sortable columns
        val baseTable = BaseTable()
        val columns = listOf(
            TableColumn("id", "ID", sortable = true),
            TableColumn("name", "Name", sortable = true),
            TableColumn("date", "Date", sortable = false)
        )
        val data = listOf(
            mapOf("id" to "1", "name" to "Item 1", "date" to "2025-01-01")
        )
        
        // when - preparing table view data with sortable columns and sort key
        val tableViewData = baseTable.prepareTableViewData(columns, data, sortKey = "name", sortDirection = SortDirection.DESC)
        
        // then - should include sorting configuration
        assertNotNull(tableViewData)
        assertEquals("name", tableViewData.sortKey)
        assertEquals(SortDirection.DESC, tableViewData.sortDirection)
        assertTrue(tableViewData.columns.any { it.sortable })
    }

    @Test
    fun `given base table when preparing view data with pagination then should include pagination configuration`() {
        // given - base table component with pagination configuration
        val baseTable = BaseTable()
        val columns = listOf(
            TableColumn("id", "ID", sortable = true),
            TableColumn("name", "Name", sortable = true)
        )
        val data = (1..25).map { mapOf("id" to it.toString(), "name" to "Item $it") }
        val paginationConfig = PaginationConfig(pageSize = 10, currentPage = 1, totalItems = 25)
        
        // when - preparing table view data with pagination
        val tableViewData = baseTable.prepareTableViewData(columns, data, paginationConfig = paginationConfig)
        
        // then - should include pagination configuration
        assertNotNull(tableViewData)
        assertEquals(paginationConfig, tableViewData.paginationConfig)
        assertEquals(3, tableViewData.totalPages)
    }

    @Test
    fun `given base table when preparing view data with search functionality then should include search configuration`() {
        // given - base table component with search functionality enabled
        val baseTable = BaseTable()
        val columns = listOf(
            TableColumn("id", "ID", sortable = true),
            TableColumn("name", "Name", sortable = true)
        )
        val data = listOf(
            mapOf("id" to "1", "name" to "Item 1"),
            mapOf("id" to "2", "name" to "Item 2")
        )
        
        // when - preparing table view data with search functionality
        val tableViewData = baseTable.prepareTableViewData(columns, data, searchEnabled = true)
        
        // then - should include search configuration
        assertNotNull(tableViewData)
        assertTrue(tableViewData.searchEnabled)
    }

    @Test
    fun `given base table when applying sort then should return sorted data`() {
        // given - base table component with unsorted data
        val baseTable = BaseTable()
        val data = listOf(
            mapOf("id" to "3", "name" to "Item C"),
            mapOf("id" to "1", "name" to "Item A"),
            mapOf("id" to "2", "name" to "Item B")
        )
        
        // when - applying sort by name ascending
        val sortedData = baseTable.applySorting(data, "name", SortDirection.ASC)
        
        // then - should return data sorted by name
        assertNotNull(sortedData)
        assertEquals(3, sortedData.size)
        assertEquals("Item A", sortedData[0]["name"])
        assertEquals("Item B", sortedData[1]["name"])
        assertEquals("Item C", sortedData[2]["name"])
    }

    @Test
    fun `given base table when applying sort descending then should return reverse sorted data`() {
        // given - base table component with unsorted data
        val baseTable = BaseTable()
        val data = listOf(
            mapOf("id" to "1", "name" to "Item A"),
            mapOf("id" to "2", "name" to "Item B"),
            mapOf("id" to "3", "name" to "Item C")
        )
        
        // when - applying sort by name descending
        val sortedData = baseTable.applySorting(data, "name", SortDirection.DESC)
        
        // then - should return data sorted by name in reverse order
        assertNotNull(sortedData)
        assertEquals(3, sortedData.size)
        assertEquals("Item C", sortedData[0]["name"])
        assertEquals("Item B", sortedData[1]["name"])
        assertEquals("Item A", sortedData[2]["name"])
    }

    @Test
    fun `given base table when applying pagination then should return paginated data`() {
        // given - base table component with large dataset
        val baseTable = BaseTable()
        val data = (1..25).map { mapOf("id" to it.toString(), "name" to "Item $it") }
        
        // when - applying pagination for page 2 with page size 10
        val paginatedData = baseTable.applyPagination(data, pageSize = 10, currentPage = 2)
        
        // then - should return data for page 2
        assertNotNull(paginatedData)
        assertEquals(10, paginatedData.size)
        assertEquals("Item 11", paginatedData[0]["name"])
        assertEquals("Item 20", paginatedData[9]["name"])
    }

    @Test
    fun `given base table when applying search filter then should return filtered data`() {
        // given - base table component with data to filter
        val baseTable = BaseTable()
        val data = listOf(
            mapOf("id" to "1", "name" to "Apple", "category" to "Fruit"),
            mapOf("id" to "2", "name" to "Banana", "category" to "Fruit"),
            mapOf("id" to "3", "name" to "Carrot", "category" to "Vegetable")
        )
        
        // when - applying search filter for "Fruit"
        val filteredData = baseTable.applySearch(data, "Fruit")
        
        // then - should return only matching items
        assertNotNull(filteredData)
        assertEquals(2, filteredData.size)
        assertTrue(filteredData.all { it["category"] == "Fruit" })
    }

    @Test
    fun `given base table when preparing view data with custom table id then should include custom table id`() {
        // given - base table component
        val baseTable = BaseTable()
        val columns = listOf(
            TableColumn("id", "ID", sortable = true),
            TableColumn("name", "Name", sortable = true)
        )
        val data = listOf(
            mapOf("id" to "1", "name" to "Item 1")
        )
        
        // when - preparing table view data with custom table id
        val tableViewData = baseTable.prepareTableViewData(columns, data, tableId = "custom-table")
        
        // then - should include custom table id
        assertNotNull(tableViewData)
        assertEquals("custom-table", tableViewData.tableId)
    }

    @Test
    fun `given base table when calculating total pages then should return correct page count`() {
        // given - base table component with data
        val baseTable = BaseTable()
        val totalItems = 25
        val pageSize = 10
        
        // when - calculating total pages
        val totalPages = baseTable.calculateTotalPages(totalItems, pageSize)
        
        // then - should return correct page count
        assertEquals(3, totalPages)
    }

    @Test
    fun `given base table when checking if page is valid then should validate correctly`() {
        // given - base table component with pagination
        val baseTable = BaseTable()
        val totalPages = 5
        
        // when - checking if page numbers are valid
        val isPage1Valid = baseTable.isValidPage(1, totalPages)
        val isPage5Valid = baseTable.isValidPage(5, totalPages)
        val isPage0Valid = baseTable.isValidPage(0, totalPages)
        val isPage6Valid = baseTable.isValidPage(6, totalPages)
        
        // then - should validate page numbers correctly
        assertTrue(isPage1Valid)
        assertTrue(isPage5Valid)
        assertFalse(isPage0Valid)
        assertFalse(isPage6Valid)
    }

    @Test
    fun `given base table when preparing view data with all parameters then should include all configurations`() {
        // given - base table component
        val baseTable = BaseTable()
        val columns = listOf(
            TableColumn("id", "ID", sortable = true),
            TableColumn("name", "Name", sortable = true)
        )
        val data = listOf(
            mapOf("id" to "1", "name" to "Item 1")
        )
        val paginationConfig = PaginationConfig(pageSize = 10, currentPage = 1, totalItems = 1)
        
        // when - preparing table view data with all parameters
        val tableViewData = baseTable.prepareTableViewData(
            columns = columns,
            data = data,
            tableId = "test-table",
            paginationConfig = paginationConfig,
            searchEnabled = true,
            sortKey = "id",
            sortDirection = SortDirection.ASC
        )
        
        // then - should include all configurations
        assertNotNull(tableViewData)
        assertEquals(columns, tableViewData.columns)
        assertEquals(data, tableViewData.data)
        assertEquals("test-table", tableViewData.tableId)
        assertEquals(paginationConfig, tableViewData.paginationConfig)
        assertEquals(1, tableViewData.totalPages)
        assertTrue(tableViewData.searchEnabled)
        assertEquals("id", tableViewData.sortKey)
        assertEquals(SortDirection.ASC, tableViewData.sortDirection)
    }
}