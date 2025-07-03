package me.underlow.receipt.service.ocr

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.nio.file.Path

class OcrRequestTest {

    @TempDir
    lateinit var tempDir: Path

    @Test
    fun `when creating request with valid file then validation passes`() {
        // Given
        val validFile = tempDir.resolve("receipt.jpg").toFile()
        validFile.writeText("test image content")
        
        // When
        val request = OcrRequest(file = validFile)
        
        // Then
        assertTrue(request.isValid())
        assertTrue(request.validate().isEmpty())
    }

    @Test
    fun `when file does not exist then validation fails`() {
        // Given
        val nonExistentFile = File("/non/existent/file.jpg")
        
        // When
        val request = OcrRequest(file = nonExistentFile)
        val errors = request.validate()
        
        // Then
        assertFalse(request.isValid())
        assertTrue(errors.any { it.contains("File does not exist") })
    }

    @Test
    fun `when file is empty then validation fails`() {
        // Given
        val emptyFile = tempDir.resolve("empty.jpg").toFile()
        emptyFile.createNewFile()
        
        // When
        val request = OcrRequest(file = emptyFile)
        val errors = request.validate()
        
        // Then
        assertFalse(request.isValid())
        assertTrue(errors.any { it.contains("File is empty") })
    }

    @Test
    fun `when max retries is negative then validation fails`() {
        // Given
        val validFile = tempDir.resolve("receipt.jpg").toFile()
        validFile.writeText("test content")
        
        // When
        val request = OcrRequest(file = validFile, maxRetries = -1)
        val errors = request.validate()
        
        // Then
        assertFalse(request.isValid())
        assertTrue(errors.any { it.contains("Max retries cannot be negative") })
    }

    @Test
    fun `when timeout is zero or negative then validation fails`() {
        // Given
        val validFile = tempDir.resolve("receipt.jpg").toFile()
        validFile.writeText("test content")
        
        // When
        val requestZeroTimeout = OcrRequest(file = validFile, timeoutMs = 0)
        val requestNegativeTimeout = OcrRequest(file = validFile, timeoutMs = -1000)
        
        // Then
        assertFalse(requestZeroTimeout.isValid())
        assertFalse(requestNegativeTimeout.isValid())
        assertTrue(requestZeroTimeout.validate().any { it.contains("Timeout must be positive") })
        assertTrue(requestNegativeTimeout.validate().any { it.contains("Timeout must be positive") })
    }

    @Test
    fun `when creating request with custom parameters then values are set correctly`() {
        // Given
        val validFile = tempDir.resolve("receipt.jpg").toFile()
        validFile.writeText("test content")
        val expectedLanguage = "en"
        val expectedRetries = 5
        val expectedTimeout = 60000L
        val expectedHints = listOf("receipt", "invoice")
        
        // When
        val request = OcrRequest(
            file = validFile,
            expectedLanguage = expectedLanguage,
            maxRetries = expectedRetries,
            timeoutMs = expectedTimeout,
            extractionHints = expectedHints
        )
        
        // Then
        assertEquals(validFile, request.file)
        assertEquals(expectedLanguage, request.expectedLanguage)
        assertEquals(expectedRetries, request.maxRetries)
        assertEquals(expectedTimeout, request.timeoutMs)
        assertEquals(expectedHints, request.extractionHints)
        assertTrue(request.isValid())
    }

    @Test
    fun `when creating request with defaults then default values are used`() {
        // Given
        val validFile = tempDir.resolve("receipt.jpg").toFile()
        validFile.writeText("test content")
        
        // When
        val request = OcrRequest(file = validFile)
        
        // Then
        assertEquals(validFile, request.file)
        assertNull(request.expectedLanguage)
        assertEquals(3, request.maxRetries)
        assertEquals(30000L, request.timeoutMs)
        assertTrue(request.extractionHints.isEmpty())
        assertTrue(request.isValid())
    }
}