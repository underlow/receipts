package me.underlow.receipt.service

import me.underlow.receipt.model.LoginEvent
import me.underlow.receipt.model.User
import me.underlow.receipt.repository.LoginEventRepository
import me.underlow.receipt.repository.UserRepository
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class CustomOAuth2UserService(
    private val userRepository: UserRepository,
    private val loginEventRepository: LoginEventRepository
) : DefaultOAuth2UserService() {

    override fun loadUser(userRequest: OAuth2UserRequest): OAuth2User {
        val oauth2User = super.loadUser(userRequest)

        val email = oauth2User.attributes["email"] as String
        val name = oauth2User.attributes["name"] as String

        var user = userRepository.findByEmail(email)
        if (user == null) {
            user = User(email = email, name = name)
            userRepository.save(user)
        } else {
            userRepository.save(user.copy(lastLoginAt = LocalDateTime.now()))
        }
        // Log login event
        user.id?.let { userId ->
            val loginEvent = LoginEvent(userId = userId)
            loginEventRepository.save(loginEvent)
        }
        return oauth2User
    }
}

