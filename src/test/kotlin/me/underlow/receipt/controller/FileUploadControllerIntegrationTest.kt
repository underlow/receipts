package me.underlow.receipt.controller

import me.underlow.receipt.config.ReceiptsProperties
import me.underlow.receipt.dto.ErrorResponse
import me.underlow.receipt.dto.FileUploadResponse
import me.underlow.receipt.model.BillStatus
import me.underlow.receipt.model.IncomingFile
import me.underlow.receipt.model.User
import me.underlow.receipt.repository.IncomingFileRepository
import me.underlow.receipt.repository.UserRepository
import me.underlow.receipt.service.FileProcessingService
import me.underlow.receipt.service.IncomingFileOcrService
import me.underlow.receipt.service.UserService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.mockito.kotlin.*
import org.springframework.http.HttpStatus
import org.springframework.mock.web.MockMultipartFile
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken
import org.springframework.security.oauth2.core.oidc.OidcIdToken
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser
import java.io.File
import java.nio.file.Path
import java.time.LocalDateTime
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class FileUploadControllerIntegrationTest {

    private lateinit var incomingFileRepository: IncomingFileRepository
    private lateinit var userRepository: UserRepository
    private lateinit var receiptsProperties: ReceiptsProperties
    private lateinit var fileProcessingService: FileProcessingService
    private lateinit var incomingFileOcrService: IncomingFileOcrService
    private lateinit var userService: UserService
    private lateinit var fileUploadController: FileUploadController

    @TempDir
    lateinit var tempDir: Path

    @BeforeEach
    fun setup() {
        // Setup mock repositories
        incomingFileRepository = mock()
        userRepository = mock()

        // Setup properties with temp directories
        receiptsProperties = ReceiptsProperties(
            inboxPath = tempDir.resolve("inbox").toString(),
            attachmentsPath = tempDir.resolve("attachments").toString()
        )

        // Create mock services
        incomingFileOcrService = mock()
        
        // Create services with actual implementations
        fileProcessingService = FileProcessingService(incomingFileRepository, receiptsProperties, incomingFileOcrService, userRepository)
        userService = UserService(userRepository)
        fileUploadController = FileUploadController(fileProcessingService, userService)
    }

    @Test
    fun `given complete upload workflow when processing file then should handle end to end successfully`() {
        // Given: A user and valid file upload
        val user = User(
            id = 1L,
            email = "integration@example.com",
            name = "Integration Test User"
        )

        val testFile = MockMultipartFile(
            "file",
            "integration-test.pdf",
            "application/pdf",
            "Integration test PDF content".toByteArray()
        )

        val principal = createMockPrincipal(user.email)

        // Mock repository responses
        whenever(userRepository.findByEmail(user.email)).thenReturn(user)
        whenever(incomingFileRepository.findByChecksum(any())).thenReturn(null) // No duplicates
        whenever(incomingFileRepository.save(any())).thenAnswer { invocation ->
            val incomingFile = invocation.getArgument<IncomingFile>(0)
            incomingFile.copy(id = 999L)
        }

        // When: Uploading file through complete workflow
        val response = fileUploadController.uploadFile(testFile, principal)

        // Then: Should complete successfully with proper file handling
        assertEquals(200, response.statusCodeValue)
        assertIs<FileUploadResponse>(response.body)

        val responseBody = response.body as FileUploadResponse
        assertEquals(999L, responseBody.id)
        assertEquals("integration-test.pdf", responseBody.filename)
        assertEquals(BillStatus.PENDING, responseBody.status)
        assertTrue(responseBody.success)
        assertNotNull(responseBody.checksum)

        // Verify repository interactions
        verify(userRepository).findByEmail(user.email)
        verify(incomingFileRepository).findByChecksum(any())
        verify(incomingFileRepository).save(argThat { incomingFile ->
            incomingFile.filename == "integration-test.pdf" &&
            incomingFile.userId == 1L &&
            incomingFile.status == BillStatus.PENDING &&
            incomingFile.checksum.isNotEmpty()
        })

        // Verify file was moved to storage
        val attachmentsDir = Path.of(receiptsProperties.attachmentsPath)
        assertTrue(attachmentsDir.toFile().exists())

        // File should be moved to attachments directory with date prefix
        val movedFiles = attachmentsDir.toFile().listFiles()?.filter {
            it.name.contains("integration-test.pdf")
        }
        assertNotNull(movedFiles)
        assertTrue(movedFiles.isNotEmpty())
    }

    @Test
    fun `given file with identical content when uploading then should detect duplicate and reject`() {
        // Given: Two files with identical content
        val user = User(id = 2L, email = "duplicate@example.com", name = "Duplicate Test User")

        val originalFile = MockMultipartFile(
            "file",
            "original.pdf",
            "application/pdf",
            "Identical content".toByteArray()
        )

        val duplicateFile = MockMultipartFile(
            "file",
            "duplicate.pdf",
            "application/pdf",
            "Identical content".toByteArray() // Same content, different name
        )

        val principal = createMockPrincipal(user.email)

        // Setup: First file upload succeeds
        whenever(userRepository.findByEmail(user.email)).thenReturn(user)
        whenever(incomingFileRepository.findByChecksum(any())).thenReturn(null).thenReturn(
            IncomingFile(
                id = 100L,
                filename = "original.pdf",
                filePath = "/storage/original.pdf",
                uploadDate = LocalDateTime.now(),
                status = BillStatus.PENDING,
                checksum = "duplicate_checksum",
                userId = 2L
            )
        )
        whenever(incomingFileRepository.save(any())).thenAnswer { invocation ->
            val incomingFile = invocation.getArgument<IncomingFile>(0)
            incomingFile.copy(id = 100L)
        }

        // When: Uploading original file first
        val firstResponse = fileUploadController.uploadFile(originalFile, principal)

        // Then: First upload should succeed
        assertEquals(200, firstResponse.statusCodeValue)

        // When: Uploading duplicate file
        val duplicateResponse = fileUploadController.uploadFile(duplicateFile, principal)

        // Then: Duplicate should be rejected
        assertEquals(409, duplicateResponse.statusCodeValue) // Conflict
        assertIs<ErrorResponse>(duplicateResponse.body)

        val errorResponse = duplicateResponse.body as ErrorResponse
        assertEquals("File already exists or processing failed", errorResponse.message)
        assertEquals("DUPLICATE_FILE", errorResponse.code)
    }

    @Test
    fun `given large file within limits when uploading then should process successfully`() {
        // Given: File near but within size limit (9MB)
        val fileSize = 9 * 1024 * 1024 // 9MB
        val largeContent = ByteArray(fileSize) { 'A'.code.toByte() }
        val largeFile = MockMultipartFile(
            "file",
            "large-receipt.pdf",
            "application/pdf",
            largeContent
        )

        val user = User(id = 3L, email = "large@example.com", name = "Large File User")
        val principal = createMockPrincipal(user.email)

        whenever(userRepository.findByEmail(user.email)).thenReturn(user)
        whenever(incomingFileRepository.findByChecksum(any())).thenReturn(null)
        whenever(incomingFileRepository.save(any())).thenAnswer { invocation ->
            val incomingFile = invocation.getArgument<IncomingFile>(0)
            incomingFile.copy(id = 200L)
        }

        // When: Uploading large file within limits
        val response = fileUploadController.uploadFile(largeFile, principal)

        // Then: Should process successfully despite large size
        assertEquals(200, response.statusCodeValue)
        assertIs<FileUploadResponse>(response.body)

        val responseBody = response.body as FileUploadResponse
        assertEquals("large-receipt.pdf", responseBody.filename)
        assertTrue(responseBody.success)
    }

    @Test
    fun `given multiple file types when uploading then should handle all supported formats`() {
        // Given: Different supported file types
        val supportedFiles = listOf(
            MockMultipartFile("file", "receipt.pdf", "application/pdf", "PDF".toByteArray()),
            MockMultipartFile("file", "receipt.jpg", "image/jpeg", "JPEG".toByteArray()),
            MockMultipartFile("file", "receipt.png", "image/png", "PNG".toByteArray()),
            MockMultipartFile("file", "receipt.gif", "image/gif", "GIF".toByteArray()),
            MockMultipartFile("file", "receipt.bmp", "image/bmp", "BMP".toByteArray()),
            MockMultipartFile("file", "receipt.tiff", "image/tiff", "TIFF".toByteArray())
        )

        val user = User(id = 4L, email = "formats@example.com", name = "Format Test User")
        val principal = createMockPrincipal(user.email)

        whenever(userRepository.findByEmail(user.email)).thenReturn(user)
        whenever(incomingFileRepository.findByChecksum(any())).thenReturn(null)
        var fileIdCounter = 300L
        whenever(incomingFileRepository.save(any())).thenAnswer { invocation ->
            val incomingFile = invocation.getArgument<IncomingFile>(0)
            incomingFile.copy(id = ++fileIdCounter)
        }

        // When: Uploading all supported file types
        supportedFiles.forEach { file ->
            val response = fileUploadController.uploadFile(file, principal)

            // Then: Each file type should be accepted
            assertEquals(200, response.statusCodeValue, "Failed for file: ${file.originalFilename}")
            assertIs<FileUploadResponse>(response.body)
        }

        // Verify all files were processed
        verify(incomingFileRepository, times(supportedFiles.size)).save(any())
    }

    @Test
    fun `given file processing error when uploading then should cleanup temporary file`() {
        // Given: File that will cause processing error
        val testFile = MockMultipartFile(
            "file",
            "error-file.pdf",
            "application/pdf",
            "Content that causes error".toByteArray()
        )

        val user = User(id = 5L, email = "error@example.com", name = "Error Test User")
        val principal = createMockPrincipal(user.email)

        // Record existing temp files before test
        val tempDirPath = File(System.getProperty("java.io.tmpdir"))
        val existingTempFiles = tempDirPath.listFiles()?.filter {
            it.name.contains("upload-")
        }?.map { it.name }?.toSet() ?: emptySet()

        whenever(userRepository.findByEmail(user.email)).thenReturn(user)
        whenever(incomingFileRepository.findByChecksum(any())).thenThrow(RuntimeException("Database error"))

        // When: Upload fails due to processing error
        val response = fileUploadController.uploadFile(testFile, principal)

        // Then: Should handle error gracefully and cleanup temp files
        assertEquals(HttpStatus.CONFLICT, response.statusCode)
        assertIs<ErrorResponse>(response.body)

        val errorResponse = response.body as ErrorResponse
        assertEquals("File already exists or processing failed", errorResponse.message)

        // Verify no NEW temporary files are left behind
        val currentTempFiles = tempDirPath.listFiles()?.filter {
            it.name.contains("upload-")
        }?.map { it.name }?.toSet() ?: emptySet()

        // Should not have any new temp files or directories from this failed upload
        val newTempFiles = currentTempFiles - existingTempFiles
        assertTrue(newTempFiles.isEmpty(), "New temporary files should be cleaned up: $newTempFiles")
    }

    /**
     * Creates a mock OAuth2AuthenticationToken for integration testing.
     */
    private fun createMockPrincipal(email: String): OAuth2AuthenticationToken {
        val attributes = mapOf(
            "email" to email,
            "name" to "Integration Test User"
        )

        val idToken = OidcIdToken.withTokenValue("integration-token")
            .claim("sub", "integration-subject")
            .claim("email", email)
            .build()

        val oidcUser = DefaultOidcUser(emptyList(), idToken, "email")

        return OAuth2AuthenticationToken(oidcUser, emptyList(), "google")
    }
}
