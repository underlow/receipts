package me.underlow.receipt.service.ocr

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import reactor.core.publisher.Mono
import java.io.File
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.*

/**
 * Anthropic Claude OCR engine implementation using Claude Vision API for receipt processing.
 * Extracts receipt information from images and returns standardized results.
 */
class ClaudeOcrEngine(
    private val apiKey: String,
    private val webClient: WebClient,
    private val objectMapper: ObjectMapper
) : OcrEngine {
    
    private val logger = LoggerFactory.getLogger(ClaudeOcrEngine::class.java)
    
    companion object {
        private const val CLAUDE_API_URL = "https://api.anthropic.com/v1/messages"
        private const val MODEL = "claude-3-haiku-20240307"
        private const val MAX_TOKENS = 500
        private const val ANTHROPIC_VERSION = "2023-06-01"
    }
    
    override suspend fun processFile(file: File): OcrResult {
        val startTime = System.currentTimeMillis()
        
        return try {
            logger.debug("Processing file with Claude: ${file.name}")
            
            if (!isAvailable()) {
                return OcrResult.failure("Claude OCR engine is not available", processingTimeMs = System.currentTimeMillis() - startTime)
            }
            
            val base64Image = encodeFileToBase64(file)
            val mimeType = determineMimeType(file)
            val response = callClaudeApi(base64Image, mimeType)
            
            val processingTime = System.currentTimeMillis() - startTime
            parseClaudeResponse(response, processingTime)
            
        } catch (e: Exception) {
            logger.error("Error processing file with Claude: ${file.name}", e)
            val processingTime = System.currentTimeMillis() - startTime
            OcrResult.failure("Claude API error: ${e.message}", processingTimeMs = processingTime)
        }
    }
    
    override fun getEngineName(): String = "Anthropic Claude Vision"
    
    override fun isAvailable(): Boolean = apiKey.isNotBlank()
    
    /**
     * Encodes file to base64 for Claude API transmission.
     */
    private fun encodeFileToBase64(file: File): String {
        val bytes = file.readBytes()
        return Base64.getEncoder().encodeToString(bytes)
    }
    
    /**
     * Determines MIME type based on file extension for Claude API.
     */
    private fun determineMimeType(file: File): String {
        return when (file.extension.lowercase()) {
            "jpg", "jpeg" -> "image/jpeg"
            "png" -> "image/png"
            "gif" -> "image/gif"
            "bmp" -> "image/bmp"
            "webp" -> "image/webp"
            else -> "image/jpeg" // Default fallback
        }
    }
    
    /**
     * Calls Claude Messages API with vision capabilities for receipt analysis.
     */
    private suspend fun callClaudeApi(base64Image: String, mimeType: String): String {
        val requestBody = buildClaudeRequestBody(base64Image, mimeType)
        
        return webClient.post()
            .uri(CLAUDE_API_URL)
            .header("x-api-key", apiKey)
            .header("anthropic-version", ANTHROPIC_VERSION)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(requestBody)
            .retrieve()
            .bodyToMono(String::class.java)
            .doOnError { error ->
                logger.error("Claude API call failed", error)
            }
            .block() ?: throw RuntimeException("Empty response from Claude API")
    }
    
    /**
     * Builds the request body for Claude API with receipt analysis prompt.
     */
    private fun buildClaudeRequestBody(base64Image: String, mimeType: String): Map<String, Any> {
        return mapOf(
            "model" to MODEL,
            "max_tokens" to MAX_TOKENS,
            "messages" to listOf(
                mapOf(
                    "role" to "user",
                    "content" to listOf(
                        mapOf(
                            "type" to "image",
                            "source" to mapOf(
                                "type" to "base64",
                                "media_type" to mimeType,
                                "data" to base64Image
                            )
                        ),
                        mapOf(
                            "type" to "text",
                            "text" to buildReceiptAnalysisPrompt()
                        )
                    )
                )
            )
        )
    }
    
    /**
     * Creates a prompt for receipt analysis that requests structured JSON output.
     */
    private fun buildReceiptAnalysisPrompt(): String {
        return """
            Analyze this receipt image and extract the following information in JSON format:
            {
                "provider": "merchant/store name",
                "amount": total_amount_as_number,
                "date": "YYYY-MM-DD",
                "currency": "currency_code"
            }
            
            Instructions:
            - Extract the exact merchant name as it appears on the receipt
            - Use the total amount (including tax if shown)
            - Date should be in YYYY-MM-DD format
            - Currency should be 3-letter code (USD, EUR, etc.)
            - If any field cannot be determined, use null
            - Return only valid JSON, no additional text or explanation
        """.trimIndent()
    }
    
    /**
     * Parses Claude API response and extracts receipt information.
     */
    private fun parseClaudeResponse(response: String, processingTimeMs: Long): OcrResult {
        return try {
            val jsonResponse = objectMapper.readTree(response)
            val content = extractContentFromResponse(jsonResponse)
            
            if (content.isNullOrBlank()) {
                return OcrResult.failure("Empty content in Claude response", response, processingTimeMs)
            }
            
            parseReceiptData(content, response, processingTimeMs)
            
        } catch (e: Exception) {
            logger.error("Error parsing Claude response", e)
            OcrResult.failure("Failed to parse Claude response: ${e.message}", response, processingTimeMs)
        }
    }
    
    /**
     * Extracts the content text from Claude API response structure.
     */
    private fun extractContentFromResponse(jsonResponse: JsonNode): String? {
        val content = jsonResponse.get("content")
        if (content != null && content.isArray && content.size() > 0) {
            val firstContent = content.get(0)
            if (firstContent.get("type")?.asText() == "text") {
                return firstContent.get("text")?.asText()
            }
        }
        return null
    }
    
    /**
     * Parses the extracted content as JSON and creates OcrResult.
     */
    private fun parseReceiptData(content: String, rawResponse: String, processingTimeMs: Long): OcrResult {
        return try {
            // Extract JSON from content if it contains additional text
            val jsonContent = extractJsonFromContent(content)
            val receiptData = objectMapper.readTree(jsonContent)
            
            val provider = receiptData.get("provider")?.asText()
            val amount = receiptData.get("amount")?.asDouble()
            val dateStr = receiptData.get("date")?.asText()
            val currency = receiptData.get("currency")?.asText()
            
            val date = parseDate(dateStr)
            
            OcrResult.success(
                provider = provider,
                amount = amount,
                date = date,
                currency = currency,
                rawJson = rawResponse,
                processingTimeMs = processingTimeMs
            )
            
        } catch (e: Exception) {
            logger.error("Error parsing receipt data from content: $content", e)
            OcrResult.failure("Failed to parse receipt data: ${e.message}", rawResponse, processingTimeMs)
        }
    }
    
    /**
     * Extracts JSON portion from content that might contain additional text.
     */
    private fun extractJsonFromContent(content: String): String {
        // Look for JSON block between curly braces
        val jsonStart = content.indexOf('{')
        val jsonEnd = content.lastIndexOf('}')
        
        return if (jsonStart != -1 && jsonEnd != -1 && jsonEnd > jsonStart) {
            content.substring(jsonStart, jsonEnd + 1)
        } else {
            content // Return as-is if no JSON structure found
        }
    }
    
    /**
     * Safely parses date string into LocalDate with multiple format support.
     */
    private fun parseDate(dateStr: String?): LocalDate? {
        if (dateStr.isNullOrBlank()) return null
        
        val formatters = listOf(
            DateTimeFormatter.ofPattern("yyyy-MM-dd"),
            DateTimeFormatter.ofPattern("MM/dd/yyyy"),
            DateTimeFormatter.ofPattern("dd/MM/yyyy"),
            DateTimeFormatter.ofPattern("yyyy/MM/dd")
        )
        
        for (formatter in formatters) {
            try {
                return LocalDate.parse(dateStr, formatter)
            } catch (e: DateTimeParseException) {
                // Try next formatter
            }
        }
        
        logger.warn("Could not parse date: $dateStr")
        return null
    }
}