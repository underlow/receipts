package me.underlow.receipt.model

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import java.time.LocalDateTime

/**
 * Represents a file that has been detected in the inbox directory and moved to storage.
 * IncomingFiles are in PENDING status until they are processed through OCR and converted 
 * to Bills or Receipts after user approval.
 */
data class IncomingFile(
    val id: Long? = null,
    
    @field:NotBlank(message = "Filename cannot be blank")
    val filename: String,
    
    @field:NotBlank(message = "File path cannot be blank")
    val filePath: String,
    
    @field:NotNull(message = "Upload date cannot be null")
    val uploadDate: LocalDateTime,
    
    @field:NotNull(message = "Status cannot be null")
    val status: BillStatus,
    
    @field:NotBlank(message = "Checksum cannot be blank")
    val checksum: String,
    
    @field:NotNull(message = "User ID cannot be null")
    val userId: Long
)