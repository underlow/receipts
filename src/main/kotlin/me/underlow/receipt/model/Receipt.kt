package me.underlow.receipt.model

import jakarta.validation.constraints.NotNull

data class Receipt(
    val id: Long? = null,
    
    @field:NotNull(message = "User ID cannot be null")
    val userId: Long,
    
    val billId: Long? = null
)