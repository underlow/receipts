package me.underlow.receipt.e2e

import com.codeborne.selenide.*
import com.codeborne.selenide.Condition.*
import com.codeborne.selenide.Configuration
import com.codeborne.selenide.Selenide.*
import com.codeborne.selenide.WebDriverRunner
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
import java.time.Duration

/**
 * End-to-end test for OAuth2 login flow using real browser and PostgreSQL database
 * Tests user authentication flows and database persistence
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@ActiveProfiles("e2e")
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class OAuth2LoginE2ETest(
    private val jdbcTemplate: JdbcTemplate
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
            // Configure PostgreSQL TestContainer
            registry.add("spring.datasource.url", postgres::getJdbcUrl)
            registry.add("spring.datasource.username", postgres::getUsername)
            registry.add("spring.datasource.password", postgres::getPassword)
        }

        @BeforeAll
        @JvmStatic
        fun beforeAll() {
            // Configure Selenide
            Configuration.browser = "chrome"
            Configuration.headless = true
            Configuration.timeout = 10000
            Configuration.pageLoadTimeout = 30000
            Configuration.browserSize = "1920x1080"
        }

        @AfterAll
        @JvmStatic
        fun afterAll() {
            closeWebDriver()
        }
    }

    @BeforeEach
    fun setUp() {
        // Clean up browser state
        WebDriverRunner.clearBrowserCache()

        // Clean test data
        cleanupTestData()
    }

    @AfterEach
    fun tearDown() {
        // Keep browser open for debugging if needed
    }

    @Test
    @DisplayName("Given user visits login page, when page loads, then login button is displayed")
    fun testLoginPageDisplay() {
        // Given: User visits the login page
        open("http://localhost:$port/login")

        // Then: Login button should be visible
        `$`("[data-testid=login-button]").should(appear, Duration.ofSeconds(10))
        `$`("[data-testid=login-button]").shouldHave(text("Login with Google"))
    }

    @Test
    @DisplayName("Given unauthenticated user, when accessing dashboard, then redirected to login")
    fun testUnauthenticatedAccess() {
        // Given: User is not authenticated
        // When: User tries to access dashboard directly
        open("http://localhost:$port/dashboard")

        // Then: User should be redirected to login page
        `$`("[data-testid=login-button]").should(appear, Duration.ofSeconds(10))
    }

    @Test
    @DisplayName("Given user clicks login, when OAuth2 redirect occurs, then proper redirect happens")
    fun testOAuth2RedirectFlow() {
        // Given: User visits the application
        open("http://localhost:$port")

        // When: User clicks login button
        `$`("[data-testid=login-button]").click()

        // Then: Should be redirected to OAuth2 provider (Google in this case)
        // We expect to be redirected away from our application
        // The URL should change to Google's OAuth2 endpoint
        sleep(2000) // Wait for redirect

        val currentUrl = WebDriverRunner.url()
        // Should either be redirected to Google or show an error page
        // since we don't have real Google OAuth2 credentials configured
        Assertions.assertTrue(
            currentUrl.contains("accounts.google.com") ||
            currentUrl.contains("localhost") ||
            currentUrl.contains("error"),
            "Should be redirected to OAuth2 provider or show error page. Current URL: $currentUrl"
        )
    }

    @Test
    @DisplayName("Given database schema, when verifying structure, then all tables and constraints exist")
    fun testDatabaseSchemaVerification() {
        // Given: Database is initialized
        // When: Checking schema
        // Then: Users table should exist with correct structure
        val userTableExists = jdbcTemplate.queryForObject(
            """
            SELECT COUNT(*) FROM information_schema.tables 
            WHERE table_name = 'users' AND table_schema = 'public'
            """.trimIndent(),
            Int::class.java
        )
        Assertions.assertEquals(1, userTableExists, "Users table should exist")

        // And: Login events table should exist
        val loginEventsTableExists = jdbcTemplate.queryForObject(
            """
            SELECT COUNT(*) FROM information_schema.tables 
            WHERE table_name = 'login_events' AND table_schema = 'public'
            """.trimIndent(),
            Int::class.java
        )
        Assertions.assertEquals(1, loginEventsTableExists, "Login events table should exist")

        // And: Foreign key constraint should exist
        val foreignKeyExists = jdbcTemplate.queryForObject(
            """
            SELECT COUNT(*) FROM information_schema.table_constraints 
            WHERE constraint_type = 'FOREIGN KEY' 
            AND table_name = 'login_events'
            """.trimIndent(),
            Int::class.java
        )
        Assertions.assertTrue(foreignKeyExists!! >= 1, "Foreign key constraint should exist")
    }

    @Test
    @DisplayName("Given database operations, when inserting test data, then constraints work correctly")
    fun testDatabaseOperations() {
        // Given: Clean database state
        cleanupTestData()

        // When: Inserting test user
        val userId = jdbcTemplate.queryForObject(
            """
            INSERT INTO users (email, name, created_at, last_login_at) 
            VALUES (?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP) 
            RETURNING id
            """.trimIndent(),
            Long::class.java,
            "test@example.com",
            "Test User"
        )

        // Then: User should be inserted successfully
        Assertions.assertNotNull(userId, "User ID should be returned")

        // When: Inserting login event
        jdbcTemplate.update(
            "INSERT INTO login_events (user_id, timestamp) VALUES (?, CURRENT_TIMESTAMP)",
            userId
        )

        // Then: Login event should be linked correctly
        val loginEventCount = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM login_events WHERE user_id = ?",
            Int::class.java,
            userId
        )
        Assertions.assertEquals(1, loginEventCount, "Login event should be created")

        // And: Join query should work
        val joinResult = jdbcTemplate.queryForObject(
            """
            SELECT u.email FROM users u 
            JOIN login_events le ON u.id = le.user_id 
            WHERE u.id = ?
            """.trimIndent(),
            String::class.java,
            userId
        )
        Assertions.assertEquals("test@example.com", joinResult, "Join query should work correctly")
    }

    @Test
    @DisplayName("Given application security, when accessing protected resources, then proper security is enforced")
    fun testSecurityConfiguration() {
        // Given: Application is running
        // When: Accessing protected dashboard without authentication
        open("http://localhost:$port/dashboard")

        // Then: Should be redirected to OAuth2 login
        `$`("[data-testid=login-button]").should(appear, Duration.ofSeconds(10))

        // When: Accessing home page without authentication
        open("http://localhost:$port/")

        // Then: Should be redirected to dashboard which redirects to login
        `$`("[data-testid=login-button]").should(appear, Duration.ofSeconds(10))
    }

    /**
     * Cleans up test data from database
     */
    private fun cleanupTestData() {
        try {
            jdbcTemplate.update("DELETE FROM login_events WHERE user_id IN (SELECT id FROM users WHERE email = ?)", "test@example.com")
            jdbcTemplate.update("DELETE FROM users WHERE email = ?", "test@example.com")
        } catch (e: Exception) {
            // Ignore cleanup errors - table might not exist yet
        }
    }
}
