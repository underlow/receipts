package me.underlow.receipt.service

import me.underlow.receipt.config.ReceiptsProperties
import me.underlow.receipt.model.BillStatus
import me.underlow.receipt.model.IncomingFile
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
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class FileProcessingServiceTest {

    private lateinit var incomingFileRepository: IncomingFileRepository
    private lateinit var receiptsProperties: ReceiptsProperties
    private lateinit var fileProcessingService: FileProcessingService

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
    }

    @Test
    fun `given valid file when calculating checksum then should return consistent hash`() {
        // Given: A temporary file with known content
        val testFile = tempDir.resolve("test.txt").toFile()
        testFile.writeText("Hello World")

        // When: Calculating checksum twice
        val checksum1 = fileProcessingService.calculateFileChecksum(testFile)
        val checksum2 = fileProcessingService.calculateFileChecksum(testFile)

        // Then: Checksums should be identical and not empty
        assertNotNull(checksum1)
        assertNotNull(checksum2)
        assertEquals(checksum1, checksum2)
        assertTrue(checksum1.isNotEmpty())
        assertEquals(64, checksum1.length) // SHA-256 produces 64 character hex string
    }

    @Test
    fun `given filename when generating storage path then should create organized file with date prefix`() {
        // Given: A filename
        val filename = "receipt.pdf"

        // When: Generating storage path
        val storagePath = fileProcessingService.generateStoragePath(filename)

        // Then: Path should be organized with date prefix and contain original filename
        assertTrue(storagePath.toString().contains(receiptsProperties.attachmentsPath))
        assertTrue(storagePath.fileName.toString().contains("receipt.pdf"))
        assertTrue(storagePath.fileName.toString().matches(Regex("\\d{4}-\\d{2}-\\d{2}-receipt\\.pdf")))
        assertTrue(storagePath.parent.toFile().exists()) // Directory should be created
    }

    @Test
    fun `given duplicate filename when generating storage path then should add incremental suffix`() {
        // Given: A filename and create the first file
        val filename = "duplicate.pdf"
        val firstPath = fileProcessingService.generateStoragePath(filename)
        firstPath.toFile().createNewFile() // Create the first file

        // When: Generating storage path for duplicate
        val secondPath = fileProcessingService.generateStoragePath(filename)
        secondPath.toFile().createNewFile() // Create the second file
        val thirdPath = fileProcessingService.generateStoragePath(filename)

        // Then: Should add incremental suffixes
        assertTrue(firstPath.fileName.toString().matches(Regex("\\d{4}-\\d{2}-\\d{2}-duplicate\\.pdf")))
        assertTrue(secondPath.fileName.toString().matches(Regex("\\d{4}-\\d{2}-\\d{2}-duplicate-1\\.pdf")))
        assertTrue(thirdPath.fileName.toString().matches(Regex("\\d{4}-\\d{2}-\\d{2}-duplicate-2\\.pdf")))
        
        // All paths should be different
        assertNotEquals(firstPath, secondPath)
        assertNotEquals(secondPath, thirdPath)
        assertNotEquals(firstPath, thirdPath)
    }

    @Test
    fun `given filename without extension when generating storage path then should handle correctly`() {
        // Given: A filename without extension
        val filename = "document"

        // When: Generating storage path
        val storagePath = fileProcessingService.generateStoragePath(filename)

        // Then: Should handle file without extension
        assertTrue(storagePath.fileName.toString().matches(Regex("\\d{4}-\\d{2}-\\d{2}-document")))
        assertTrue(storagePath.toString().contains(receiptsProperties.attachmentsPath))
    }

    @Test
    fun `given valid PDF file when checking readiness then should return true`() {
        // Given: A valid PDF file
        val pdfFile = tempDir.resolve("document.pdf").toFile()
        pdfFile.writeText("PDF content") // Mock PDF content

        // When: Checking if file is ready for processing
        val isReady = fileProcessingService.isFileReadyForProcessing(pdfFile)

        // Then: File should be ready
        assertTrue(isReady)
    }

    @Test
    fun `given unsupported file extension when checking readiness then should return false`() {
        // Given: A file with unsupported extension
        val textFile = tempDir.resolve("document.txt").toFile()
        textFile.writeText("Text content")

        // When: Checking if file is ready for processing
        val isReady = fileProcessingService.isFileReadyForProcessing(textFile)

        // Then: File should not be ready
        assertFalse(isReady)
    }

    @Test
    fun `given empty file when checking readiness then should return false`() {
        // Given: An empty file
        val emptyFile = tempDir.resolve("empty.pdf").toFile()
        emptyFile.createNewFile()

        // When: Checking if file is ready for processing
        val isReady = fileProcessingService.isFileReadyForProcessing(emptyFile)

        // Then: File should not be ready
        assertFalse(isReady)
    }

    @Test
    fun `given new file when processing then should create IncomingFile entity`() {
        // Given: A valid file and no existing duplicate
        val testFile = tempDir.resolve("receipt.pdf").toFile()
        testFile.writeText("Receipt content")
        val userId = 1L

        whenever(incomingFileRepository.findByChecksum(any())).thenReturn(null)
        whenever(incomingFileRepository.save(any())).thenAnswer { invocation ->
            val incomingFile = invocation.getArgument<IncomingFile>(0)
            incomingFile.copy(id = 123L)
        }

        // When: Processing the file
        val result = fileProcessingService.processFile(testFile, userId)

        // Then: Should create IncomingFile entity and move file
        assertNotNull(result)
        assertEquals(123L, result.id)
        assertEquals("receipt.pdf", result.filename)
        assertEquals(BillStatus.PENDING, result.status)
        assertEquals(userId, result.userId)

        verify(incomingFileRepository).findByChecksum(any())
        verify(incomingFileRepository).save(any())
        assertFalse(testFile.exists()) // Original file should be moved
    }

    @Test
    fun `given duplicate file when processing then should skip and return null`() {
        // Given: A file with existing checksum
        val testFile = tempDir.resolve("duplicate.pdf").toFile()
        testFile.writeText("Duplicate content")
        val userId = 1L

        val existingFile = IncomingFile(
            id = 456L,
            filename = "existing.pdf",
            filePath = "/path/to/existing.pdf",
            uploadDate = LocalDateTime.now(),
            status = BillStatus.APPROVED,
            checksum = "existing_checksum",
            userId = userId
        )

        whenever(incomingFileRepository.findByChecksum(any())).thenReturn(existingFile)

        // When: Processing the duplicate file
        val result = fileProcessingService.processFile(testFile, userId)

        // Then: Should skip processing and return null
        assertEquals(null, result)

        verify(incomingFileRepository).findByChecksum(any())
        verify(incomingFileRepository, never()).save(any())
    }

    @Test
    fun `given file when moving to storage then should move file successfully`() {
        // Given: A source file and target path
        val sourceFile = tempDir.resolve("source.pdf").toFile()
        sourceFile.writeText("Source content")
        val targetPath = tempDir.resolve("storage").resolve("target.pdf")

        // When: Moving file to storage
        val movedFile = fileProcessingService.moveFileToStorage(sourceFile, targetPath)

        // Then: File should be moved successfully
        assertFalse(sourceFile.exists()) // Source should no longer exist
        assertTrue(movedFile.exists()) // Target should exist
        assertEquals("Source content", movedFile.readText())
        assertEquals(targetPath.toString(), movedFile.absolutePath)
    }
}