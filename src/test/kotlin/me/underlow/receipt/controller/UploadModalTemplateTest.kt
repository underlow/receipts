package me.underlow.receipt.controller

import me.underlow.receipt.config.SecurityConfiguration
import me.underlow.receipt.dashboard.BaseTable
import me.underlow.receipt.dashboard.BillsView
import me.underlow.receipt.dashboard.InboxView
import me.underlow.receipt.dashboard.NavigationPanel
import me.underlow.receipt.dashboard.ReceiptsView
import me.underlow.receipt.service.CustomAuthenticationFailureHandler
import me.underlow.receipt.service.CustomAuthenticationSuccessHandler
import me.underlow.receipt.service.CustomOAuth2UserService
import me.underlow.receipt.service.MockBillsService
import me.underlow.receipt.service.MockInboxService
import me.underlow.receipt.service.MockReceiptsService
import org.hamcrest.CoreMatchers.containsString
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

/**
 * Test class for upload modal template rendering and functionality.
 * Tests upload modal component integration with dashboard template including structure, 
 * cropper.js integration, and Bootstrap modal functionality.
 */
@ExtendWith(MockitoExtension::class)
@WebMvcTest(DashboardController::class)
@Import(SecurityConfiguration::class, MockBillsService::class, MockInboxService::class, BillsView::class, InboxView::class, BaseTable::class, NavigationPanel::class)
class UploadModalTemplateTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockitoBean
    private lateinit var customOAuth2UserService: CustomOAuth2UserService

    @MockitoBean
    private lateinit var customAuthenticationSuccessHandler: CustomAuthenticationSuccessHandler

    @MockitoBean
    private lateinit var customAuthenticationFailureHandler: CustomAuthenticationFailureHandler

    @MockitoBean
    private lateinit var mockInboxService: MockInboxService

    @MockitoBean
    private lateinit var inboxView: InboxView

    @MockitoBean
    private lateinit var baseTable: BaseTable

    @MockitoBean
    private lateinit var mockReceiptsService: MockReceiptsService

    @MockitoBean
    private lateinit var receiptsView: ReceiptsView

    @Test
    @WithMockUser
    fun `given dashboard template when rendered then should include upload modal HTML structure`() {
        // Given: Dashboard template with upload modal component
        // When: Dashboard page is requested
        // Then: Upload modal HTML structure is present in the template
        mockMvc.perform(get("/dashboard"))
            .andExpect(status().isOk)
            .andExpect(view().name("dashboard"))
            .andExpect(content().string(containsString("id=\"uploadModal\"")))
            .andExpect(content().string(containsString("class=\"modal fade\"")))
            .andExpect(content().string(containsString("modal-dialog modal-lg")))
            .andExpect(content().string(containsString("modal-content")))
    }

    @Test
    @WithMockUser
    fun `given upload modal when rendered then should have proper modal header with title`() {
        // Given: Upload modal component is included in dashboard
        // When: Dashboard page is rendered
        // Then: Modal header with proper title is present
        mockMvc.perform(get("/dashboard"))
            .andExpect(status().isOk)
            .andExpect(view().name("dashboard"))
            .andExpect(content().string(containsString("modal-header")))
            .andExpect(content().string(containsString("Upload Receipt Image")))
            .andExpect(content().string(containsString("modal-title")))
    }

    @Test
    @WithMockUser
    fun `given upload modal when rendered then should have image preview container`() {
        // Given: Upload modal with image preview functionality
        // When: Dashboard page is rendered
        // Then: Image preview container is present with proper structure
        mockMvc.perform(get("/dashboard"))
            .andExpect(status().isOk)
            .andExpect(view().name("dashboard"))
            .andExpect(content().string(containsString("id=\"imagePreview\"")))
            .andExpect(content().string(containsString("id=\"cropperImage\"")))
    }

    @Test
    @WithMockUser
    fun `given upload modal when rendered then should have resize controls section`() {
        // Given: Upload modal with image editing capabilities
        // When: Dashboard page is rendered
        // Then: Resize controls section is present for image editing
        mockMvc.perform(get("/dashboard"))
            .andExpect(status().isOk)
            .andExpect(view().name("dashboard"))
            .andExpect(content().string(containsString("resize-controls")))
            .andExpect(content().string(containsString("width")))
            .andExpect(content().string(containsString("height")))
    }

    @Test
    @WithMockUser
    fun `given upload modal when rendered then should have upload and cancel buttons`() {
        // Given: Upload modal with user action buttons
        // When: Dashboard page is rendered
        // Then: Upload and cancel buttons are present with proper styling
        mockMvc.perform(get("/dashboard"))
            .andExpect(status().isOk)
            .andExpect(view().name("dashboard"))
            .andExpect(content().string(containsString("id=\"cancelUpload\"")))
            .andExpect(content().string(containsString("id=\"confirmUpload\"")))
            .andExpect(content().string(containsString("btn btn-secondary")))
            .andExpect(content().string(containsString("btn btn-success")))
    }

    @Test
    @WithMockUser
    fun `given upload modal when rendered then should have proper modal footer structure`() {
        // Given: Upload modal with footer containing action buttons
        // When: Dashboard page is rendered
        // Then: Modal footer is present with proper Bootstrap structure
        mockMvc.perform(get("/dashboard"))
            .andExpect(status().isOk)
            .andExpect(view().name("dashboard"))
            .andExpect(content().string(containsString("modal-footer")))
            .andExpect(content().string(containsString("Cancel")))
            .andExpect(content().string(containsString("Upload")))
    }

    @Test
    @WithMockUser
    fun `given upload modal when rendered then should be integrated with Cropper_js library`() {
        // Given: Upload modal requiring image editing functionality
        // When: Dashboard page is rendered
        // Then: Cropper.js integration is present for image editing
        mockMvc.perform(get("/dashboard"))
            .andExpect(status().isOk)
            .andExpect(view().name("dashboard"))
            .andExpect(content().string(containsString("cropper")))
            .andExpect(content().string(containsString("cropperImage")))
    }

    @Test
    @WithMockUser
    fun `given upload modal when rendered then should have proper Bootstrap modal attributes`() {
        // Given: Upload modal using Bootstrap modal component
        // When: Dashboard page is rendered
        // Then: Proper Bootstrap modal attributes are present for functionality
        mockMvc.perform(get("/dashboard"))
            .andExpect(status().isOk)
            .andExpect(view().name("dashboard"))
            .andExpect(content().string(containsString("data-bs-toggle=\"modal\"")))
            .andExpect(content().string(containsString("data-bs-target=\"#uploadModal\"")))
            .andExpect(content().string(containsString("aria-label")))
    }

    @Test
    @WithMockUser
    fun `given upload modal when rendered then should have accessibility attributes`() {
        // Given: Upload modal requiring accessibility compliance
        // When: Dashboard page is rendered
        // Then: Proper accessibility attributes are present
        mockMvc.perform(get("/dashboard"))
            .andExpect(status().isOk)
            .andExpect(view().name("dashboard"))
            .andExpect(content().string(containsString("aria-")))
            .andExpect(content().string(containsString("role=")))
            .andExpect(content().string(containsString("tabindex=")))
    }

    @Test
    @WithMockUser
    fun `given upload modal when rendered then should have proper modal body structure`() {
        // Given: Upload modal with content area for image editing
        // When: Dashboard page is rendered
        // Then: Modal body has proper structure with image container and controls
        mockMvc.perform(get("/dashboard"))
            .andExpect(status().isOk)
            .andExpect(view().name("dashboard"))
            .andExpect(content().string(containsString("modal-body")))
            .andExpect(content().string(containsString("imagePreview")))
            .andExpect(content().string(containsString("resize-controls")))
    }

    @Test
    @WithMockUser
    fun `given upload modal when rendered then should be positioned correctly for large screens`() {
        // Given: Upload modal designed for responsive layout
        // When: Dashboard page is rendered
        // Then: Modal has proper sizing classes for large screens
        mockMvc.perform(get("/dashboard"))
            .andExpect(status().isOk)
            .andExpect(view().name("dashboard"))
            .andExpect(content().string(containsString("modal-lg")))
            .andExpect(content().string(containsString("modal-dialog")))
    }
}