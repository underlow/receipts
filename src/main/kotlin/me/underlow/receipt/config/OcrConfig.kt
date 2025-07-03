package me.underlow.receipt.config

import com.fasterxml.jackson.databind.ObjectMapper
import me.underlow.receipt.service.ocr.ClaudeOcrEngine
import me.underlow.receipt.service.ocr.GoogleAiOcrEngine
import me.underlow.receipt.service.ocr.OcrEngine
import me.underlow.receipt.service.ocr.OpenAiOcrEngine
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClient

/**
 * Configuration for OCR engines with conditional bean creation based on API key availability.
 * Creates OCR engine instances only when their respective API keys are configured.
 */
@Configuration
class OcrConfig {
    
    private val logger = LoggerFactory.getLogger(OcrConfig::class.java)
    
    /**
     * Creates a WebClient instance for making HTTP requests to OCR APIs.
     */
    @Bean
    fun ocrWebClient(): WebClient {
        return WebClient.builder()
            .codecs { configurer ->
                configurer.defaultCodecs().maxInMemorySize(10 * 1024 * 1024) // 10MB buffer
            }
            .build()
    }
    
    /**
     * Creates OpenAI OCR engine bean when API key is configured.
     */
    @Bean
    @ConditionalOnProperty(
        prefix = "receipts",
        name = ["openai-api-key"],
        havingValue = "",
        matchIfMissing = false
    )
    fun openAiOcrEngine(
        receiptsProperties: ReceiptsProperties,
        webClient: WebClient,
        objectMapper: ObjectMapper
    ): OpenAiOcrEngine? {
        val apiKey = receiptsProperties.openaiApiKey
        
        return if (!apiKey.isNullOrBlank() && apiKey != "openaiApiKey") {
            logger.info("Configuring OpenAI OCR engine")
            OpenAiOcrEngine(apiKey, webClient, objectMapper)
        } else {
            logger.debug("OpenAI API key not configured, skipping OpenAI OCR engine")
            null
        }
    }
    
    /**
     * Creates Claude OCR engine bean when API key is configured.
     */
    @Bean
    @ConditionalOnProperty(
        prefix = "receipts",
        name = ["claude-api-key"],
        havingValue = "",
        matchIfMissing = false
    )
    fun claudeOcrEngine(
        receiptsProperties: ReceiptsProperties,
        webClient: WebClient,
        objectMapper: ObjectMapper
    ): ClaudeOcrEngine? {
        val apiKey = receiptsProperties.claudeApiKey
        
        return if (!apiKey.isNullOrBlank() && apiKey != "claudeApiKey") {
            logger.info("Configuring Claude OCR engine")
            ClaudeOcrEngine(apiKey, webClient, objectMapper)
        } else {
            logger.debug("Claude API key not configured, skipping Claude OCR engine")
            null
        }
    }
    
    /**
     * Creates Google AI OCR engine bean when API key is configured.
     */
    @Bean
    @ConditionalOnProperty(
        prefix = "receipts",
        name = ["google-ai-api-key"],
        havingValue = "",
        matchIfMissing = false
    )
    fun googleAiOcrEngine(
        receiptsProperties: ReceiptsProperties,
        webClient: WebClient,
        objectMapper: ObjectMapper
    ): GoogleAiOcrEngine? {
        val apiKey = receiptsProperties.googleAiApiKey
        
        return if (!apiKey.isNullOrBlank() && apiKey != "googleAiApiKey") {
            logger.info("Configuring Google AI OCR engine")
            GoogleAiOcrEngine(apiKey, webClient, objectMapper)
        } else {
            logger.debug("Google AI API key not configured, skipping Google AI OCR engine")
            null
        }
    }
    
    /**
     * Creates a list of available OCR engines for runtime selection.
     * Only includes engines that are properly configured with API keys.
     */
    @Bean
    fun availableOcrEngines(
        openAiOcrEngine: OpenAiOcrEngine?,
        claudeOcrEngine: ClaudeOcrEngine?,
        googleAiOcrEngine: GoogleAiOcrEngine?
    ): List<OcrEngine> {
        val engines = mutableListOf<OcrEngine>()
        
        openAiOcrEngine?.let { engines.add(it) }
        claudeOcrEngine?.let { engines.add(it) }
        googleAiOcrEngine?.let { engines.add(it) }
        
        logger.info("Available OCR engines: ${engines.map { it.getEngineName() }}")
        
        if (engines.isEmpty()) {
            logger.warn("No OCR engines configured! Please configure at least one API key.")
        }
        
        return engines
    }
}