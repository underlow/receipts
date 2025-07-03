package me.underlow.receipt.service.ocr

import java.time.LocalDate

/**
 * Standardized result from OCR processing containing extracted receipt information.
 * All OCR engines return data in this common format for consistent processing.
 */
data class OcrResult(
    val success: Boolean,
    val extractedProvider: String? = null,
    val extractedAmount: Double? = null,
    val extractedDate: LocalDate? = null,
    val extractedCurrency: String? = null,
    val confidence: Double? = null,
    val rawJson: String,
    val errorMessage: String? = null,
    val processingTimeMs: Long? = null
) {
    companion object {
        
        /**
         * Creates a successful OCR result with extracted data.
         */
        fun success(
            provider: String? = null,
            amount: Double? = null,
            date: LocalDate? = null,
            currency: String? = null,
            confidence: Double? = null,
            rawJson: String,
            processingTimeMs: Long? = null
        ): OcrResult {
            return OcrResult(
                success = true,
                extractedProvider = provider,
                extractedAmount = amount,
                extractedDate = date,
                extractedCurrency = currency,
                confidence = confidence,
                rawJson = rawJson,
                processingTimeMs = processingTimeMs
            )
        }
        
        /**
         * Creates a failed OCR result with error information.
         */
        fun failure(
            errorMessage: String,
            rawJson: String = "{}",
            processingTimeMs: Long? = null
        ): OcrResult {
            return OcrResult(
                success = false,
                rawJson = rawJson,
                errorMessage = errorMessage,
                processingTimeMs = processingTimeMs
            )
        }
    }
}