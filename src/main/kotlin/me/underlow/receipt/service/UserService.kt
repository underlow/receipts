package me.underlow.receipt.service

import me.underlow.receipt.model.User
import me.underlow.receipt.repository.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

/**
 * Service for managing user operations.
 * Provides user lookup and management functionality.
 */
@Service
class UserService(private val userRepository: UserRepository) {

    private val logger = LoggerFactory.getLogger(UserService::class.java)
    
    /**
     * Finds a user by email address.
     */
    fun findByEmail(email: String): User? {
        logger.debug("Attempting to find user by email: {}", email)
        val user = userRepository.findByEmail(email)
        if (user == null) {
            logger.info("User with email {} not found.", email)
        } else {
            logger.debug("Found user with email {}: {}", email, user.id)
        }
        return user
    }
    
    /**
     * Saves a user entity.
     */
    fun save(user: User): User {
        logger.debug("Attempting to save user with email: {}", user.email)
        val savedUser = userRepository.save(user)
        logger.info("User with ID {} saved successfully.", savedUser.id)
        return savedUser
    }
}
