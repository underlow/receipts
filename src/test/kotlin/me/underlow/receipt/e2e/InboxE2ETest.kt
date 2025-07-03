package me.underlow.receipt.e2e

import com.codeborne.selenide.*
import com.codeborne.selenide.Condition.*
import com.codeborne.selenide.Configuration
import com.codeborne.selenide.Selenide.*
import com.codeborne.selenide.CollectionCondition
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
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter.event
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.io.File
import java.nio.file.Files
import java.time.Duration
import java.time.LocalDateTime
import kotlin.test.assertTrue

/**
 * End-to-end tests for inbox functionality using real browser and PostgreSQL database.
 * Tests complete user journey through inbox with various status scenarios.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@ActiveProfiles("e2e")
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class InboxE2ETest(
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
     * Test the bug where inbox page crashes when user has no files (all status counts are null)
     * Given: User has no files in the system
     * When: User navigates to inbox page
     * Then: Page loads successfully with all status counts showing 0
     */
    @Test
    fun `Given user with no files, when opening inbox, then should display zero counts without error`() {
        // Given: Create a user with no files
        val testUser = createTestUser("nofiles@example.com", "No Files User")

        // When: Navigate directly to inbox with userEmail parameter (bypassing login)
        open("/inbox?userEmail=${testUser.email}")

        // Then: Page should load successfully
        `$`("h1").shouldHave(text("Inbox"))

        // And: All status badges should show count 0
        `$`(".status-badge.all").shouldHave(text("All (0)"))
        `$`(".status-badge.pending").shouldHave(text("Pending (0)"))
        `$`(".status-badge.processing").shouldHave(text("Processing (0)"))
        `$`(".status-badge.approved").shouldHave(text("Approved (0)"))
        `$`(".status-badge.rejected").shouldHave(text("Rejected (0)"))

        // And: Empty state should be displayed
        `$`(".empty-state").shouldBe(visible)
        `$`(".empty-state h3").shouldHave(text("No files found"))
    }

    /**
     * Test the bug where inbox page crashes when user has files with only one status type
     * Given: User has files with only PENDING status (other statuses have null counts)
     * When: User navigates to inbox page
     * Then: Page loads successfully with correct counts for each status
     */
    @Test
    fun `Given user with only pending files, when opening inbox, then should display correct counts without error`() {
        // Given: Create a user with only pending files
        val testUser = createTestUser("pendingonly@example.com", "Pending Only User")
        createTestIncomingFile(testUser.id!!, "pending-file-1.pdf", BillStatus.PENDING)
        createTestIncomingFile(testUser.id!!, "pending-file-2.jpg", BillStatus.PENDING)

        // When: Navigate to inbox page as authenticated user
        simulateLogin(testUser)
        open("/inbox")

        // Then: Page should load successfully
        `$`("h1").shouldHave(text("Inbox"))

        // And: Status badges should show correct counts
        `$`(".status-badge.all").shouldHave(text("All (2)"))
        `$`(".status-badge.pending").shouldHave(text("Pending (2)"))
        `$`(".status-badge.processing").shouldHave(text("Processing (0)"))
        `$`(".status-badge.approved").shouldHave(text("Approved (0)"))
        `$`(".status-badge.rejected").shouldHave(text("Rejected (0)"))

        // And: Files should be displayed
        `$$`(".file-card").shouldHave(CollectionCondition.size(2))
        `$`(".empty-state").shouldNot(exist)
    }

    /**
     * Test with mixed status files to ensure all counts work correctly
     * Given: User has files with various statuses
     * When: User navigates to inbox page
     * Then: Page loads successfully with correct counts for each status
     */
    @Test
    fun `Given user with mixed status files, when opening inbox, then should display correct status counts`() {
        // Given: Create a user with files in various statuses
        val testUser = createTestUser("mixed@example.com", "Mixed Status User")
        createTestIncomingFile(testUser.id!!, "pending-file.pdf", BillStatus.PENDING)
        createTestIncomingFile(testUser.id!!, "processing-file.jpg", BillStatus.PROCESSING)
        createTestIncomingFile(testUser.id!!, "approved-file-1.png", BillStatus.APPROVED)
        createTestIncomingFile(testUser.id!!, "approved-file-2.pdf", BillStatus.APPROVED)
        createTestIncomingFile(testUser.id!!, "rejected-file.jpg", BillStatus.REJECTED)

        // When: Navigate to inbox page as authenticated user
        simulateLogin(testUser)
        open("/inbox")

        // Then: Page should load successfully
        `$`("h1").shouldHave(text("Inbox"))

        // And: Status badges should show correct counts
        `$`(".status-badge.all").shouldHave(text("All (5)"))
        `$`(".status-badge.pending").shouldHave(text("Pending (1)"))
        `$`(".status-badge.processing").shouldHave(text("Processing (1)"))
        `$`(".status-badge.approved").shouldHave(text("Approved (2)"))
        `$`(".status-badge.rejected").shouldHave(text("Rejected (1)"))

        // And: All files should be displayed
        `$$`(".file-card").shouldHave(CollectionCondition.size(5))
        `$`(".empty-state").shouldNot(exist)
    }

    /**
     * Test status filtering functionality works correctly
     * Given: User has mixed status files
     * When: User clicks on different status filter badges
     * Then: Only files with selected status are displayed
     */
    @Test
    fun `Given user with mixed files, when filtering by status, then should show only files with selected status`() {
        // Given: Create a user with files in various statuses
        val testUser = createTestUser("filter@example.com", "Filter Test User")
        createTestIncomingFile(testUser.id!!, "pending-file.pdf", BillStatus.PENDING)
        createTestIncomingFile(testUser.id!!, "approved-file.jpg", BillStatus.APPROVED)

        // When: Navigate to inbox page
        simulateLogin(testUser)
        open("/inbox")

        // Then: Initially should show all files
        `$$`(".file-card").shouldHave(CollectionCondition.size(2))

        // When: Click on pending filter
        `$`(".status-badge.pending").click()

        // Then: Should show only pending files
        `$$`(".file-card").shouldHave(CollectionCondition.size(1))
        `$`(".file-card .file-status.pending").shouldBe(visible)

        // When: Click on approved filter
        `$`(".status-badge.approved").click()

        // Then: Should show only approved files
        `$$`(".file-card").shouldHave(CollectionCondition.size(1))
        `$`(".file-card .file-status.approved").shouldBe(visible)

        // When: Click on all filter
        `$`(".status-badge.all").click()

        // Then: Should show all files again
        `$$`(".file-card").shouldHave(CollectionCondition.size(2))
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
        // Create a temporary file for testing
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

    /**
     * Simulates OAuth2 login by navigating to a page with the user email parameter
     * The TestSecurityConfig mock filter will automatically set up authentication
     */
    private fun simulateLogin(user: User) {
        // Navigate directly to inbox with userEmail parameter
        // The MockOAuth2AuthenticationFilter will set up authentication automatically
        open("/inbox?userEmail=${user.email}")
        
        // The page should load with proper authentication
        `$`("h1").shouldHave(text("Inbox"))
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
