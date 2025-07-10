package me.underlow.receipt.service

import org.slf4j.LoggerFactory
import org.slf4j.MDC
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest
import org.springframework.security.oauth2.core.OAuth2AuthenticationException
import org.springframework.security.oauth2.core.oidc.user.OidcUser
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.stereotype.Service

/**
 * Custom OAuth2 user service that extends DefaultOAuth2UserService.
 * Handles OAuth2 user loading with email validation and user management.
 * Validates users against an email allowlist and creates/updates users in the database.
 */
@Service
class CustomOAuth2UserService(
    private val userService: UserService
) : OidcUserService()  {

    private val logger = LoggerFactory.getLogger(CustomOAuth2UserService::class.java)

    /**
     * Loads an OAuth2 user and validates against email allowlist.
     * Creates or updates the user in the database if email is allowed.
     *
     * @param userRequest The OAuth2 user request containing client registration and access token
     * @return The authenticated OAuth2 user
     * @throws OAuth2AuthenticationException if email is not allowed or required attributes are missing
     */
    override fun loadUser(userRequest: OidcUserRequest): OidcUser {

        try {
            logger.debug("OAUTH2_USER_LOADING: Starting OAuth2 user loading process")

            // Load the OAuth2 user from the parent class
            val oauth2User = super.loadUser(userRequest)

            // Extract user attributes from OAuth2 response
            val attributes = oauth2User.attributes
            logger.debug("OAUTH2_ATTRIBUTES: Received OAuth2 attributes: {}", attributes.keys)

            // Extract email (required)
            val email = attributes["email"] as? String
            if (email.isNullOrBlank()) {
                logger.error("OAUTH2_ERROR: Email attribute is required but not found or empty")
                throw OAuth2AuthenticationException("Email attribute is required but not found or empty")
            }

            // Extract name (required)
            val name = attributes["name"] as? String
            if (name.isNullOrBlank()) {
                logger.error("OAUTH2_ERROR: Name attribute is required but not found or empty")
                throw OAuth2AuthenticationException("Name attribute is required but not found or empty")
            }

            // Extract avatar (optional)
            val avatar = attributes["picture"] as? String

            logger.info("OAUTH2_USER_EXTRACTED: email={}, name={}, hasAvatar={}",
                       email, name, avatar != null)

            // Validate email against allowlist
            if (!userService.isEmailAllowed(email)) {
                logger.warn("OAUTH2_AUTHORIZATION_DENIED: Email '{}' is not in the allowlist", email)
                logger.warn("SECURITY_EVENT: type=UNAUTHORIZED_ACCESS_ATTEMPT, email={}, timestamp={}",
                           email, System.currentTimeMillis())
                throw OAuth2AuthenticationException("Email $email is not in the allowlist")
            }

            logger.info("OAUTH2_AUTHORIZATION_GRANTED: Email '{}' is authorized", email)

            // Create or update user in database
            userService.createOrUpdateUser(email, name, avatar)
            logger.info("OAUTH2_USER_PROCESSED: User '{}' created/updated in database", email)

            logger.info("OAUTH2_USER_LOADING_SUCCESS: OAuth2 user loading completed successfully for '{}'", email)
            return oauth2User

        } catch (exception: OAuth2AuthenticationException) {
            logger.error("OAUTH2_USER_LOADING_FAILURE: OAuth2 user loading failed: {}", exception.message)
            throw exception
        }
    }

    /**
     * Helper method to call the parent class loadUser method.
     * This is separated for testing purposes to allow mocking.
     *
     * @param userRequest The OAuth2 user request
     * @return The OAuth2 user from the parent class
     */
//    fun callSuperLoadUser(userRequest: OAuth2UserRequest): OAuth2User {
//        return super.loadUser(userRequest)
//    }

    /**
     * Generates a unique correlation ID for request tracking.
     *
     * @return A unique correlation ID
     */
    private fun generateCorrelationId(): String {
        return java.util.UUID.randomUUID().toString().substring(0, 8)
    }
}
