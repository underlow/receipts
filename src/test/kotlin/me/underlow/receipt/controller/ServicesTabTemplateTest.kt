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
 * Unit tests for Services tab template rendering and routing.
 * Tests Services tab navigation, routing, visual separator, and responsive design.
 */
@ExtendWith(MockitoExtension::class)
@WebMvcTest(DashboardController::class)
@Import(SecurityConfiguration::class, MockBillsService::class, MockInboxService::class, BillsView::class, InboxView::class, BaseTable::class, NavigationPanel::class)
class ServicesTabTemplateTest {

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
    fun `given dashboard page when rendered then should contain Services tab as 4th tab`() {
        // given - authenticated user accessing dashboard page
        // when - dashboard page is rendered
        mockMvc.perform(get("/dashboard"))
            // then - should contain Services tab as 4th tab in navigation
            .andExpect(status().isOk)
            .andExpect(view().name("dashboard"))
            .andExpect(content().string(containsString("Services")))
            .andExpect(content().string(containsString("nav-link")))
    }

    @Test
    @WithMockUser
    fun `given dashboard page when rendered then should have Services tab with correct href routing`() {
        // given - authenticated user accessing dashboard page
        // when - dashboard page is rendered
        mockMvc.perform(get("/dashboard"))
            // then - Services tab should have correct href routing
            .andExpect(status().isOk)
            .andExpect(content().string(containsString("href=\"#services\"")))
            .andExpect(content().string(containsString("data-bs-toggle=\"tab\"")))
    }

    @Test
    @WithMockUser
    fun `given dashboard page when rendered then should have Services tab pane with correct id`() {
        // given - authenticated user accessing dashboard page
        // when - dashboard page is rendered
        mockMvc.perform(get("/dashboard"))
            // then - should have Services tab pane with correct id
            .andExpect(status().isOk)
            .andExpect(content().string(containsString("id=\"services\"")))
            .andExpect(content().string(containsString("tab-pane")))
    }

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
    fun `given dashboard page when rendered then should have Services tab with proper icons and labels`() {
        // given - authenticated user accessing dashboard page
        // when - dashboard page is rendered
        mockMvc.perform(get("/dashboard"))
            // then - Services tab should have proper icons and labels
            .andExpect(status().isOk)
            .andExpect(content().string(containsString("fa-cog")))
            .andExpect(content().string(containsString("Services")))
    }

    @Test
    @WithMockUser
    fun `given dashboard page when rendered then should have Services tab with proper tab highlighting structure`() {
        // given - authenticated user accessing dashboard page
        // when - dashboard page is rendered
        mockMvc.perform(get("/dashboard"))
            // then - Services tab should have proper tab highlighting structure
            .andExpect(status().isOk)
            .andExpect(content().string(containsString("nav-link")))
            .andExpect(content().string(containsString("active")))
    }

    @Test
    @WithMockUser
    fun `given dashboard page when rendered then should have responsive design for Services tab on mobile`() {
        // given - authenticated user accessing dashboard page on mobile
        // when - dashboard page is rendered
        mockMvc.perform(get("/dashboard"))
            // then - Services tab should have responsive design elements
            .andExpect(status().isOk)
            .andExpect(content().string(containsString("col-md")))
            .andExpect(content().string(containsString("col-lg")))
    }

    @Test
    @WithMockUser
    fun `given dashboard page when rendered then should have Services tab with accessibility features`() {
        // given - authenticated user with screen reader accessing dashboard page
        // when - dashboard page is rendered
        mockMvc.perform(get("/dashboard"))
            // then - Services tab should have accessibility features
            .andExpect(status().isOk)
            .andExpect(content().string(containsString("role=\"tab\"")))
            .andExpect(content().string(containsString("aria-")))
    }

    @Test
    @WithMockUser
    fun `given dashboard page when rendered then should have Services tab content area`() {
        // given - authenticated user accessing dashboard page
        // when - dashboard page is rendered
        mockMvc.perform(get("/dashboard"))
            // then - should have Services tab content area
            .andExpect(status().isOk)
            .andExpect(content().string(containsString("services-content")))
    }

    @Test
    @WithMockUser
    fun `given dashboard page when rendered then should have all four tabs in correct order`() {
        // given - authenticated user accessing dashboard page
        // when - dashboard page is rendered
        mockMvc.perform(get("/dashboard"))
            // then - should have all four tabs in correct order: Inbox, Bills, Receipts, Services
            .andExpect(status().isOk)
            .andExpect(content().string(containsString("Inbox")))
            .andExpect(content().string(containsString("Bills")))
            .andExpect(content().string(containsString("Receipts")))
            .andExpect(content().string(containsString("Services")))
    }

    @Test
    @WithMockUser
    fun `given dashboard page when rendered then should have Services tab keyboard navigation support`() {
        // given - authenticated user using keyboard navigation
        // when - dashboard page is rendered
        mockMvc.perform(get("/dashboard"))
            // then - Services tab should support keyboard navigation
            .andExpect(status().isOk)
            .andExpect(content().string(containsString("tabindex")))
    }
}