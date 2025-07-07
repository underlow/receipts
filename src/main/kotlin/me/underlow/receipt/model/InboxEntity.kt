package me.underlow.receipt.model

import java.time.LocalDateTime

/**
 * Represents an inbox item in the document processing system.
 * An inbox entity tracks the lifecycle of an uploaded document from initial upload
 * through OCR processing to final approval and conversion to bill or receipt.
 */
data class InboxEntity(
    val id: String,
    val uploadedImage: String,
    val uploadDate: LocalDateTime,
    val ocrResults: String? = null,
    val linkedEntityId: String? = null,
    val linkedEntityType: EntityType? = null,
    val state: InboxState = InboxState.CREATED,
    val failureReason: String? = null
) {
    /**
     * Transitions the inbox entity from CREATED to PROCESSED state after successful OCR processing.
     * 
     * @param ocrResults the extracted text results from OCR processing
     * @return new InboxEntity instance in PROCESSED state
     * @throws IllegalStateException if the current state is not CREATED
     */
    fun processOCR(ocrResults: String): InboxEntity {
        if (state != InboxState.CREATED) {
            throw IllegalStateException("Cannot process OCR from state $state")
        }
        
        return copy(
            state = InboxState.PROCESSED,
            ocrResults = ocrResults,
            failureReason = null
        )
    }

    /**
     * Transitions the inbox entity from CREATED to FAILED state when OCR processing fails.
     * 
     * @param reason the failure reason explaining why OCR failed
     * @return new InboxEntity instance in FAILED state
     * @throws IllegalStateException if the current state is not CREATED
     */
    fun failOCR(reason: String): InboxEntity {
        if (state != InboxState.CREATED) {
            throw IllegalStateException("Cannot fail OCR from state $state")
        }
        
        return copy(
            state = InboxState.FAILED,
            failureReason = reason,
            ocrResults = null
        )
    }

    /**
     * Transitions the inbox entity from PROCESSED to APPROVED state when user approves the document.
     * Creates a link to the generated bill or receipt entity.
     * 
     * @param entityId the ID of the created bill or receipt entity
     * @param entityType the type of entity created (BILL or RECEIPT)
     * @return new InboxEntity instance in APPROVED state
     * @throws IllegalStateException if the current state is not PROCESSED
     */
    fun approve(entityId: String, entityType: EntityType): InboxEntity {
        if (state != InboxState.PROCESSED) {
            throw IllegalStateException("Cannot approve from state $state")
        }
        
        return copy(
            state = InboxState.APPROVED,
            linkedEntityId = entityId,
            linkedEntityType = entityType
        )
    }

    /**
     * Transitions the inbox entity from FAILED back to CREATED state for retry.
     * Clears the failure reason and any partial OCR results.
     * 
     * @return new InboxEntity instance in CREATED state ready for retry
     * @throws IllegalStateException if the current state is not FAILED
     */
    fun retryOCR(): InboxEntity {
        if (state != InboxState.FAILED) {
            throw IllegalStateException("Cannot retry OCR from state $state")
        }
        
        return copy(
            state = InboxState.CREATED,
            failureReason = null,
            ocrResults = null
        )
    }

    /**
     * Checks if the inbox entity can be approved by the user.
     * Only entities in PROCESSED state can be approved.
     * 
     * @return true if the entity can be approved, false otherwise
     */
    fun canApprove(): Boolean {
        return state == InboxState.PROCESSED
    }

    /**
     * Checks if the inbox entity can be retried for OCR processing.
     * Only entities in FAILED state can be retried.
     * 
     * @return true if the entity can be retried, false otherwise
     */
    fun canRetry(): Boolean {
        return state == InboxState.FAILED
    }
}

/**
 * Represents the current state of an inbox entity in the document processing workflow.
 */
enum class InboxState {
    /** Just uploaded, OCR processing pending */
    CREATED,
    
    /** OCR processing completed successfully */
    PROCESSED,
    
    /** OCR processing failed */
    FAILED,
    
    /** User approved, bill or receipt created */
    APPROVED
}

/**
 * Represents the type of entity that can be created from an approved inbox item.
 */
enum class EntityType {
    /** Bill/invoice document */
    BILL,
    
    /** Receipt document */
    RECEIPT
}