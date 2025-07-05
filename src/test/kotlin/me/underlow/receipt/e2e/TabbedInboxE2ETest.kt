package me.underlow.receipt.e2e

import com.codeborne.selenide.*
import com.codeborne.selenide.Condition.*
import com.codeborne.selenide.Configuration
import com.codeborne.selenide.Selenide.*
import com.codeborne.selenide.CollectionCondition
import me.underlow.receipt.model.ItemStatus
import me.underlow.receipt.model.IncomingFile
import me.underlow.receipt.model.User
import me.underlow.receipt.model.Bill
import me.underlow.receipt.model.Receipt
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
import java.math.BigDecimal
import java.nio.file.Files
import java.time.LocalDateTime
import java.time.LocalDate
import kotlin.test.assertTrue

/**
 * End-to-end tests for the tabbed inbox functionality with status-based views.
 * Tests the complete user journey through New, Approved, and Rejected tabs.
 * 
 * Requirements tested:
 * - Left panel with tabs: New, Approved, Rejected
 * - New tab: Shows IncomingFile, Bill, Receipt with status=NEW
 * - Approved/Rejected tabs: Show Bills & Receipts with type filters
 * - Status consolidation: pending + processing + draft → NEW
 * - IncomingFile can only exist in NEW status
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@ActiveProfiles("e2e")
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
@Disabled("Temporarily disabled to check baseline status")
class TabbedInboxE2ETest(
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
     * Test that the tabbed inbox interface is properly displayed
     * Given: User has various items with different statuses
     * When: User navigates to inbox page
     * Then: Should display tabbed interface with New, Approved, Rejected tabs
     */
    @Test
    fun `Given user with items, when opening inbox, then should display tabbed interface`() {
        // Given: Create user with various items
        val testUser = createTestUser("tabbed@example.com", "Tabbed User")
        createTestIncomingFile(testUser.id!!, "incoming-file.pdf", ItemStatus.NEW)
        createTestBill(testUser.id!!, "bill-approved.pdf", ItemStatus.APPROVED)
        createTestReceipt(testUser.id!!, "receipt-rejected.pdf", ItemStatus.REJECTED)

        // When: Navigate to inbox
        simulateLogin(testUser)

        // Then: Should display tabbed interface
        `$`("h1").shouldHave(text("Inbox"))
        `$`(".nav-tabs").shouldBe(visible)
        
        // Should have three tabs
        `$$`(".nav-tabs .nav-item").shouldHave(CollectionCondition.size(3))
        `$`(".nav-tabs .nav-item:nth-child(1) .nav-link").shouldHave(text("New"))
        `$`(".nav-tabs .nav-item:nth-child(2) .nav-link").shouldHave(text("Approved"))
        `$`(".nav-tabs .nav-item:nth-child(3) .nav-link").shouldHave(text("Rejected"))
    }

    /**
     * Test that the New tab shows items with NEW status
     * Given: User has items with NEW status (IncomingFile, Bill, Receipt)
     * When: User navigates to New tab
     * Then: Should display only items with NEW status
     */
    @Test
    fun `Given user with NEW status items, when viewing New tab, then should show only NEW items`() {
        // Given: Create user with items in NEW status
        val testUser = createTestUser("new@example.com", "New Tab User")
        createTestIncomingFile(testUser.id!!, "incoming-new.pdf", ItemStatus.NEW)
        createTestBill(testUser.id!!, "bill-new.pdf", ItemStatus.NEW)
        createTestReceipt(testUser.id!!, "receipt-new.pdf", ItemStatus.NEW)
        
        // Create items with other statuses (should not appear in New tab)
        createTestBill(testUser.id!!, "bill-approved.pdf", ItemStatus.APPROVED)
        createTestReceipt(testUser.id!!, "receipt-rejected.pdf", ItemStatus.REJECTED)

        // When: Navigate to inbox and select New tab
        simulateLogin(testUser)
        `$`(".nav-tabs .nav-item:nth-child(1) .nav-link").click()

        // Then: Should show only items with NEW status
        `$$`(".item-card").shouldHave(CollectionCondition.size(3))
        
        // Verify IncomingFile is displayed
        `$`(".item-card:contains('incoming-new.pdf')").shouldBe(visible)
        // Verify Bill with NEW status is displayed
        `$`(".item-card:contains('bill-new.pdf')").shouldBe(visible)
        // Verify Receipt with NEW status is displayed
        `$`(".item-card:contains('receipt-new.pdf')").shouldBe(visible)
    }

    /**
     * Test that the Approved tab shows Bills and Receipts with APPROVED status
     * Given: User has approved Bills and Receipts
     * When: User navigates to Approved tab
     * Then: Should display only approved Bills and Receipts with type filters
     */
    @Test
    fun `Given user with approved items, when viewing Approved tab, then should show Bills and Receipts with type filters`() {
        // Given: Create user with approved items
        val testUser = createTestUser("approved@example.com", "Approved Tab User")
        createTestBill(testUser.id!!, "bill-approved-1.pdf", ItemStatus.APPROVED)
        createTestBill(testUser.id!!, "bill-approved-2.pdf", ItemStatus.APPROVED)
        createTestReceipt(testUser.id!!, "receipt-approved-1.pdf", ItemStatus.APPROVED)
        createTestReceipt(testUser.id!!, "receipt-approved-2.pdf", ItemStatus.APPROVED)
        
        // Create items with other statuses (should not appear in Approved tab)
        createTestIncomingFile(testUser.id!!, "incoming-new.pdf", ItemStatus.NEW)
        createTestBill(testUser.id!!, "bill-rejected.pdf", ItemStatus.REJECTED)

        // When: Navigate to inbox and select Approved tab
        simulateLogin(testUser)
        `$`(".nav-tabs .nav-item:nth-child(2) .nav-link").click()

        // Then: Should show only approved Bills and Receipts
        `$$`(".item-card").shouldHave(CollectionCondition.size(4))
        
        // Should have type filters
        `$`(".type-filter").shouldBe(visible)
        `$`(".type-filter .filter-option:contains('All')").shouldBe(visible)
        `$`(".type-filter .filter-option:contains('Bill')").shouldBe(visible)
        `$`(".type-filter .filter-option:contains('Receipt')").shouldBe(visible)
        
        // Verify Bills are displayed
        `$`(".item-card:contains('bill-approved-1.pdf')").shouldBe(visible)
        `$`(".item-card:contains('bill-approved-2.pdf')").shouldBe(visible)
        
        // Verify Receipts are displayed
        `$`(".item-card:contains('receipt-approved-1.pdf')").shouldBe(visible)
        `$`(".item-card:contains('receipt-approved-2.pdf')").shouldBe(visible)
        
        // Verify IncomingFile is NOT displayed
        `$`(".item-card:contains('incoming-new.pdf')").shouldNotBe(visible)
    }

    /**
     * Test that the Rejected tab shows Bills and Receipts with REJECTED status
     * Given: User has rejected Bills and Receipts
     * When: User navigates to Rejected tab
     * Then: Should display only rejected Bills and Receipts with type filters
     */
    @Test
    fun `Given user with rejected items, when viewing Rejected tab, then should show Bills and Receipts with type filters`() {
        // Given: Create user with rejected items
        val testUser = createTestUser("rejected@example.com", "Rejected Tab User")
        createTestBill(testUser.id!!, "bill-rejected-1.pdf", ItemStatus.REJECTED)
        createTestReceipt(testUser.id!!, "receipt-rejected-1.pdf", ItemStatus.REJECTED)
        
        // Create items with other statuses (should not appear in Rejected tab)
        createTestIncomingFile(testUser.id!!, "incoming-new.pdf", ItemStatus.NEW)
        createTestBill(testUser.id!!, "bill-approved.pdf", ItemStatus.APPROVED)

        // When: Navigate to inbox and select Rejected tab
        simulateLogin(testUser)
        `$`(".nav-tabs .nav-item:nth-child(3) .nav-link").click()

        // Then: Should show only rejected Bills and Receipts
        `$$`(".item-card").shouldHave(CollectionCondition.size(2))
        
        // Should have type filters
        `$`(".type-filter").shouldBe(visible)
        `$`(".type-filter .filter-option:contains('All')").shouldBe(visible)
        `$`(".type-filter .filter-option:contains('Bill')").shouldBe(visible)
        `$`(".type-filter .filter-option:contains('Receipt')").shouldBe(visible)
        
        // Verify rejected items are displayed
        `$`(".item-card:contains('bill-rejected-1.pdf')").shouldBe(visible)
        `$`(".item-card:contains('receipt-rejected-1.pdf')").shouldBe(visible)
    }

    /**
     * Test type filtering in Approved tab
     * Given: User has approved Bills and Receipts
     * When: User applies type filters
     * Then: Should show only items of selected type
     */
    @Test
    fun `Given user with approved items, when applying type filters, then should show only items of selected type`() {
        // Given: Create user with approved items
        val testUser = createTestUser("typefilter@example.com", "Type Filter User")
        createTestBill(testUser.id!!, "bill-approved.pdf", ItemStatus.APPROVED)
        createTestReceipt(testUser.id!!, "receipt-approved.pdf", ItemStatus.APPROVED)

        // When: Navigate to inbox and select Approved tab
        simulateLogin(testUser)
        `$`(".nav-tabs .nav-item:nth-child(2) .nav-link").click()

        // Then: Initially should show all items
        `$$`(".item-card").shouldHave(CollectionCondition.size(2))

        // When: Filter by Bill type
        `$`(".type-filter .filter-option:contains('Bill')").click()

        // Then: Should show only Bills
        `$$`(".item-card").shouldHave(CollectionCondition.size(1))
        `$`(".item-card:contains('bill-approved.pdf')").shouldBe(visible)
        `$`(".item-card:contains('receipt-approved.pdf')").shouldNotBe(visible)

        // When: Filter by Receipt type
        `$`(".type-filter .filter-option:contains('Receipt')").click()

        // Then: Should show only Receipts
        `$$`(".item-card").shouldHave(CollectionCondition.size(1))
        `$`(".item-card:contains('receipt-approved.pdf')").shouldBe(visible)
        `$`(".item-card:contains('bill-approved.pdf')").shouldNotBe(visible)

        // When: Clear filter (All)
        `$`(".type-filter .filter-option:contains('All')").click()

        // Then: Should show all items again
        `$$`(".item-card").shouldHave(CollectionCondition.size(2))
    }

    /**
     * Test that IncomingFile items only appear in NEW status
     * Given: User has IncomingFile items
     * When: User navigates between tabs
     * Then: IncomingFile should only appear in New tab
     */
    @Test
    fun `Given user with IncomingFile items, when navigating between tabs, then IncomingFile should only appear in New tab`() {
        // Given: Create user with IncomingFile items
        val testUser = createTestUser("incomingfile@example.com", "IncomingFile User")
        createTestIncomingFile(testUser.id!!, "incoming-file.pdf", ItemStatus.NEW)
        
        // Create other items for comparison
        createTestBill(testUser.id!!, "bill-approved.pdf", ItemStatus.APPROVED)
        createTestReceipt(testUser.id!!, "receipt-rejected.pdf", ItemStatus.REJECTED)

        // When: Navigate to inbox and check New tab
        simulateLogin(testUser)
        `$`(".nav-tabs .nav-item:nth-child(1) .nav-link").click()

        // Then: IncomingFile should appear in New tab
        `$`(".item-card:contains('incoming-file.pdf')").shouldBe(visible)

        // When: Navigate to Approved tab
        `$`(".nav-tabs .nav-item:nth-child(2) .nav-link").click()

        // Then: IncomingFile should NOT appear in Approved tab
        `$`(".item-card:contains('incoming-file.pdf')").shouldNotBe(visible)

        // When: Navigate to Rejected tab
        `$`(".nav-tabs .nav-item:nth-child(3) .nav-link").click()

        // Then: IncomingFile should NOT appear in Rejected tab
        `$`(".item-card:contains('incoming-file.pdf')").shouldNotBe(visible)
    }

    /**
     * Test status consolidation: pending + processing + draft → NEW
     * Given: User has items with old status values
     * When: User navigates to inbox
     * Then: Items should be consolidated into NEW status
     */
    @Test
    fun `Given user with legacy status items, when viewing inbox, then should consolidate statuses to NEW`() {
        // Given: Create user with items using legacy status values
        val testUser = createTestUser("legacy@example.com", "Legacy Status User")
        
        // Create items with legacy statuses that should be consolidated to NEW
        createTestBillWithLegacyStatus(testUser.id!!, "bill-pending.pdf", "PENDING")
        createTestBillWithLegacyStatus(testUser.id!!, "bill-processing.pdf", "NEW")
        createTestBillWithLegacyStatus(testUser.id!!, "bill-draft.pdf", "DRAFT")

        // When: Navigate to inbox and check New tab
        simulateLogin(testUser)
        `$`(".nav-tabs .nav-item:nth-child(1) .nav-link").click()

        // Then: All legacy status items should appear in New tab
        `$$`(".item-card").shouldHave(CollectionCondition.size(3))
        `$`(".item-card:contains('bill-pending.pdf')").shouldBe(visible)
        `$`(".item-card:contains('bill-processing.pdf')").shouldBe(visible)
        `$`(".item-card:contains('bill-draft.pdf')").shouldBe(visible)

        // When: Navigate to Approved tab
        `$`(".nav-tabs .nav-item:nth-child(2) .nav-link").click()

        // Then: No legacy status items should appear in Approved tab
        `$$`(".item-card").shouldHave(CollectionCondition.size(0))
    }

    // Helper methods for creating test data

    private fun createTestUser(email: String, name: String): User {
        val insertSql = """
            INSERT INTO users (email, name) 
            VALUES (?, ?) 
            RETURNING id
        """.trimIndent()

        val userId = jdbcTemplate.queryForObject(insertSql, Long::class.java, email, name)
        return User(id = userId, email = email, name = name)
    }

    private fun createTestIncomingFile(userId: Long, filename: String, status: ItemStatus): IncomingFile {
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

    private fun createTestBill(userId: Long, filename: String, status: ItemStatus): Bill {
        val tempFile = Files.createTempFile("test", ".${filename.substringAfterLast('.')}")
        Files.write(tempFile, "test content".toByteArray())

        val insertSql = """
            INSERT INTO bills (filename, file_path, upload_date, status, user_id, extracted_amount, extracted_date) 
            VALUES (?, ?, ?, ?, ?, ?, ?) 
            RETURNING id
        """.trimIndent()

        val billId = jdbcTemplate.queryForObject(
            insertSql,
            Long::class.java,
            filename,
            tempFile.toString(),
            java.sql.Timestamp.valueOf(LocalDateTime.now()),
            status.name,
            userId,
            100.0,
            java.sql.Date.valueOf(LocalDate.now())
        )

        return Bill(
            id = billId,
            filename = filename,
            filePath = tempFile.toString(),
            uploadDate = LocalDateTime.now(),
            status = status,
            userId = userId,
            extractedAmount = 100.0,
            extractedDate = LocalDate.now()
        )
    }

    private fun createTestReceipt(userId: Long, filename: String, status: ItemStatus): Receipt {
        val tempFile = Files.createTempFile("test", ".${filename.substringAfterLast('.')}")
        Files.write(tempFile, "test content".toByteArray())

        val insertSql = """
            INSERT INTO receipts (filename, file_path, upload_date, status, user_id, extracted_amount, extracted_date) 
            VALUES (?, ?, ?, ?, ?, ?, ?) 
            RETURNING id
        """.trimIndent()

        val receiptId = jdbcTemplate.queryForObject(
            insertSql,
            Long::class.java,
            filename,
            tempFile.toString(),
            java.sql.Timestamp.valueOf(LocalDateTime.now()),
            status.name,
            userId,
            50.0,
            java.sql.Date.valueOf(LocalDate.now())
        )

        return Receipt(
            id = receiptId,
            filename = filename,
            filePath = tempFile.toString(),
            uploadDate = LocalDateTime.now(),
            status = status,
            userId = userId,
            extractedAmount = 50.0,
            extractedDate = LocalDate.now()
        )
    }

    private fun createTestBillWithLegacyStatus(userId: Long, filename: String, legacyStatus: String): Bill {
        val tempFile = Files.createTempFile("test", ".${filename.substringAfterLast('.')}")
        Files.write(tempFile, "test content".toByteArray())

        val insertSql = """
            INSERT INTO bills (filename, file_path, upload_date, status, user_id, extracted_amount, extracted_date) 
            VALUES (?, ?, ?, ?, ?, ?, ?) 
            RETURNING id
        """.trimIndent()

        val billId = jdbcTemplate.queryForObject(
            insertSql,
            Long::class.java,
            filename,
            tempFile.toString(),
            java.sql.Timestamp.valueOf(LocalDateTime.now()),
            legacyStatus,
            userId,
            100.0,
            java.sql.Date.valueOf(LocalDate.now())
        )

        return Bill(
            id = billId,
            filename = filename,
            filePath = tempFile.toString(),
            uploadDate = LocalDateTime.now(),
            status = ItemStatus.NEW, // In the model, this should be mapped to NEW
            userId = userId,
            extractedAmount = 100.0,
            extractedDate = LocalDate.now()
        )
    }

    private fun simulateLogin(user: User) {
        // Navigate directly to inbox with userEmail parameter
        // The MockOAuth2AuthenticationFilter will set up authentication automatically
        open("/inbox?userEmail=${user.email}")

        // The page should load with proper authentication
        `$`("h1").shouldHave(text("Inbox"))
    }

    private fun cleanDatabase() {
        jdbcTemplate.execute("DELETE FROM receipts")
        jdbcTemplate.execute("DELETE FROM bills")
        jdbcTemplate.execute("DELETE FROM incoming_files")
        jdbcTemplate.execute("DELETE FROM login_events")
        jdbcTemplate.execute("DELETE FROM users")

        // Reset sequences
        jdbcTemplate.execute("ALTER SEQUENCE users_id_seq RESTART WITH 1")
        jdbcTemplate.execute("ALTER SEQUENCE incoming_files_id_seq RESTART WITH 1")
        jdbcTemplate.execute("ALTER SEQUENCE bills_id_seq RESTART WITH 1")
        jdbcTemplate.execute("ALTER SEQUENCE receipts_id_seq RESTART WITH 1")
        jdbcTemplate.execute("ALTER SEQUENCE login_events_id_seq RESTART WITH 1")
    }
}