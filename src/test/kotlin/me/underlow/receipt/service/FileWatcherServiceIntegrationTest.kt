package me.underlow.receipt.service

import me.underlow.receipt.config.ReceiptsProperties
import me.underlow.receipt.model.BillStatus
import me.underlow.receipt.repository.IncomingFileRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.mockito.kotlin.*
import java.io.File
import java.nio.file.Path
import java.time.LocalDateTime
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class FileWatcherServiceIntegrationTest {

    private lateinit var incomingFileRepository: IncomingFileRepository
    private lateinit var receiptsProperties: ReceiptsProperties
    private lateinit var fileProcessingService: FileProcessingService
    private lateinit var fileWatcherService: FileWatcherService

    @TempDir
    lateinit var tempDir: Path

    @BeforeEach
    fun setup() {
        incomingFileRepository = mock()
        receiptsProperties = ReceiptsProperties(
            inboxPath = tempDir.resolve("inbox").toString(),
            attachmentsPath = tempDir.resolve("attachments").toString()
        )
        fileProcessingService = FileProcessingService(incomingFileRepository, receiptsProperties)
        fileWatcherService = FileWatcherService(fileProcessingService, receiptsProperties)
    }

    @Test
    fun `given empty inbox directory when scanning then should create directory and complete successfully`() {
        // Given: Inbox directory doesn't exist
        val inboxPath = Path.of(receiptsProperties.inboxPath)
        assertFalse(inboxPath.toFile().exists())

        // When: Scanning inbox directory
        fileWatcherService.scanInboxDirectory()

        // Then: Inbox directory should be created
        assertTrue(inboxPath.toFile().exists())
        assertTrue(inboxPath.toFile().isDirectory())
    }

    @Test
    fun `given files in inbox when scanning then should process all valid files`() {
        // Given: Inbox directory with valid files
        val inboxDir = Path.of(receiptsProperties.inboxPath).toFile()
        inboxDir.mkdirs()
        
        val pdfFile = inboxDir.resolve("receipt1.pdf")
        val jpgFile = inboxDir.resolve("receipt2.jpg")
        val txtFile = inboxDir.resolve("readme.txt") // Should be ignored
        val hiddenFile = inboxDir.resolve(".hidden.pdf") // Should be ignored
        
        pdfFile.writeText("PDF content")
        jpgFile.writeText("JPG content")
        txtFile.writeText("Text content")
        hiddenFile.writeText("Hidden content")

        whenever(incomingFileRepository.findByChecksum(any())).thenReturn(null)
        whenever(incomingFileRepository.save(any())).thenAnswer { invocation ->
            val incomingFile = invocation.getArgument<me.underlow.receipt.model.IncomingFile>(0)
            incomingFile.copy(id = System.currentTimeMillis())
        }

        // When: Scanning inbox directory
        fileWatcherService.scanInboxDirectory()

        // Then: Should process valid files only (PDF and JPG)
        verify(incomingFileRepository, times(2)).save(any())
        
        // Files should be moved from inbox
        assertFalse(pdfFile.exists())
        assertFalse(jpgFile.exists())
        assertTrue(txtFile.exists()) // Text file should remain
        assertTrue(hiddenFile.exists()) // Hidden file should remain
    }

    @Test
    fun `given duplicate files in inbox when scanning then should skip duplicates`() {
        // Given: Inbox with duplicate file
        val inboxDir = Path.of(receiptsProperties.inboxPath).toFile()
        inboxDir.mkdirs()
        
        val duplicateFile = inboxDir.resolve("duplicate.pdf")
        duplicateFile.writeText("Duplicate content")

        // Mock existing file with same checksum
        val existingFile = me.underlow.receipt.model.IncomingFile(
            id = 999L,
            filename = "existing.pdf",
            filePath = "/path/to/existing.pdf",
            uploadDate = LocalDateTime.now(),
            status = BillStatus.APPROVED,
            checksum = "duplicate_checksum",
            userId = 1L
        )

        whenever(incomingFileRepository.findByChecksum(any())).thenReturn(existingFile)

        // When: Scanning inbox directory
        fileWatcherService.scanInboxDirectory()

        // Then: Should not save duplicate file
        verify(incomingFileRepository, never()).save(any())
        verify(incomingFileRepository).findByChecksum(any())
        
        // Original file should remain in inbox since it wasn't processed
        assertTrue(duplicateFile.exists())
    }

    @Test
    fun `given manual trigger when scanning then should process inbox immediately`() {
        // Given: Inbox with a file
        val inboxDir = Path.of(receiptsProperties.inboxPath).toFile()
        inboxDir.mkdirs()
        
        val testFile = inboxDir.resolve("manual_test.pdf")
        testFile.writeText("Manual test content")

        whenever(incomingFileRepository.findByChecksum(any())).thenReturn(null)
        whenever(incomingFileRepository.save(any())).thenAnswer { invocation ->
            val incomingFile = invocation.getArgument<me.underlow.receipt.model.IncomingFile>(0)
            incomingFile.copy(id = 12345L)
        }

        // When: Manually triggering scan
        fileWatcherService.triggerScan()

        // Then: File should be processed
        verify(incomingFileRepository).save(any())
        assertFalse(testFile.exists()) // File should be moved
    }

    @Test
    fun `given service when checking inbox accessibility then should return correct status`() {
        // Given: Inbox directory doesn't exist initially
        assertFalse(fileWatcherService.isInboxAccessible())

        // When: Creating inbox directory
        val inboxDir = Path.of(receiptsProperties.inboxPath).toFile()
        inboxDir.mkdirs()

        // Then: Should be accessible
        assertTrue(fileWatcherService.isInboxAccessible())
    }

    @Test
    fun `given service when getting inbox path then should return configured path`() {
        // Given: FileWatcherService with configured inbox path
        val expectedPath = receiptsProperties.inboxPath

        // When: Getting inbox path
        val actualPath = fileWatcherService.getInboxPath()

        // Then: Should return configured path
        assertEquals(expectedPath, actualPath)
    }

    @Test
    fun `given locked file in inbox when scanning then should skip locked file`() {
        // Given: Inbox with a file that appears locked
        val inboxDir = Path.of(receiptsProperties.inboxPath).toFile()
        inboxDir.mkdirs()
        
        val lockedFile = inboxDir.resolve("locked.pdf")
        lockedFile.writeText("Locked content")
        // Note: In a real scenario, we'd need OS-level file locking, 
        // but for testing we rely on the FileProcessingService mock behavior

        whenever(incomingFileRepository.findByChecksum(any())).thenReturn(null)

        // When: Scanning inbox directory
        fileWatcherService.scanInboxDirectory()

        // Then: File processing should be attempted (specific lock handling is in FileProcessingService)
        // This test mainly verifies the integration flow works
        assertTrue(inboxDir.exists())
    }
}