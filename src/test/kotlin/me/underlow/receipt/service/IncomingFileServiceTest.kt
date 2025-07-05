package me.underlow.receipt.service

import me.underlow.receipt.model.ItemStatus
import me.underlow.receipt.model.IncomingFile
import me.underlow.receipt.model.User
import me.underlow.receipt.repository.IncomingFileRepository
import me.underlow.receipt.repository.UserRepository
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.time.LocalDateTime
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Unit tests for IncomingFileService specifically testing the status counts bug fix
 */
class IncomingFileServiceTest {

    private val incomingFileRepository = mock<IncomingFileRepository>()
    private val userRepository = mock<UserRepository>()
    private val incomingFileOcrService = mock<IncomingFileOcrService>()
    private val fileDispatchService = mock<FileDispatchService>()
    private val incomingFileService = IncomingFileService(incomingFileRepository, userRepository, incomingFileOcrService, fileDispatchService)

    /**
     * Test that reproduces the original bug where missing status counts would cause null pointer issues
     * Given: User with no files (empty list)
     * When: Getting file statistics
     * Then: Should return all statuses with count 0
     */
    @Test
    fun `Given user with no files, when getting statistics, then should return all statuses with zero counts`() {
        // Given: User with no files
        val userEmail = "nofiles@example.com"
        val user = User(id = 1L, email = userEmail, name = "No Files User")
        
        whenever(userRepository.findByEmail(userEmail)).thenReturn(user)
        whenever(incomingFileRepository.findByUserId(1L)).thenReturn(emptyList())

        // When: Getting file statistics
        val statistics = incomingFileService.getFileStatistics(userEmail)

        // Then: All statuses should be present with count 0
        assertEquals(3, statistics.size, "Should have entries for all 3 status types")
        assertEquals(0, statistics[ItemStatus.NEW], "NEW count should be 0")
        assertEquals(0, statistics[ItemStatus.APPROVED], "APPROVED count should be 0")
        assertEquals(0, statistics[ItemStatus.REJECTED], "REJECTED count should be 0")
        
        // And: All ItemStatus values should be present as keys
        ItemStatus.values().forEach { status ->
            assertTrue(statistics.containsKey(status), "Statistics should contain key for $status")
        }
    }

    /**
     * Test the bug scenario where user has files with only one status
     * Given: User with only PENDING files
     * When: Getting file statistics
     * Then: Should return PENDING count > 0 and other statuses with count 0
     */
    @Test
    fun `Given user with only pending files, when getting statistics, then should return correct counts with zeros for missing statuses`() {
        // Given: User with only pending files
        val userEmail = "pendingonly@example.com"
        val user = User(id = 2L, email = userEmail, name = "Pending Only User")
        val pendingFiles = listOf(
            IncomingFile(1L, "file1.pdf", "/path/file1.pdf", LocalDateTime.now(), ItemStatus.NEW, "checksum1", 2L),
            IncomingFile(2L, "file2.jpg", "/path/file2.jpg", LocalDateTime.now(), ItemStatus.NEW, "checksum2", 2L)
        )
        
        whenever(userRepository.findByEmail(userEmail)).thenReturn(user)
        whenever(incomingFileRepository.findByUserId(2L)).thenReturn(pendingFiles)

        // When: Getting file statistics
        val statistics = incomingFileService.getFileStatistics(userEmail)

        // Then: Should have correct counts
        assertEquals(3, statistics.size, "Should have entries for all 3 status types")
        assertEquals(2, statistics[ItemStatus.NEW], "NEW count should be 2")
        assertEquals(0, statistics[ItemStatus.APPROVED], "APPROVED count should be 0")
        assertEquals(0, statistics[ItemStatus.REJECTED], "REJECTED count should be 0")
    }

    /**
     * Test with mixed status files to ensure counts are correct
     * Given: User with files in various statuses
     * When: Getting file statistics
     * Then: Should return correct counts for each status
     */
    @Test
    fun `Given user with mixed status files, when getting statistics, then should return correct counts for each status`() {
        // Given: User with files in various statuses
        val userEmail = "mixed@example.com"
        val user = User(id = 3L, email = userEmail, name = "Mixed Status User")
        val mixedFiles = listOf(
            IncomingFile(1L, "new.pdf", "/path/new.pdf", LocalDateTime.now(), ItemStatus.NEW, "checksum1", 3L),
            IncomingFile(2L, "new2.jpg", "/path/new2.jpg", LocalDateTime.now(), ItemStatus.NEW, "checksum2", 3L),
            IncomingFile(3L, "approved1.png", "/path/approved1.png", LocalDateTime.now(), ItemStatus.APPROVED, "checksum3", 3L),
            IncomingFile(4L, "approved2.pdf", "/path/approved2.pdf", LocalDateTime.now(), ItemStatus.APPROVED, "checksum4", 3L),
            IncomingFile(5L, "rejected.jpg", "/path/rejected.jpg", LocalDateTime.now(), ItemStatus.REJECTED, "checksum5", 3L)
        )
        
        whenever(userRepository.findByEmail(userEmail)).thenReturn(user)
        whenever(incomingFileRepository.findByUserId(3L)).thenReturn(mixedFiles)

        // When: Getting file statistics
        val statistics = incomingFileService.getFileStatistics(userEmail)

        // Then: Should have correct counts for each status
        assertEquals(3, statistics.size, "Should have entries for all 3 status types")
        assertEquals(2, statistics[ItemStatus.NEW], "NEW count should be 2")
        assertEquals(2, statistics[ItemStatus.APPROVED], "APPROVED count should be 2")
        assertEquals(1, statistics[ItemStatus.REJECTED], "REJECTED count should be 1")
    }

    /**
     * Test pagination functionality works correctly with different status counts
     * Given: User with files and pagination parameters
     * When: Getting paginated files
     * Then: Should return correct page and total count
     */
    @Test
    fun `Given user with files, when getting paginated results, then should return correct page and total`() {
        // Given: User with multiple files
        val userEmail = "paginated@example.com"
        val user = User(id = 4L, email = userEmail, name = "Paginated User")
        val files = (1..10).map { i ->
            IncomingFile(i.toLong(), "file$i.pdf", "/path/file$i.pdf", LocalDateTime.now(), ItemStatus.NEW, "checksum$i", 4L)
        }
        
        whenever(userRepository.findByEmail(userEmail)).thenReturn(user)
        whenever(incomingFileRepository.findByUserId(4L)).thenReturn(files)

        // When: Getting first page with size 3
        val (paginatedFiles, totalCount) = incomingFileService.findByUserEmailWithPagination(
            userEmail, null, 0, 3
        )

        // Then: Should return correct pagination results
        assertEquals(3, paginatedFiles.size, "Should return 3 files for page size 3")
        assertEquals(10L, totalCount, "Total count should be 10")
        assertEquals("file10.pdf", paginatedFiles[0].filename, "Should be sorted by upload date desc")
    }
}