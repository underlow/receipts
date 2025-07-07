package me.underlow.receipt.controller

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.security.test.context.support.WithAnonymousUser
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import me.underlow.receipt.service.CustomOAuth2UserService
import me.underlow.receipt.service.CustomAuthenticationSuccessHandler
import me.underlow.receipt.service.CustomAuthenticationFailureHandler
import me.underlow.receipt.config.SecurityConfiguration
import org.springframework.context.annotation.Import

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

    @MockBean
    private lateinit var customOAuth2UserService: CustomOAuth2UserService

    @MockBean
    private lateinit var customAuthenticationSuccessHandler: CustomAuthenticationSuccessHandler

    @MockBean
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
        mockMvc.perform(get("/dashboard"))
            // then - redirects to login page
            .andExpect(status().is3xxRedirection)
            .andExpect(redirectedUrl("http://localhost/login"))
    }
}