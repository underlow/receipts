package me.underlow.receipt.controller

import com.fasterxml.jackson.databind.ObjectMapper
import me.underlow.receipt.model.BillStatus
import me.underlow.receipt.service.IncomingFileService
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
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import kotlin.test.assertTrue

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
}
