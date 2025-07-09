package me.underlow.receipt.service

import me.underlow.receipt.dao.InboxEntityDao
import me.underlow.receipt.model.InboxEntity
import me.underlow.receipt.model.InboxState
import me.underlow.receipt.model.EntityType
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.mockito.kotlin.any
import org.assertj.core.api.Assertions.assertThat
import java.time.LocalDateTime

/**
 * Unit tests for InboxService.
 * Tests business logic for creating, updating, and retrieving inbox entities.
 */
class InboxServiceTest {
    
    private val inboxEntityDao: InboxEntityDao = mock()
    private val inboxService = InboxService(inboxEntityDao)
    
    @Test
    fun `createInboxEntityFromUpload should create InboxEntity with CREATED state`() {
        // Given - a valid file path is provided
        val filePath = "receipt_20240115_123456_abc12345_1234.jpg"
        val mockSavedEntity = InboxEntity(
            id = "test-id",
            uploadedImage = filePath,
            uploadDate = LocalDateTime.now(),
            ocrResults = null,
            linkedEntityId = null,
            linkedEntityType = null,
            state = InboxState.CREATED,
            failureReason = null
        )
        whenever(inboxEntityDao.save(any<InboxEntity>())).thenReturn(mockSavedEntity)
        
        // When - creating inbox entity from upload
        val result = inboxService.createInboxEntityFromUpload(filePath)
        
        // Then - entity should be created with correct properties
        assertThat(result.uploadedImage).isEqualTo(filePath)
        assertThat(result.state).isEqualTo(InboxState.CREATED)
        assertThat(result.ocrResults).isNull()
        assertThat(result.linkedEntityId).isNull()
        assertThat(result.linkedEntityType).isNull()
        assertThat(result.failureReason).isNull()
        assertThat(result.uploadDate).isNotNull()
        
        verify(inboxEntityDao).save(any<InboxEntity>())
    }
    
    @Test
    fun `createInboxEntityFromUpload should generate unique ID for each entity`() {
        // Given - multiple file paths are provided
        val filePath1 = "receipt_001.jpg"
        val filePath2 = "receipt_002.jpg"
        
        val mockEntity1 = InboxEntity(
            id = "id-1",
            uploadedImage = filePath1,
            uploadDate = LocalDateTime.now(),
            state = InboxState.CREATED
        )
        val mockEntity2 = InboxEntity(
            id = "id-2", 
            uploadedImage = filePath2,
            uploadDate = LocalDateTime.now(),
            state = InboxState.CREATED
        )
        
        whenever(inboxEntityDao.save(any<InboxEntity>())).thenReturn(mockEntity1, mockEntity2)
        
        // When - creating multiple entities
        val result1 = inboxService.createInboxEntityFromUpload(filePath1)
        val result2 = inboxService.createInboxEntityFromUpload(filePath2)
        
        // Then - each entity should have unique ID
        assertThat(result1.id).isNotEqualTo(result2.id)
        assertThat(result1.uploadedImage).isEqualTo(filePath1)
        assertThat(result2.uploadedImage).isEqualTo(filePath2)
    }
    
    @Test
    fun `createInboxEntityFromUpload should throw exception for blank file path`() {
        // Given - blank file path is provided
        val blankPath = ""
        
        // When/Then - should throw IllegalArgumentException
        val exception = assertThrows<IllegalArgumentException> {
            inboxService.createInboxEntityFromUpload(blankPath)
        }
        
        assertThat(exception.message).isEqualTo("File path cannot be blank")
    }
    
    @Test
    fun `createInboxEntityFromUpload should throw exception for whitespace-only file path`() {
        // Given - whitespace-only file path is provided
        val whitespacePath = "   "
        
        // When/Then - should throw IllegalArgumentException
        val exception = assertThrows<IllegalArgumentException> {
            inboxService.createInboxEntityFromUpload(whitespacePath)
        }
        
        assertThat(exception.message).isEqualTo("File path cannot be blank")
    }
    
    @Test
    fun `findById should return entity when found`() {
        // Given - entity exists in database
        val entityId = "test-id"
        val mockEntity = InboxEntity(
            id = entityId,
            uploadedImage = "test.jpg",
            uploadDate = LocalDateTime.now(),
            state = InboxState.CREATED
        )
        whenever(inboxEntityDao.findById(entityId)).thenReturn(mockEntity)
        
        // When - finding entity by ID
        val result = inboxService.findById(entityId)
        
        // Then - entity should be returned
        assertThat(result).isNotNull()
        assertThat(result?.id).isEqualTo(entityId)
        verify(inboxEntityDao).findById(entityId)
    }
    
    @Test
    fun `findById should return null when entity not found`() {
        // Given - entity does not exist in database
        val entityId = "non-existent-id"
        whenever(inboxEntityDao.findById(entityId)).thenReturn(null)
        
        // When - finding entity by ID
        val result = inboxService.findById(entityId)
        
        // Then - null should be returned
        assertThat(result).isNull()
        verify(inboxEntityDao).findById(entityId)
    }
    
    @Test
    fun `findAll should return paginated results with correct parameters`() {
        // Given - paginated request with custom parameters
        val page = 1
        val size = 5
        val sortBy = "uploadDate"
        val sortDirection = "ASC"
        val mockEntities = listOf(
            InboxEntity(
                id = "1",
                uploadedImage = "test1.jpg",
                uploadDate = LocalDateTime.now(),
                state = InboxState.CREATED
            ),
            InboxEntity(
                id = "2",
                uploadedImage = "test2.jpg", 
                uploadDate = LocalDateTime.now(),
                state = InboxState.PROCESSED
            )
        )
        whenever(inboxEntityDao.findAll(page, size, "upload_date", sortDirection)).thenReturn(mockEntities)
        
        // When - finding all entities with pagination
        val result = inboxService.findAll(page, size, sortBy, sortDirection)
        
        // Then - paginated results should be returned
        assertThat(result).hasSize(2)
        assertThat(result[0].id).isEqualTo("1")
        assertThat(result[1].id).isEqualTo("2")
        verify(inboxEntityDao).findAll(page, size, "upload_date", sortDirection)
    }
    
    @Test
    fun `findAll should use default parameters when not specified`() {
        // Given - no parameters specified
        val mockEntities = listOf(
            InboxEntity(
                id = "1",
                uploadedImage = "test.jpg",
                uploadDate = LocalDateTime.now(),
                state = InboxState.CREATED
            )
        )
        whenever(inboxEntityDao.findAll(0, 10, "upload_date", "DESC")).thenReturn(mockEntities)
        
        // When - finding all entities with default parameters
        val result = inboxService.findAll()
        
        // Then - default parameters should be used
        assertThat(result).hasSize(1)
        verify(inboxEntityDao).findAll(0, 10, "upload_date", "DESC")
    }
    
    @Test
    fun `findAll should throw exception for negative page number`() {
        // Given - negative page number
        val page = -1
        val size = 10
        
        // When/Then - should throw IllegalArgumentException
        val exception = assertThrows<IllegalArgumentException> {
            inboxService.findAll(page, size)
        }
        
        assertThat(exception.message).isEqualTo("Page number must be non-negative")
    }
    
    @Test
    fun `findAll should throw exception for zero page size`() {
        // Given - zero page size
        val page = 0
        val size = 0
        
        // When/Then - should throw IllegalArgumentException
        val exception = assertThrows<IllegalArgumentException> {
            inboxService.findAll(page, size)
        }
        
        assertThat(exception.message).isEqualTo("Page size must be positive")
    }
    
    @Test
    fun `findByState should return entities with specified state`() {
        // Given - entities with CREATED state exist
        val state = InboxState.CREATED
        val mockEntities = listOf(
            InboxEntity(
                id = "1",
                uploadedImage = "test1.jpg",
                uploadDate = LocalDateTime.now(),
                state = InboxState.CREATED
            ),
            InboxEntity(
                id = "2",
                uploadedImage = "test2.jpg",
                uploadDate = LocalDateTime.now(),
                state = InboxState.CREATED
            )
        )
        whenever(inboxEntityDao.findByState(state)).thenReturn(mockEntities)
        
        // When - finding entities by state
        val result = inboxService.findByState(state)
        
        // Then - entities with specified state should be returned
        assertThat(result).hasSize(2)
        assertThat(result).allMatch { it.state == InboxState.CREATED }
        verify(inboxEntityDao).findByState(state)
    }
    
    @Test
    fun `getTotalCount should return total count of entities`() {
        // Given - total count is available
        val totalCount = 25
        whenever(inboxEntityDao.getTotalCount()).thenReturn(totalCount)
        
        // When - getting total count
        val result = inboxService.getTotalCount()
        
        // Then - total count should be returned
        assertThat(result).isEqualTo(totalCount)
        verify(inboxEntityDao).getTotalCount()
    }
    
    @Test
    fun `save should delegate to DAO`() {
        // Given - entity to save
        val entity = InboxEntity(
            id = "test-id",
            uploadedImage = "test.jpg",
            uploadDate = LocalDateTime.now(),
            state = InboxState.CREATED
        )
        whenever(inboxEntityDao.save(entity)).thenReturn(entity)
        
        // When - saving entity
        val result = inboxService.save(entity)
        
        // Then - DAO save should be called
        assertThat(result).isEqualTo(entity)
        verify(inboxEntityDao).save(entity)
    }
    
    @Test
    fun `deleteById should delegate to DAO`() {
        // Given - entity ID to delete
        val entityId = "test-id"
        whenever(inboxEntityDao.deleteById(entityId)).thenReturn(true)
        
        // When - deleting entity
        val result = inboxService.deleteById(entityId)
        
        // Then - DAO delete should be called
        assertThat(result).isTrue()
        verify(inboxEntityDao).deleteById(entityId)
    }
}