package me.underlow.receipt.model

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import java.time.LocalDate
import java.time.LocalDateTime

data class Receipt(
    val id: Long? = null,
    
    @field:NotNull(message = "User ID cannot be null")
    val userId: Long,
    
    val billId: Long? = null,
    
    // File metadata fields (optional for receipts created from IncomingFile)
    val filename: String? = null,
    
    val filePath: String? = null,
    
    val uploadDate: LocalDateTime? = null,
    
    val checksum: String? = null,
    
    @field:NotNull(message = "Status cannot be null")
    val status: BillStatus = BillStatus.PENDING,
    
    // OCR processing fields
    val ocrRawJson: String? = null,
    
    val extractedAmount: Double? = null,
    
    val extractedDate: LocalDate? = null,
    
    val extractedProvider: String? = null,
    
    val ocrProcessedAt: LocalDateTime? = null,
    
    val ocrErrorMessage: String? = null,
    
    val originalIncomingFileId: Long? = null
)