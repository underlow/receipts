package me.underlow.receipt.service.ocr

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import java.time.LocalDate

class OcrResultTest {

    @Test
    fun `when creating successful result then all fields are set correctly`() {
        // Given
        val provider = "Target"
        val amount = 45.67
        val date = LocalDate.of(2024, 7, 3)
        val currency = "USD"
        val confidence = 0.95
        val rawJson = """{"provider":"Target","amount":45.67}"""
        val processingTime = 1500L

        // When
        val result = OcrResult.success(
            provider = provider,
            amount = amount,
            date = date,
            currency = currency,
            confidence = confidence,
            rawJson = rawJson,
            processingTimeMs = processingTime
        )

        // Then
        assertTrue(result.success)
        assertEquals(provider, result.extractedProvider)
        assertEquals(amount, result.extractedAmount)
        assertEquals(date, result.extractedDate)
        assertEquals(currency, result.extractedCurrency)
        assertEquals(confidence, result.confidence)
        assertEquals(rawJson, result.rawJson)
        assertEquals(processingTime, result.processingTimeMs)
        assertNull(result.errorMessage)
    }

    @Test
    fun `when creating successful result with minimal data then required fields are set`() {
        // Given
        val rawJson = """{"status":"processed"}"""

        // When
        val result = OcrResult.success(rawJson = rawJson)

        // Then
        assertTrue(result.success)
        assertNull(result.extractedProvider)
        assertNull(result.extractedAmount)
        assertNull(result.extractedDate)
        assertNull(result.extractedCurrency)
        assertNull(result.confidence)
        assertEquals(rawJson, result.rawJson)
        assertNull(result.processingTimeMs)
        assertNull(result.errorMessage)
    }

    @Test
    fun `when creating failure result then error fields are set correctly`() {
        // Given
        val errorMessage = "API rate limit exceeded"
        val rawJson = """{"error":"rate_limit"}"""
        val processingTime = 500L

        // When
        val result = OcrResult.failure(
            errorMessage = errorMessage,
            rawJson = rawJson,
            processingTimeMs = processingTime
        )

        // Then
        assertFalse(result.success)
        assertEquals(errorMessage, result.errorMessage)
        assertEquals(rawJson, result.rawJson)
        assertEquals(processingTime, result.processingTimeMs)
        assertNull(result.extractedProvider)
        assertNull(result.extractedAmount)
        assertNull(result.extractedDate)
        assertNull(result.extractedCurrency)
        assertNull(result.confidence)
    }

    @Test
    fun `when creating failure result with minimal data then defaults are used`() {
        // Given
        val errorMessage = "Connection timeout"

        // When
        val result = OcrResult.failure(errorMessage)

        // Then
        assertFalse(result.success)
        assertEquals(errorMessage, result.errorMessage)
        assertEquals("{}", result.rawJson)
        assertNull(result.processingTimeMs)
        assertNull(result.extractedProvider)
        assertNull(result.extractedAmount)
        assertNull(result.extractedDate)
        assertNull(result.extractedCurrency)
        assertNull(result.confidence)
    }
}