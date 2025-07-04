package me.underlow.receipt.dto

import me.underlow.receipt.service.InboxService
import java.time.LocalDateTime

/**
 * Data transfer object for inbox tab interface
 * Represents unified item data across IncomingFile, Bill, and Receipt entities
 */
data class InboxTabDto(
    val id: Long,
    val type: String, // "incoming_file", "bill", "receipt"
    val filename: String,
    val uploadDate: LocalDateTime,
    val status: String,
    val statusDisplayName: String,
    val thumbnailUrl: String,
    val fileUrl: String,
    val hasOcrResults: Boolean,
    val extractedAmount: Double?,
    val extractedDate: String?,
    val extractedProvider: String?,
    val ocrErrorMessage: String?,
    val canApprove: Boolean,
    val canReject: Boolean,
    val canDelete: Boolean,
    val detailUrl: String,
    val actions: List<String> // Available actions for this item
) {
    companion object {
        /**
         * Creates InboxTabDto from InboxService.InboxItem
         */
        fun fromInboxItem(item: InboxService.InboxItem): InboxTabDto {
            val typeString = when (item.type) {
                InboxService.InboxItemType.INCOMING_FILE -> "incoming_file"
                InboxService.InboxItemType.BILL -> "bill"
                InboxService.InboxItemType.RECEIPT -> "receipt"
            }
            
            val statusDisplayName = when (item.status) {
                me.underlow.receipt.model.ItemStatus.NEW -> "New"
                me.underlow.receipt.model.ItemStatus.PROCESSING -> "Processing"
                me.underlow.receipt.model.ItemStatus.APPROVED -> "Approved"
                me.underlow.receipt.model.ItemStatus.REJECTED -> "Rejected"
            }
            
            val hasOcrResults = item.extractedAmount != null || 
                              item.extractedProvider != null || 
                              item.extractedDate != null
            
            // Generate appropriate URLs based on item type
            val (thumbnailUrl, fileUrl, detailUrl) = when (item.type) {
                InboxService.InboxItemType.INCOMING_FILE -> Triple(
                    "/api/files/${item.id}/thumbnail",
                    "/api/files/${item.id}",
                    "/inbox/files/${item.id}"
                )
                InboxService.InboxItemType.BILL -> Triple(
                    "/api/bills/${item.id}/thumbnail",
                    "/api/bills/${item.id}/image",
                    "/bills/${item.id}"
                )
                InboxService.InboxItemType.RECEIPT -> Triple(
                    "/api/receipts/${item.id}/thumbnail",
                    "/api/receipts/${item.id}/image",
                    "/receipts/${item.id}"
                )
            }
            
            // Determine available actions based on status and type
            val actions = mutableListOf<String>()
            if (item.status == me.underlow.receipt.model.ItemStatus.NEW) {
                actions.add("approve")
                actions.add("reject")
                if (item.type == InboxService.InboxItemType.INCOMING_FILE) {
                    actions.add("convert-to-bill")
                    actions.add("convert-to-receipt")
                }
            }
            actions.add("delete")
            actions.add("view")
            actions.add("detail")
            
            return InboxTabDto(
                id = item.id,
                type = typeString,
                filename = item.filename,
                uploadDate = item.uploadDate,
                status = item.status.name.lowercase(),
                statusDisplayName = statusDisplayName,
                thumbnailUrl = thumbnailUrl,
                fileUrl = fileUrl,
                hasOcrResults = hasOcrResults,
                extractedAmount = item.extractedAmount,
                extractedDate = item.extractedDate?.toString(),
                extractedProvider = item.extractedProvider,
                ocrErrorMessage = null, // TODO: Add OCR error handling
                canApprove = item.status == me.underlow.receipt.model.ItemStatus.NEW,
                canReject = item.status == me.underlow.receipt.model.ItemStatus.NEW,
                canDelete = true,
                detailUrl = detailUrl,
                actions = actions
            )
        }
    }
}

/**
 * Response object for inbox tab data
 */
data class InboxTabResponse(
    val tab: String, // "new", "approved", "rejected"
    val items: List<InboxTabDto>,
    val totalItems: Int,
    val itemTypeFilter: String?, // "bill", "receipt", null for all
    val availableTypes: List<String> // Available item types for filtering
)

/**
 * Response object for tab counts
 */
data class InboxTabCountsResponse(
    val new: Int,
    val approved: Int,
    val rejected: Int
)