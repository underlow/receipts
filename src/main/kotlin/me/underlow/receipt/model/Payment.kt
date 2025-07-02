package me.underlow.receipt.model

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive
import java.math.BigDecimal
import java.time.LocalDate

data class Payment(
    val id: Long? = null,
    
    @field:NotNull(message = "Service provider ID cannot be null")
    val serviceProviderId: Long,
    
    @field:NotNull(message = "Payment method ID cannot be null")
    val paymentMethodId: Long,
    
    @field:NotNull(message = "Amount cannot be null")
    @field:Positive(message = "Amount must be positive")
    val amount: BigDecimal,
    
    @field:NotBlank(message = "Currency cannot be blank")
    val currency: String,
    
    @field:NotNull(message = "Invoice date cannot be null")
    val invoiceDate: LocalDate,
    
    @field:NotNull(message = "Payment date cannot be null")
    val paymentDate: LocalDate,
    
    val billId: Long? = null,
    
    @field:NotNull(message = "User ID cannot be null")
    val userId: Long,
    
    val comment: String? = null
)