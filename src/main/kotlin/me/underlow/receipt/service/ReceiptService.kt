package me.underlow.receipt.service

import me.underlow.receipt.model.Receipt
import me.underlow.receipt.model.Bill
import me.underlow.receipt.repository.ReceiptRepository
import me.underlow.receipt.repository.BillRepository
import me.underlow.receipt.repository.UserRepository
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

    /**
     * Finds a Receipt by ID and verifies user ownership via email
     */
    fun findByIdAndUserEmail(receiptId: Long, userEmail: String): Receipt? {
        val user = userRepository.findByEmail(userEmail) ?: return null
        val receipt = receiptRepository.findById(receiptId) ?: return null
        
        return if (receipt.userId == user.id) receipt else null
    }

    /**
     * Finds all Receipts for a user by email
     */
    fun findByUserEmail(userEmail: String): List<Receipt> {
        val user = userRepository.findByEmail(userEmail) ?: return emptyList()
        return receiptRepository.findByUserId(user.id!!)
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
        billId: Long? = null
    ): Receipt? {
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
}