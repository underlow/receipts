package me.underlow.receipt.repository

import me.underlow.receipt.model.Bill
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
 * Unit tests for BillRepository status-based query methods
 * Tests the new findByStatus() and findByUserIdAndStatus() methods
 */
class BillRepositoryTest {

    private lateinit var jdbcTemplate: JdbcTemplate
    private lateinit var billRepository: BillRepository

    @BeforeEach
    fun setup() {
        jdbcTemplate = mock()
        billRepository = BillRepositoryImpl(jdbcTemplate)
    }

    /**
     * Test findByStatus method retrieves bills with specific status
     * Given: Database contains bills with different statuses
     * When: Calling findByStatus with NEW status
     * Then: Should return only bills with NEW status
     */
    @Test
    fun `Given bills with different statuses, when calling findByStatus with NEW, then should return only NEW bills`() {
        // Given: Mock bills with NEW status
        val expectedBills = listOf(
            Bill(
                id = 1L,
                filename = "bill1.pdf",
                filePath = "/path/bill1.pdf",
                uploadDate = LocalDateTime.now(),
                status = ItemStatus.NEW,
                ocrRawJson = null,
                extractedAmount = null,
                extractedDate = null,
                extractedProvider = null,
                userId = 1L,
                checksum = "checksum1",
                originalIncomingFileId = 1L
            ),
            Bill(
                id = 2L,
                filename = "bill2.jpg",
                filePath = "/path/bill2.jpg",
                uploadDate = LocalDateTime.now(),
                status = ItemStatus.NEW,
                ocrRawJson = null,
                extractedAmount = null,
                extractedDate = null,
                extractedProvider = null,
                userId = 2L,
                checksum = "checksum2",
                originalIncomingFileId = 2L
            )
        )

        // Mock JdbcTemplate query response
        whenever(jdbcTemplate.query(any<String>(), any<RowMapper<Bill>>(), any<String>()))
            .thenReturn(expectedBills)

        // When: Calling findByStatus with NEW status
        val result = billRepository.findByStatus(ItemStatus.NEW)

        // Then: Should return bills with NEW status
        assertNotNull(result, "Result should not be null")
        assertEquals(2, result.size, "Should return 2 bills")
        assertEquals(ItemStatus.NEW, result[0].status, "First bill should have NEW status")
        assertEquals(ItemStatus.NEW, result[1].status, "Second bill should have NEW status")
        assertEquals("bill1.pdf", result[0].filename, "First bill should match expected filename")
        assertEquals("bill2.jpg", result[1].filename, "Second bill should match expected filename")
    }

    /**
     * Test findByStatus method with PROCESSING status
     * Given: Database contains bills with PROCESSING status
     * When: Calling findByStatus with PROCESSING status
     * Then: Should return only bills with PROCESSING status
     */
    @Test
    fun `Given bills with PROCESSING status, when calling findByStatus with PROCESSING, then should return only PROCESSING bills`() {
        // Given: Mock bills with PROCESSING status
        val expectedBills = listOf(
            Bill(
                id = 3L,
                filename = "processing.pdf",
                filePath = "/path/processing.pdf",
                uploadDate = LocalDateTime.now(),
                status = ItemStatus.PROCESSING,
                ocrRawJson = "{\"amount\": 100.50}",
                extractedAmount = 100.50,
                extractedDate = null,
                extractedProvider = "Test Provider",
                userId = 1L,
                checksum = "checksum3",
                originalIncomingFileId = 3L
            )
        )

        // Mock JdbcTemplate query response
        whenever(jdbcTemplate.query(any<String>(), any<RowMapper<Bill>>(), any<String>()))
            .thenReturn(expectedBills)

        // When: Calling findByStatus with PROCESSING status
        val result = billRepository.findByStatus(ItemStatus.PROCESSING)

        // Then: Should return bills with PROCESSING status
        assertNotNull(result, "Result should not be null")
        assertEquals(1, result.size, "Should return 1 bill")
        assertEquals(ItemStatus.PROCESSING, result[0].status, "Bill should have PROCESSING status")
        assertEquals("processing.pdf", result[0].filename, "Bill should match expected filename")
        assertEquals(100.50, result[0].extractedAmount, "Bill should have extracted amount")
    }

    /**
     * Test findByStatus method returns empty list when no bills match status
     * Given: Database contains no bills with specified status
     * When: Calling findByStatus with REJECTED status
     * Then: Should return empty list
     */
    @Test
    fun `Given no bills with REJECTED status, when calling findByStatus with REJECTED, then should return empty list`() {
        // Given: Mock empty response
        whenever(jdbcTemplate.query(any<String>(), any<RowMapper<Bill>>(), any<String>()))
            .thenReturn(emptyList())

        // When: Calling findByStatus with REJECTED status
        val result = billRepository.findByStatus(ItemStatus.REJECTED)

        // Then: Should return empty list
        assertNotNull(result, "Result should not be null")
        assertEquals(0, result.size, "Should return empty list")
    }

    /**
     * Test findByUserIdAndStatus method retrieves bills for specific user and status
     * Given: Database contains bills for multiple users and statuses
     * When: Calling findByUserIdAndStatus with specific user and status
     * Then: Should return only bills matching both user and status
     */
    @Test
    fun `Given bills for multiple users and statuses, when calling findByUserIdAndStatus, then should return only matching bills`() {
        // Given: Mock bills for specific user and status
        val userId = 1L
        val status = ItemStatus.APPROVED
        val expectedBills = listOf(
            Bill(
                id = 4L,
                filename = "approved1.pdf",
                filePath = "/path/approved1.pdf",
                uploadDate = LocalDateTime.now(),
                status = status,
                ocrRawJson = "{\"amount\": 200.00}",
                extractedAmount = 200.00,
                extractedDate = null,
                extractedProvider = "Approved Provider",
                userId = userId,
                checksum = "checksum4",
                originalIncomingFileId = 4L
            ),
            Bill(
                id = 5L,
                filename = "approved2.jpg",
                filePath = "/path/approved2.jpg",
                uploadDate = LocalDateTime.now(),
                status = status,
                ocrRawJson = "{\"amount\": 150.75}",
                extractedAmount = 150.75,
                extractedDate = null,
                extractedProvider = "Another Provider",
                userId = userId,
                checksum = "checksum5",
                originalIncomingFileId = 5L
            )
        )

        // Mock JdbcTemplate query response
        whenever(jdbcTemplate.query(any<String>(), any<RowMapper<Bill>>(), any<Long>(), any<String>()))
            .thenReturn(expectedBills)

        // When: Calling findByUserIdAndStatus with user 1 and APPROVED status
        val result = billRepository.findByUserIdAndStatus(userId, status)

        // Then: Should return bills for user 1 with APPROVED status
        assertNotNull(result, "Result should not be null")
        assertEquals(2, result.size, "Should return 2 bills")
        assertEquals(userId, result[0].userId, "First bill should belong to user 1")
        assertEquals(userId, result[1].userId, "Second bill should belong to user 1")
        assertEquals(status, result[0].status, "First bill should have APPROVED status")
        assertEquals(status, result[1].status, "Second bill should have APPROVED status")
        assertEquals("approved1.pdf", result[0].filename, "First bill should match expected filename")
        assertEquals("approved2.jpg", result[1].filename, "Second bill should match expected filename")
    }

    /**
     * Test findByUserIdAndStatus method returns empty list when no bills match criteria
     * Given: Database contains no bills for specified user and status combination
     * When: Calling findByUserIdAndStatus with user and status
     * Then: Should return empty list
     */
    @Test
    fun `Given no bills for user and status combination, when calling findByUserIdAndStatus, then should return empty list`() {
        // Given: Mock empty response
        whenever(jdbcTemplate.query(any<String>(), any<RowMapper<Bill>>(), any<Long>(), any<String>()))
            .thenReturn(emptyList())

        // When: Calling findByUserIdAndStatus with user 999 and NEW status
        val result = billRepository.findByUserIdAndStatus(999L, ItemStatus.NEW)

        // Then: Should return empty list
        assertNotNull(result, "Result should not be null")
        assertEquals(0, result.size, "Should return empty list")
    }

    /**
     * Test findByUserIdAndStatus method with different status values
     * Given: Database contains bills with different statuses for same user
     * When: Calling findByUserIdAndStatus with each status
     * Then: Should return only bills with matching status for that user
     */
    @Test
    fun `Given bills with different statuses for same user, when calling findByUserIdAndStatus with each status, then should return only matching bills`() {
        // Given: Mock bills with different statuses for user 2
        val userId = 2L
        val newBills = listOf(
            Bill(
                id = 6L,
                filename = "new.pdf",
                filePath = "/path/new.pdf",
                uploadDate = LocalDateTime.now(),
                status = ItemStatus.NEW,
                ocrRawJson = null,
                extractedAmount = null,
                extractedDate = null,
                extractedProvider = null,
                userId = userId,
                checksum = "checksum6",
                originalIncomingFileId = 6L
            )
        )

        val processingBills = listOf(
            Bill(
                id = 7L,
                filename = "processing.pdf",
                filePath = "/path/processing.pdf",
                uploadDate = LocalDateTime.now(),
                status = ItemStatus.PROCESSING,
                ocrRawJson = "{\"amount\": 75.25}",
                extractedAmount = 75.25,
                extractedDate = null,
                extractedProvider = "Processing Provider",
                userId = userId,
                checksum = "checksum7",
                originalIncomingFileId = 7L
            )
        )

        // Mock JdbcTemplate query responses for different statuses
        whenever(jdbcTemplate.query(any<String>(), any<RowMapper<Bill>>(), any<Long>(), any<String>()))
            .thenReturn(newBills, processingBills)

        // When: Calling findByUserIdAndStatus with NEW status
        val newResult = billRepository.findByUserIdAndStatus(userId, ItemStatus.NEW)

        // Then: Should return only NEW bills for user 2
        assertNotNull(newResult, "NEW result should not be null")
        assertEquals(1, newResult.size, "Should return 1 NEW bill")
        assertEquals(ItemStatus.NEW, newResult[0].status, "Bill should have NEW status")
        assertEquals("new.pdf", newResult[0].filename, "Bill should match expected filename")

        // When: Calling findByUserIdAndStatus with PROCESSING status
        val processingResult = billRepository.findByUserIdAndStatus(userId, ItemStatus.PROCESSING)

        // Then: Should return only PROCESSING bills for user 2
        assertNotNull(processingResult, "PROCESSING result should not be null")
        assertEquals(1, processingResult.size, "Should return 1 PROCESSING bill")
        assertEquals(ItemStatus.PROCESSING, processingResult[0].status, "Bill should have PROCESSING status")
        assertEquals("processing.pdf", processingResult[0].filename, "Bill should match expected filename")
    }

    /**
     * Test findByStatus method with all possible status values
     * Given: Database contains bills with all possible status values
     * When: Calling findByStatus with each status value
     * Then: Should return correct bills for each status
     */
    @Test
    fun `Given bills with all status values, when calling findByStatus with each status, then should return correct bills`() {
        // Given: Mock bills with APPROVED status
        val approvedBills = listOf(
            Bill(
                id = 8L,
                filename = "approved.pdf",
                filePath = "/path/approved.pdf",
                uploadDate = LocalDateTime.now(),
                status = ItemStatus.APPROVED,
                ocrRawJson = "{\"amount\": 300.00}",
                extractedAmount = 300.00,
                extractedDate = null,
                extractedProvider = "Final Provider",
                userId = 3L,
                checksum = "checksum8",
                originalIncomingFileId = 8L
            )
        )

        val rejectedBills = listOf(
            Bill(
                id = 9L,
                filename = "rejected.pdf",
                filePath = "/path/rejected.pdf",
                uploadDate = LocalDateTime.now(),
                status = ItemStatus.REJECTED,
                ocrRawJson = null,
                extractedAmount = null,
                extractedDate = null,
                extractedProvider = null,
                userId = 3L,
                checksum = "checksum9",
                originalIncomingFileId = 9L
            )
        )

        // Mock JdbcTemplate query responses for different statuses
        whenever(jdbcTemplate.query(any<String>(), any<RowMapper<Bill>>(), any<String>()))
            .thenReturn(approvedBills, rejectedBills)

        // When: Calling findByStatus with APPROVED status
        val approvedResult = billRepository.findByStatus(ItemStatus.APPROVED)

        // Then: Should return only APPROVED bills
        assertNotNull(approvedResult, "APPROVED result should not be null")
        assertEquals(1, approvedResult.size, "Should return 1 APPROVED bill")
        assertEquals(ItemStatus.APPROVED, approvedResult[0].status, "Bill should have APPROVED status")
        assertEquals("approved.pdf", approvedResult[0].filename, "Bill should match expected filename")

        // When: Calling findByStatus with REJECTED status
        val rejectedResult = billRepository.findByStatus(ItemStatus.REJECTED)

        // Then: Should return only REJECTED bills
        assertNotNull(rejectedResult, "REJECTED result should not be null")
        assertEquals(1, rejectedResult.size, "Should return 1 REJECTED bill")
        assertEquals(ItemStatus.REJECTED, rejectedResult[0].status, "Bill should have REJECTED status")
        assertEquals("rejected.pdf", rejectedResult[0].filename, "Bill should match expected filename")
    }
}