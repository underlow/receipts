package me.underlow.receipt.service

import kotlinx.coroutines.test.runTest
import me.underlow.receipt.model.BillStatus
import me.underlow.receipt.model.IncomingFile
import me.underlow.receipt.service.ocr.OcrEngine
import me.underlow.receipt.service.ocr.OcrResult
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.io.TempDir
import org.mockito.kotlin.*
import java.io.File
import java.nio.file.Path
import java.time.LocalDate
import java.time.LocalDateTime

class OcrServiceTest {

    @TempDir
    lateinit var tempDir: Path

    private lateinit var mockEngine1: OcrEngine
    private lateinit var mockEngine2: OcrEngine
    private lateinit var testFile: File

    @BeforeEach
    fun setup() {
        mockEngine1 = mock {
            on { getEngineName() } doReturn "MockEngine1"
            on { isAvailable() } doReturn true
        }
        
        mockEngine2 = mock {
            on { getEngineName() } doReturn "MockEngine2"
            on { isAvailable() } doReturn true
        }

        testFile = tempDir.resolve("test-receipt.jpg").toFile()
        testFile.writeText("test image content")
    }

    @Test
    fun `when no engines available then returns failure result`() = runTest {
        // Given
        val ocrService = OcrService(emptyList())
        
        // When
        val result = ocrService.processFile(testFile)
        
        // Then
        assertFalse(result.success)
        assertEquals("No OCR engines available", result.errorMessage)
    }

    @Test
    fun `when primary engine succeeds then returns success result`() = runTest {
        // Given
        val expectedResult = OcrResult.success(
            provider = "Target",
            amount = 25.99,
            date = LocalDate.of(2024, 7, 3),
            rawJson = """{"provider":"Target","amount":25.99}"""
        )
        
        whenever(mockEngine1.processFile(testFile)) doReturn expectedResult
        
        val ocrService = OcrService(listOf(mockEngine1, mockEngine2))
        
        // When
        val result = ocrService.processFile(testFile)
        
        // Then
        assertTrue(result.success)
        assertEquals("Target", result.extractedProvider)
        assertEquals(25.99, result.extractedAmount)
        verify(mockEngine1).processFile(testFile)
        verify(mockEngine2, never()).processFile(any())
    }

    @Test
    fun `when processing incoming file then uses file path from entity`() = runTest {
        // Given
        val incomingFile = IncomingFile(
            id = 1L,
            filename = "receipt.jpg",
            filePath = testFile.absolutePath,
            uploadDate = LocalDateTime.now(),
            status = BillStatus.PENDING,
            checksum = "abc123",
            userId = 1L
        )
        
        val expectedResult = OcrResult.success(rawJson = """{"processed":true}""")
        whenever(mockEngine1.processFile(any())) doReturn expectedResult
        
        val ocrService = OcrService(listOf(mockEngine1))
        
        // When
        val result = ocrService.processIncomingFile(incomingFile)
        
        // Then
        assertTrue(result.success)
        verify(mockEngine1).processFile(argThat<File> { 
            this.absolutePath == testFile.absolutePath 
        })
    }

    @Test
    fun `when using fallback and first engine fails then tries second engine`() = runTest {
        // Given
        val failureResult = OcrResult.failure("First engine failed")
        val successResult = OcrResult.success(
            provider = "Walmart",
            amount = 15.50,
            rawJson = """{"provider":"Walmart","amount":15.50}"""
        )
        
        whenever(mockEngine1.processFile(testFile)) doReturn failureResult
        whenever(mockEngine2.processFile(testFile)) doReturn successResult
        
        val ocrService = OcrService(listOf(mockEngine1, mockEngine2))
        
        // When
        val result = ocrService.processFileWithFallback(testFile)
        
        // Then
        assertTrue(result.success)
        assertEquals("Walmart", result.extractedProvider)
        assertEquals(15.50, result.extractedAmount)
        verify(mockEngine1).processFile(testFile)
        verify(mockEngine2).processFile(testFile)
    }

    @Test
    fun `when all engines fail then returns failure with last error`() = runTest {
        // Given
        val failure1 = OcrResult.failure("Engine 1 failed")
        val failure2 = OcrResult.failure("Engine 2 failed")
        
        whenever(mockEngine1.processFile(testFile)) doReturn failure1
        whenever(mockEngine2.processFile(testFile)) doReturn failure2
        
        val ocrService = OcrService(listOf(mockEngine1, mockEngine2))
        
        // When
        val result = ocrService.processFileWithFallback(testFile)
        
        // Then
        assertFalse(result.success)
        assertTrue(result.errorMessage!!.contains("Engine 2 failed"))
        verify(mockEngine1).processFile(testFile)
        verify(mockEngine2).processFile(testFile)
    }

    @Test
    fun `when engine throws exception then continues to next engine`() = runTest {
        // Given
        val successResult = OcrResult.success(rawJson = """{"processed":true}""")
        
        whenever(mockEngine1.processFile(testFile)) doThrow RuntimeException("API timeout")
        whenever(mockEngine2.processFile(testFile)) doReturn successResult
        
        val ocrService = OcrService(listOf(mockEngine1, mockEngine2))
        
        // When
        val result = ocrService.processFileWithFallback(testFile)
        
        // Then
        assertTrue(result.success)
        verify(mockEngine1).processFile(testFile)
        verify(mockEngine2).processFile(testFile)
    }

    @Test
    fun `when checking available engines then returns only available ones`() {
        // Given
        val unavailableEngine = mock<OcrEngine> {
            on { getEngineName() } doReturn "UnavailableEngine"
            on { isAvailable() } doReturn false
        }
        
        val ocrService = OcrService(listOf(mockEngine1, unavailableEngine, mockEngine2))
        
        // When
        val engineNames = ocrService.getAvailableEngineNames()
        
        // Then
        assertEquals(2, engineNames.size)
        assertTrue(engineNames.contains("MockEngine1"))
        assertTrue(engineNames.contains("MockEngine2"))
        assertFalse(engineNames.contains("UnavailableEngine"))
    }

    @Test
    fun `when has available engines check then returns correct status`() {
        // Given
        val unavailableEngine = mock<OcrEngine> {
            on { isAvailable() } doReturn false
        }
        
        val serviceWithEngines = OcrService(listOf(mockEngine1))
        val serviceWithUnavailableEngines = OcrService(listOf(unavailableEngine))
        val serviceWithNoEngines = OcrService(emptyList())
        
        // When & Then
        assertTrue(serviceWithEngines.hasAvailableEngines())
        assertFalse(serviceWithUnavailableEngines.hasAvailableEngines())
        assertFalse(serviceWithNoEngines.hasAvailableEngines())
    }
}