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
}