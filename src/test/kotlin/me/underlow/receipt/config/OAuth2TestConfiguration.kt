package me.underlow.receipt.config

import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService
import org.springframework.security.oauth2.client.registration.ClientRegistration
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository
import org.springframework.security.oauth2.client.web.AuthenticatedPrincipalOAuth2AuthorizedClientRepository
import org.springframework.security.oauth2.core.AuthorizationGrantType
import org.springframework.security.oauth2.core.ClientAuthenticationMethod
import org.springframework.security.oauth2.core.oidc.IdTokenClaimNames
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.security.oauth2.core.user.DefaultOAuth2User
import org.springframework.test.context.ActiveProfiles

/**
 * Test configuration for OAuth2 integration tests.
 * Provides mock OAuth2 provider setup, test client credentials, and test users.
 */
@TestConfiguration
@ActiveProfiles("test")
class OAuth2TestConfiguration {

    companion object {
        // Test OAuth2 client credentials
        const val TEST_CLIENT_ID = "test-client-id"
        const val TEST_CLIENT_SECRET = "test-client-secret"
        const val TEST_REDIRECT_URI = "http://localhost:8080/login/oauth2/code/google"
        
        // Test users with different email addresses
        const val ALLOWED_EMAIL_1 = "allowed1@example.com"
        const val ALLOWED_EMAIL_2 = "allowed2@example.com"
        const val NOT_ALLOWED_EMAIL = "notallowed@example.com"
        
        // Test allowlist for integration testing
        const val TEST_ALLOWLIST = "$ALLOWED_EMAIL_1,$ALLOWED_EMAIL_2"
        
        // Test user attributes
        const val TEST_USER_NAME_1 = "Allowed User 1"
        const val TEST_USER_NAME_2 = "Allowed User 2"
        const val TEST_USER_NAME_NOT_ALLOWED = "Not Allowed User"
        const val TEST_USER_AVATAR_1 = "https://example.com/avatar1.jpg"
        const val TEST_USER_AVATAR_2 = "https://example.com/avatar2.jpg"
        const val TEST_USER_AVATAR_NOT_ALLOWED = "https://example.com/avatar-not-allowed.jpg"
    }

    /**
     * Creates test OAuth2 client registration for Google provider.
     * Configures mock OAuth2 provider with test credentials.
     */
    @Bean
    @Primary
    fun testClientRegistrationRepository(): ClientRegistrationRepository {
        val registration = ClientRegistration.withRegistrationId("google")
            .clientId(TEST_CLIENT_ID)
            .clientSecret(TEST_CLIENT_SECRET)
            .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
            .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
            .redirectUri(TEST_REDIRECT_URI)
            .scope("openid", "email", "profile")
            .authorizationUri("https://accounts.google.com/o/oauth2/v2/auth")
            .tokenUri("https://oauth2.googleapis.com/token")
            .userInfoUri("https://openidconnect.googleapis.com/v1/userinfo")
            .userNameAttributeName(IdTokenClaimNames.SUB)
            .jwkSetUri("https://www.googleapis.com/oauth2/v3/certs")
            .clientName("Google")
            .build()
        
        return InMemoryClientRegistrationRepository(registration)
    }

    /**
     * Creates test OAuth2 authorized client repository.
     * Uses authenticated principal repository for test scenarios.
     */
    @Bean
    @Primary
    fun testOAuth2AuthorizedClientRepository(
        authorizedClientService: OAuth2AuthorizedClientService
    ): OAuth2AuthorizedClientRepository {
        return AuthenticatedPrincipalOAuth2AuthorizedClientRepository(authorizedClientService)
    }

    /**
     * Creates test OAuth2 user for allowed email scenario.
     * Returns OAuth2User with allowed email and complete user attributes.
     */
    fun createTestOAuth2UserAllowed1(): OAuth2User {
        val attributes = mapOf(
            "sub" to "test-user-id-1",
            "email" to ALLOWED_EMAIL_1,
            "name" to TEST_USER_NAME_1,
            "picture" to TEST_USER_AVATAR_1,
            "email_verified" to true,
            "given_name" to "Allowed",
            "family_name" to "User1"
        )
        
        return DefaultOAuth2User(
            emptyList(),
            attributes,
            "email"
        )
    }

    /**
     * Creates test OAuth2 user for second allowed email scenario.
     * Returns OAuth2User with allowed email and complete user attributes.
     */
    fun createTestOAuth2UserAllowed2(): OAuth2User {
        val attributes = mapOf(
            "sub" to "test-user-id-2",
            "email" to ALLOWED_EMAIL_2,
            "name" to TEST_USER_NAME_2,
            "picture" to TEST_USER_AVATAR_2,
            "email_verified" to true,
            "given_name" to "Allowed",
            "family_name" to "User2"
        )
        
        return DefaultOAuth2User(
            emptyList(),
            attributes,
            "email"
        )
    }

    /**
     * Creates test OAuth2 user for not allowed email scenario.
     * Returns OAuth2User with email that should be rejected by allowlist.
     */
    fun createTestOAuth2UserNotAllowed(): OAuth2User {
        val attributes = mapOf(
            "sub" to "test-user-id-not-allowed",
            "email" to NOT_ALLOWED_EMAIL,
            "name" to TEST_USER_NAME_NOT_ALLOWED,
            "picture" to TEST_USER_AVATAR_NOT_ALLOWED,
            "email_verified" to true,
            "given_name" to "Not Allowed",
            "family_name" to "User"
        )
        
        return DefaultOAuth2User(
            emptyList(),
            attributes,
            "email"
        )
    }

    /**
     * Creates test OAuth2 user with missing email attribute.
     * Returns OAuth2User without email for testing error scenarios.
     */
    fun createTestOAuth2UserMissingEmail(): OAuth2User {
        val attributes = mapOf(
            "sub" to "test-user-id-missing-email",
            "name" to "Missing Email User",
            "picture" to "https://example.com/avatar-missing-email.jpg",
            "email_verified" to true,
            "given_name" to "Missing Email",
            "family_name" to "User"
        )
        
        return DefaultOAuth2User(
            emptyList(),
            attributes,
            "sub"
        )
    }

    /**
     * Creates test OAuth2 user with missing name attribute.
     * Returns OAuth2User without name for testing error scenarios.
     */
    fun createTestOAuth2UserMissingName(): OAuth2User {
        val attributes = mapOf(
            "sub" to "test-user-id-missing-name",
            "email" to "missing-name@example.com",
            "picture" to "https://example.com/avatar-missing-name.jpg",
            "email_verified" to true,
            "given_name" to "Missing",
            "family_name" to "Name"
        )
        
        return DefaultOAuth2User(
            emptyList(),
            attributes,
            "email"
        )
    }

    /**
     * Creates test OAuth2 user with missing picture attribute.
     * Returns OAuth2User without picture for testing optional avatar scenario.
     */
    fun createTestOAuth2UserMissingPicture(): OAuth2User {
        val attributes = mapOf(
            "sub" to "test-user-id-missing-picture",
            "email" to "missing-picture@example.com",
            "name" to "Missing Picture User",
            "email_verified" to true,
            "given_name" to "Missing Picture",
            "family_name" to "User"
        )
        
        return DefaultOAuth2User(
            emptyList(),
            attributes,
            "email"
        )
    }
}