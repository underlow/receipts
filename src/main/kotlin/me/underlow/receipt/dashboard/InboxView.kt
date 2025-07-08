package me.underlow.receipt.dashboard

import me.underlow.receipt.model.InboxEntity
import me.underlow.receipt.model.InboxState
import me.underlow.receipt.model.EntityType
import org.springframework.stereotype.Component
import java.time.format.DateTimeFormatter

/**
 * InboxView component displays inbox items in a table format with appropriate columns and actions.
 * This component uses the BaseTable component to provide sorting, pagination, and search functionality
 * while adding inbox-specific features like OCR status indicators and state-based action buttons.
 */
@Component
class InboxView(private val baseTable: BaseTable) {

    companion object {
        private val DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
    }

    /**
     * Renders the inbox view with the provided inbox data.
     * 
     * @param inboxData the list of inbox entities to display
     * @param paginationConfig optional pagination configuration
     * @param searchEnabled whether to show search functionality
     * @param sortKey optional current sort key
     * @param sortDirection optional current sort direction
     * @return HTML string containing the rendered inbox table
     */
    fun render(
        inboxData: List<InboxEntity>,
        paginationConfig: PaginationConfig? = null,
        searchEnabled: Boolean = false,
        sortKey: String? = null,
        sortDirection: SortDirection = SortDirection.ASC
    ): String {
        val columns = getTableColumns()
        val rowData = inboxData.map { convertEntityToRowData(it) }
        
        return baseTable.render(
            columns = columns,
            data = rowData,
            tableId = "inbox",
            paginationConfig = paginationConfig,
            searchEnabled = searchEnabled,
            sortKey = sortKey,
            sortDirection = sortDirection
        )
    }

    /**
     * Gets the table column definitions for the inbox view.
     * 
     * @return list of table columns for inbox display
     */
    fun getTableColumns(): List<TableColumn> {
        return listOf(
            TableColumn(key = "uploadDate", label = "Upload Date", sortable = true),
            TableColumn(key = "image", label = "Image", sortable = false),
            TableColumn(key = "status", label = "OCR Status", sortable = true),
            TableColumn(key = "actions", label = "Actions", sortable = false)
        )
    }

    /**
     * Converts an InboxEntity to a row data map for table display.
     * 
     * @param entity the inbox entity to convert
     * @return map containing row data for table display
     */
    fun convertEntityToRowData(entity: InboxEntity): Map<String, String> {
        return mapOf(
            "uploadDate" to entity.uploadDate.format(DATE_FORMATTER),
            "image" to formatImagePreview(entity.uploadedImage),
            "status" to formatOCRStatus(entity.state),
            "actions" to formatActions(entity.state, entity.id)
        )
    }

    /**
     * Formats the OCR status for display based on the inbox state.
     * 
     * @param state the current inbox state
     * @return HTML string with appropriate status indicator
     */
    fun formatOCRStatus(state: InboxState): String {
        return when (state) {
            InboxState.CREATED -> """
                <span class="badge bg-warning text-dark">
                    <i class="fas fa-clock me-1"></i>
                    Pending
                </span>
            """.trimIndent()
            
            InboxState.PROCESSED -> """
                <span class="badge bg-success">
                    <i class="fas fa-check me-1"></i>
                    Processed
                </span>
            """.trimIndent()
            
            InboxState.FAILED -> """
                <span class="badge bg-danger">
                    <i class="fas fa-times me-1"></i>
                    Failed
                </span>
            """.trimIndent()
            
            InboxState.APPROVED -> """
                <span class="badge bg-primary">
                    <i class="fas fa-thumbs-up me-1"></i>
                    Approved
                </span>
            """.trimIndent()
        }
    }

    /**
     * Formats action buttons based on the inbox state.
     * 
     * @param state the current inbox state
     * @param entityId the entity ID for action buttons
     * @return HTML string with appropriate action buttons
     */
    fun formatActions(state: InboxState, entityId: String): String {
        return when (state) {
            InboxState.CREATED -> """
                <span class="text-muted">
                    <i class="fas fa-spinner fa-spin me-1"></i>
                    Processing...
                </span>
            """.trimIndent()
            
            InboxState.PROCESSED -> """
                <div class="btn-group" role="group">
                    <button type="button" class="btn btn-sm btn-success" 
                            onclick="approveItem('$entityId', 'BILL')" 
                            title="Approve as Bill">
                        <i class="fas fa-file-invoice me-1"></i>
                        Approve as Bill
                    </button>
                    <button type="button" class="btn btn-sm btn-info" 
                            onclick="approveItem('$entityId', 'RECEIPT')" 
                            title="Approve as Receipt">
                        <i class="fas fa-receipt me-1"></i>
                        Approve as Receipt
                    </button>
                </div>
            """.trimIndent()
            
            InboxState.FAILED -> """
                <button type="button" class="btn btn-sm btn-warning" 
                        onclick="retryItem('$entityId')" 
                        title="Retry OCR Processing">
                    <i class="fas fa-redo me-1"></i>
                    Retry
                </button>
            """.trimIndent()
            
            InboxState.APPROVED -> """
                <span class="text-success">
                    <i class="fas fa-check-circle me-1"></i>
                    Complete
                </span>
            """.trimIndent()
        }
    }

    /**
     * Formats image preview for display in the table.
     * 
     * @param imagePath the path to the image
     * @return HTML string with image preview
     */
    private fun formatImagePreview(imagePath: String): String {
        return """
            <div class="d-flex align-items-center">
                <img src="/api/inbox/image/$imagePath" 
                     class="img-thumbnail me-2" 
                     style="width: 40px; height: 40px; object-fit: cover;" 
                     alt="Document thumbnail"
                     onclick="showImageModal('$imagePath')"
                     style="cursor: pointer;">
                <span class="text-muted small">$imagePath</span>
            </div>
        """.trimIndent()
    }

    /**
     * Applies sorting to inbox data using the BaseTable sorting functionality.
     * 
     * @param inboxData the inbox data to sort
     * @param sortKey the key to sort by
     * @param sortDirection the direction to sort
     * @return sorted inbox data converted to row data
     */
    fun applySorting(
        inboxData: List<InboxEntity>,
        sortKey: String,
        sortDirection: SortDirection
    ): List<Map<String, String>> {
        val rowData = inboxData.map { convertEntityToRowData(it) }
        return baseTable.applySorting(rowData, sortKey, sortDirection)
    }

    /**
     * Applies pagination to inbox data using the BaseTable pagination functionality.
     * 
     * @param inboxData the inbox data to paginate
     * @param pageSize the number of items per page
     * @param currentPage the current page number (1-based)
     * @return paginated inbox data converted to row data
     */
    fun applyPagination(
        inboxData: List<InboxEntity>,
        pageSize: Int,
        currentPage: Int
    ): List<Map<String, String>> {
        val rowData = inboxData.map { convertEntityToRowData(it) }
        return baseTable.applyPagination(rowData, pageSize, currentPage)
    }

    /**
     * Applies search filtering to inbox data using the BaseTable search functionality.
     * 
     * @param inboxData the inbox data to filter
     * @param searchTerm the search term to filter by
     * @return filtered inbox data converted to row data
     */
    fun applySearch(
        inboxData: List<InboxEntity>,
        searchTerm: String
    ): List<Map<String, String>> {
        val rowData = inboxData.map { convertEntityToRowData(it) }
        return baseTable.applySearch(rowData, searchTerm)
    }
}