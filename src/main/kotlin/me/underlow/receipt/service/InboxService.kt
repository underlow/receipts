package me.underlow.receipt.service

import me.underlow.receipt.dao.InboxEntityDao
import me.underlow.receipt.model.InboxEntity
import me.underlow.receipt.model.InboxState
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.UUID

/**
 * Service for managing inbox entities in the document processing system.
 * Provides business logic for creating, updating, and retrieving inbox items.
 */
@Service
@Transactional
class InboxService(
    private val inboxEntityDao: InboxEntityDao
) {
    
    /**
     * Creates InboxEntity from uploaded file path for OCR processing workflow.
     * This method is called after successful file upload to create a database record
     * that tracks the uploaded file through the OCR processing lifecycle.
     * 
     * @param filePath relative path to the uploaded file
     * @return created InboxEntity with CREATED state
     */
    fun createInboxEntityFromUpload(filePath: String): InboxEntity {
        require(filePath.isNotBlank()) { "File path cannot be blank" }
        
        val inboxEntity = InboxEntity(
            id = UUID.randomUUID().toString(),
            uploadedImage = filePath,
            uploadDate = LocalDateTime.now(),
            ocrResults = null,
            linkedEntityId = null,
            linkedEntityType = null,
            state = InboxState.CREATED,
            failureReason = null
        )
        
        return inboxEntityDao.save(inboxEntity)
    }
    
    /**
     * Find inbox entity by ID.
     * @param id InboxEntity ID
     * @return InboxEntity if found, null otherwise
     */
    fun findById(id: String): InboxEntity? {
        return inboxEntityDao.findById(id)
    }
    
    /**
     * Find all inbox entities with pagination and sorting.
     * @param page page number (0-based)
     * @param size page size  
     * @param sortBy field to sort by (default: "uploadDate")
     * @param sortDirection sort direction (default: "DESC")
     * @return list of inbox entities
     */
    fun findAll(page: Int = 0, size: Int = 10, sortBy: String = "uploadDate", sortDirection: String = "DESC"): List<InboxEntity> {
        require(page >= 0) { "Page number must be non-negative" }
        require(size > 0) { "Page size must be positive" }
        
        // Map entity field names to database column names
        val dbSortBy = when (sortBy) {
            "uploadDate" -> "upload_date"
            "state" -> "state"
            "id" -> "id"
            else -> "upload_date"
        }
        
        return inboxEntityDao.findAll(page, size, dbSortBy, sortDirection)
    }
    
    /**
     * Get total count of inbox entities.
     * @return total number of entities
     */
    fun getTotalCount(): Int {
        return inboxEntityDao.getTotalCount()
    }
    
    /**
     * Find inbox entities by state.
     * @param state InboxState to filter by
     * @return list of inbox entities with specified state
     */
    fun findByState(state: InboxState): List<InboxEntity> {
        return inboxEntityDao.findByState(state)
    }
    
    /**
     * Save inbox entity to database.
     * @param entity InboxEntity to save
     * @return saved InboxEntity
     */
    fun save(entity: InboxEntity): InboxEntity {
        return inboxEntityDao.save(entity)
    }
    
    /**
     * Delete inbox entity by ID.
     * @param id InboxEntity ID
     * @return true if entity was deleted, false if not found
     */
    fun deleteById(id: String): Boolean {
        return inboxEntityDao.deleteById(id)
    }
}