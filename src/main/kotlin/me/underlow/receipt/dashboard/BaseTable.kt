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
 * BaseTable component providing common table functionality for all entity views.
 * This component provides reusable table functionality including sorting, pagination,
 * search/filter capabilities, and proper empty state handling.
 */
@Component
class BaseTable {

    /**
     * Renders a data table with the provided columns and data.
     * 
     * @param columns the list of table columns to display
     * @param data the data to display in the table
     * @param paginationConfig optional pagination configuration
     * @param searchEnabled whether to show search functionality
     * @param sortKey optional current sort key
     * @param sortDirection optional current sort direction
     * @return HTML string containing the rendered table
     */
    fun render(
        columns: List<TableColumn>,
        data: List<Map<String, String>>,
        paginationConfig: PaginationConfig? = null,
        searchEnabled: Boolean = false,
        sortKey: String? = null,
        sortDirection: SortDirection = SortDirection.ASC
    ): String {
        return buildString {
            append("""
                <div class="table-container">
            """.trimIndent())
            
            // Add search box if enabled
            if (searchEnabled) {
                append(renderSearchBox())
            }
            
            // Add table wrapper for responsiveness
            append("""
                    <div class="table-responsive">
                        <table class="table table-striped table-hover" role="table" aria-label="Data table">
                            <thead class="table-dark">
                                <tr>
            """.trimIndent())
            
            // Render column headers
            columns.forEach { column ->
                val sortClass = if (column.sortable) "sortable" else ""
                val sortIcon = if (column.sortable && sortKey == column.key) {
                    when (sortDirection) {
                        SortDirection.ASC -> "fa-sort-up"
                        SortDirection.DESC -> "fa-sort-down"
                    }
                } else if (column.sortable) {
                    "fa-sort"
                } else ""
                
                append("""
                                    <th scope="col" class="$sortClass" ${if (column.sortable) "data-sort=\"${column.key}\"" else ""}>
                                        ${column.label}
                                        ${if (column.sortable) "<i class=\"fas $sortIcon ms-1\"></i>" else ""}
                                    </th>
                """.trimIndent())
            }
            
            append("""
                                </tr>
                            </thead>
                            <tbody>
            """.trimIndent())
            
            // Render data rows or empty state
            if (data.isEmpty()) {
                append(renderEmptyState(columns.size))
            } else {
                data.forEach { row ->
                    append("""
                                <tr>
                    """.trimIndent())
                    
                    columns.forEach { column ->
                        val cellValue = row[column.key] ?: ""
                        append("""
                                    <td>$cellValue</td>
                        """.trimIndent())
                    }
                    
                    append("""
                                </tr>
                    """.trimIndent())
                }
            }
            
            append("""
                            </tbody>
                        </table>
                    </div>
            """.trimIndent())
            
            // Add pagination if configured
            paginationConfig?.let { config ->
                append(renderPagination(config))
            }
            
            append("""
                </div>
            """.trimIndent())
        }
    }

    /**
     * Renders the search box for table filtering.
     * 
     * @return HTML string containing the search box
     */
    private fun renderSearchBox(): String {
        return """
            <div class="search-container mb-3">
                <div class="input-group">
                    <span class="input-group-text">
                        <i class="fas fa-search"></i>
                    </span>
                    <input type="text" class="form-control" id="table-search" placeholder="Search..." 
                           aria-label="Search table data">
                </div>
            </div>
        """.trimIndent()
    }

    /**
     * Renders the empty state when no data is available.
     * 
     * @param columnCount the number of columns in the table
     * @return HTML string containing the empty state
     */
    private fun renderEmptyState(columnCount: Int): String {
        return """
            <tr>
                <td colspan="$columnCount" class="text-center text-muted py-4">
                    <i class="fas fa-inbox fa-3x mb-3"></i>
                    <div class="h5">No data available</div>
                    <p class="mb-0">There are no items to display at the moment.</p>
                </td>
            </tr>
        """.trimIndent()
    }

    /**
     * Renders pagination controls for the table.
     * 
     * @param config the pagination configuration
     * @return HTML string containing pagination controls
     */
    private fun renderPagination(config: PaginationConfig): String {
        val totalPages = calculateTotalPages(config.totalItems, config.pageSize)
        val currentPage = config.currentPage
        
        return buildString {
            append("""
                <nav aria-label="Table pagination">
                    <ul class="pagination justify-content-center">
            """.trimIndent())
            
            // Previous button
            val prevDisabled = if (currentPage <= 1) "disabled" else ""
            append("""
                        <li class="page-item $prevDisabled">
                            <a class="page-link" href="#" data-page="${currentPage - 1}" aria-label="Previous">
                                <span aria-hidden="true">&laquo;</span>
                                Previous
                            </a>
                        </li>
            """.trimIndent())
            
            // Page numbers
            for (page in 1..totalPages) {
                val activeClass = if (page == currentPage) "active" else ""
                append("""
                        <li class="page-item $activeClass">
                            <a class="page-link" href="#" data-page="$page">$page</a>
                        </li>
                """.trimIndent())
            }
            
            // Next button
            val nextDisabled = if (currentPage >= totalPages) "disabled" else ""
            append("""
                        <li class="page-item $nextDisabled">
                            <a class="page-link" href="#" data-page="${currentPage + 1}" aria-label="Next">
                                Next
                                <span aria-hidden="true">&raquo;</span>
                            </a>
                        </li>
                    </ul>
                </nav>
            """.trimIndent())
        }
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