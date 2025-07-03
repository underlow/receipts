package me.underlow.receipt.oauth2

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.*
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import org.junit.jupiter.api.*
import org.springframework.web.client.RestTemplate

/**
 * Complete working demonstration of WireMock OAuth2 functionality.
 * This test successfully mocks Google OAuth2 endpoints for testing.
 */
class WireMockOAuth2FinalTest {

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
     * Test complete OAuth2 flow simulation
     * Given: WireMock server with OAuth2 endpoints configured
     * When: OAuth2 discovery, token exchange, and userinfo requests are made
     * Then: Should return valid OAuth2 responses
     */
    @Test
    fun `Successfully mock complete Google OAuth2 flow with WireMock`() {
        println("=== Testing Complete OAuth2 Flow ===")

        // 1. Test OIDC Discovery
        val discoveryResponse = restTemplate.getForObject(
            "http://localhost:8888/.well-known/openid_configuration",
            String::class.java
        )
        println("✓ OIDC Discovery successful")
        Assertions.assertTrue(discoveryResponse!!.contains("\"issuer\": \"http://localhost:8888\""))
        Assertions.assertTrue(discoveryResponse.contains("\"userinfo_endpoint\": \"http://localhost:8888/oauth2/v2/userinfo\""))

        // 2. Test Token Exchange
        val tokenResponse = restTemplate.postForObject(
            "http://localhost:8888/token",
            mapOf(
                "grant_type" to "authorization_code",
                "code" to "test_authorization_code",
                "client_id" to "test-client-id",
                "client_secret" to "test-client-secret"
            ),
            String::class.java
        )
        println("✓ Token exchange successful")
        Assertions.assertTrue(tokenResponse!!.contains("\"access_token\": \"test_access_token\""))
        Assertions.assertTrue(tokenResponse.contains("\"token_type\": \"Bearer\""))

        // 3. Test UserInfo Request
        val headers = org.springframework.http.HttpHeaders()
        headers.set("Authorization", "Bearer test_access_token")
        val entity = org.springframework.http.HttpEntity<String>(headers)

        val userInfoResponse = restTemplate.exchange(
            "http://localhost:8888/oauth2/v2/userinfo",
            org.springframework.http.HttpMethod.GET,
            entity,
            String::class.java
        )
        println("✓ UserInfo request successful")
        Assertions.assertEquals(200, userInfoResponse.statusCode.value())
        Assertions.assertTrue(userInfoResponse.body!!.contains("\"email\": \"test@example.com\""))
        Assertions.assertTrue(userInfoResponse.body!!.contains("\"name\": \"Test User\""))

        println("=== OAuth2 Flow Test Complete ===")
    }

    /**
     * Test different user scenarios
     * Given: WireMock server with configurable user responses
     * When: Different users are configured
     * Then: Should return appropriate user information
     */
    @Test
    fun `Successfully configure different OAuth2 users with WireMock`() {
        println("=== Testing Different User Scenarios ===")

        // Test User 1: Developer
        configureUser("developer@company.com", "John Developer", "John", "Developer")
        val devUserResponse = getUserInfo()
        Assertions.assertTrue(devUserResponse.contains("\"email\": \"developer@company.com\""))
        Assertions.assertTrue(devUserResponse.contains("\"name\": \"John Developer\""))
        println("✓ Developer user configured successfully")

        // Test User 2: Manager
        configureUser("manager@company.com", "Jane Manager", "Jane", "Manager")
        val managerUserResponse = getUserInfo()
        Assertions.assertTrue(managerUserResponse.contains("\"email\": \"manager@company.com\""))
        Assertions.assertTrue(managerUserResponse.contains("\"name\": \"Jane Manager\""))
        println("✓ Manager user configured successfully")

        println("=== User Scenarios Test Complete ===")
    }

    /**
     * Test OAuth2 error scenarios
     * Given: WireMock server configured with error responses
     * When: Invalid tokens or requests are made
     * Then: Should return appropriate error responses
     */
    @Test
    fun `Successfully simulate OAuth2 error scenarios with WireMock`() {
        println("=== Testing OAuth2 Error Scenarios ===")

        // Configure error response for invalid token
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
            val errorResponse = restTemplate.exchange(
                "http://localhost:8888/oauth2/v2/userinfo",
                org.springframework.http.HttpMethod.GET,
                entity,
                String::class.java
            )
            Assertions.assertEquals(401, errorResponse.statusCode.value())
            Assertions.assertTrue(errorResponse.body!!.contains("\"error\":\"invalid_token\""))
            println("✓ Invalid token error simulation successful")
        } catch (e: org.springframework.web.client.HttpClientErrorException) {
            // RestTemplate throws exception for 4xx status - this is also valid
            Assertions.assertEquals(401, e.statusCode.value())
            Assertions.assertTrue(e.responseBodyAsString.contains("\"error\": \"invalid_token\""))
            println("✓ Invalid token error simulation successful (via exception)")
        }

        println("=== Error Scenarios Test Complete ===")
    }

    private fun setupOAuth2Endpoints() {
        // OIDC Discovery Configuration
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
                                "jwks_uri": "http://localhost:8888/oauth2/v3/certs"
                            }
                        """.trimIndent())
                )
        )

        // Token Exchange Endpoint
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
                                "scope": "openid profile email"
                            }
                        """.trimIndent())
                )
        )

        // Default UserInfo Endpoint
        wireMockServer.stubFor(
            get(urlPathEqualTo("/oauth2/v2/userinfo"))
                .withHeader("Authorization", equalTo("Bearer test_access_token"))
                .willReturn(
                    aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                            {
                                "sub": "test-user-123",
                                "email": "test@example.com",
                                "email_verified": true,
                                "name": "Test User",
                                "given_name": "Test",
                                "family_name": "User"
                            }
                        """.trimIndent())
                )
        )
    }

    private fun configureUser(email: String, name: String, givenName: String, familyName: String) {
        wireMockServer.stubFor(
            get(urlPathEqualTo("/oauth2/v2/userinfo"))
                .withHeader("Authorization", equalTo("Bearer test_access_token"))
                .willReturn(
                    aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                            {
                                "sub": "user-${email.hashCode()}",
                                "email": "$email",
                                "email_verified": true,
                                "name": "$name",
                                "given_name": "$givenName",
                                "family_name": "$familyName"
                            }
                        """.trimIndent())
                )
        )
    }

    private fun getUserInfo(): String {
        val headers = org.springframework.http.HttpHeaders()
        headers.set("Authorization", "Bearer test_access_token")
        val entity = org.springframework.http.HttpEntity<String>(headers)

        val response = restTemplate.exchange(
            "http://localhost:8888/oauth2/v2/userinfo",
            org.springframework.http.HttpMethod.GET,
            entity,
            String::class.java
        )

        return response.body!!
    }
}
