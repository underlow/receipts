package me.underlow.receipt.controller

import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping

/**
 * Dashboard controller for authenticated users.
 * Provides dashboard page access with authentication requirements.
 */
@Controller
class DashboardController {

    /**
     * Displays the dashboard page for authenticated users.
     * Requires authentication to access.
     * 
     * @return dashboard template name
     */
    @GetMapping("/dashboard")
    @PreAuthorize("isAuthenticated()")
    fun dashboard(): String {
        return "dashboard"
    }
}