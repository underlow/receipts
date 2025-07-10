package me.underlow.receipt.e2e

import me.underlow.receipt.service.CustomAuthenticationFailureHandler
import me.underlow.receipt.service.CustomAuthenticationSuccessHandler
import me.underlow.receipt.service.CustomOAuth2UserService
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken
import org.springframework.security.oauth2.core.user.DefaultOAuth2User
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.security.test.context.support.WithAnonymousUser
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers

/**
 * End-to-end tests for controller interactions and complete user flows.
 * Tests complex redirect scenarios, template rendering with correct data, and error handling.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ExtendWith(MockitoExtension::class)
@ActiveProfiles("test")
@Testcontainers
@TestPropertySource(
    properties = [
        "spring.security.oauth2.client.provider.google.issuer-uri=https://accounts.google.com",
        "spring.security.oauth2.client.registration.google.client-id=test-client-id",
        "spring.security.oauth2.client.registration.google.client-secret=test-secret"
    ]
)
class ControllerE2ETest {

    companion object {
        @Container
        @JvmStatic
        val postgres = PostgreSQLContainer<Nothing>("postgres:15-alpine").apply {
            withDatabaseName("testdb")
            withUsername("test")
            withPassword("test")
        }

        @JvmStatic
        @DynamicPropertySource
        fun configureProperties(registry: DynamicPropertyRegistry) {
            registry.add("spring.datasource.url", postgres::getJdbcUrl)
            registry.add("spring.datasource.username", postgres::getUsername)
            registry.add("spring.datasource.password", postgres::getPassword)
        }
    }

    @MockitoBean
    private lateinit var customOAuth2UserService: CustomOAuth2UserService

    @MockitoBean
    private lateinit var customAuthenticationSuccessHandler: CustomAuthenticationSuccessHandler

    @MockitoBean
    private lateinit var customAuthenticationFailureHandler: CustomAuthenticationFailureHandler

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Test
    @WithAnonymousUser
    fun `given unauthenticated user when accessing protected pages then should handle redirect chains correctly`() {
        // Given: unauthenticated user trying to access protected pages
        // When: accessing dashboard (should redirect to login)
        mockMvc.perform(MockMvcRequestBuilders.get("/dashboard"))
            // Then: redirects to login page
            .andExpect(MockMvcResultMatchers.status().is3xxRedirection)
            .andExpect(MockMvcResultMatchers.redirectedUrl("http://localhost/login"))

        // When: accessing profile (should redirect to login)
        mockMvc.perform(MockMvcRequestBuilders.get("/profile"))
            // Then: redirects to login page
            .andExpect(MockMvcResultMatchers.status().is3xxRedirection)
            .andExpect(MockMvcResultMatchers.redirectedUrl("http://localhost/login"))

        // When: accessing settings (should redirect to login)
        mockMvc.perform(MockMvcRequestBuilders.get("/settings"))
            // Then: redirects to login page
            .andExpect(MockMvcResultMatchers.status().is3xxRedirection)
            .andExpect(MockMvcResultMatchers.redirectedUrl("http://localhost/login"))

        // When: accessing login page (should show login)
        mockMvc.perform(MockMvcRequestBuilders.get("/login"))
            // Then: shows login page
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.view().name("login"))
    }

    @Test
    fun `given authenticated user when accessing login then should redirect to dashboard creating redirect chain`() {
        // Given: authenticated OAuth2 user
        val oAuth2User = createMockOAuth2User("test@example.com", "Test User", "https://example.com/avatar.jpg")
        val authentication =
            OAuth2AuthenticationToken(oAuth2User, listOf(SimpleGrantedAuthority("ROLE_USER")), "google")

        // When: authenticated user tries to access login
        mockMvc.perform(
            MockMvcRequestBuilders.get("/login")
                .with(SecurityMockMvcRequestPostProcessors.authentication(authentication)))
            // Then: redirects to dashboard
            .andExpect(MockMvcResultMatchers.status().is3xxRedirection)
            .andExpect(MockMvcResultMatchers.redirectedUrl("/dashboard"))

        // When: following redirect to dashboard
        mockMvc.perform(
            MockMvcRequestBuilders.get("/dashboard")
                .with(SecurityMockMvcRequestPostProcessors.authentication(authentication)))
            // Then: shows dashboard page
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.view().name("dashboard"))
    }

    @Test
    fun `given authenticated user when accessing various pages then should render templates with correct data`() {
        // Given: authenticated OAuth2 user with complete profile
        val oAuth2User = createMockOAuth2User("john.doe@example.com", "John Doe", "https://example.com/john.jpg")
        val authentication =
            OAuth2AuthenticationToken(oAuth2User, listOf(SimpleGrantedAuthority("ROLE_USER")), "google")

        // When: accessing dashboard
        mockMvc.perform(
            MockMvcRequestBuilders.get("/dashboard")
                .with(SecurityMockMvcRequestPostProcessors.authentication(authentication)))
            // Then: renders dashboard template with correct user data
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.view().name("dashboard"))
            .andExpect(MockMvcResultMatchers.model().attribute("userName", "John Doe"))
            .andExpect(MockMvcResultMatchers.model().attribute("userEmail", "john.doe@example.com"))
            .andExpect(MockMvcResultMatchers.model().attribute("userAvatar", "https://example.com/john.jpg"))

        // When: accessing profile page
        mockMvc.perform(
            MockMvcRequestBuilders.get("/profile")
                .with(SecurityMockMvcRequestPostProcessors.authentication(authentication)))
            // Then: renders profile template with correct user data
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.view().name("profile"))
            .andExpect(MockMvcResultMatchers.model().attribute("userEmail", "john.doe@example.com"))
            .andExpect(MockMvcResultMatchers.model().attribute("userName", "John Doe"))
            .andExpect(MockMvcResultMatchers.model().attribute("userAvatar", "https://example.com/john.jpg"))

        // When: accessing settings page
        mockMvc.perform(
            MockMvcRequestBuilders.get("/settings")
                .with(SecurityMockMvcRequestPostProcessors.authentication(authentication)))
            // Then: renders settings template with correct user data
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.view().name("settings"))
            .andExpect(MockMvcResultMatchers.model().attribute("userEmail", "john.doe@example.com"))
            .andExpect(MockMvcResultMatchers.model().attribute("userName", "John Doe"))
            .andExpect(MockMvcResultMatchers.model().attribute("userAvatar", "https://example.com/john.jpg"))
    }

    @Test
    fun `given authenticated user with incomplete profile when accessing pages then should handle missing data gracefully`() {
        // Given: authenticated OAuth2 user with missing profile attributes
        val oAuth2User = createMockOAuth2UserWithMissingAttributes("jane@example.com")
        val authentication =
            OAuth2AuthenticationToken(oAuth2User, listOf(SimpleGrantedAuthority("ROLE_USER")), "google")

        // When: accessing dashboard
        mockMvc.perform(
            MockMvcRequestBuilders.get("/dashboard")
                .with(SecurityMockMvcRequestPostProcessors.authentication(authentication)))
            // Then: renders dashboard template with fallback values
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.view().name("dashboard"))
            .andExpect(MockMvcResultMatchers.model().attribute("userName", "Unknown User"))
            .andExpect(MockMvcResultMatchers.model().attribute("userEmail", "jane@example.com"))
            .andExpect(MockMvcResultMatchers.model().attribute("userAvatar", ""))

        // When: accessing profile page
        mockMvc.perform(
            MockMvcRequestBuilders.get("/profile")
                .with(SecurityMockMvcRequestPostProcessors.authentication(authentication)))
            // Then: renders profile template with available data
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.view().name("profile"))
            .andExpect(MockMvcResultMatchers.model().attribute("userEmail", "jane@example.com"))
            .andExpect(MockMvcResultMatchers.model().attribute("userName", "Unknown User"))
            .andExpect(MockMvcResultMatchers.model().attribute("userAvatar", ""))
    }

    @Test
    @WithAnonymousUser
    fun `given various error scenarios when accessing login then should handle error scenarios gracefully`() {
        // Given: unauthenticated user encountering different error scenarios

        // When: accessing login with access_denied error
        mockMvc.perform(MockMvcRequestBuilders.get("/login").param("error", "access_denied"))
            // Then: shows login page with appropriate error message
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.view().name("login"))
            .andExpect(MockMvcResultMatchers.model().attributeExists("errorMessage"))
            .andExpect(MockMvcResultMatchers.model().attribute("errorMessage", "Access denied. Your email is not in the allowlist."))

        // When: accessing login with invalid_request error
        mockMvc.perform(MockMvcRequestBuilders.get("/login").param("error", "invalid_request"))
            // Then: shows login page with appropriate error message
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.view().name("login"))
            .andExpect(MockMvcResultMatchers.model().attributeExists("errorMessage"))
            .andExpect(MockMvcResultMatchers.model().attribute("errorMessage", "Invalid request. Please try again."))

        // When: accessing login with login_failed error
        mockMvc.perform(MockMvcRequestBuilders.get("/login").param("error", "login_failed"))
            // Then: shows login page with appropriate error message
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.view().name("login"))
            .andExpect(MockMvcResultMatchers.model().attributeExists("errorMessage"))
            .andExpect(MockMvcResultMatchers.model().attribute("errorMessage", "Authentication failed. Please try again."))

        // When: accessing login with unknown error
        mockMvc.perform(MockMvcRequestBuilders.get("/login").param("error", "unknown_error"))
            // Then: shows login page with generic error message
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.view().name("login"))
            .andExpect(MockMvcResultMatchers.model().attributeExists("errorMessage"))
            .andExpect(MockMvcResultMatchers.model().attribute("errorMessage", "An error occurred during login. Please try again."))
    }

    @Test
    fun `given complex user journey when navigating through application then should maintain correct state throughout`() {
        // Given: authenticated OAuth2 user
        val oAuth2User = createMockOAuth2User("user@example.com", "Test User", "https://example.com/avatar.jpg")
        val authentication =
            OAuth2AuthenticationToken(oAuth2User, listOf(SimpleGrantedAuthority("ROLE_USER")), "google")

        // When: user tries to access login (should redirect to dashboard)
        mockMvc.perform(
            MockMvcRequestBuilders.get("/login")
                .with(SecurityMockMvcRequestPostProcessors.authentication(authentication)))
            // Then: redirects to dashboard
            .andExpect(MockMvcResultMatchers.status().is3xxRedirection)
            .andExpect(MockMvcResultMatchers.redirectedUrl("/dashboard"))

        // When: user accesses dashboard
        mockMvc.perform(
            MockMvcRequestBuilders.get("/dashboard")
                .with(SecurityMockMvcRequestPostProcessors.authentication(authentication)))
            // Then: shows dashboard with correct user data
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.view().name("dashboard"))
            .andExpect(MockMvcResultMatchers.model().attribute("userName", "Test User"))

        // When: user navigates to profile
        mockMvc.perform(
            MockMvcRequestBuilders.get("/profile")
                .with(SecurityMockMvcRequestPostProcessors.authentication(authentication)))
            // Then: shows profile with correct user data
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.view().name("profile"))
            .andExpect(MockMvcResultMatchers.model().attribute("userName", "Test User"))
            .andExpect(MockMvcResultMatchers.model().attribute("userEmail", "user@example.com"))
            .andExpect(MockMvcResultMatchers.model().attribute("userAvatar", "https://example.com/avatar.jpg"))

        // When: user navigates to settings
        mockMvc.perform(
            MockMvcRequestBuilders.get("/settings")
                .with(SecurityMockMvcRequestPostProcessors.authentication(authentication)))
            // Then: shows settings with correct user data
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.view().name("settings"))
            .andExpect(MockMvcResultMatchers.model().attribute("userName", "Test User"))
            .andExpect(MockMvcResultMatchers.model().attribute("userEmail", "user@example.com"))
            .andExpect(MockMvcResultMatchers.model().attribute("userAvatar", "https://example.com/avatar.jpg"))
    }

    /**
     * Creates a mock OAuth2User with complete profile information.
     */
    private fun createMockOAuth2User(email: String, name: String, picture: String): OAuth2User {
        val attributes = mapOf(
            "email" to email,
            "name" to name,
            "picture" to picture,
            "sub" to "123456789"
        )
        return DefaultOAuth2User(
            listOf(SimpleGrantedAuthority("ROLE_USER")),
            attributes,
            "email"
        )
    }

    /**
     * Creates a mock OAuth2User with missing profile attributes for testing fallback behavior.
     */
    private fun createMockOAuth2UserWithMissingAttributes(email: String): OAuth2User {
        val attributes = mapOf(
            "email" to email,
            "sub" to "123456789"
            // Missing "name" and "picture" attributes
        )
        return DefaultOAuth2User(
            listOf(SimpleGrantedAuthority("ROLE_USER")),
            attributes,
            "email"
        )
    }
}
