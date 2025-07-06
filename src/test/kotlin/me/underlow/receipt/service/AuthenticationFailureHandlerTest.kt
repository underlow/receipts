package me.underlow.receipt.service

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.*
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.authentication.AuthenticationFailureHandler
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.DisabledException
import org.springframework.security.authentication.AccountExpiredException
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse

/**
 * Unit tests for AuthenticationFailureHandler.
 * Tests authentication failure handling and redirection to login page with error parameters.
 */
@ExtendWith(MockitoExtension::class)
class AuthenticationFailureHandlerTest {
    
    @Mock
    private lateinit var request: HttpServletRequest
    
    @Mock
    private lateinit var response: HttpServletResponse
    
    @Mock
    private lateinit var authenticationException: AuthenticationException
    
    private lateinit var authenticationFailureHandler: AuthenticationFailureHandler
    
    @BeforeEach
    fun setUp() {
        authenticationFailureHandler = CustomAuthenticationFailureHandler()
    }
    
    @Test
    fun `given authentication failure when onAuthenticationFailure then redirects to login with error parameter`() {
        // given - authentication failure with exception
        val expectedRedirectUrl = "/login?error=true"
        
        // when - authentication failure occurs
        authenticationFailureHandler.onAuthenticationFailure(request, response, authenticationException)
        
        // then - redirects to login page with error parameter
        verify(response).sendRedirect(expectedRedirectUrl)
    }
    
    @Test
    fun `given bad credentials exception when onAuthenticationFailure then redirects to login with error`() {
        // given - bad credentials authentication failure
        val badCredentialsException = BadCredentialsException("Invalid credentials")
        
        // when - authentication failure occurs with bad credentials
        authenticationFailureHandler.onAuthenticationFailure(request, response, badCredentialsException)
        
        // then - redirects to login page with error parameter
        verify(response).sendRedirect("/login?error=true")
    }
    
    @Test
    fun `given disabled account exception when onAuthenticationFailure then redirects to login with error`() {
        // given - disabled account authentication failure
        val disabledException = DisabledException("Account is disabled")
        
        // when - authentication failure occurs with disabled account
        authenticationFailureHandler.onAuthenticationFailure(request, response, disabledException)
        
        // then - redirects to login page with error parameter
        verify(response).sendRedirect("/login?error=true")
    }
    
    @Test
    fun `given expired account exception when onAuthenticationFailure then redirects to login with error`() {
        // given - expired account authentication failure
        val expiredException = AccountExpiredException("Account has expired")
        
        // when - authentication failure occurs with expired account
        authenticationFailureHandler.onAuthenticationFailure(request, response, expiredException)
        
        // then - redirects to login page with error parameter
        verify(response).sendRedirect("/login?error=true")
    }
    
    @Test
    fun `given authentication failure when onAuthenticationFailure then logs failure event`() {
        // given - authentication failure with exception message
        val customHandler = spy(CustomAuthenticationFailureHandler())
        whenever(authenticationException.message).thenReturn("Authentication failed")
        
        // when - authentication failure occurs
        customHandler.onAuthenticationFailure(request, response, authenticationException)
        
        // then - failure event is logged and redirect occurs
        verify(customHandler).handleAuthenticationFailure(authenticationException)
        verify(response).sendRedirect("/login?error=true")
    }
    
    @Test
    fun `given authentication failure when handleAuthenticationFailure then processes exception details`() {
        // given - authentication failure with specific exception details
        val customHandler = CustomAuthenticationFailureHandler()
        whenever(authenticationException.message).thenReturn("OAuth2 authentication failed")
        
        // when - authentication failure handling is executed
        customHandler.handleAuthenticationFailure(authenticationException)
        
        // then - exception details are processed (verified by no exceptions thrown)
        verify(authenticationException).message
    }
}