package me.underlow.receipt.e2e

import com.codeborne.selenide.*
import com.codeborne.selenide.Condition.*
import com.codeborne.selenide.Configuration
import com.codeborne.selenide.Selenide.*
import me.underlow.receipt.model.User
import org.junit.jupiter.api.*
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.context.annotation.Import
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.context.TestConstructor
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.io.File
import java.nio.file.Files
import java.time.Duration
import kotlin.test.assertEquals

/**
 * End-to-end tests for upload page functionality using real browser and PostgreSQL database.
 * Tests complete user journey through file upload UI with various scenarios.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@ActiveProfiles("e2e")
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class UploadPageE2ETest(
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
            registry.add("spring.datasource.url", postgres::getJdbcUrl)
            registry.add("spring.datasource.username", postgres::getUsername)
            registry.add("spring.datasource.password", postgres::getPassword)
            registry.add("spring.security.oauth2.client.registration.google.client-id") { "test-client-id" }
            registry.add("spring.security.oauth2.client.registration.google.client-secret") { "test-client-secret" }
            registry.add("receipts.inbox-path") { Files.createTempDirectory("inbox").toString() }
            registry.add("receipts.attachments-path") { Files.createTempDirectory("attachments").toString() }
        }

        @BeforeAll
        @JvmStatic
        fun beforeAll() {
            Configuration.browser = "chrome"
            Configuration.headless = true
            Configuration.timeout = 10000
            Configuration.pageLoadTimeout = 30000
            Configuration.browserSize = "1920x1080"
            Configuration.downloadsFolder = Files.createTempDirectory("e2e-downloads").toString()
        }

        @AfterAll
        @JvmStatic
        fun afterAll() {
            closeWebDriver()
        }
    }

    @BeforeEach
    fun setup() {
        Configuration.baseUrl = "http://localhost:$port"

        // Clean database before each test
        cleanDatabase()
    }

    @AfterEach
    fun tearDown() {
        // Clean up any test files and data
        cleanDatabase()
    }

    /**
     * Test that upload page loads successfully for authenticated user
     * Given: User is authenticated
     * When: User navigates to upload page
     * Then: Upload page loads with all required elements
     */
    @Test
    fun `Given authenticated user, when opening upload page, then should display upload interface`() {
        // Given: Authentication handled by TestSecurityConfig

        // When: Navigate to upload page
        open("/upload")

        // Then: Page should load successfully
        `$`("h1").shouldHave(text("Upload Files"))

        // And: Upload instructions should be visible
        `$`(".upload-instructions h3").shouldHave(text("Upload Receipt Images or PDFs"))

        // And: Drop zone should be present
        `$`(".drop-zone").should(exist)
        `$`(".drop-zone p").shouldHave(text("Drag files here or click to browse"))

        // And: Browse button should be present
        `$`("#browseBtn").should(exist).shouldHave(text("Choose Files"))

        // And: File input should be present (hidden)
        `$`("#fileInput").should(exist)

        // And: Supported formats info should be visible
        `$`(".supported-formats").shouldHave(text("PDF, JPG, JPEG, PNG, GIF, BMP, TIFF"))
        `$`(".supported-formats").shouldHave(text("10MB per file"))

        // And: Navigation links should be present
        `$`("a[href='/dashboard']").should(exist).shouldHave(text("← Back to Dashboard"))
        `$`("a[href='/inbox']").should(exist).shouldHave(text("View Inbox"))
    }

    /**
     * Test file upload through UI interaction
     * Given: User is on upload page
     * When: User selects and uploads a valid file
     * Then: File should be uploaded successfully and show in UI
     */
    @Test
    fun `Given user on upload page, when uploading valid file, then should show success status`() {
        // Given: Test file (authentication handled by TestSecurityConfig, user created by FileUploadController)
        val testFile = createTestFile("test-receipt.pdf", "PDF test content")
        val testUser = createTestUser("testuser@example.com", "Test User")

        // When: Navigate to upload page
        open("/upload?userEmail=testuser@example.com")

        // And: Upload file through file input
        `$`("#fileInput").uploadFile(testFile)

        // Then: File should appear in file list with pending status
        `$`("#fileList .file-item").should(appear, Duration.ofSeconds(5))

        // And: File name should be displayed
        `$`("#fileList .file-item .file-info strong").shouldHave(text("test-receipt.pdf"))

        // And: File should show uploading status initially
        // cannot get here, uploads too fast
//        `$`("#fileList .file-item .file-status").shouldHave(text("Uploading..."))

        // And: Eventually should show success status
        `$`("#fileList .file-item .file-status").should(text("Success"), Duration.ofSeconds(10))
        `$`("#fileList .file-item .file-status").shouldHave(cssClass("status-success"))

        // And: File should be stored in database (for test user)
        val fileCount = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM incoming_files WHERE filename = ? AND user_id = ?",
            Int::class.java,
            "test-receipt.pdf",
            testUser.id
        )
        assertEquals(1, fileCount)

        // Cleanup
        testFile.delete()
    }

    /**
     * Test that oversized file upload shows error
     * Given: User is on upload page
     * When: User attempts to upload file larger than 10MB
     * Then: Should show validation error in UI
     */
    @Test
    fun `Given user on upload page, when uploading oversized file, then should show error message`() {
        // Given: Oversized file (authentication handled by TestSecurityConfig)

        // Create a file larger than 10MB
        val oversizedFile = Files.createTempFile("oversized", ".pdf").toFile()
        val content = ByteArray(11 * 1024 * 1024) { 'A'.code.toByte() } // 11MB
        oversizedFile.writeBytes(content)

        // When: Navigate to upload page
        open("/upload")

        // And: Try to upload oversized file
        `$`("#fileInput").uploadFile(oversizedFile)

        // Then: Should show error status in file list
        `$`("#fileList .file-item").should(appear, Duration.ofSeconds(5))
        `$`("#fileList .file-item .file-status").should(text("Upload failed"), Duration.ofSeconds(10))
        `$`("#fileList .file-item .file-status").shouldHave(cssClass("status-error"))

        // And: No file should be stored in database
        val testUser = createTestUser("testuser@example.com", "Test User")
        val fileCount = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM incoming_files WHERE user_id = ?",
            Int::class.java,
            testUser.id
        )
        assertEquals(0, fileCount)

        // Cleanup
        oversizedFile.delete()
    }

    /**
     * Test multiple file uploads
     * Given: User is on upload page
     * When: User uploads multiple valid files at once
     * Then: All files should be uploaded and show individual statuses
     */
    @Test
    fun `Given user on upload page, when uploading multiple files, then should handle all files independently`() {
        // Given: Multiple test files (authentication handled by TestSecurityConfig)
        val testUser = createTestUser("testuser@example.com", "Test User")

        val file1 = createTestFile("receipt1.pdf", "Receipt 1 content")
        val file2 = createTestFile("receipt2.jpg", "Receipt 2 content")
        val file3 = createTestFile("receipt3.png", "Receipt 3 content")

        // When: Navigate to upload page
        open("/upload?userEmail=${testUser.email}")

        // And: Upload multiple files
        `$`("#fileInput").uploadFile(file1, file2, file3)

        // Then: All files should appear in the file list
        `$$`("#fileList .file-item").shouldHave(CollectionCondition.size(3))

        // And: All files should eventually show success status
        `$$`("#fileList .file-item .file-status").forEach { status ->
            status.should(text("Success"), Duration.ofSeconds(15))
        }

        // And: All files should be stored in database
        val fileCount = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM incoming_files WHERE user_id = ?",
            Int::class.java,
            testUser.id
        )
        assertEquals(3, fileCount)

        // Cleanup
        file1.delete()
        file2.delete()
        file3.delete()
    }

    /**
     * Test navigation from upload page
     * Given: User is on upload page
     * When: User clicks navigation links
     * Then: Should navigate to correct pages
     */
    @Test
    fun `Given user on upload page, when clicking navigation links, then should navigate correctly`() {
        // Given: Authentication handled by TestSecurityConfig

        // When: Navigate to upload page
        open("/upload")

        // And: Click back to dashboard link
        `$`("a[href='/dashboard']").click()

        // Then: Should be on dashboard page
        `$`("h1").shouldHave(text("Receipt Tracker Dashboard"))

        // When: Navigate back to upload page
        open("/upload")

        // And: Click view inbox link
        `$`("a[href='/inbox']").click()

        // Then: Should be on inbox page
        `$`("h1").shouldHave(text("Inbox"))
    }

    /**
     * Creates a test user in the database
     */
    private fun createTestUser(email: String, name: String): User {
        jdbcTemplate.update(
            "INSERT INTO users (email, name) VALUES (?, ?) ON CONFLICT (email) DO NOTHING",
            email, name
        )

        return jdbcTemplate.queryForObject(
            "SELECT id, email, name FROM users WHERE email = ?",
            { rs, _ ->
                User(
                    id = rs.getLong("id"),
                    email = rs.getString("email"),
                    name = rs.getString("name")
                )
            },
            email
        )!!
    }


    /**
     * Creates a test file with specified name and content
     */
    private fun createTestFile(filename: String, content: String): File {
        val tempFile = Files.createTempFile("e2e-test", filename.substringAfterLast(".")).toFile()
        tempFile.writeText(content)
        return File(tempFile.parent, filename).also {
            tempFile.renameTo(it)
        }
    }

    /**
     * Cleans up test data from database
     */
    private fun cleanDatabase() {
        try {
            jdbcTemplate.update("DELETE FROM incoming_files")
            jdbcTemplate.update("DELETE FROM login_events")
            jdbcTemplate.update("DELETE FROM users WHERE email LIKE '%test%' OR email LIKE '%upload%'")
        } catch (e: Exception) {
            // Ignore cleanup errors during test setup
        }
    }
}
