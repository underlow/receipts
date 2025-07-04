package me.underlow.receipt.controller

import com.fasterxml.jackson.databind.ObjectMapper
import me.underlow.receipt.model.BillStatus
import me.underlow.receipt.model.IncomingFile
import me.underlow.receipt.service.IncomingFileService
import me.underlow.receipt.service.EntityConversionService
import java.time.LocalDateTime
import org.junit.jupiter.api.Test
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken
import org.springframework.security.oauth2.core.user.DefaultOAuth2User
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import kotlin.test.assertTrue
import java.io.File
import me.underlow.receipt.model.Bill
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf

/**
 * Integration tests for InboxController specifically testing the status counts bug fix
 */
@WebMvcTest(InboxController::class)
class InboxControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @MockitoBean
    private lateinit var incomingFileService: IncomingFileService

    @MockitoBean
    private lateinit var entityConversionService: EntityConversionService

    /**
     * Test that the inbox page loads correctly when user has no files
     * This reproduces the original bug scenario where null status counts caused SpEL evaluation errors
     * Given: User with no files (all status counts are 0)
     * When: GET /inbox
     * Then: Page should load successfully with zero counts
     */
    @Test
    fun `Given user with no files, when getting inbox page, then should render successfully with zero counts`() {
        // Given: User with no files - all status counts are 0
        val userEmail = "nofiles@example.com"
        val emptyStatusCounts = mapOf(
            BillStatus.PENDING to 0,
            BillStatus.PROCESSING to 0,
            BillStatus.APPROVED to 0,
            BillStatus.REJECTED to 0
        )

        whenever(incomingFileService.findByUserEmailWithPagination(userEmail, null, 0, 20, "uploadDate", "desc"))
            .thenReturn(Pair(emptyList(), 0L))
        whenever(incomingFileService.getFileStatistics(userEmail))
            .thenReturn(emptyStatusCounts)

        val oauth2User = DefaultOAuth2User(
            listOf(SimpleGrantedAuthority("ROLE_USER")),
            mapOf(
                "email" to "nofiles@example.com",
                "name" to "Test User"
            ),
            "email"
        )
        val auth = OAuth2AuthenticationToken(oauth2User, oauth2User.authorities, "google")


        // When: Making request to inbox page
        val result = mockMvc.perform(
            get("/inbox")
                .with(authentication(auth))
        )

        // Then: Should return successful response and render inbox template
        result.andExpect(status().isOk)
            .andExpect(view().name("inbox"))
            .andExpect(model().attribute("userEmail", userEmail))
            .andExpect(model().attribute("totalFiles", 0L))
            .andExpect(model().attribute("files", emptyList<Any>()))
            .andExpect(model().attributeExists("statusCounts"))

        // And: Status counts should be available in model to prevent SpEL errors
        val model = result.andReturn().modelAndView?.model
        val statusCounts = model?.get("statusCounts") as? Map<*, *>
        assertTrue(statusCounts != null, "statusCounts should not be null")
        assertTrue(statusCounts.containsKey(BillStatus.PENDING), "Should contain PENDING status")
        assertTrue(statusCounts.containsKey(BillStatus.PROCESSING), "Should contain PROCESSING status")
        assertTrue(statusCounts.containsKey(BillStatus.APPROVED), "Should contain APPROVED status")
        assertTrue(statusCounts.containsKey(BillStatus.REJECTED), "Should contain REJECTED status")
    }

    /**
     * Test that the API endpoint returns correct structure for empty file list
     * Given: User with no files
     * When: GET /inbox/api/list
     * Then: Should return proper JSON response with zero counts
     */
    @Test
    fun `Given user with no files, when getting inbox API, then should return proper JSON with zero counts`() {
        // Given: User with no files
        val userEmail = "nofiles@example.com"
        val emptyStatusCounts = mapOf(
            BillStatus.PENDING to 0,
            BillStatus.PROCESSING to 0,
            BillStatus.APPROVED to 0,
            BillStatus.REJECTED to 0
        )

        whenever(incomingFileService.findByUserEmailWithPagination(userEmail, null, 0, 20, "uploadDate", "desc"))
            .thenReturn(Pair(emptyList(), 0L))
        whenever(incomingFileService.getFileStatistics(userEmail))
            .thenReturn(emptyStatusCounts)

        val oauth2User = DefaultOAuth2User(
            listOf(SimpleGrantedAuthority("ROLE_USER")),
            mapOf(
                "email" to "nofiles@example.com",
                "name" to "Test User"
            ),
            "email"
        )
        val auth = OAuth2AuthenticationToken(oauth2User, oauth2User.authorities, "google")


        // When: Making API request
        val result = mockMvc.perform(
            get("/inbox/api/list")
                .with(authentication(auth))
        )

        // Then: Should return successful JSON response
        result.andExpect(status().isOk)
            .andExpect(content().contentType("application/json"))
            .andExpect(jsonPath("$.files").isArray)
            .andExpect(jsonPath("$.files").isEmpty)
            .andExpect(jsonPath("$.totalFiles").value(0))
            .andExpect(jsonPath("$.currentPage").value(0))
            .andExpect(jsonPath("$.totalPages").value(0))
            .andExpect(jsonPath("$.statusCounts.pending").value(0))
            .andExpect(jsonPath("$.statusCounts.processing").value(0))
            .andExpect(jsonPath("$.statusCounts.approved").value(0))
            .andExpect(jsonPath("$.statusCounts.rejected").value(0))
    }

    /**
     * Test that filter and sort parameters are properly handled
     * Given: User with filter and sort parameters
     * When: GET /inbox with status=pending&sortBy=filename&sortDirection=asc
     * Then: Should pass correct parameters to service and display in model
     */
    @Test
    fun `Given filter and sort parameters, when getting inbox page, then should pass correct parameters to service`() {
        // Given: User with filter and sort parameters
        val userEmail = "test@example.com"
        val statusCounts = mapOf(
            BillStatus.PENDING to 5,
            BillStatus.PROCESSING to 2,
            BillStatus.APPROVED to 3,
            BillStatus.REJECTED to 1
        )

        whenever(incomingFileService.findByUserEmailWithPagination(userEmail, BillStatus.PENDING, 0, 20, "filename", "asc"))
            .thenReturn(Pair(emptyList(), 5L))
        whenever(incomingFileService.getFileStatistics(userEmail))
            .thenReturn(statusCounts)

        val oauth2User = DefaultOAuth2User(
            listOf(SimpleGrantedAuthority("ROLE_USER")),
            mapOf(
                "email" to userEmail,
                "name" to "Test User"
            ),
            "email"
        )
        val auth = OAuth2AuthenticationToken(oauth2User, oauth2User.authorities, "google")

        // When: Making request with filter and sort parameters
        val result = mockMvc.perform(
            get("/inbox")
                .param("status", "pending")
                .param("sortBy", "filename")
                .param("sortDirection", "asc")
                .with(authentication(auth))
        )

        // Then: Should return successful response with correct model attributes
        result.andExpect(status().isOk)
            .andExpect(view().name("inbox"))
            .andExpect(model().attribute("selectedStatus", "pending"))
            .andExpect(model().attribute("sortBy", "filename"))
            .andExpect(model().attribute("sortDirection", "asc"))
            .andExpect(model().attributeExists("statusCounts"))
    }

    /**
     * Test that invalid status filter defaults to showing all files
     * Given: User with invalid status parameter
     * When: GET /inbox with status=invalid
     * Then: Should treat as no filter and show all files
     */
    @Test
    fun `Given invalid status filter, when getting inbox page, then should show all files`() {
        // Given: User with invalid status parameter
        val userEmail = "test@example.com"
        val statusCounts = mapOf(
            BillStatus.PENDING to 5,
            BillStatus.PROCESSING to 2,
            BillStatus.APPROVED to 3,
            BillStatus.REJECTED to 1
        )

        whenever(incomingFileService.findByUserEmailWithPagination(userEmail, null, 0, 20, "uploadDate", "desc"))
            .thenReturn(Pair(emptyList(), 11L))
        whenever(incomingFileService.getFileStatistics(userEmail))
            .thenReturn(statusCounts)

        val oauth2User = DefaultOAuth2User(
            listOf(SimpleGrantedAuthority("ROLE_USER")),
            mapOf(
                "email" to userEmail,
                "name" to "Test User"
            ),
            "email"
        )
        val auth = OAuth2AuthenticationToken(oauth2User, oauth2User.authorities, "google")

        // When: Making request with invalid status parameter
        val result = mockMvc.perform(
            get("/inbox")
                .param("status", "invalid")
                .with(authentication(auth))
        )

        // Then: Should return successful response showing all files
        result.andExpect(status().isOk)
            .andExpect(view().name("inbox"))
            .andExpect(model().attribute("selectedStatus", "invalid"))
            .andExpect(model().attribute("totalFiles", 11L))
    }

    /**
     * Test that file detail view loads correctly for existing file
     * Given: User owns a file with ID 123
     * When: GET /inbox/files/123
     * Then: Should render detail view with file information
     */
    @Test
    fun `Given user owns file, when getting file detail page, then should render detail view`() {
        // Given: User owns a file
        val userEmail = "test@example.com"
        val fileId = 123L
        val testFile = IncomingFile(
            id = fileId,
            filename = "test-document.pdf",
            filePath = "/tmp/test-document.pdf",
            uploadDate = LocalDateTime.now(),
            status = BillStatus.PENDING,
            checksum = "abc123def456",
            userId = 1L
        )

        whenever(incomingFileService.findByIdAndUserEmail(fileId, userEmail))
            .thenReturn(testFile)

        val oauth2User = DefaultOAuth2User(
            listOf(SimpleGrantedAuthority("ROLE_USER")),
            mapOf(
                "email" to userEmail,
                "name" to "Test User"
            ),
            "email"
        )
        val auth = OAuth2AuthenticationToken(oauth2User, oauth2User.authorities, "google")

        // When: Making request to file detail page
        val result = mockMvc.perform(
            get("/inbox/files/$fileId")
                .with(authentication(auth))
        )

        // Then: Should return successful response and render detail template
        result.andExpect(status().isOk)
            .andExpect(view().name("inbox-detail"))
            .andExpect(model().attribute("userEmail", userEmail))
            .andExpect(model().attribute("userName", "Test User"))
            .andExpect(model().attributeExists("file"))

        // And: File data should be properly converted to DTO
        val model = result.andReturn().modelAndView?.model
        val fileDto = model?.get("file")
        assertTrue(fileDto != null, "File DTO should not be null")
    }

    /**
     * Test that file detail view redirects for non-existent file
     * Given: User requests a file that doesn't exist
     * When: GET /inbox/files/999
     * Then: Should redirect to inbox with error
     */
    @Test
    fun `Given file does not exist, when getting file detail page, then should redirect to inbox with error`() {
        // Given: File doesn't exist
        val userEmail = "test@example.com"
        val fileId = 999L

        whenever(incomingFileService.findByIdAndUserEmail(fileId, userEmail))
            .thenReturn(null)

        val oauth2User = DefaultOAuth2User(
            listOf(SimpleGrantedAuthority("ROLE_USER")),
            mapOf(
                "email" to userEmail,
                "name" to "Test User"
            ),
            "email"
        )
        val auth = OAuth2AuthenticationToken(oauth2User, oauth2User.authorities, "google")

        // When: Making request to non-existent file
        val result = mockMvc.perform(
            get("/inbox/files/$fileId")
                .with(authentication(auth))
        )

        // Then: Should redirect to inbox with error parameter
        result.andExpect(status().is3xxRedirection)
            .andExpect(redirectedUrl("/inbox?error=file_not_found"))
    }

    /**
     * Test that file detail API returns correct JSON for existing file
     * Given: User owns a file with ID 123
     * When: GET /inbox/api/files/123/detail
     * Then: Should return detailed file information as JSON
     */
    @Test
    fun `Given user owns file, when getting file detail API, then should return detailed file information`() {
        // Given: User owns a file
        val userEmail = "test@example.com"
        val fileId = 123L
        val testFile = IncomingFile(
            id = fileId,
            filename = "test-document.pdf",
            filePath = "/tmp/test-document.pdf",
            uploadDate = LocalDateTime.of(2024, 1, 15, 10, 30, 0),
            status = BillStatus.PENDING,
            checksum = "abc123def456",
            userId = 1L
        )

        whenever(incomingFileService.findByIdAndUserEmail(fileId, userEmail))
            .thenReturn(testFile)

        val oauth2User = DefaultOAuth2User(
            listOf(SimpleGrantedAuthority("ROLE_USER")),
            mapOf(
                "email" to userEmail,
                "name" to "Test User"
            ),
            "email"
        )
        val auth = OAuth2AuthenticationToken(oauth2User, oauth2User.authorities, "google")

        // When: Making API request for file detail
        val result = mockMvc.perform(
            get("/inbox/api/files/$fileId/detail")
                .with(authentication(auth))
        )

        // Then: Should return successful JSON response with file details
        result.andExpect(status().isOk)
            .andExpect(content().contentType("application/json"))
            .andExpect(jsonPath("$.id").value(fileId))
            .andExpect(jsonPath("$.filename").value("test-document.pdf"))
            .andExpect(jsonPath("$.status").value("PENDING"))
            .andExpect(jsonPath("$.statusDisplayName").value("Pending Review"))
            .andExpect(jsonPath("$.checksum").value("abc123def456"))
            .andExpect(jsonPath("$.fileUrl").value("/api/files/$fileId"))
            .andExpect(jsonPath("$.thumbnailUrl").value("/api/files/$fileId/thumbnail"))
            .andExpect(jsonPath("$.canApprove").value(true))
            .andExpect(jsonPath("$.canReject").value(true))
            .andExpect(jsonPath("$.canDelete").value(true))
            .andExpect(jsonPath("$.isPdf").value(true))
            .andExpect(jsonPath("$.isImage").value(false))
    }

    /**
     * Test that file detail API returns 404 for non-existent file
     * Given: User requests a file that doesn't exist
     * When: GET /inbox/api/files/999/detail
     * Then: Should return 404 Not Found
     */
    @Test
    fun `Given file does not exist, when getting file detail API, then should return 404`() {
        // Given: File doesn't exist
        val userEmail = "test@example.com"
        val fileId = 999L

        whenever(incomingFileService.findByIdAndUserEmail(fileId, userEmail))
            .thenReturn(null)

        val oauth2User = DefaultOAuth2User(
            listOf(SimpleGrantedAuthority("ROLE_USER")),
            mapOf(
                "email" to userEmail,
                "name" to "Test User"
            ),
            "email"
        )
        val auth = OAuth2AuthenticationToken(oauth2User, oauth2User.authorities, "google")

        // When: Making API request for non-existent file
        val result = mockMvc.perform(
            get("/inbox/api/files/$fileId/detail")
                .with(authentication(auth))
        )

        // Then: Should return 404 Not Found
        result.andExpect(status().isNotFound)
    }

    /**
     * Test that unauthenticated user cannot access file detail
     * Given: User is not authenticated
     * When: GET /inbox/files/123
     * Then: Should return 401 Unauthorized
     */
    @Test
    fun `Given user is not authenticated, when getting file detail page, then should return 401`() {
        // Given: User is not authenticated
        val fileId = 123L

        // When: Making request without authentication
        val result = mockMvc.perform(
            get("/inbox/files/$fileId")
        )

        // Then: Should return 401 Unauthorized (handled by Spring Security)
        result.andExpect(status().isUnauthorized)
    }

    /**
     * Test that converting IncomingFile to Bill returns the correct Bill ID
     * This test addresses the bug fix where frontend JavaScript was trying to access
     * `data.entityId` instead of `data.fileId` for the converted Bill ID
     * Given: User owns a file with ID 123
     * When: POST /inbox/api/files/123/convert-to-bill
     * Then: Should return success response with Bill ID in fileId field
     */
    @Test
    fun `Given user owns file, when converting file to bill, then should return success response with bill ID in fileId field`() {
        // Given: User owns a file that can be converted to Bill
        val userEmail = "test@example.com"
        val fileId = 123L
        val convertedBillId = 456L
        
        val testBill = Bill(
            id = convertedBillId,
            filename = "test-document.pdf",
            filePath = "/tmp/test-document.pdf",
            uploadDate = LocalDateTime.now(),
            status = BillStatus.PENDING,
            checksum = "abc123def456",
            userId = 1L,
            originalIncomingFileId = fileId
        )

        whenever(entityConversionService.convertIncomingFileToBill(eq(fileId), eq(userEmail)))
            .thenReturn(testBill)

        val oauth2User = DefaultOAuth2User(
            listOf(SimpleGrantedAuthority("ROLE_USER")),
            mapOf(
                "email" to userEmail,
                "name" to "Test User"
            ),
            "email"
        )
        val auth = OAuth2AuthenticationToken(oauth2User, oauth2User.authorities, "google")

        // When: Making request to convert file to bill
        val result = mockMvc.perform(
            post("/inbox/api/files/$fileId/convert-to-bill")
                .with(authentication(auth))
                .with(csrf())
        )

        // Then: Should return successful JSON response with Bill ID in fileId field
        result.andExpect(status().isOk)
            .andExpect(content().contentType("application/json"))
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.message").value("File converted to Bill successfully"))
            .andExpect(jsonPath("$.fileId").value(convertedBillId))
            .andExpect(jsonPath("$.fileId").isNumber)
            .andExpect(jsonPath("$.entityId").doesNotExist())
    }

    /**
     * Test that converting IncomingFile to Bill fails gracefully when conversion fails
     * Given: User owns a file but conversion fails
     * When: POST /inbox/api/files/123/convert-to-bill
     * Then: Should return error response
     */
    @Test
    fun `Given conversion fails, when converting file to bill, then should return error response`() {
        // Given: User owns a file but conversion fails
        val userEmail = "test@example.com"
        val fileId = 123L

        whenever(entityConversionService.convertIncomingFileToBill(eq(fileId), eq(userEmail)))
            .thenReturn(null)

        val oauth2User = DefaultOAuth2User(
            listOf(SimpleGrantedAuthority("ROLE_USER")),
            mapOf(
                "email" to userEmail,
                "name" to "Test User"
            ),
            "email"
        )
        val auth = OAuth2AuthenticationToken(oauth2User, oauth2User.authorities, "google")

        // When: Making request to convert file to bill
        val result = mockMvc.perform(
            post("/inbox/api/files/$fileId/convert-to-bill")
                .with(authentication(auth))
                .with(csrf())
        )

        // Then: Should return error response
        result.andExpect(status().isBadRequest)
            .andExpect(content().contentType("application/json"))
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message").value("Failed to convert file to Bill"))
    }
}
