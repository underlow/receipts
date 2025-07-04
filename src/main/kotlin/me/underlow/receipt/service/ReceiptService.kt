package me.underlow.receipt.service

import me.underlow.receipt.model.Receipt
import me.underlow.receipt.model.Bill
import me.underlow.receipt.model.ItemStatus
import me.underlow.receipt.repository.ReceiptRepository
import me.underlow.receipt.repository.BillRepository
import me.underlow.receipt.repository.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

/**
 * Business logic service for managing Receipt entities and bill associations
 */
@Service
class ReceiptService(
    private val receiptRepository: ReceiptRepository,
    private val billRepository: BillRepository,
    private val userRepository: UserRepository
) {

    private val logger = LoggerFactory.getLogger(ReceiptService::class.java)

    /**
     * Finds a Receipt by ID and verifies user ownership via email
     */
    fun findByIdAndUserEmail(receiptId: Long, userEmail: String): Receipt? {
        val user = userRepository.findByEmail(userEmail) ?: return null
        val receipt = receiptRepository.findById(receiptId) ?: return null
        
        return if (receipt.userId == user.id) receipt else null
    }

    /**
     * Finds all Receipts for a user by email with optional status filtering
     */
    fun findByUserEmail(userEmail: String, status: ItemStatus? = null): List<Receipt> {
        logger.debug("Attempting to find receipts for user: {} with status: {}", userEmail, status)
        val user = userRepository.findByEmail(userEmail)
        if (user == null) {
            logger.warn("User with email {} not found.", userEmail)
            return emptyList()
        }
        val receipts = receiptRepository.findByUserId(user.id!!)
        
        return if (status != null) {
            val filteredReceipts = receipts.filter { it.status == status }
            logger.debug("Found {} receipts for user {} with status {}.", filteredReceipts.size, userEmail, status)
            filteredReceipts
        } else {
            logger.debug("Found {} receipts for user {}.", receipts.size, userEmail)
            receipts
        }
    }

    /**
     * Creates a new Receipt for a user
     */
    fun createReceipt(userEmail: String, billId: Long? = null): Receipt? {
        val user = userRepository.findByEmail(userEmail) ?: return null
        
        // If billId is provided, verify it belongs to the user
        if (billId != null) {
            val bill = billRepository.findById(billId)
            if (bill == null || bill.userId != user.id) {
                return null
            }
        }
        
        val receipt = Receipt(
            userId = user.id!!,
            billId = billId
        )
        
        return receiptRepository.save(receipt)
    }

    /**
     * Associates a Receipt with a Bill
     */
    fun associateWithBill(receiptId: Long, billId: Long, userEmail: String): Receipt? {
        val user = userRepository.findByEmail(userEmail) ?: return null
        val receipt = findByIdAndUserEmail(receiptId, userEmail) ?: return null
        val bill = billRepository.findById(billId) ?: return null
        
        // Verify the bill belongs to the same user
        if (bill.userId != user.id) {
            return null
        }
        
        val updatedReceipt = receipt.copy(billId = billId)
        return receiptRepository.save(updatedReceipt)
    }

    /**
     * Removes association between Receipt and Bill
     */
    fun removeFromBill(receiptId: Long, userEmail: String): Receipt? {
        val receipt = findByIdAndUserEmail(receiptId, userEmail) ?: return null
        
        val updatedReceipt = receipt.copy(billId = null)
        return receiptRepository.save(updatedReceipt)
    }

    /**
     * Gets the Bill associated with a Receipt
     */
    fun getAssociatedBill(receiptId: Long, userEmail: String): Bill? {
        val receipt = findByIdAndUserEmail(receiptId, userEmail) ?: return null
        val billId = receipt.billId ?: return null
        
        val bill = billRepository.findById(billId) ?: return null
        
        // Verify the bill belongs to the same user
        val user = userRepository.findByEmail(userEmail) ?: return null
        return if (bill.userId == user.id) bill else null
    }

    /**
     * Finds all Receipts associated with a specific Bill
     */
    fun findByBillId(billId: Long, userEmail: String): List<Receipt> {
        val user = userRepository.findByEmail(userEmail) ?: return emptyList()
        val bill = billRepository.findById(billId) ?: return emptyList()
        
        return if (bill.userId == user.id) {
            receiptRepository.findByBillId(billId)
        } else {
            emptyList()
        }
    }

    /**
     * Finds all standalone Receipts (not associated with any Bill)
     */
    fun findStandaloneReceipts(userEmail: String): List<Receipt> {
        val user = userRepository.findByEmail(userEmail) ?: return emptyList()
        val allReceipts = receiptRepository.findByUserId(user.id!!)
        
        return allReceipts.filter { it.billId == null }
    }

    /**
     * Finds all Bills that could be associated with a Receipt
     */
    fun findAvailableBills(receiptId: Long, userEmail: String): List<Bill> {
        val user = userRepository.findByEmail(userEmail) ?: return emptyList()
        val receipt = findByIdAndUserEmail(receiptId, userEmail) ?: return emptyList()
        
        // Return all bills for the user that are not rejected
        return billRepository.findByUserId(user.id!!)
            .filter { it.status != me.underlow.receipt.model.ItemStatus.REJECTED }
    }

    /**
     * Deletes a Receipt
     */
    fun deleteReceipt(receiptId: Long, userEmail: String): Boolean {
        val receipt = findByIdAndUserEmail(receiptId, userEmail) ?: return false
        return receiptRepository.delete(receiptId)
    }

    /**
     * Updates the status of a Receipt
     */
    fun updateStatus(receiptId: Long, userEmail: String, newStatus: ItemStatus): Boolean {
        logger.info("Attempting to update status of receipt ID: {} to {} for user: {}", receiptId, newStatus, userEmail)
        val existingReceipt = findByIdAndUserEmail(receiptId, userEmail)
        if (existingReceipt == null) {
            logger.warn("Receipt with ID {} not found or does not belong to user {}. Cannot update status.", receiptId, userEmail)
            return false
        }
        
        val updatedReceipt = existingReceipt.copy(status = newStatus)
        receiptRepository.save(updatedReceipt)
        logger.info("Status of receipt ID: {} updated to {} successfully for user: {}", receiptId, newStatus, userEmail)
        return true
    }

    /**
     * Approves a Receipt for conversion to Payment
     */
    fun approveReceipt(receiptId: Long, userEmail: String): Receipt? {
        logger.info("Attempting to approve receipt ID: {} for user: {}", receiptId, userEmail)
        val success = updateStatus(receiptId, userEmail, ItemStatus.APPROVED)
        return if (success) {
            val approvedReceipt = findByIdAndUserEmail(receiptId, userEmail)
            logger.info("Receipt ID: {} approved successfully for user: {}", receiptId, userEmail)
            approvedReceipt
        } else {
            logger.error("Failed to approve receipt ID: {} for user: {}", receiptId, userEmail)
            null
        }
    }

    /**
     * Rejects a Receipt
     */
    fun rejectReceipt(receiptId: Long, userEmail: String): Receipt? {
        logger.info("Attempting to reject receipt ID: {} for user: {}", receiptId, userEmail)
        val success = updateStatus(receiptId, userEmail, ItemStatus.REJECTED)
        return if (success) {
            val rejectedReceipt = findByIdAndUserEmail(receiptId, userEmail)
            logger.info("Receipt ID: {} rejected successfully for user: {}", receiptId, userEmail)
            rejectedReceipt
        } else {
            logger.error("Failed to reject receipt ID: {} for user: {}", receiptId, userEmail)
            null
        }
    }

    /**
     * Counts Receipts by status for a user
     */
    fun getReceiptStatisticsByStatus(userEmail: String): Map<ItemStatus, Int> {
        logger.debug("Attempting to get receipt statistics by status for user: {}", userEmail)
        val user = userRepository.findByEmail(userEmail)
        if (user == null) {
            logger.warn("User with email {} not found.", userEmail)
            return emptyMap()
        }
        val receipts = receiptRepository.findByUserId(user.id!!)
        
        val statistics = receipts.groupingBy { it.status }.eachCount()
        logger.debug("Generated receipt status statistics for user {}: {}", userEmail, statistics)
        return statistics
    }

    /**
     * Counts Receipts by association status for a user
     */
    fun getReceiptStatistics(userEmail: String): Map<String, Int> {
        val user = userRepository.findByEmail(userEmail) ?: return emptyMap()
        val receipts = receiptRepository.findByUserId(user.id!!)
        
        val associated = receipts.count { it.billId != null }
        val standalone = receipts.count { it.billId == null }
        
        return mapOf(
            "associated" to associated,
            "standalone" to standalone,
            "total" to receipts.size
        )
    }

    /**
     * Creates a new Receipt from an approved IncomingFile
     */
    fun createReceiptFromFile(
        filename: String,
        filePath: String,
        userEmail: String,
        billId: Long? = null,
        ocrRawJson: String? = null,
        extractedAmount: Double? = null,
        extractedDate: java.time.LocalDate? = null,
        extractedProvider: String? = null
    ): Receipt? {
        logger.info("Attempting to create new receipt from file: {} for user: {}", filename, userEmail)
        val user = userRepository.findByEmail(userEmail)
        if (user == null) {
            logger.warn("User with email {} not found, cannot create receipt.", userEmail)
            return null
        }
        
        // If billId is provided, verify it belongs to the user
        if (billId != null) {
            val bill = billRepository.findById(billId)
            if (bill == null || bill.userId != user.id) {
                logger.warn("Bill with ID {} not found or does not belong to user {}.", billId, userEmail)
                return null
            }
        }
        
        val receipt = Receipt(
            userId = user.id!!,
            billId = billId,
            filename = filename,
            filePath = filePath,
            uploadDate = java.time.LocalDateTime.now(),
            status = ItemStatus.NEW,
            ocrRawJson = ocrRawJson,
            extractedAmount = extractedAmount,
            extractedDate = extractedDate,
            extractedProvider = extractedProvider
        )
        
        val savedReceipt = receiptRepository.save(receipt)
        logger.info("Receipt created successfully with ID: {} for user: {}", savedReceipt?.id, userEmail)
        return savedReceipt
    }
}