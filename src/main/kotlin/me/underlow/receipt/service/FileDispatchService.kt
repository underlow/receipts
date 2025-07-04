package me.underlow.receipt.service

import me.underlow.receipt.model.IncomingFile
import me.underlow.receipt.model.Bill
import me.underlow.receipt.model.BillStatus
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
        logger.info("Dispatching IncomingFile: ${incomingFile.filename} (ID: ${incomingFile.id})")
        
        // Validate that file has been processed through OCR
        if (!isFileReadyForDispatch(incomingFile)) {
            logger.warn("IncomingFile ${incomingFile.filename} is not ready for dispatch")
            return null
        }
        
        return try {
            // Convert to Bill entity
            val bill = convertIncomingFileToBill(incomingFile)
            val savedBill = billRepository.save(bill)
            
            logger.info("Successfully dispatched IncomingFile ${incomingFile.filename} to Bill with ID: ${savedBill.id}")
            savedBill
            
        } catch (e: Exception) {
            logger.error("Error dispatching IncomingFile ${incomingFile.filename}", e)
            null
        }
    }
    
    /**
     * Dispatches all approved IncomingFiles that haven't been dispatched yet.
     * Used for batch processing of files that have completed OCR processing.
     */
    fun dispatchAllReadyFiles(): List<Bill> {
        logger.info("Dispatching all ready IncomingFiles to Bills")
        
        val approvedFiles = incomingFileRepository.findByStatus(BillStatus.APPROVED)
        val readyFiles = approvedFiles.filter { isFileReadyForDispatch(it) }
        
        logger.info("Found ${readyFiles.size} IncomingFiles ready for dispatch")
        
        return readyFiles.mapNotNull { dispatchIncomingFile(it) }
    }
    
    /**
     * Checks if an IncomingFile is ready for dispatch.
     * File must be APPROVED and have OCR results.
     */
    fun isFileReadyForDispatch(incomingFile: IncomingFile): Boolean {
        return incomingFile.status == BillStatus.APPROVED &&
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
            status = BillStatus.APPROVED, // Bills start as approved since they come from approved IncomingFiles
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
        val incomingFile = incomingFileRepository.findById(incomingFileId)
        
        return if (incomingFile != null) {
            val bill = dispatchIncomingFile(incomingFile)
            if (bill != null) {
                Pair(incomingFile, bill)
            } else {
                null
            }
        } else {
            logger.warn("IncomingFile not found: $incomingFileId")
            null
        }
    }
    
    /**
     * Gets statistics about files ready for dispatch.
     */
    fun getDispatchStatistics(): DispatchStatistics {
        val allApprovedFiles = incomingFileRepository.findByStatus(BillStatus.APPROVED)
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