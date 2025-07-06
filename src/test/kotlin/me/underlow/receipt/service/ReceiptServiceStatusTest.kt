package me.underlow.receipt.service

import me.underlow.receipt.model.ItemStatus
import me.underlow.receipt.model.Receipt
import me.underlow.receipt.model.User
import me.underlow.receipt.model.Bill
import me.underlow.receipt.repository.ReceiptRepository
import me.underlow.receipt.repository.BillRepository
import me.underlow.receipt.repository.UserRepository
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.mockito.kotlin.verify
import org.mockito.kotlin.any
import java.time.LocalDateTime
import java.time.LocalDate
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertNull
import kotlin.test.assertNotNull

/**
 * Unit tests for ReceiptService status-based operations
 */
class ReceiptServiceStatusTest {

    private val receiptRepository = mock<ReceiptRepository>()
    private val billRepository = mock<BillRepository>()
    private val userRepository = mock<UserRepository>()
    private val receiptService = ReceiptService(receiptRepository, billRepository, userRepository)

    /**
     * Test status-based filtering functionality
     * Given: User with receipts in different statuses
     * When: Finding receipts by status
     * Then: Should return only receipts with specified status
     */
    @Test
    fun `Given receipts with different statuses, when finding by status, then should return only matching status`() {
        // Given: User with receipts in different statuses
        val userEmail = "status@example.com"
        val user = User(id = 1L, email = userEmail, name = "Status User")
        val receipts = listOf(
            Receipt(1L, 1L, null, "new.pdf", "/path/new.pdf", LocalDateTime.now(), 
                   "checksum1", ItemStatus.NEW),
            Receipt(2L, 1L, null, "approved.pdf", "/path/approved.pdf", LocalDateTime.now(), 
                   "checksum2", ItemStatus.APPROVED),
            Receipt(3L, 1L, null, "rejected.pdf", "/path/rejected.pdf", LocalDateTime.now(), 
                   "checksum3", ItemStatus.REJECTED)
        )
        
        whenever(userRepository.findByEmail(userEmail)).thenReturn(user)
        whenever(receiptRepository.findByUserId(1L)).thenReturn(receipts)

        // When: Finding receipts by NEW status
        val newReceipts = receiptService.findByUserEmail(userEmail, ItemStatus.NEW)

        // Then: Should return only NEW receipts
        assertEquals(1, newReceipts.size, "Should return only NEW receipts")
        assertEquals(ItemStatus.NEW, newReceipts[0].status, "Receipt should have NEW status")
        assertEquals("new.pdf", newReceipts[0].filename, "Should return correct receipt")
    }

    /**
     * Test finding receipts without status filter
     * Given: User with receipts in different statuses
     * When: Finding receipts without status filter
     * Then: Should return all receipts
     */
    @Test
    fun `Given receipts with different statuses, when finding without status filter, then should return all receipts`() {
        // Given: User with receipts in different statuses
        val userEmail = "allreceipts@example.com"
        val user = User(id = 2L, email = userEmail, name = "All Receipts User")
        val receipts = listOf(
            Receipt(1L, 2L, null, "receipt1.pdf", "/path/receipt1.pdf", LocalDateTime.now(), 
                   "checksum1", ItemStatus.NEW),
            Receipt(2L, 2L, null, "receipt2.pdf", "/path/receipt2.pdf", LocalDateTime.now(), 
                   "checksum2", ItemStatus.APPROVED)
        )
        
        whenever(userRepository.findByEmail(userEmail)).thenReturn(user)
        whenever(receiptRepository.findByUserId(2L)).thenReturn(receipts)

        // When: Finding receipts without status filter
        val allReceipts = receiptService.findByUserEmail(userEmail)

        // Then: Should return all receipts
        assertEquals(2, allReceipts.size, "Should return all receipts")
    }

    /**
     * Test status update functionality
     * Given: Receipt belonging to user
     * When: Updating status
     * Then: Should update status successfully
     */
    @Test
    fun `Given receipt belonging to user, when updating status, then should update successfully`() {
        // Given: Receipt belonging to user
        val userEmail = "update@example.com"
        val user = User(id = 3L, email = userEmail, name = "Update User")
        val receipt = Receipt(1L, 3L, null, "update.pdf", "/path/update.pdf", LocalDateTime.now(), 
                             "checksum1", ItemStatus.NEW)
        val updatedReceipt = receipt.copy(status = ItemStatus.APPROVED)
        
        whenever(userRepository.findByEmail(userEmail)).thenReturn(user)
        whenever(receiptRepository.findById(1L)).thenReturn(receipt)
        whenever(receiptRepository.save(any())).thenReturn(updatedReceipt)

        // When: Updating status to APPROVED
        val result = receiptService.updateStatus(1L, userEmail, ItemStatus.APPROVED)

        // Then: Should update successfully
        assertTrue(result, "Status update should be successful")
        verify(receiptRepository).save(any())
    }

    /**
     * Test approval functionality
     * Given: Receipt belonging to user
     * When: Approving receipt
     * Then: Should approve successfully and return updated receipt
     */
    @Test
    fun `Given receipt belonging to user, when approving, then should approve successfully`() {
        // Given: Receipt belonging to user
        val userEmail = "approve@example.com"
        val user = User(id = 4L, email = userEmail, name = "Approve User")
        val receipt = Receipt(1L, 4L, null, "approve.pdf", "/path/approve.pdf", LocalDateTime.now(), 
                             "checksum1", ItemStatus.NEW)
        val approvedReceipt = receipt.copy(status = ItemStatus.APPROVED)
        
        whenever(userRepository.findByEmail(userEmail)).thenReturn(user)
        whenever(receiptRepository.findById(1L)).thenReturn(receipt).thenReturn(approvedReceipt)
        whenever(receiptRepository.save(any())).thenReturn(approvedReceipt)

        // When: Approving receipt
        val result = receiptService.approveReceipt(1L, userEmail)

        // Then: Should approve successfully
        assertNotNull(result, "Approval should return receipt")
        assertEquals(ItemStatus.APPROVED, result.status, "Receipt should be approved")
    }

    /**
     * Test rejection functionality
     * Given: Receipt belonging to user
     * When: Rejecting receipt
     * Then: Should reject successfully and return updated receipt
     */
    @Test
    fun `Given receipt belonging to user, when rejecting, then should reject successfully`() {
        // Given: Receipt belonging to user
        val userEmail = "reject@example.com"
        val user = User(id = 5L, email = userEmail, name = "Reject User")
        val receipt = Receipt(1L, 5L, null, "reject.pdf", "/path/reject.pdf", LocalDateTime.now(), 
                             "checksum1", ItemStatus.NEW)
        val rejectedReceipt = receipt.copy(status = ItemStatus.REJECTED)
        
        whenever(userRepository.findByEmail(userEmail)).thenReturn(user)
        whenever(receiptRepository.findById(1L)).thenReturn(receipt).thenReturn(rejectedReceipt)
        whenever(receiptRepository.save(any())).thenReturn(rejectedReceipt)

        // When: Rejecting receipt
        val result = receiptService.rejectReceipt(1L, userEmail)

        // Then: Should reject successfully
        assertNotNull(result, "Rejection should return receipt")
        assertEquals(ItemStatus.REJECTED, result.status, "Receipt should be rejected")
    }

    /**
     * Test status statistics generation
     * Given: User with receipts in different statuses
     * When: Getting receipt statistics by status
     * Then: Should return correct counts for each status
     */
    @Test
    fun `Given receipts with different statuses, when getting statistics, then should return correct counts`() {
        // Given: User with receipts in different statuses
        val userEmail = "stats@example.com"
        val user = User(id = 6L, email = userEmail, name = "Stats User")
        val receipts = listOf(
            Receipt(1L, 6L, null, "new1.pdf", "/path/new1.pdf", LocalDateTime.now(), 
                   "checksum1", ItemStatus.NEW),
            Receipt(2L, 6L, null, "new2.pdf", "/path/new2.pdf", LocalDateTime.now(), 
                   "checksum2", ItemStatus.NEW),
            Receipt(3L, 6L, null, "approved.pdf", "/path/approved.pdf", LocalDateTime.now(), 
                   "checksum3", ItemStatus.APPROVED),
            Receipt(4L, 6L, null, "rejected.pdf", "/path/rejected.pdf", LocalDateTime.now(), 
                   "checksum4", ItemStatus.REJECTED)
        )
        
        whenever(userRepository.findByEmail(userEmail)).thenReturn(user)
        whenever(receiptRepository.findByUserId(6L)).thenReturn(receipts)

        // When: Getting receipt statistics by status
        val statistics = receiptService.getReceiptStatisticsByStatus(userEmail)

        // Then: Should return correct counts
        assertEquals(2, statistics[ItemStatus.NEW], "NEW count should be 2")
        assertEquals(1, statistics[ItemStatus.APPROVED], "APPROVED count should be 1")
        assertEquals(1, statistics[ItemStatus.REJECTED], "REJECTED count should be 1")
    }

    /**
     * Test creating receipt from file with OCR data
     * Given: Valid user and file data with OCR results
     * When: Creating receipt from file
     * Then: Should create receipt with all OCR data
     */
    @Test
    fun `Given valid user and OCR data, when creating receipt from file, then should create receipt with OCR data`() {
        // Given: Valid user and file data with OCR results
        val userEmail = "create@example.com"
        val user = User(id = 7L, email = userEmail, name = "Create User")
        val filename = "receipt.pdf"
        val filePath = "/path/receipt.pdf"
        val ocrRawJson = "{\"amount\": 25.50, \"provider\": \"TestCorp\"}"
        val extractedAmount = 25.50
        val extractedDate = LocalDate.of(2024, 1, 15)
        val extractedProvider = "TestCorp"
        
        val savedReceipt = Receipt(
            id = 1L, userId = 7L, billId = null, filename = filename, filePath = filePath,
            uploadDate = LocalDateTime.now(), checksum = "", status = ItemStatus.NEW,
            ocrRawJson = ocrRawJson, extractedAmount = extractedAmount,
            extractedDate = extractedDate, extractedProvider = extractedProvider
        )
        
        whenever(userRepository.findByEmail(userEmail)).thenReturn(user)
        whenever(receiptRepository.save(any())).thenReturn(savedReceipt)

        // When: Creating receipt from file with OCR data
        val result = receiptService.createReceiptFromFile(
            filename, filePath, userEmail, null, ocrRawJson, 
            extractedAmount, extractedDate, extractedProvider
        )

        // Then: Should create receipt with OCR data
        assertNotNull(result, "Receipt should be created")
        assertEquals(filename, result.filename, "Filename should match")
        assertEquals(filePath, result.filePath, "File path should match")
        assertEquals(ItemStatus.NEW, result.status, "Status should be NEW")
        assertEquals(ocrRawJson, result.ocrRawJson, "OCR JSON should be preserved")
        assertEquals(extractedAmount, result.extractedAmount, "Extracted amount should match")
        assertEquals(extractedDate, result.extractedDate, "Extracted date should match")
        assertEquals(extractedProvider, result.extractedProvider, "Extracted provider should match")
    }

    /**
     * Test that status updates fail for receipts not owned by user
     * Given: Receipt not belonging to user
     * When: Attempting to update status
     * Then: Should fail and return false
     */
    @Test
    fun `Given receipt not belonging to user, when updating status, then should fail`() {
        // Given: Receipt not belonging to user
        val userEmail = "notowner@example.com"
        val user = User(id = 8L, email = userEmail, name = "Not Owner User")
        val receipt = Receipt(1L, 999L, null, "notmine.pdf", "/path/notmine.pdf", LocalDateTime.now(), 
                             "checksum1", ItemStatus.NEW) // Different userId
        
        whenever(userRepository.findByEmail(userEmail)).thenReturn(user)
        whenever(receiptRepository.findById(1L)).thenReturn(receipt)

        // When: Attempting to update status
        val result = receiptService.updateStatus(1L, userEmail, ItemStatus.APPROVED)

        // Then: Should fail
        assertEquals(false, result, "Status update should fail for non-owned receipt")
    }

    /**
     * Test that approval fails for non-existent receipts
     * Given: Non-existent receipt ID
     * When: Attempting to approve
     * Then: Should return null
     */
    @Test
    fun `Given non-existent receipt, when approving, then should return null`() {
        // Given: Non-existent receipt ID
        val userEmail = "nonexistent@example.com"
        val user = User(id = 9L, email = userEmail, name = "Non-existent User")
        
        whenever(userRepository.findByEmail(userEmail)).thenReturn(user)
        whenever(receiptRepository.findById(999L)).thenReturn(null)

        // When: Attempting to approve non-existent receipt
        val result = receiptService.approveReceipt(999L, userEmail)

        // Then: Should return null
        assertNull(result, "Approval should return null for non-existent receipt")
    }
}