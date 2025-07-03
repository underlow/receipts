package me.underlow.receipt.service

import me.underlow.receipt.model.Bill
import me.underlow.receipt.model.BillStatus
import me.underlow.receipt.model.Receipt
import me.underlow.receipt.repository.BillRepository
import me.underlow.receipt.repository.ReceiptRepository
import me.underlow.receipt.repository.UserRepository
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

    /**
     * Finds a Bill by ID and verifies user ownership via email
     */
    fun findByIdAndUserEmail(billId: Long, userEmail: String): Bill? {
        val user = userRepository.findByEmail(userEmail) ?: return null
        val bill = billRepository.findById(billId) ?: return null
        
        return if (bill.userId == user.id) bill else null
    }

    /**
     * Finds all Bills for a user by email with optional status filtering
     */
    fun findByUserEmail(userEmail: String, status: BillStatus? = null): List<Bill> {
        val user = userRepository.findByEmail(userEmail) ?: return emptyList()
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
        val user = userRepository.findByEmail(userEmail) ?: return null
        
        val bill = Bill(
            filename = filename,
            filePath = filePath,
            uploadDate = java.time.LocalDateTime.now(),
            status = BillStatus.PENDING,
            ocrRawJson = ocrRawJson,
            extractedAmount = extractedAmount,
            extractedDate = extractedDate,
            extractedProvider = extractedProvider,
            userId = user.id!!
        )
        
        return billRepository.save(bill)
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
        val existingBill = findByIdAndUserEmail(billId, userEmail) ?: return null
        
        val updatedBill = existingBill.copy(
            ocrRawJson = ocrRawJson,
            extractedAmount = extractedAmount,
            extractedDate = extractedDate,
            extractedProvider = extractedProvider,
            status = BillStatus.PROCESSING
        )
        
        return billRepository.save(updatedBill)
    }

    /**
     * Updates the status of a Bill
     */
    fun updateStatus(billId: Long, userEmail: String, newStatus: BillStatus): Boolean {
        val existingBill = findByIdAndUserEmail(billId, userEmail) ?: return false
        
        val updatedBill = existingBill.copy(status = newStatus)
        billRepository.save(updatedBill)
        
        return true
    }

    /**
     * Gets all Receipts associated with a Bill
     */
    fun getAssociatedReceipts(billId: Long, userEmail: String): List<Receipt> {
        val user = userRepository.findByEmail(userEmail) ?: return emptyList()
        val bill = billRepository.findById(billId) ?: return emptyList()
        
        return if (bill.userId == user.id) {
            receiptRepository.findByBillId(billId)
        } else {
            emptyList()
        }
    }

    /**
     * Deletes a Bill and all associated Receipts
     */
    fun deleteBill(billId: Long, userEmail: String): Boolean {
        val bill = findByIdAndUserEmail(billId, userEmail) ?: return false
        
        // First delete all associated receipts
        val receipts = receiptRepository.findByBillId(billId)
        receipts.forEach { receipt ->
            receiptRepository.delete(receipt.id!!)
        }
        
        // Then delete the bill
        return billRepository.delete(billId)
    }

    /**
     * Counts Bills by status for a user
     */
    fun getBillStatistics(userEmail: String): Map<BillStatus, Int> {
        val user = userRepository.findByEmail(userEmail) ?: return emptyMap()
        val bills = billRepository.findByUserId(user.id!!)
        
        return bills.groupingBy { it.status }.eachCount()
    }

    /**
     * Approves a Bill for conversion to Payment
     */
    fun approveBill(billId: Long, userEmail: String): Bill? {
        return updateStatus(billId, userEmail, BillStatus.APPROVED)
            .takeIf { it }
            ?.let { findByIdAndUserEmail(billId, userEmail) }
    }

    /**
     * Rejects a Bill
     */
    fun rejectBill(billId: Long, userEmail: String): Bill? {
        return updateStatus(billId, userEmail, BillStatus.REJECTED)
            .takeIf { it }
            ?.let { findByIdAndUserEmail(billId, userEmail) }
    }
}