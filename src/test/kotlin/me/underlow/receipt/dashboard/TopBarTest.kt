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
    fun `given authenticated user with avatar when rendering top bar then should display user name and avatar`() {
        // given - authenticated user with complete profile information
        val user = User(
            email = "test@example.com",
            name = "John Doe",
            avatar = "https://example.com/avatar.jpg"
        )
        val topBar = TopBar()
        
        // when - rendering top bar with user information
        val html = topBar.render(user)
        
        // then - should display user name and avatar
        assertNotNull(html)
        assertTrue(html.contains("John Doe"))
        assertTrue(html.contains("https://example.com/avatar.jpg"))
        assertTrue(html.contains("user-avatar"))
    }

    @Test
    fun `given authenticated user without avatar when rendering top bar then should display user name with fallback avatar`() {
        // given - authenticated user without avatar
        val user = User(
            email = "test@example.com",
            name = "Jane Smith",
            avatar = null
        )
        val topBar = TopBar()
        
        // when - rendering top bar with user information
        val html = topBar.render(user)
        
        // then - should display user name with fallback avatar showing first initial
        assertNotNull(html)
        assertTrue(html.contains("Jane Smith"))
        assertTrue(html.contains("J")) // first initial of name
        assertTrue(html.contains("user-avatar-fallback"))
        assertFalse(html.contains("img"))
    }

    @Test
    fun `given authenticated user when rendering top bar then should display user menu with logout option`() {
        // given - authenticated user
        val user = User(
            email = "test@example.com",
            name = "John Doe",
            avatar = "https://example.com/avatar.jpg"
        )
        val topBar = TopBar()
        
        // when - rendering top bar with user information
        val html = topBar.render(user)
        
        // then - should display user menu with logout functionality
        assertNotNull(html)
        assertTrue(html.contains("dropdown"))
        assertTrue(html.contains("logout"))
        assertTrue(html.contains("Profile"))
        assertTrue(html.contains("Settings"))
    }

    @Test
    fun `given authenticated user when rendering top bar then should handle authentication state properly`() {
        // given - authenticated user
        val user = User(
            email = "test@example.com",
            name = "John Doe",
            avatar = "https://example.com/avatar.jpg"
        )
        val topBar = TopBar()
        
        // when - rendering top bar with user information
        val html = topBar.render(user)
        
        // then - should handle authentication state correctly
        assertNotNull(html)
        assertTrue(html.contains("user-dropdown"))
        assertTrue(html.contains("dropdown-toggle"))
        assertTrue(html.contains("test@example.com"))
    }

    @Test
    fun `given user with empty name when rendering top bar then should display fallback text`() {
        // given - user with empty name
        val user = User(
            email = "test@example.com",
            name = "",
            avatar = null
        )
        val topBar = TopBar()
        
        // when - rendering top bar with user information
        val html = topBar.render(user)
        
        // then - should display fallback text for empty name
        assertNotNull(html)
        assertTrue(html.contains("U")) // fallback initial
        assertTrue(html.contains("user-avatar-fallback"))
    }

    @Test
    fun `given user with long name when rendering top bar then should handle name truncation for mobile view`() {
        // given - user with very long name
        val user = User(
            email = "test@example.com",
            name = "John Jacob Jingleheimer Schmidt",
            avatar = "https://example.com/avatar.jpg"
        )
        val topBar = TopBar()
        
        // when - rendering top bar with user information
        val html = topBar.render(user)
        
        // then - should handle long names properly
        assertNotNull(html)
        assertTrue(html.contains("John Jacob Jingleheimer Schmidt"))
        assertTrue(html.contains("d-none d-md-inline")) // responsive display class
    }

    @Test
    fun `given user when rendering top bar then should include proper accessibility attributes`() {
        // given - authenticated user
        val user = User(
            email = "test@example.com",
            name = "John Doe",
            avatar = "https://example.com/avatar.jpg"
        )
        val topBar = TopBar()
        
        // when - rendering top bar with user information
        val html = topBar.render(user)
        
        // then - should include proper accessibility attributes
        assertNotNull(html)
        assertTrue(html.contains("aria-label"))
        assertTrue(html.contains("aria-expanded"))
        assertTrue(html.contains("alt="))
    }

    @Test
    fun `given user when rendering top bar then should include CSRF token in logout form`() {
        // given - authenticated user
        val user = User(
            email = "test@example.com",
            name = "John Doe",
            avatar = "https://example.com/avatar.jpg"
        )
        val topBar = TopBar()
        
        // when - rendering top bar with user information
        val html = topBar.render(user)
        
        // then - should include CSRF token in logout form
        assertNotNull(html)
        assertTrue(html.contains("csrf"))
        assertTrue(html.contains("form"))
        assertTrue(html.contains("method=\"post\""))
    }
}