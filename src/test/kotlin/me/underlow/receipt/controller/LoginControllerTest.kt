package me.underlow.receipt.controller

import org.junit.jupiter.api.BeforeEach
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
 * Unit tests for LoginController.
 * Tests login page rendering and authentication-based routing functionality.
 */
@ExtendWith(MockitoExtension::class)
@WebMvcTest(LoginController::class)
@Import(SecurityConfiguration::class)
class LoginControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockBean
    private lateinit var customOAuth2UserService: CustomOAuth2UserService

    @MockBean
    private lateinit var customAuthenticationSuccessHandler: CustomAuthenticationSuccessHandler

    @MockBean
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
}