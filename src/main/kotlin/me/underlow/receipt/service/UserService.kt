package me.underlow.receipt.service

import me.underlow.receipt.dao.UserDao
import me.underlow.receipt.model.User
import org.springframework.core.env.Environment
import org.springframework.stereotype.Service

/**
 * Service for managing user operations including email validation and user management.
 * Handles OAuth2 user creation and validation against email allowlist.
 */
@Service
class UserService(
    private val userDao: UserDao,
    private val environment: Environment
) {
    
    /**
     * Checks if the given email is allowed based on the ALLOWED_EMAILS environment variable.
     * The allowlist is a comma-separated string of email addresses.
     * 
     * @param email Email address to validate
     * @return true if email is in the allowlist, false otherwise
     */
    fun isEmailAllowed(email: String): Boolean {
        val allowedEmails = environment.getProperty("ALLOWED_EMAILS") ?: return false
        
        if (allowedEmails.isBlank()) {
            return false
        }
        
        val emailList = allowedEmails
            .split(",")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
        
        return emailList.any { allowedEmail ->
            allowedEmail.equals(email, ignoreCase = true)
        }
    }
    
    /**
     * Creates a new user or updates an existing user based on email address.
     * Uses database-level upsert to handle race conditions safely.
     * 
     * @param email User's email address
     * @param name User's display name
     * @param avatar User's avatar URL (optional)
     * @return The created or updated user
     */
    fun createOrUpdateUser(email: String, name: String, avatar: String?): User {
        return userDao.upsert(email, name, avatar)
    }
}