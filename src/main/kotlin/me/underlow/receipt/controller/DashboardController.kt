package me.underlow.receipt.controller

import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping

/**
 * Dashboard controller for authenticated users.
 * Provides dashboard page access with authentication requirements.
 */
@Controller
class DashboardController {

    /**
     * Displays the dashboard page for authenticated users.
     * Extracts user profile information from OAuth2User principal and adds to model.
     * Requires authentication to access.
     * 
     * @param model the model to add user profile attributes to
     * @param authentication the authentication object containing user principal
     * @return dashboard template name
     */
    @GetMapping("/dashboard")
    @PreAuthorize("isAuthenticated()")
    fun dashboard(model: Model, authentication: Authentication): String {
        extractUserProfileToModel(model, authentication)
        return "dashboard"
    }

    /**
     * Extracts user profile information from OAuth2User principal and adds to model.
     * Handles missing attributes gracefully with fallback values.
     * 
     * @param model the model to add user profile attributes to
     * @param authentication the authentication object containing user principal
     */
    private fun extractUserProfileToModel(model: Model, authentication: Authentication) {
        val userName = when (val principal = authentication.principal) {
            is OAuth2User -> principal.getAttribute<String>("name") ?: "Unknown User"
            else -> "Unknown User"
        }
        
        val userEmail = when (val principal = authentication.principal) {
            is OAuth2User -> principal.getAttribute<String>("email") ?: ""
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