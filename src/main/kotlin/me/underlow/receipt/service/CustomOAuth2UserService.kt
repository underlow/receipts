package me.underlow.receipt.service

import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest
import org.springframework.security.oauth2.core.OAuth2AuthenticationException
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
) : DefaultOAuth2UserService() {
    
    /**
     * Loads an OAuth2 user and validates against email allowlist.
     * Creates or updates the user in the database if email is allowed.
     * 
     * @param userRequest The OAuth2 user request containing client registration and access token
     * @return The authenticated OAuth2 user
     * @throws OAuth2AuthenticationException if email is not allowed or required attributes are missing
     */
    override fun loadUser(userRequest: OAuth2UserRequest): OAuth2User {
        // Load the OAuth2 user from the parent class
        val oauth2User = callSuperLoadUser(userRequest)
        
        // Extract user attributes from OAuth2 response
        val attributes = oauth2User.attributes
        
        // Extract email (required)
        val email = attributes["email"] as? String
        if (email.isNullOrBlank()) {
            throw OAuth2AuthenticationException("Email attribute is required but not found or empty")
        }
        
        // Extract name (required)
        val name = attributes["name"] as? String
        if (name.isNullOrBlank()) {
            throw OAuth2AuthenticationException("Name attribute is required but not found or empty")
        }
        
        // Extract avatar (optional)
        val avatar = attributes["picture"] as? String
        
        // Validate email against allowlist
        if (!userService.isEmailAllowed(email)) {
            throw OAuth2AuthenticationException("Email $email is not in the allowlist")
        }
        
        // Create or update user in database
        userService.createOrUpdateUser(email, name, avatar)
        
        return oauth2User
    }
    
    /**
     * Helper method to call the parent class loadUser method.
     * This is separated for testing purposes to allow mocking.
     * 
     * @param userRequest The OAuth2 user request
     * @return The OAuth2 user from the parent class
     */
    fun callSuperLoadUser(userRequest: OAuth2UserRequest): OAuth2User {
        return super.loadUser(userRequest)
    }
}