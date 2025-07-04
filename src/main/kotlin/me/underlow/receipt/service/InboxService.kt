package me.underlow.receipt.service

import me.underlow.receipt.model.*
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

/**
 * Comprehensive service for handling status-based inbox operations and transitions
 * Aggregates IncomingFile, Bill, and Receipt operations for tabbed inbox views
 */
@Service
class InboxService(
    private val incomingFileService: IncomingFileService,
    private val billService: BillService,
    private val receiptService: ReceiptService,
    private val entityConversionService: EntityConversionService
) {
    
    private val logger = LoggerFactory.getLogger(InboxService::class.java)

    /**
     * Container for inbox item data
     */
    data class InboxItem(
        val id: Long,
        val type: InboxItemType,
        val filename: String,
        val filePath: String,
        val uploadDate: java.time.LocalDateTime,
        val status: ItemStatus,
        val extractedAmount: Double? = null,
        val extractedDate: java.time.LocalDate? = null,
        val extractedProvider: String? = null,
        val ocrRawJson: String? = null,
        val userId: Long
    )

    enum class InboxItemType {
        INCOMING_FILE,
        BILL,
        RECEIPT
    }

    /**
     * Gets all items for the New tab (IncomingFile, Bill, Receipt with status=NEW)
     * This consolidates pending + processing + draft → NEW as per requirements
     */
    fun getNewItems(userEmail: String): List<InboxItem> {
        logger.debug("Getting new items for user: {}", userEmail)
        
        val newItems = mutableListOf<InboxItem>()
        
        // Get IncomingFiles with NEW status
        val incomingFiles = incomingFileService.findByUserEmailAndStatus(userEmail, ItemStatus.NEW)
        newItems.addAll(incomingFiles.map { file ->
            InboxItem(
                id = file.id!!,
                type = InboxItemType.INCOMING_FILE,
                filename = file.filename,
                filePath = file.filePath,
                uploadDate = file.uploadDate,
                status = file.status,
                extractedAmount = file.extractedAmount,
                extractedDate = file.extractedDate,
                extractedProvider = file.extractedProvider,
                ocrRawJson = file.ocrRawJson,
                userId = file.userId
            )
        })
        
        // Get Bills with NEW status
        val bills = billService.findByUserEmail(userEmail, ItemStatus.NEW)
        newItems.addAll(bills.map { bill ->
            InboxItem(
                id = bill.id!!,
                type = InboxItemType.BILL,
                filename = bill.filename,
                filePath = bill.filePath,
                uploadDate = bill.uploadDate,
                status = bill.status,
                extractedAmount = bill.extractedAmount,
                extractedDate = bill.extractedDate,
                extractedProvider = bill.extractedProvider,
                ocrRawJson = bill.ocrRawJson,
                userId = bill.userId
            )
        })
        
        // Get Receipts with NEW status
        val receipts = receiptService.findByUserEmail(userEmail, ItemStatus.NEW)
        newItems.addAll(receipts.mapNotNull { receipt ->
            // Only include receipts that have file data
            if (receipt.filename != null && receipt.filePath != null && receipt.uploadDate != null) {
                InboxItem(
                    id = receipt.id!!,
                    type = InboxItemType.RECEIPT,
                    filename = receipt.filename!!,
                    filePath = receipt.filePath!!,
                    uploadDate = receipt.uploadDate!!,
                    status = receipt.status,
                    extractedAmount = receipt.extractedAmount,
                    extractedDate = receipt.extractedDate,
                    extractedProvider = receipt.extractedProvider,
                    ocrRawJson = receipt.ocrRawJson,
                    userId = receipt.userId
                )
            } else null
        })
        
        // Sort by upload date (newest first)
        val sortedItems = newItems.sortedByDescending { it.uploadDate }
        logger.debug("Found {} new items for user {}", sortedItems.size, userEmail)
        return sortedItems
    }

    /**
     * Gets all items for the Approved tab (Bills & Receipts with status=APPROVED)
     */
    fun getApprovedItems(userEmail: String): List<InboxItem> {
        logger.debug("Getting approved items for user: {}", userEmail)
        
        val approvedItems = mutableListOf<InboxItem>()
        
        // Get Bills with APPROVED status
        val bills = billService.findByUserEmail(userEmail, ItemStatus.APPROVED)
        approvedItems.addAll(bills.map { bill ->
            InboxItem(
                id = bill.id!!,
                type = InboxItemType.BILL,
                filename = bill.filename,
                filePath = bill.filePath,
                uploadDate = bill.uploadDate,
                status = bill.status,
                extractedAmount = bill.extractedAmount,
                extractedDate = bill.extractedDate,
                extractedProvider = bill.extractedProvider,
                ocrRawJson = bill.ocrRawJson,
                userId = bill.userId
            )
        })
        
        // Get Receipts with APPROVED status
        val receipts = receiptService.findByUserEmail(userEmail, ItemStatus.APPROVED)
        approvedItems.addAll(receipts.mapNotNull { receipt ->
            // Only include receipts that have file data
            if (receipt.filename != null && receipt.filePath != null && receipt.uploadDate != null) {
                InboxItem(
                    id = receipt.id!!,
                    type = InboxItemType.RECEIPT,
                    filename = receipt.filename!!,
                    filePath = receipt.filePath!!,
                    uploadDate = receipt.uploadDate!!,
                    status = receipt.status,
                    extractedAmount = receipt.extractedAmount,
                    extractedDate = receipt.extractedDate,
                    extractedProvider = receipt.extractedProvider,
                    ocrRawJson = receipt.ocrRawJson,
                    userId = receipt.userId
                )
            } else null
        })
        
        // Sort by upload date (newest first)
        val sortedItems = approvedItems.sortedByDescending { it.uploadDate }
        logger.debug("Found {} approved items for user {}", sortedItems.size, userEmail)
        return sortedItems
    }

    /**
     * Gets all items for the Rejected tab (Bills & Receipts with status=REJECTED)
     */
    fun getRejectedItems(userEmail: String): List<InboxItem> {
        logger.debug("Getting rejected items for user: {}", userEmail)
        
        val rejectedItems = mutableListOf<InboxItem>()
        
        // Get Bills with REJECTED status
        val bills = billService.findByUserEmail(userEmail, ItemStatus.REJECTED)
        rejectedItems.addAll(bills.map { bill ->
            InboxItem(
                id = bill.id!!,
                type = InboxItemType.BILL,
                filename = bill.filename,
                filePath = bill.filePath,
                uploadDate = bill.uploadDate,
                status = bill.status,
                extractedAmount = bill.extractedAmount,
                extractedDate = bill.extractedDate,
                extractedProvider = bill.extractedProvider,
                ocrRawJson = bill.ocrRawJson,
                userId = bill.userId
            )
        })
        
        // Get Receipts with REJECTED status
        val receipts = receiptService.findByUserEmail(userEmail, ItemStatus.REJECTED)
        rejectedItems.addAll(receipts.mapNotNull { receipt ->
            // Only include receipts that have file data
            if (receipt.filename != null && receipt.filePath != null && receipt.uploadDate != null) {
                InboxItem(
                    id = receipt.id!!,
                    type = InboxItemType.RECEIPT,
                    filename = receipt.filename!!,
                    filePath = receipt.filePath!!,
                    uploadDate = receipt.uploadDate!!,
                    status = receipt.status,
                    extractedAmount = receipt.extractedAmount,
                    extractedDate = receipt.extractedDate,
                    extractedProvider = receipt.extractedProvider,
                    ocrRawJson = receipt.ocrRawJson,
                    userId = receipt.userId
                )
            } else null
        })
        
        // Sort by upload date (newest first)
        val sortedItems = rejectedItems.sortedByDescending { it.uploadDate }
        logger.debug("Found {} rejected items for user {}", sortedItems.size, userEmail)
        return sortedItems
    }

    /**
     * Gets items by status with optional type filtering for approved/rejected tabs
     */
    fun getItemsByStatus(
        userEmail: String, 
        status: ItemStatus, 
        itemType: InboxItemType? = null
    ): List<InboxItem> {
        logger.debug("Getting items for user: {} with status: {} and type: {}", userEmail, status, itemType)
        
        return when (status) {
            ItemStatus.NEW -> getNewItems(userEmail)
            ItemStatus.APPROVED -> {
                val items = getApprovedItems(userEmail)
                if (itemType != null) items.filter { it.type == itemType } else items
            }
            ItemStatus.REJECTED -> {
                val items = getRejectedItems(userEmail)
                if (itemType != null) items.filter { it.type == itemType } else items
            }
            ItemStatus.PROCESSING -> {
                // For processing items, only check IncomingFiles
                val incomingFiles = incomingFileService.findByUserEmailAndStatus(userEmail, ItemStatus.PROCESSING)
                incomingFiles.map { file ->
                    InboxItem(
                        id = file.id!!,
                        type = InboxItemType.INCOMING_FILE,
                        filename = file.filename,
                        filePath = file.filePath,
                        uploadDate = file.uploadDate,
                        status = file.status,
                        extractedAmount = file.extractedAmount,
                        extractedDate = file.extractedDate,
                        extractedProvider = file.extractedProvider,
                        ocrRawJson = file.ocrRawJson,
                        userId = file.userId
                    )
                }.sortedByDescending { it.uploadDate }
            }
        }
    }

    /**
     * Approves an item (changes status to APPROVED)
     */
    fun approveItem(itemId: Long, itemType: InboxItemType, userEmail: String): Boolean {
        logger.info("Approving {} item {} for user {}", itemType, itemId, userEmail)
        
        return when (itemType) {
            InboxItemType.INCOMING_FILE -> incomingFileService.updateStatus(itemId, userEmail, ItemStatus.APPROVED)
            InboxItemType.BILL -> billService.updateStatus(itemId, userEmail, ItemStatus.APPROVED)
            InboxItemType.RECEIPT -> receiptService.updateStatus(itemId, userEmail, ItemStatus.APPROVED)
        }
    }

    /**
     * Rejects an item (changes status to REJECTED)
     */
    fun rejectItem(itemId: Long, itemType: InboxItemType, userEmail: String): Boolean {
        logger.info("Rejecting {} item {} for user {}", itemType, itemId, userEmail)
        
        return when (itemType) {
            InboxItemType.INCOMING_FILE -> incomingFileService.updateStatus(itemId, userEmail, ItemStatus.REJECTED)
            InboxItemType.BILL -> billService.updateStatus(itemId, userEmail, ItemStatus.REJECTED)
            InboxItemType.RECEIPT -> receiptService.updateStatus(itemId, userEmail, ItemStatus.REJECTED)
        }
    }

    /**
     * Converts IncomingFile to Bill
     */
    fun convertIncomingFileToBill(incomingFileId: Long, userEmail: String): Bill? {
        logger.info("Converting IncomingFile {} to Bill for user {}", incomingFileId, userEmail)
        return entityConversionService.convertIncomingFileToBill(incomingFileId, userEmail)
    }

    /**
     * Converts IncomingFile to Receipt
     */
    fun convertIncomingFileToReceipt(incomingFileId: Long, userEmail: String): Receipt? {
        logger.info("Converting IncomingFile {} to Receipt for user {}", incomingFileId, userEmail)
        return entityConversionService.convertIncomingFileToReceipt(incomingFileId, userEmail)
    }

    /**
     * Reverts Bill back to IncomingFile
     */
    fun revertBillToIncomingFile(billId: Long, userEmail: String): IncomingFile? {
        logger.info("Reverting Bill {} to IncomingFile for user {}", billId, userEmail)
        return entityConversionService.revertBillToIncomingFile(billId, userEmail)
    }

    /**
     * Reverts Receipt back to IncomingFile
     */
    fun revertReceiptToIncomingFile(receiptId: Long, userEmail: String): IncomingFile? {
        logger.info("Reverting Receipt {} to IncomingFile for user {}", receiptId, userEmail)
        return entityConversionService.revertReceiptToIncomingFile(receiptId, userEmail)
    }

    /**
     * Gets comprehensive statistics for all inbox items by status
     */
    fun getInboxStatistics(userEmail: String): Map<String, Map<ItemStatus, Int>> {
        logger.debug("Getting inbox statistics for user: {}", userEmail)
        
        val incomingFileStats = incomingFileService.getFileStatistics(userEmail)
        val billStats = billService.getBillStatistics(userEmail)
        val receiptStats = receiptService.getReceiptStatisticsByStatus(userEmail)
        
        val statistics = mapOf(
            "incomingFiles" to incomingFileStats,
            "bills" to billStats,
            "receipts" to receiptStats
        )
        
        logger.debug("Generated inbox statistics for user {}: {}", userEmail, statistics)
        return statistics
    }

    /**
     * Gets count of items per tab for quick overview
     */
    fun getTabCounts(userEmail: String): Map<String, Int> {
        logger.debug("Getting tab counts for user: {}", userEmail)
        
        val newCount = getNewItems(userEmail).size
        val approvedCount = getApprovedItems(userEmail).size
        val rejectedCount = getRejectedItems(userEmail).size
        
        val counts = mapOf(
            "new" to newCount,
            "approved" to approvedCount,
            "rejected" to rejectedCount
        )
        
        logger.debug("Generated tab counts for user {}: {}", userEmail, counts)
        return counts
    }
}