package me.underlow.receipt.service

import me.underlow.receipt.model.IncomingFile
import me.underlow.receipt.model.EntityType
import me.underlow.receipt.model.OcrProcessingStatus
import me.underlow.receipt.service.ocr.OcrEngine
import me.underlow.receipt.service.ocr.OcrResult
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.io.File

/**
 * Service responsible for orchestrating OCR processing using available OCR engines.
 * Provides fallback mechanisms and engine selection strategies.
 */
@Service
class OcrService(
    private val availableOcrEngines: List<OcrEngine>,
    private val ocrAttemptService: OcrAttemptService
) {
    
    private val logger = LoggerFactory.getLogger(OcrService::class.java)
    
    /**
     * Processes an IncomingFile using the first available OCR engine.
     * Returns OCR result with extracted receipt information.
     */
    suspend fun processIncomingFile(incomingFile: IncomingFile, userEmail: String): OcrResult {
        val file = File(incomingFile.filePath)
        
        logger.info("Processing incoming file: ${incomingFile.filename} (${incomingFile.filePath})")
        
        if (availableOcrEngines.isEmpty()) {
            logger.warn("No OCR engines available for processing file: ${incomingFile.filename}")
            return OcrResult.failure("No OCR engines available")
        }
        
        if (!file.exists()) {
            logger.error("File not found on disk: ${incomingFile.filePath}")
            return OcrResult.failure("File not found on disk: ${incomingFile.filePath}")
        }
        
        logger.info("File exists, processing with available engines: ${availableOcrEngines.map { it.getEngineName() }}")
        
        return processEntityWithOcrTracking(
            EntityType.INCOMING_FILE,
            incomingFile.id!!,
            userEmail,
            file
        )
    }
    
    /**
     * Processes a file using the first available OCR engine.
     * In the future, this could implement more sophisticated engine selection.
     */
    suspend fun processFile(file: File): OcrResult {
        return if (availableOcrEngines.isEmpty()) {
            logger.warn("No OCR engines available for processing file: ${file.name}")
            OcrResult.failure("No OCR engines available")
        } else {
            processFileWithPrimaryEngine(file)
        }
    }
    
    /**
     * Processes file with fallback mechanism - tries engines in order until one succeeds.
     */
    suspend fun processFileWithFallback(file: File): OcrResult {
        if (availableOcrEngines.isEmpty()) {
            logger.warn("No OCR engines available for processing file: ${file.name}")
            return OcrResult.failure("No OCR engines available")
        }
        
        var lastError: String? = null
        
        for (engine in availableOcrEngines) {
            if (!engine.isAvailable()) {
                logger.debug("Skipping unavailable engine: ${engine.getEngineName()}")
                continue
            }
            
            try {
                logger.info("Attempting OCR processing with engine: ${engine.getEngineName()}")
                val result = engine.processFile(file)
                
                if (result.success) {
                    logger.info("Successfully processed file ${file.name} with ${engine.getEngineName()}")
                    return result
                } else {
                    logger.warn("OCR processing failed with ${engine.getEngineName()}: ${result.errorMessage}")
                    lastError = result.errorMessage
                }
                
            } catch (e: Exception) {
                logger.error("Exception during OCR processing with ${engine.getEngineName()}", e)
                lastError = "Engine error: ${e.message}"
            }
        }
        
        return OcrResult.failure("All OCR engines failed. Last error: $lastError")
    }
    
    /**
     * Returns list of available OCR engine names for configuration/status purposes.
     */
    fun getAvailableEngineNames(): List<String> {
        return availableOcrEngines
            .filter { it.isAvailable() }
            .map { it.getEngineName() }
    }
    
    /**
     * Returns true if at least one OCR engine is available.
     */
    fun hasAvailableEngines(): Boolean {
        return availableOcrEngines.any { it.isAvailable() }
    }
    
    /**
     * Processes file with the primary (first) available engine.
     */
    private suspend fun processFileWithPrimaryEngine(file: File): OcrResult {
        val primaryEngine = availableOcrEngines.firstOrNull { it.isAvailable() }
        
        return if (primaryEngine != null) {
            try {
                logger.info("Processing file ${file.name} with primary OCR engine: ${primaryEngine.getEngineName()}")
                primaryEngine.processFile(file)
            } catch (e: Exception) {
                logger.error("Error processing file with primary OCR engine", e)
                OcrResult.failure("Primary OCR engine failed: ${e.message}")
            }
        } else {
            OcrResult.failure("No available OCR engines")
        }
    }
    
    /**
     * Processes an entity file with OCR tracking for audit and history purposes
     */
    suspend fun processEntityWithOcrTracking(
        entityType: EntityType,
        entityId: Long,
        userEmail: String,
        file: File
    ): OcrResult {
        val primaryEngine = availableOcrEngines.firstOrNull { it.isAvailable() }
        
        if (primaryEngine == null) {
            val errorMessage = "No available OCR engines"
            ocrAttemptService.recordOcrAttempt(
                entityType = entityType,
                entityId = entityId,
                userEmail = userEmail,
                ocrEngineUsed = "NONE",
                processingStatus = OcrProcessingStatus.FAILED,
                errorMessage = errorMessage
            )
            return OcrResult.failure(errorMessage)
        }
        
        // Record the start of OCR processing
        val attempt = ocrAttemptService.recordOcrAttempt(
            entityType = entityType,
            entityId = entityId,
            userEmail = userEmail,
            ocrEngineUsed = primaryEngine.getEngineName(),
            processingStatus = OcrProcessingStatus.IN_PROGRESS
        )
        
        return try {
            logger.info("Processing file ${file.name} with OCR engine: ${primaryEngine.getEngineName()}")
            val result = primaryEngine.processFile(file)
            
            // Update the attempt with the result
            if (attempt != null) {
                ocrAttemptService.updateAttemptStatus(
                    attemptId = attempt.id!!,
                    processingStatus = if (result.success) OcrProcessingStatus.SUCCESS else OcrProcessingStatus.FAILED,
                    extractedDataJson = result.rawJson,
                    errorMessage = if (!result.success) result.errorMessage else null
                )
            }
            
            logger.info("OCR processing completed for file ${file.name} with status: ${if (result.success) "SUCCESS" else "FAILED"}")
            result
            
        } catch (e: Exception) {
            val errorMessage = "OCR engine failed: ${e.message}"
            logger.error("Error processing file with OCR engine", e)
            
            // Update the attempt with failure
            if (attempt != null) {
                ocrAttemptService.updateAttemptStatus(
                    attemptId = attempt.id!!,
                    processingStatus = OcrProcessingStatus.FAILED,
                    errorMessage = errorMessage
                )
            }
            
            OcrResult.failure(errorMessage)
        }
    }
    
    /**
     * Processes Bill entity with OCR tracking
     */
    suspend fun processBill(billId: Long, userEmail: String, filePath: String): OcrResult {
        val file = File(filePath)
        return processEntityWithOcrTracking(EntityType.BILL, billId, userEmail, file)
    }
    
    /**
     * Processes Receipt entity with OCR tracking
     */
    suspend fun processReceipt(receiptId: Long, userEmail: String, filePath: String): OcrResult {
        val file = File(filePath)
        return processEntityWithOcrTracking(EntityType.RECEIPT, receiptId, userEmail, file)
    }
}