package me.underlow.receipt.controller

import me.underlow.receipt.config.SecurityConfiguration
import me.underlow.receipt.service.CustomAuthenticationFailureHandler
import me.underlow.receipt.service.CustomAuthenticationSuccessHandler
import me.underlow.receipt.service.CustomOAuth2UserService
import org.hamcrest.CoreMatchers.containsString
import org.hamcrest.CoreMatchers.not
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.security.test.context.support.WithAnonymousUser
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

/**
 * Test class for login template rendering and functionality.
 * Tests login template with various scenarios including error handling and responsive design.
 */
@ExtendWith(MockitoExtension::class)
@WebMvcTest(LoginController::class)
@Import(SecurityConfiguration::class)
@TestPropertySource(properties = ["spring.profiles.active="])
class LoginTemplateTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockitoBean
    private lateinit var customOAuth2UserService: CustomOAuth2UserService

    @MockitoBean
    private lateinit var customAuthenticationSuccessHandler: CustomAuthenticationSuccessHandler

    @MockitoBean
    private lateinit var customAuthenticationFailureHandler: CustomAuthenticationFailureHandler

    @Test
    @WithAnonymousUser
    fun `should render login template without errors when accessed without parameters`() {
        // Given: User navigates to login page without any parameters
        // When: Login page is requested
        // Then: Login template renders successfully with all required elements
        mockMvc.perform(get("/login"))
            .andExpect(status().isOk)
            .andExpect(view().name("login"))
            .andExpect(content().string(containsString("Login - Receipt Manager")))
            .andExpect(content().string(containsString("Continue with Google")))
            .andExpect(content().string(containsString("Bootstrap")))
            .andExpect(content().string(containsString("viewport")))
    }

    @Test
    @WithAnonymousUser
    fun `should show error message when error parameter is present`() {
        // Given: User attempts authentication and fails
        // When: Login page is requested with error parameter
        // Then: Error message is displayed to inform user of authentication failure
        mockMvc.perform(get("/login").param("error", "true"))
            .andExpect(status().isOk)
            .andExpect(view().name("login"))
            .andExpect(content().string(containsString("Authentication failed")))
            .andExpect(content().string(containsString("alert-danger")))
    }

    @Test
    @WithAnonymousUser
    fun `should hide error message when no error parameter is present`() {
        // Given: User navigates to login page normally
        // When: Login page is requested without error parameter
        // Then: Error message container is hidden or not present
        mockMvc.perform(get("/login"))
            .andExpect(status().isOk)
            .andExpect(view().name("login"))
            .andExpect(content().string(not(containsString("Authentication failed"))))
    }

    @Test
    @WithAnonymousUser
    fun `should show access denied error when access_denied parameter is present`() {
        // Given: User denies OAuth permission request
        // When: Login page is requested with access_denied parameter
        // Then: Access denied message is displayed to inform user
        mockMvc.perform(get("/login").param("error", "access_denied"))
            .andExpect(status().isOk)
            .andExpect(view().name("login"))
            .andExpect(content().string(containsString("Access denied")))
            .andExpect(content().string(containsString("alert-warning")))
    }

    @Test
    @WithAnonymousUser
    fun `should have correct Google OAuth button link`() {
        // Given: User wants to authenticate with Google
        // When: Login page is rendered
        // Then: Google OAuth button links to correct authorization endpoint
        mockMvc.perform(get("/login"))
            .andExpect(status().isOk)
            .andExpect(view().name("login"))
            .andExpect(content().string(containsString("/oauth2/authorization/google")))
            .andExpect(content().string(containsString("btn-google")))
    }

    @Test
    @WithAnonymousUser
    fun `should have responsive design elements`() {
        // Given: User accesses login page on different devices
        // When: Login page is rendered
        // Then: Responsive design elements are present including viewport meta tag and Bootstrap grid
        mockMvc.perform(get("/login"))
            .andExpect(status().isOk)
            .andExpect(view().name("login"))
            .andExpect(content().string(containsString("viewport")))
            .andExpect(content().string(containsString("container")))
            .andExpect(content().string(containsString("col-")))
            .andExpect(content().string(containsString("responsive")))
    }

    @Test
    @WithAnonymousUser
    fun `should have accessibility features`() {
        // Given: User with screen reader or keyboard navigation accesses login page
        // When: Login page is rendered
        // Then: Accessibility features are present including ARIA labels and proper semantic HTML
        mockMvc.perform(get("/login"))
            .andExpect(status().isOk)
            .andExpect(view().name("login"))
            .andExpect(content().string(containsString("aria-label")))
            .andExpect(content().string(containsString("role=")))
            .andExpect(content().string(containsString("lang=")))
            .andExpect(content().string(containsString("alt=")))
    }

    @Test
    @WithAnonymousUser
    fun `should have proper page structure and branding`() {
        // Given: User accesses login page for the first time
        // When: Login page is rendered
        // Then: Page has proper structure with branding and welcome message
        mockMvc.perform(get("/login"))
            .andExpect(status().isOk)
            .andExpect(view().name("login"))
            .andExpect(content().string(containsString("Receipt Manager")))
            .andExpect(content().string(containsString("Welcome")))
            .andExpect(content().string(containsString("card")))
            .andExpect(content().string(containsString("text-center")))
    }
}
