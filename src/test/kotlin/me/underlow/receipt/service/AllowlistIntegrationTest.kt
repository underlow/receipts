package me.underlow.receipt.service

import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestPropertySource
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Integration tests for email allowlist functionality.
 * Tests the complete allowlist validation flow including environment variable parsing,
 * user service validation, and OAuth2 authentication integration.
 */
@SpringBootTest
@ExtendWith(MockitoExtension::class)
@ActiveProfiles("test")
@Testcontainers
class AllowlistIntegrationTest {

    companion object {
        @Container
        @JvmStatic
        val postgres = PostgreSQLContainer<Nothing>("postgres:15-alpine").apply {
            withDatabaseName("testdb")
            withUsername("test")
            withPassword("test")
        }
        
        @JvmStatic
        @org.springframework.test.context.DynamicPropertySource
        fun configureProperties(registry: org.springframework.test.context.DynamicPropertyRegistry) {
            registry.add("spring.datasource.url", postgres::getJdbcUrl)
            registry.add("spring.datasource.username", postgres::getUsername)
            registry.add("spring.datasource.password", postgres::getPassword)
        }
    }

    @Nested
    @TestPropertySource(properties = ["ALLOWED_EMAILS=single@example.com"])
    inner class SingleEmailAllowlistTest {
        
        @Autowired
        private lateinit var userService: UserService

        @Test
        fun `given single email in allowlist when isEmailAllowed then returns true for allowed email`() {
            // given - single email configured in allowlist
            val allowedEmail = "single@example.com"
            val notAllowedEmail = "notallowed@example.com"
            
            // when - checking if allowed email is permitted
            val allowedResult = userService.isEmailAllowed(allowedEmail)
            
            // then - returns true for allowed email
            assertTrue(allowedResult, "Email $allowedEmail should be allowed")
            
            // when - checking if not allowed email is permitted
            val notAllowedResult = userService.isEmailAllowed(notAllowedEmail)
            
            // then - returns false for not allowed email
            assertFalse(notAllowedResult, "Email $notAllowedEmail should not be allowed")
        }
    }

    @Nested
    @TestPropertySource(properties = ["ALLOWED_EMAILS=first@example.com,second@example.com,third@example.com"])
    inner class MultipleEmailsAllowlistTest {
        
        @Autowired
        private lateinit var userService: UserService

        @Test
        fun `given multiple emails in allowlist when isEmailAllowed then returns correct results for all emails`() {
            // given - multiple emails configured in allowlist
            val allowedEmails = listOf("first@example.com", "second@example.com", "third@example.com")
            val notAllowedEmails = listOf("fourth@example.com", "notallowed@example.com")
            
            // when - checking each allowed email
            allowedEmails.forEach { email ->
                val result = userService.isEmailAllowed(email)
                // then - returns true for each allowed email
                assertTrue(result, "Email $email should be allowed")
            }
            
            // when - checking each not allowed email
            notAllowedEmails.forEach { email ->
                val result = userService.isEmailAllowed(email)
                // then - returns false for each not allowed email
                assertFalse(result, "Email $email should not be allowed")
            }
        }
    }

    @Nested
    @TestPropertySource(properties = ["ALLOWED_EMAILS="])
    inner class EmptyAllowlistTest {
        
        @Autowired
        private lateinit var userService: UserService

        @Test
        fun `given empty allowlist when isEmailAllowed then returns false for any email`() {
            // given - empty allowlist configuration
            val testEmails = listOf("test@example.com", "admin@example.com", "user@example.com")
            
            // when - checking any email against empty allowlist
            testEmails.forEach { email ->
                val result = userService.isEmailAllowed(email)
                // then - returns false for any email
                assertFalse(result, "Email $email should not be allowed with empty allowlist")
            }
        }
    }

    @Nested
    @TestPropertySource(properties = ["ALLOWED_EMAILS=test@example.com , admin@example.com ,  user@example.com  "])
    inner class AllowlistWithSpacesTest {
        
        @Autowired
        private lateinit var userService: UserService

        @Test
        fun `given allowlist with spaces and special characters when isEmailAllowed then parses correctly`() {
            // given - allowlist with spaces around commas and trailing spaces
            val allowedEmails = listOf("test@example.com", "admin@example.com", "user@example.com")
            val notAllowedEmail = "notallowed@example.com"
            
            // when - checking each allowed email
            allowedEmails.forEach { email ->
                val result = userService.isEmailAllowed(email)
                // then - returns true for each allowed email (spaces trimmed)
                assertTrue(result, "Email $email should be allowed despite spaces in configuration")
            }
            
            // when - checking not allowed email
            val notAllowedResult = userService.isEmailAllowed(notAllowedEmail)
            
            // then - returns false for not allowed email
            assertFalse(notAllowedResult, "Email $notAllowedEmail should not be allowed")
        }
    }

    @Nested
    @TestPropertySource(properties = ["ALLOWED_EMAILS=Test@Example.Com,ADMIN@EXAMPLE.COM"])
    inner class CaseInsensitiveAllowlistTest {
        
        @Autowired
        private lateinit var userService: UserService

        @Test
        fun `given allowlist with mixed case when isEmailAllowed then handles case insensitively`() {
            // given - allowlist with mixed case emails
            val testCases = mapOf(
                "test@example.com" to true,
                "TEST@EXAMPLE.COM" to true,
                "Test@Example.Com" to true,
                "admin@example.com" to true,
                "ADMIN@EXAMPLE.COM" to true,
                "Admin@Example.Com" to true,
                "notallowed@example.com" to false,
                "NOTALLOWED@EXAMPLE.COM" to false
            )
            
            // when - checking each email with different case variations
            testCases.forEach { (email, expected) ->
                val result = userService.isEmailAllowed(email)
                // then - returns expected result regardless of case
                assertEquals(expected, result, "Email $email should ${if (expected) "be allowed" else "not be allowed"}")
            }
        }
    }

    @Nested
    @TestPropertySource(properties = ["ALLOWED_EMAILS=test@example.com,admin@example.com,"])
    inner class TrailingCommaAllowlistTest {
        
        @Autowired
        private lateinit var userService: UserService

        @Test
        fun `given allowlist with trailing comma when isEmailAllowed then ignores empty entries`() {
            // given - allowlist with trailing comma creating empty entry
            val allowedEmails = listOf("test@example.com", "admin@example.com")
            val notAllowedEmail = "notallowed@example.com"
            
            // when - checking each allowed email
            allowedEmails.forEach { email ->
                val result = userService.isEmailAllowed(email)
                // then - returns true for each allowed email
                assertTrue(result, "Email $email should be allowed")
            }
            
            // when - checking not allowed email
            val notAllowedResult = userService.isEmailAllowed(notAllowedEmail)
            
            // then - returns false for not allowed email
            assertFalse(notAllowedResult, "Email $notAllowedEmail should not be allowed")
        }
    }

    @Nested
    @TestPropertySource(properties = ["ALLOWED_EMAILS=test@example.com,admin@example.com, ,empty@example.com"])
    inner class EmptyEntriesAllowlistTest {
        
        @Autowired
        private lateinit var userService: UserService

        @Test
        fun `given allowlist with empty entries when isEmailAllowed then filters out empty entries correctly`() {
            // given - allowlist with empty entries between valid emails
            val allowedEmails = listOf("test@example.com", "admin@example.com", "empty@example.com")
            val notAllowedEmail = "notallowed@example.com"
            
            // when - checking each allowed email
            allowedEmails.forEach { email ->
                val result = userService.isEmailAllowed(email)
                // then - returns true for each allowed email (empty entries ignored)
                assertTrue(result, "Email $email should be allowed")
            }
            
            // when - checking not allowed email
            val notAllowedResult = userService.isEmailAllowed(notAllowedEmail)
            
            // then - returns false for not allowed email
            assertFalse(notAllowedResult, "Email $notAllowedEmail should not be allowed")
        }
    }

    @Nested
    @TestPropertySource(properties = ["ALLOWED_EMAILS=test@example.com"])
    inner class OAuth2IntegrationTest {
        
        @Autowired
        private lateinit var userService: UserService

        @Test
        fun `given allowlist configuration when OAuth2 authentication then integrates correctly with CustomOAuth2UserService`() {
            // given - allowlist with test email and OAuth2 user with allowed email
            val allowedEmail = "test@example.com"
            val notAllowedEmail = "notallowed@example.com"
            
            // when - checking if allowed email passes validation
            val allowedResult = userService.isEmailAllowed(allowedEmail)
            
            // then - allowed email should pass validation
            assertTrue(allowedResult, "Email $allowedEmail should be allowed for OAuth2 authentication")
            
            // when - checking if not allowed email fails validation
            val notAllowedResult = userService.isEmailAllowed(notAllowedEmail)
            
            // then - not allowed email should fail validation
            assertFalse(notAllowedResult, "Email $notAllowedEmail should not be allowed for OAuth2 authentication")
        }
    }
}