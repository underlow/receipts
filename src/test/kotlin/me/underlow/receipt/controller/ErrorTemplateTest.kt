package me.underlow.receipt.controller

import jakarta.servlet.RequestDispatcher
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
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

/**
 * Test class for error template rendering and functionality.
 * Tests error templates for various HTTP status codes with proper error handling and user experience.
 */
@ExtendWith(MockitoExtension::class)
@WebMvcTest(CustomErrorController::class)
@Import(SecurityConfiguration::class)
@TestPropertySource(properties = ["spring.profiles.active="])
class ErrorTemplateTest {

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
    fun `should render 404 error template when not found error occurs`() {
        // Given: User requests a non-existent page
        // When: Error page is requested with 404 status
        // Then: 404 error template renders with appropriate message and navigation
        mockMvc.perform(
            get("/error")
                .requestAttr(RequestDispatcher.ERROR_STATUS_CODE, 404)
                .requestAttr(RequestDispatcher.ERROR_REQUEST_URI, "/nonexistent")
        )
            .andExpect(status().isOk)
            .andExpect(view().name("error/404"))
            .andExpect(model().attribute("statusCode", 404))
            .andExpect(model().attribute("errorTitle", "Page Not Found"))
            .andExpect(model().attribute("errorDescription", "The page you are looking for does not exist."))
    }

    @Test
    @WithAnonymousUser
    fun `should render 403 error template when access denied error occurs`() {
        // Given: User attempts to access forbidden resource
        // When: Error page is requested with 403 status
        // Then: 403 error template renders with access denied message and navigation
        mockMvc.perform(
            get("/error")
                .requestAttr(RequestDispatcher.ERROR_STATUS_CODE, 403)
                .requestAttr(RequestDispatcher.ERROR_REQUEST_URI, "/admin")
        )
            .andExpect(status().isOk)
            .andExpect(view().name("error/403"))
            .andExpect(model().attribute("statusCode", 403))
            .andExpect(model().attribute("errorTitle", "Access Denied"))
            .andExpect(model().attribute("errorDescription", "You do not have permission to access this resource."))
    }

    @Test
    @WithAnonymousUser
    fun `should render general error template when server error occurs`() {
        // Given: Application encounters internal server error
        // When: Error page is requested with 500 status
        // Then: General error template renders with server error message and navigation
        mockMvc.perform(
            get("/error")
                .requestAttr(RequestDispatcher.ERROR_STATUS_CODE, 500)
                .requestAttr(RequestDispatcher.ERROR_MESSAGE, "Internal Server Error")
        )
            .andExpect(status().isOk)
            .andExpect(view().name("error/error"))
            .andExpect(model().attribute("statusCode", 500))
            .andExpect(model().attribute("errorTitle", "Internal Server Error"))
            .andExpect(model().attribute("errorDescription", "An unexpected error occurred. Please try again later."))
    }

    @Test
    @WithAnonymousUser
    fun `should render general error template when unknown error occurs`() {
        // Given: Application encounters unknown error
        // When: Error page is requested with unknown status
        // Then: General error template renders with generic error message
        mockMvc.perform(
            get("/error")
                .requestAttr(RequestDispatcher.ERROR_STATUS_CODE, 999)
        )
            .andExpect(status().isOk)
            .andExpect(view().name("error/error"))
            .andExpect(model().attribute("statusCode", 999))
            .andExpect(model().attribute("errorTitle", "Internal Server Error"))
            .andExpect(model().attribute("errorDescription", "An unexpected error occurred. Please try again later."))
    }

    @Test
    @WithAnonymousUser
    fun `should handle null status code gracefully`() {
        // Given: Error occurs without status code information
        // When: Error page is requested without status code
        // Then: General error template renders with default 500 status
        mockMvc.perform(get("/error"))
            .andExpect(status().isOk)
            .andExpect(view().name("error/error"))
            .andExpect(model().attribute("statusCode", 500))
            .andExpect(model().attribute("errorTitle", "Internal Server Error"))
    }

    @Test
    @WithAnonymousUser
    fun `should include error details in model`() {
        // Given: Error occurs with detailed information
        // When: Error page is requested with error details
        // Then: Error details are properly included in the model for template rendering
        mockMvc.perform(
            get("/error")
                .requestAttr(RequestDispatcher.ERROR_STATUS_CODE, 404)
                .requestAttr(RequestDispatcher.ERROR_MESSAGE, "Resource not found")
                .requestAttr(RequestDispatcher.ERROR_REQUEST_URI, "/missing-page")
        )
            .andExpect(status().isOk)
            .andExpect(view().name("error/404"))
            .andExpect(model().attribute("statusCode", 404))
            .andExpect(model().attribute("errorMessage", "Resource not found"))
            .andExpect(model().attribute("requestUri", "/missing-page"))
            .andExpect(model().attributeExists("timestamp"))
    }
}
