package me.underlow.receipt.controller

import me.underlow.receipt.dto.FileOperationResponse
import me.underlow.receipt.dto.InboxFileDto
import me.underlow.receipt.dto.InboxListResponse
import me.underlow.receipt.dto.IncomingFileDetailDto
import me.underlow.receipt.model.ItemStatus
import me.underlow.receipt.service.IncomingFileService
import me.underlow.receipt.service.EntityConversionService
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*

/**
 * Web and API controller for inbox functionality
 */
@Controller
@RequestMapping("/inbox")
class InboxController(
    private val incomingFileService: IncomingFileService,
    private val entityConversionService: EntityConversionService
) {

    private val logger = LoggerFactory.getLogger(InboxController::class.java)

    /**
     * Shows the inbox list page
     */
    @GetMapping
    fun showInbox(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
        @RequestParam(defaultValue = "") status: String,
        @RequestParam(defaultValue = "uploadDate") sortBy: String,
        @RequestParam(defaultValue = "desc") sortDirection: String,
        authentication: OAuth2AuthenticationToken,
        model: Model
    ): String {
        logger.info("Showing inbox for page: $page, size: $size, status: '$status', sortBy: $sortBy, sortDirection: $sortDirection")
        val userEmail = authentication.principal.getAttribute<String>("email")
        if (userEmail == null) {
            logger.warn("Unauthorized access attempt to inbox. User email not found.")
            return "redirect:/login"
        }
        val userName = authentication.principal.getAttribute<String>("name") ?: "Unknown User"

        // Parse status filter
        val statusFilter = if (status.isBlank()) null else try {
            ItemStatus.valueOf(status.uppercase())
        } catch (e: IllegalArgumentException) {
            logger.warn("Invalid status filter: '$status'")
            null
        }

        // Get paginated files
        val (files, totalFiles) = incomingFileService.findByUserEmailWithPagination(
            userEmail, statusFilter, page, size, sortBy, sortDirection
        )

        // Get status counts for filter badges
        val statusCounts = incomingFileService.getFileStatistics(userEmail)

        // Calculate pagination info
        val totalPages = if (totalFiles > 0) ((totalFiles - 1) / size + 1).toInt() else 0

        // Convert to DTOs
        val fileDtos = files.map { InboxFileDto.fromIncomingFile(it) }

        // Add data to model
        model.addAttribute("userEmail", userEmail)
        model.addAttribute("userName", userName)
        model.addAttribute("files", fileDtos)
        model.addAttribute("currentPage", page)
        model.addAttribute("totalPages", totalPages)
        model.addAttribute("totalFiles", totalFiles)
        model.addAttribute("pageSize", size)
        model.addAttribute("selectedStatus", status)
        model.addAttribute("sortBy", sortBy)
        model.addAttribute("sortDirection", sortDirection)
        model.addAttribute("statusCounts", statusCounts)

        logger.info("Successfully showed inbox for user: $userEmail. Found $totalFiles files.")
        return "inbox"
    }

    /**
     * API endpoint to get inbox data (for AJAX requests)
     */
    @GetMapping("/api/list")
    @ResponseBody
    fun getInboxList(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
        @RequestParam(defaultValue = "") status: String,
        @RequestParam(defaultValue = "uploadDate") sortBy: String,
        @RequestParam(defaultValue = "desc") sortDirection: String,
        authentication: OAuth2AuthenticationToken
    ): ResponseEntity<InboxListResponse> {
        logger.debug("Fetching inbox list via API for page: $page, size: $size, status: '$status'")
        val userEmail = authentication.principal.getAttribute<String>("email")
        if (userEmail == null) {
            logger.warn("Unauthorized API access attempt to inbox list. User email not found.")
            return ResponseEntity.badRequest().build()
        }

        val statusFilter = if (status.isBlank()) null else try {
            ItemStatus.valueOf(status.uppercase())
        } catch (e: IllegalArgumentException) {
            logger.warn("Invalid status filter for API inbox list: '$status'")
            null
        }

        val (files, totalFiles) = incomingFileService.findByUserEmailWithPagination(
            userEmail, statusFilter, page, size, sortBy, sortDirection
        )

        val statusCounts = incomingFileService.getFileStatistics(userEmail)
        val totalPages = if (totalFiles > 0) ((totalFiles - 1) / size + 1).toInt() else 0
        val fileDtos = files.map { InboxFileDto.fromIncomingFile(it) }

        val response = InboxListResponse(
            files = fileDtos,
            totalFiles = totalFiles,
            currentPage = page,
            totalPages = totalPages,
            pageSize = size,
            statusCounts = statusCounts.mapKeys { it.key.name.lowercase() }
        )

        logger.debug("Successfully fetched inbox list via API for user: $userEmail. Found $totalFiles files.")
        return ResponseEntity.ok(response)
    }

    /**
     * API endpoint to approve a file
     */
    @PostMapping("/api/files/{fileId}/approve")
    @ResponseBody
    fun approveFile(
        @PathVariable fileId: Long,
        authentication: OAuth2AuthenticationToken
    ): ResponseEntity<FileOperationResponse> {
        logger.info("Attempting to approve file with ID: $fileId")
        val userEmail = authentication.principal.getAttribute<String>("email")
        if (userEmail == null) {
            logger.warn("Unauthorized access attempt to approve file. User email not found.")
            return ResponseEntity.badRequest().body(
                FileOperationResponse(false, "User not authenticated")
            )
        }

        val success = incomingFileService.updateStatus(fileId, userEmail, ItemStatus.APPROVED)
        val response = if (success) {
            logger.info("File $fileId approved successfully by user: $userEmail")
            FileOperationResponse(true, "File approved successfully", fileId)
        } else {
            logger.error("Failed to approve file $fileId for user: $userEmail or file not found.")
            FileOperationResponse(false, "Failed to approve file or file not found")
        }

        return ResponseEntity.ok(response)
    }

    /**
     * API endpoint to reject a file
     */
    @PostMapping("/api/files/{fileId}/reject")
    @ResponseBody
    fun rejectFile(
        @PathVariable fileId: Long,
        authentication: OAuth2AuthenticationToken
    ): ResponseEntity<FileOperationResponse> {
        logger.info("Attempting to reject file with ID: $fileId")
        val userEmail = authentication.principal.getAttribute<String>("email")
        if (userEmail == null) {
            logger.warn("Unauthorized access attempt to reject file. User email not found.")
            return ResponseEntity.badRequest().body(
                FileOperationResponse(false, "User not authenticated")
            )
        }

        val success = incomingFileService.updateStatus(fileId, userEmail, ItemStatus.REJECTED)
        val response = if (success) {
            logger.info("File $fileId rejected successfully by user: $userEmail")
            FileOperationResponse(true, "File rejected successfully", fileId)
        } else {
            logger.error("Failed to reject file $fileId for user: $userEmail or file not found.")
            FileOperationResponse(false, "Failed to reject file or file not found")
        }

        return ResponseEntity.ok(response)
    }

    /**
     * API endpoint to delete a file
     */
    @DeleteMapping("/api/files/{fileId}")
    @ResponseBody
    fun deleteFile(
        @PathVariable fileId: Long,
        authentication: OAuth2AuthenticationToken
    ): ResponseEntity<FileOperationResponse> {
        logger.info("Attempting to delete file with ID: $fileId")
        val userEmail = authentication.principal.getAttribute<String>("email")
        if (userEmail == null) {
            logger.warn("Unauthorized access attempt to delete file. User email not found.")
            return ResponseEntity.badRequest().body(
                FileOperationResponse(false, "User not authenticated")
            )
        }

        val success = incomingFileService.deleteFile(fileId, userEmail)
        val response = if (success) {
            logger.info("File $fileId deleted successfully by user: $userEmail")
            FileOperationResponse(true, "File deleted successfully", fileId)
        } else {
            logger.error("Failed to delete file $fileId for user: $userEmail or file not found.")
            FileOperationResponse(false, "Failed to delete file or file not found")
        }

        return ResponseEntity.ok(response)
    }

    /**
     * Shows the detailed view for a specific file
     */
    @GetMapping("/files/{fileId}")
    fun showFileDetail(
        @PathVariable fileId: Long,
        authentication: OAuth2AuthenticationToken,
        model: Model
    ): String {
        logger.info("Showing file detail for fileId: $fileId")
        val userEmail = authentication.principal.getAttribute<String>("email")
        if (userEmail == null) {
            logger.warn("Unauthorized access attempt to file detail. User email not found.")
            return "redirect:/login"
        }
        val userName = authentication.principal.getAttribute<String>("name") ?: "Unknown User"

        val incomingFile = incomingFileService.findByIdAndUserEmail(fileId, userEmail)
        if (incomingFile == null) {
            logger.warn("Incoming file with ID $fileId not found for user $userEmail or user does not own it.")
            return "redirect:/inbox?error=file_not_found"
        }

        val fileDetailDto = IncomingFileDetailDto.fromIncomingFile(incomingFile)

        model.addAttribute("userEmail", userEmail)
        model.addAttribute("userName", userName)
        model.addAttribute("file", fileDetailDto)

        logger.info("Successfully showed file detail for fileId: $fileId for user: $userEmail")
        return "inbox-detail"
    }

    /**
     * API endpoint to get detailed file information
     */
    @GetMapping("/api/files/{fileId}/detail")
    @ResponseBody
    fun getFileDetail(
        @PathVariable fileId: Long,
        authentication: OAuth2AuthenticationToken
    ): ResponseEntity<IncomingFileDetailDto> {
        logger.debug("Fetching file detail via API for fileId: $fileId")
        val userEmail = authentication.principal.getAttribute<String>("email")
        if (userEmail == null) {
            logger.warn("Unauthorized API access attempt to get file detail. User email not found.")
            return ResponseEntity.badRequest().build()
        }

        val incomingFile = incomingFileService.findByIdAndUserEmail(fileId, userEmail)
        if (incomingFile == null) {
            logger.warn("Incoming file with ID $fileId not found for user $userEmail or user does not own it (API request).")
            return ResponseEntity.notFound().build()
        }

        val fileDetailDto = IncomingFileDetailDto.fromIncomingFile(incomingFile)
        logger.debug("Successfully fetched file detail via API for fileId: $fileId for user: $userEmail")
        return ResponseEntity.ok(fileDetailDto)
    }
    
    /**
     * API endpoint to trigger OCR processing for a file
     */
    @PostMapping("/api/files/{fileId}/ocr")
    @ResponseBody
    fun triggerOcrProcessing(
        @PathVariable fileId: Long,
        authentication: OAuth2AuthenticationToken
    ): ResponseEntity<FileOperationResponse> {
        logger.info("Attempting to trigger OCR processing for fileId: $fileId")
        val userEmail = authentication.principal.getAttribute<String>("email")
        if (userEmail == null) {
            logger.warn("Unauthorized access attempt to trigger OCR. User email not found.")
            return ResponseEntity.badRequest().body(
                FileOperationResponse(false, "User not authenticated")
            )
        }

        // Check if OCR processing is available
        if (!incomingFileService.isOcrProcessingAvailable()) {
            logger.warn("OCR processing not available for file $fileId. No OCR engines configured.")
            return ResponseEntity.ok(
                FileOperationResponse(
                    false, 
                    "OCR processing is not available. Please configure at least one API key (OpenAI, Claude, or Google AI) in the application settings.",
                    fileId
                )
            )
        }

        val success = incomingFileService.triggerOcrProcessing(fileId, userEmail)
        val response = if (success) {
            logger.info("OCR processing triggered successfully for fileId: $fileId by user: $userEmail")
            FileOperationResponse(true, "OCR processing triggered successfully", fileId)
        } else {
            logger.error("Failed to trigger OCR processing for file $fileId for user: $userEmail or file not found.")
            FileOperationResponse(false, "Failed to trigger OCR processing or file not found")
        }

        return ResponseEntity.ok(response)
    }
    
    /**
     * API endpoint to retry OCR processing for a failed file
     */
    @PostMapping("/api/files/{fileId}/ocr-retry")
    @ResponseBody
    fun retryOcrProcessing(
        @PathVariable fileId: Long,
        authentication: OAuth2AuthenticationToken
    ): ResponseEntity<FileOperationResponse> {
        logger.info("Attempting to retry OCR processing for fileId: $fileId")
        val userEmail = authentication.principal.getAttribute<String>("email")
        if (userEmail == null) {
            logger.warn("Unauthorized access attempt to retry OCR. User email not found.")
            return ResponseEntity.badRequest().body(
                FileOperationResponse(false, "User not authenticated")
            )
        }

        // Check if OCR processing is available
        if (!incomingFileService.isOcrProcessingAvailable()) {
            logger.warn("OCR processing not available for retry for file $fileId. No OCR engines configured.")
            return ResponseEntity.ok(
                FileOperationResponse(
                    false, 
                    "OCR processing is not available. Please configure at least one API key (OpenAI, Claude, or Google AI) in the application settings.",
                    fileId
                )
            )
        }

        val success = incomingFileService.retryOcrProcessing(fileId, userEmail)
        val response = if (success) {
            logger.info("OCR processing retry triggered successfully for fileId: $fileId by user: $userEmail")
            FileOperationResponse(true, "OCR processing retry triggered successfully", fileId)
        } else {
            logger.error("Failed to retry OCR processing for file $fileId for user: $userEmail or file not found.")
            FileOperationResponse(false, "Failed to retry OCR processing or file not found")
        }

        return ResponseEntity.ok(response)
    }
    
    /**
     * API endpoint to dispatch an IncomingFile to Bill
     */
    @PostMapping("/api/files/{fileId}/dispatch")
    @ResponseBody
    fun dispatchToBill(
        @PathVariable fileId: Long,
        authentication: OAuth2AuthenticationToken
    ): ResponseEntity<FileOperationResponse> {
        logger.info("Attempting to dispatch file with ID: $fileId to Bill")
        val userEmail = authentication.principal.getAttribute<String>("email")
        if (userEmail == null) {
            logger.warn("Unauthorized access attempt to dispatch file. User email not found.")
            return ResponseEntity.badRequest().body(
                FileOperationResponse(false, "User not authenticated")
            )
        }

        val success = incomingFileService.dispatchToBill(fileId, userEmail)
        val response = if (success) {
            logger.info("File $fileId successfully dispatched to Bill by user: $userEmail")
            FileOperationResponse(true, "File successfully dispatched to Bill", fileId)
        } else {
            logger.error("Failed to dispatch file $fileId to Bill for user: $userEmail or file not ready.")
            FileOperationResponse(false, "Failed to dispatch file or file not ready")
        }

        return ResponseEntity.ok(response)
    }
    
    /**
     * API endpoint to get OCR statistics for the user
     */
    @GetMapping("/api/ocr-statistics")
    @ResponseBody
    fun getOcrStatistics(
        authentication: OAuth2AuthenticationToken
    ): ResponseEntity<Map<String, Any>> {
        logger.debug("Fetching OCR statistics")
        val userEmail = authentication.principal.getAttribute<String>("email")
        if (userEmail == null) {
            logger.warn("Unauthorized API access attempt to get OCR statistics. User email not found.")
            return ResponseEntity.badRequest().build()
        }

        val ocrStats = incomingFileService.getOcrStatistics(userEmail)
        val response = mapOf(
            "ocrStatistics" to ocrStats,
            "ocrAvailable" to incomingFileService.isOcrProcessingAvailable(),
            "availableEngines" to incomingFileService.getAvailableOcrEngines()
        )

        logger.debug("Successfully fetched OCR statistics for user: $userEmail")
        return ResponseEntity.ok(response)
    }
    
    /**
     * API endpoint to convert IncomingFile to Bill
     */
    @PostMapping("/api/files/{fileId}/convert-to-bill")
    @ResponseBody
    fun convertToBill(
        @PathVariable fileId: Long,
        authentication: OAuth2AuthenticationToken
    ): ResponseEntity<FileOperationResponse> {
        logger.info("Attempting to convert file $fileId to Bill")
        val userEmail = authentication.principal.getAttribute<String>("email")
        if (userEmail == null) {
            logger.warn("Unauthorized access attempt to convert file to Bill. User email not found.")
            return ResponseEntity.badRequest().body(
                FileOperationResponse(false, "User not authenticated")
            )
        }

        val bill = entityConversionService.convertIncomingFileToBill(fileId, userEmail)
        
        return if (bill != null) {
            logger.info("File $fileId converted to Bill ${bill.id} successfully by user: $userEmail")
            ResponseEntity.ok(
                FileOperationResponse(true, "File converted to Bill successfully", bill.id)
            )
        } else {
            logger.error("Failed to convert file $fileId to Bill for user: $userEmail")
            ResponseEntity.badRequest().body(
                FileOperationResponse(false, "Failed to convert file to Bill")
            )
        }
    }
    
    /**
     * API endpoint to convert IncomingFile to Receipt
     */
    @PostMapping("/api/files/{fileId}/convert-to-receipt")
    @ResponseBody
    fun convertToReceipt(
        @PathVariable fileId: Long,
        authentication: OAuth2AuthenticationToken
    ): ResponseEntity<FileOperationResponse> {
        logger.info("Attempting to convert file $fileId to Receipt")
        val userEmail = authentication.principal.getAttribute<String>("email")
        if (userEmail == null) {
            logger.warn("Unauthorized access attempt to convert file to Receipt. User email not found.")
            return ResponseEntity.badRequest().body(
                FileOperationResponse(false, "User not authenticated")
            )
        }

        val receipt = entityConversionService.convertIncomingFileToReceipt(fileId, userEmail)
        
        return if (receipt != null) {
            logger.info("File $fileId converted to Receipt ${receipt.id} successfully by user: $userEmail")
            ResponseEntity.ok(
                FileOperationResponse(true, "File converted to Receipt successfully", receipt.id)
            )
        } else {
            logger.error("Failed to convert file $fileId to Receipt for user: $userEmail")
            ResponseEntity.badRequest().body(
                FileOperationResponse(false, "Failed to convert file to Receipt")
            )
        }
    }
}
