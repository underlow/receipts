package me.underlow.receipt.service

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.authentication.AuthenticationFailureHandler
import org.springframework.stereotype.Component

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

        // Set up MDC context for structured logging
        // Log authentication failure with structured data
        logger.warn("AUTHENTICATION_FAILURE: Authentication failed with error: '{}'", errorMessage)

        // Security event logging
        logger.warn(
            "SECURITY_EVENT: type=LOGIN_FAILURE, error='{}', exceptionType={}, timestamp={}",
            errorMessage, exception.javaClass.simpleName, System.currentTimeMillis()
        )

        // Log specific OAuth2 authentication failures
        when (exception.javaClass.simpleName) {
            "OAuth2AuthenticationException" -> {
                logger.warn("OAUTH2_FAILURE: OAuth2 authentication failed - {}", errorMessage)
            }

            "InsufficientAuthenticationException" -> {
                logger.warn("INSUFFICIENT_AUTH: Insufficient authentication - {}", errorMessage)
            }

            "BadCredentialsException" -> {
                logger.warn("BAD_CREDENTIALS: Bad credentials provided - {}", errorMessage)
            }

            else -> {
                logger.warn("UNKNOWN_AUTH_ERROR: Unknown authentication error - {}", errorMessage)
            }
        }
    }

    /**
     * Generates a unique correlation ID for request tracking.
     *
     * @return A unique correlation ID
     */
    private fun generateCorrelationId(): String {
        return java.util.UUID.randomUUID().toString().substring(0, 8)
    }
}
