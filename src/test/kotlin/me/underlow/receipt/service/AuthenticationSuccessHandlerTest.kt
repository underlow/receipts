package me.underlow.receipt.service

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.*
import org.springframework.security.core.Authentication
import org.springframework.security.web.authentication.AuthenticationSuccessHandler
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import kotlin.test.assertEquals

/**
 * Unit tests for AuthenticationSuccessHandler.
 * Tests authentication success handling and redirection to dashboard.
 */
@ExtendWith(MockitoExtension::class)
class AuthenticationSuccessHandlerTest {
    
    @Mock
    private lateinit var request: HttpServletRequest
    
    @Mock
    private lateinit var response: HttpServletResponse
    
    @Mock
    private lateinit var authentication: Authentication
    
    private lateinit var authenticationSuccessHandler: AuthenticationSuccessHandler
    
    @BeforeEach
    fun setUp() {
        authenticationSuccessHandler = CustomAuthenticationSuccessHandler()
    }
    
    @Test
    fun `given successful authentication when onAuthenticationSuccess then redirects to dashboard`() {
        // given - successful authentication with all required parameters
        val expectedRedirectUrl = "/dashboard"
        
        // when - authentication success occurs
        authenticationSuccessHandler.onAuthenticationSuccess(request, response, authentication)
        
        // then - redirects to dashboard page
        verify(response).sendRedirect(expectedRedirectUrl)
    }
    
    @Test
    fun `given successful authentication when onAuthenticationSuccess then calls post-authentication logic`() {
        // given - successful authentication with valid user
        val mockHandler = spy(CustomAuthenticationSuccessHandler())
        
        // when - authentication success occurs
        mockHandler.onAuthenticationSuccess(request, response, authentication)
        
        // then - post-authentication logic is executed
        verify(mockHandler).handlePostAuthentication(authentication)
        verify(response).sendRedirect("/dashboard")
    }
    
    @Test
    fun `given authentication success when handlePostAuthentication then logs authentication event`() {
        // given - successful authentication with user details
        val customHandler = CustomAuthenticationSuccessHandler()
        whenever(authentication.name).thenReturn("test@example.com")
        
        // when - post-authentication logic is executed
        customHandler.handlePostAuthentication(authentication)
        
        // then - authentication event is logged (verified by no exceptions thrown)
        verify(authentication).name
    }
}