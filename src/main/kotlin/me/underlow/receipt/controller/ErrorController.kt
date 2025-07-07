package me.underlow.receipt.controller

import jakarta.servlet.RequestDispatcher
import jakarta.servlet.http.HttpServletRequest
import org.springframework.boot.web.servlet.error.ErrorController
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.RequestMapping

/**
 * Error controller for handling application errors and displaying appropriate error pages.
 * Implements Spring Boot's ErrorController interface to provide custom error handling.
 */
@Controller
class CustomErrorController : ErrorController {

    /**
     * Handles all error requests and routes them to appropriate error templates.
     * Determines the error type based on HTTP status code and displays user-friendly error pages.
     * 
     * @param request HTTP servlet request containing error information
     * @param model Spring MVC model for template rendering
     * @return appropriate error template name based on HTTP status code
     */
    @RequestMapping("/error")
    fun handleError(request: HttpServletRequest, model: Model): String {
        val status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE)
        val statusCode = status?.toString()?.toIntOrNull() ?: 500
        
        // Get error details
        val errorMessage = request.getAttribute(RequestDispatcher.ERROR_MESSAGE) as? String
        val requestUri = request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI) as? String
        
        // Add common attributes to model
        model.addAttribute("statusCode", statusCode)
        model.addAttribute("errorMessage", errorMessage)
        model.addAttribute("requestUri", requestUri)
        model.addAttribute("timestamp", System.currentTimeMillis())
        
        // Route to specific error templates based on status code
        return when (statusCode) {
            403 -> {
                model.addAttribute("errorTitle", "Access Denied")
                model.addAttribute("errorDescription", "You do not have permission to access this resource.")
                "error/403"
            }
            404 -> {
                model.addAttribute("errorTitle", "Page Not Found")
                model.addAttribute("errorDescription", "The page you are looking for does not exist.")
                "error/404"
            }
            else -> {
                model.addAttribute("errorTitle", "Internal Server Error")
                model.addAttribute("errorDescription", "An unexpected error occurred. Please try again later.")
                "error/error"
            }
        }
    }
}