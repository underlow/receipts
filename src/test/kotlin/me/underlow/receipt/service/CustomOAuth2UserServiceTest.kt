package me.underlow.receipt.service

import me.underlow.receipt.model.User
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.*
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest
import org.springframework.security.oauth2.core.oidc.user.OidcUser
import org.springframework.security.oauth2.core.OAuth2AuthenticationException
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull

/**
 * Unit tests for CustomOAuth2UserService.
 * Tests OAuth2 user loading, email validation, and user management functionality.
 */
@ExtendWith(MockitoExtension::class)
class CustomOAuth2UserServiceTest {
    
    @Mock
    private lateinit var userService: UserService
    
    @Mock
    private lateinit var oidcUserRequest: OidcUserRequest
    
    @Mock
    private lateinit var oidcUser: OidcUser
    
    private lateinit var customOAuth2UserService: CustomOAuth2UserService
    
    @BeforeEach
    fun setUp() {
        customOAuth2UserService = CustomOAuth2UserService(userService)
    }
    
    @Test
    fun `given allowed email when loadUser then returns authenticated user`() {
        // given - OAuth2 user with allowed email
        val allowedEmail = "allowed@example.com"
        val userName = "Test User"
        val userAvatar = "https://example.com/avatar.jpg"
        val savedUser = User(id = 1L, email = allowedEmail, name = userName, avatar = userAvatar)
        
        val userAttributes = mapOf(
            "email" to allowedEmail,
            "name" to userName,
            "picture" to userAvatar
        )
        
        whenever(oidcUser.attributes).thenReturn(userAttributes)
        whenever(userService.isEmailAllowed(allowedEmail)).thenReturn(true)
        whenever(userService.createOrUpdateUser(allowedEmail, userName, userAvatar)).thenReturn(savedUser)
        
        // Mock parent class behavior
        val customService = spy(customOAuth2UserService)
        doReturn(oidcUser).whenever(customService).loadUser(oidcUserRequest)
        
        // when - loading OAuth2 user
        val result = customService.loadUser(oidcUserRequest)
        
        // then - returns authenticated user with proper attributes
        assertNotNull(result)
        assertEquals(allowedEmail, result.attributes["email"])
        assertEquals(userName, result.attributes["name"])
        assertEquals(userAvatar, result.attributes["picture"])
        
        verify(userService).isEmailAllowed(allowedEmail)
        verify(userService).createOrUpdateUser(allowedEmail, userName, userAvatar)
    }
    
    @Test
    fun `given non-allowed email when loadUser then throws OAuth2AuthenticationException`() {
        // given - OAuth2 user with non-allowed email
        val nonAllowedEmail = "notallowed@example.com"
        val userName = "Test User"
        val userAvatar = "https://example.com/avatar.jpg"
        
        val userAttributes = mapOf(
            "email" to nonAllowedEmail,
            "name" to userName,
            "picture" to userAvatar
        )
        
        whenever(oidcUser.attributes).thenReturn(userAttributes)
        whenever(userService.isEmailAllowed(nonAllowedEmail)).thenReturn(false)
        
        // Mock parent class behavior
        val customService = spy(customOAuth2UserService)
        doReturn(oidcUser).whenever(customService).loadUser(oidcUserRequest)
        
        // when - loading OAuth2 user with non-allowed email
        // then - throws OAuth2AuthenticationException
        assertFailsWith<OAuth2AuthenticationException> {
            customService.loadUser(oidcUserRequest)
        }
        
        verify(userService).isEmailAllowed(nonAllowedEmail)
        verify(userService, never()).createOrUpdateUser(any(), any(), any())
    }
    
    @Test
    fun `given new OAuth2 user when loadUser then creates new user`() {
        // given - new OAuth2 user that doesn't exist in database
        val newEmail = "newuser@example.com"
        val newName = "New User"
        val newAvatar = "https://example.com/newavatar.jpg"
        val createdUser = User(id = 1L, email = newEmail, name = newName, avatar = newAvatar)
        
        val userAttributes = mapOf(
            "email" to newEmail,
            "name" to newName,
            "picture" to newAvatar
        )
        
        whenever(oidcUser.attributes).thenReturn(userAttributes)
        whenever(userService.isEmailAllowed(newEmail)).thenReturn(true)
        whenever(userService.createOrUpdateUser(newEmail, newName, newAvatar)).thenReturn(createdUser)
        
        // Mock parent class behavior
        val customService = spy(customOAuth2UserService)
        doReturn(oidcUser).whenever(customService).loadUser(oidcUserRequest)
        
        // when - loading new OAuth2 user
        val result = customService.loadUser(oidcUserRequest)
        
        // then - creates new user in database
        assertNotNull(result)
        verify(userService).createOrUpdateUser(newEmail, newName, newAvatar)
    }
    
    @Test
    fun `given existing OAuth2 user when loadUser then updates existing user`() {
        // given - existing OAuth2 user with updated information
        val existingEmail = "existing@example.com"
        val updatedName = "Updated Name"
        val updatedAvatar = "https://example.com/updated-avatar.jpg"
        val updatedUser = User(id = 2L, email = existingEmail, name = updatedName, avatar = updatedAvatar)
        
        val userAttributes = mapOf(
            "email" to existingEmail,
            "name" to updatedName,
            "picture" to updatedAvatar
        )
        
        whenever(oidcUser.attributes).thenReturn(userAttributes)
        whenever(userService.isEmailAllowed(existingEmail)).thenReturn(true)
        whenever(userService.createOrUpdateUser(existingEmail, updatedName, updatedAvatar)).thenReturn(updatedUser)
        
        // Mock parent class behavior
        val customService = spy(customOAuth2UserService)
        doReturn(oidcUser).whenever(customService).loadUser(oidcUserRequest)
        
        // when - loading existing OAuth2 user
        val result = customService.loadUser(oidcUserRequest)
        
        // then - updates existing user in database
        assertNotNull(result)
        verify(userService).createOrUpdateUser(existingEmail, updatedName, updatedAvatar)
    }
    
    @Test
    fun `given missing email attribute when loadUser then throws OAuth2AuthenticationException`() {
        // given - OAuth2 user without email attribute
        val userAttributes = mapOf(
            "name" to "Test User",
            "picture" to "https://example.com/avatar.jpg"
        )
        
        whenever(oidcUser.attributes).thenReturn(userAttributes)
        
        // Mock parent class behavior
        val customService = spy(customOAuth2UserService)
        doReturn(oidcUser).whenever(customService).loadUser(oidcUserRequest)
        
        // when - loading OAuth2 user without email
        // then - throws OAuth2AuthenticationException
        assertFailsWith<OAuth2AuthenticationException> {
            customService.loadUser(oidcUserRequest)
        }
        
        verify(userService, never()).isEmailAllowed(any())
        verify(userService, never()).createOrUpdateUser(any(), any(), any())
    }
    
    @Test
    fun `given missing name attribute when loadUser then throws OAuth2AuthenticationException`() {
        // given - OAuth2 user without name attribute
        val userAttributes = mapOf(
            "email" to "test@example.com",
            "picture" to "https://example.com/avatar.jpg"
        )
        
        whenever(oidcUser.attributes).thenReturn(userAttributes)
        
        // Mock parent class behavior
        val customService = spy(customOAuth2UserService)
        doReturn(oidcUser).whenever(customService).loadUser(oidcUserRequest)
        
        // when - loading OAuth2 user without name
        // then - throws OAuth2AuthenticationException
        assertFailsWith<OAuth2AuthenticationException> {
            customService.loadUser(oidcUserRequest)
        }
        
        verify(userService, never()).isEmailAllowed(any())
        verify(userService, never()).createOrUpdateUser(any(), any(), any())
    }
    
    @Test
    fun `given missing picture attribute when loadUser then handles null avatar correctly`() {
        // given - OAuth2 user without picture attribute (avatar is optional)
        val allowedEmail = "allowed@example.com"
        val userName = "Test User"
        val savedUser = User(id = 1L, email = allowedEmail, name = userName, avatar = null)
        
        val userAttributes = mapOf(
            "email" to allowedEmail,
            "name" to userName
        )
        
        whenever(oidcUser.attributes).thenReturn(userAttributes)
        whenever(userService.isEmailAllowed(allowedEmail)).thenReturn(true)
        whenever(userService.createOrUpdateUser(allowedEmail, userName, null)).thenReturn(savedUser)
        
        // Mock parent class behavior
        val customService = spy(customOAuth2UserService)
        doReturn(oidcUser).whenever(customService).loadUser(oidcUserRequest)
        
        // when - loading OAuth2 user without picture
        val result = customService.loadUser(oidcUserRequest)
        
        // then - handles null avatar correctly
        assertNotNull(result)
        verify(userService).createOrUpdateUser(allowedEmail, userName, null)
    }
    
    @Test
    fun `given empty email attribute when loadUser then throws OAuth2AuthenticationException`() {
        // given - OAuth2 user with empty email attribute
        val userAttributes = mapOf(
            "email" to "",
            "name" to "Test User",
            "picture" to "https://example.com/avatar.jpg"
        )
        
        whenever(oidcUser.attributes).thenReturn(userAttributes)
        
        // Mock parent class behavior
        val customService = spy(customOAuth2UserService)
        doReturn(oidcUser).whenever(customService).loadUser(oidcUserRequest)
        
        // when - loading OAuth2 user with empty email
        // then - throws OAuth2AuthenticationException
        assertFailsWith<OAuth2AuthenticationException> {
            customService.loadUser(oidcUserRequest)
        }
        
        verify(userService, never()).isEmailAllowed(any())
        verify(userService, never()).createOrUpdateUser(any(), any(), any())
    }
    
    @Test
    fun `given empty name attribute when loadUser then throws OAuth2AuthenticationException`() {
        // given - OAuth2 user with empty name attribute
        val userAttributes = mapOf(
            "email" to "test@example.com",
            "name" to "",
            "picture" to "https://example.com/avatar.jpg"
        )
        
        whenever(oidcUser.attributes).thenReturn(userAttributes)
        
        // Mock parent class behavior
        val customService = spy(customOAuth2UserService)
        doReturn(oidcUser).whenever(customService).loadUser(oidcUserRequest)
        
        // when - loading OAuth2 user with empty name
        // then - throws OAuth2AuthenticationException
        assertFailsWith<OAuth2AuthenticationException> {
            customService.loadUser(oidcUserRequest)
        }
        
        verify(userService, never()).isEmailAllowed(any())
        verify(userService, never()).createOrUpdateUser(any(), any(), any())
    }
}