package me.underlow.receipt.service

import me.underlow.receipt.model.User
import me.underlow.receipt.repository.UserRepository
import org.springframework.stereotype.Service

/**
 * Service for managing user operations.
 * Provides user lookup and management functionality.
 */
@Service
class UserService(private val userRepository: UserRepository) {
    
    /**
     * Finds a user by email address.
     */
    fun findByEmail(email: String): User? {
        return userRepository.findByEmail(email)
    }
    
    /**
     * Saves a user entity.
     */
    fun save(user: User): User {
        return userRepository.save(user)
    }
}