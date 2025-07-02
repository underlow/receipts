package me.underlow.receipt.controller

import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import java.security.Principal

@Controller
class LoginController {

    @GetMapping("/")
    fun home(): String {
        return "redirect:/dashboard"
    }

    @GetMapping("/login")
    fun login(): String {
        return "login"
    }

    @GetMapping("/dashboard")
    fun dashboard(model: Model, principal: Principal?): String {
        if (principal is OAuth2AuthenticationToken) {
            val attributes = principal.principal.attributes
            model.addAttribute("userEmail", attributes["email"] ?: "")
            model.addAttribute("userName", attributes["name"] ?: "")
        }
        return "dashboard"
    }

    @GetMapping("/upload")
    fun upload(): String {
        return "upload"
    }
}
