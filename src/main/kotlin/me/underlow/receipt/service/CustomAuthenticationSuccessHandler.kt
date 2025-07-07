package me.underlow.receipt.service

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.security.web.authentication.AuthenticationSuccessHandler
import org.springframework.stereotype.Component

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

        // Extract additional user information for logging
        val userEmail = if (authentication.principal is OAuth2User) {
            (authentication.principal as OAuth2User).getAttribute<String>("email") ?: username
        } else {
            username
        }

        // Log successful authentication with structured data
        logger.info(
            "AUTHENTICATION_SUCCESS: User '{}' (email: '{}') successfully authenticated via OAuth2. Session established.",
            username, userEmail
        )

        // Security event logging
        logger.info(
            "SECURITY_EVENT: type=LOGIN_SUCCESS, user={}, email={}, timestamp={}",
            username, userEmail, System.currentTimeMillis()
        )

    }
}
