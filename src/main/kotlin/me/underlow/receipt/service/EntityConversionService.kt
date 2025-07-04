package me.underlow.receipt.service

import me.underlow.receipt.model.*
import me.underlow.receipt.repository.*
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.LocalDateTime

/**
 * Service for converting between IncomingFile, Bill, and Receipt entities
 */
@Service
class EntityConversionService(
    private val incomingFileRepository: IncomingFileRepository,
    private val billRepository: BillRepository,
    private val receiptRepository: ReceiptRepository,
    private val userRepository: UserRepository,
    private val ocrAttemptService: OcrAttemptService
) {
    
    private val logger = LoggerFactory.getLogger(EntityConversionService::class.java)

    /**
     * Converts an IncomingFile to a Bill
     */
    fun convertIncomingFileToBill(incomingFileId: Long, userEmail: String): Bill? {
        logger.info("Attempting to convert IncomingFile $incomingFileId to Bill for user $userEmail")
        val user = userRepository.findByEmail(userEmail)
        if (user == null) {
            logger.warn("User with email {} not found, cannot convert IncomingFile to Bill.", userEmail)
            return null
        }
        val incomingFile = incomingFileRepository.findById(incomingFileId)
        if (incomingFile == null) {
            logger.warn("IncomingFile with ID {} not found.", incomingFileId)
            return null
        }
        
        // Verify user ownership
        if (incomingFile.userId != user.id) {
            logger.warn("User $userEmail attempted to convert IncomingFile $incomingFileId they don't own")
            return null
        }
        
        val bill = Bill(
            filename = incomingFile.filename,
            filePath = incomingFile.filePath,
            uploadDate = incomingFile.uploadDate,
            status = ItemStatus.NEW,
            ocrRawJson = incomingFile.ocrRawJson,
            extractedAmount = incomingFile.extractedAmount,
            extractedDate = incomingFile.extractedDate,
            extractedProvider = incomingFile.extractedProvider,
            userId = incomingFile.userId,
            checksum = incomingFile.checksum,
            originalIncomingFileId = incomingFileId,
            ocrProcessedAt = incomingFile.ocrProcessedAt,
            ocrErrorMessage = incomingFile.ocrErrorMessage
        )
        
        val savedBill = billRepository.save(bill)
        
        // Transfer OCR history
        if (savedBill?.id != null) {
            ocrAttemptService.transferOcrHistory(
                EntityType.INCOMING_FILE, 
                incomingFileId, 
                EntityType.BILL, 
                savedBill.id!!, 
                userEmail
            )
            
            // Delete the original IncomingFile
            incomingFileRepository.delete(incomingFileId)
            logger.info("Converted IncomingFile $incomingFileId to Bill ${savedBill.id} for user $userEmail")
        }
        
        return savedBill
    }

    /**
     * Converts an IncomingFile to a Receipt
     */
    fun convertIncomingFileToReceipt(incomingFileId: Long, userEmail: String): Receipt? {
        logger.info("Attempting to convert IncomingFile $incomingFileId to Receipt for user $userEmail")
        val user = userRepository.findByEmail(userEmail)
        if (user == null) {
            logger.warn("User with email {} not found, cannot convert IncomingFile to Receipt.", userEmail)
            return null
        }
        val incomingFile = incomingFileRepository.findById(incomingFileId)
        if (incomingFile == null) {
            logger.warn("IncomingFile with ID {} not found.", incomingFileId)
            return null
        }
        
        // Verify user ownership
        if (incomingFile.userId != user.id) {
            logger.warn("User $userEmail attempted to convert IncomingFile $incomingFileId they don't own")
            return null
        }
        
        val receipt = Receipt(
            userId = incomingFile.userId,
            billId = null,
            filename = incomingFile.filename,
            filePath = incomingFile.filePath,
            uploadDate = incomingFile.uploadDate,
            checksum = incomingFile.checksum,
            status = ItemStatus.NEW,
            ocrRawJson = incomingFile.ocrRawJson,
            extractedAmount = incomingFile.extractedAmount,
            extractedDate = incomingFile.extractedDate,
            extractedProvider = incomingFile.extractedProvider,
            ocrProcessedAt = incomingFile.ocrProcessedAt,
            ocrErrorMessage = incomingFile.ocrErrorMessage,
            originalIncomingFileId = incomingFileId
        )
        
        val savedReceipt = receiptRepository.save(receipt)
        
        // Transfer OCR history
        if (savedReceipt?.id != null) {
            ocrAttemptService.transferOcrHistory(
                EntityType.INCOMING_FILE, 
                incomingFileId, 
                EntityType.RECEIPT, 
                savedReceipt.id!!, 
                userEmail
            )
            
            // Delete the original IncomingFile
            incomingFileRepository.delete(incomingFileId)
            logger.info("Converted IncomingFile $incomingFileId to Receipt ${savedReceipt.id} for user $userEmail")
        }
        
        return savedReceipt
    }

    /**
     * Reverts a Bill back to an IncomingFile
     */
    fun revertBillToIncomingFile(billId: Long, userEmail: String): IncomingFile? {
        logger.info("Attempting to revert Bill $billId to IncomingFile for user $userEmail")
        val user = userRepository.findByEmail(userEmail)
        if (user == null) {
            logger.warn("User with email {} not found, cannot revert Bill to IncomingFile.", userEmail)
            return null
        }
        val bill = billRepository.findById(billId)
        if (bill == null) {
            logger.warn("Bill with ID {} not found.", billId)
            return null
        }
        
        // Verify user ownership
        if (bill.userId != user.id) {
            logger.warn("User $userEmail attempted to revert Bill $billId they don't own")
            return null
        }
        
        val incomingFile = IncomingFile(
            filename = bill.filename,
            filePath = bill.filePath,
            uploadDate = bill.uploadDate,
            status = ItemStatus.NEW,
            checksum = bill.checksum ?: "",
            userId = bill.userId,
            ocrRawJson = bill.ocrRawJson,
            extractedAmount = bill.extractedAmount,
            extractedDate = bill.extractedDate,
            extractedProvider = bill.extractedProvider,
            ocrProcessedAt = bill.ocrProcessedAt,
            ocrErrorMessage = bill.ocrErrorMessage
        )
        
        val savedIncomingFile = incomingFileRepository.save(incomingFile)
        
        // Transfer OCR history back
        if (savedIncomingFile?.id != null) {
            ocrAttemptService.transferOcrHistory(
                EntityType.BILL, 
                billId, 
                EntityType.INCOMING_FILE, 
                savedIncomingFile.id!!, 
                userEmail
            )
            
            // Delete the Bill
            billRepository.delete(billId)
            logger.info("Reverted Bill $billId to IncomingFile ${savedIncomingFile.id} for user $userEmail")
        }
        
        return savedIncomingFile
    }

    /**
     * Reverts a Receipt back to an IncomingFile
     */
    fun revertReceiptToIncomingFile(receiptId: Long, userEmail: String): IncomingFile? {
        logger.info("Attempting to revert Receipt $receiptId to IncomingFile for user $userEmail")
        val user = userRepository.findByEmail(userEmail)
        if (user == null) {
            logger.warn("User with email {} not found, cannot revert Receipt to IncomingFile.", userEmail)
            return null
        }
        val receipt = receiptRepository.findById(receiptId)
        if (receipt == null) {
            logger.warn("Receipt with ID {} not found.", receiptId)
            return null
        }
        
        // Verify user ownership
        if (receipt.userId != user.id) {
            logger.warn("User $userEmail attempted to revert Receipt $receiptId they don't own")
            return null
        }
        
        // Only revert if receipt has file metadata
        if (receipt.filename == null || receipt.filePath == null) {
            logger.warn("Receipt $receiptId doesn't have file metadata, cannot revert to IncomingFile")
            return null
        }
        
        val incomingFile = IncomingFile(
            filename = receipt.filename!!,
            filePath = receipt.filePath!!,
            uploadDate = receipt.uploadDate ?: LocalDateTime.now(),
            status = ItemStatus.NEW,
            checksum = receipt.checksum ?: "",
            userId = receipt.userId,
            ocrRawJson = receipt.ocrRawJson,
            extractedAmount = receipt.extractedAmount,
            extractedDate = receipt.extractedDate,
            extractedProvider = receipt.extractedProvider,
            ocrProcessedAt = receipt.ocrProcessedAt,
            ocrErrorMessage = receipt.ocrErrorMessage
        )
        
        val savedIncomingFile = incomingFileRepository.save(incomingFile)
        
        // Transfer OCR history back
        if (savedIncomingFile?.id != null) {
            ocrAttemptService.transferOcrHistory(
                EntityType.RECEIPT, 
                receiptId, 
                EntityType.INCOMING_FILE, 
                savedIncomingFile.id!!, 
                userEmail
            )
            
            // Delete the Receipt
            receiptRepository.delete(receiptId)
            logger.info("Reverted Receipt $receiptId to IncomingFile ${savedIncomingFile.id} for user $userEmail")
        }
        
        return savedIncomingFile
    }

    /**
     * Checks if an entity can be reverted to IncomingFile
     */
    fun canRevertToIncomingFile(entityType: EntityType, entityId: Long, userEmail: String): Boolean {
        logger.debug("Checking if entity type {} with ID {} can be reverted to IncomingFile for user {}", entityType, entityId, userEmail)
        val user = userRepository.findByEmail(userEmail)
        if (user == null) {
            logger.warn("User with email {} not found, cannot check revert capability.", userEmail)
            return false
        }
        
        return when (entityType) {
            EntityType.BILL -> {
                val bill = billRepository.findById(entityId)
                val canRevert = bill != null && bill.userId == user.id && bill.originalIncomingFileId != null
                logger.debug("Bill {} can be reverted: {}", entityId, canRevert)
                canRevert
            }
            EntityType.RECEIPT -> {
                val receipt = receiptRepository.findById(entityId)
                val canRevert = receipt != null && receipt.userId == user.id && 
                receipt.filename != null && receipt.filePath != null
                logger.debug("Receipt {} can be reverted: {}", entityId, canRevert)
                canRevert
            }
            EntityType.INCOMING_FILE -> {
                logger.debug("Entity {} is already an IncomingFile, cannot revert.", entityId)
                false // Already an IncomingFile
            }
        }
    }
}