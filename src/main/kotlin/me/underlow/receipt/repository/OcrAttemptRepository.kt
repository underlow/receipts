package me.underlow.receipt.repository

import me.underlow.receipt.model.EntityType
import me.underlow.receipt.model.OcrAttempt
import me.underlow.receipt.model.OcrProcessingStatus

/**
 * Repository interface for OCR attempt operations
 */
interface OcrAttemptRepository {
    
    /**
     * Saves an OCR attempt record
     */
    fun save(ocrAttempt: OcrAttempt): OcrAttempt
    
    /**
     * Finds an OCR attempt by ID
     */
    fun findById(id: Long): OcrAttempt?
    
    /**
     * Finds all OCR attempts for a specific entity
     */
    fun findByEntityTypeAndEntityId(entityType: EntityType, entityId: Long): List<OcrAttempt>
    
    /**
     * Finds all OCR attempts for a user
     */
    fun findByUserId(userId: Long): List<OcrAttempt>
    
    /**
     * Finds OCR attempts by status
     */
    fun findByProcessingStatus(status: OcrProcessingStatus): List<OcrAttempt>
    
    /**
     * Finds the most recent OCR attempt for an entity
     */
    fun findLatestByEntityTypeAndEntityId(entityType: EntityType, entityId: Long): OcrAttempt?
    
    /**
     * Deletes an OCR attempt by ID
     */
    fun delete(id: Long): Boolean
    
    /**
     * Deletes all OCR attempts for a specific entity
     */
    fun deleteByEntityTypeAndEntityId(entityType: EntityType, entityId: Long): Int
}