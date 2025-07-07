package me.underlow.receipt.controller

import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping

/**
 * Web controller for handling main application pages.
 * Provides endpoints for profile and settings pages with full user profile support.
 */
@Controller
class WebController {
    
    /**
     * Displays the user profile page.
     * Extracts user profile information from OAuth2User principal and adds to model.
     * Requires authentication to access.
     *
     * @param model the model to add user profile attributes to
     * @param authentication the authentication object containing user principal
     * @return profile template name
     */
    @GetMapping("/profile")
    fun profile(model: Model, authentication: Authentication): String {
        extractUserProfileToModel(model, authentication)
        return "profile"
    }

    /**
     * Displays the settings page.
     * Extracts user profile information from OAuth2User principal and adds to model.
     * Requires authentication to access.
     *
     * @param model the model to add user profile attributes to
     * @param authentication the authentication object containing user principal
     * @return settings template name
     */
    @GetMapping("/settings")
    fun settings(model: Model, authentication: Authentication): String {
        extractUserProfileToModel(model, authentication)
        return "settings"
    }

    /**
     * Extracts user profile information from OAuth2User principal and adds to model.
     * Handles missing attributes gracefully with fallback values.
     * Also handles regular User principal for test environments.
     * 
     * @param model the model to add user profile attributes to
     * @param authentication the authentication object containing user principal
     */
    private fun extractUserProfileToModel(model: Model, authentication: Authentication) {
        val userName = when (val principal = authentication.principal) {
            is OAuth2User -> principal.getAttribute<String>("name") ?: "Unknown User"
            is org.springframework.security.core.userdetails.User -> principal.username
            else -> "Unknown User"
        }
        
        val userEmail = when (val principal = authentication.principal) {
            is OAuth2User -> principal.getAttribute<String>("email") ?: ""
            is org.springframework.security.core.userdetails.User -> principal.username
            else -> ""
        }
        
        val userAvatar = when (val principal = authentication.principal) {
            is OAuth2User -> principal.getAttribute<String>("picture") ?: ""
            else -> ""
        }
        
        model.addAttribute("userName", userName)
        model.addAttribute("userEmail", userEmail)
        model.addAttribute("userAvatar", userAvatar)
    }
}
