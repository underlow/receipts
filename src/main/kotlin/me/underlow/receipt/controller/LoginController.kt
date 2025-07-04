package me.underlow.receipt.controller

import org.slf4j.LoggerFactory
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import java.security.Principal

@Controller
class LoginController {

    private val logger = LoggerFactory.getLogger(LoginController::class.java)

    @GetMapping("/")
    fun home(): String {
        logger.info("Redirecting from root to dashboard.")
        return "redirect:/dashboard"
    }

    @GetMapping("/login")
    fun login(): String {
        logger.info("Displaying login page.")
        return "login"
    }

    @GetMapping("/dashboard")
    fun dashboard(model: Model, principal: Principal?): String {
        logger.info("Displaying dashboard page.")
        if (principal is OAuth2AuthenticationToken) {
            val attributes = principal.principal.attributes
            val userEmail = attributes["email"] ?: ""
            val userName = attributes["name"] ?: ""
            model.addAttribute("userEmail", userEmail)
            model.addAttribute("userName", userName)
            logger.info("User {} ({}) accessed dashboard.", userName, userEmail)
        } else {
            logger.info("Unauthenticated user accessed dashboard.")
        }
        return "dashboard"
    }

    @GetMapping("/upload")
    fun upload(): String {
        logger.info("Displaying upload page.")
        return "upload"
    }
}
