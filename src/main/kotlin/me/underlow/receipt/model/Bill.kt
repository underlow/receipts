package me.underlow.receipt.model

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import java.time.LocalDate
import java.time.LocalDateTime

data class Bill(
    val id: Long? = null,
    
    @field:NotBlank(message = "Filename cannot be blank")
    val filename: String,
    
    @field:NotBlank(message = "File path cannot be blank")
    val filePath: String,
    
    @field:NotNull(message = "Upload date cannot be null")
    val uploadDate: LocalDateTime,
    
    @field:NotNull(message = "Status cannot be null")
    val status: BillStatus,
    
    val ocrRawJson: String? = null,
    
    val extractedAmount: Double? = null,
    
    val extractedDate: LocalDate? = null,
    
    val extractedProvider: String? = null,
    
    @field:NotNull(message = "User ID cannot be null")
    val userId: Long
)