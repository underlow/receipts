package me.underlow.receipt.dashboard

import me.underlow.receipt.model.User
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.security.test.context.support.WithMockUser
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.test.assertFalse

/**
 * Unit tests for TopBar component.
 * Tests component rendering, user profile display, and authentication integration.
 */
@ExtendWith(MockitoExtension::class)
class TopBarTest {

    @Test
    fun `given authenticated user with avatar when getting user context then should provide user name and avatar`() {
        // given - authenticated user with complete profile information
        val user = User(
            email = "test@example.com",
            name = "John Doe",
            avatar = "https://example.com/avatar.jpg"
        )
        val topBar = TopBar()
        
        // when - getting user context for template
        val context = topBar.getUserContext(user)
        
        // then - should provide user name and avatar context
        assertNotNull(context)
        assertEquals("John Doe", context["userName"])
        assertEquals("https://example.com/avatar.jpg", context["userAvatar"])
        assertEquals("test@example.com", context["userEmail"])
        assertEquals("J", context["userInitial"])
    }

    @Test
    fun `given authenticated user without avatar when getting user context then should provide user name with fallback avatar`() {
        // given - authenticated user without avatar
        val user = User(
            email = "test@example.com",
            name = "Jane Smith",
            avatar = null
        )
        val topBar = TopBar()
        
        // when - getting user context for template
        val context = topBar.getUserContext(user)
        
        // then - should provide user name with fallback avatar showing first initial
        assertNotNull(context)
        assertEquals("Jane Smith", context["userName"])
        assertEquals("J", context["userInitial"])
        assertEquals("", context["userAvatar"])
        assertEquals("test@example.com", context["userEmail"])
    }

    @Test
    fun `given authenticated user when getting user context then should provide complete user data`() {
        // given - authenticated user
        val user = User(
            email = "test@example.com",
            name = "John Doe",
            avatar = "https://example.com/avatar.jpg"
        )
        val topBar = TopBar()
        
        // when - getting user context for template
        val context = topBar.getUserContext(user)
        
        // then - should provide complete user data for template
        assertNotNull(context)
        assertEquals("John Doe", context["userName"])
        assertEquals("test@example.com", context["userEmail"])
        assertEquals("https://example.com/avatar.jpg", context["userAvatar"])
        assertEquals("J", context["userInitial"])
    }

    @Test
    fun `given authenticated user when getting user context then should handle authentication state properly`() {
        // given - authenticated user
        val user = User(
            email = "test@example.com",
            name = "John Doe",
            avatar = "https://example.com/avatar.jpg"
        )
        val topBar = TopBar()
        
        // when - getting user context for template
        val context = topBar.getUserContext(user)
        
        // then - should handle authentication state correctly
        assertNotNull(context)
        assertEquals("John Doe", context["userName"])
        assertEquals("test@example.com", context["userEmail"])
        assertEquals("https://example.com/avatar.jpg", context["userAvatar"])
        assertEquals("J", context["userInitial"])
    }

    @Test
    fun `given user with empty name when getting user context then should provide fallback text`() {
        // given - user with empty name
        val user = User(
            email = "test@example.com",
            name = "",
            avatar = null
        )
        val topBar = TopBar()
        
        // when - getting user context for template
        val context = topBar.getUserContext(user)
        
        // then - should provide fallback text for empty name
        assertNotNull(context)
        assertEquals("Unknown User", context["userName"])
        assertEquals("U", context["userInitial"]) // fallback initial
        assertEquals("", context["userAvatar"])
        assertEquals("test@example.com", context["userEmail"])
    }

    @Test
    fun `given user with long name when getting user context then should handle long names properly`() {
        // given - user with very long name
        val user = User(
            email = "test@example.com",
            name = "John Jacob Jingleheimer Schmidt",
            avatar = "https://example.com/avatar.jpg"
        )
        val topBar = TopBar()
        
        // when - getting user context for template
        val context = topBar.getUserContext(user)
        
        // then - should handle long names properly
        assertNotNull(context)
        assertEquals("John Jacob Jingleheimer Schmidt", context["userName"])
        assertEquals("J", context["userInitial"])
        assertEquals("https://example.com/avatar.jpg", context["userAvatar"])
        assertEquals("test@example.com", context["userEmail"])
    }

    @Test
    fun `given user when getting welcome context then should provide complete user data`() {
        // given - authenticated user
        val user = User(
            email = "test@example.com",
            name = "John Doe",
            avatar = "https://example.com/avatar.jpg"
        )
        val topBar = TopBar()
        
        // when - getting welcome context for template
        val context = topBar.getWelcomeContext(user)
        
        // then - should provide complete user data for welcome section
        assertNotNull(context)
        assertEquals("John Doe", context["userName"])
        assertEquals("test@example.com", context["userEmail"])
        assertEquals("https://example.com/avatar.jpg", context["userAvatar"])
        assertEquals("J", context["userInitial"])
    }

    @Test
    fun `given user with empty email when getting welcome context then should provide fallback email`() {
        // given - authenticated user with empty email
        val user = User(
            email = "",
            name = "John Doe",
            avatar = "https://example.com/avatar.jpg"
        )
        val topBar = TopBar()
        
        // when - getting welcome context for template
        val context = topBar.getWelcomeContext(user)
        
        // then - should provide fallback email text
        assertNotNull(context)
        assertEquals("John Doe", context["userName"])
        assertEquals("No email provided", context["userEmail"])
        assertEquals("https://example.com/avatar.jpg", context["userAvatar"])
        assertEquals("J", context["userInitial"])
    }
}