package me.underlow.receipt.config

import com.fasterxml.jackson.databind.ObjectMapper
import me.underlow.receipt.service.ocr.ClaudeOcrEngine
import me.underlow.receipt.service.ocr.GoogleAiOcrEngine
import me.underlow.receipt.service.ocr.OcrEngine
import me.underlow.receipt.service.ocr.OpenAiOcrEngine
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.springframework.web.reactive.function.client.WebClient

class OcrConfigTest {

    private val ocrConfig = OcrConfig()
    private val webClient = WebClient.builder().build()
    private val objectMapper = ObjectMapper()

    @Test
    fun `when OpenAI API key is configured then creates OpenAI engine`() {
        // Given
        val receiptsProperties = ReceiptsProperties().apply {
            openaiApiKey = "sk-test-key"
        }

        // When
        val engine = ocrConfig.openAiOcrEngine(receiptsProperties, webClient, objectMapper)

        // Then
        assertNotNull(engine)
        assertTrue(engine is OpenAiOcrEngine)
        assertEquals("OpenAI GPT-4 Vision", engine?.getEngineName())
        assertTrue(engine?.isAvailable() == true)
    }

    @Test
    fun `when OpenAI API key is not configured then returns null`() {
        // Given
        val receiptsProperties = ReceiptsProperties().apply {
            openaiApiKey = null
        }

        // When
        val engine = ocrConfig.openAiOcrEngine(receiptsProperties, webClient, objectMapper)

        // Then
        assertNull(engine)
    }

    @Test
    fun `when OpenAI API key is placeholder then returns null`() {
        // Given
        val receiptsProperties = ReceiptsProperties().apply {
            openaiApiKey = "openaiApiKey"
        }

        // When
        val engine = ocrConfig.openAiOcrEngine(receiptsProperties, webClient, objectMapper)

        // Then
        assertNull(engine)
    }

    @Test
    fun `when Claude API key is configured then creates Claude engine`() {
        // Given
        val receiptsProperties = ReceiptsProperties().apply {
            claudeApiKey = "sk-ant-test-key"
        }

        // When
        val engine = ocrConfig.claudeOcrEngine(receiptsProperties, webClient, objectMapper)

        // Then
        assertNotNull(engine)
        assertTrue(engine is ClaudeOcrEngine)
        assertEquals("Anthropic Claude Vision", engine?.getEngineName())
        assertTrue(engine?.isAvailable() == true)
    }

    @Test
    fun `when Google AI API key is configured then creates Google AI engine`() {
        // Given
        val receiptsProperties = ReceiptsProperties().apply {
            googleAiApiKey = "AIza-test-key"
        }

        // When
        val engine = ocrConfig.googleAiOcrEngine(receiptsProperties, webClient, objectMapper)

        // Then
        assertNotNull(engine)
        assertTrue(engine is GoogleAiOcrEngine)
        assertEquals("Google AI Gemini Vision", engine?.getEngineName())
        assertTrue(engine?.isAvailable() == true)
    }

    @Test
    fun `when multiple engines configured then available engines list contains all`() {
        // Given
        val openAiEngine = OpenAiOcrEngine("sk-test", webClient, objectMapper)
        val claudeEngine = ClaudeOcrEngine("sk-ant-test", webClient, objectMapper)
        val googleEngine = GoogleAiOcrEngine("AIza-test", webClient, objectMapper)

        // When
        val availableEngines = ocrConfig.availableOcrEngines(openAiEngine, claudeEngine, googleEngine)

        // Then
        assertEquals(3, availableEngines.size)
        assertTrue(availableEngines.any { it.getEngineName().contains("OpenAI") })
        assertTrue(availableEngines.any { it.getEngineName().contains("Claude") })
        assertTrue(availableEngines.any { it.getEngineName().contains("Google") })
    }

    @Test
    fun `when no engines configured then available engines list is empty`() {
        // Given
        // All engines are null

        // When
        val availableEngines = ocrConfig.availableOcrEngines(null, null, null)

        // Then
        assertTrue(availableEngines.isEmpty())
    }

    @Test
    fun `when creating WebClient then configures 10MB buffer`() {
        // When
        val webClient = ocrConfig.ocrWebClient()

        // Then
        assertNotNull(webClient)
        // Note: Testing buffer size would require reflection or integration test
        // We can only verify the bean is created successfully
    }
}