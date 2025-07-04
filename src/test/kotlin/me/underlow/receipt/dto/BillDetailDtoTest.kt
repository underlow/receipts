package me.underlow.receipt.dto

import me.underlow.receipt.model.Bill
import me.underlow.receipt.model.BillStatus
import me.underlow.receipt.model.Receipt
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.LocalDateTime
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class BillDetailDtoTest {

    @Test
    fun `given Bill with all properties when creating BillDetailDto then should map all properties correctly`() {
        // Given: Bill with all properties including checksum, ocrProcessedAt, ocrErrorMessage, originalIncomingFileId
        val billId = 1L
        val filename = "test_bill.pdf"
        val filePath = "/path/to/test_bill.pdf"
        val uploadDate = LocalDateTime.now()
        val status = BillStatus.PENDING
        val ocrRawJson = """{"provider": "Test Provider", "amount": 100.50}"""
        val extractedAmount = 100.50
        val extractedDate = LocalDate.of(2024, 1, 15)
        val extractedProvider = "Test Provider"
        val userId = 1L
        val checksum = "a1b2c3d4e5f6789012345678901234567890abcdef1234567890abcdef123456"
        val originalIncomingFileId = 2L
        val ocrProcessedAt = LocalDateTime.now().minusMinutes(5)
        val ocrErrorMessage = "OCR processing completed successfully"

        val bill = Bill(
            id = billId,
            filename = filename,
            filePath = filePath,
            uploadDate = uploadDate,
            status = status,
            ocrRawJson = ocrRawJson,
            extractedAmount = extractedAmount,
            extractedDate = extractedDate,
            extractedProvider = extractedProvider,
            userId = userId,
            checksum = checksum,
            originalIncomingFileId = originalIncomingFileId,
            ocrProcessedAt = ocrProcessedAt,
            ocrErrorMessage = ocrErrorMessage
        )

        // When: BillDetailDto is created from Bill
        val billDetailDto = BillDetailDto.fromBill(bill)

        // Then: All properties should be correctly mapped including new properties
        assertNotNull(billDetailDto)
        assertEquals(billId, billDetailDto.id)
        assertEquals(filename, billDetailDto.filename)
        assertEquals(filePath, billDetailDto.filePath)
        assertEquals(uploadDate, billDetailDto.uploadDate)
        assertEquals(status, billDetailDto.status)
        assertEquals("Pending Review", billDetailDto.statusDisplayName)
        assertEquals(ocrRawJson, billDetailDto.ocrRawJson)
        assertEquals(extractedAmount, billDetailDto.extractedAmount)
        assertEquals(extractedDate, billDetailDto.extractedDate)
        assertEquals(extractedProvider, billDetailDto.extractedProvider)
        assertEquals("/api/bills/$billId/image", billDetailDto.imageUrl)
        assertEquals("/api/bills/$billId/thumbnail", billDetailDto.thumbnailUrl)
        assertEquals(checksum, billDetailDto.checksum)
        assertEquals(originalIncomingFileId, billDetailDto.originalIncomingFileId)
        assertEquals(ocrProcessedAt, billDetailDto.ocrProcessedAt)
        assertEquals(ocrErrorMessage, billDetailDto.ocrErrorMessage)
    }

    @Test
    fun `given Bill with null optional properties when creating BillDetailDto then should handle nulls correctly`() {
        // Given: Bill with null optional properties
        val billId = 1L
        val filename = "test_bill.pdf"
        val filePath = "/path/to/test_bill.pdf"
        val uploadDate = LocalDateTime.now()
        val status = BillStatus.APPROVED
        val userId = 1L

        val bill = Bill(
            id = billId,
            filename = filename,
            filePath = filePath,
            uploadDate = uploadDate,
            status = status,
            ocrRawJson = null,
            extractedAmount = null,
            extractedDate = null,
            extractedProvider = null,
            userId = userId,
            checksum = null,
            originalIncomingFileId = null,
            ocrProcessedAt = null,
            ocrErrorMessage = null
        )

        // When: BillDetailDto is created from Bill
        val billDetailDto = BillDetailDto.fromBill(bill)

        // Then: Null properties should be handled correctly
        assertNotNull(billDetailDto)
        assertEquals(billId, billDetailDto.id)
        assertEquals(filename, billDetailDto.filename)
        assertEquals(filePath, billDetailDto.filePath)
        assertEquals(uploadDate, billDetailDto.uploadDate)
        assertEquals(status, billDetailDto.status)
        assertEquals("Approved", billDetailDto.statusDisplayName)
        assertEquals(null, billDetailDto.ocrRawJson)
        assertEquals(null, billDetailDto.extractedAmount)
        assertEquals(null, billDetailDto.extractedDate)
        assertEquals(null, billDetailDto.extractedProvider)
        assertEquals("/api/bills/$billId/image", billDetailDto.imageUrl)
        assertEquals("/api/bills/$billId/thumbnail", billDetailDto.thumbnailUrl)
        assertEquals(null, billDetailDto.checksum)
        assertEquals(null, billDetailDto.originalIncomingFileId)
        assertEquals(null, billDetailDto.ocrProcessedAt)
        assertEquals(null, billDetailDto.ocrErrorMessage)
    }

    @Test
    fun `given Bill with receipts when creating BillDetailDto then should map associated receipts correctly`() {
        // Given: Bill with associated receipts
        val billId = 1L
        val userId = 1L
        val bill = Bill(
            id = billId,
            filename = "test_bill.pdf",
            filePath = "/path/to/test_bill.pdf",
            uploadDate = LocalDateTime.now(),
            status = BillStatus.PROCESSING,
            userId = userId,
            checksum = "test_checksum"
        )

        val receipt1 = Receipt(id = 10L, userId = userId, billId = billId)
        val receipt2 = Receipt(id = 11L, userId = userId, billId = billId)
        val receipts = listOf(receipt1, receipt2)

        // When: BillDetailDto is created from Bill with receipts
        val billDetailDto = BillDetailDto.fromBill(bill, receipts)

        // Then: Associated receipts should be mapped correctly
        assertNotNull(billDetailDto)
        assertEquals(billId, billDetailDto.id)
        assertEquals("Processing", billDetailDto.statusDisplayName)
        assertEquals(2, billDetailDto.associatedReceipts.size)
        
        val receiptDto1 = billDetailDto.associatedReceipts[0]
        assertEquals(10L, receiptDto1.id)
        assertEquals("/api/receipts/10/thumbnail", receiptDto1.thumbnailUrl)
        assertEquals("/receipts/10", receiptDto1.detailUrl)
        
        val receiptDto2 = billDetailDto.associatedReceipts[1]
        assertEquals(11L, receiptDto2.id)
        assertEquals("/api/receipts/11/thumbnail", receiptDto2.thumbnailUrl)
        assertEquals("/receipts/11", receiptDto2.detailUrl)
    }

    @Test
    fun `given Bill with checksum when creating BillDetailDto then checksum should be accessible for template`() {
        // Given: Bill with checksum (the specific case mentioned in the issue)
        val billId = 1L
        val checksum = "sha256_checksum_example_123456789abcdef"
        val bill = Bill(
            id = billId,
            filename = "bill_with_checksum.pdf",
            filePath = "/path/to/bill_with_checksum.pdf",
            uploadDate = LocalDateTime.now(),
            status = BillStatus.PENDING,
            userId = 1L,
            checksum = checksum
        )

        // When: BillDetailDto is created from Bill
        val billDetailDto = BillDetailDto.fromBill(bill)

        // Then: Checksum should be accessible (this fixes the template error "Property or field 'checksum' cannot be found")
        assertNotNull(billDetailDto)
        assertEquals(checksum, billDetailDto.checksum)
        
        // Verify that all other template-referenced properties are also accessible
        assertNotNull(billDetailDto.id)
        assertNotNull(billDetailDto.filename)
        assertNotNull(billDetailDto.filePath)
        assertNotNull(billDetailDto.uploadDate)
        
        // These can be null but should still be accessible
        assertEquals(null, billDetailDto.originalIncomingFileId)
        assertEquals(null, billDetailDto.ocrProcessedAt)
        assertEquals(null, billDetailDto.ocrErrorMessage)
    }
}