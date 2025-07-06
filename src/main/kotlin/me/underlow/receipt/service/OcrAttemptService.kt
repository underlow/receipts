package me.underlow.receipt.service

import me.underlow.receipt.model.EntityType
import me.underlow.receipt.model.OcrAttempt
import me.underlow.receipt.model.OcrProcessingStatus
import me.underlow.receipt.repository.OcrAttemptRepository
import me.underlow.receipt.repository.UserRepository
import org.springframework.stereotype.Service
import java.time.LocalDateTime

/**
 * Business logic service for managing OCR attempt history
 */
@Service
class OcrAttemptService(
    private val ocrAttemptRepository: OcrAttemptRepository,
    private val userRepository: UserRepository
) {

    /**
     * Records a new OCR attempt
     */
    fun recordOcrAttempt(
        entityType: EntityType,
        entityId: Long,
        userEmail: String,
        ocrEngineUsed: String,
        processingStatus: OcrProcessingStatus,
        extractedDataJson: String? = null,
        errorMessage: String? = null,
        rawResponse: String? = null
    ): OcrAttempt? {
        val user = userRepository.findByEmail(userEmail) ?: return null
        
        val ocrAttempt = OcrAttempt(
            entityType = entityType,
            entityId = entityId,
            attemptTimestamp = LocalDateTime.now(),
            ocrEngineUsed = ocrEngineUsed,
            processingStatus = processingStatus,
            extractedDataJson = extractedDataJson,
            errorMessage = errorMessage,
            rawResponse = rawResponse,
            userId = user.id!!
        )
        
        return ocrAttemptRepository.save(ocrAttempt)
    }

    /**
     * Gets OCR attempt history for a specific entity
     */
    fun getOcrHistory(entityType: EntityType, entityId: Long, userEmail: String): List<OcrAttempt> {
        val user = userRepository.findByEmail(userEmail) ?: return emptyList()
        val attempts = ocrAttemptRepository.findByEntityTypeAndEntityId(entityType, entityId)
        
        // Filter to only return attempts by this user
        return attempts.filter { it.userId == user.id }
    }

    /**
     * Gets the latest OCR attempt for an entity
     */
    fun getLatestOcrAttempt(entityType: EntityType, entityId: Long, userEmail: String): OcrAttempt? {
        val user = userRepository.findByEmail(userEmail) ?: return null
        val latestAttempt = ocrAttemptRepository.findLatestByEntityTypeAndEntityId(entityType, entityId)
        
        // Verify attempt belongs to this user
        return if (latestAttempt?.userId == user.id) latestAttempt else null
    }

    /**
     * Gets OCR statistics for a user
     */
    fun getOcrStatistics(userEmail: String): Map<String, Int> {
        val user = userRepository.findByEmail(userEmail) ?: return emptyMap()
        val attempts = ocrAttemptRepository.findByUserId(user.id!!)
        
        val successfulAttempts = attempts.count { it.processingStatus == OcrProcessingStatus.SUCCESS }
        val failedAttempts = attempts.count { it.processingStatus == OcrProcessingStatus.FAILED }
        val inProgressAttempts = attempts.count { it.processingStatus == OcrProcessingStatus.IN_PROGRESS }
        
        val engineStats = attempts.groupBy { it.ocrEngineUsed }.mapValues { it.value.size }
        
        return mapOf(
            "totalAttempts" to attempts.size,
            "successfulAttempts" to successfulAttempts,
            "failedAttempts" to failedAttempts,
            "inProgressAttempts" to inProgressAttempts
        ) + engineStats.mapKeys { "engine_${it.key}" }
    }

    /**
     * Transfers OCR history from one entity to another (used during entity conversion)
     */
    fun transferOcrHistory(
        fromEntityType: EntityType,
        fromEntityId: Long,
        toEntityType: EntityType,
        toEntityId: Long,
        userEmail: String
    ): Boolean {
        val user = userRepository.findByEmail(userEmail) ?: return false
        val attempts = ocrAttemptRepository.findByEntityTypeAndEntityId(fromEntityType, fromEntityId)
        
        // Filter to only transfer attempts by this user
        val userAttempts = attempts.filter { it.userId == user.id }
        
        // Create new records for the target entity
        userAttempts.forEach { attempt ->
            val newAttempt = attempt.copy(
                id = null,
                entityType = toEntityType,
                entityId = toEntityId
            )
            ocrAttemptRepository.save(newAttempt)
        }
        
        return true
    }

    /**
     * Deletes OCR history for an entity
     */
    fun deleteOcrHistory(entityType: EntityType, entityId: Long): Int {
        return ocrAttemptRepository.deleteByEntityTypeAndEntityId(entityType, entityId)
    }

    /**
     * Updates an OCR attempt status (e.g., from IN_PROGRESS to SUCCESS/FAILED)
     */
    fun updateAttemptStatus(
        attemptId: Long,
        processingStatus: OcrProcessingStatus,
        extractedDataJson: String? = null,
        errorMessage: String? = null
    ): OcrAttempt? {
        val attempt = ocrAttemptRepository.findById(attemptId) ?: return null
        
        val updatedAttempt = attempt.copy(
            processingStatus = processingStatus,
            extractedDataJson = extractedDataJson ?: attempt.extractedDataJson,
            errorMessage = errorMessage ?: attempt.errorMessage
        )
        
        return ocrAttemptRepository.save(updatedAttempt)
    }
}