package me.underlow.receipt.dashboard

import me.underlow.receipt.model.InboxEntity
import me.underlow.receipt.model.InboxState
import me.underlow.receipt.model.EntityType
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.Mockito.lenient
import org.mockito.kotlin.any
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.test.assertFalse
import java.time.LocalDateTime

/**
 * Unit tests for InboxView component.
 * Tests inbox-specific table rendering, column definitions, OCR status display,
 * action buttons, and integration with BaseTable functionality.
 */
@ExtendWith(MockitoExtension::class)
class InboxViewTest {

    @Mock
    private lateinit var baseTable: BaseTable

    @Test
    fun `given inbox view when get table columns then should return correct inbox columns`() {
        // given - inbox view component
        val inboxView = InboxView(baseTable)
        
        // when - getting table columns
        val columns = inboxView.getTableColumns()
        
        // then - should return correct inbox columns
        assertNotNull(columns)
        assertEquals(4, columns.size)
        assertTrue(columns.any { it.key == "uploadDate" && it.label == "Upload Date" })
        assertTrue(columns.any { it.key == "image" && it.label == "Image" })
        assertTrue(columns.any { it.key == "status" && it.label == "OCR Status" })
        assertTrue(columns.any { it.key == "actions" && it.label == "Actions" })
    }

    @Test
    fun `given inbox view when converting entity to row data then should include all required fields`() {
        // given - inbox view component and inbox entity
        val inboxView = InboxView(baseTable)
        val entity = InboxEntity(
            id = "1",
            uploadedImage = "image1.jpg",
            uploadDate = LocalDateTime.of(2025, 1, 1, 12, 0),
            state = InboxState.PROCESSED,
            ocrResults = "Sample OCR text"
        )
        
        // when - converting entity to row data
        val rowData = inboxView.convertEntityToRowData(entity)
        
        // then - should include all required fields
        assertNotNull(rowData)
        assertTrue(rowData.containsKey("uploadDate"))
        assertTrue(rowData.containsKey("image"))
        assertTrue(rowData.containsKey("status"))
        assertTrue(rowData.containsKey("actions"))
    }

    @Test
    fun `given inbox view when formatting OCR status then should return appropriate status text`() {
        // given - inbox view component
        val inboxView = InboxView(baseTable)
        
        // when - formatting OCR status for different states
        val createdStatus = inboxView.formatOCRStatus(InboxState.CREATED)
        val processedStatus = inboxView.formatOCRStatus(InboxState.PROCESSED)
        val failedStatus = inboxView.formatOCRStatus(InboxState.FAILED)
        val approvedStatus = inboxView.formatOCRStatus(InboxState.APPROVED)
        
        // then - should return appropriate status text
        assertNotNull(createdStatus)
        assertNotNull(processedStatus)
        assertNotNull(failedStatus)
        assertNotNull(approvedStatus)
        assertTrue(createdStatus.contains("Pending"))
        assertTrue(processedStatus.contains("Processed"))
        assertTrue(failedStatus.contains("Failed"))
        assertTrue(approvedStatus.contains("Approved"))
    }

    @Test
    fun `given inbox view when formatting actions then should return appropriate action buttons`() {
        // given - inbox view component
        val inboxView = InboxView(baseTable)
        
        // when - formatting actions for different states
        val createdActions = inboxView.formatActions(InboxState.CREATED, "1")
        val processedActions = inboxView.formatActions(InboxState.PROCESSED, "2")
        val failedActions = inboxView.formatActions(InboxState.FAILED, "3")
        val approvedActions = inboxView.formatActions(InboxState.APPROVED, "4")
        
        // then - should return appropriate action buttons
        assertNotNull(createdActions)
        assertNotNull(processedActions)
        assertNotNull(failedActions)
        assertNotNull(approvedActions)
        assertTrue(createdActions.contains("Processing"))
        assertTrue(processedActions.contains("Approve"))
        assertTrue(failedActions.contains("Retry"))
        assertTrue(approvedActions.contains("Complete"))
    }

    @Test
    fun `given inbox view when converting processed entity then should show approve buttons`() {
        // given - inbox view component with processed entity
        val inboxView = InboxView(baseTable)
        val processedEntity = InboxEntity(
            id = "1",
            uploadedImage = "image1.jpg",
            uploadDate = LocalDateTime.of(2025, 1, 1, 12, 0),
            state = InboxState.PROCESSED,
            ocrResults = "Sample OCR text"
        )
        
        // when - converting processed entity to row data
        val rowData = inboxView.convertEntityToRowData(processedEntity)
        
        // then - should show approve buttons
        assertNotNull(rowData)
        assertTrue(rowData["actions"]?.contains("Approve") == true)
    }

    @Test
    fun `given inbox view when converting failed entity then should show retry buttons`() {
        // given - inbox view component with failed entity
        val inboxView = InboxView(baseTable)
        val failedEntity = InboxEntity(
            id = "1",
            uploadedImage = "image1.jpg",
            uploadDate = LocalDateTime.of(2025, 1, 1, 12, 0),
            state = InboxState.FAILED,
            failureReason = "OCR processing failed"
        )
        
        // when - converting failed entity to row data
        val rowData = inboxView.convertEntityToRowData(failedEntity)
        
        // then - should show retry buttons
        assertNotNull(rowData)
        assertTrue(rowData["actions"]?.contains("Retry") == true)
    }

    @Test
    fun `given inbox view when converting approved entity then should show completion status`() {
        // given - inbox view component with approved entity
        val inboxView = InboxView(baseTable)
        val approvedEntity = InboxEntity(
            id = "1",
            uploadedImage = "image1.jpg",
            uploadDate = LocalDateTime.of(2025, 1, 1, 12, 0),
            state = InboxState.APPROVED,
            ocrResults = "Sample OCR text",
            linkedEntityId = "bill123",
            linkedEntityType = EntityType.BILL
        )
        
        // when - converting approved entity to row data
        val rowData = inboxView.convertEntityToRowData(approvedEntity)
        
        // then - should show completion status
        assertNotNull(rowData)
        assertTrue(rowData["status"]?.contains("Approved") == true)
        assertTrue(rowData["actions"]?.contains("Complete") == true)
    }

    @Test
    fun `given inbox view when converting different states then should show appropriate OCR status indicators`() {
        // given - inbox view component with entities in different states
        val inboxView = InboxView(baseTable)
        val createdEntity = InboxEntity(
            id = "1",
            uploadedImage = "image1.jpg",
            uploadDate = LocalDateTime.of(2025, 1, 1, 12, 0),
            state = InboxState.CREATED
        )
        val processedEntity = InboxEntity(
            id = "2",
            uploadedImage = "image2.jpg",
            uploadDate = LocalDateTime.of(2025, 1, 2, 12, 0),
            state = InboxState.PROCESSED,
            ocrResults = "Sample text"
        )
        val failedEntity = InboxEntity(
            id = "3",
            uploadedImage = "image3.jpg",
            uploadDate = LocalDateTime.of(2025, 1, 3, 12, 0),
            state = InboxState.FAILED,
            failureReason = "Error"
        )
        
        // when - converting entities to row data
        val createdRowData = inboxView.convertEntityToRowData(createdEntity)
        val processedRowData = inboxView.convertEntityToRowData(processedEntity)
        val failedRowData = inboxView.convertEntityToRowData(failedEntity)
        
        // then - should show appropriate OCR status indicators
        assertNotNull(createdRowData)
        assertNotNull(processedRowData)
        assertNotNull(failedRowData)
        assertTrue(createdRowData["status"]?.contains("Pending") == true)
        assertTrue(processedRowData["status"]?.contains("Processed") == true)
        assertTrue(failedRowData["status"]?.contains("Failed") == true)
    }


    @Test
    fun `given inbox view when applying sorting then should use base table sorting`() {
        // given - inbox view component with mock BaseTable
        val inboxView = InboxView(baseTable)
        val inboxData = listOf(
            InboxEntity(
                id = "1",
                uploadedImage = "image1.jpg",
                uploadDate = LocalDateTime.of(2025, 1, 1, 12, 0),
                state = InboxState.PROCESSED,
                ocrResults = "Sample text"
            )
        )
        
        // Mock BaseTable behavior
        `when`(baseTable.applySorting(any(), any(), any())).thenReturn(
            listOf(mapOf("key" to "sorted_value"))
        )
        
        // when - applying sorting
        val sortedData = inboxView.applySorting(inboxData, "uploadDate", SortDirection.ASC)
        
        // then - should use base table sorting functionality
        assertNotNull(sortedData)
        assertTrue(sortedData.isNotEmpty())
    }

    @Test
    fun `given inbox view when applying pagination then should use base table pagination`() {
        // given - inbox view component with mock BaseTable
        val inboxView = InboxView(baseTable)
        val inboxData = listOf(
            InboxEntity(
                id = "1",
                uploadedImage = "image1.jpg",
                uploadDate = LocalDateTime.of(2025, 1, 1, 12, 0),
                state = InboxState.PROCESSED,
                ocrResults = "Sample text"
            )
        )
        
        // Mock BaseTable behavior
        `when`(baseTable.applyPagination(any(), any(), any())).thenReturn(
            listOf(mapOf("key" to "paginated_value"))
        )
        
        // when - applying pagination
        val paginatedData = inboxView.applyPagination(inboxData, 10, 1)
        
        // then - should use base table pagination functionality
        assertNotNull(paginatedData)
        assertTrue(paginatedData.isNotEmpty())
    }

    @Test
    fun `given inbox view when applying search then should use base table search`() {
        // given - inbox view component with mock BaseTable
        val inboxView = InboxView(baseTable)
        val inboxData = listOf(
            InboxEntity(
                id = "1",
                uploadedImage = "image1.jpg",
                uploadDate = LocalDateTime.of(2025, 1, 1, 12, 0),
                state = InboxState.PROCESSED,
                ocrResults = "Sample text"
            )
        )
        
        // Mock BaseTable behavior
        `when`(baseTable.applySearch(any(), any())).thenReturn(
            listOf(mapOf("key" to "filtered_value"))
        )
        
        // when - applying search
        val filteredData = inboxView.applySearch(inboxData, "search_term")
        
        // then - should use base table search functionality
        assertNotNull(filteredData)
        assertTrue(filteredData.isNotEmpty())
    }
}