package me.underlow.receipt.controller

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping

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
    fun dashboard(): String {
        return "dashboard"
    }
}
