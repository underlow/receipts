package me.underlow.receipt.service

import me.underlow.receipt.model.LoginEvent
import me.underlow.receipt.model.User
import me.underlow.receipt.repository.LoginEventRepository
import me.underlow.receipt.repository.UserRepository
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService
import org.springframework.security.oauth2.core.oidc.user.OidcUser
import org.springframework.stereotype.Service
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes
import java.time.LocalDateTime

@Service
class CustomOAuth2UserService(
    private val userRepository: UserRepository,
    private val loginEventRepository: LoginEventRepository
) : OidcUserService() {

    override fun loadUser(userRequest: OidcUserRequest): OidcUser {
        val oauth2User = super.loadUser(userRequest)

        val email = oauth2User.attributes["email"] as String
        val name = oauth2User.attributes["name"] as String

        println("CustomOAuth2UserService: Processing login for user: $email")

        // Find or create user
        var user = userRepository.findByEmail(email)
        user = if (user == null) {
            // Create new user
            println("CustomOAuth2UserService: Creating new user for email: $email")
            val newUser = User(email = email, name = name)
            val savedUser = userRepository.save(newUser)
            println("CustomOAuth2UserService: Created user with ID: ${savedUser.id}")
            savedUser
        } else {
            // Update last login time for existing user
            println("CustomOAuth2UserService: Updating existing user: ${user.id}")
            userRepository.save(user.copy(lastLoginAt = LocalDateTime.now()))
        }

        // Get IP address from request
        val ipAddress = try {
            val request = (RequestContextHolder.getRequestAttributes() as? ServletRequestAttributes)?.request
            request?.getHeader("X-Forwarded-For") ?: request?.remoteAddr
        } catch (e: Exception) {
            null
        }

        // Log login event (user should have ID now)
        user.id?.let { userId ->
            println("CustomOAuth2UserService: Creating login event for user ID: $userId")
            val loginEvent = LoginEvent(userId = userId, ipAddress = ipAddress)
            val savedLoginEvent = loginEventRepository.save(loginEvent)
            println("CustomOAuth2UserService: Created login event with ID: ${savedLoginEvent.id}")
        } ?: println("CustomOAuth2UserService: ERROR - User ID is null, cannot create login event")

        return oauth2User
    }
}

