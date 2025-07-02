package me.underlow.receipt.model

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

data class ServiceProvider(
    val id: Long? = null,
    
    @field:NotBlank(message = "Service provider name cannot be blank")
    val name: String,
    
    @field:NotBlank(message = "Category cannot be blank")
    val category: String,
    
    @field:NotBlank(message = "Default payment method cannot be blank")
    val defaultPaymentMethod: String,
    
    @field:NotNull(message = "isActive status cannot be null")
    val isActive: Boolean,
    
    val comment: String? = null
)