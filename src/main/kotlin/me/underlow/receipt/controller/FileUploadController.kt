package me.underlow.receipt.controller

import me.underlow.receipt.dto.ErrorResponse
import me.underlow.receipt.dto.FileUploadResponse
import me.underlow.receipt.service.FileProcessingService
import me.underlow.receipt.service.UserService
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.security.Principal

/**
 * REST controller for handling file uploads via web interface.
 * Provides multipart file upload endpoint for authenticated users.
 */
@RestController
@RequestMapping("/api/files")
class FileUploadController(
    private val fileProcessingService: FileProcessingService,
    private val userService: UserService
) {
    private val logger = LoggerFactory.getLogger(FileUploadController::class.java)

    /**
     * Handles multipart file uploads from authenticated users.
     * Validates file, saves to storage, and creates IncomingFile entity.
     */
    @PostMapping("/upload")
    fun uploadFile(
        @RequestParam("file") file: MultipartFile,
        principal: Principal
    ): ResponseEntity<Any> {
        return try {
            logger.info("Received file upload request: ${file.originalFilename}")
            
            // Validate authentication
            val userId = extractUserIdFromPrincipal(principal)
                ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse(message = "User not authenticated"))
            
            // Validate file
            val validationError = validateUploadedFile(file)
            if (validationError != null) {
                return ResponseEntity.badRequest().body(validationError)
            }
            
            // Save file to temporary location for processing
            val tempFile = saveToTempFile(file)
            
            try {
                // Process file using existing service
                val incomingFile = fileProcessingService.processFile(tempFile, userId)
                
                if (incomingFile != null) {
                    val response = FileUploadResponse(
                        id = incomingFile.id!!,
                        filename = incomingFile.filename,
                        uploadDate = incomingFile.uploadDate,
                        status = incomingFile.status,
                        checksum = incomingFile.checksum,
                        message = "File uploaded successfully"
                    )
                    
                    logger.info("Successfully processed uploaded file: ${file.originalFilename} for user: $userId")
                    ResponseEntity.ok(response)
                } else {
                    // File processing failed (likely duplicate)
                    ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(ErrorResponse(
                            message = "File already exists or processing failed",
                            code = "DUPLICATE_FILE"
                        ))
                }
                
            } finally {
                // Clean up temporary file
                tempFile.delete()
            }
            
        } catch (e: Exception) {
            logger.error("Error processing file upload: ${file.originalFilename}", e)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorResponse(
                    message = "Internal server error during file upload",
                    code = "INTERNAL_ERROR"
                ))
        }
    }

    /**
     * Extracts user ID from OAuth2 authentication principal.
     */
    private fun extractUserIdFromPrincipal(principal: Principal): Long? {
        return when (principal) {
            is OAuth2AuthenticationToken -> {
                val email = principal.principal.attributes["email"] as? String
                email?.let { userService.findByEmail(it)?.id }
            }
            else -> null
        }
    }

    /**
     * Validates uploaded file for size, type, and content requirements.
     */
    private fun validateUploadedFile(file: MultipartFile): ErrorResponse? {
        // Check if file is empty
        if (file.isEmpty) {
            return ErrorResponse(
                message = "File cannot be empty",
                code = "EMPTY_FILE"
            )
        }
        
        // Check file size (10MB limit by default)
        val maxSize = 10 * 1024 * 1024L // 10MB
        if (file.size > maxSize) {
            return ErrorResponse(
                message = "File size exceeds maximum limit of ${maxSize / (1024 * 1024)}MB",
                code = "FILE_TOO_LARGE",
                details = mapOf("maxSize" to maxSize, "actualSize" to file.size)
            )
        }
        
        // Check file extension
        val originalFilename = file.originalFilename ?: ""
        val supportedExtensions = listOf("pdf", "jpg", "jpeg", "png", "gif", "bmp", "tiff")
        val fileExtension = originalFilename.substringAfterLast(".", "").lowercase()
        
        if (fileExtension.isBlank() || fileExtension !in supportedExtensions) {
            return ErrorResponse(
                message = "Unsupported file type. Supported types: ${supportedExtensions.joinToString(", ")}",
                code = "UNSUPPORTED_FILE_TYPE",
                details = mapOf("supportedTypes" to supportedExtensions, "actualType" to fileExtension)
            )
        }
        
        return null
    }

    /**
     * Saves multipart file to temporary location for processing.
     */
    private fun saveToTempFile(file: MultipartFile): File {
        val tempDir = Files.createTempDirectory("upload-")
        val tempFile = tempDir.resolve(file.originalFilename ?: "uploaded-file").toFile()
        
        file.transferTo(tempFile)
        
        logger.debug("Saved uploaded file to temporary location: ${tempFile.absolutePath}")
        return tempFile
    }
}