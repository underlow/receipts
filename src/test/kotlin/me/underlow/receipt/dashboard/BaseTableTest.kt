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
    fun `given base table when rendered with data then should display table with correct columns`() {
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
        
        // when - rendering table with data
        val html = baseTable.render(columns, data)
        
        // then - should display table with correct columns
        assertNotNull(html)
        assertTrue(html.contains("table"))
        assertTrue(html.contains("ID"))
        assertTrue(html.contains("Name"))
        assertTrue(html.contains("Date"))
        assertTrue(html.contains("Item 1"))
        assertTrue(html.contains("Item 2"))
    }

    @Test
    fun `given base table when rendered with empty data then should display empty state`() {
        // given - base table component with empty data
        val baseTable = BaseTable()
        val columns = listOf(
            TableColumn("id", "ID", sortable = true),
            TableColumn("name", "Name", sortable = true)
        )
        val data = emptyList<Map<String, String>>()
        
        // when - rendering table with empty data
        val html = baseTable.render(columns, data)
        
        // then - should display empty state
        assertNotNull(html)
        assertTrue(html.contains("table"))
        assertTrue(html.contains("No data"))
    }

    @Test
    fun `given base table when rendered with sortable columns then should include sorting functionality`() {
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
        
        // when - rendering table with sortable columns
        val html = baseTable.render(columns, data)
        
        // then - should include sorting functionality for sortable columns
        assertNotNull(html)
        assertTrue(html.contains("sortable"))
        assertTrue(html.contains("data-sort"))
    }

    @Test
    fun `given base table when rendered with pagination then should display pagination controls`() {
        // given - base table component with pagination configuration
        val baseTable = BaseTable()
        val columns = listOf(
            TableColumn("id", "ID", sortable = true),
            TableColumn("name", "Name", sortable = true)
        )
        val data = (1..25).map { mapOf("id" to it.toString(), "name" to "Item $it") }
        val paginationConfig = PaginationConfig(pageSize = 10, currentPage = 1, totalItems = 25)
        
        // when - rendering table with pagination
        val html = baseTable.render(columns, data, paginationConfig = paginationConfig)
        
        // then - should display pagination controls
        assertNotNull(html)
        assertTrue(html.contains("pagination"))
        assertTrue(html.contains("Previous"))
        assertTrue(html.contains("Next"))
        assertTrue(html.contains("page-link"))
    }

    @Test
    fun `given base table when rendered with search functionality then should include search box`() {
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
        
        // when - rendering table with search functionality
        val html = baseTable.render(columns, data, searchEnabled = true)
        
        // then - should include search box
        assertNotNull(html)
        assertTrue(html.contains("search"))
        assertTrue(html.contains("input"))
        assertTrue(html.contains("placeholder"))
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
    fun `given base table when rendered then should include proper CSS classes for styling`() {
        // given - base table component
        val baseTable = BaseTable()
        val columns = listOf(
            TableColumn("id", "ID", sortable = true),
            TableColumn("name", "Name", sortable = true)
        )
        val data = listOf(
            mapOf("id" to "1", "name" to "Item 1")
        )
        
        // when - rendering table
        val html = baseTable.render(columns, data)
        
        // then - should include proper CSS classes for styling
        assertNotNull(html)
        assertTrue(html.contains("table"))
        assertTrue(html.contains("table-striped"))
        assertTrue(html.contains("table-responsive"))
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
    fun `given base table when rendered with accessibility features then should include proper attributes`() {
        // given - base table component
        val baseTable = BaseTable()
        val columns = listOf(
            TableColumn("id", "ID", sortable = true),
            TableColumn("name", "Name", sortable = true)
        )
        val data = listOf(
            mapOf("id" to "1", "name" to "Item 1")
        )
        
        // when - rendering table
        val html = baseTable.render(columns, data)
        
        // then - should include proper accessibility attributes
        assertNotNull(html)
        assertTrue(html.contains("role=\"table\""))
        assertTrue(html.contains("aria-label"))
        assertTrue(html.contains("scope=\"col\""))
    }
}