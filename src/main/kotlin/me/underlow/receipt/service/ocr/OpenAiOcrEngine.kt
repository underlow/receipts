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
 * OpenAI OCR engine implementation using GPT-4 Vision API for receipt processing.
 * Extracts receipt information from images and returns standardized results.
 */
class OpenAiOcrEngine(
    private val apiKey: String,
    private val webClient: WebClient,
    private val objectMapper: ObjectMapper
) : OcrEngine {
    
    private val logger = LoggerFactory.getLogger(OpenAiOcrEngine::class.java)
    
    companion object {
        private const val OPENAI_API_URL = "https://api.openai.com/v1/chat/completions"
        private const val MODEL = "gpt-4-vision-preview"
        private const val MAX_TOKENS = 500
    }
    
    override suspend fun processFile(file: File): OcrResult {
        val startTime = System.currentTimeMillis()
        
        return try {
            logger.debug("Processing file with OpenAI: ${file.name}")
            
            if (!isAvailable()) {
                return OcrResult.failure("OpenAI OCR engine is not available", processingTimeMs = System.currentTimeMillis() - startTime)
            }
            
            val base64Image = encodeFileToBase64(file)
            val response = callOpenAiApi(base64Image)
            
            val processingTime = System.currentTimeMillis() - startTime
            parseOpenAiResponse(response, processingTime)
            
        } catch (e: Exception) {
            logger.error("Error processing file with OpenAI: ${file.name}", e)
            val processingTime = System.currentTimeMillis() - startTime
            OcrResult.failure("OpenAI API error: ${e.message}", processingTimeMs = processingTime)
        }
    }
    
    override fun getEngineName(): String = "OpenAI GPT-4 Vision"
    
    override fun isAvailable(): Boolean = apiKey.isNotBlank()
    
    /**
     * Encodes file to base64 for OpenAI API transmission.
     */
    private fun encodeFileToBase64(file: File): String {
        val bytes = file.readBytes()
        return Base64.getEncoder().encodeToString(bytes)
    }
    
    /**
     * Calls OpenAI Chat Completions API with vision capabilities for receipt analysis.
     */
    private suspend fun callOpenAiApi(base64Image: String): String {
        val requestBody = buildOpenAiRequestBody(base64Image)
        
        return webClient.post()
            .uri(OPENAI_API_URL)
            .header(HttpHeaders.AUTHORIZATION, "Bearer $apiKey")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(requestBody)
            .retrieve()
            .bodyToMono(String::class.java)
            .doOnError { error ->
                logger.error("OpenAI API call failed", error)
            }
            .block() ?: throw RuntimeException("Empty response from OpenAI API")
    }
    
    /**
     * Builds the request body for OpenAI API with receipt analysis prompt.
     */
    private fun buildOpenAiRequestBody(base64Image: String): Map<String, Any> {
        return mapOf(
            "model" to MODEL,
            "messages" to listOf(
                mapOf(
                    "role" to "user",
                    "content" to listOf(
                        mapOf(
                            "type" to "text",
                            "text" to buildReceiptAnalysisPrompt()
                        ),
                        mapOf(
                            "type" to "image_url",
                            "image_url" to mapOf(
                                "url" to "data:image/jpeg;base64,$base64Image"
                            )
                        )
                    )
                )
            ),
            "max_tokens" to MAX_TOKENS
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
            - Extract the exact merchant name as it appears
            - Use the total amount (including tax if shown)
            - Date should be in YYYY-MM-DD format
            - Currency should be 3-letter code (USD, EUR, etc.)
            - If any field cannot be determined, use null
            - Return only valid JSON, no additional text
        """.trimIndent()
    }
    
    /**
     * Parses OpenAI API response and extracts receipt information.
     */
    private fun parseOpenAiResponse(response: String, processingTimeMs: Long): OcrResult {
        return try {
            val jsonResponse = objectMapper.readTree(response)
            val content = extractContentFromResponse(jsonResponse)
            
            if (content.isNullOrBlank()) {
                return OcrResult.failure("Empty content in OpenAI response", response, processingTimeMs)
            }
            
            parseReceiptData(content, response, processingTimeMs)
            
        } catch (e: Exception) {
            logger.error("Error parsing OpenAI response", e)
            OcrResult.failure("Failed to parse OpenAI response: ${e.message}", response, processingTimeMs)
        }
    }
    
    /**
     * Extracts the content text from OpenAI API response structure.
     */
    private fun extractContentFromResponse(jsonResponse: JsonNode): String? {
        return jsonResponse
            .get("choices")
            ?.get(0)
            ?.get("message")
            ?.get("content")
            ?.asText()
    }
    
    /**
     * Parses the extracted content as JSON and creates OcrResult.
     */
    private fun parseReceiptData(content: String, rawResponse: String, processingTimeMs: Long): OcrResult {
        return try {
            val receiptData = objectMapper.readTree(content)
            
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