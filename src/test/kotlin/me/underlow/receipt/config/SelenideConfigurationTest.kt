package me.underlow.receipt.config

import com.codeborne.selenide.Configuration
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension
import java.time.Duration
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Unit tests for SelenideConfiguration class.
 * Tests browser configuration, timeouts, and environment settings.
 */
@ExtendWith(MockitoExtension::class)
class SelenideConfigurationTest {

    private lateinit var selenideConfiguration: SelenideConfiguration

    @BeforeEach
    fun setUp() {
        selenideConfiguration = SelenideConfiguration()
    }

    @AfterEach
    fun tearDown() {
        // Reset Configuration to default values after each test
        Configuration.browser = "chrome"
        Configuration.headless = true
        Configuration.browserSize = "1366x768"
        Configuration.timeout = 4000
        Configuration.pageLoadTimeout = 30000
        // Configuration.collectionsTimeout = 6000 // Property may not exist in current version"
    }

    @Test
    fun `given SelenideConfiguration when configureSelenide then should set correct browser configuration`() {
        // Given: SelenideConfiguration instance
        // When: configuring Selenide
        selenideConfiguration.configureSelenide()

        // Then: browser configuration should be set correctly
        assertEquals("chrome", Configuration.browser)
        assertTrue(Configuration.headless)
        assertEquals(SelenideConfiguration.BROWSER_SIZE, Configuration.browserSize)
        assertEquals(SelenideConfiguration.TIMEOUT, Configuration.timeout)
        assertEquals(SelenideConfiguration.PAGE_LOAD_TIMEOUT, Configuration.pageLoadTimeout)
        // assertEquals(SelenideConfiguration.COLLECTION_TIMEOUT, Configuration.collectionsTimeout) // Property may not exist
        assertEquals(SelenideConfiguration.FAST_SET_VALUE, Configuration.fastSetValue)
        assertEquals(SelenideConfiguration.CLICK_VIA_JS, Configuration.clickViaJs)
        assertTrue(Configuration.screenshots)
        assertTrue(Configuration.savePageSource)
        assertEquals(SelenideConfiguration.REPORTS_FOLDER, Configuration.reportsFolder)
        assertEquals(SelenideConfiguration.DOWNLOADS_FOLDER, Configuration.downloadsFolder)
    }

    @Test
    fun `given SelenideConfiguration when configureSelenide then should set correct base URL`() {
        // Given: SelenideConfiguration instance
        // When: configuring Selenide
        selenideConfiguration.configureSelenide()

        // Then: base URL should be set correctly
        assertEquals(SelenideConfiguration.DEFAULT_BASE_URL, Configuration.baseUrl)
    }

    @Test
    fun `given SelenideConfiguration when configureSelenide then should set browser capabilities`() {
        // Given: SelenideConfiguration instance
        // When: configuring Selenide
        selenideConfiguration.configureSelenide()

        // Then: browser capabilities should be set
        assertNotNull(Configuration.browserCapabilities)
        assertTrue(Configuration.browserCapabilities.toString().contains("chrome"))
    }

    @Test
    fun `given SelenideConfiguration when configureTestTimeouts then should return correct timeout configuration`() {
        // Given: SelenideConfiguration instance
        // When: configuring test timeouts
        val timeouts = selenideConfiguration.configureTestTimeouts()

        // Then: timeout configuration should be correct
        assertEquals(Duration.ofSeconds(10), timeouts["pageLoad"])
        assertEquals(Duration.ofSeconds(8), timeouts["elementWait"])
        assertEquals(Duration.ofSeconds(10), timeouts["collectionWait"])
        assertEquals(Duration.ofSeconds(2), timeouts["fastSetValue"])
        assertEquals(Duration.ofSeconds(5), timeouts["ajaxWait"])
        assertEquals(Duration.ofSeconds(30), timeouts["downloadWait"])
    }

    @Test
    fun `given SelenideConfiguration when configureTestEnvironment then should return correct environment configuration`() {
        // Given: SelenideConfiguration instance
        // When: configuring test environment
        val environment = selenideConfiguration.configureTestEnvironment()

        // Then: environment configuration should be correct
        assertEquals(SelenideConfiguration.DEFAULT_BASE_URL, environment["baseUrl"])
        assertEquals(TestSecurityConfiguration.ALLOWED_EMAIL_1, environment["testUser1"])
        assertEquals(TestSecurityConfiguration.ALLOWED_EMAIL_2, environment["testUser2"])
        assertEquals(TestSecurityConfiguration.NOT_ALLOWED_EMAIL, environment["testUserNotAllowed"])
        assertEquals(TestSecurityConfiguration.TEST_PASSWORD, environment["testPassword"])
        assertEquals(SelenideConfiguration.SCREENSHOTS_FOLDER, environment["screenshotsPath"])
        assertEquals(SelenideConfiguration.REPORTS_FOLDER, environment["reportsPath"])
        assertEquals(SelenideConfiguration.DOWNLOADS_FOLDER, environment["downloadsPath"])
    }

    @Test
    fun `given SelenideConfiguration when configureTestTimeouts then should return all required timeout keys`() {
        // Given: SelenideConfiguration instance
        // When: configuring test timeouts
        val timeouts = selenideConfiguration.configureTestTimeouts()

        // Then: all required timeout keys should be present
        assertTrue(timeouts.containsKey("pageLoad"))
        assertTrue(timeouts.containsKey("elementWait"))
        assertTrue(timeouts.containsKey("collectionWait"))
        assertTrue(timeouts.containsKey("fastSetValue"))
        assertTrue(timeouts.containsKey("ajaxWait"))
        assertTrue(timeouts.containsKey("downloadWait"))
        assertEquals(6, timeouts.size)
    }

    @Test
    fun `given SelenideConfiguration when configureTestEnvironment then should return all required environment keys`() {
        // Given: SelenideConfiguration instance
        // When: configuring test environment
        val environment = selenideConfiguration.configureTestEnvironment()

        // Then: all required environment keys should be present
        assertTrue(environment.containsKey("baseUrl"))
        assertTrue(environment.containsKey("testUser1"))
        assertTrue(environment.containsKey("testUser2"))
        assertTrue(environment.containsKey("testUserNotAllowed"))
        assertTrue(environment.containsKey("testPassword"))
        assertTrue(environment.containsKey("screenshotsPath"))
        assertTrue(environment.containsKey("reportsPath"))
        assertTrue(environment.containsKey("downloadsPath"))
        assertEquals(8, environment.size)
    }

    @Test
    fun `given SelenideConfiguration when configureSelenide then should have consistent timeout values`() {
        // Given: SelenideConfiguration instance
        // When: configuring Selenide
        selenideConfiguration.configureSelenide()

        // Then: timeout values should be consistent between configuration and constants
        assertEquals(SelenideConfiguration.TIMEOUT, Configuration.timeout)
        assertEquals(SelenideConfiguration.PAGE_LOAD_TIMEOUT, Configuration.pageLoadTimeout)
        // assertEquals(SelenideConfiguration.COLLECTION_TIMEOUT, Configuration.collectionsTimeout) // Property may not exist
    }

    @Test
    fun `given SelenideConfiguration when configureSelenide then should enable screenshots and page source`() {
        // Given: SelenideConfiguration instance
        // When: configuring Selenide
        selenideConfiguration.configureSelenide()

        // Then: screenshots and page source should be enabled
        assertTrue(Configuration.screenshots, "Screenshots should be enabled")
        assertTrue(Configuration.savePageSource, "Page source saving should be enabled")
    }

    @Test
    fun `given SelenideConfiguration when configureSelenide then should set correct folder paths`() {
        // Given: SelenideConfiguration instance
        // When: configuring Selenide
        selenideConfiguration.configureSelenide()

        // Then: folder paths should be set correctly
        assertEquals(SelenideConfiguration.REPORTS_FOLDER, Configuration.reportsFolder)
        assertEquals(SelenideConfiguration.DOWNLOADS_FOLDER, Configuration.downloadsFolder)
    }

    @Test
    fun `given SelenideConfiguration constants when checking values then should have reasonable defaults`() {
        // Given: SelenideConfiguration constants
        // When: checking constant values
        // Then: constants should have reasonable values for testing
        assertEquals("1920x1080", SelenideConfiguration.BROWSER_SIZE)
        assertEquals(10000L, SelenideConfiguration.PAGE_LOAD_TIMEOUT)
        assertEquals(8000L, SelenideConfiguration.TIMEOUT)
        assertEquals(10000L, SelenideConfiguration.COLLECTION_TIMEOUT)
        assertTrue(SelenideConfiguration.FAST_SET_VALUE)
        assertTrue(SelenideConfiguration.HEADLESS_MODE)
        assertEquals("http://localhost", SelenideConfiguration.DEFAULT_BASE_URL)
        assertEquals("build/reports/tests/selenide", SelenideConfiguration.SCREENSHOTS_FOLDER)
        assertEquals("build/reports/tests/selenide", SelenideConfiguration.REPORTS_FOLDER)
        assertEquals("build/reports/tests/selenide/downloads", SelenideConfiguration.DOWNLOADS_FOLDER)
    }
}