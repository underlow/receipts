package me.underlow.receipt.service

import me.underlow.receipt.dao.UserDao
import me.underlow.receipt.model.User
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.*
import org.springframework.core.env.Environment
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Unit tests for UserService.
 * Tests email validation and user management functionality.
 */
@ExtendWith(MockitoExtension::class)
class UserServiceTest {
    
    @Mock
    private lateinit var userDao: UserDao
    
    @Mock
    private lateinit var environment: Environment
    
    private lateinit var userService: UserService
    
    @BeforeEach
    fun setUp() {
        userService = UserService(userDao, environment)
    }
    
    @Test
    fun `given email in allowlist when isEmailAllowed then returns true`() {
        // given - email is in the allowlist
        val allowedEmail = "allowed@example.com"
        whenever(environment.getProperty("ALLOWED_EMAILS")).thenReturn("allowed@example.com,another@example.com")
        
        // when - checking if email is allowed
        val result = userService.isEmailAllowed(allowedEmail)
        
        // then - returns true
        assertTrue(result)
    }
    
    @Test
    fun `given email not in allowlist when isEmailAllowed then returns false`() {
        // given - email is not in the allowlist
        val notAllowedEmail = "notallowed@example.com"
        whenever(environment.getProperty("ALLOWED_EMAILS")).thenReturn("allowed@example.com,another@example.com")
        
        // when - checking if email is allowed
        val result = userService.isEmailAllowed(notAllowedEmail)
        
        // then - returns false
        assertFalse(result)
    }
    
    @Test
    fun `given empty allowlist when isEmailAllowed then returns false`() {
        // given - empty allowlist
        val anyEmail = "any@example.com"
        whenever(environment.getProperty("ALLOWED_EMAILS")).thenReturn("")
        
        // when - checking if email is allowed
        val result = userService.isEmailAllowed(anyEmail)
        
        // then - returns false
        assertFalse(result)
    }
    
    @Test
    fun `given null environment variable when isEmailAllowed then returns false`() {
        // given - null environment variable
        val anyEmail = "any@example.com"
        whenever(environment.getProperty("ALLOWED_EMAILS")).thenReturn(null)
        
        // when - checking if email is allowed
        val result = userService.isEmailAllowed(anyEmail)
        
        // then - returns false
        assertFalse(result)
    }
    
    @Test
    fun `given allowlist with spaces when isEmailAllowed then parses correctly`() {
        // given - allowlist with spaces around commas
        val allowedEmail = "test@example.com"
        whenever(environment.getProperty("ALLOWED_EMAILS")).thenReturn("test@example.com , another@example.com ,  third@example.com")
        
        // when - checking if email is allowed
        val result = userService.isEmailAllowed(allowedEmail)
        
        // then - returns true (spaces are trimmed)
        assertTrue(result)
    }
    
    @Test
    fun `given allowlist with single email when isEmailAllowed then works correctly`() {
        // given - allowlist with single email
        val allowedEmail = "single@example.com"
        whenever(environment.getProperty("ALLOWED_EMAILS")).thenReturn("single@example.com")
        
        // when - checking if email is allowed
        val result = userService.isEmailAllowed(allowedEmail)
        
        // then - returns true
        assertTrue(result)
    }
    
    @Test
    fun `given case sensitive email when isEmailAllowed then handles case insensitively`() {
        // given - allowlist with lowercase email
        val uppercaseEmail = "TEST@EXAMPLE.COM"
        whenever(environment.getProperty("ALLOWED_EMAILS")).thenReturn("test@example.com,another@example.com")
        
        // when - checking if uppercase email is allowed
        val result = userService.isEmailAllowed(uppercaseEmail)
        
        // then - returns true (case insensitive comparison)
        assertTrue(result)
    }
    
    @Test
    fun `given allowlist with trailing comma when isEmailAllowed then ignores empty entries`() {
        // given - allowlist with trailing comma
        val allowedEmail = "test@example.com"
        whenever(environment.getProperty("ALLOWED_EMAILS")).thenReturn("test@example.com,another@example.com,")
        
        // when - checking if email is allowed
        val result = userService.isEmailAllowed(allowedEmail)
        
        // then - returns true (empty entries are ignored)
        assertTrue(result)
    }
    
    @Test
    fun `given new user when createOrUpdateUser then creates new user`() {
        // given - user does not exist in database
        val email = "newuser@example.com"
        val name = "New User"
        val avatar = "https://example.com/avatar.jpg"
        val savedUser = User(id = 1L, email = email, name = name, avatar = avatar)
        
        whenever(userDao.upsert(email, name, avatar)).thenReturn(savedUser)
        
        // when - creating or updating user
        val result = userService.createOrUpdateUser(email, name, avatar)
        
        // then - creates new user and returns saved user
        assertEquals(savedUser, result)
        verify(userDao).upsert(email, name, avatar)
    }
    
    @Test
    fun `given existing user when createOrUpdateUser then updates existing user`() {
        // given - user exists in database and will be updated
        val email = "existing@example.com"
        val name = "Updated Name"
        val avatar = "https://example.com/new-avatar.jpg"
        val updatedUser = User(id = 1L, email = email, name = name, avatar = avatar)
        
        whenever(userDao.upsert(email, name, avatar)).thenReturn(updatedUser)
        
        // when - creating or updating user
        val result = userService.createOrUpdateUser(email, name, avatar)
        
        // then - updates existing user and returns updated user
        assertEquals(updatedUser, result)
        verify(userDao).upsert(email, name, avatar)
    }
    
    @Test
    fun `given user with null avatar when createOrUpdateUser then handles null avatar correctly`() {
        // given - user with null avatar
        val email = "user@example.com"
        val name = "User Name"
        val avatar = null
        val savedUser = User(id = 1L, email = email, name = name, avatar = null)
        
        whenever(userDao.upsert(email, name, avatar)).thenReturn(savedUser)
        
        // when - creating or updating user with null avatar
        val result = userService.createOrUpdateUser(email, name, avatar)
        
        // then - creates user with null avatar
        assertEquals(savedUser, result)
        verify(userDao).upsert(email, name, avatar)
    }
    
    @Test
    fun `given existing user with different details when createOrUpdateUser then updates all fields`() {
        // given - existing user with different name and avatar will be updated
        val email = "test@example.com"
        val newName = "New Name"
        val newAvatar = "https://example.com/new-avatar.jpg"
        val updatedUser = User(id = 2L, email = email, name = newName, avatar = newAvatar)
        
        whenever(userDao.upsert(email, newName, newAvatar)).thenReturn(updatedUser)
        
        // when - updating user with new details
        val result = userService.createOrUpdateUser(email, newName, newAvatar)
        
        // then - updates user with new name and avatar
        assertEquals(updatedUser, result)
        verify(userDao).upsert(email, newName, newAvatar)
    }
}