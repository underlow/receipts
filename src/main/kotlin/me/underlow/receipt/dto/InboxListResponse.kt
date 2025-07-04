package me.underlow.receipt.dto

import me.underlow.receipt.model.ItemStatus
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * DTO for inbox file list item display
 */
data class InboxFileDto(
    val id: Long,
    val filename: String,
    val uploadDate: LocalDateTime,
    val status: ItemStatus,
    val thumbnailUrl: String,
    val fileUrl: String,
    val statusDisplayName: String,
    // OCR fields
    val hasOcrResults: Boolean,
    val extractedAmount: Double?,
    val extractedDate: LocalDate?,
    val extractedProvider: String?,
    val ocrProcessedAt: LocalDateTime?,
    val ocrErrorMessage: String?
) {
    companion object {
        fun fromIncomingFile(incomingFile: me.underlow.receipt.model.IncomingFile): InboxFileDto {
            return InboxFileDto(
                id = incomingFile.id!!,
                filename = incomingFile.filename,
                uploadDate = incomingFile.uploadDate,
                status = incomingFile.status,
                thumbnailUrl = "/api/files/${incomingFile.id}/thumbnail",
                fileUrl = "/api/files/${incomingFile.id}",
                statusDisplayName = formatStatus(incomingFile.status),
                hasOcrResults = incomingFile.ocrRawJson != null,
                extractedAmount = incomingFile.extractedAmount,
                extractedDate = incomingFile.extractedDate,
                extractedProvider = incomingFile.extractedProvider,
                ocrProcessedAt = incomingFile.ocrProcessedAt,
                ocrErrorMessage = incomingFile.ocrErrorMessage
            )
        }

        private fun formatStatus(status: ItemStatus): String {
            return when (status) {
                ItemStatus.NEW -> "New"
                ItemStatus.PROCESSING -> "Processing"
                ItemStatus.APPROVED -> "Approved"
                ItemStatus.REJECTED -> "Rejected"
            }
        }
    }
}

/**
 * DTO for paginated inbox list response
 */
data class InboxListResponse(
    val files: List<InboxFileDto>,
    val totalFiles: Long,
    val currentPage: Int,
    val totalPages: Int,
    val pageSize: Int,
    val statusCounts: Map<String, Int>
)

/**
 * DTO for file operation responses (approve, reject, delete)
 */
data class FileOperationResponse(
    val success: Boolean,
    val message: String,
    val fileId: Long? = null
)