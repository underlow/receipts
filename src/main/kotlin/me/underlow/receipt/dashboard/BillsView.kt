package me.underlow.receipt.dashboard

import me.underlow.receipt.model.BillEntity
import me.underlow.receipt.model.BillState
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.text.NumberFormat
import java.time.format.DateTimeFormatter
import java.util.*

/**
 * BillsView component displays bills in a table format with appropriate columns and actions.
 * This component uses the BaseTable component to provide sorting, pagination, and search functionality
 * while adding bill-specific features like amount formatting, service provider display, and state-based action buttons.
 */
@Component
class BillsView(private val baseTable: BaseTable) {

    companion object {
        private val DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        private val DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
        private val CURRENCY_FORMATTER = NumberFormat.getCurrencyInstance(Locale.US)
        private const val DESCRIPTION_MAX_LENGTH = 50
    }

    /**
     * Renders the bills view with the provided bill data.
     * 
     * @param billsData the list of bill entities to display
     * @param paginationConfig optional pagination configuration
     * @param searchEnabled whether to show search functionality
     * @param sortKey optional current sort key
     * @param sortDirection optional current sort direction
     * @return HTML string containing the rendered bills table
     */
    fun render(
        billsData: List<BillEntity>,
        paginationConfig: PaginationConfig? = null,
        searchEnabled: Boolean = false,
        sortKey: String? = null,
        sortDirection: SortDirection = SortDirection.ASC
    ): String {
        val columns = getTableColumns()
        val rowData = billsData.map { convertEntityToRowData(it) }
        
        return baseTable.render(
            columns = columns,
            data = rowData,
            paginationConfig = paginationConfig,
            searchEnabled = searchEnabled,
            sortKey = sortKey,
            sortDirection = sortDirection
        )
    }

    /**
     * Gets the table column definitions for the bills view.
     * 
     * @return list of table columns for bills display
     */
    fun getTableColumns(): List<TableColumn> {
        return listOf(
            TableColumn(key = "billDate", label = "Bill Date", sortable = true),
            TableColumn(key = "serviceProvider", label = "Service Provider", sortable = false),
            TableColumn(key = "amount", label = "Amount", sortable = true),
            TableColumn(key = "description", label = "Description", sortable = false),
            TableColumn(key = "createdDate", label = "Created Date", sortable = true),
            TableColumn(key = "actions", label = "Actions", sortable = false)
        )
    }

    /**
     * Converts a BillEntity to a row data map for table display.
     * 
     * @param entity the bill entity to convert
     * @return map containing row data for table display
     */
    fun convertEntityToRowData(entity: BillEntity): Map<String, String> {
        return mapOf(
            "billDate" to entity.billDate.format(DATE_FORMATTER),
            "serviceProvider" to formatServiceProviderWithSource(entity.serviceProviderId, entity.inboxEntityId),
            "amount" to formatAmount(entity.amount),
            "description" to formatDescription(entity.description),
            "createdDate" to entity.createdDate.format(DATETIME_FORMATTER),
            "actions" to formatActions(entity.state, entity.id)
        )
    }

    /**
     * Formats the amount as currency for display.
     * 
     * @param amount the amount to format
     * @return formatted currency string
     */
    fun formatAmount(amount: BigDecimal): String {
        return CURRENCY_FORMATTER.format(amount)
    }

    /**
     * Formats the service provider for display based on provider ID.
     * 
     * @param serviceProviderId the service provider ID
     * @return HTML string with service provider display
     */
    fun formatServiceProvider(serviceProviderId: String): String {
        val providerName = when (serviceProviderId) {
            "electric_company_123" -> "Electric Company"
            "gas_company_456" -> "Gas Company"
            "water_company_789" -> "Water Company"
            "internet_provider_101" -> "Internet Provider"
            "phone_provider_202" -> "Phone Provider"
            else -> serviceProviderId.replace("_", " ").split(" ").joinToString(" ") { 
                it.lowercase().replaceFirstChar { char -> char.uppercase() }
            }
        }
        
        return """
            <div>
                <span class="fw-semibold">$providerName</span>
            </div>
        """.trimIndent()
    }

    /**
     * Formats the service provider for display with creation source indication.
     * 
     * @param serviceProviderId the service provider ID
     * @param inboxEntityId optional inbox entity ID for creation source indication
     * @return HTML string with service provider display and creation source
     */
    private fun formatServiceProviderWithSource(serviceProviderId: String, inboxEntityId: String?): String {
        val providerName = when (serviceProviderId) {
            "electric_company_123" -> "Electric Company"
            "gas_company_456" -> "Gas Company"
            "water_company_789" -> "Water Company"
            "internet_provider_101" -> "Internet Provider"
            "phone_provider_202" -> "Phone Provider"
            else -> serviceProviderId.replace("_", " ").split(" ").joinToString(" ") { 
                it.lowercase().replaceFirstChar { char -> char.uppercase() }
            }
        }
        
        val creationSource = if (inboxEntityId != null) {
            """<small class="text-muted d-block">
                <i class="fas fa-inbox me-1"></i>
                From inbox
            </small>"""
        } else {
            """<small class="text-muted d-block">
                <i class="fas fa-keyboard me-1"></i>
                manual entry
            </small>"""
        }
        
        return """
            <div>
                <span class="fw-semibold">$providerName</span>
                $creationSource
            </div>
        """.trimIndent()
    }

    /**
     * Formats the description for display, truncating if necessary.
     * 
     * @param description the description to format
     * @return formatted description with truncation if needed
     */
    fun formatDescription(description: String?): String {
        return if (description.isNullOrBlank()) {
            """<span class="text-muted">-</span>"""
        } else if (description.length <= DESCRIPTION_MAX_LENGTH) {
            description
        } else {
            val truncated = description.substring(0, DESCRIPTION_MAX_LENGTH)
            """<span title="$description">$truncated...</span>"""
        }
    }

    /**
     * Formats action buttons based on the bill state.
     * 
     * @param state the current bill state
     * @param entityId the entity ID for action buttons
     * @return HTML string with appropriate action buttons
     */
    fun formatActions(state: BillState, entityId: String): String {
        return when (state) {
            BillState.CREATED -> """
                <div class="btn-group" role="group">
                    <button type="button" class="btn btn-sm btn-outline-primary" 
                            onclick="editBill('$entityId')" 
                            title="Edit Bill">
                        <i class="fas fa-edit me-1"></i>
                        Edit
                    </button>
                    <button type="button" class="btn btn-sm btn-outline-danger" 
                            onclick="removeBill('$entityId')" 
                            title="Remove Bill">
                        <i class="fas fa-trash me-1"></i>
                        Remove
                    </button>
                </div>
            """.trimIndent()
            
            BillState.REMOVED -> """
                <span class="text-muted">
                    <i class="fas fa-trash me-1"></i>
                    Removed
                </span>
            """.trimIndent()
        }
    }

    /**
     * Applies sorting to bills data using the BaseTable sorting functionality.
     * 
     * @param billsData the bills data to sort
     * @param sortKey the key to sort by
     * @param sortDirection the direction to sort
     * @return sorted bills data converted to row data
     */
    fun applySorting(
        billsData: List<BillEntity>,
        sortKey: String,
        sortDirection: SortDirection
    ): List<Map<String, String>> {
        val rowData = billsData.map { convertEntityToRowData(it) }
        return baseTable.applySorting(rowData, sortKey, sortDirection)
    }

    /**
     * Applies pagination to bills data using the BaseTable pagination functionality.
     * 
     * @param billsData the bills data to paginate
     * @param pageSize the number of items per page
     * @param currentPage the current page number (1-based)
     * @return paginated bills data converted to row data
     */
    fun applyPagination(
        billsData: List<BillEntity>,
        pageSize: Int,
        currentPage: Int
    ): List<Map<String, String>> {
        val rowData = billsData.map { convertEntityToRowData(it) }
        return baseTable.applyPagination(rowData, pageSize, currentPage)
    }

    /**
     * Applies search filtering to bills data using the BaseTable search functionality.
     * 
     * @param billsData the bills data to filter
     * @param searchTerm the search term to filter by
     * @return filtered bills data converted to row data
     */
    fun applySearch(
        billsData: List<BillEntity>,
        searchTerm: String
    ): List<Map<String, String>> {
        val rowData = billsData.map { convertEntityToRowData(it) }
        return baseTable.applySearch(rowData, searchTerm)
    }
}