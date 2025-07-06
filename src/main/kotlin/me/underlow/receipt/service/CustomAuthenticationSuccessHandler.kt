package me.underlow.receipt.service

import org.slf4j.LoggerFactory
import org.springframework.security.core.Authentication
import org.springframework.security.web.authentication.AuthenticationSuccessHandler
import org.springframework.stereotype.Component
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse

/**
 * Custom authentication success handler that handles successful OAuth2 authentication.
 * Redirects users to the dashboard after successful authentication and performs post-authentication logic.
 */
@Component
class CustomAuthenticationSuccessHandler : AuthenticationSuccessHandler {
    
    private val logger = LoggerFactory.getLogger(CustomAuthenticationSuccessHandler::class.java)
    
    /**
     * Handles successful authentication by redirecting to dashboard and performing post-authentication logic.
     * 
     * @param request The HTTP request
     * @param response The HTTP response
     * @param authentication The authentication object containing user details
     */
    override fun onAuthenticationSuccess(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authentication: Authentication
    ) {
        // Perform post-authentication logic
        handlePostAuthentication(authentication)
        
        // Redirect to dashboard
        response.sendRedirect("/dashboard")
    }
    
    /**
     * Handles post-authentication logic such as logging and user activity tracking.
     * 
     * @param authentication The authentication object containing user details
     */
    fun handlePostAuthentication(authentication: Authentication) {
        val username = authentication.name
        logger.info("User '{}' successfully authenticated via OAuth2", username)
        
        // Additional post-authentication logic can be added here:
        // - Update last login timestamp
        // - Log user activity
        // - Initialize user session data
        // - Send authentication notifications
    }
}