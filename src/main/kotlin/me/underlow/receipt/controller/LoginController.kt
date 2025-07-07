package me.underlow.receipt.controller

import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping

/**
 * Login controller for handling login page and authentication-based routing.
 * Provides login page display and redirects authenticated users to dashboard.
 */
@Controller
class LoginController {

    /**
     * Displays the login page for unauthenticated users.
     * Redirects authenticated users to the dashboard.
     * 
     * @param model Spring MVC model for template rendering
     * @return login template name or redirect to dashboard
     */
    @GetMapping("/login")
    fun login(model: Model): String {
        val authentication = SecurityContextHolder.getContext().authentication
        
        // If user is already authenticated (not anonymous), redirect to dashboard
        if (authentication != null && 
            authentication.isAuthenticated && 
            authentication.principal != "anonymousUser" &&
            authentication.authorities.none { it.authority == "ROLE_ANONYMOUS" }) {
            return "redirect:/dashboard"
        }
        
        // Show login page for unauthenticated users
        return "login"
    }
}