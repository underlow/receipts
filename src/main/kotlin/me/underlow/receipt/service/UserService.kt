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
        val user = userRepository.findByEmail(email)
        if (user == null) {
            logger.info("User authentication failed - user not found for email: {}", email)
        }
        return user
    }
    
    /**
     * Saves a user entity.
     */
    fun save(user: User): User {
        val savedUser = userRepository.save(user)
        logger.info("User registration/update completed for email: {} with ID: {}", user.email, savedUser.id)
        return savedUser
    }
}
