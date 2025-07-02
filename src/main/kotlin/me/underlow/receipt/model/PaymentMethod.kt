package me.underlow.receipt.model

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

data class PaymentMethod(
    val id: Long? = null,
    
    @field:NotBlank(message = "Payment method name cannot be blank")
    val name: String,
    
    @field:NotNull(message = "Payment method type cannot be null")
    val type: PaymentMethodType,
    
    val comment: String? = null
)