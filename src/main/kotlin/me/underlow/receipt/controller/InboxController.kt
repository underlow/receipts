package me.underlow.receipt.controller

import me.underlow.receipt.dto.FileOperationResponse
import me.underlow.receipt.dto.InboxFileDto
import me.underlow.receipt.dto.InboxListResponse
import me.underlow.receipt.model.BillStatus
import me.underlow.receipt.service.IncomingFileService
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
    private val incomingFileService: IncomingFileService
) {

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
        val userEmail = authentication.principal.getAttribute<String>("email") ?: return "redirect:/login"
        val userName = authentication.principal.getAttribute<String>("name") ?: "Unknown User"

        // Parse status filter
        val statusFilter = if (status.isBlank()) null else try {
            BillStatus.valueOf(status.uppercase())
        } catch (e: IllegalArgumentException) {
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
        val userEmail = authentication.principal.getAttribute<String>("email")
            ?: return ResponseEntity.badRequest().build()

        val statusFilter = if (status.isBlank()) null else try {
            BillStatus.valueOf(status.uppercase())
        } catch (e: IllegalArgumentException) {
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
        val userEmail = authentication.principal.getAttribute<String>("email")
            ?: return ResponseEntity.badRequest().body(
                FileOperationResponse(false, "User not authenticated")
            )

        val success = incomingFileService.updateStatus(fileId, userEmail, BillStatus.APPROVED)
        val response = if (success) {
            FileOperationResponse(true, "File approved successfully", fileId)
        } else {
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
        val userEmail = authentication.principal.getAttribute<String>("email")
            ?: return ResponseEntity.badRequest().body(
                FileOperationResponse(false, "User not authenticated")
            )

        val success = incomingFileService.updateStatus(fileId, userEmail, BillStatus.REJECTED)
        val response = if (success) {
            FileOperationResponse(true, "File rejected successfully", fileId)
        } else {
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
        val userEmail = authentication.principal.getAttribute<String>("email")
            ?: return ResponseEntity.badRequest().body(
                FileOperationResponse(false, "User not authenticated")
            )

        val success = incomingFileService.deleteFile(fileId, userEmail)
        val response = if (success) {
            FileOperationResponse(true, "File deleted successfully", fileId)
        } else {
            FileOperationResponse(false, "Failed to delete file or file not found")
        }

        return ResponseEntity.ok(response)
    }
}