package me.underlow.receipt.e2e

import com.codeborne.selenide.Selenide.*
import com.codeborne.selenide.Configuration
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
 * Debug test to understand why the InboxE2ETest files aren't being found
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@ActiveProfiles("e2e")
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class InboxDebugTest(
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
     * Debug test to verify database operations
     */
    @Test
    @org.junit.jupiter.api.Disabled("Debug test that doesn't work with OAuth2 authentication")
    fun `Debug database operations and user file lookup`() {
        println("=== Starting debug test ===")

        // Step 1: Create test user
        val testUser = createTestUser("debug@example.com", "Debug User")
        println("Created user: id=${testUser.id}, email=${testUser.email}")

        // Step 2: Verify user was created
        val userCount = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM users WHERE email = ?",
            Int::class.java,
            "debug@example.com"
        )
        println("User count in database: $userCount")

        // Step 3: Create test files
        val file1 = createTestIncomingFile(testUser.id!!, "test-file-1.pdf", BillStatus.PENDING)
        val file2 = createTestIncomingFile(testUser.id!!, "test-file-2.jpg", BillStatus.APPROVED)
        println("Created files: file1.id=${file1.id}, file2.id=${file2.id}")

        // Step 4: Verify files were created
        val fileCount = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM incoming_files WHERE user_id = ?",
            Int::class.java,
            testUser.id
        )
        println("File count in database for user ${testUser.id}: $fileCount")

        // Step 5: Query files directly by email (simulate service call)
        val filesForUser = jdbcTemplate.query(
            """
            SELECT f.id, f.filename, f.status 
            FROM incoming_files f 
            JOIN users u ON f.user_id = u.id 
            WHERE u.email = ?
            """.trimIndent(),
            { rs, _ ->
                Triple(rs.getLong("id"), rs.getString("filename"), rs.getString("status"))
            },
            "debug@example.com"
        )
        println("Files found for user debug@example.com: $filesForUser")

        // Step 6: Test authentication and page load
        println("Navigating to inbox with authentication...")
        open("/inbox?userEmail=${testUser.email}")

        // Verify page loads
        val pageTitle = `$`("h1").text()
        println("Page title: $pageTitle")

        // Check what's actually displayed - using space selector for multiple classes
        val statusBadgeText = `$`("a.status-badge.all").text()
        println("Status badge text: $statusBadgeText")

        println("=== Debug test completed ===")
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
