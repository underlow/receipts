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
 * Google AI (Gemini) OCR engine implementation using Gemini Vision API for receipt processing.
 * Extracts receipt information from images and returns standardized results.
 */
class GoogleAiOcrEngine(
    private val apiKey: String,
    private val webClient: WebClient,
    private val objectMapper: ObjectMapper
) : OcrEngine {
    
    private val logger = LoggerFactory.getLogger(GoogleAiOcrEngine::class.java)
    
    companion object {
        private const val GOOGLE_AI_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-pro-vision:generateContent"
    }
    
    override suspend fun processFile(file: File): OcrResult {
        val startTime = System.currentTimeMillis()
        
        return try {
            logger.debug("Processing file with Google AI: ${file.name}")
            
            if (!isAvailable()) {
                return OcrResult.failure("Google AI OCR engine is not available", processingTimeMs = System.currentTimeMillis() - startTime)
            }
            
            val base64Image = encodeFileToBase64(file)
            val mimeType = determineMimeType(file)
            val response = callGoogleAiApi(base64Image, mimeType)
            
            val processingTime = System.currentTimeMillis() - startTime
            parseGoogleAiResponse(response, processingTime)
            
        } catch (e: Exception) {
            logger.error("Error processing file with Google AI: ${file.name}", e)
            val processingTime = System.currentTimeMillis() - startTime
            OcrResult.failure("Google AI API error: ${e.message}", processingTimeMs = processingTime)
        }
    }
    
    override fun getEngineName(): String = "Google AI Gemini Vision"
    
    override fun isAvailable(): Boolean = apiKey.isNotBlank()
    
    /**
     * Encodes file to base64 for Google AI API transmission.
     */
    private fun encodeFileToBase64(file: File): String {
        val bytes = file.readBytes()
        return Base64.getEncoder().encodeToString(bytes)
    }
    
    /**
     * Determines MIME type based on file extension for Google AI API.
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
     * Calls Google AI Gemini API with vision capabilities for receipt analysis.
     */
    private suspend fun callGoogleAiApi(base64Image: String, mimeType: String): String {
        val requestBody = buildGoogleAiRequestBody(base64Image, mimeType)
        val url = "$GOOGLE_AI_API_URL?key=$apiKey"
        
        return webClient.post()
            .uri(url)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(requestBody)
            .retrieve()
            .bodyToMono(String::class.java)
            .doOnError { error ->
                logger.error("Google AI API call failed", error)
            }
            .block() ?: throw RuntimeException("Empty response from Google AI API")
    }
    
    /**
     * Builds the request body for Google AI API with receipt analysis prompt.
     */
    private fun buildGoogleAiRequestBody(base64Image: String, mimeType: String): Map<String, Any> {
        return mapOf(
            "contents" to listOf(
                mapOf(
                    "parts" to listOf(
                        mapOf(
                            "text" to buildReceiptAnalysisPrompt()
                        ),
                        mapOf(
                            "inline_data" to mapOf(
                                "mime_type" to mimeType,
                                "data" to base64Image
                            )
                        )
                    )
                )
            ),
            "generationConfig" to mapOf(
                "maxOutputTokens" to 500,
                "temperature" to 0.1
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
     * Parses Google AI API response and extracts receipt information.
     */
    private fun parseGoogleAiResponse(response: String, processingTimeMs: Long): OcrResult {
        return try {
            val jsonResponse = objectMapper.readTree(response)
            val content = extractContentFromResponse(jsonResponse)
            
            if (content.isNullOrBlank()) {
                return OcrResult.failure("Empty content in Google AI response", response, processingTimeMs)
            }
            
            parseReceiptData(content, response, processingTimeMs)
            
        } catch (e: Exception) {
            logger.error("Error parsing Google AI response", e)
            OcrResult.failure("Failed to parse Google AI response: ${e.message}", response, processingTimeMs)
        }
    }
    
    /**
     * Extracts the content text from Google AI API response structure.
     */
    private fun extractContentFromResponse(jsonResponse: JsonNode): String? {
        val candidates = jsonResponse.get("candidates")
        if (candidates != null && candidates.isArray && candidates.size() > 0) {
            val firstCandidate = candidates.get(0)
            val content = firstCandidate.get("content")
            val parts = content?.get("parts")
            if (parts != null && parts.isArray && parts.size() > 0) {
                val firstPart = parts.get(0)
                return firstPart.get("text")?.asText()
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