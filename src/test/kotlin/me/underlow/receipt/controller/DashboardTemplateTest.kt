package me.underlow.receipt.controller

import me.underlow.receipt.config.SecurityConfiguration
import me.underlow.receipt.dashboard.BaseTable
import me.underlow.receipt.dashboard.BillsView
import me.underlow.receipt.dashboard.InboxView
import me.underlow.receipt.dashboard.NavigationPanel
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
 * Test class for dashboard template rendering and functionality.
 * Tests dashboard template with various scenarios including user profile display, navigation, and responsive design.
 */
@ExtendWith(MockitoExtension::class)
@WebMvcTest(DashboardController::class)
@Import(SecurityConfiguration::class, MockBillsService::class, MockInboxService::class, BillsView::class, InboxView::class, BaseTable::class, NavigationPanel::class)
class DashboardTemplateTest {

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

    @Test
    @WithMockUser
    fun `should render dashboard template with Bootstrap styling and responsive design`() {
        // Given: Authenticated user accesses dashboard
        // When: Dashboard page is requested
        // Then: Dashboard template renders with Bootstrap styling and responsive design elements
        mockMvc.perform(get("/dashboard"))
            .andExpect(status().isOk)
            .andExpect(view().name("dashboard"))
            .andExpect(content().string(containsString("Dashboard - Receipt Manager")))
            .andExpect(content().string(containsString("Bootstrap")))
            .andExpect(content().string(containsString("viewport")))
            .andExpect(content().string(containsString("container")))
            .andExpect(content().string(containsString("col-")))
    }

    @Test
    @WithMockUser(username = "Unknown User")
    fun `should display user profile information with welcome message`() {
        // Given: Authenticated user with profile information
        // When: Dashboard page is requested
        // Then: User profile information is displayed prominently with welcome message
        mockMvc.perform(get("/dashboard"))
            .andExpect(status().isOk)
            .andExpect(view().name("dashboard"))
            .andExpect(content().string(containsString("Welcome")))
            .andExpect(content().string(containsString("Unknown User")))
            .andExpect(content().string(containsString("user")))
    }

    @Test
    @WithMockUser
    fun `should display user avatar with fallback for missing avatar`() {
        // Given: Authenticated user with or without avatar
        // When: Dashboard page is requested
        // Then: User avatar is displayed if available, with graceful fallback if missing
        mockMvc.perform(get("/dashboard"))
            .andExpect(status().isOk)
            .andExpect(view().name("dashboard"))
            .andExpect(content().string(containsString("user-avatar")))
            .andExpect(content().string(containsString("avatar")))
            .andExpect(content().string(containsString("profile")))
    }

    @Test
    @WithMockUser
    fun `should have navigation menu with profile and settings links`() {
        // Given: Authenticated user navigates dashboard
        // When: Dashboard page is rendered
        // Then: Navigation menu contains links to profile and settings pages
        mockMvc.perform(get("/dashboard"))
            .andExpect(status().isOk)
            .andExpect(view().name("dashboard"))
            .andExpect(content().string(containsString("profile")))
            .andExpect(content().string(containsString("settings")))
            .andExpect(content().string(containsString("nav")))
    }

    @Test
    @WithMockUser
    fun `should have logout button with proper CSRF protection`() {
        // Given: Authenticated user wants to logout
        // When: Dashboard page is rendered
        // Then: Logout button is present with proper CSRF protection
        mockMvc.perform(get("/dashboard"))
            .andExpect(status().isOk)
            .andExpect(view().name("dashboard"))
            .andExpect(content().string(containsString("logout")))
            .andExpect(content().string(containsString("_csrf")))
            .andExpect(content().string(containsString("method=\"post\"")))
    }

    @Test
    @WithMockUser
    fun `should have placeholder content areas for future features`() {
        // Given: User accesses dashboard for receipt management
        // When: Dashboard page is rendered
        // Then: Placeholder content areas are present for future receipt management features
        mockMvc.perform(get("/dashboard"))
            .andExpect(status().isOk)
            .andExpect(view().name("dashboard"))
            .andExpect(content().string(containsString("placeholder")))
            .andExpect(content().string(containsString("receipt")))
            .andExpect(content().string(containsString("management")))
    }

    @Test
    @WithMockUser
    fun `should have responsive design elements for mobile devices`() {
        // Given: User accesses dashboard on mobile device
        // When: Dashboard page is rendered
        // Then: Responsive design elements are present including mobile navigation
        mockMvc.perform(get("/dashboard"))
            .andExpect(status().isOk)
            .andExpect(view().name("dashboard"))
            .andExpect(content().string(containsString("col-md")))
            .andExpect(content().string(containsString("col-lg")))
            .andExpect(content().string(containsString("navbar-toggler")))
    }

    @Test
    @WithMockUser
    fun `should have accessibility features for screen readers`() {
        // Given: User with screen reader accesses dashboard
        // When: Dashboard page is rendered
        // Then: Accessibility features are present including ARIA labels and semantic HTML
        mockMvc.perform(get("/dashboard"))
            .andExpect(status().isOk)
            .andExpect(view().name("dashboard"))
            .andExpect(content().string(containsString("aria-label")))
            .andExpect(content().string(containsString("aria-current")))
            .andExpect(content().string(containsString("lang=\"en\"")))
            .andExpect(content().string(containsString("alt=")))
    }

    @Test
    @WithMockUser
    fun `should have consistent styling with application branding`() {
        // Given: User accesses dashboard after login
        // When: Dashboard page is rendered
        // Then: Page has consistent styling with application branding and color scheme
        mockMvc.perform(get("/dashboard"))
            .andExpect(status().isOk)
            .andExpect(view().name("dashboard"))
            .andExpect(content().string(containsString("Receipt Manager")))
            .andExpect(content().string(containsString("card-title")))
            .andExpect(content().string(containsString("text-muted")))
    }

    @Test
    @WithMockUser
    fun `should have user dropdown menu functionality`() {
        // Given: User wants to access profile options
        // When: Dashboard page is rendered
        // Then: User dropdown menu is present with profile options
        mockMvc.perform(get("/dashboard"))
            .andExpect(status().isOk)
            .andExpect(view().name("dashboard"))
            .andExpect(content().string(containsString("dropdown")))
            .andExpect(content().string(containsString("user")))
            .andExpect(content().string(containsString("menu")))
    }
}
