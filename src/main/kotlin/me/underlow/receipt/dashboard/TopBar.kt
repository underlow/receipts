package me.underlow.receipt.dashboard

import me.underlow.receipt.model.User
import org.springframework.stereotype.Component

/**
 * TopBar component for displaying user profile information and providing logout functionality.
 * This component integrates with the authentication system to display user name, avatar, and provide
 * a dropdown menu with logout functionality.
 */
@Component
class TopBar {

    /**
     * Renders the top bar HTML with user authentication integration.
     * 
     * @param user the authenticated user information
     * @return HTML string containing the top bar with user profile and logout functionality
     */
    fun render(user: User): String {
        val userName = user.name.ifEmpty { "Unknown User" }
        val userEmail = user.email
        val userAvatar = user.avatar
        val userInitial = if (userName.isNotEmpty()) userName.first().uppercaseChar() else 'U'
        
        return buildString {
            append("""
                <div class="dropdown">
                    <button class="btn btn-link dropdown-toggle d-flex align-items-center text-white text-decoration-none" 
                            type="button" 
                            id="user-dropdown" 
                            data-bs-toggle="dropdown" 
                            aria-expanded="false" 
                            aria-label="User menu">
            """.trimIndent())
            
            // Avatar or fallback
            if (!userAvatar.isNullOrEmpty()) {
                append("""
                        <img src="$userAvatar" 
                             alt="$userName avatar" 
                             class="user-avatar">
                """.trimIndent())
            } else {
                append("""
                        <div class="user-avatar-fallback" 
                             title="$userName">
                            $userInitial
                        </div>
                """.trimIndent())
            }
            
            append("""
                        <span class="d-none d-md-inline ms-2">$userName</span>
                    </button>
                    <ul class="dropdown-menu dropdown-menu-end" aria-labelledby="user-dropdown">
                        <li><a class="dropdown-item" href="/profile">
                            <i class="fas fa-user me-2"></i> Profile
                        </a></li>
                        <li><a class="dropdown-item" href="/settings">
                            <i class="fas fa-cog me-2"></i> Settings
                        </a></li>
                        <li><hr class="dropdown-divider"></li>
                        <li>
                            <div class="dropdown-item-text small text-muted">
                                $userEmail
                            </div>
                        </li>
                        <li><hr class="dropdown-divider"></li>
                        <li>
                            <form action="/logout" method="post" class="d-inline">
                                <input type="hidden" name="${'$'}{_csrf.parameterName}" value="${'$'}{_csrf.token}" />
                                <button type="submit" class="dropdown-item text-danger">
                                    <i class="fas fa-sign-out-alt me-2"></i> Logout
                                </button>
                            </form>
                        </li>
                    </ul>
                </div>
            """.trimIndent())
        }
    }
    
    /**
     * Renders the complete top bar with welcome section.
     * This includes both the dropdown menu and the welcome section with user info.
     * 
     * @param user the authenticated user information
     * @return HTML string containing the complete top bar section
     */
    fun renderWelcomeSection(user: User): String {
        val userName = user.name.ifEmpty { "Unknown User" }
        val userEmail = user.email.ifEmpty { "No email provided" }
        val userAvatar = user.avatar
        val userInitial = if (userName.isNotEmpty()) userName.first().uppercaseChar() else 'U'
        
        return buildString {
            append("""
                <div class="top-bar">
                    <div class="welcome-section">
                        <h1 class="welcome-title">Welcome to your Dashboard</h1>
                        <div class="user-info">
                            <div class="d-flex align-items-center">
            """.trimIndent())
            
            // Avatar or fallback for welcome section
            if (!userAvatar.isNullOrEmpty()) {
                append("""
                                <img src="$userAvatar" 
                                     alt="$userName profile avatar" 
                                     class="user-avatar">
                """.trimIndent())
            } else {
                append("""
                                <div class="user-avatar-fallback" 
                                     title="$userName">
                                    $userInitial
                                </div>
                """.trimIndent())
            }
            
            append("""
                            </div>
                            <div class="user-details">
                                <div class="user-name">$userName</div>
                                <div class="user-email">$userEmail</div>
                            </div>
                        </div>
                    </div>
                </div>
            """.trimIndent())
        }
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