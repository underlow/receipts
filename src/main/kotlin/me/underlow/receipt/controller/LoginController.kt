package me.underlow.receipt.controller

import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam

/**
 * Login controller for handling login page and authentication-based routing.
 * Provides login page display and redirects authenticated users to dashboard.
 */
@Controller
class LoginController {

    /**
     * Displays the login page for unauthenticated users.
     * Redirects authenticated users to the dashboard.
     * Handles error parameters and adds appropriate error messages to the model.
     * 
     * @param model Spring MVC model for template rendering
     * @param error optional error parameter indicating authentication failure
     * @return login template name or redirect to dashboard
     */
    @GetMapping("/login")
    fun login(model: Model, @RequestParam(required = false) error: String?): String {
        val authentication = SecurityContextHolder.getContext().authentication
        
        // If user is already authenticated (not anonymous), redirect to dashboard
        if (authentication != null && 
            authentication.isAuthenticated && 
            authentication.principal != "anonymousUser" &&
            authentication.authorities.none { it.authority == "ROLE_ANONYMOUS" }) {
            return "redirect:/dashboard"
        }
        
        // Add error message to model if error parameter is present
        if (error != null) {
            val errorMessage = when (error) {
                "access_denied" -> "Access denied. Your email is not in the allowlist."
                "invalid_request" -> "Invalid request. Please try again."
                "login_failed" -> "Authentication failed. Please try again."
                else -> "An error occurred during login. Please try again."
            }
            model.addAttribute("errorMessage", errorMessage)
        }
        
        // Show login page for unauthenticated users
        return "login"
    }
}