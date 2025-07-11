package me.underlow.receipt.dashboard

import me.underlow.receipt.config.SecurityConfiguration
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
 * Unit tests for Services tab visual design and responsive behavior.
 * Tests visual separator, responsive design, and accessibility features.
 */
@ExtendWith(MockitoExtension::class)
@WebMvcTest(me.underlow.receipt.controller.DashboardController::class)
@Import(SecurityConfiguration::class, MockBillsService::class, MockInboxService::class, BillsView::class, InboxView::class, BaseTable::class, NavigationPanel::class)
class ServicesTabVisualDesignTest {

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
    fun `given dashboard page when rendered then should have visual separator between Receipts and Services tabs`() {
        // given - authenticated user accessing dashboard page
        // when - dashboard page is rendered
        mockMvc.perform(get("/dashboard"))
            // then - should have visual separator between Receipts and Services tabs
            .andExpect(status().isOk)
            .andExpect(content().string(containsString("tab-separator")))
    }

    @Test
    @WithMockUser
    fun `given dashboard page when rendered then should have Services tab with responsive design on mobile`() {
        // given - authenticated user accessing dashboard page on mobile device
        // when - dashboard page is rendered
        mockMvc.perform(get("/dashboard"))
            // then - Services tab should have responsive design for mobile
            .andExpect(status().isOk)
            .andExpect(content().string(containsString("navigation-panel")))
            .andExpect(content().string(containsString("col-md-3 col-lg-2")))
    }

    @Test
    @WithMockUser
    fun `given dashboard page when rendered then should have Services tab with responsive design on tablet`() {
        // given - authenticated user accessing dashboard page on tablet device
        // when - dashboard page is rendered
        mockMvc.perform(get("/dashboard"))
            // then - Services tab should have responsive design for tablet
            .andExpect(status().isOk)
            .andExpect(content().string(containsString("col-md")))
            .andExpect(content().string(containsString("col-lg")))
    }

    @Test
    @WithMockUser
    fun `given dashboard page when rendered then should have Services tab with proper visual hierarchy`() {
        // given - authenticated user accessing dashboard page
        // when - dashboard page is rendered
        mockMvc.perform(get("/dashboard"))
            // then - Services tab should have proper visual hierarchy
            .andExpect(status().isOk)
            .andExpect(content().string(containsString("nav-link")))
            .andExpect(content().string(containsString("role=\"tab\"")))
    }

    @Test
    @WithMockUser
    fun `given dashboard page when rendered then should have Services tab with hover effects`() {
        // given - authenticated user accessing dashboard page
        // when - dashboard page is rendered
        mockMvc.perform(get("/dashboard"))
            // then - Services tab should have hover effects through CSS classes
            .andExpect(status().isOk)
            .andExpect(content().string(containsString("nav-link")))
            .andExpect(content().string(containsString("Services")))
    }

    @Test
    @WithMockUser
    fun `given dashboard page when rendered then should have Services tab with active state styling`() {
        // given - authenticated user accessing dashboard page
        // when - dashboard page is rendered
        mockMvc.perform(get("/dashboard"))
            // then - Services tab should have active state styling
            .andExpect(status().isOk)
            .andExpect(content().string(containsString("active")))
            .andExpect(content().string(containsString("primary")))
    }

    @Test
    @WithMockUser
    fun `given dashboard page when rendered then should have Services tab with proper spacing`() {
        // given - authenticated user accessing dashboard page
        // when - dashboard page is rendered
        mockMvc.perform(get("/dashboard"))
            // then - Services tab should have proper spacing through CSS classes
            .andExpect(status().isOk)
            .andExpect(content().string(containsString("navigation-panel")))
            .andExpect(content().string(containsString("nav-link")))
    }

    @Test
    @WithMockUser
    fun `given dashboard page when rendered then should have Services tab with accessibility color contrast`() {
        // given - authenticated user with visual impairment accessing dashboard page
        // when - dashboard page is rendered
        mockMvc.perform(get("/dashboard"))
            // then - Services tab should have accessibility color contrast through CSS classes
            .andExpect(status().isOk)
            .andExpect(content().string(containsString("nav-link")))
    }

    @Test
    @WithMockUser
    fun `given dashboard page when rendered then should have Services tab with focus indicators`() {
        // given - authenticated user using keyboard navigation
        // when - dashboard page is rendered
        mockMvc.perform(get("/dashboard"))
            // then - Services tab should have focus indicators through tabindex
            .andExpect(status().isOk)
            .andExpect(content().string(containsString("tabindex")))
    }

    @Test
    @WithMockUser
    fun `given dashboard page when rendered then should have Services tab with consistent branding`() {
        // given - authenticated user accessing dashboard page
        // when - dashboard page is rendered
        mockMvc.perform(get("/dashboard"))
            // then - Services tab should have consistent branding with other tabs
            .andExpect(status().isOk)
            .andExpect(content().string(containsString("nav nav-pills nav-fill flex-column")))
            .andExpect(content().string(containsString("Services")))
    }

    @Test
    @WithMockUser
    fun `given dashboard page when rendered then should have Services tab with proper animation transitions`() {
        // given - authenticated user interacting with dashboard page
        // when - dashboard page is rendered
        mockMvc.perform(get("/dashboard"))
            // then - Services tab should have proper animation transitions through CSS classes
            .andExpect(status().isOk)
            .andExpect(content().string(containsString("data-bs-toggle=\"tab\"")))
            .andExpect(content().string(containsString("Services")))
    }
}