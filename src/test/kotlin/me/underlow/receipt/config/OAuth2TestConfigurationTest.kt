package me.underlow.receipt.config

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

/**
 * Unit tests for OAuth2TestConfiguration.
 * Tests OAuth2 test configuration setup and test user creation.
 */
class OAuth2TestConfigurationTest {

    @Test
    fun `given test configuration when getting client registration then returns test client`() {
        // given - OAuth2 test configuration
        val config = OAuth2TestConfiguration()
        
        // when - getting client registration repository
        val repository = config.testClientRegistrationRepository()
        val registration = repository.findByRegistrationId("google")
        
        // then - returns test client registration with correct configuration
        assertNotNull(registration)
        assertEquals(OAuth2TestConfiguration.TEST_CLIENT_ID, registration.clientId)
        assertEquals(OAuth2TestConfiguration.TEST_CLIENT_SECRET, registration.clientSecret)
        assertEquals(OAuth2TestConfiguration.TEST_REDIRECT_URI, registration.redirectUri)
        assertEquals("Google", registration.clientName)
        assertEquals(setOf("openid", "email", "profile"), registration.scopes)
    }

    @Test
    fun `given test configuration when creating allowed user 1 then returns correct OAuth2User`() {
        // given - OAuth2 test configuration
        val config = OAuth2TestConfiguration()
        
        // when - creating test OAuth2 user for allowed email 1
        val user = config.createTestOAuth2UserAllowed1()
        
        // then - returns OAuth2User with correct attributes
        assertNotNull(user)
        assertEquals(OAuth2TestConfiguration.ALLOWED_EMAIL_1, user.attributes["email"])
        assertEquals(OAuth2TestConfiguration.TEST_USER_NAME_1, user.attributes["name"])
        assertEquals(OAuth2TestConfiguration.TEST_USER_AVATAR_1, user.attributes["picture"])
        assertEquals(true, user.attributes["email_verified"])
        assertEquals("test-user-id-1", user.attributes["sub"])
    }

    @Test
    fun `given test configuration when creating allowed user 2 then returns correct OAuth2User`() {
        // given - OAuth2 test configuration
        val config = OAuth2TestConfiguration()
        
        // when - creating test OAuth2 user for allowed email 2
        val user = config.createTestOAuth2UserAllowed2()
        
        // then - returns OAuth2User with correct attributes
        assertNotNull(user)
        assertEquals(OAuth2TestConfiguration.ALLOWED_EMAIL_2, user.attributes["email"])
        assertEquals(OAuth2TestConfiguration.TEST_USER_NAME_2, user.attributes["name"])
        assertEquals(OAuth2TestConfiguration.TEST_USER_AVATAR_2, user.attributes["picture"])
        assertEquals(true, user.attributes["email_verified"])
        assertEquals("test-user-id-2", user.attributes["sub"])
    }

    @Test
    fun `given test configuration when creating not allowed user then returns correct OAuth2User`() {
        // given - OAuth2 test configuration
        val config = OAuth2TestConfiguration()
        
        // when - creating test OAuth2 user for not allowed email
        val user = config.createTestOAuth2UserNotAllowed()
        
        // then - returns OAuth2User with correct attributes
        assertNotNull(user)
        assertEquals(OAuth2TestConfiguration.NOT_ALLOWED_EMAIL, user.attributes["email"])
        assertEquals(OAuth2TestConfiguration.TEST_USER_NAME_NOT_ALLOWED, user.attributes["name"])
        assertEquals(OAuth2TestConfiguration.TEST_USER_AVATAR_NOT_ALLOWED, user.attributes["picture"])
        assertEquals(true, user.attributes["email_verified"])
        assertEquals("test-user-id-not-allowed", user.attributes["sub"])
    }

    @Test
    fun `given test configuration when creating user with missing email then returns OAuth2User without email`() {
        // given - OAuth2 test configuration
        val config = OAuth2TestConfiguration()
        
        // when - creating test OAuth2 user with missing email
        val user = config.createTestOAuth2UserMissingEmail()
        
        // then - returns OAuth2User without email attribute
        assertNotNull(user)
        assertEquals(null, user.attributes["email"])
        assertEquals("Missing Email User", user.attributes["name"])
        assertEquals("https://example.com/avatar-missing-email.jpg", user.attributes["picture"])
        assertEquals("test-user-id-missing-email", user.attributes["sub"])
    }

    @Test
    fun `given test configuration when creating user with missing name then returns OAuth2User without name`() {
        // given - OAuth2 test configuration
        val config = OAuth2TestConfiguration()
        
        // when - creating test OAuth2 user with missing name
        val user = config.createTestOAuth2UserMissingName()
        
        // then - returns OAuth2User without name attribute
        assertNotNull(user)
        assertEquals("missing-name@example.com", user.attributes["email"])
        assertEquals(null, user.attributes["name"])
        assertEquals("https://example.com/avatar-missing-name.jpg", user.attributes["picture"])
        assertEquals("test-user-id-missing-name", user.attributes["sub"])
    }

    @Test
    fun `given test configuration when creating user with missing picture then returns OAuth2User without picture`() {
        // given - OAuth2 test configuration
        val config = OAuth2TestConfiguration()
        
        // when - creating test OAuth2 user with missing picture
        val user = config.createTestOAuth2UserMissingPicture()
        
        // then - returns OAuth2User without picture attribute
        assertNotNull(user)
        assertEquals("missing-picture@example.com", user.attributes["email"])
        assertEquals("Missing Picture User", user.attributes["name"])
        assertEquals(null, user.attributes["picture"])
        assertEquals("test-user-id-missing-picture", user.attributes["sub"])
    }
}