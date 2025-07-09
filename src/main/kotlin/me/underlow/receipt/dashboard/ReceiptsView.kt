package me.underlow.receipt.dashboard

import me.underlow.receipt.model.ReceiptEntity
import me.underlow.receipt.model.ReceiptState
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.text.NumberFormat
import java.time.format.DateTimeFormatter
import java.util.*

/**
 * ReceiptsView component displays receipts in a table format with appropriate columns and actions.
 * This component uses the BaseTable component to provide sorting, pagination, and search functionality
 * while adding receipt-specific features like amount formatting, payment type display, merchant name handling,
 * and state-based action buttons.
 */
@Component
class ReceiptsView(private val baseTable: BaseTable) {

    companion object {
        private val DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        private val DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
        private val CURRENCY_FORMATTER = NumberFormat.getCurrencyInstance(Locale.US)
        private const val DESCRIPTION_MAX_LENGTH = 50
    }

    /**
     * Prepares the receipts view data for template rendering.
     * 
     * @param receiptsData the list of receipt entities to display
     * @param paginationConfig optional pagination configuration
     * @param searchEnabled whether to show search functionality
     * @param sortKey optional current sort key
     * @param sortDirection optional current sort direction
     * @return TableViewData object for template rendering
     */
    fun prepareTableViewData(
        receiptsData: List<ReceiptEntity>,
        paginationConfig: PaginationConfig? = null,
        searchEnabled: Boolean = false,
        sortKey: String? = null,
        sortDirection: SortDirection = SortDirection.ASC
    ): TableViewData {
        val columns = getTableColumns()
        val rowData = receiptsData.map { convertEntityToRowData(it) }
        
        return baseTable.prepareTableViewData(
            columns = columns,
            data = rowData,
            tableId = "receipts",
            paginationConfig = paginationConfig,
            searchEnabled = searchEnabled,
            sortKey = sortKey,
            sortDirection = sortDirection
        )
    }

    /**
     * Gets the table column definitions for the receipts view.
     * 
     * @return list of table columns for receipts display
     */
    fun getTableColumns(): List<TableColumn> {
        return listOf(
            TableColumn(key = "paymentDate", label = "Payment Date", sortable = true),
            TableColumn(key = "merchantName", label = "Merchant Name", sortable = false),
            TableColumn(key = "amount", label = "Amount", sortable = true),
            TableColumn(key = "paymentType", label = "Payment Type", sortable = false),
            TableColumn(key = "description", label = "Description", sortable = false),
            TableColumn(key = "createdDate", label = "Created Date", sortable = true),
            TableColumn(key = "actions", label = "Actions", sortable = false)
        )
    }

    /**
     * Converts a ReceiptEntity to a row data map for table display.
     * 
     * @param entity the receipt entity to convert
     * @return map containing row data for table display
     */
    fun convertEntityToRowData(entity: ReceiptEntity): Map<String, String> {
        return mapOf(
            "id" to entity.id,
            "paymentDate" to entity.paymentDate.format(DATE_FORMATTER),
            "merchantName" to formatMerchantNameWithSource(entity.merchantName, entity.inboxEntityId),
            "amount" to formatAmount(entity.amount),
            "paymentType" to formatPaymentType(entity.paymentTypeId),
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
     * Formats the payment type for display based on payment type ID.
     * 
     * @param paymentTypeId the payment type ID
     * @return formatted payment type name
     */
    fun formatPaymentType(paymentTypeId: String): String {
        return when (paymentTypeId) {
            "credit_card_001", "credit_card" -> "Credit Card"
            "debit_card_001", "debit_card" -> "Debit Card"
            "cash_001", "cash" -> "Cash"
            "check_001", "check" -> "Check"
            "bank_transfer_001", "bank_transfer" -> "Bank Transfer"
            "mobile_payment_001", "mobile_payment" -> "Mobile Payment"
            "gift_card_001", "gift_card" -> "Gift Card"
            else -> paymentTypeId.replace("_", " ").split(" ").joinToString(" ") { 
                it.lowercase().replaceFirstChar { char -> char.uppercase() }
            }
        }
    }

    /**
     * Formats the merchant name for display, handling null values.
     * 
     * @param merchantName the merchant name to format
     * @return formatted merchant name or placeholder for null values
     */
    fun formatMerchantName(merchantName: String?): String {
        return if (merchantName.isNullOrBlank()) {
            "-"
        } else {
            merchantName
        }
    }

    /**
     * Formats the merchant name for display with creation source indication.
     * 
     * @param merchantName the merchant name
     * @param inboxEntityId optional inbox entity ID for creation source indication
     * @return formatted merchant name with source indication
     */
    private fun formatMerchantNameWithSource(merchantName: String?, inboxEntityId: String?): String {
        val formattedName = formatMerchantName(merchantName)
        val sourceIndicator = if (inboxEntityId != null) " (from inbox)" else " (manual)"
        return formattedName + sourceIndicator
    }

    /**
     * Formats the description for display, truncating if necessary.
     * 
     * @param description the description to format
     * @return formatted description with truncation if needed
     */
    fun formatDescription(description: String?): String {
        return if (description.isNullOrBlank()) {
            "-"
        } else if (description.length <= DESCRIPTION_MAX_LENGTH) {
            description
        } else {
            description.substring(0, DESCRIPTION_MAX_LENGTH) + "..."
        }
    }

    /**
     * Formats action buttons based on the receipt state.
     * 
     * @param state the current receipt state
     * @param entityId the entity ID for action buttons
     * @return formatted action description
     */
    fun formatActions(state: ReceiptState, entityId: String): String {
        return when (state) {
            ReceiptState.CREATED -> "Edit | Remove"
            ReceiptState.REMOVED -> "Removed"
        }
    }

    /**
     * Applies sorting to receipts data using the BaseTable sorting functionality.
     * 
     * @param receiptsData the receipts data to sort
     * @param sortKey the key to sort by
     * @param sortDirection the direction to sort
     * @return sorted receipts data converted to row data
     */
    fun applySorting(
        receiptsData: List<ReceiptEntity>,
        sortKey: String,
        sortDirection: SortDirection
    ): List<Map<String, String>> {
        val rowData = receiptsData.map { convertEntityToRowData(it) }
        return baseTable.applySorting(rowData, sortKey, sortDirection)
    }

    /**
     * Applies pagination to receipts data using the BaseTable pagination functionality.
     * 
     * @param receiptsData the receipts data to paginate
     * @param pageSize the number of items per page
     * @param currentPage the current page number (1-based)
     * @return paginated receipts data converted to row data
     */
    fun applyPagination(
        receiptsData: List<ReceiptEntity>,
        pageSize: Int,
        currentPage: Int
    ): List<Map<String, String>> {
        val rowData = receiptsData.map { convertEntityToRowData(it) }
        return baseTable.applyPagination(rowData, pageSize, currentPage)
    }

    /**
     * Applies search filtering to receipts data using the BaseTable search functionality.
     * 
     * @param receiptsData the receipts data to filter
     * @param searchTerm the search term to filter by
     * @return filtered receipts data converted to row data
     */
    fun applySearch(
        receiptsData: List<ReceiptEntity>,
        searchTerm: String
    ): List<Map<String, String>> {
        val rowData = receiptsData.map { convertEntityToRowData(it) }
        return baseTable.applySearch(rowData, searchTerm)
    }
}