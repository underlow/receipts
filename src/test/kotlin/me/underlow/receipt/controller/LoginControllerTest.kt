package me.underlow.receipt.controller

import me.underlow.receipt.config.SecurityConfiguration
import me.underlow.receipt.service.CustomAuthenticationFailureHandler
import me.underlow.receipt.service.CustomAuthenticationSuccessHandler
import me.underlow.receipt.service.CustomOAuth2UserService
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.security.test.context.support.WithAnonymousUser
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

/**
 * Unit tests for LoginController.
 * Tests login page rendering and authentication-based routing functionality.
 */
@ExtendWith(MockitoExtension::class)
@WebMvcTest(LoginController::class)
@Import(SecurityConfiguration::class)
@TestPropertySource(properties = ["spring.profiles.active="])
class LoginControllerTest {

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
    fun `given unauthenticated user when GET login then should return login template`() {
        // given - unauthenticated user accessing login page
        // when - GET request to /login
        mockMvc.perform(get("/login"))
            // then - returns login template
            .andExpect(status().isOk)
            .andExpect(view().name("login"))
    }

    @Test
    @WithMockUser
    fun `given authenticated user when GET login then should redirect to dashboard`() {
        // given - authenticated user accessing login page
        // when - GET request to /login
        mockMvc.perform(get("/login"))
            // then - redirects to dashboard
            .andExpect(status().is3xxRedirection)
            .andExpect(redirectedUrl("/dashboard"))
    }

    @Test
    @WithAnonymousUser
    fun `given unauthenticated user when GET login with error param then should add error to model`() {
        // given - unauthenticated user accessing login page with error parameter
        // when - GET request to /login with error parameter
        mockMvc.perform(get("/login").param("error", "login_failed"))
            // then - returns login template with error message in model
            .andExpect(status().isOk)
            .andExpect(view().name("login"))
            .andExpect(model().attributeExists("errorMessage"))
            .andExpect(model().attribute("errorMessage", "Authentication failed. Please try again."))
    }

    @Test
    @WithAnonymousUser
    fun `given unauthenticated user when GET login with different error types then should handle various error messages`() {
        // given - unauthenticated user accessing login page with different error types

        // when - GET request with access_denied error
        mockMvc.perform(get("/login").param("error", "access_denied"))
            // then - returns login template with access denied message
            .andExpect(status().isOk)
            .andExpect(view().name("login"))
            .andExpect(model().attributeExists("errorMessage"))
            .andExpect(model().attribute("errorMessage", "Access denied. Your email is not in the allowlist."))

        // when - GET request with invalid_request error
        mockMvc.perform(get("/login").param("error", "invalid_request"))
            // then - returns login template with invalid request message
            .andExpect(status().isOk)
            .andExpect(view().name("login"))
            .andExpect(model().attributeExists("errorMessage"))
            .andExpect(model().attribute("errorMessage", "Invalid request. Please try again."))

        // when - GET request with unknown error
        mockMvc.perform(get("/login").param("error", "unknown_error"))
            // then - returns login template with generic error message
            .andExpect(status().isOk)
            .andExpect(view().name("login"))
            .andExpect(model().attributeExists("errorMessage"))
            .andExpect(model().attribute("errorMessage", "An error occurred during login. Please try again."))
    }
}
