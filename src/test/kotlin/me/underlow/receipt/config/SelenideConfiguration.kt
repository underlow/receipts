package me.underlow.receipt.config

import com.codeborne.selenide.Configuration
import com.codeborne.selenide.logevents.SelenideLogger
import org.openqa.selenium.chrome.ChromeOptions
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Profile
import org.springframework.test.context.TestPropertySource
import java.time.Duration

/**
 * Configuration class for Selenide browser testing.
 * Sets up Chrome WebDriver with headless mode for CI/CD environments.
 * Configures timeouts, base URL, and browser options for E2E tests.
 */
@TestConfiguration
@Profile("test")
class SelenideConfiguration {

    companion object {
        // Browser configuration constants
        const val BROWSER_SIZE = "1920x1080"
        const val PAGE_LOAD_TIMEOUT = 10000L
        const val TIMEOUT = 8000L
        const val COLLECTION_TIMEOUT = 10000L
        const val FAST_SET_VALUE = true
        const val CLICK_VIA_JS = false
        const val SCREENSHOTS_FOLDER = "build/reports/tests/selenide"
        const val REPORTS_FOLDER = "build/reports/tests/selenide"
        const val DOWNLOADS_FOLDER = "build/reports/tests/selenide/downloads"

        // Test environment configuration
        const val DEFAULT_BASE_URL = "http://localhost"
        const val HEADLESS_MODE = true

        // Chrome options for CI/CD
        private val CI_CD_CHROME_OPTIONS = listOf(
            "--no-sandbox",
            "--disable-dev-shm-usage",
            "--disable-gpu",
            "--disable-extensions",
            "--disable-infobars",
            "--disable-notifications",
            "--disable-popup-blocking",
            "--disable-default-apps",
            "--disable-background-networking",
            "--disable-sync",
            "--disable-translate",
            "--disable-web-security",
            "--disable-features=TranslateUI",
            "--disable-ipc-flooding-protection",
            "--window-size=1920,1080"
        )
    }

    /**
     * Configures Selenide for headless browser testing.
     * Sets up Chrome WebDriver with appropriate options for CI/CD environments.
     * Configures timeouts, base URL, and browser settings.
     */
    @Bean
    fun configureSelenide(): SelenideConfiguration {
        // WebDriverManager setup handled by Selenide automatically

        // Configure Selenide settings
        Configuration.browser = "chrome"
        Configuration.headless = HEADLESS_MODE
        Configuration.browserSize = BROWSER_SIZE
        Configuration.timeout = TIMEOUT
        Configuration.pageLoadTimeout = PAGE_LOAD_TIMEOUT
        // Configuration.collectionsTimeout = COLLECTION_TIMEOUT // Property may not exist in current version"
        Configuration.fastSetValue = FAST_SET_VALUE
        Configuration.clickViaJs = CLICK_VIA_JS
        Configuration.screenshots = true
        Configuration.savePageSource = true
        Configuration.reportsFolder = REPORTS_FOLDER
        Configuration.downloadsFolder = DOWNLOADS_FOLDER
        Configuration.holdBrowserOpen = false;

        // Configure Chrome options for CI/CD
        Configuration.browserCapabilities = createChromeOptions()

        // Configure base URL (will be overridden in tests with random port)
        Configuration.baseUrl = DEFAULT_BASE_URL

        // Enable detailed logging for better test reporting
        // SelenideLogger.addListener("selenide",
        //     com.codeborne.selenide.logevents.PrettyReportCreator()
        // )

        return this
    }

    /**
     * Creates Chrome options optimized for CI/CD environments.
     * Includes options for headless mode, performance, and stability.
     *
     * @return ChromeOptions configured for testing
     */
    private fun createChromeOptions(): ChromeOptions {
        val options = ChromeOptions()

        // Add all CI/CD specific options
        options.addArguments(CI_CD_CHROME_OPTIONS)

        // Additional performance and stability options
        options.addArguments("--disable-blink-features=AutomationControlled")
        options.addArguments("--disable-features=VizDisplayCompositor")
        options.addArguments("--enable-features=NetworkService,NetworkServiceLogging")
        options.addArguments("--log-level=3")
        options.addArguments("--silent")

        // Set user agent to avoid detection
        options.addArguments("--user-agent=Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")

        // Memory and performance optimizations
        options.addArguments("--memory-pressure-off")
        options.addArguments("--max_old_space_size=4096")

        // Set preferences for downloads and file handling
        val prefs = mapOf(
            "profile.default_content_settings.popups" to 0,
            "profile.default_content_setting_values.notifications" to 2,
            "profile.default_content_setting_values.geolocation" to 2,
            "profile.managed_default_content_settings.images" to 2,
            "download.prompt_for_download" to false,
            "download.directory_upgrade" to true,
            "download.default_directory" to DOWNLOADS_FOLDER,
            "safebrowsing.enabled" to false
        )
        options.setExperimentalOption("prefs", prefs)

        // Disable various features for stability
        options.setExperimentalOption("excludeSwitches", listOf("enable-automation"))
        options.setExperimentalOption("useAutomationExtension", false)

        return options
    }

    /**
     * Configures test-specific timeouts and retry settings.
     * Sets up appropriate timeouts for different types of operations.
     *
     * @return Duration-based timeout configuration
     */
    @Bean
    fun configureTestTimeouts(): Map<String, Duration> {
        return mapOf(
            "pageLoad" to Duration.ofSeconds(10),
            "elementWait" to Duration.ofSeconds(8),
            "collectionWait" to Duration.ofSeconds(10),
            "fastSetValue" to Duration.ofSeconds(2),
            "ajaxWait" to Duration.ofSeconds(5),
            "downloadWait" to Duration.ofSeconds(30)
        )
    }

    /**
     * Configures test data and environment settings.
     * Sets up test users and base URLs for E2E testing.
     *
     * @return Test environment configuration map
     */
    @Bean
    fun configureTestEnvironment(): Map<String, String> {
        return mapOf(
            "baseUrl" to DEFAULT_BASE_URL,
            "testUser1" to TestSecurityConfiguration.ALLOWED_EMAIL_1,
            "testUser2" to TestSecurityConfiguration.ALLOWED_EMAIL_2,
            "testUserNotAllowed" to TestSecurityConfiguration.NOT_ALLOWED_EMAIL,
            "testPassword" to TestSecurityConfiguration.TEST_PASSWORD,
            "screenshotsPath" to SCREENSHOTS_FOLDER,
            "reportsPath" to REPORTS_FOLDER,
            "downloadsPath" to DOWNLOADS_FOLDER
        )
    }
}
