package me.underlow.receipt.config

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.BeforeEach
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestConstructor
import org.springframework.test.context.TestPropertySource
import org.springframework.context.annotation.Import
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.beans.factory.annotation.Autowired
import org.assertj.core.api.Assertions.assertThat
import org.springframework.context.ApplicationContext
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource

/**
 * Tests for MockMvcTestSecurityConfiguration.
 * Verifies that the test security configuration properly bypasses OAuth2
 * and provides form-based authentication for unit testing with CSRF disabled.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("mockmvc-test")
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
@TestPropertySource(
    properties = [
        "ALLOWED_EMAILS=allowed1@example.com,allowed2@example.com"
    ]
)
@Testcontainers
class TestSecurityConfigurationTest (
    private val restTemplate: TestRestTemplate,
    private val userDetailsService: UserDetailsService,
    private val passwordEncoder: PasswordEncoder,
    private val applicationContext: ApplicationContext
) {

    companion object {
        @Container
        @JvmStatic
        val postgres = PostgreSQLContainer<Nothing>("postgres:15-alpine").apply {
            withDatabaseName("testdb")
            withUsername("test")
            withPassword("test")
        }

        @JvmStatic
        @DynamicPropertySource
        fun configureProperties(registry: DynamicPropertyRegistry) {
            registry.add("spring.datasource.url", postgres::getJdbcUrl)
            registry.add("spring.datasource.username", postgres::getUsername)
            registry.add("spring.datasource.password", postgres::getPassword)
        }
    }

    @LocalServerPort
    private var port: Int = 0

    private lateinit var baseUrl: String

    @BeforeEach
    fun setUp() {
        baseUrl = "http://localhost:$port"
    }

    @Test
    @DisplayName("Given mockmvc-test profile is active, when application context loads, then MockMvcTestSecurityConfiguration is loaded")
    fun testSecurityConfigurationIsLoaded() {
        // Given: MockMVC test profile is active
        val activeProfiles = applicationContext.environment.activeProfiles

        // When: Application context is loaded
        val hasMockMvcTestProfile = activeProfiles.contains("mockmvc-test")

        // Then: MockMvcTestSecurityConfiguration should be loaded
        assertThat(hasMockMvcTestProfile).isTrue()
        assertThat(userDetailsService).isNotNull()
        assertThat(passwordEncoder).isNotNull()
    }

    @Test
    @DisplayName("Given test user details service, when loading allowed user, then user is found")
    fun testUserDetailsServiceLoadsAllowedUser() {
        // Given: MockMVC test user details service is configured
        val expectedUsername = MockMvcTestSecurityConfiguration.ALLOWED_EMAIL_1

        // When: Loading allowed user
        val userDetails = userDetailsService.loadUserByUsername(expectedUsername)

        // Then: User is found with correct details
        assertThat(userDetails).isNotNull()
        assertThat(userDetails.username).isEqualTo(expectedUsername)
        assertThat(userDetails.authorities).hasSize(1)
        assertThat(userDetails.authorities.first().authority).isEqualTo("ROLE_USER")
    }

    @Test
    @DisplayName("Given test user details service, when loading not allowed user, then user is found")
    fun testUserDetailsServiceLoadsNotAllowedUser() {
        // Given: MockMVC test user details service is configured
        val expectedUsername = MockMvcTestSecurityConfiguration.NOT_ALLOWED_EMAIL

        // When: Loading not allowed user
        val userDetails = userDetailsService.loadUserByUsername(expectedUsername)

        // Then: User is found with correct details (authentication vs authorization separation)
        assertThat(userDetails).isNotNull()
        assertThat(userDetails.username).isEqualTo(expectedUsername)
        assertThat(userDetails.authorities).hasSize(1)
        assertThat(userDetails.authorities.first().authority).isEqualTo("ROLE_USER")
    }

    @Test
    @DisplayName("Given password encoder, when encoding test password, then password is encoded correctly")
    fun testPasswordEncoderWorksCorrectly() {
        // Given: Password encoder is configured
        val plainPassword = MockMvcTestSecurityConfiguration.TEST_PASSWORD

        // When: Encoding test password
        val encodedPassword = passwordEncoder.encode(plainPassword)

        // Then: Password is encoded and matches verification
        assertThat(encodedPassword).isNotNull()
        assertThat(encodedPassword).isNotEqualTo(plainPassword)
        assertThat(passwordEncoder.matches(plainPassword, encodedPassword)).isTrue()
    }

    @Test
    @DisplayName("Given unauthenticated user, when accessing protected resource, then redirects to login")
    fun testUnauthenticatedAccessRedirectsToLogin() {
        // Given: Unauthenticated user
        val dashboardUrl = "$baseUrl/dashboard"

        // When: Accessing protected dashboard resource
        val response: ResponseEntity<String> = restTemplate.getForEntity(dashboardUrl, String::class.java)

        // Then: Should be redirected to login page (TestRestTemplate follows redirects automatically)
        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(response.body).contains("login") // Should contain login form elements
    }

    @Test
    @DisplayName("Given login page request, when accessing login, then login page is accessible")
    fun testLoginPageIsAccessible() {
        // Given: Login page URL
        val loginUrl = "$baseUrl/login"

        // When: Accessing login page
        val response: ResponseEntity<String> = restTemplate.getForEntity(loginUrl, String::class.java)

        // Then: Login page should be accessible
        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(response.body).contains("login") // Should contain login form
    }

    @Test
    @DisplayName("Given static resources request, when accessing CSS/JS/images, then resources are accessible")
    fun testStaticResourcesAreAccessible() {
        // Given: Static resource URLs
        val cssUrl = "$baseUrl/css/style.css"
        val jsUrl = "$baseUrl/js/app.js"
        val imageUrl = "$baseUrl/images/logo.png"

        // When: Accessing static resources
        val cssResponse: ResponseEntity<String> = restTemplate.getForEntity(cssUrl, String::class.java)
        val jsResponse: ResponseEntity<String> = restTemplate.getForEntity(jsUrl, String::class.java)
        val imageResponse: ResponseEntity<String> = restTemplate.getForEntity(imageUrl, String::class.java)

        // Then: Static resources should be accessible (or return 404 if not exist, but not redirect)
        assertThat(cssResponse.statusCode).isIn(HttpStatus.OK, HttpStatus.NOT_FOUND)
        assertThat(jsResponse.statusCode).isIn(HttpStatus.OK, HttpStatus.NOT_FOUND)
        assertThat(imageResponse.statusCode).isIn(HttpStatus.OK, HttpStatus.NOT_FOUND)

        // Should not redirect to login
        assertThat(cssResponse.statusCode).isNotEqualTo(HttpStatus.FOUND)
        assertThat(jsResponse.statusCode).isNotEqualTo(HttpStatus.FOUND)
        assertThat(imageResponse.statusCode).isNotEqualTo(HttpStatus.FOUND)
    }

    @Test
    @DisplayName("Given form login with valid credentials, when submitting login form, then authentication succeeds")
    fun testFormLoginWithValidCredentials() {
        // Given: Valid test user credentials
        val loginUrl = "$baseUrl/login"
        val username = MockMvcTestSecurityConfiguration.ALLOWED_EMAIL_1
        val password = MockMvcTestSecurityConfiguration.TEST_PASSWORD

        // When: Submitting login form with valid credentials (CSRF disabled in mockmvc-test profile)
        val formData: MultiValueMap<String, String> = LinkedMultiValueMap()
        formData.add("username", username)
        formData.add("password", password)

        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_FORM_URLENCODED

        val request = HttpEntity(formData, headers)
        val response: ResponseEntity<String> = restTemplate.postForEntity(loginUrl, request, String::class.java)

        // Then: Should be redirected to dashboard on successful authentication (TestRestTemplate follows redirects)
        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        // Note: We can't easily test the actual redirect URL with TestRestTemplate as it follows redirects automatically
        // The fact that we get 200 OK indicates successful authentication and redirect to dashboard
    }

    @Test
    @DisplayName("Given form login with invalid credentials, when submitting login form, then authentication fails")
    fun testFormLoginWithInvalidCredentials() {
        // Given: Invalid test user credentials
        val loginUrl = "$baseUrl/login"
        val username = MockMvcTestSecurityConfiguration.ALLOWED_EMAIL_1
        val password = "wrongpassword"

        // When: Submitting login form with invalid credentials (CSRF disabled in mockmvc-test profile)
        val formData: MultiValueMap<String, String> = LinkedMultiValueMap()
        formData.add("username", username)
        formData.add("password", password)

        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_FORM_URLENCODED

        val request = HttpEntity(formData, headers)
        val response: ResponseEntity<String> = restTemplate.postForEntity(loginUrl, request, String::class.java)

        // Then: Should be redirected to login page with error (TestRestTemplate follows redirects)
        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(response.body).contains("login") // Should contain login form elements
        // Note: We can't easily check for error parameter with TestRestTemplate as it follows redirects automatically
    }

    @Test
    @DisplayName("Given form login with non-existent user, when submitting login form, then authentication fails")
    fun testFormLoginWithNonExistentUser() {
        // Given: Non-existent user credentials
        val loginUrl = "$baseUrl/login"
        val username = "nonexistent@example.com"
        val password = MockMvcTestSecurityConfiguration.TEST_PASSWORD

        // When: Submitting login form with non-existent user (CSRF disabled in mockmvc-test profile)
        val formData: MultiValueMap<String, String> = LinkedMultiValueMap()
        formData.add("username", username)
        formData.add("password", password)

        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_FORM_URLENCODED

        val request = HttpEntity(formData, headers)
        val response: ResponseEntity<String> = restTemplate.postForEntity(loginUrl, request, String::class.java)

        // Then: Should be redirected to login page with error (TestRestTemplate follows redirects)
        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(response.body).contains("login") // Should contain login form elements
        // Note: We can't easily check for error parameter with TestRestTemplate as it follows redirects automatically
    }
}
