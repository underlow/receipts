package me.underlow.receipt.service.ocr

import java.io.File

/**
 * Request data for OCR processing containing file and processing options.
 * Provides flexibility for future enhancements like language hints or processing preferences.
 */
data class OcrRequest(
    val file: File,
    val expectedLanguage: String? = null,
    val maxRetries: Int = 3,
    val timeoutMs: Long = 30000,
    val extractionHints: List<String> = emptyList()
) {
    
    /**
     * Validates that the request is properly formed and the file is accessible.
     */
    fun validate(): List<String> {
        val errors = mutableListOf<String>()
        
        if (!file.exists()) {
            errors.add("File does not exist: ${file.absolutePath}")
        }
        
        if (!file.canRead()) {
            errors.add("File is not readable: ${file.absolutePath}")
        }
        
        if (file.length() == 0L) {
            errors.add("File is empty: ${file.absolutePath}")
        }
        
        if (maxRetries < 0) {
            errors.add("Max retries cannot be negative: $maxRetries")
        }
        
        if (timeoutMs <= 0) {
            errors.add("Timeout must be positive: $timeoutMs")
        }
        
        return errors
    }
    
    /**
     * Returns true if the request is valid for processing.
     */
    fun isValid(): Boolean = validate().isEmpty()
}