package me.underlow.receipt.service

import me.underlow.receipt.model.LoginEvent
import me.underlow.receipt.model.User
import me.underlow.receipt.repository.LoginEventRepository
import me.underlow.receipt.repository.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService
import org.springframework.security.oauth2.core.oidc.user.OidcUser
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class CustomOAuth2UserService(
    private val userRepository: UserRepository,
    private val loginEventRepository: LoginEventRepository
) : OidcUserService() {

    private val logger = LoggerFactory.getLogger(CustomOAuth2UserService::class.java)

    override fun loadUser(userRequest: OidcUserRequest): OidcUser {
        val oauth2User = super.loadUser(userRequest)

        val email = oauth2User.attributes["email"] as String
        val name = oauth2User.attributes["name"] as String

        logger.info("Processing login for user: $email")

        // Find or create user
        var user = userRepository.findByEmail(email)
        user = if (user == null) {
            // Create new user
            logger.info("Creating new user for email: $email")
            val newUser = User(email = email, name = name)
            val savedUser = userRepository.save(newUser)
            logger.info("Created user with ID: ${savedUser.id}")
            savedUser
        } else {
            // Update last login time for existing user
            logger.info("Updating existing user: ${user.id}")
            userRepository.save(user.copy(lastLoginAt = LocalDateTime.now()))
        }

        // Log login event (user should have ID now)
        user.id?.let { userId ->
            logger.info("Creating login event for user ID: $userId")
            val loginEvent = LoginEvent(userId = userId)
            val savedLoginEvent = loginEventRepository.save(loginEvent)
            logger.info("Created login event with ID: ${savedLoginEvent.id}")
        } ?: logger.error("User ID is null, cannot create login event")

        return oauth2User
    }
}

