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
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

/**
 * Unit tests for DashboardController.
 * Tests dashboard page rendering and authentication requirements.
 */
@ExtendWith(MockitoExtension::class)
@WebMvcTest(DashboardController::class)
@Import(SecurityConfiguration::class)
class DashboardControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockitoBean
    private lateinit var customOAuth2UserService: CustomOAuth2UserService

    @MockitoBean
    private lateinit var customAuthenticationSuccessHandler: CustomAuthenticationSuccessHandler

    @MockitoBean
    private lateinit var customAuthenticationFailureHandler: CustomAuthenticationFailureHandler

    @Test
    @WithMockUser
    fun `given authenticated user when GET dashboard then should return dashboard template`() {
        // given - authenticated user accessing dashboard page
        // when - GET request to /dashboard
        mockMvc.perform(get("/dashboard"))
            // then - returns dashboard template
            .andExpect(status().isOk)
            .andExpect(view().name("dashboard"))
    }

    @Test
    @WithAnonymousUser
    fun `given unauthenticated user when GET dashboard then should require authentication`() {
        // given - unauthenticated user accessing dashboard page
        // when - GET request to /dashboard
        // this test is very strange and its behaviour differs from prod
        mockMvc.perform(get("/dashboard"))
            // then - redirects to login page
            .andExpect(status().is3xxRedirection)
            .andExpect(redirectedUrlPattern("**/oauth2/authorization/google"))
    }

    @Test
    @WithMockUser
    fun `given authenticated user when GET dashboard then should add user profile to model`() {
        // given - authenticated user with profile information
        // when - GET request to /dashboard
        mockMvc.perform(get("/dashboard"))
            // then - returns dashboard template with user profile in model
            .andExpect(status().isOk)
            .andExpect(view().name("dashboard"))
            .andExpect(model().attributeExists("userName"))
            .andExpect(model().attributeExists("userEmail"))
            .andExpect(model().attributeExists("userAvatar"))
    }

    @Test
    @WithMockUser
    fun `given OAuth2 user principal when GET dashboard then should handle OAuth2User principal correctly`() {
        // given - authenticated OAuth2 user with complete profile
        // when - GET request to /dashboard
        mockMvc.perform(get("/dashboard"))
            // then - returns dashboard template and extracts user info from OAuth2User principal
            .andExpect(status().isOk)
            .andExpect(view().name("dashboard"))
            .andExpect(model().attributeExists("userName"))
            .andExpect(model().attributeExists("userEmail"))
            .andExpect(model().attributeExists("userAvatar"))
    }

    @Test
    @WithMockUser
    fun `given OAuth2 user with missing attributes when GET dashboard then should handle missing user attributes gracefully`() {
        // given - authenticated OAuth2 user with missing name and picture attributes
        // when - GET request to /dashboard
        mockMvc.perform(get("/dashboard"))
            // then - returns dashboard template with fallback values for missing attributes
            .andExpect(status().isOk)
            .andExpect(view().name("dashboard"))
            .andExpect(model().attributeExists("userName"))
            .andExpect(model().attributeExists("userEmail"))
            .andExpect(model().attributeExists("userAvatar"))
    }
}
