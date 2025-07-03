package me.underlow.receipt.e2e

import com.codeborne.selenide.*
import com.codeborne.selenide.Condition.*
import com.codeborne.selenide.Configuration
import com.codeborne.selenide.Selenide.*
import com.github.tomakehurst.wiremock.WireMockServer
import me.underlow.receipt.config.WireMockOAuth2Config
import me.underlow.receipt.model.BillStatus
import me.underlow.receipt.model.IncomingFile
import me.underlow.receipt.model.User
import org.junit.jupiter.api.*
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.context.TestConstructor
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.nio.file.Files
import java.time.LocalDateTime

/**
 * End-to-end tests using WireMock for OAuth2 authentication simulation.
 * Tests complete OAuth2 flow with realistic Google OAuth2 server simulation.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@ActiveProfiles("e2e")
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class WireMockOAuth2E2ETest(
    private val jdbcTemplate: JdbcTemplate,
    private val wireMockOAuth2Config: WireMockOAuth2Config
) {

    @LocalServerPort
    private var port: Int = 0

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
            registry.add("spring.datasource.url", postgres::getJdbcUrl)
            registry.add("spring.datasource.username", postgres::getUsername)
            registry.add("spring.datasource.password", postgres::getPassword)

            // Configure OAuth2 to use WireMock server
            registry.add("spring.security.oauth2.client.registration.google.client-id") { "test-client-id" }
            registry.add("spring.security.oauth2.client.registration.google.client-secret") { "test-client-secret" }
            registry.add("spring.security.oauth2.client.registration.google.scope") { "openid,profile,email" }
            registry.add("spring.security.oauth2.client.registration.google.redirect-uri") { "http://localhost:8080/login/oauth2/code/google" }

            // Point to WireMock OAuth2 endpoints
            registry.add("spring.security.oauth2.client.provider.google.issuer-uri") { "http://localhost:8888" }
            registry.add("spring.security.oauth2.client.provider.google.authorization-uri") { "http://localhost:8888/o/oauth2/auth" }
            registry.add("spring.security.oauth2.client.provider.google.token-uri") { "http://localhost:8888/token" }
            registry.add("spring.security.oauth2.client.provider.google.user-info-uri") { "http://localhost:8888/oauth2/v2/userinfo" }
            registry.add("spring.security.oauth2.client.provider.google.jwk-set-uri") { "http://localhost:8888/oauth2/v3/certs" }
            registry.add("spring.security.oauth2.client.provider.google.user-name-attribute") { "email" }

            registry.add("receipts.inbox-path") { Files.createTempDirectory("inbox").toString() }
            registry.add("receipts.attachment-path") { Files.createTempDirectory("attachments").toString() }
        }
    }

    @BeforeEach
    fun setup() {
        Configuration.browserSize = "1920x1080"
        Configuration.timeout = 10000
        Configuration.headless = true
        Configuration.baseUrl = "http://localhost:$port"

        // Clean database before each test
        cleanDatabase()
    }

    @AfterEach
    fun tearDown() {
        closeWebDriver()
    }

    /**
     * Test complete OAuth2 login flow with WireMock
     * Given: User is not authenticated
     * When: User attempts to access protected resource
     * Then: User is redirected to login, completes OAuth2 flow, and accesses the resource
     */
    @Test
    fun `Given unauthenticated user, when accessing protected resource, then should complete OAuth2 flow and access resource`() {
        // Given: User is not authenticated and tries to access inbox
        open("/inbox")

        // Then: Should be redirected to login page
        `$`("h1").shouldHave(text("Login"))
        `$`("a[href*='/oauth2/authorization/google']").shouldBe(visible)

        // When: User clicks on Google login
        `$`("a[href*='/oauth2/authorization/google']").click()

        // Then: Should complete OAuth2 flow and redirect to dashboard
        // WireMock will simulate the OAuth2 flow and redirect back with auth code
        `$`("h1").shouldHave(text("Dashboard"))

        // And: User should be authenticated and able to access inbox
        open("/inbox")
        `$`("h1").shouldHave(text("Inbox"))

        // And: User should be created in database
        val userCount = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM users WHERE email = ?",
            Int::class.java,
            "test@example.com"
        )
        Assertions.assertEquals(1, userCount)
    }

    /**
     * Test OAuth2 login with existing user
     * Given: User already exists in database
     * When: User completes OAuth2 login
     * Then: User record should be updated, not duplicated
     */
    @Test
    fun `Given existing user, when completing OAuth2 login, then should update existing user record`() {
        // Given: User already exists in database
        val existingUser = createTestUser("test@example.com", "Old Name")

        // When: User completes OAuth2 login flow
        open("/inbox")
        `$`("h1").shouldHave(text("Login"))
        `$`("a[href*='/oauth2/authorization/google']").click()

        // Then: Should successfully authenticate and access inbox
        `$`("h1").shouldHave(text("Inbox"))

        // And: User count should remain 1 (no duplicate)
        val userCount = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM users WHERE email = ?",
            Int::class.java,
            "test@example.com"
        )
        Assertions.assertEquals(1, userCount)

        // And: User name should be updated to OAuth2 provided name
        val updatedName = jdbcTemplate.queryForObject(
            "SELECT name FROM users WHERE email = ?",
            String::class.java,
            "test@example.com"
        )
        Assertions.assertEquals("Test User", updatedName)
    }

    /**
     * Test OAuth2 login creates login event
     * Given: User completes OAuth2 login
     * When: Authentication is successful
     * Then: Login event should be recorded
     */
    @Test
    fun `Given successful OAuth2 login, when user is authenticated, then should record login event`() {
        // Given: User completes OAuth2 login
        open("/inbox")
        `$`("a[href*='/oauth2/authorization/google']").click()

        // Then: Should be authenticated
        `$`("h1").shouldHave(text("Inbox"))

        // And: Login event should be recorded
        val loginEventCount = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM login_events WHERE user_email = ?",
            Int::class.java,
            "test@example.com"
        )
        Assertions.assertEquals(1, loginEventCount)
    }

    /**
     * Test logout functionality after OAuth2 login
     * Given: User is authenticated via OAuth2
     * When: User logs out
     * Then: Session should be invalidated and user redirected to login
     */
    @Test
    fun `Given authenticated user, when logging out, then should invalidate session and redirect to login`() {
        // Given: User is authenticated via OAuth2
        open("/inbox")
        `$`("a[href*='/oauth2/authorization/google']").click()
        `$`("h1").shouldHave(text("Inbox"))

        // When: User logs out
        open("/logout")

        // Then: Should be redirected to login page
        `$`("h1").shouldHave(text("Login"))

        // And: Accessing protected resource should require authentication again
        open("/inbox")
        `$`("h1").shouldHave(text("Login"))
    }

    private fun createTestUser(email: String, name: String): User {
        val insertSql = """
            INSERT INTO users (email, name) 
            VALUES (?, ?) 
            RETURNING id
        """.trimIndent()

        val userId = jdbcTemplate.queryForObject(insertSql, Long::class.java, email, name)
        return User(id = userId, email = email, name = name)
    }

    private fun createTestIncomingFile(userId: Long, filename: String, status: BillStatus): IncomingFile {
        val tempFile = Files.createTempFile("test", ".${filename.substringAfterLast('.')}")
        Files.write(tempFile, "test content".toByteArray())

        val insertSql = """
            INSERT INTO incoming_files (filename, file_path, upload_date, status, checksum, user_id) 
            VALUES (?, ?, ?, ?, ?, ?) 
            RETURNING id
        """.trimIndent()

        val fileId = jdbcTemplate.queryForObject(
            insertSql,
            Long::class.java,
            filename,
            tempFile.toString(),
            java.sql.Timestamp.valueOf(LocalDateTime.now()),
            status.name,
            "checksum-$filename",
            userId
        )

        return IncomingFile(
            id = fileId,
            filename = filename,
            filePath = tempFile.toString(),
            uploadDate = LocalDateTime.now(),
            status = status,
            checksum = "checksum-$filename",
            userId = userId
        )
    }

    private fun cleanDatabase() {
        jdbcTemplate.execute("DELETE FROM login_events")
        jdbcTemplate.execute("DELETE FROM incoming_files")
        jdbcTemplate.execute("DELETE FROM users")

        // Reset sequences
        jdbcTemplate.execute("ALTER SEQUENCE users_id_seq RESTART WITH 1")
        jdbcTemplate.execute("ALTER SEQUENCE incoming_files_id_seq RESTART WITH 1")
        jdbcTemplate.execute("ALTER SEQUENCE login_events_id_seq RESTART WITH 1")
    }
}
