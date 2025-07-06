package me.underlow.receipt.model

import java.time.LocalDateTime

data class LoginEvent(
    val id: Long? = null,
    val userId: Long,
    val timestamp: LocalDateTime = LocalDateTime.now()
)
