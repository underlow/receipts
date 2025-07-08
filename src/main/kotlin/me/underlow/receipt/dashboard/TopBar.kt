package me.underlow.receipt.dashboard

import me.underlow.receipt.model.User
import org.springframework.stereotype.Component

/**
 * TopBar component for providing user profile context data.
 * This component processes user information and provides context data for templates.
 */
@Component
class TopBar {

    /**
     * Prepares user context data for top bar rendering.
     * 
     * @param user the authenticated user information
     * @return map containing user context data for templates
     */
    fun getUserContext(user: User): Map<String, Any> {
        val userName = user.name.ifEmpty { "Unknown User" }
        val userEmail = user.email
        val userAvatar = user.avatar
        val userInitial = if (userName.isNotEmpty()) userName.first().uppercaseChar() else 'U'
        
        return mapOf(
            "userName" to userName,
            "userEmail" to userEmail,
            "userAvatar" to (userAvatar ?: ""),
            "userInitial" to userInitial.toString()
        )
    }
    
    /**
     * Prepares user context data for welcome section rendering.
     * 
     * @param user the authenticated user information
     * @return map containing user context data for welcome section templates
     */
    fun getWelcomeContext(user: User): Map<String, Any> {
        val userName = user.name.ifEmpty { "Unknown User" }
        val userEmail = user.email.ifEmpty { "No email provided" }
        val userAvatar = user.avatar
        val userInitial = if (userName.isNotEmpty()) userName.first().uppercaseChar() else 'U'
        
        return mapOf(
            "userName" to userName,
            "userEmail" to userEmail,
            "userAvatar" to (userAvatar ?: ""),
            "userInitial" to userInitial.toString()
        )
    }
    
    /**
     * Checks if the user has authentication state properly set.
     * This method validates that the user object contains the necessary information
     * for rendering the top bar.
     * 
     * @param user the user object to validate
     * @return true if user has valid authentication state, false otherwise
     */
    fun hasValidAuthenticationState(user: User): Boolean {
        return user.email.isNotEmpty() && user.name.isNotEmpty()
    }
}