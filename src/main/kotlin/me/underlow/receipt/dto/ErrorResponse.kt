package me.underlow.receipt.dto

/**
 * Standard error response DTO for API endpoints.
 * Provides structured error information to clients.
 */
data class ErrorResponse(
    val success: Boolean = false,
    val message: String,
    val code: String? = null,
    val details: Map<String, Any>? = null
)