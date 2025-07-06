package me.underlow.receipt.service

import me.underlow.receipt.model.IncomingFile
import me.underlow.receipt.model.Bill
import me.underlow.receipt.model.ItemStatus
import me.underlow.receipt.repository.IncomingFileRepository
import me.underlow.receipt.repository.BillRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.LocalDateTime

/**
 * Service responsible for dispatching processed IncomingFiles to appropriate domain entities.
 * Converts IncomingFiles with successful OCR results to Bills or Receipts based on business logic.
 */
@Service
class FileDispatchService(
    private val incomingFileRepository: IncomingFileRepository,
    private val billRepository: BillRepository
) {
    
    private val logger = LoggerFactory.getLogger(FileDispatchService::class.java)
    
    /**
     * Dispatches an IncomingFile to the appropriate domain entity based on OCR results.
     * Currently converts to Bill entity - in future could implement Receipt logic.
     */
    fun dispatchIncomingFile(incomingFile: IncomingFile): Bill? {
        logger.info("Dispatching IncomingFile: ${incomingFile.filename} (ID: ${incomingFile.id}) for user: ${incomingFile.userId}")
        
        // Validate that file has been processed through OCR
        if (!isFileReadyForDispatch(incomingFile)) {
            logger.warn("IncomingFile ${incomingFile.filename} (ID: ${incomingFile.id}) is not ready for dispatch - status: ${incomingFile.status}, OCR: ${incomingFile.ocrRawJson != null}")
            return null
        }
        
        return try {
            // Convert to Bill entity
            val bill = convertIncomingFileToBill(incomingFile)
            val savedBill = billRepository.save(bill)
            
            logger.info("Successfully dispatched IncomingFile ${incomingFile.filename} (ID: ${incomingFile.id}) to Bill with ID: ${savedBill.id}, extracted amount: ${savedBill.extractedAmount}, provider: ${savedBill.extractedProvider}")
            savedBill
            
        } catch (e: Exception) {
            logger.error("Error dispatching IncomingFile ${incomingFile.filename} (ID: ${incomingFile.id})", e)
            null
        }
    }
    
    /**
     * Dispatches all approved IncomingFiles that haven't been dispatched yet.
     * Used for batch processing of files that have completed OCR processing.
     */
    fun dispatchAllReadyFiles(): List<Bill> {
        logger.info("Starting batch dispatch of all ready IncomingFiles to Bills")
        
        val approvedFiles = incomingFileRepository.findByStatus(ItemStatus.APPROVED)
        val readyFiles = approvedFiles.filter { isFileReadyForDispatch(it) }
        
        logger.info("Found ${readyFiles.size} IncomingFiles ready for dispatch out of ${approvedFiles.size} approved files")
        
        val dispatchedBills = readyFiles.mapNotNull { dispatchIncomingFile(it) }
        
        logger.info("Batch dispatch completed - successfully dispatched ${dispatchedBills.size} files to Bills")
        return dispatchedBills
    }
    
    /**
     * Checks if an IncomingFile is ready for dispatch.
     * File must be APPROVED and have OCR results.
     */
    fun isFileReadyForDispatch(incomingFile: IncomingFile): Boolean {
        return incomingFile.status == ItemStatus.APPROVED &&
               incomingFile.ocrRawJson != null &&
               incomingFile.ocrProcessedAt != null
    }
    
    /**
     * Converts an IncomingFile to a Bill entity.
     * Maps OCR results and metadata to Bill structure.
     */
    private fun convertIncomingFileToBill(incomingFile: IncomingFile): Bill {
        return Bill(
            filename = incomingFile.filename,
            filePath = incomingFile.filePath,
            uploadDate = incomingFile.uploadDate,
            status = ItemStatus.APPROVED, // Bills start as approved since they come from approved IncomingFiles
            ocrRawJson = incomingFile.ocrRawJson,
            extractedAmount = incomingFile.extractedAmount,
            extractedDate = incomingFile.extractedDate,
            extractedProvider = incomingFile.extractedProvider,
            userId = incomingFile.userId
        )
    }
    
    /**
     * Converts a specific IncomingFile to Bill and returns both entities.
     * Useful for UI operations where you need to show the result.
     */
    fun convertToBillAndReturn(incomingFileId: Long): Pair<IncomingFile, Bill>? {
        logger.info("Converting IncomingFile {} to Bill and returning both entities", incomingFileId)
        val incomingFile = incomingFileRepository.findById(incomingFileId)
        
        return if (incomingFile != null) {
            val bill = dispatchIncomingFile(incomingFile)
            if (bill != null) {
                logger.info("Successfully converted IncomingFile {} to Bill {} - returning both entities", incomingFileId, bill.id)
                Pair(incomingFile, bill)
            } else {
                logger.warn("Failed to convert IncomingFile {} to Bill - dispatch failed", incomingFileId)
                null
            }
        } else {
            logger.warn("IncomingFile not found: {}", incomingFileId)
            null
        }
    }
    
    /**
     * Gets statistics about files ready for dispatch.
     */
    fun getDispatchStatistics(): DispatchStatistics {
        val allApprovedFiles = incomingFileRepository.findByStatus(ItemStatus.APPROVED)
        val readyFiles = allApprovedFiles.filter { isFileReadyForDispatch(it) }
        
        return DispatchStatistics(
            totalApprovedFiles = allApprovedFiles.size,
            readyForDispatch = readyFiles.size,
            needsManualReview = allApprovedFiles.size - readyFiles.size
        )
    }
}

/**
 * Statistics about files ready for dispatch.
 */
data class DispatchStatistics(
    val totalApprovedFiles: Int,
    val readyForDispatch: Int,
    val needsManualReview: Int
)