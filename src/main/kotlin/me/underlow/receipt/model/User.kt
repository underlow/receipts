package me.underlow.receipt.model

import java.time.LocalDateTime

data class User(
    val id: Long? = null,
    val email: String,
    val name: String,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val lastLoginAt: LocalDateTime = LocalDateTime.now()
)
