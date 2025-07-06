package me.underlow.receipt.model

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import java.time.LocalDateTime

/**
 * User entity representing authenticated users in the system.
 * Contains user profile information from OAuth provider.
 */
data class User(
    val id: Long? = null,
    
    @field:NotBlank(message = "Email is required")
    @field:Email(message = "Email must be valid")
    val email: String,
    
    @field:NotBlank(message = "Name is required")
    val name: String,
    
    val avatar: String? = null,
    
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
) {
    /**
     * Constructor for creating new users without ID and timestamps
     */
    constructor(email: String, name: String, avatar: String? = null) : this(
        id = null,
        email = email,
        name = name,
        avatar = avatar,
        createdAt = LocalDateTime.now(),
        updatedAt = LocalDateTime.now()
    )
}