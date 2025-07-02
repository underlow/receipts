package me.underlow.receipt.dto

import me.underlow.receipt.model.BillStatus
import java.time.LocalDateTime

/**
 * Response DTO for file upload operations.
 * Contains the result of a successful file upload with IncomingFile details.
 */
data class FileUploadResponse(
    val id: Long,
    val filename: String,
    val uploadDate: LocalDateTime,
    val status: BillStatus,
    val checksum: String,
    val success: Boolean = true,
    val message: String? = null
)