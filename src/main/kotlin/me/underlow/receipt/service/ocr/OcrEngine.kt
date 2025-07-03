package me.underlow.receipt.service.ocr

import java.io.File

/**
 * Interface for OCR engines that can extract receipt information from files.
 * Provides a standardized contract for different OCR providers (OpenAI, Claude, Google AI).
 */
interface OcrEngine {
    
    /**
     * Processes a file and extracts receipt information using OCR.
     * Returns standardized OCR result with extracted fields and raw response.
     */
    suspend fun processFile(file: File): OcrResult
    
    /**
     * Returns the name of this OCR engine for logging and identification.
     */
    fun getEngineName(): String
    
    /**
     * Checks if this OCR engine is available and properly configured.
     */
    fun isAvailable(): Boolean
}