package me.underlow.receipt.service

import org.slf4j.LoggerFactory
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.authentication.AuthenticationFailureHandler
import org.springframework.stereotype.Component
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse

/**
 * Custom authentication failure handler that handles failed OAuth2 authentication attempts.
 * Redirects users to the login page with error parameter and logs authentication failures.
 */
@Component
class CustomAuthenticationFailureHandler : AuthenticationFailureHandler {
    
    private val logger = LoggerFactory.getLogger(CustomAuthenticationFailureHandler::class.java)
    
    /**
     * Handles authentication failure by redirecting to login page with error parameter.
     * 
     * @param request The HTTP request
     * @param response The HTTP response
     * @param exception The authentication exception that caused the failure
     */
    override fun onAuthenticationFailure(
        request: HttpServletRequest,
        response: HttpServletResponse,
        exception: AuthenticationException
    ) {
        // Handle authentication failure logic
        handleAuthenticationFailure(exception)
        
        // Redirect to login page with error parameter
        response.sendRedirect("/login?error=true")
    }
    
    /**
     * Handles authentication failure processing such as logging and error analysis.
     * 
     * @param exception The authentication exception containing failure details
     */
    fun handleAuthenticationFailure(exception: AuthenticationException) {
        val errorMessage = exception.message ?: "Unknown authentication error"
        logger.warn("Authentication failure occurred: {}", errorMessage)
        
        // Additional failure handling logic can be added here:
        // - Log security events
        // - Track failed login attempts
        // - Implement rate limiting
        // - Send security notifications
        // - Analyze authentication failure patterns
    }
}