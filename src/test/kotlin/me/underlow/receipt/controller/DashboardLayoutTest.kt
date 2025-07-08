package me.underlow.receipt.controller

import me.underlow.receipt.config.SecurityConfiguration
import me.underlow.receipt.dashboard.BaseTable
import me.underlow.receipt.dashboard.BillsView
import me.underlow.receipt.dashboard.InboxView
import me.underlow.receipt.service.CustomAuthenticationFailureHandler
import me.underlow.receipt.service.CustomAuthenticationSuccessHandler
import me.underlow.receipt.service.CustomOAuth2UserService
import me.underlow.receipt.service.MockBillsService
import me.underlow.receipt.service.MockInboxService
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
 * Unit tests for DashboardLayout component.
 * Tests dashboard layout structure, responsive behavior, and CSS classes.
 */
@ExtendWith(MockitoExtension::class)
@WebMvcTest(DashboardController::class)
@Import(SecurityConfiguration::class, MockBillsService::class, MockInboxService::class, BillsView::class, InboxView::class, BaseTable::class)
class DashboardLayoutTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockitoBean
    private lateinit var customOAuth2UserService: CustomOAuth2UserService

    @MockitoBean
    private lateinit var customAuthenticationSuccessHandler: CustomAuthenticationSuccessHandler

    @MockitoBean
    private lateinit var customAuthenticationFailureHandler: CustomAuthenticationFailureHandler

    @Autowired
    private lateinit var mockInboxService: MockInboxService

    @Autowired
    private lateinit var mockBillsService: MockBillsService

    @Autowired
    private lateinit var inboxView: InboxView

    @Autowired
    private lateinit var billsView: BillsView

    @Autowired
    private lateinit var baseTable: BaseTable

    @Test
    @WithMockUser
    fun `given dashboard page when rendered then should have correct three-panel structure`() {
        // given - authenticated user accessing dashboard page
        // when - GET request to /dashboard
        mockMvc.perform(get("/dashboard"))
            // then - returns dashboard template with three-panel layout structure
            .andExpect(status().isOk)
            .andExpect(view().name("dashboard"))
            .andExpect(content().string(containsString("dashboard-layout")))
            .andExpect(content().string(containsString("top-bar")))
            .andExpect(content().string(containsString("navigation-panel")))
            .andExpect(content().string(containsString("content-area")))
    }

    @Test
    @WithMockUser
    fun `given dashboard page when rendered then should have responsive CSS classes`() {
        // given - authenticated user accessing dashboard page
        // when - GET request to /dashboard
        mockMvc.perform(get("/dashboard"))
            // then - returns dashboard template with responsive CSS classes
            .andExpect(status().isOk)
            .andExpect(content().string(containsString("dashboard-layout")))
            .andExpect(content().string(containsString("col-")))
            .andExpect(content().string(containsString("d-flex")))
    }

    @Test
    @WithMockUser
    fun `given dashboard page when rendered then should have proper grid layout`() {
        // given - authenticated user accessing dashboard page
        // when - GET request to /dashboard
        mockMvc.perform(get("/dashboard"))
            // then - returns dashboard template with proper grid layout
            .andExpect(status().isOk)
            .andExpect(content().string(containsString("container-fluid")))
            .andExpect(content().string(containsString("row")))
    }

    @Test
    @WithMockUser
    fun `given dashboard page when rendered then should have collapsible navigation for mobile`() {
        // given - authenticated user accessing dashboard page
        // when - GET request to /dashboard
        mockMvc.perform(get("/dashboard"))
            // then - returns dashboard template with collapsible navigation
            .andExpect(status().isOk)
            .andExpect(content().string(containsString("navbar-toggler")))
            .andExpect(content().string(containsString("collapse")))
    }

    @Test
    @WithMockUser
    fun `given dashboard page when rendered then should have proper semantic HTML structure`() {
        // given - authenticated user accessing dashboard page
        // when - GET request to /dashboard
        mockMvc.perform(get("/dashboard"))
            // then - returns dashboard template with proper semantic HTML
            .andExpect(status().isOk)
            .andExpect(content().string(containsString("<header")))
            .andExpect(content().string(containsString("<nav")))
            .andExpect(content().string(containsString("<main")))
            .andExpect(content().string(containsString("<aside")))
    }
}
