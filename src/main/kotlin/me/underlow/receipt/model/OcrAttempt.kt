package me.underlow.receipt.model

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import java.time.LocalDateTime

/**
 * Represents an OCR processing attempt for tracking history and audit purposes
 */
data class OcrAttempt(
    val id: Long? = null,
    
    @field:NotNull(message = "Entity type cannot be null")
    val entityType: EntityType,
    
    @field:NotNull(message = "Entity ID cannot be null")
    val entityId: Long,
    
    @field:NotNull(message = "Attempt timestamp cannot be null")
    val attemptTimestamp: LocalDateTime,
    
    @field:NotBlank(message = "OCR engine used cannot be blank")
    val ocrEngineUsed: String,
    
    @field:NotNull(message = "Processing status cannot be null")
    val processingStatus: OcrProcessingStatus,
    
    val extractedDataJson: String? = null,
    
    val errorMessage: String? = null,
    
    val rawResponse: String? = null,
    
    @field:NotNull(message = "User ID cannot be null")
    val userId: Long
)

/**
 * Entity types that can have OCR processing attempts
 */
enum class EntityType {
    INCOMING_FILE,
    BILL,
    RECEIPT
}

/**
 * OCR processing status enumeration
 */
enum class OcrProcessingStatus {
    SUCCESS,
    FAILED,
    IN_PROGRESS
}