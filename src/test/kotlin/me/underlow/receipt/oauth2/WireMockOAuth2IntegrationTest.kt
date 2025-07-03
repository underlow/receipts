package me.underlow.receipt.oauth2

import com.github.tomakehurst.wiremock.client.WireMock.*
import me.underlow.receipt.util.OAuth2TestUtils
import org.junit.jupiter.api.*
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.HttpStatus
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.context.TestConstructor
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Integration test for WireMock OAuth2 functionality.
 * Tests OAuth2 endpoint mocking without full browser automation.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@ActiveProfiles("e2e")
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
@Disabled("Login is mocked this test should be reworked")
class WireMockOAuth2IntegrationTest {

    @LocalServerPort
    private var port: Int = 0

    private val restTemplate = TestRestTemplate()

    companion object {
        @Container
        @JvmStatic
        val postgres = PostgreSQLContainer("postgres:15-alpine")
            .withDatabaseName("receipt_test")
            .withUsername("test")
            .withPassword("test")

        @DynamicPropertySource
        @JvmStatic
        fun configureProperties(registry: DynamicPropertyRegistry) {
            // Start WireMock server before Spring context initialization

            registry.add("spring.datasource.url", postgres::getJdbcUrl)
            registry.add("spring.datasource.username", postgres::getUsername)
            registry.add("spring.datasource.password", postgres::getPassword)

            // Configure OAuth2 to use WireMock server
            registry.add("spring.security.oauth2.client.registration.google.client-id") { "test-client-id" }
            registry.add("spring.security.oauth2.client.registration.google.client-secret") { "test-client-secret" }
            registry.add("spring.security.oauth2.client.provider.google.issuer-uri") { "http://localhost:8888" }
        }

    }

    @BeforeEach
    fun setup() {
    }

    /**
     * Test that WireMock server is running and responds to OAuth2 configuration requests
     * Given: WireMock server is configured with OAuth2 endpoints
     * When: Request is made to OIDC configuration endpoint
     * Then: Should return proper OAuth2 configuration
     */
    @Test
    fun `Given WireMock OAuth2 server, when requesting OIDC configuration, then should return valid configuration`() {
        // When: Request OIDC configuration
        val response = restTemplate.getForEntity(
            "http://localhost:8888/.well-known/openid_configuration",
            String::class.java
        )

        // Then: Should return valid OAuth2 configuration
        assertEquals(HttpStatus.OK, response.statusCode)
        val responseBody = response.body!!
        assertTrue(responseBody.contains("\"issuer\": \"http://localhost:8888\""))
        assertTrue(responseBody.contains("\"authorization_endpoint\": \"http://localhost:8888/o/oauth2/auth\""))
        assertTrue(responseBody.contains("\"token_endpoint\": \"http://localhost:8888/token\""))
        assertTrue(responseBody.contains("\"userinfo_endpoint\": \"http://localhost:8888/oauth2/v2/userinfo\""))
    }

    /**
     * Test OAuth2 token endpoint mock
     * Given: WireMock server with token endpoint configured
     * When: POST request is made to token endpoint
     * Then: Should return valid token response
     */
    @Test
    fun `Given WireMock OAuth2 server, when requesting token, then should return valid token response`() {
        // When: Request token (simulating OAuth2 flow)
        val response = restTemplate.postForEntity(
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
        assertEquals(HttpStatus.OK, response.statusCode)
        val responseBody = response.body!!
        assertTrue(responseBody.contains("\"access_token\": \"test_access_token\""))
        assertTrue(responseBody.contains("\"token_type\": \"Bearer\""))
        assertTrue(responseBody.contains("\"expires_in\": 3600"))
    }

    /**
     * Test userinfo endpoint with different user configurations
     * Given: WireMock server with configurable userinfo endpoint
     * When: Different user configurations are set
     * Then: Should return appropriate user information
     */
    @Test
    fun `Given WireMock OAuth2 server, when requesting different user configurations, then should return appropriate user info`() {
        // Given: Configure different user

        // When: Request userinfo
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
        assertEquals(HttpStatus.OK, response.statusCode)
        val responseBody = response.body!!
        assertTrue(responseBody.contains("\"email\": \"customuser@example.com\""))
        assertTrue(responseBody.contains("\"name\": \"Custom User\""))
        assertTrue(responseBody.contains("\"given_name\": \"Custom\""))
        assertTrue(responseBody.contains("\"family_name\": \"User\""))
    }

    /**
     * Test OAuth2 error scenarios
     * Given: WireMock server configured with error responses
     * When: Request is made to endpoints with errors
     * Then: Should return appropriate error responses
     */
    @Test
    fun `Given WireMock OAuth2 server with errors, when requesting endpoints, then should return appropriate error responses`() {
        // Given: Configure error response
//        OAuth2TestUtils.configureOAuth2Error(wireMockServer, OAuth2TestUtils.OAuth2ErrorType.INVALID_TOKEN)

        // When: Request userinfo with invalid token
        val headers = org.springframework.http.HttpHeaders()
        headers.set("Authorization", "Bearer invalid_token")
        val entity = org.springframework.http.HttpEntity<String>(headers)
        val response = restTemplate.exchange(
            "http://localhost:8888/oauth2/v2/userinfo",
            org.springframework.http.HttpMethod.GET,
            entity,
            String::class.java
        )

        // Then: Should return error response
        assertEquals(HttpStatus.UNAUTHORIZED, response.statusCode)
        val responseBody = response.body!!
        assertTrue(responseBody.contains("\"error\": \"invalid_token\""))
    }

    /**
     * Test application redirect to login when accessing protected resource
     * Given: Application is running with WireMock OAuth2 configuration
     * When: Unauthenticated request is made to protected resource
     * Then: Should redirect to login page
     */
    @Test
    @Disabled("Login is mocked this test should be reworked")
    fun `Given application with WireMock OAuth2, when accessing protected resource, then should redirect to login`() {
        // When: Request protected resource without authentication
        val response = restTemplate.getForEntity(
            "http://localhost:$port/inbox",
            String::class.java
        )

        // Then: Should redirect to login
        assertEquals(HttpStatus.FOUND, response.statusCode)
        val location = response.headers.getFirst("Location")
        assertTrue(location!!.contains("/login"))
    }

    private fun setupOAuth2Stubs() {
        // Token endpoint

    }
}
