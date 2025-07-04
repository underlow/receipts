package me.underlow.receipt.repository

import me.underlow.receipt.model.Receipt
import me.underlow.receipt.model.ItemStatus
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import java.time.LocalDateTime
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

/**
 * Unit tests for ReceiptRepository status-based query methods
 * Tests the new findByStatus() and findByUserIdAndStatus() methods
 */
class ReceiptRepositoryTest {

    private lateinit var jdbcTemplate: JdbcTemplate
    private lateinit var receiptRepository: ReceiptRepository

    @BeforeEach
    fun setup() {
        jdbcTemplate = mock()
        receiptRepository = ReceiptRepositoryImpl(jdbcTemplate)
    }

    /**
     * Test findByStatus method retrieves receipts with specific status
     * Given: Database contains receipts with different statuses
     * When: Calling findByStatus with NEW status
     * Then: Should return only receipts with NEW status
     */
    @Test
    fun `Given receipts with different statuses, when calling findByStatus with NEW, then should return only NEW receipts`() {
        // Given: Mock receipts with NEW status
        val expectedReceipts = listOf(
            Receipt(
                id = 1L,
                userId = 1L,
                billId = null,
                filename = "receipt1.pdf",
                filePath = "/path/receipt1.pdf",
                uploadDate = LocalDateTime.now(),
                checksum = "checksum1",
                status = ItemStatus.NEW
            ),
            Receipt(
                id = 2L,
                userId = 2L,
                billId = null,
                filename = "receipt2.jpg",
                filePath = "/path/receipt2.jpg",
                uploadDate = LocalDateTime.now(),
                checksum = "checksum2",
                status = ItemStatus.NEW
            )
        )

        // Mock JdbcTemplate query response
        whenever(jdbcTemplate.query(any<String>(), any<RowMapper<Receipt>>(), any<String>()))
            .thenReturn(expectedReceipts)

        // When: Calling findByStatus with NEW status
        val result = receiptRepository.findByStatus(ItemStatus.NEW)

        // Then: Should return receipts with NEW status
        assertNotNull(result, "Result should not be null")
        assertEquals(2, result.size, "Should return 2 receipts")
        assertEquals(ItemStatus.NEW, result[0].status, "First receipt should have NEW status")
        assertEquals(ItemStatus.NEW, result[1].status, "Second receipt should have NEW status")
        assertEquals("receipt1.pdf", result[0].filename, "First receipt should match expected filename")
        assertEquals("receipt2.jpg", result[1].filename, "Second receipt should match expected filename")
    }

    /**
     * Test findByStatus method with PROCESSING status
     * Given: Database contains receipts with PROCESSING status
     * When: Calling findByStatus with PROCESSING status
     * Then: Should return only receipts with PROCESSING status
     */
    @Test
    fun `Given receipts with PROCESSING status, when calling findByStatus with PROCESSING, then should return only PROCESSING receipts`() {
        // Given: Mock receipts with PROCESSING status
        val expectedReceipts = listOf(
            Receipt(
                id = 3L,
                userId = 1L,
                billId = 1L,
                filename = "processing.pdf",
                filePath = "/path/processing.pdf",
                uploadDate = LocalDateTime.now(),
                checksum = "checksum3",
                status = ItemStatus.PROCESSING
            )
        )

        // Mock JdbcTemplate query response
        whenever(jdbcTemplate.query(any<String>(), any<RowMapper<Receipt>>(), any<String>()))
            .thenReturn(expectedReceipts)

        // When: Calling findByStatus with PROCESSING status
        val result = receiptRepository.findByStatus(ItemStatus.PROCESSING)

        // Then: Should return receipts with PROCESSING status
        assertNotNull(result, "Result should not be null")
        assertEquals(1, result.size, "Should return 1 receipt")
        assertEquals(ItemStatus.PROCESSING, result[0].status, "Receipt should have PROCESSING status")
        assertEquals("processing.pdf", result[0].filename, "Receipt should match expected filename")
    }

    /**
     * Test findByStatus method returns empty list when no receipts match status
     * Given: Database contains no receipts with specified status
     * When: Calling findByStatus with REJECTED status
     * Then: Should return empty list
     */
    @Test
    fun `Given no receipts with REJECTED status, when calling findByStatus with REJECTED, then should return empty list`() {
        // Given: Mock empty response
        whenever(jdbcTemplate.query(any<String>(), any<RowMapper<Receipt>>(), any<String>()))
            .thenReturn(emptyList())

        // When: Calling findByStatus with REJECTED status
        val result = receiptRepository.findByStatus(ItemStatus.REJECTED)

        // Then: Should return empty list
        assertNotNull(result, "Result should not be null")
        assertEquals(0, result.size, "Should return empty list")
    }

    /**
     * Test findByUserIdAndStatus method retrieves receipts for specific user and status
     * Given: Database contains receipts for multiple users and statuses
     * When: Calling findByUserIdAndStatus with specific user and status
     * Then: Should return only receipts matching both user and status
     */
    @Test
    fun `Given receipts for multiple users and statuses, when calling findByUserIdAndStatus, then should return only matching receipts`() {
        // Given: Mock receipts for specific user and status
        val userId = 1L
        val status = ItemStatus.APPROVED
        val expectedReceipts = listOf(
            Receipt(
                id = 4L,
                userId = userId,
                billId = 2L,
                filename = "approved1.pdf",
                filePath = "/path/approved1.pdf",
                uploadDate = LocalDateTime.now(),
                checksum = "checksum4",
                status = status
            ),
            Receipt(
                id = 5L,
                userId = userId,
                billId = 3L,
                filename = "approved2.jpg",
                filePath = "/path/approved2.jpg",
                uploadDate = LocalDateTime.now(),
                checksum = "checksum5",
                status = status
            )
        )

        // Mock JdbcTemplate query response
        whenever(jdbcTemplate.query(any<String>(), any<RowMapper<Receipt>>(), any<Long>(), any<String>()))
            .thenReturn(expectedReceipts)

        // When: Calling findByUserIdAndStatus with user 1 and APPROVED status
        val result = receiptRepository.findByUserIdAndStatus(userId, status)

        // Then: Should return receipts for user 1 with APPROVED status
        assertNotNull(result, "Result should not be null")
        assertEquals(2, result.size, "Should return 2 receipts")
        assertEquals(userId, result[0].userId, "First receipt should belong to user 1")
        assertEquals(userId, result[1].userId, "Second receipt should belong to user 1")
        assertEquals(status, result[0].status, "First receipt should have APPROVED status")
        assertEquals(status, result[1].status, "Second receipt should have APPROVED status")
        assertEquals("approved1.pdf", result[0].filename, "First receipt should match expected filename")
        assertEquals("approved2.jpg", result[1].filename, "Second receipt should match expected filename")
    }

    /**
     * Test findByUserIdAndStatus method returns empty list when no receipts match criteria
     * Given: Database contains no receipts for specified user and status combination
     * When: Calling findByUserIdAndStatus with user and status
     * Then: Should return empty list
     */
    @Test
    fun `Given no receipts for user and status combination, when calling findByUserIdAndStatus, then should return empty list`() {
        // Given: Mock empty response
        whenever(jdbcTemplate.query(any<String>(), any<RowMapper<Receipt>>(), any<Long>(), any<String>()))
            .thenReturn(emptyList())

        // When: Calling findByUserIdAndStatus with user 999 and NEW status
        val result = receiptRepository.findByUserIdAndStatus(999L, ItemStatus.NEW)

        // Then: Should return empty list
        assertNotNull(result, "Result should not be null")
        assertEquals(0, result.size, "Should return empty list")
    }

    /**
     * Test findByUserIdAndStatus method with different status values
     * Given: Database contains receipts with different statuses for same user
     * When: Calling findByUserIdAndStatus with each status
     * Then: Should return only receipts with matching status for that user
     */
    @Test
    fun `Given receipts with different statuses for same user, when calling findByUserIdAndStatus with each status, then should return only matching receipts`() {
        // Given: Mock receipts with different statuses for user 2
        val userId = 2L
        val newReceipts = listOf(
            Receipt(
                id = 6L,
                userId = userId,
                billId = null,
                filename = "new.pdf",
                filePath = "/path/new.pdf",
                uploadDate = LocalDateTime.now(),
                checksum = "checksum6",
                status = ItemStatus.NEW
            )
        )

        val processingReceipts = listOf(
            Receipt(
                id = 7L,
                userId = userId,
                billId = 4L,
                filename = "processing.pdf",
                filePath = "/path/processing.pdf",
                uploadDate = LocalDateTime.now(),
                checksum = "checksum7",
                status = ItemStatus.PROCESSING
            )
        )

        // Mock JdbcTemplate query responses for different statuses
        whenever(jdbcTemplate.query(any<String>(), any<RowMapper<Receipt>>(), any<Long>(), any<String>()))
            .thenReturn(newReceipts, processingReceipts)

        // When: Calling findByUserIdAndStatus with NEW status
        val newResult = receiptRepository.findByUserIdAndStatus(userId, ItemStatus.NEW)

        // Then: Should return only NEW receipts for user 2
        assertNotNull(newResult, "NEW result should not be null")
        assertEquals(1, newResult.size, "Should return 1 NEW receipt")
        assertEquals(ItemStatus.NEW, newResult[0].status, "Receipt should have NEW status")
        assertEquals("new.pdf", newResult[0].filename, "Receipt should match expected filename")

        // When: Calling findByUserIdAndStatus with PROCESSING status
        val processingResult = receiptRepository.findByUserIdAndStatus(userId, ItemStatus.PROCESSING)

        // Then: Should return only PROCESSING receipts for user 2
        assertNotNull(processingResult, "PROCESSING result should not be null")
        assertEquals(1, processingResult.size, "Should return 1 PROCESSING receipt")
        assertEquals(ItemStatus.PROCESSING, processingResult[0].status, "Receipt should have PROCESSING status")
        assertEquals("processing.pdf", processingResult[0].filename, "Receipt should match expected filename")
    }
}