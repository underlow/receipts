package me.underlow.receipt.oauth2

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.*
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import org.junit.jupiter.api.*
import org.springframework.web.client.RestTemplate
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Working demonstration of WireMock OAuth2 functionality.
 * This test shows how to successfully mock Google OAuth2 endpoints.
 */
class WireMockOAuth2WorkingTest {

    private lateinit var wireMockServer: WireMockServer
    private val restTemplate = RestTemplate()

    @BeforeEach
    fun setup() {
        wireMockServer = WireMockServer(WireMockConfiguration.wireMockConfig().port(8888))
        wireMockServer.start()
        setupOAuth2Endpoints()
    }

    @AfterEach
    fun tearDown() {
        wireMockServer.stop()
    }

    /**
     * Test that demonstrates successful OAuth2 endpoint mocking
     * Given: WireMock server with OAuth2 endpoints configured
     * When: Requests are made to OAuth2 endpoints
     * Then: Should return valid OAuth2 responses
     */
    @Test
    fun `Given WireMock OAuth2 server, when requesting OAuth2 endpoints, then should return valid responses`() {
        // Test OIDC configuration endpoint
        val configResponse = restTemplate.getForObject(
            "http://localhost:8888/.well-known/openid_configuration",
            String::class.java
        )
        
        assertTrue(configResponse!!.contains("\"issuer\":\"http://localhost:8888\""))
        assertTrue(configResponse.contains("\"token_endpoint\":\"http://localhost:8888/token\""))
        assertTrue(configResponse.contains("\"userinfo_endpoint\":\"http://localhost:8888/oauth2/v2/userinfo\""))

        // Test token endpoint
        val tokenResponse = restTemplate.postForObject(
            "http://localhost:8888/token",
            mapOf(
                "grant_type" to "authorization_code",
                "code" to "test_auth_code"
            ),
            String::class.java
        )
        
        assertTrue(tokenResponse!!.contains("\"access_token\":\"test_access_token\""))
        assertTrue(tokenResponse.contains("\"token_type\":\"Bearer\""))

        // Test userinfo endpoint
        val headers = org.springframework.http.HttpHeaders()
        headers.set("Authorization", "Bearer test_access_token")
        val entity = org.springframework.http.HttpEntity<String>(headers)
        val userinfoResponse = restTemplate.exchange(
            "http://localhost:8888/oauth2/v2/userinfo",
            org.springframework.http.HttpMethod.GET,
            entity,
            String::class.java
        )

        assertEquals(200, userinfoResponse.statusCode.value())
        assertTrue(userinfoResponse.body!!.contains("\"email\":\"test@example.com\""))
        assertTrue(userinfoResponse.body!!.contains("\"name\":\"Test User\""))
    }

    /**
     * Test custom user configuration by directly configuring WireMock
     * Given: WireMock server with custom user configuration
     * When: Userinfo endpoint is called
     * Then: Should return custom user data
     */
    @Test
    fun `Given custom user configuration, when requesting userinfo, then should return custom user data`() {
        // Configure custom user by updating the userinfo stub
        wireMockServer.stubFor(
            get(urlPathEqualTo("/oauth2/v2/userinfo"))
                .withHeader("Authorization", equalTo("Bearer test_access_token"))
                .willReturn(
                    aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                            {
                                "sub": "custom-user-id",
                                "email": "custom@example.com",
                                "email_verified": true,
                                "name": "Custom User",
                                "given_name": "Custom",
                                "family_name": "User"
                            }
                        """.trimIndent())
                )
        )

        // Test custom userinfo
        val headers = org.springframework.http.HttpHeaders()
        headers.set("Authorization", "Bearer test_access_token")
        val entity = org.springframework.http.HttpEntity<String>(headers)
        val response = restTemplate.exchange(
            "http://localhost:8888/oauth2/v2/userinfo",
            org.springframework.http.HttpMethod.GET,
            entity,
            String::class.java
        )

        assertEquals(200, response.statusCode.value())
        assertTrue(response.body!!.contains("\"email\":\"custom@example.com\""))
        assertTrue(response.body!!.contains("\"name\":\"Custom User\""))
    }

    /**
     * Test error scenario configuration
     * Given: WireMock server configured with error response
     * When: Request is made with invalid token
     * Then: Should return error response
     */
    @Test
    fun `Given WireMock with error configuration, when requesting with invalid token, then should return error`() {
        // Configure error response for different authorization header
        wireMockServer.stubFor(
            get(urlPathEqualTo("/oauth2/v2/userinfo"))
                .withHeader("Authorization", equalTo("Bearer invalid_token"))
                .willReturn(
                    aResponse()
                        .withStatus(401)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                            {
                                "error": "invalid_token",
                                "error_description": "The access token provided is invalid"
                            }
                        """.trimIndent())
                )
        )

        // Test error response
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
            
            assertEquals(401, response.statusCode.value())
            assertTrue(response.body!!.contains("\"error\":\"invalid_token\""))
        } catch (e: org.springframework.web.client.HttpClientErrorException) {
            // RestTemplate throws exception for 4xx status
            assertEquals(401, e.statusCode.value())
            assertTrue(e.responseBodyAsString.contains("\"error\":\"invalid_token\""))
        }
    }

    private fun setupOAuth2Endpoints() {
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

        // UserInfo endpoint (default user)
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
                                "email": "test@example.com",
                                "email_verified": true,
                                "name": "Test User",
                                "given_name": "Test",
                                "family_name": "User",
                                "picture": "https://example.com/avatar.png"
                            }
                        """.trimIndent())
                )
        )

        // JWKS endpoint
        wireMockServer.stubFor(
            get(urlPathEqualTo("/oauth2/v3/certs"))
                .willReturn(
                    aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                            {
                                "keys": [
                                    {
                                        "kty": "RSA",
                                        "kid": "test-key-id",
                                        "use": "sig",
                                        "alg": "RS256",
                                        "n": "dummy_n_value",
                                        "e": "AQAB"
                                    }
                                ]
                            }
                        """.trimIndent())
                )
        )
    }
}