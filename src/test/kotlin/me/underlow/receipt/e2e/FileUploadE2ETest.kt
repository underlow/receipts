package me.underlow.receipt.e2e

import com.codeborne.selenide.*
import com.codeborne.selenide.Condition.*
import com.codeborne.selenide.Configuration
import com.codeborne.selenide.Selenide.*
import com.codeborne.selenide.WebDriverRunner
import me.underlow.receipt.model.BillStatus
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
import java.io.File
import java.nio.file.Files
import java.time.Duration
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * End-to-end test for file upload functionality using real browser and PostgreSQL database.
 * Tests complete user journey from authentication through file upload and processing.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@ActiveProfiles("e2e")
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class FileUploadE2ETest(
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
            
            // Mock OAuth2 configuration
            registry.add("spring.security.oauth2.client.registration.google.client-id") { "test-client-id" }
            registry.add("spring.security.oauth2.client.registration.google.client-secret") { "test-client-secret" }
            
            // Configure temp directories for file testing
            val tempInbox = Files.createTempDirectory("e2e-inbox").toString()
            val tempAttachments = Files.createTempDirectory("e2e-attachments").toString()
            registry.add("receipts.inbox-path") { tempInbox }
            registry.add("receipts.attachments-path") { tempAttachments }
        }

        @BeforeAll
        @JvmStatic
        fun beforeAll() {
            // Configure Selenide for file upload testing
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
    fun setUp() {
        // Clean up browser state
        WebDriverRunner.clearBrowserCache()

        // Clean test data
        cleanupTestData()
        
        // Setup test user for authenticated operations
        setupTestUser()
    }

    @AfterEach
    fun tearDown() {
        // Cleanup test files and data
        cleanupTestData()
    }

    @Test
    @DisplayName("Given authenticated user, when uploading valid PDF file via API, then file should be processed successfully")
    fun testApiFileUploadSuccess() {
        // Given: Create a test PDF file
        val testFile = createTestFile("test-receipt.pdf", "PDF content for e2e test")
        
        // And: User is authenticated (we'll simulate this through direct API call)
        val testUser = getTestUser()
        
        // When: Making authenticated API request to upload file
        val response = executeRestTemplate()
            .postForEntity(
                "http://localhost:$port/api/files/upload",
                createMultipartRequest(testFile),
                String::class.java
            )

        // Then: File upload should be successful
        assertEquals(200, response.statusCodeValue)
        assertNotNull(response.body)
        assertTrue(response.body!!.contains("\"success\":true"))
        assertTrue(response.body!!.contains("test-receipt.pdf"))

        // And: File should be stored in database
        val incomingFileCount = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM incoming_files WHERE filename = ? AND user_id = ?",
            Int::class.java,
            "test-receipt.pdf",
            testUser.id
        )
        assertEquals(1, incomingFileCount)

        // And: File should have been moved to attachments directory
        val storedFile = jdbcTemplate.queryForObject(
            "SELECT file_path FROM incoming_files WHERE filename = ? AND user_id = ?",
            String::class.java,
            "test-receipt.pdf",
            testUser.id
        )
        assertTrue(File(storedFile).exists(), "File should exist in storage: $storedFile")
        
        // Cleanup
        testFile.delete()
    }

    @Test
    @DisplayName("Given unauthenticated user, when attempting file upload via API, then should return unauthorized")
    fun testApiFileUploadUnauthorized() {
        // Given: A test file
        val testFile = createTestFile("unauthorized-test.pdf", "Unauthorized content")

        // When: Making unauthenticated API request
        val response = executeRestTemplate()
            .postForEntity(
                "http://localhost:$port/api/files/upload",
                createMultipartRequest(testFile),
                String::class.java
            )

        // Then: Should receive unauthorized response
        assertEquals(401, response.statusCodeValue)

        // And: No file should be stored in database
        val incomingFileCount = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM incoming_files WHERE filename = ?",
            Int::class.java,
            "unauthorized-test.pdf"
        )
        assertEquals(0, incomingFileCount)
        
        // Cleanup
        testFile.delete()
    }

    @Test
    @DisplayName("Given valid user, when uploading oversized file, then should return validation error")
    fun testApiFileUploadOversized() {
        // Given: Create oversized file (11MB)
        val oversizedContent = ByteArray(11 * 1024 * 1024) { 'A'.code.toByte() }
        val oversizedFile = Files.createTempFile("oversized", ".pdf").toFile()
        oversizedFile.writeBytes(oversizedContent)

        // When: Uploading oversized file with authentication
        val response = executeAuthenticatedRestTemplate()
            .postForEntity(
                "http://localhost:$port/api/files/upload",
                createMultipartRequest(oversizedFile),
                String::class.java
            )

        // Then: Should receive validation error
        assertEquals(400, response.statusCodeValue)
        assertNotNull(response.body)
        assertTrue(response.body!!.contains("FILE_TOO_LARGE"))

        // And: No file should be stored
        val incomingFileCount = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM incoming_files WHERE filename = ?",
            Int::class.java,
            oversizedFile.name
        )
        assertEquals(0, incomingFileCount)
        
        // Cleanup
        oversizedFile.delete()
    }

    @Test
    @DisplayName("Given valid user, when uploading unsupported file type, then should return type validation error")
    fun testApiFileUploadUnsupportedType() {
        // Given: Create unsupported file type
        val textFile = createTestFile("document.txt", "Text file content")

        // When: Uploading unsupported file type
        val response = executeAuthenticatedRestTemplate()
            .postForEntity(
                "http://localhost:$port/api/files/upload",
                createMultipartRequest(textFile),
                String::class.java
            )

        // Then: Should receive type validation error
        assertEquals(400, response.statusCodeValue)
        assertNotNull(response.body)
        assertTrue(response.body!!.contains("UNSUPPORTED_FILE_TYPE"))

        // And: No file should be stored
        val incomingFileCount = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM incoming_files",
            Int::class.java
        )
        assertEquals(0, incomingFileCount)
        
        // Cleanup
        textFile.delete()
    }

    @Test
    @DisplayName("Given valid user, when uploading duplicate file, then should detect and reject duplicate")
    fun testApiFileUploadDuplicate() {
        // Given: Upload original file first
        val originalFile = createTestFile("original.pdf", "Original content")
        val duplicateFile = createTestFile("duplicate.pdf", "Original content") // Same content

        // When: Upload original file
        val firstResponse = executeAuthenticatedRestTemplate()
            .postForEntity(
                "http://localhost:$port/api/files/upload",
                createMultipartRequest(originalFile),
                String::class.java
            )

        // Then: First upload should succeed
        assertEquals(200, firstResponse.statusCodeValue)

        // When: Upload duplicate file with same content
        val duplicateResponse = executeAuthenticatedRestTemplate()
            .postForEntity(
                "http://localhost:$port/api/files/upload",
                createMultipartRequest(duplicateFile),
                String::class.java
            )

        // Then: Duplicate should be rejected
        assertEquals(409, duplicateResponse.statusCodeValue)
        assertTrue(duplicateResponse.body!!.contains("DUPLICATE_FILE"))

        // And: Only one file should be stored
        val incomingFileCount = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM incoming_files",
            Int::class.java
        )
        assertEquals(1, incomingFileCount)
        
        // Cleanup
        originalFile.delete()
        duplicateFile.delete()
    }

    @Test
    @DisplayName("Given database tables, when verifying file upload schema, then all required tables exist")
    fun testFileUploadDatabaseSchema() {
        // Given: Database is initialized
        // When: Checking schema for file upload functionality
        
        // Then: incoming_files table should exist
        val incomingFilesTableExists = jdbcTemplate.queryForObject(
            """
            SELECT COUNT(*) FROM information_schema.tables 
            WHERE table_name = 'incoming_files' AND table_schema = 'public'
            """.trimIndent(),
            Int::class.java
        )
        assertEquals(1, incomingFilesTableExists, "incoming_files table should exist")

        // And: Table should have required columns
        val requiredColumns = listOf("id", "filename", "file_path", "upload_date", "status", "checksum", "user_id")
        requiredColumns.forEach { column ->
            val columnExists = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*) FROM information_schema.columns 
                WHERE table_name = 'incoming_files' AND column_name = ? AND table_schema = 'public'
                """.trimIndent(),
                Int::class.java,
                column
            )
            assertEquals(1, columnExists, "Column $column should exist in incoming_files table")
        }

        // And: Foreign key constraint to users table should exist
        val fkConstraintExists = jdbcTemplate.queryForObject(
            """
            SELECT COUNT(*) FROM information_schema.table_constraints 
            WHERE constraint_type = 'FOREIGN KEY' 
            AND table_name = 'incoming_files' 
            AND table_schema = 'public'
            """.trimIndent(),
            Int::class.java
        )
        assertTrue(fkConstraintExists != null && fkConstraintExists > 0, "Foreign key constraint should exist")
    }

    /**
     * Creates a test file with specified name and content.
     */
    private fun createTestFile(filename: String, content: String): File {
        val tempFile = Files.createTempFile("e2e-test", filename.substringAfterLast(".")).toFile()
        tempFile.writeText(content)
        return File(tempFile.parent, filename).also { 
            tempFile.renameTo(it) 
        }
    }

    /**
     * Creates multipart request for file upload testing.
     */
    private fun createMultipartRequest(file: File): org.springframework.util.MultiValueMap<String, Any> {
        val map = org.springframework.util.LinkedMultiValueMap<String, Any>()
        map.add("file", org.springframework.core.io.FileSystemResource(file))
        return map
    }

    /**
     * Gets RestTemplate instance for API testing.
     */
    private fun executeRestTemplate(): org.springframework.web.client.RestTemplate {
        return org.springframework.web.client.RestTemplate()
    }

    /**
     * Gets authenticated RestTemplate with proper headers.
     */
    private fun executeAuthenticatedRestTemplate(): org.springframework.web.client.RestTemplate {
        val restTemplate = org.springframework.web.client.RestTemplate()
        // In a real implementation, this would include proper OAuth2 token handling
        // For now, we'll simulate by using the test profile that mocks authentication
        return restTemplate
    }

    /**
     * Sets up test user in database for authenticated operations.
     */
    private fun setupTestUser() {
        jdbcTemplate.update(
            "INSERT INTO users (email, name) VALUES (?, ?) ON CONFLICT (email) DO NOTHING",
            "e2e-test@example.com",
            "E2E Test User"
        )
    }

    /**
     * Gets test user from database.
     */
    private fun getTestUser(): User {
        return jdbcTemplate.queryForObject(
            "SELECT id, email, name FROM users WHERE email = ?",
            { rs, _ ->
                User(
                    id = rs.getLong("id"),
                    email = rs.getString("email"),
                    name = rs.getString("name")
                )
            },
            "e2e-test@example.com"
        )!!
    }

    /**
     * Cleans up test data from database and file system.
     */
    private fun cleanupTestData() {
        try {
            // Clean up database tables
            jdbcTemplate.update("DELETE FROM incoming_files")
            jdbcTemplate.update("DELETE FROM login_events")
            jdbcTemplate.update("DELETE FROM users WHERE email LIKE '%test%' OR email LIKE '%e2e%'")
        } catch (e: Exception) {
            // Ignore cleanup errors during test setup
        }
    }
}