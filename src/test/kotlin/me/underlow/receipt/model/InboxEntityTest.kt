package me.underlow.receipt.model

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.test.assertFalse
import kotlin.test.assertNull
import java.time.LocalDateTime

/**
 * Unit tests for InboxEntity class.
 * Tests entity creation, state transitions, and validation methods.
 */
@ExtendWith(MockitoExtension::class)
class InboxEntityTest {

    @Test
    fun `given new inbox entity when created then should have correct initial state`() {
        // given - parameters for new inbox entity
        val id = "inbox-123"
        val uploadedImage = "/path/to/image.jpg"
        val uploadDate = LocalDateTime.now()
        
        // when - creating new inbox entity
        val entity = InboxEntity(
            id = id,
            uploadedImage = uploadedImage,
            uploadDate = uploadDate
        )
        
        // then - should have correct initial state
        assertNotNull(entity)
        assertEquals(id, entity.id)
        assertEquals(uploadedImage, entity.uploadedImage)
        assertEquals(uploadDate, entity.uploadDate)
        assertEquals(InboxState.CREATED, entity.state)
        assertNull(entity.ocrResults)
        assertNull(entity.linkedEntityId)
        assertNull(entity.linkedEntityType)
        assertNull(entity.failureReason)
    }

    @Test
    fun `given inbox entity in CREATED state when processOCR called then should transition to PROCESSED`() {
        // given - inbox entity in CREATED state
        val entity = InboxEntity(
            id = "inbox-123",
            uploadedImage = "/path/to/image.jpg",
            uploadDate = LocalDateTime.now()
        )
        val ocrResults = "Invoice #12345 Amount: $100.00"
        
        // when - processing OCR
        val processedEntity = entity.processOCR(ocrResults)
        
        // then - should transition to PROCESSED state
        assertNotNull(processedEntity)
        assertEquals(InboxState.PROCESSED, processedEntity.state)
        assertEquals(ocrResults, processedEntity.ocrResults)
        assertNull(processedEntity.failureReason)
    }

    @Test
    fun `given inbox entity in CREATED state when failOCR called then should transition to FAILED`() {
        // given - inbox entity in CREATED state
        val entity = InboxEntity(
            id = "inbox-123",
            uploadedImage = "/path/to/image.jpg",
            uploadDate = LocalDateTime.now()
        )
        val failureReason = "OCR service unavailable"
        
        // when - failing OCR
        val failedEntity = entity.failOCR(failureReason)
        
        // then - should transition to FAILED state
        assertNotNull(failedEntity)
        assertEquals(InboxState.FAILED, failedEntity.state)
        assertEquals(failureReason, failedEntity.failureReason)
        assertNull(failedEntity.ocrResults)
    }

    @Test
    fun `given inbox entity in PROCESSED state when approve called then should transition to APPROVED`() {
        // given - inbox entity in PROCESSED state
        val entity = InboxEntity(
            id = "inbox-123",
            uploadedImage = "/path/to/image.jpg",
            uploadDate = LocalDateTime.now(),
            state = InboxState.PROCESSED,
            ocrResults = "Invoice #12345 Amount: $100.00"
        )
        val linkedEntityId = "bill-456"
        val linkedEntityType = EntityType.BILL
        
        // when - approving entity
        val approvedEntity = entity.approve(linkedEntityId, linkedEntityType)
        
        // then - should transition to APPROVED state
        assertNotNull(approvedEntity)
        assertEquals(InboxState.APPROVED, approvedEntity.state)
        assertEquals(linkedEntityId, approvedEntity.linkedEntityId)
        assertEquals(linkedEntityType, approvedEntity.linkedEntityType)
    }

    @Test
    fun `given inbox entity in FAILED state when retryOCR called then should transition to CREATED`() {
        // given - inbox entity in FAILED state
        val entity = InboxEntity(
            id = "inbox-123",
            uploadedImage = "/path/to/image.jpg",
            uploadDate = LocalDateTime.now(),
            state = InboxState.FAILED,
            failureReason = "OCR service unavailable"
        )
        
        // when - retrying OCR
        val retriedEntity = entity.retryOCR()
        
        // then - should transition to CREATED state
        assertNotNull(retriedEntity)
        assertEquals(InboxState.CREATED, retriedEntity.state)
        assertNull(retriedEntity.failureReason)
        assertNull(retriedEntity.ocrResults)
    }

    @Test
    fun `given inbox entity in PROCESSED state when canApprove called then should return true`() {
        // given - inbox entity in PROCESSED state
        val entity = InboxEntity(
            id = "inbox-123",
            uploadedImage = "/path/to/image.jpg",
            uploadDate = LocalDateTime.now(),
            state = InboxState.PROCESSED,
            ocrResults = "Invoice #12345 Amount: $100.00"
        )
        
        // when - checking if can approve
        val canApprove = entity.canApprove()
        
        // then - should return true
        assertTrue(canApprove)
    }

    @Test
    fun `given inbox entity in CREATED state when canApprove called then should return false`() {
        // given - inbox entity in CREATED state
        val entity = InboxEntity(
            id = "inbox-123",
            uploadedImage = "/path/to/image.jpg",
            uploadDate = LocalDateTime.now()
        )
        
        // when - checking if can approve
        val canApprove = entity.canApprove()
        
        // then - should return false
        assertFalse(canApprove)
    }

    @Test
    fun `given inbox entity in APPROVED state when canApprove called then should return false`() {
        // given - inbox entity in APPROVED state
        val entity = InboxEntity(
            id = "inbox-123",
            uploadedImage = "/path/to/image.jpg",
            uploadDate = LocalDateTime.now(),
            state = InboxState.APPROVED,
            ocrResults = "Invoice #12345 Amount: $100.00",
            linkedEntityId = "bill-456",
            linkedEntityType = EntityType.BILL
        )
        
        // when - checking if can approve
        val canApprove = entity.canApprove()
        
        // then - should return false
        assertFalse(canApprove)
    }

    @Test
    fun `given inbox entity in FAILED state when canRetry called then should return true`() {
        // given - inbox entity in FAILED state
        val entity = InboxEntity(
            id = "inbox-123",
            uploadedImage = "/path/to/image.jpg",
            uploadDate = LocalDateTime.now(),
            state = InboxState.FAILED,
            failureReason = "OCR service unavailable"
        )
        
        // when - checking if can retry
        val canRetry = entity.canRetry()
        
        // then - should return true
        assertTrue(canRetry)
    }

    @Test
    fun `given inbox entity in PROCESSED state when canRetry called then should return false`() {
        // given - inbox entity in PROCESSED state
        val entity = InboxEntity(
            id = "inbox-123",
            uploadedImage = "/path/to/image.jpg",
            uploadDate = LocalDateTime.now(),
            state = InboxState.PROCESSED,
            ocrResults = "Invoice #12345 Amount: $100.00"
        )
        
        // when - checking if can retry
        val canRetry = entity.canRetry()
        
        // then - should return false
        assertFalse(canRetry)
    }

    @Test
    fun `given inbox entity in APPROVED state when canRetry called then should return false`() {
        // given - inbox entity in APPROVED state
        val entity = InboxEntity(
            id = "inbox-123",
            uploadedImage = "/path/to/image.jpg",
            uploadDate = LocalDateTime.now(),
            state = InboxState.APPROVED,
            ocrResults = "Invoice #12345 Amount: $100.00",
            linkedEntityId = "bill-456",
            linkedEntityType = EntityType.BILL
        )
        
        // when - checking if can retry
        val canRetry = entity.canRetry()
        
        // then - should return false
        assertFalse(canRetry)
    }

    @Test
    fun `given inbox entity when processOCR called from invalid state then should throw exception`() {
        // given - inbox entity in PROCESSED state (invalid for OCR processing)
        val entity = InboxEntity(
            id = "inbox-123",
            uploadedImage = "/path/to/image.jpg",
            uploadDate = LocalDateTime.now(),
            state = InboxState.PROCESSED,
            ocrResults = "Already processed"
        )
        
        // when/then - should throw exception
        try {
            entity.processOCR("New OCR results")
            assert(false) { "Should have thrown exception" }
        } catch (e: IllegalStateException) {
            assertEquals("Cannot process OCR from state PROCESSED", e.message)
        }
    }

    @Test
    fun `given inbox entity when approve called from invalid state then should throw exception`() {
        // given - inbox entity in CREATED state (invalid for approval)
        val entity = InboxEntity(
            id = "inbox-123",
            uploadedImage = "/path/to/image.jpg",
            uploadDate = LocalDateTime.now()
        )
        
        // when/then - should throw exception
        try {
            entity.approve("bill-456", EntityType.BILL)
            assert(false) { "Should have thrown exception" }
        } catch (e: IllegalStateException) {
            assertEquals("Cannot approve from state CREATED", e.message)
        }
    }

    @Test
    fun `given inbox entity when retryOCR called from invalid state then should throw exception`() {
        // given - inbox entity in APPROVED state (invalid for retry)
        val entity = InboxEntity(
            id = "inbox-123",
            uploadedImage = "/path/to/image.jpg",
            uploadDate = LocalDateTime.now(),
            state = InboxState.APPROVED,
            ocrResults = "Invoice #12345 Amount: $100.00",
            linkedEntityId = "bill-456",
            linkedEntityType = EntityType.BILL
        )
        
        // when/then - should throw exception
        try {
            entity.retryOCR()
            assert(false) { "Should have thrown exception" }
        } catch (e: IllegalStateException) {
            assertEquals("Cannot retry OCR from state APPROVED", e.message)
        }
    }

    @Test
    fun `given inbox entity when created with all parameters then should set all fields correctly`() {
        // given - all parameters for inbox entity
        val id = "inbox-123"
        val uploadedImage = "/path/to/image.jpg"
        val uploadDate = LocalDateTime.now()
        val ocrResults = "Invoice #12345 Amount: $100.00"
        val linkedEntityId = "bill-456"
        val linkedEntityType = EntityType.RECEIPT
        val state = InboxState.APPROVED
        val failureReason = null
        
        // when - creating entity with all parameters
        val entity = InboxEntity(
            id = id,
            uploadedImage = uploadedImage,
            uploadDate = uploadDate,
            ocrResults = ocrResults,
            linkedEntityId = linkedEntityId,
            linkedEntityType = linkedEntityType,
            state = state,
            failureReason = failureReason
        )
        
        // then - should set all fields correctly
        assertNotNull(entity)
        assertEquals(id, entity.id)
        assertEquals(uploadedImage, entity.uploadedImage)
        assertEquals(uploadDate, entity.uploadDate)
        assertEquals(ocrResults, entity.ocrResults)
        assertEquals(linkedEntityId, entity.linkedEntityId)
        assertEquals(linkedEntityType, entity.linkedEntityType)
        assertEquals(state, entity.state)
        assertEquals(failureReason, entity.failureReason)
    }

    @Test
    fun `given inbox entity when equals and hashCode called then should work correctly`() {
        // given - two identical inbox entities
        val uploadDate = LocalDateTime.now()
        val entity1 = InboxEntity(
            id = "inbox-123",
            uploadedImage = "/path/to/image.jpg",
            uploadDate = uploadDate
        )
        val entity2 = InboxEntity(
            id = "inbox-123",
            uploadedImage = "/path/to/image.jpg",
            uploadDate = uploadDate
        )
        val entity3 = InboxEntity(
            id = "inbox-456",
            uploadedImage = "/path/to/image.jpg",
            uploadDate = uploadDate
        )
        
        // when/then - should work correctly
        assertEquals(entity1, entity2)
        assertEquals(entity1.hashCode(), entity2.hashCode())
        assertFalse(entity1 == entity3)
        assertFalse(entity1.hashCode() == entity3.hashCode())
    }
}