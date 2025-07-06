package me.underlow.receipt.controller

import me.underlow.receipt.dto.ErrorResponse
import me.underlow.receipt.dto.FileUploadResponse
import me.underlow.receipt.model.ItemStatus
import me.underlow.receipt.model.IncomingFile
import me.underlow.receipt.model.User
import me.underlow.receipt.service.FileProcessingService
import me.underlow.receipt.service.UserService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*
import org.springframework.http.HttpStatus
import org.springframework.mock.web.MockMultipartFile
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken
import org.springframework.security.oauth2.core.oidc.OidcIdToken
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser
import java.time.LocalDateTime
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class FileUploadControllerTest {

    private lateinit var fileProcessingService: FileProcessingService
    private lateinit var userService: UserService
    private lateinit var fileUploadController: FileUploadController

    @BeforeEach
    fun setup() {
        fileProcessingService = mock()
        userService = mock()
        fileUploadController = FileUploadController(fileProcessingService, userService)
    }

    @Test
    fun `given valid file upload when authenticated user uploads then should return success response`() {
        // Given: Valid file upload with authenticated user
        val testFile = MockMultipartFile(
            "file",
            "receipt.pdf",
            "application/pdf",
            "PDF content".toByteArray()
        )
        
        val user = User(
            id = 1L,
            email = "test@example.com",
            name = "Test User"
        )
        
        val incomingFile = IncomingFile(
            id = 123L,
            filename = "receipt.pdf",
            filePath = "/storage/receipt.pdf",
            uploadDate = LocalDateTime.now(),
            status = ItemStatus.NEW,
            checksum = "abc123",
            userId = 1L
        )

        val principal = createMockPrincipal("test@example.com")
        
        whenever(userService.findByEmail("test@example.com")).thenReturn(user)
        whenever(fileProcessingService.processFile(any(), eq(1L))).thenReturn(incomingFile)

        // When: Uploading file
        val response = fileUploadController.uploadFile(testFile, principal)

        // Then: Should return success response with file details
        assertEquals(HttpStatus.OK, response.statusCode)
        assertIs<FileUploadResponse>(response.body)
        
        val responseBody = response.body as FileUploadResponse
        assertEquals(123L, responseBody.id)
        assertEquals("receipt.pdf", responseBody.filename)
        assertEquals(ItemStatus.NEW, responseBody.status)
        assertEquals("abc123", responseBody.checksum)
        assertTrue(responseBody.success)
        
        verify(userService).findByEmail("test@example.com")
        verify(fileProcessingService).processFile(any(), eq(1L))
    }

    @Test
    fun `given unauthenticated user when uploading file then should return unauthorized`() {
        // Given: File upload without valid authentication
        val testFile = MockMultipartFile("file", "receipt.pdf", "application/pdf", "content".toByteArray())
        val principal = createMockPrincipal("unknown@example.com")
        
        whenever(userService.findByEmail("unknown@example.com")).thenReturn(null)

        // When: Uploading file
        val response = fileUploadController.uploadFile(testFile, principal)

        // Then: Should return unauthorized status
        assertEquals(HttpStatus.UNAUTHORIZED, response.statusCode)
        assertIs<ErrorResponse>(response.body)
        
        val errorResponse = response.body as ErrorResponse
        assertEquals("User not authenticated", errorResponse.message)
        
        verify(userService).findByEmail("unknown@example.com")
        verifyNoInteractions(fileProcessingService)
    }

    @Test
    fun `given empty file when uploading then should return validation error`() {
        // Given: Empty file upload
        val emptyFile = MockMultipartFile("file", "empty.pdf", "application/pdf", ByteArray(0))
        val user = User(id = 1L, email = "test@example.com", name = "Test User")
        val principal = createMockPrincipal("test@example.com")
        
        whenever(userService.findByEmail("test@example.com")).thenReturn(user)

        // When: Uploading empty file
        val response = fileUploadController.uploadFile(emptyFile, principal)

        // Then: Should return validation error
        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
        assertIs<ErrorResponse>(response.body)
        
        val errorResponse = response.body as ErrorResponse
        assertEquals("File cannot be empty", errorResponse.message)
        assertEquals("EMPTY_FILE", errorResponse.code)
        
        verifyNoInteractions(fileProcessingService)
    }

    @Test
    fun `given oversized file when uploading then should return size validation error`() {
        // Given: File exceeding size limit (10MB)
        val oversizedContent = ByteArray(11 * 1024 * 1024) // 11MB
        val largeFile = MockMultipartFile("file", "large.pdf", "application/pdf", oversizedContent)
        val user = User(id = 1L, email = "test@example.com", name = "Test User")
        val principal = createMockPrincipal("test@example.com")
        
        whenever(userService.findByEmail("test@example.com")).thenReturn(user)

        // When: Uploading oversized file
        val response = fileUploadController.uploadFile(largeFile, principal)

        // Then: Should return size validation error
        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
        assertIs<ErrorResponse>(response.body)
        
        val errorResponse = response.body as ErrorResponse
        assertTrue(errorResponse.message.contains("File size exceeds maximum limit"))
        assertEquals("FILE_TOO_LARGE", errorResponse.code)
        assertEquals(10485760L, errorResponse.details?.get("maxSize")) // 10MB in bytes
        
        verifyNoInteractions(fileProcessingService)
    }

    @Test
    fun `given unsupported file type when uploading then should return type validation error`() {
        // Given: File with unsupported extension
        val textFile = MockMultipartFile("file", "document.txt", "text/plain", "content".toByteArray())
        val user = User(id = 1L, email = "test@example.com", name = "Test User")
        val principal = createMockPrincipal("test@example.com")
        
        whenever(userService.findByEmail("test@example.com")).thenReturn(user)

        // When: Uploading unsupported file type
        val response = fileUploadController.uploadFile(textFile, principal)

        // Then: Should return type validation error
        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
        assertIs<ErrorResponse>(response.body)
        
        val errorResponse = response.body as ErrorResponse
        assertTrue(errorResponse.message.contains("Unsupported file type"))
        assertEquals("UNSUPPORTED_FILE_TYPE", errorResponse.code)
        assertEquals("txt", errorResponse.details?.get("actualType"))
        
        verifyNoInteractions(fileProcessingService)
    }

    @Test
    fun `given duplicate file when uploading then should return conflict response`() {
        // Given: File that already exists (processFile returns null)
        val testFile = MockMultipartFile("file", "duplicate.pdf", "application/pdf", "content".toByteArray())
        val user = User(id = 1L, email = "test@example.com", name = "Test User")
        val principal = createMockPrincipal("test@example.com")
        
        whenever(userService.findByEmail("test@example.com")).thenReturn(user)
        whenever(fileProcessingService.processFile(any(), eq(1L))).thenReturn(null) // Duplicate detected

        // When: Uploading duplicate file
        val response = fileUploadController.uploadFile(testFile, principal)

        // Then: Should return conflict status
        assertEquals(HttpStatus.CONFLICT, response.statusCode)
        assertIs<ErrorResponse>(response.body)
        
        val errorResponse = response.body as ErrorResponse
        assertEquals("File already exists or processing failed", errorResponse.message)
        assertEquals("DUPLICATE_FILE", errorResponse.code)
        
        verify(fileProcessingService).processFile(any(), eq(1L))
    }

    @Test
    fun `given processing failure when uploading then should return internal server error`() {
        // Given: File processing throws exception
        val testFile = MockMultipartFile("file", "receipt.pdf", "application/pdf", "content".toByteArray())
        val user = User(id = 1L, email = "test@example.com", name = "Test User")
        val principal = createMockPrincipal("test@example.com")
        
        whenever(userService.findByEmail("test@example.com")).thenReturn(user)
        whenever(fileProcessingService.processFile(any(), eq(1L))).thenThrow(RuntimeException("Processing failed"))

        // When: Processing fails during upload
        val response = fileUploadController.uploadFile(testFile, principal)

        // Then: Should return internal server error
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.statusCode)
        assertIs<ErrorResponse>(response.body)
        
        val errorResponse = response.body as ErrorResponse
        assertEquals("Internal server error during file upload", errorResponse.message)
        assertEquals("INTERNAL_ERROR", errorResponse.code)
    }

    @Test
    fun `given supported image file when uploading then should accept file`() {
        // Given: Valid image file upload
        val imageFile = MockMultipartFile("file", "receipt.jpg", "image/jpeg", "JPEG content".toByteArray())
        val user = User(id = 1L, email = "test@example.com", name = "Test User")
        val principal = createMockPrincipal("test@example.com")
        
        val incomingFile = IncomingFile(
            id = 124L,
            filename = "receipt.jpg",
            filePath = "/storage/receipt.jpg",
            uploadDate = LocalDateTime.now(),
            status = ItemStatus.NEW,
            checksum = "def456",
            userId = 1L
        )
        
        whenever(userService.findByEmail("test@example.com")).thenReturn(user)
        whenever(fileProcessingService.processFile(any(), eq(1L))).thenReturn(incomingFile)

        // When: Uploading image file
        val response = fileUploadController.uploadFile(imageFile, principal)

        // Then: Should accept and process image file successfully
        assertEquals(HttpStatus.OK, response.statusCode)
        assertIs<FileUploadResponse>(response.body)
        
        val responseBody = response.body as FileUploadResponse
        assertEquals("receipt.jpg", responseBody.filename)
        assertTrue(responseBody.success)
    }

    /**
     * Creates a mock OAuth2AuthenticationToken for testing authentication scenarios.
     */
    private fun createMockPrincipal(email: String): OAuth2AuthenticationToken {
        val attributes = mapOf(
            "email" to email,
            "name" to "Test User"
        )
        
        val idToken = OidcIdToken.withTokenValue("mock-token")
            .claim("sub", "test-subject")
            .claim("email", email)
            .build()
        
        val oidcUser = DefaultOidcUser(emptyList(), idToken, "email")
        
        return OAuth2AuthenticationToken(oidcUser, emptyList(), "google")
    }
}