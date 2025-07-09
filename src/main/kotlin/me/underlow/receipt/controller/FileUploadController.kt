package me.underlow.receipt.controller

import me.underlow.receipt.service.FileUploadService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

/**
 * REST controller for handling file upload requests.
 * Provides endpoint for multipart file uploads with validation and error handling.
 */
@RestController
@RequestMapping("/api")
class FileUploadController(
    private val fileUploadService: FileUploadService
) {
    
    /**
     * Handles file upload requests via multipart form data.
     * Validates uploaded files and stores them securely with unique filenames.
     * Returns JSON response with upload status and file path information.
     *
     * @param file MultipartFile from the upload request
     * @return ResponseEntity with UploadResponse containing success status and file path
     */
    @PostMapping("/upload")
    fun uploadFile(@RequestParam("file") file: MultipartFile?): ResponseEntity<UploadResponse> {
        return try {
            // Check if file parameter is present
            if (file == null) {
                return ResponseEntity.badRequest().body(
                    UploadResponse(
                        success = false,
                        filePath = null,
                        message = "Missing file parameter",
                        error = "File parameter is required"
                    )
                )
            }
            
            // Validate file using service
            if (!fileUploadService.validateFile(file)) {
                return ResponseEntity.badRequest().body(
                    UploadResponse(
                        success = false,
                        filePath = null,
                        message = "Invalid file",
                        error = "Only JPEG, PNG, GIF, WebP files are allowed"
                    )
                )
            }
            
            // Save file using service
            val filePath = fileUploadService.saveFile(file)
            
            // Return success response
            ResponseEntity.ok(
                UploadResponse(
                    success = true,
                    filePath = filePath,
                    message = "File uploaded successfully",
                    error = null
                )
            )
            
        } catch (e: Exception) {
            // Handle any exceptions during upload process
            ResponseEntity.internalServerError().body(
                UploadResponse(
                    success = false,
                    filePath = null,
                    message = "File upload failed",
                    error = e.message ?: "Unknown error occurred"
                )
            )
        }
    }
    
    /**
     * Exception handler for handling multipart file upload errors.
     * Provides user-friendly error messages for common multipart failures.
     *
     * @param e Exception that occurred during multipart processing
     * @return ResponseEntity with error response
     */
    @ExceptionHandler(org.springframework.web.multipart.MaxUploadSizeExceededException::class)
    fun handleMaxSizeException(e: org.springframework.web.multipart.MaxUploadSizeExceededException): ResponseEntity<UploadResponse> {
        return ResponseEntity.badRequest().body(
            UploadResponse(
                success = false,
                filePath = null,
                message = "File too large",
                error = "File size exceeds maximum allowed size of 20MB"
            )
        )
    }
    
    /**
     * Exception handler for handling missing servlet request part errors.
     * Provides user-friendly error messages when required file parameter is missing.
     *
     * @param e Exception that occurred during request processing
     * @return ResponseEntity with error response
     */
    @ExceptionHandler(org.springframework.web.multipart.support.MissingServletRequestPartException::class)
    fun handleMissingServletRequestPartException(e: org.springframework.web.multipart.support.MissingServletRequestPartException): ResponseEntity<UploadResponse> {
        return ResponseEntity.badRequest().body(
            UploadResponse(
                success = false,
                filePath = null,
                message = "Missing file parameter",
                error = "File parameter is required"
            )
        )
    }
}

/**
 * Data class representing the response from file upload endpoint.
 * Provides structured response with success status, file path, and error information.
 */
data class UploadResponse(
    val success: Boolean,
    val filePath: String?,
    val message: String,
    val error: String?
)