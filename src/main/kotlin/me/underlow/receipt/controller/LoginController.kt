package me.underlow.receipt.controller

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping

@Controller
class LoginController {

    @GetMapping("/login")
    fun login(): String {
        return "login"
    }

    @GetMapping("/login/oauth2/code/google")
    fun oauth2Callback(): String {
        println("Manual OAuth2 Callback endpoint reached!")
        return "redirect:/"
    }
}
