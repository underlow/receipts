package me.underlow.receipt.dto

import me.underlow.receipt.model.BillStatus
import me.underlow.receipt.model.IncomingFile
import java.io.File
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * DTO for detailed IncomingFile information displayed in the detail view
 */
data class IncomingFileDetailDto(
    val id: Long,
    val filename: String,
    val filePath: String,
    val uploadDate: LocalDateTime,
    val uploadDateFormatted: String,
    val status: BillStatus,
    val statusDisplayName: String,
    val checksum: String,
    val fileSize: Long,
    val fileSizeFormatted: String,
    val fileUrl: String,
    val thumbnailUrl: String,
    val canApprove: Boolean,
    val canReject: Boolean,
    val canDelete: Boolean,
    val fileExists: Boolean,
    val isImage: Boolean,
    val isPdf: Boolean,
    // OCR fields
    val hasOcrResults: Boolean,
    val ocrRawJson: String?,
    val extractedAmount: Double?,
    val extractedDate: LocalDate?,
    val extractedProvider: String?,
    val ocrProcessedAt: LocalDateTime?,
    val ocrProcessedAtFormatted: String?,
    val ocrErrorMessage: String?,
    val canRetryOcr: Boolean,
    val readyForDispatch: Boolean
) {
    companion object {
        private val dateFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy 'at' HH:mm")
        
        /**
         * Creates IncomingFileDetailDto from IncomingFile entity
         */
        fun fromIncomingFile(incomingFile: IncomingFile): IncomingFileDetailDto {
            val file = File(incomingFile.filePath)
            val fileSize = if (file.exists()) file.length() else 0L
            val fileExtension = incomingFile.filename.substringAfterLast('.', "").lowercase()
            
            return IncomingFileDetailDto(
                id = incomingFile.id!!,
                filename = incomingFile.filename,
                filePath = incomingFile.filePath,
                uploadDate = incomingFile.uploadDate,
                uploadDateFormatted = incomingFile.uploadDate.format(dateFormatter),
                status = incomingFile.status,
                statusDisplayName = formatStatus(incomingFile.status),
                checksum = incomingFile.checksum,
                fileSize = fileSize,
                fileSizeFormatted = formatFileSize(fileSize),
                fileUrl = "/api/files/${incomingFile.id}",
                thumbnailUrl = "/api/files/${incomingFile.id}/thumbnail",
                canApprove = incomingFile.status == BillStatus.PENDING,
                canReject = incomingFile.status == BillStatus.PENDING,
                canDelete = true, // Users can always delete their own files
                fileExists = file.exists(),
                isImage = isImageFile(fileExtension),
                isPdf = fileExtension == "pdf",
                // OCR fields
                hasOcrResults = incomingFile.ocrRawJson != null,
                ocrRawJson = incomingFile.ocrRawJson,
                extractedAmount = incomingFile.extractedAmount,
                extractedDate = incomingFile.extractedDate,
                extractedProvider = incomingFile.extractedProvider,
                ocrProcessedAt = incomingFile.ocrProcessedAt,
                ocrProcessedAtFormatted = incomingFile.ocrProcessedAt?.format(dateFormatter),
                ocrErrorMessage = incomingFile.ocrErrorMessage,
                canRetryOcr = incomingFile.status == BillStatus.REJECTED && incomingFile.ocrErrorMessage != null,
                readyForDispatch = incomingFile.status == BillStatus.APPROVED && incomingFile.ocrRawJson != null
            )
        }
        
        /**
         * Formats the status for display
         */
        private fun formatStatus(status: BillStatus): String {
            return when (status) {
                BillStatus.PENDING -> "Pending Review"
                BillStatus.PROCESSING -> "Processing"
                BillStatus.APPROVED -> "Approved"
                BillStatus.REJECTED -> "Rejected"
            }
        }
        
        /**
         * Formats file size in human-readable format
         */
        private fun formatFileSize(sizeInBytes: Long): String {
            if (sizeInBytes <= 0) return "0 B"
            
            val units = arrayOf("B", "KB", "MB", "GB", "TB")
            var size = sizeInBytes.toDouble()
            var unitIndex = 0
            
            while (size >= 1024 && unitIndex < units.size - 1) {
                size /= 1024
                unitIndex++
            }
            
            return String.format("%.1f %s", size, units[unitIndex])
        }
        
        /**
         * Checks if the file is an image based on extension
         */
        private fun isImageFile(extension: String): Boolean {
            return extension in setOf("jpg", "jpeg", "png", "gif", "bmp", "tiff", "tif")
        }
    }
}