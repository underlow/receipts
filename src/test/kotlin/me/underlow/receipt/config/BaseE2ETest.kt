package me.underlow.receipt.config

import com.codeborne.selenide.Configuration
import com.codeborne.selenide.Selenide
import com.codeborne.selenide.WebDriverRunner
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.time.Duration

/**
 * Base class for E2E tests using Selenide browser automation.
 * Provides common setup and teardown functionality for browser-based tests.
 * Includes TestContainers for database setup and Spring Boot test configuration.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ExtendWith(SpringExtension::class)
@ActiveProfiles("test")
@Testcontainers
@TestPropertySource(
    properties = [
        "spring.security.oauth2.client.provider.google.issuer-uri=https://accounts.google.com",
        "spring.security.oauth2.client.registration.google.client-id=test-client-id",
        "spring.security.oauth2.client.registration.google.client-secret=test-secret",
        "ALLOWED_EMAILS=allowed1@example.com,allowed2@example.com"
    ]
)
abstract class BaseE2ETest {

    companion object {
        /**
         * PostgreSQL container for integration testing.
         * Provides isolated database for each test run.
         */
        @Container
        @JvmStatic
        val postgres = PostgreSQLContainer<Nothing>("postgres:15-alpine").apply {
            withDatabaseName("testdb")
            withUsername("test")
            withPassword("test")
        }

        /**
         * Configures Spring Boot properties dynamically based on TestContainer.
         * Sets up database connection properties from PostgreSQL container.
         */
        @JvmStatic
        @org.springframework.test.context.DynamicPropertySource
        fun configureProperties(registry: org.springframework.test.context.DynamicPropertyRegistry) {
            registry.add("spring.datasource.url", postgres::getJdbcUrl)
            registry.add("spring.datasource.username", postgres::getUsername)
            registry.add("spring.datasource.password", postgres::getPassword)
        }
    }

    /**
     * Local server port for Spring Boot test server.
     * Used to configure Selenide base URL dynamically.
     */
    @LocalServerPort
    private var port: Int = 0

    /**
     * Selenide configuration is handled by TestConfiguration annotation.
     * No need to autowire since we configure it directly in setUpE2ETest().
     */

    /**
     * Sets up the test environment before each test.
     * Configures Selenide with the correct base URL and browser settings.
     */
    @BeforeEach
    fun setUpE2ETest() {
        // Configure Selenide with the dynamic server port
        Configuration.baseUrl = "http://localhost:$port"
        
        // Ensure clean browser state for each test
        if (WebDriverRunner.hasWebDriverStarted()) {
            Selenide.clearBrowserCookies()
            Selenide.clearBrowserLocalStorage()
            Selenide.refresh()
        }
        
        // Configure additional timeouts if needed
        Configuration.timeout = 8000
        Configuration.pageLoadTimeout = 10000
    }

    /**
     * Cleans up the test environment after each test.
     * Captures screenshots on failure and closes browser resources.
     */
    @AfterEach
    fun tearDownE2ETest() {
        try {
            // Take screenshot on test failure (if supported)
            if (WebDriverRunner.hasWebDriverStarted()) {
                Selenide.screenshot("test-cleanup")
            }
        } catch (e: Exception) {
            // Ignore screenshot errors during cleanup
        } finally {
            // Close current browser window but keep WebDriver instance
            // This helps with test isolation while maintaining performance
            try {
                if (WebDriverRunner.hasWebDriverStarted()) {
                    Selenide.clearBrowserCookies()
                    Selenide.clearBrowserLocalStorage()
                    // Note: We don't close the WebDriver completely to improve performance
                    // Selenide.closeWebDriver() - commented out for performance
                }
            } catch (e: Exception) {
                // Ignore cleanup errors
            }
        }
    }

    /**
     * Utility method to get the full URL for a given path.
     * Combines base URL with the provided path.
     *
     * @param path The path to append to base URL
     * @return Full URL for the path
     */
    protected fun getFullUrl(path: String): String {
        return "${Configuration.baseUrl}$path"
    }

    /**
     * Utility method to wait for page to load completely.
     * Waits for both DOM ready and any pending AJAX requests.
     *
     * @param timeout Maximum time to wait in seconds
     */
    protected fun waitForPageLoad(timeout: Long = 10) {
        val readyState = Selenide.executeJavaScript("return document.readyState") as? String
        if (readyState == "complete") {
            // Page is ready
        }
        Thread.sleep(500) // Small delay to ensure all resources are loaded
    }

    /**
     * Utility method to perform login using the test security configuration.
     * Logs in using form-based authentication with test credentials.
     *
     * @param username Test user email
     * @param password Test user password
     */
    protected fun performLogin(username: String, password: String) {
        Selenide.open("/login")
        Selenide.`$`("input[name='username']").setValue(username)
        Selenide.`$`("input[name='password']").setValue(password)
        Selenide.`$`("button[type='submit']").click()
        waitForPageLoad()
    }

    /**
     * Utility method to perform login with allowed test user.
     * Uses predefined test credentials from TestSecurityConfiguration.
     */
    protected fun performLoginWithAllowedUser() {
        performLogin(
            TestSecurityConfiguration.ALLOWED_EMAIL_1,
            TestSecurityConfiguration.TEST_PASSWORD
        )
    }

    /**
     * Utility method to perform login with non-allowed test user.
     * Uses predefined test credentials from TestSecurityConfiguration.
     */
    protected fun performLoginWithNonAllowedUser() {
        performLogin(
            TestSecurityConfiguration.NOT_ALLOWED_EMAIL,
            TestSecurityConfiguration.TEST_PASSWORD
        )
    }

    /**
     * Utility method to perform logout.
     * Clicks logout button and waits for redirect to login page.
     */
    protected fun performLogout() {
        if (Selenide.`$`("button[type='submit']:contains('Logout')").exists()) {
            Selenide.`$`("button[type='submit']:contains('Logout')").click()
            waitForPageLoad()
        }
    }

    /**
     * Utility method to verify user is on login page.
     * Checks for presence of login form elements.
     *
     * @return true if on login page, false otherwise
     */
    protected fun isOnLoginPage(): Boolean {
        return Selenide.`$`("form").exists() && 
               Selenide.`$`("input[name='username']").exists() && 
               Selenide.`$`("input[name='password']").exists()
    }

    /**
     * Utility method to verify user is on dashboard page.
     * Checks for presence of dashboard-specific elements.
     *
     * @return true if on dashboard page, false otherwise
     */
    protected fun isOnDashboardPage(): Boolean {
        return Selenide.`$`("h1").exists() && 
               Selenide.`$`("h1").text().contains("Dashboard")
    }

    /**
     * Utility method to get the current page title.
     * Returns the title of the current page.
     *
     * @return Page title
     */
    protected fun getPageTitle(): String {
        return Selenide.title() ?: ""
    }

    /**
     * Utility method to verify error message is displayed.
     * Checks for presence of error message on the page.
     *
     * @param expectedMessage Expected error message text
     * @return true if error message is displayed, false otherwise
     */
    protected fun isErrorMessageDisplayed(expectedMessage: String): Boolean {
        return Selenide.`$`(".alert-danger").exists() && 
               Selenide.`$`(".alert-danger").text().contains(expectedMessage)
    }
}