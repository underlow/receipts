package me.underlow.receipt.controller

import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping

/**
 * Web controller for handling main application pages.
 * Provides endpoints for dashboard, profile, and settings pages.
 */
@Controller
class WebController {

    /**
     * Displays the login page.
     * 
     * @param model Spring MVC model for template rendering
     * @return login template name
     */
    @GetMapping("/login")
    fun login(model: Model): String {
        return "login"
    }

    /**
     * Displays the main dashboard page.
     * Requires authentication.
     * 
     * @param user authenticated OAuth2 user
     * @param model Spring MVC model for template rendering
     * @return dashboard template name
     */
    @GetMapping("/dashboard")
    fun dashboard(@AuthenticationPrincipal user: OAuth2User?, model: Model): String {
        user?.let {
            model.addAttribute("user", it)
            model.addAttribute("email", it.getAttribute<String>("email"))
            model.addAttribute("name", it.getAttribute<String>("name"))
        }
        return "dashboard"
    }

    /**
     * Displays the user profile page.
     * Requires authentication.
     * 
     * @param user authenticated OAuth2 user
     * @param model Spring MVC model for template rendering
     * @return profile template name
     */
    @GetMapping("/profile")
    fun profile(@AuthenticationPrincipal user: OAuth2User?, model: Model): String {
        user?.let {
            model.addAttribute("user", it)
            model.addAttribute("email", it.getAttribute<String>("email"))
            model.addAttribute("name", it.getAttribute<String>("name"))
        }
        return "profile"
    }

    /**
     * Displays the settings page.
     * Requires authentication.
     * 
     * @param user authenticated OAuth2 user
     * @param model Spring MVC model for template rendering
     * @return settings template name
     */
    @GetMapping("/settings")
    fun settings(@AuthenticationPrincipal user: OAuth2User?, model: Model): String {
        user?.let {
            model.addAttribute("user", it)
            model.addAttribute("email", it.getAttribute<String>("email"))
            model.addAttribute("name", it.getAttribute<String>("name"))
        }
        return "settings"
    }
}