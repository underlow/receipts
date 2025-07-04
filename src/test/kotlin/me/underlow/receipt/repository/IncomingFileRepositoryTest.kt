package me.underlow.receipt.repository

import me.underlow.receipt.model.IncomingFile
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
 * Unit tests for IncomingFileRepository status-based query methods
 * Tests the findByStatus() method (already existed) and new findByUserIdAndStatus() method
 */
class IncomingFileRepositoryTest {

    private lateinit var jdbcTemplate: JdbcTemplate
    private lateinit var incomingFileRepository: IncomingFileRepository

    @BeforeEach
    fun setup() {
        jdbcTemplate = mock()
        incomingFileRepository = IncomingFileRepositoryImpl(jdbcTemplate)
    }

    /**
     * Test findByStatus method retrieves incoming files with specific status
     * Given: Database contains incoming files with different statuses
     * When: Calling findByStatus with NEW status
     * Then: Should return only incoming files with NEW status
     */
    @Test
    fun `Given incoming files with different statuses, when calling findByStatus with NEW, then should return only NEW files`() {
        // Given: Mock incoming files with NEW status
        val expectedFiles = listOf(
            IncomingFile(
                id = 1L,
                filename = "file1.pdf",
                filePath = "/path/file1.pdf",
                uploadDate = LocalDateTime.now(),
                status = ItemStatus.NEW,
                checksum = "checksum1",
                userId = 1L,
                ocrRawJson = null,
                extractedAmount = null,
                extractedDate = null,
                extractedProvider = null
            ),
            IncomingFile(
                id = 2L,
                filename = "file2.jpg",
                filePath = "/path/file2.jpg",
                uploadDate = LocalDateTime.now(),
                status = ItemStatus.NEW,
                checksum = "checksum2",
                userId = 2L,
                ocrRawJson = null,
                extractedAmount = null,
                extractedDate = null,
                extractedProvider = null
            )
        )

        // Mock JdbcTemplate query response
        whenever(jdbcTemplate.query(any<String>(), any<RowMapper<IncomingFile>>(), any<String>()))
            .thenReturn(expectedFiles)

        // When: Calling findByStatus with NEW status
        val result = incomingFileRepository.findByStatus(ItemStatus.NEW)

        // Then: Should return incoming files with NEW status
        assertNotNull(result, "Result should not be null")
        assertEquals(2, result.size, "Should return 2 incoming files")
        assertEquals(ItemStatus.NEW, result[0].status, "First file should have NEW status")
        assertEquals(ItemStatus.NEW, result[1].status, "Second file should have NEW status")
        assertEquals("file1.pdf", result[0].filename, "First file should match expected filename")
        assertEquals("file2.jpg", result[1].filename, "Second file should match expected filename")
    }

    /**
     * Test findByStatus method with PROCESSING status
     * Given: Database contains incoming files with PROCESSING status
     * When: Calling findByStatus with PROCESSING status
     * Then: Should return only incoming files with PROCESSING status
     */
    @Test
    fun `Given incoming files with PROCESSING status, when calling findByStatus with PROCESSING, then should return only PROCESSING files`() {
        // Given: Mock incoming files with PROCESSING status
        val expectedFiles = listOf(
            IncomingFile(
                id = 3L,
                filename = "processing.pdf",
                filePath = "/path/processing.pdf",
                uploadDate = LocalDateTime.now(),
                status = ItemStatus.PROCESSING,
                checksum = "checksum3",
                userId = 1L,
                ocrRawJson = "{\"amount\": 100.50}",
                extractedAmount = 100.50,
                extractedDate = null,
                extractedProvider = "Test Provider"
            )
        )

        // Mock JdbcTemplate query response
        whenever(jdbcTemplate.query(any<String>(), any<RowMapper<IncomingFile>>(), any<String>()))
            .thenReturn(expectedFiles)

        // When: Calling findByStatus with PROCESSING status
        val result = incomingFileRepository.findByStatus(ItemStatus.PROCESSING)

        // Then: Should return incoming files with PROCESSING status
        assertNotNull(result, "Result should not be null")
        assertEquals(1, result.size, "Should return 1 incoming file")
        assertEquals(ItemStatus.PROCESSING, result[0].status, "File should have PROCESSING status")
        assertEquals("processing.pdf", result[0].filename, "File should match expected filename")
        assertEquals(100.50, result[0].extractedAmount, "File should have extracted amount")
    }

    /**
     * Test findByUserIdAndStatus method retrieves incoming files for specific user and status
     * Given: Database contains incoming files for multiple users and statuses
     * When: Calling findByUserIdAndStatus with specific user and status
     * Then: Should return only incoming files matching both user and status
     */
    @Test
    fun `Given incoming files for multiple users and statuses, when calling findByUserIdAndStatus, then should return only matching files`() {
        // Given: Mock incoming files for specific user and status
        val userId = 1L
        val status = ItemStatus.APPROVED
        val expectedFiles = listOf(
            IncomingFile(
                id = 4L,
                filename = "approved1.pdf",
                filePath = "/path/approved1.pdf",
                uploadDate = LocalDateTime.now(),
                status = status,
                checksum = "checksum4",
                userId = userId,
                ocrRawJson = "{\"amount\": 200.00}",
                extractedAmount = 200.00,
                extractedDate = null,
                extractedProvider = "Approved Provider"
            ),
            IncomingFile(
                id = 5L,
                filename = "approved2.jpg",
                filePath = "/path/approved2.jpg",
                uploadDate = LocalDateTime.now(),
                status = status,
                checksum = "checksum5",
                userId = userId,
                ocrRawJson = "{\"amount\": 150.75}",
                extractedAmount = 150.75,
                extractedDate = null,
                extractedProvider = "Another Provider"
            )
        )

        // Mock JdbcTemplate query response
        whenever(jdbcTemplate.query(any<String>(), any<RowMapper<IncomingFile>>(), any<Long>(), any<String>()))
            .thenReturn(expectedFiles)

        // When: Calling findByUserIdAndStatus with user 1 and APPROVED status
        val result = incomingFileRepository.findByUserIdAndStatus(userId, status)

        // Then: Should return incoming files for user 1 with APPROVED status
        assertNotNull(result, "Result should not be null")
        assertEquals(2, result.size, "Should return 2 incoming files")
        assertEquals(userId, result[0].userId, "First file should belong to user 1")
        assertEquals(userId, result[1].userId, "Second file should belong to user 1")
        assertEquals(status, result[0].status, "First file should have APPROVED status")
        assertEquals(status, result[1].status, "Second file should have APPROVED status")
        assertEquals("approved1.pdf", result[0].filename, "First file should match expected filename")
        assertEquals("approved2.jpg", result[1].filename, "Second file should match expected filename")
    }

    /**
     * Test findByUserIdAndStatus method returns empty list when no files match criteria
     * Given: Database contains no incoming files for specified user and status combination
     * When: Calling findByUserIdAndStatus with user and status
     * Then: Should return empty list
     */
    @Test
    fun `Given no incoming files for user and status combination, when calling findByUserIdAndStatus, then should return empty list`() {
        // Given: Mock empty response
        whenever(jdbcTemplate.query(any<String>(), any<RowMapper<IncomingFile>>(), any<Long>(), any<String>()))
            .thenReturn(emptyList())

        // When: Calling findByUserIdAndStatus with user 999 and NEW status
        val result = incomingFileRepository.findByUserIdAndStatus(999L, ItemStatus.NEW)

        // Then: Should return empty list
        assertNotNull(result, "Result should not be null")
        assertEquals(0, result.size, "Should return empty list")
    }

    /**
     * Test findByUserIdAndStatus method with different status values
     * Given: Database contains incoming files with different statuses for same user
     * When: Calling findByUserIdAndStatus with each status
     * Then: Should return only incoming files with matching status for that user
     */
    @Test
    fun `Given incoming files with different statuses for same user, when calling findByUserIdAndStatus with each status, then should return only matching files`() {
        // Given: Mock incoming files with different statuses for user 2
        val userId = 2L
        val newFiles = listOf(
            IncomingFile(
                id = 6L,
                filename = "new.pdf",
                filePath = "/path/new.pdf",
                uploadDate = LocalDateTime.now(),
                status = ItemStatus.NEW,
                checksum = "checksum6",
                userId = userId,
                ocrRawJson = null,
                extractedAmount = null,
                extractedDate = null,
                extractedProvider = null
            )
        )

        val processingFiles = listOf(
            IncomingFile(
                id = 7L,
                filename = "processing.pdf",
                filePath = "/path/processing.pdf",
                uploadDate = LocalDateTime.now(),
                status = ItemStatus.PROCESSING,
                checksum = "checksum7",
                userId = userId,
                ocrRawJson = "{\"amount\": 75.25}",
                extractedAmount = 75.25,
                extractedDate = null,
                extractedProvider = "Processing Provider"
            )
        )

        // Mock JdbcTemplate query responses for different statuses
        whenever(jdbcTemplate.query(any<String>(), any<RowMapper<IncomingFile>>(), any<Long>(), any<String>()))
            .thenReturn(newFiles, processingFiles)

        // When: Calling findByUserIdAndStatus with NEW status
        val newResult = incomingFileRepository.findByUserIdAndStatus(userId, ItemStatus.NEW)

        // Then: Should return only NEW incoming files for user 2
        assertNotNull(newResult, "NEW result should not be null")
        assertEquals(1, newResult.size, "Should return 1 NEW incoming file")
        assertEquals(ItemStatus.NEW, newResult[0].status, "File should have NEW status")
        assertEquals("new.pdf", newResult[0].filename, "File should match expected filename")

        // When: Calling findByUserIdAndStatus with PROCESSING status
        val processingResult = incomingFileRepository.findByUserIdAndStatus(userId, ItemStatus.PROCESSING)

        // Then: Should return only PROCESSING incoming files for user 2
        assertNotNull(processingResult, "PROCESSING result should not be null")
        assertEquals(1, processingResult.size, "Should return 1 PROCESSING incoming file")
        assertEquals(ItemStatus.PROCESSING, processingResult[0].status, "File should have PROCESSING status")
        assertEquals("processing.pdf", processingResult[0].filename, "File should match expected filename")
    }

    /**
     * Test findByStatus method with all possible status values
     * Given: Database contains incoming files with all possible status values
     * When: Calling findByStatus with each status value
     * Then: Should return correct incoming files for each status
     */
    @Test
    fun `Given incoming files with all status values, when calling findByStatus with each status, then should return correct files`() {
        // Given: Mock incoming files with APPROVED status
        val approvedFiles = listOf(
            IncomingFile(
                id = 8L,
                filename = "approved.pdf",
                filePath = "/path/approved.pdf",
                uploadDate = LocalDateTime.now(),
                status = ItemStatus.APPROVED,
                checksum = "checksum8",
                userId = 3L,
                ocrRawJson = "{\"amount\": 300.00}",
                extractedAmount = 300.00,
                extractedDate = null,
                extractedProvider = "Final Provider"
            )
        )

        val rejectedFiles = listOf(
            IncomingFile(
                id = 9L,
                filename = "rejected.pdf",
                filePath = "/path/rejected.pdf",
                uploadDate = LocalDateTime.now(),
                status = ItemStatus.REJECTED,
                checksum = "checksum9",
                userId = 3L,
                ocrRawJson = null,
                extractedAmount = null,
                extractedDate = null,
                extractedProvider = null
            )
        )

        // Mock JdbcTemplate query responses for different statuses
        whenever(jdbcTemplate.query(any<String>(), any<RowMapper<IncomingFile>>(), any<String>()))
            .thenReturn(approvedFiles, rejectedFiles)

        // When: Calling findByStatus with APPROVED status
        val approvedResult = incomingFileRepository.findByStatus(ItemStatus.APPROVED)

        // Then: Should return only APPROVED incoming files
        assertNotNull(approvedResult, "APPROVED result should not be null")
        assertEquals(1, approvedResult.size, "Should return 1 APPROVED incoming file")
        assertEquals(ItemStatus.APPROVED, approvedResult[0].status, "File should have APPROVED status")
        assertEquals("approved.pdf", approvedResult[0].filename, "File should match expected filename")

        // When: Calling findByStatus with REJECTED status
        val rejectedResult = incomingFileRepository.findByStatus(ItemStatus.REJECTED)

        // Then: Should return only REJECTED incoming files
        assertNotNull(rejectedResult, "REJECTED result should not be null")
        assertEquals(1, rejectedResult.size, "Should return 1 REJECTED incoming file")
        assertEquals(ItemStatus.REJECTED, rejectedResult[0].status, "File should have REJECTED status")
        assertEquals("rejected.pdf", rejectedResult[0].filename, "File should match expected filename")
    }

    /**
     * Test findByStatus method returns empty list when no files match status
     * Given: Database contains no incoming files with specified status
     * When: Calling findByStatus with status
     * Then: Should return empty list
     */
    @Test
    fun `Given no incoming files with specified status, when calling findByStatus, then should return empty list`() {
        // Given: Mock empty response
        whenever(jdbcTemplate.query(any<String>(), any<RowMapper<IncomingFile>>(), any<String>()))
            .thenReturn(emptyList())

        // When: Calling findByStatus with any status
        val result = incomingFileRepository.findByStatus(ItemStatus.REJECTED)

        // Then: Should return empty list
        assertNotNull(result, "Result should not be null")
        assertEquals(0, result.size, "Should return empty list")
    }
}