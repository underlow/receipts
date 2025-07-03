package me.underlow.receipt.oauth2

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.*
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import me.underlow.receipt.util.OAuth2TestUtils
import org.junit.jupiter.api.*
import org.springframework.web.client.RestTemplate
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Simple unit test demonstrating WireMock OAuth2 functionality.
 * Shows how to mock Google OAuth2 endpoints for testing.
 */
class WireMockOAuth2SimpleTest {

    private lateinit var wireMockServer: WireMockServer
    private val restTemplate = RestTemplate()

    @BeforeEach
    fun setup() {
        wireMockServer = WireMockServer(WireMockConfiguration.wireMockConfig().port(8888))
        wireMockServer.start()
        setupBasicOAuth2Stubs()
    }

    @AfterEach
    fun tearDown() {
        wireMockServer.stop()
    }

    /**
     * Test basic WireMock OAuth2 server functionality
     * Given: WireMock server with OAuth2 endpoints configured
     * When: Requests are made to OAuth2 endpoints
     * Then: Should return appropriate responses
     */
    @Test
    fun `Given WireMock OAuth2 server, when requesting endpoints, then should return appropriate responses`() {
        // Test if WireMock is actually running
        assertTrue(wireMockServer.isRunning, "WireMock server should be running")

        // When: Request OIDC configuration
        val configResponse = restTemplate.getForObject(
            "http://localhost:8888/.well-known/openid_configuration",
            String::class.java
        )

        // Then: Should return valid OAuth2 configuration
        println("Config response: $configResponse")
        assertTrue(configResponse != null, "Config response should not be null")
        assertTrue(configResponse!!.contains("issuer"), "Config response should contain 'issuer'")
        assertTrue(configResponse.contains("authorization_endpoint"), "Config response should contain 'authorization_endpoint'")
        assertTrue(configResponse.contains("token_endpoint"), "Config response should contain 'token_endpoint'")

        // When: Request token
        val tokenResponse = restTemplate.postForObject(
            "http://localhost:8888/token",
            mapOf(
                "grant_type" to "authorization_code",
                "code" to "test_auth_code",
                "client_id" to "test-client-id",
                "client_secret" to "test-client-secret"
            ),
            String::class.java
        )

        // Then: Should return valid token response
        println("Token response: $tokenResponse")
        assertTrue(tokenResponse != null, "Token response should not be null")
        assertTrue(tokenResponse!!.contains("access_token"), "Token response should contain 'access_token'")
        assertTrue(tokenResponse.contains("Bearer"), "Token response should contain 'Bearer'")
    }

    /**
     * Test OAuth2 userinfo endpoint with custom user configuration
     * Given: WireMock server with configurable userinfo endpoint
     * When: Different user configurations are set
     * Then: Should return appropriate user information
     */
    @Test
    fun `Given WireMock OAuth2 server, when configuring different users, then should return appropriate user info`() {
        // Given: Configure custom user
        OAuth2TestUtils.configureOAuth2User(
            wireMockServer,
            "testuser@example.com",
            "Test User",
            "Test",
            "User",
            "https://example.com/avatar.png"
        )

        // When: Request userinfo with proper authorization header
        val headers = org.springframework.http.HttpHeaders()
        headers.set("Authorization", "Bearer test_access_token")
        val entity = org.springframework.http.HttpEntity<String>(headers)
        val response = restTemplate.exchange(
            "http://localhost:8888/oauth2/v2/userinfo",
            org.springframework.http.HttpMethod.GET,
            entity,
            String::class.java
        )

        // Then: Should return custom user information
        assertEquals(200, response.statusCode.value())
        val responseBody = response.body!!
        assertTrue(responseBody.contains("\"email\": \"testuser@example.com\""))
        assertTrue(responseBody.contains("\"name\": \"Test User\""))
        assertTrue(responseBody.contains("\"given_name\": \"Test\""))
        assertTrue(responseBody.contains("\"family_name\": \"User\""))
    }

    /**
     * Test OAuth2 error scenarios
     * Given: WireMock server configured with error responses
     * When: Request is made to endpoints with errors
     * Then: Should return appropriate error responses
     */
    @Test
    fun `Given WireMock OAuth2 server with errors, when requesting endpoints, then should return error responses`() {
        // Given: Configure error response for invalid token
        OAuth2TestUtils.configureOAuth2Error(wireMockServer, OAuth2TestUtils.OAuth2ErrorType.INVALID_TOKEN)

        // When: Request userinfo with invalid token
        val headers = org.springframework.http.HttpHeaders()
        headers.set("Authorization", "Bearer invalid_token")
        val entity = org.springframework.http.HttpEntity<String>(headers)

        try {
            val response = restTemplate.exchange(
                "http://localhost:8888/oauth2/v2/userinfo",
                org.springframework.http.HttpMethod.GET,
                entity,
                String::class.java
            )

            // Then: Should return error response
            assertEquals(401, response.statusCode.value())
            val responseBody = response.body!!
            assertTrue(responseBody.contains("\"error\": \"invalid_token\""))
        } catch (e: org.springframework.web.client.HttpClientErrorException) {
            // Also acceptable - RestTemplate throws exception for 4xx status
            assertEquals(401, e.statusCode.value())
            assertTrue(e.responseBodyAsString.contains("\"error\": \"invalid_token\""))
        }
    }

    /**
     * Test that WireMock can simulate multiple OAuth2 scenarios
     * Given: WireMock server with multiple user configurations
     * When: Different users are configured sequentially
     * Then: Should return appropriate responses for each user
     */
    @Test
    fun `Given WireMock OAuth2 server, when configuring multiple users, then should handle different scenarios`() {
        // Test user 1
        OAuth2TestUtils.configureOAuth2User(
            wireMockServer,
            "user1@example.com",
            "User One"
        )

        val headers1 = org.springframework.http.HttpHeaders()
        headers1.set("Authorization", "Bearer test_access_token")
        val entity1 = org.springframework.http.HttpEntity<String>(headers1)
        val response1 = restTemplate.exchange(
            "http://localhost:8888/oauth2/v2/userinfo",
            org.springframework.http.HttpMethod.GET,
            entity1,
            String::class.java
        )

        assertEquals(200, response1.statusCode.value())
        assertTrue(response1.body!!.contains("\"email\": \"user1@example.com\""))
        assertTrue(response1.body!!.contains("\"name\": \"User One\""))

        // Test user 2
        OAuth2TestUtils.configureOAuth2User(
            wireMockServer,
            "user2@example.com",
            "User Two"
        )

        val headers2 = org.springframework.http.HttpHeaders()
        headers2.set("Authorization", "Bearer test_access_token")
        val entity2 = org.springframework.http.HttpEntity<String>(headers2)
        val response2 = restTemplate.exchange(
            "http://localhost:8888/oauth2/v2/userinfo",
            org.springframework.http.HttpMethod.GET,
            entity2,
            String::class.java
        )

        assertEquals(200, response2.statusCode.value())
        assertTrue(response2.body!!.contains("\"email\": \"user2@example.com\""))
        assertTrue(response2.body!!.contains("\"name\": \"User Two\""))
    }

    private fun setupBasicOAuth2Stubs() {
        // OIDC Configuration endpoint
        wireMockServer.stubFor(
            get(urlPathEqualTo("/.well-known/openid_configuration"))
                .willReturn(
                    aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                            {
                                "issuer": "http://localhost:8888",
                                "authorization_endpoint": "http://localhost:8888/o/oauth2/auth",
                                "token_endpoint": "http://localhost:8888/token",
                                "userinfo_endpoint": "http://localhost:8888/oauth2/v2/userinfo",
                                "jwks_uri": "http://localhost:8888/oauth2/v3/certs",
                                "response_types_supported": ["code", "token", "id_token"],
                                "subject_types_supported": ["public"],
                                "id_token_signing_alg_values_supported": ["RS256"],
                                "scopes_supported": ["openid", "profile", "email"]
                            }
                        """.trimIndent())
                )
        )

        // Token endpoint
        wireMockServer.stubFor(
            post(urlPathEqualTo("/token"))
                .willReturn(
                    aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                            {
                                "access_token": "test_access_token",
                                "token_type": "Bearer",
                                "expires_in": 3600,
                                "refresh_token": "test_refresh_token",
                                "scope": "openid profile email"
                            }
                        """.trimIndent())
                )
        )

        // Default UserInfo endpoint
        wireMockServer.stubFor(
            get(urlPathEqualTo("/oauth2/v2/userinfo"))
                .withHeader("Authorization", equalTo("Bearer test_access_token"))
                .willReturn(
                    aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                            {
                                "sub": "test-user-id",
                                "email": "default@example.com",
                                "email_verified": true,
                                "name": "Default User",
                                "given_name": "Default",
                                "family_name": "User"
                            }
                        """.trimIndent())
                )
        )
    }
}
