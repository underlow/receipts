package me.underlow.receipt.service

import me.underlow.receipt.model.*
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.mockito.kotlin.verify
import java.time.LocalDateTime
import java.time.LocalDate
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertNotNull

/**
 * Unit tests for InboxService status-based operations
 */
class InboxServiceTest {

    private val incomingFileService = mock<IncomingFileService>()
    private val billService = mock<BillService>()
    private val receiptService = mock<ReceiptService>()
    private val entityConversionService = mock<EntityConversionService>()
    private val inboxService = InboxService(incomingFileService, billService, receiptService, entityConversionService)

    /**
     * Test getting new items from all entity types
     * Given: User with IncomingFiles, Bills, and Receipts in NEW status
     * When: Getting new items
     * Then: Should return all NEW items from all entity types
     */
    @Test
    fun `Given items with NEW status across all entities, when getting new items, then should return all NEW items`() {
        // Given: User with NEW items across all entity types
        val userEmail = "new@example.com"
        val uploadTime = LocalDateTime.now()
        
        val incomingFiles = listOf(
            IncomingFile(1L, "incoming.pdf", "/path/incoming.pdf", uploadTime, ItemStatus.NEW, "checksum1", 1L)
        )
        val bills = listOf(
            Bill(2L, "bill.pdf", "/path/bill.pdf", uploadTime, ItemStatus.NEW, 
                ocrRawJson = null, extractedAmount = 100.0, extractedDate = LocalDate.now(), 
                extractedProvider = "Provider", userId = 1L, checksum = "checksum2")
        )
        val receipts = listOf(
            Receipt(3L, 1L, null, "receipt.pdf", "/path/receipt.pdf", uploadTime, 
                   "checksum3", ItemStatus.NEW)
        )
        
        whenever(incomingFileService.findByUserEmailAndStatus(userEmail, ItemStatus.NEW)).thenReturn(incomingFiles)
        whenever(billService.findByUserEmail(userEmail, ItemStatus.NEW)).thenReturn(bills)
        whenever(receiptService.findByUserEmail(userEmail, ItemStatus.NEW)).thenReturn(receipts)

        // When: Getting new items
        val newItems = inboxService.getNewItems(userEmail)

        // Then: Should return all NEW items
        assertEquals(3, newItems.size, "Should return all NEW items from all entity types")
        
        val incomingFileItem = newItems.find { it.type == InboxService.InboxItemType.INCOMING_FILE }
        assertNotNull(incomingFileItem, "Should contain IncomingFile item")
        assertEquals("incoming.pdf", incomingFileItem.filename, "IncomingFile should have correct filename")
        
        val billItem = newItems.find { it.type == InboxService.InboxItemType.BILL }
        assertNotNull(billItem, "Should contain Bill item")
        assertEquals("bill.pdf", billItem.filename, "Bill should have correct filename")
        assertEquals(100.0, billItem.extractedAmount, "Bill should have extracted amount")
        
        val receiptItem = newItems.find { it.type == InboxService.InboxItemType.RECEIPT }
        assertNotNull(receiptItem, "Should contain Receipt item")
        assertEquals("receipt.pdf", receiptItem.filename, "Receipt should have correct filename")
    }

    /**
     * Test getting approved items (only Bills and Receipts)
     * Given: User with approved Bills and Receipts
     * When: Getting approved items
     * Then: Should return only approved Bills and Receipts
     */
    @Test
    fun `Given approved Bills and Receipts, when getting approved items, then should return only approved Bills and Receipts`() {
        // Given: User with approved Bills and Receipts
        val userEmail = "approved@example.com"
        val uploadTime = LocalDateTime.now()
        
        val bills = listOf(
            Bill(1L, "approved_bill.pdf", "/path/approved_bill.pdf", uploadTime, ItemStatus.APPROVED, 
                ocrRawJson = null, extractedAmount = 150.0, extractedDate = LocalDate.now(), 
                extractedProvider = "Approved Provider", userId = 1L, checksum = "checksum1")
        )
        val receipts = listOf(
            Receipt(2L, 1L, null, "approved_receipt.pdf", "/path/approved_receipt.pdf", uploadTime, 
                   "checksum2", ItemStatus.APPROVED)
        )
        
        whenever(billService.findByUserEmail(userEmail, ItemStatus.APPROVED)).thenReturn(bills)
        whenever(receiptService.findByUserEmail(userEmail, ItemStatus.APPROVED)).thenReturn(receipts)

        // When: Getting approved items
        val approvedItems = inboxService.getApprovedItems(userEmail)

        // Then: Should return only approved Bills and Receipts
        assertEquals(2, approvedItems.size, "Should return approved Bills and Receipts")
        assertTrue(approvedItems.all { it.status == ItemStatus.APPROVED }, "All items should be approved")
        assertTrue(approvedItems.any { it.type == InboxService.InboxItemType.BILL }, "Should contain Bills")
        assertTrue(approvedItems.any { it.type == InboxService.InboxItemType.RECEIPT }, "Should contain Receipts")
    }

    /**
     * Test getting rejected items (only Bills and Receipts)
     * Given: User with rejected Bills and Receipts
     * When: Getting rejected items
     * Then: Should return only rejected Bills and Receipts
     */
    @Test
    fun `Given rejected Bills and Receipts, when getting rejected items, then should return only rejected Bills and Receipts`() {
        // Given: User with rejected Bills and Receipts
        val userEmail = "rejected@example.com"
        val uploadTime = LocalDateTime.now()
        
        val bills = listOf(
            Bill(1L, "rejected_bill.pdf", "/path/rejected_bill.pdf", uploadTime, ItemStatus.REJECTED, 
                ocrRawJson = null, extractedAmount = 75.0, extractedDate = LocalDate.now(), 
                extractedProvider = "Rejected Provider", userId = 1L, checksum = "checksum1")
        )
        val receipts = listOf(
            Receipt(2L, 1L, null, "rejected_receipt.pdf", "/path/rejected_receipt.pdf", uploadTime, 
                   "checksum2", ItemStatus.REJECTED)
        )
        
        whenever(billService.findByUserEmail(userEmail, ItemStatus.REJECTED)).thenReturn(bills)
        whenever(receiptService.findByUserEmail(userEmail, ItemStatus.REJECTED)).thenReturn(receipts)

        // When: Getting rejected items
        val rejectedItems = inboxService.getRejectedItems(userEmail)

        // Then: Should return only rejected Bills and Receipts
        assertEquals(2, rejectedItems.size, "Should return rejected Bills and Receipts")
        assertTrue(rejectedItems.all { it.status == ItemStatus.REJECTED }, "All items should be rejected")
        assertTrue(rejectedItems.any { it.type == InboxService.InboxItemType.BILL }, "Should contain Bills")
        assertTrue(rejectedItems.any { it.type == InboxService.InboxItemType.RECEIPT }, "Should contain Receipts")
    }

    // Note: Test for PROCESSING status removed as PROCESSING was removed from ItemStatus enum

    /**
     * Test item approval functionality
     * Given: Valid item and user
     * When: Approving item
     * Then: Should delegate to appropriate service
     */
    @Test
    fun `Given valid IncomingFile, when approving, then should delegate to IncomingFileService`() {
        // Given: Valid IncomingFile item
        val userEmail = "approve@example.com"
        val itemId = 1L
        
        whenever(incomingFileService.updateStatus(itemId, userEmail, ItemStatus.APPROVED)).thenReturn(true)

        // When: Approving IncomingFile item
        val result = inboxService.approveItem(itemId, InboxService.InboxItemType.INCOMING_FILE, userEmail)

        // Then: Should delegate to IncomingFileService and return true
        assertTrue(result, "Approval should be successful")
        verify(incomingFileService).updateStatus(itemId, userEmail, ItemStatus.APPROVED)
    }

    /**
     * Test item rejection functionality
     * Given: Valid item and user
     * When: Rejecting item
     * Then: Should delegate to appropriate service
     */
    @Test
    fun `Given valid Bill, when rejecting, then should delegate to BillService`() {
        // Given: Valid Bill item
        val userEmail = "reject@example.com"
        val itemId = 2L
        
        whenever(billService.updateStatus(itemId, userEmail, ItemStatus.REJECTED)).thenReturn(true)

        // When: Rejecting Bill item
        val result = inboxService.rejectItem(itemId, InboxService.InboxItemType.BILL, userEmail)

        // Then: Should delegate to BillService and return true
        assertTrue(result, "Rejection should be successful")
        verify(billService).updateStatus(itemId, userEmail, ItemStatus.REJECTED)
    }

    /**
     * Test IncomingFile to Bill conversion
     * Given: Valid IncomingFile
     * When: Converting to Bill
     * Then: Should delegate to EntityConversionService
     */
    @Test
    fun `Given valid IncomingFile, when converting to Bill, then should delegate to EntityConversionService`() {
        // Given: Valid IncomingFile
        val userEmail = "convert@example.com"
        val incomingFileId = 1L
        val expectedBill = Bill(
            id = 1L, filename = "converted.pdf", filePath = "/path/converted.pdf", 
            uploadDate = LocalDateTime.now(), status = ItemStatus.NEW, userId = 1L, checksum = "checksum1"
        )
        
        whenever(entityConversionService.convertIncomingFileToBill(incomingFileId, userEmail)).thenReturn(expectedBill)

        // When: Converting IncomingFile to Bill
        val result = inboxService.convertIncomingFileToBill(incomingFileId, userEmail)

        // Then: Should delegate to EntityConversionService and return Bill
        assertNotNull(result, "Conversion should return a Bill")
        assertEquals(expectedBill.filename, result.filename, "Converted Bill should have correct filename")
        verify(entityConversionService).convertIncomingFileToBill(incomingFileId, userEmail)
    }

    /**
     * Test IncomingFile to Receipt conversion
     * Given: Valid IncomingFile
     * When: Converting to Receipt
     * Then: Should delegate to EntityConversionService
     */
    @Test
    fun `Given valid IncomingFile, when converting to Receipt, then should delegate to EntityConversionService`() {
        // Given: Valid IncomingFile
        val userEmail = "convert@example.com"
        val incomingFileId = 2L
        val expectedReceipt = Receipt(
            id = 1L, userId = 1L, billId = null, filename = "converted.pdf", filePath = "/path/converted.pdf", 
            uploadDate = LocalDateTime.now(), checksum = "checksum1", status = ItemStatus.NEW
        )
        
        whenever(entityConversionService.convertIncomingFileToReceipt(incomingFileId, userEmail)).thenReturn(expectedReceipt)

        // When: Converting IncomingFile to Receipt
        val result = inboxService.convertIncomingFileToReceipt(incomingFileId, userEmail)

        // Then: Should delegate to EntityConversionService and return Receipt
        assertNotNull(result, "Conversion should return a Receipt")
        assertEquals(expectedReceipt.filename, result.filename, "Converted Receipt should have correct filename")
        verify(entityConversionService).convertIncomingFileToReceipt(incomingFileId, userEmail)
    }

    /**
     * Test comprehensive inbox statistics
     * Given: User with various items across all entity types
     * When: Getting inbox statistics
     * Then: Should return aggregated statistics from all services
     */
    @Test
    fun `Given items across all entity types, when getting inbox statistics, then should return aggregated statistics`() {
        // Given: User with various items across all entity types
        val userEmail = "stats@example.com"
        
        val incomingFileStats = mapOf(
            ItemStatus.NEW to 2,
            ItemStatus.APPROVED to 0,
            ItemStatus.REJECTED to 0
        )
        val billStats = mapOf(
            ItemStatus.NEW to 1,
            ItemStatus.APPROVED to 2,
            ItemStatus.REJECTED to 1
        )
        val receiptStats = mapOf(
            ItemStatus.NEW to 3,
            ItemStatus.APPROVED to 1,
            ItemStatus.REJECTED to 0
        )
        
        whenever(incomingFileService.getFileStatistics(userEmail)).thenReturn(incomingFileStats)
        whenever(billService.getBillStatistics(userEmail)).thenReturn(billStats)
        whenever(receiptService.getReceiptStatisticsByStatus(userEmail)).thenReturn(receiptStats)

        // When: Getting inbox statistics
        val statistics = inboxService.getInboxStatistics(userEmail)

        // Then: Should return aggregated statistics
        assertEquals(3, statistics.size, "Should return statistics for all entity types")
        assertEquals(incomingFileStats, statistics["incomingFiles"], "Should include IncomingFile statistics")
        assertEquals(billStats, statistics["bills"], "Should include Bill statistics")
        assertEquals(receiptStats, statistics["receipts"], "Should include Receipt statistics")
    }

    /**
     * Test tab counts functionality
     * Given: User with items in different statuses
     * When: Getting tab counts
     * Then: Should return correct counts for each tab
     */
    @Test
    fun `Given items in different statuses, when getting tab counts, then should return correct counts for each tab`() {
        // Given: User with items in different statuses
        val userEmail = "tabs@example.com"
        val uploadTime = LocalDateTime.now()
        
        // NEW items (3 total: 1 IncomingFile + 1 Bill + 1 Receipt)
        val newIncomingFiles = listOf(
            IncomingFile(1L, "new.pdf", "/path/new.pdf", uploadTime, ItemStatus.NEW, "checksum1", 1L)
        )
        val newBills = listOf(
            Bill(2L, "new_bill.pdf", "/path/new_bill.pdf", uploadTime, ItemStatus.NEW, 
                userId = 1L, checksum = "checksum2")
        )
        val newReceipts = listOf(
            Receipt(3L, 1L, null, "new_receipt.pdf", "/path/new_receipt.pdf", uploadTime, 
                   "checksum3", ItemStatus.NEW)
        )
        
        // APPROVED items (2 total: 1 Bill + 1 Receipt)
        val approvedBills = listOf(
            Bill(4L, "approved_bill.pdf", "/path/approved_bill.pdf", uploadTime, ItemStatus.APPROVED, 
                userId = 1L, checksum = "checksum4")
        )
        val approvedReceipts = listOf(
            Receipt(5L, 1L, null, "approved_receipt.pdf", "/path/approved_receipt.pdf", uploadTime, 
                   "checksum5", ItemStatus.APPROVED)
        )
        
        // REJECTED items (1 total: 1 Bill)
        val rejectedBills = listOf(
            Bill(6L, "rejected_bill.pdf", "/path/rejected_bill.pdf", uploadTime, ItemStatus.REJECTED, 
                userId = 1L, checksum = "checksum6")
        )
        val rejectedReceipts = emptyList<Receipt>()
        
        whenever(incomingFileService.findByUserEmailAndStatus(userEmail, ItemStatus.NEW)).thenReturn(newIncomingFiles)
        whenever(billService.findByUserEmail(userEmail, ItemStatus.NEW)).thenReturn(newBills)
        whenever(receiptService.findByUserEmail(userEmail, ItemStatus.NEW)).thenReturn(newReceipts)
        whenever(billService.findByUserEmail(userEmail, ItemStatus.APPROVED)).thenReturn(approvedBills)
        whenever(receiptService.findByUserEmail(userEmail, ItemStatus.APPROVED)).thenReturn(approvedReceipts)
        whenever(billService.findByUserEmail(userEmail, ItemStatus.REJECTED)).thenReturn(rejectedBills)
        whenever(receiptService.findByUserEmail(userEmail, ItemStatus.REJECTED)).thenReturn(rejectedReceipts)

        // When: Getting tab counts
        val tabCounts = inboxService.getTabCounts(userEmail)

        // Then: Should return correct counts for each tab
        assertEquals(3, tabCounts["new"], "New tab should have 3 items")
        assertEquals(2, tabCounts["approved"], "Approved tab should have 2 items")
        assertEquals(1, tabCounts["rejected"], "Rejected tab should have 1 item")
    }

    /**
     * Test that receipts without file data are excluded from inbox views
     * Given: Receipts with and without file data
     * When: Getting new items
     * Then: Should only include receipts with file data
     */
    @Test
    fun `Given receipts with and without file data, when getting new items, then should only include receipts with file data`() {
        // Given: Receipts with and without file data
        val userEmail = "filedata@example.com"
        val uploadTime = LocalDateTime.now()
        
        val receipts = listOf(
            // Receipt with file data
            Receipt(1L, 1L, null, "with_file.pdf", "/path/with_file.pdf", uploadTime, 
                   "checksum1", ItemStatus.NEW),
            // Receipt without file data (created from form, not file)
            Receipt(2L, 1L, null, null, null, null, 
                   "", ItemStatus.NEW)
        )
        
        whenever(incomingFileService.findByUserEmailAndStatus(userEmail, ItemStatus.NEW)).thenReturn(emptyList())
        whenever(billService.findByUserEmail(userEmail, ItemStatus.NEW)).thenReturn(emptyList())
        whenever(receiptService.findByUserEmail(userEmail, ItemStatus.NEW)).thenReturn(receipts)

        // When: Getting new items
        val newItems = inboxService.getNewItems(userEmail)

        // Then: Should only include receipts with file data
        assertEquals(1, newItems.size, "Should only include receipts with file data")
        assertEquals("with_file.pdf", newItems[0].filename, "Should include receipt with file data")
    }
}