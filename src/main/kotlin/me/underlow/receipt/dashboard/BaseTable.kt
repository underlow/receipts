package me.underlow.receipt.dashboard

import org.springframework.stereotype.Component

/**
 * Represents a table column configuration.
 * 
 * @param key the field key for data lookup
 * @param label the display label for the column
 * @param sortable whether the column can be sorted
 */
data class TableColumn(
    val key: String,
    val label: String,
    val sortable: Boolean = false
)

/**
 * Represents pagination configuration for the table.
 * 
 * @param pageSize the number of items per page
 * @param currentPage the current page number (1-based)
 * @param totalItems the total number of items
 */
data class PaginationConfig(
    val pageSize: Int,
    val currentPage: Int,
    val totalItems: Int
)

/**
 * Enum representing sort directions.
 */
enum class SortDirection {
    ASC, DESC
}

/**
 * Represents table view data for template rendering.
 * 
 * @param columns the list of table columns to display
 * @param data the data to display in the table
 * @param tableId unique identifier for the table to avoid ID conflicts
 * @param paginationConfig optional pagination configuration with total pages
 * @param searchEnabled whether to show search functionality
 * @param sortKey optional current sort key
 * @param sortDirection optional current sort direction
 */
data class TableViewData(
    val columns: List<TableColumn>,
    val data: List<Map<String, String>>,
    val tableId: String = "default",
    val paginationConfig: PaginationConfig? = null,
    val totalPages: Int = 1,
    val searchEnabled: Boolean = false,
    val sortKey: String? = null,
    val sortDirection: SortDirection = SortDirection.ASC
)

/**
 * BaseTable component providing common table functionality for all entity views.
 * This component provides reusable table functionality including sorting, pagination,
 * search/filter capabilities, and proper empty state handling.
 */
@Component
class BaseTable {

    /**
     * Prepares table view data for template rendering.
     * 
     * @param columns the list of table columns to display
     * @param data the data to display in the table
     * @param tableId unique identifier for the table to avoid ID conflicts
     * @param paginationConfig optional pagination configuration
     * @param searchEnabled whether to show search functionality
     * @param sortKey optional current sort key
     * @param sortDirection optional current sort direction
     * @return TableViewData object for template rendering
     */
    fun prepareTableViewData(
        columns: List<TableColumn>,
        data: List<Map<String, String>>,
        tableId: String = "default",
        paginationConfig: PaginationConfig? = null,
        searchEnabled: Boolean = false,
        sortKey: String? = null,
        sortDirection: SortDirection = SortDirection.ASC
    ): TableViewData {
        val totalPages = paginationConfig?.let { 
            calculateTotalPages(it.totalItems, it.pageSize)
        } ?: 1
        
        return TableViewData(
            columns = columns,
            data = data,
            tableId = tableId,
            paginationConfig = paginationConfig,
            totalPages = totalPages,
            searchEnabled = searchEnabled,
            sortKey = sortKey,
            sortDirection = sortDirection
        )
    }


    /**
     * Applies sorting to the provided data.
     * 
     * @param data the data to sort
     * @param sortKey the key to sort by
     * @param sortDirection the direction to sort
     * @return sorted data
     */
    fun applySorting(
        data: List<Map<String, String>>,
        sortKey: String,
        sortDirection: SortDirection
    ): List<Map<String, String>> {
        return when (sortDirection) {
            SortDirection.ASC -> data.sortedBy { it[sortKey] ?: "" }
            SortDirection.DESC -> data.sortedByDescending { it[sortKey] ?: "" }
        }
    }

    /**
     * Applies pagination to the provided data.
     * 
     * @param data the data to paginate
     * @param pageSize the number of items per page
     * @param currentPage the current page number (1-based)
     * @return paginated data
     */
    fun applyPagination(
        data: List<Map<String, String>>,
        pageSize: Int,
        currentPage: Int
    ): List<Map<String, String>> {
        val startIndex = (currentPage - 1) * pageSize
        val endIndex = minOf(startIndex + pageSize, data.size)
        
        return if (startIndex >= data.size) {
            emptyList()
        } else {
            data.subList(startIndex, endIndex)
        }
    }

    /**
     * Applies search filtering to the provided data.
     * 
     * @param data the data to filter
     * @param searchTerm the search term to filter by
     * @return filtered data
     */
    fun applySearch(
        data: List<Map<String, String>>,
        searchTerm: String
    ): List<Map<String, String>> {
        if (searchTerm.isBlank()) {
            return data
        }
        
        val searchTermLower = searchTerm.lowercase()
        return data.filter { row ->
            row.values.any { value ->
                value.lowercase().contains(searchTermLower)
            }
        }
    }

    /**
     * Calculates the total number of pages based on total items and page size.
     * 
     * @param totalItems the total number of items
     * @param pageSize the number of items per page
     * @return the total number of pages
     */
    fun calculateTotalPages(totalItems: Int, pageSize: Int): Int {
        return if (totalItems == 0) 1 else (totalItems + pageSize - 1) / pageSize
    }

    /**
     * Validates if the given page number is valid.
     * 
     * @param page the page number to validate
     * @param totalPages the total number of pages
     * @return true if the page is valid, false otherwise
     */
    fun isValidPage(page: Int, totalPages: Int): Boolean {
        return page in 1..totalPages
    }
}