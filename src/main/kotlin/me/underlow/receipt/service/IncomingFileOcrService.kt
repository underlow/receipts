package me.underlow.receipt.service

import me.underlow.receipt.model.IncomingFile
import me.underlow.receipt.model.BillStatus
import me.underlow.receipt.repository.IncomingFileRepository
import me.underlow.receipt.service.ocr.OcrResult
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import kotlinx.coroutines.runBlocking

/**
 * Service responsible for orchestrating OCR processing for IncomingFile entities.
 * Handles status management, result persistence, and error handling during OCR processing.
 */
@Service
class IncomingFileOcrService(
    private val incomingFileRepository: IncomingFileRepository,
    private val ocrService: OcrService
) {
    
    private val logger = LoggerFactory.getLogger(IncomingFileOcrService::class.java)
    
    /**
     * Processes an IncomingFile through OCR and updates the entity with results.
     * Updates status to PROCESSING during processing and APPROVED/REJECTED based on result.
     */
    fun processIncomingFile(incomingFile: IncomingFile, userEmail: String): IncomingFile {
        logger.info("Starting OCR processing for file: ${incomingFile.filename}")
        
        // Check if OCR is available before processing
        if (!ocrService.hasAvailableEngines()) {
            logger.error("No OCR engines available for processing file: ${incomingFile.filename}")
            val errorFile = incomingFile.copy(
                status = BillStatus.REJECTED,
                ocrProcessedAt = LocalDateTime.now(),
                ocrErrorMessage = "No OCR engines available. Please configure at least one API key (OpenAI, Claude, or Google AI)."
            )
            return incomingFileRepository.save(errorFile)
        }
        
        // Update status to PROCESSING
        val processingFile = incomingFile.copy(status = BillStatus.PROCESSING)
        incomingFileRepository.save(processingFile)
        
        return try {
            logger.info("Processing file ${incomingFile.filename} with available OCR engines: ${ocrService.getAvailableEngineNames()}")
            
            // Process file through OCR
            val ocrResult = runBlocking {
                ocrService.processIncomingFile(incomingFile, userEmail)
            }
            
            logger.info("OCR processing completed for file ${incomingFile.filename}. Success: ${ocrResult.success}")
            
            // Update IncomingFile with OCR results
            val updatedFile = updateIncomingFileWithOcrResult(processingFile, ocrResult)
            
            // Save updated file
            val savedFile = incomingFileRepository.save(updatedFile)
            
            if (ocrResult.success) {
                logger.info("Successfully processed file ${incomingFile.filename} with OCR. Extracted: provider=${ocrResult.extractedProvider}, amount=${ocrResult.extractedAmount}, date=${ocrResult.extractedDate}")
            } else {
                logger.warn("OCR processing failed for file ${incomingFile.filename}: ${ocrResult.errorMessage}")
            }
            
            savedFile
            
        } catch (e: Exception) {
            logger.error("Exception during OCR processing for file ${incomingFile.filename}", e)
            
            // Update with error information
            val errorFile = processingFile.copy(
                status = BillStatus.REJECTED,
                ocrProcessedAt = LocalDateTime.now(),
                ocrErrorMessage = "OCR processing failed: ${e.message}"
            )
            
            incomingFileRepository.save(errorFile)
        }
    }
    
    /**
     * Processes all pending IncomingFiles through OCR.
     * Used for batch processing of files that haven't been processed yet.
     * NOTE: This method is deprecated as OCR tracking requires userEmail.
     */
    @Deprecated("Use user-specific OCR processing instead")
    fun processAllPendingFiles(): List<IncomingFile> {
        logger.warn("processAllPendingFiles is deprecated - OCR tracking requires userEmail")
        return emptyList()
    }
    
    /**
     * Retries OCR processing for a failed file.
     * Resets the file to PENDING status and processes again.
     */
    fun retryOcrProcessing(incomingFileId: Long, userEmail: String): IncomingFile? {
        val incomingFile = incomingFileRepository.findById(incomingFileId)
        
        return if (incomingFile != null) {
            logger.info("Retrying OCR processing for file: ${incomingFile.filename}")
            
            // Check if OCR is available before retrying
            if (!ocrService.hasAvailableEngines()) {
                logger.error("Cannot retry OCR processing - no OCR engines available for file: ${incomingFile.filename}")
                val errorFile = incomingFile.copy(
                    status = BillStatus.REJECTED,
                    ocrProcessedAt = LocalDateTime.now(),
                    ocrErrorMessage = "No OCR engines available. Please configure at least one API key (OpenAI, Claude, or Google AI)."
                )
                return incomingFileRepository.save(errorFile)
            }
            
            // Reset file to PENDING status and clear previous OCR results
            val resetFile = incomingFile.copy(
                status = BillStatus.PENDING,
                ocrRawJson = null,
                extractedAmount = null,
                extractedDate = null,
                extractedProvider = null,
                ocrProcessedAt = null,
                ocrErrorMessage = null
            )
            
            val savedResetFile = incomingFileRepository.save(resetFile)
            processIncomingFile(savedResetFile, userEmail)
        } else {
            logger.warn("Cannot retry OCR processing - file not found: $incomingFileId")
            null
        }
    }
    
    /**
     * Checks if OCR processing is available (at least one OCR engine is available).
     */
    fun isOcrProcessingAvailable(): Boolean {
        return ocrService.hasAvailableEngines()
    }
    
    /**
     * Returns list of available OCR engine names for status/configuration purposes.
     */
    fun getAvailableOcrEngines(): List<String> {
        return ocrService.getAvailableEngineNames()
    }
    
    /**
     * Updates an IncomingFile with OCR processing results.
     */
    private fun updateIncomingFileWithOcrResult(incomingFile: IncomingFile, ocrResult: OcrResult): IncomingFile {
        return incomingFile.copy(
            status = if (ocrResult.success) BillStatus.APPROVED else BillStatus.REJECTED,
            ocrRawJson = ocrResult.rawJson,
            extractedAmount = ocrResult.extractedAmount,
            extractedDate = ocrResult.extractedDate,
            extractedProvider = ocrResult.extractedProvider,
            ocrProcessedAt = LocalDateTime.now(),
            ocrErrorMessage = ocrResult.errorMessage
        )
    }
}