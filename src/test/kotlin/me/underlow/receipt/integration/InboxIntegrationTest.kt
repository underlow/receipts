package me.underlow.receipt.integration

import me.underlow.receipt.dashboard.InboxView
import me.underlow.receipt.dashboard.BaseTable
import me.underlow.receipt.dashboard.SortDirection
import me.underlow.receipt.dashboard.PaginationConfig
import me.underlow.receipt.model.InboxEntity
import me.underlow.receipt.model.InboxState
import me.underlow.receipt.model.EntityType
import me.underlow.receipt.service.MockInboxService
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.test.assertFalse
import java.time.LocalDateTime

/**
 * Integration tests for Inbox functionality.
 * Tests the complete integration between InboxView, InboxEntity, and MockInboxService components.
 * Verifies that all components work together correctly in realistic scenarios.
 */
@ExtendWith(SpringExtension::class)
@SpringBootTest
@ActiveProfiles("test")
@Testcontainers

class InboxIntegrationTest {

    @Autowired
    private lateinit var mockInboxService: MockInboxService

    @Autowired
    private lateinit var inboxView: InboxView

    @Autowired
    private lateinit var baseTable: BaseTable
    companion object {
        @Container
        @JvmStatic
        val postgres = PostgreSQLContainer<Nothing>("postgres:15-alpine").apply {
            withDatabaseName("testdb")
            withUsername("test")
            withPassword("test")
        }

        @JvmStatic
        @org.springframework.test.context.DynamicPropertySource
        fun configureProperties(registry: org.springframework.test.context.DynamicPropertyRegistry) {
            registry.add("spring.datasource.url", postgres::getJdbcUrl)
            registry.add("spring.datasource.username", postgres::getUsername)
            registry.add("spring.datasource.password", postgres::getPassword)
        }
    }
    @Test
    fun `given mock inbox service when fetching all data then should return complete inbox items`() {
        // given - mock inbox service with test data
        // when - fetching all inbox items
        val allItems = mockInboxService.findAll()

        // then - should return complete inbox items in all states
        assertNotNull(allItems)
        assertTrue(allItems.isNotEmpty())

        // Verify all states are represented
        assertTrue(allItems.any { it.state == InboxState.CREATED })
        assertTrue(allItems.any { it.state == InboxState.PROCESSED })
        assertTrue(allItems.any { it.state == InboxState.FAILED })
        assertTrue(allItems.any { it.state == InboxState.APPROVED })
    }

    @Test
    fun `given mock inbox service when fetching paginated data then should support pagination correctly`() {
        // given - mock inbox service with test data
        val pageSize = 5
        val totalCount = mockInboxService.getTotalCount()
        val expectedPages = (totalCount + pageSize - 1) / pageSize

        // when - fetching first page
        val firstPage = mockInboxService.findAll(0, pageSize)

        // then - should return correct page size and data
        assertNotNull(firstPage)
        assertTrue(firstPage.size <= pageSize)
        assertTrue(firstPage.isNotEmpty())

        // when - fetching second page if it exists
        if (expectedPages > 1) {
            val secondPage = mockInboxService.findAll(1, pageSize)

            // then - should return different data
            assertNotNull(secondPage)
            assertTrue(secondPage.isNotEmpty())

            // Verify pages contain different items
            val firstPageIds = firstPage.map { it.id }.toSet()
            val secondPageIds = secondPage.map { it.id }.toSet()
            assertTrue(firstPageIds.intersect(secondPageIds).isEmpty())
        }
    }

    @Test
    fun `given mock inbox service when fetching sorted data then should support sorting correctly`() {
        // given - mock inbox service with test data
        // when - fetching data sorted by upload date ascending
        val sortedAsc = mockInboxService.findAll(0, 20, "uploadDate", "ASC")

        // then - should return data sorted by upload date in ascending order
        assertNotNull(sortedAsc)
        assertTrue(sortedAsc.isNotEmpty())

        for (i in 0 until sortedAsc.size - 1) {
            assertTrue(
                sortedAsc[i].uploadDate <= sortedAsc[i + 1].uploadDate,
                "Items should be sorted by upload date ascending"
            )
        }

        // when - fetching data sorted by upload date descending
        val sortedDesc = mockInboxService.findAll(0, 20, "uploadDate", "DESC")

        // then - should return data sorted by upload date in descending order
        assertNotNull(sortedDesc)
        assertTrue(sortedDesc.isNotEmpty())

        for (i in 0 until sortedDesc.size - 1) {
            assertTrue(
                sortedDesc[i].uploadDate >= sortedDesc[i + 1].uploadDate,
                "Items should be sorted by upload date descending"
            )
        }
    }

    @Test
    fun `given inbox view when rendering with mock service data then should display all inbox items correctly`() {
        // given - inbox view and mock service with test data
        val inboxData = mockInboxService.findAll()

        // when - rendering inbox view with mock data
        val renderedHtml = inboxView.render(inboxData)

        // then - should render complete table with all items
        assertNotNull(renderedHtml)
        assertTrue(renderedHtml.isNotEmpty())

        // Verify table structure is present
        assertTrue(renderedHtml.contains("table"))
        assertTrue(renderedHtml.contains("Upload Date"))
        assertTrue(renderedHtml.contains("Image"))
        assertTrue(renderedHtml.contains("OCR Status"))
        assertTrue(renderedHtml.contains("Actions"))
    }

    @Test
    fun `given inbox view when rendering with different states then should show appropriate actions for each state`() {
        // given - inbox view and mock service data with different states
        val inboxData = mockInboxService.findAll()

        // when - converting each entity to row data
        val createdItems = inboxData.filter { it.state == InboxState.CREATED }
        val processedItems = inboxData.filter { it.state == InboxState.PROCESSED }
        val failedItems = inboxData.filter { it.state == InboxState.FAILED }
        val approvedItems = inboxData.filter { it.state == InboxState.APPROVED }

        // then - should show appropriate actions for each state
        createdItems.forEach { item ->
            val rowData = inboxView.convertEntityToRowData(item)
            assertTrue(rowData["actions"]?.contains("Processing") == true)
        }

        processedItems.forEach { item ->
            val rowData = inboxView.convertEntityToRowData(item)
            assertTrue(rowData["actions"]?.contains("Approve") == true)
        }

        failedItems.forEach { item ->
            val rowData = inboxView.convertEntityToRowData(item)
            assertTrue(rowData["actions"]?.contains("Retry") == true)
        }

        approvedItems.forEach { item ->
            val rowData = inboxView.convertEntityToRowData(item)
            assertTrue(rowData["actions"]?.contains("Complete") == true)
        }
    }

    @Test
    fun `given inbox view when rendering with pagination then should integrate with base table pagination`() {
        // given - inbox view and mock service with paginated data
        val pageSize = 5
        val currentPage = 1
        val paginationConfig = PaginationConfig(
            pageSize = pageSize,
            currentPage = currentPage,
            totalItems = mockInboxService.getTotalCount()
        )
        val inboxData = mockInboxService.findAll(currentPage - 1, pageSize)

        // when - rendering with pagination
        val renderedHtml = inboxView.render(
            inboxData = inboxData,
            paginationConfig = paginationConfig
        )

        // then - should render table with pagination controls
        assertNotNull(renderedHtml)
        assertTrue(renderedHtml.isNotEmpty())

        // Verify pagination integration
        assertTrue(renderedHtml.contains("pagination") || renderedHtml.contains("page"))
    }

    @Test
    fun `given inbox view when rendering with sorting then should integrate with base table sorting`() {
        // given - inbox view and mock service with sorted data
        val sortKey = "uploadDate"
        val sortDirection = SortDirection.DESC
        val inboxData = mockInboxService.findAll(0, 20, sortKey, sortDirection.toString())

        // when - rendering with sorting
        val renderedHtml = inboxView.render(
            inboxData = inboxData,
            sortKey = sortKey,
            sortDirection = sortDirection
        )

        // then - should render table with sorting indicators
        assertNotNull(renderedHtml)
        assertTrue(renderedHtml.isNotEmpty())

        // Verify sorting integration
        assertTrue(renderedHtml.contains("sort") || renderedHtml.contains("sortable"))
    }

    @Test
    fun `given inbox entity when processing through different states then should maintain data integrity`() {
        // given - inbox entity in initial state
        val originalEntity = InboxEntity(
            id = "test-entity-1",
            uploadedImage = "test-image.jpg",
            uploadDate = LocalDateTime.now(),
            state = InboxState.CREATED
        )

        // when - processing through OCR workflow
        val processedEntity = originalEntity.processOCR("Sample OCR text")

        // then - should transition to processed state correctly
        assertEquals(InboxState.PROCESSED, processedEntity.state)
        assertEquals("Sample OCR text", processedEntity.ocrResults)
        assertTrue(processedEntity.canApprove())
        assertFalse(processedEntity.canRetry())

        // when - approving the processed entity
        val approvedEntity = processedEntity.approve("bill-123", EntityType.BILL)

        // then - should transition to approved state correctly
        assertEquals(InboxState.APPROVED, approvedEntity.state)
        assertEquals("bill-123", approvedEntity.linkedEntityId)
        assertEquals(EntityType.BILL, approvedEntity.linkedEntityType)
        assertFalse(approvedEntity.canApprove())
        assertFalse(approvedEntity.canRetry())
    }

    @Test
    fun `given inbox entity when OCR fails then should support retry workflow`() {
        // given - inbox entity in initial state
        val originalEntity = InboxEntity(
            id = "test-entity-2",
            uploadedImage = "test-image.jpg",
            uploadDate = LocalDateTime.now(),
            state = InboxState.CREATED
        )

        // when - OCR processing fails
        val failedEntity = originalEntity.failOCR("Image too blurry")

        // then - should transition to failed state correctly
        assertEquals(InboxState.FAILED, failedEntity.state)
        assertEquals("Image too blurry", failedEntity.failureReason)
        assertFalse(failedEntity.canApprove())
        assertTrue(failedEntity.canRetry())

        // when - retrying OCR processing
        val retriedEntity = failedEntity.retryOCR()

        // then - should transition back to created state
        assertEquals(InboxState.CREATED, retriedEntity.state)
        assertEquals(null, retriedEntity.failureReason)
        assertEquals(null, retriedEntity.ocrResults)
    }

    @Test
    fun `given complete inbox workflow when processing multiple items then should handle batch operations correctly`() {
        // given - multiple inbox items in different states
        val inboxData = mockInboxService.findAll()

        // when - applying view rendering to all items
        val allRowData = inboxData.map { inboxView.convertEntityToRowData(it) }

        // then - should handle all items correctly
        assertNotNull(allRowData)
        assertEquals(inboxData.size, allRowData.size)

        // Verify each row has all required fields
        allRowData.forEach { rowData ->
            assertTrue(rowData.containsKey("uploadDate"))
            assertTrue(rowData.containsKey("image"))
            assertTrue(rowData.containsKey("status"))
            assertTrue(rowData.containsKey("actions"))

            // Verify all field values are non-empty
            assertTrue(rowData["uploadDate"]?.isNotEmpty() == true)
            assertTrue(rowData["image"]?.isNotEmpty() == true)
            assertTrue(rowData["status"]?.isNotEmpty() == true)
            assertTrue(rowData["actions"]?.isNotEmpty() == true)
        }
    }

    @Test
    fun `given inbox view when handling empty data then should render empty state correctly`() {
        // given - inbox view with empty data
        val emptyData = emptyList<InboxEntity>()

        // when - rendering empty inbox
        val renderedHtml = inboxView.render(emptyData)

        // then - should render empty state without errors
        assertNotNull(renderedHtml)
        assertTrue(renderedHtml.isNotEmpty())

        // Should still contain table headers
        assertTrue(renderedHtml.contains("Upload Date"))
        assertTrue(renderedHtml.contains("Image"))
        assertTrue(renderedHtml.contains("OCR Status"))
        assertTrue(renderedHtml.contains("Actions"))
    }

    @Test
    fun `given inbox view when applying sorting and filtering then should integrate with base table functionality`() {
        // given - inbox view with test data
        val inboxData = mockInboxService.findAll()

        // when - applying sorting
        val sortedData = inboxView.applySorting(inboxData, "uploadDate", SortDirection.ASC)

        // then - should return sorted data
        assertNotNull(sortedData)
        assertTrue(sortedData.isNotEmpty())

        // when - applying pagination
        val paginatedData = inboxView.applyPagination(inboxData, 5, 1)

        // then - should return paginated data
        assertNotNull(paginatedData)
        assertTrue(paginatedData.size <= 5)

        // when - applying search
        val searchedData = inboxView.applySearch(inboxData, "grocery")

        // then - should return filtered data
        assertNotNull(searchedData)
        // Search functionality should work with the base table
    }
}
