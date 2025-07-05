package me.underlow.receipt.service

import me.underlow.receipt.model.Bill
import me.underlow.receipt.model.ItemStatus
import me.underlow.receipt.model.Receipt
import me.underlow.receipt.repository.BillRepository
import me.underlow.receipt.repository.ReceiptRepository
import me.underlow.receipt.repository.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

/**
 * Business logic service for managing Bill entities and OCR data handling
 */
@Service
class BillService(
    private val billRepository: BillRepository,
    private val receiptRepository: ReceiptRepository,
    private val userRepository: UserRepository
) {

    private val logger = LoggerFactory.getLogger(BillService::class.java)

    /**
     * Finds a Bill by ID and verifies user ownership via email
     */
    fun findByIdAndUserEmail(billId: Long, userEmail: String): Bill? {
        val user = userRepository.findByEmail(userEmail)
        if (user == null) {
            logger.warn("User with email {} not found.", userEmail)
            return null
        }
        val bill = billRepository.findById(billId)
        if (bill == null) {
            logger.warn("Bill with ID {} not found.", billId)
            return null
        }
        
        return if (bill.userId == user.id) {
            bill
        } else {
            logger.warn("Unauthorized access attempt - Bill with ID {} does not belong to user {}.", billId, userEmail)
            null
        }
    }

    /**
     * Finds all Bills for a user by email with optional status filtering
     */
    fun findByUserEmail(userEmail: String, status: ItemStatus? = null): List<Bill> {
        val user = userRepository.findByEmail(userEmail)
        if (user == null) {
            logger.warn("User with email {} not found.", userEmail)
            return emptyList()
        }
        val bills = billRepository.findByUserId(user.id!!)
        
        return if (status != null) {
            bills.filter { it.status == status }
        } else {
            bills
        }
    }

    /**
     * Creates a new Bill from uploaded file data
     */
    fun createBill(
        filename: String,
        filePath: String,
        userEmail: String,
        ocrRawJson: String? = null,
        extractedAmount: Double? = null,
        extractedDate: java.time.LocalDate? = null,
        extractedProvider: String? = null
    ): Bill? {
        logger.info("Attempting to create new bill for file: {} for user: {}", filename, userEmail)
        val user = userRepository.findByEmail(userEmail)
        if (user == null) {
            logger.warn("User with email {} not found, cannot create bill.", userEmail)
            return null
        }
        
        val bill = Bill(
            filename = filename,
            filePath = filePath,
            uploadDate = java.time.LocalDateTime.now(),
            status = ItemStatus.NEW,
            ocrRawJson = ocrRawJson,
            extractedAmount = extractedAmount,
            extractedDate = extractedDate,
            extractedProvider = extractedProvider,
            userId = user.id!!
        )
        
        val savedBill = billRepository.save(bill)
        logger.info("Bill created successfully with ID: {} for user: {}", savedBill.id, userEmail)
        return savedBill
    }

    /**
     * Updates OCR data for a Bill
     */
    fun updateOcrData(
        billId: Long,
        userEmail: String,
        ocrRawJson: String,
        extractedAmount: Double? = null,
        extractedDate: java.time.LocalDate? = null,
        extractedProvider: String? = null
    ): Bill? {
        logger.info("Attempting to update OCR data for bill ID: {} for user: {}", billId, userEmail)
        val existingBill = findByIdAndUserEmail(billId, userEmail)
        if (existingBill == null) {
            logger.warn("Bill with ID {} not found or does not belong to user {}. Cannot update OCR data.", billId, userEmail)
            return null
        }
        
        val updatedBill = existingBill.copy(
            ocrRawJson = ocrRawJson,
            extractedAmount = extractedAmount,
            extractedDate = extractedDate,
            extractedProvider = extractedProvider,
            status = ItemStatus.PROCESSING
        )
        
        val savedBill = billRepository.save(updatedBill)
        logger.info("OCR data updated successfully for bill ID: {} for user: {}", savedBill.id, userEmail)
        return savedBill
    }

    /**
     * Updates the status of a Bill
     */
    fun updateStatus(billId: Long, userEmail: String, newStatus: ItemStatus): Boolean {
        logger.info("Attempting to update status of bill ID: {} to {} for user: {}", billId, newStatus, userEmail)
        val existingBill = findByIdAndUserEmail(billId, userEmail)
        if (existingBill == null) {
            logger.warn("Bill with ID {} not found or does not belong to user {}. Cannot update status.", billId, userEmail)
            return false
        }
        
        val updatedBill = existingBill.copy(status = newStatus)
        billRepository.save(updatedBill)
        logger.info("Status of bill ID: {} updated to {} successfully for user: {}", billId, newStatus, userEmail)
        return true
    }

    /**
     * Gets all Receipts associated with a Bill
     */
    fun getAssociatedReceipts(billId: Long, userEmail: String): List<Receipt> {
        val user = userRepository.findByEmail(userEmail)
        if (user == null) {
            logger.warn("User with email {} not found.", userEmail)
            return emptyList()
        }
        val bill = billRepository.findById(billId)
        if (bill == null) {
            logger.warn("Bill with ID {} not found.", billId)
            return emptyList()
        }
        
        return if (bill.userId == user.id) {
            receiptRepository.findByBillId(billId)
        } else {
            logger.warn("Unauthorized access attempt - Bill with ID {} does not belong to user {}.", billId, userEmail)
            emptyList()
        }
    }

    /**
     * Deletes a Bill and all associated Receipts
     */
    fun deleteBill(billId: Long, userEmail: String): Boolean {
        logger.info("User {} deleting bill ID: {}", userEmail, billId)
        val bill = findByIdAndUserEmail(billId, userEmail)
        if (bill == null) {
            logger.warn("Bill deletion failed - Bill with ID {} not found or unauthorized for user {}", billId, userEmail)
            return false
        }
        
        // First delete all associated receipts
        val receipts = receiptRepository.findByBillId(billId)
        if (receipts.isNotEmpty()) {
            receipts.forEach { receipt ->
                receiptRepository.delete(receipt.id!!)
            }
            logger.info("Deleted {} associated receipts for bill ID: {}", receipts.size, billId)
        }
        
        // Then delete the bill
        val deleted = billRepository.delete(billId)
        if (deleted) {
            logger.info("Bill ID: {} ({}) deleted successfully by user: {}", billId, bill.filename, userEmail)
        } else {
            logger.error("Failed to delete bill ID: {} for user: {}", billId, userEmail)
        }
        return deleted
    }

    /**
     * Counts Bills by status for a user
     */
    fun getBillStatistics(userEmail: String): Map<ItemStatus, Int> {
        val user = userRepository.findByEmail(userEmail)
        if (user == null) {
            logger.warn("User with email {} not found.", userEmail)
            return emptyMap()
        }
        val bills = billRepository.findByUserId(user.id!!)
        
        return bills.groupingBy { it.status }.eachCount()
    }

    /**
     * Approves a Bill for conversion to Payment
     */
    fun approveBill(billId: Long, userEmail: String): Bill? {
        logger.info("Attempting to approve bill ID: {} for user: {}", billId, userEmail)
        val success = updateStatus(billId, userEmail, ItemStatus.APPROVED)
        return if (success) {
            val approvedBill = findByIdAndUserEmail(billId, userEmail)
            logger.info("Bill ID: {} approved successfully for user: {}", billId, userEmail)
            approvedBill
        } else {
            logger.error("Failed to approve bill ID: {} for user: {}", billId, userEmail)
            null
        }
    }

    /**
     * Rejects a Bill
     */
    fun rejectBill(billId: Long, userEmail: String): Bill? {
        logger.info("Attempting to reject bill ID: {} for user: {}", billId, userEmail)
        val success = updateStatus(billId, userEmail, ItemStatus.REJECTED)
        return if (success) {
            val rejectedBill = findByIdAndUserEmail(billId, userEmail)
            logger.info("Bill ID: {} rejected successfully for user: {}", billId, userEmail)
            rejectedBill
        } else {
            logger.error("Failed to reject bill ID: {} for user: {}", billId, userEmail)
            null
        }
    }
}