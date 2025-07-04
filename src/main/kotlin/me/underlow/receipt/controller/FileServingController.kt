package me.underlow.receipt.controller

import me.underlow.receipt.service.IncomingFileService
import me.underlow.receipt.service.ThumbnailService
import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken
import org.springframework.web.bind.annotation.*
import java.io.File
import java.nio.file.Files

/**
 * REST controller for serving files and thumbnails with user access control
 */
@RestController
@RequestMapping("/api/files")
class FileServingController(
    private val incomingFileService: IncomingFileService,
    private val thumbnailService: ThumbnailService
) {

    private val logger = LoggerFactory.getLogger(FileServingController::class.java)

    /**
     * Serves the original file for authenticated users who own the file
     */
    @GetMapping("/{fileId}")
    fun serveFile(
        @PathVariable fileId: Long,
        authentication: OAuth2AuthenticationToken
    ): ResponseEntity<ByteArray> {
        logger.debug("Attempting to serve file with ID: $fileId")
        val userEmail = authentication.principal.getAttribute<String>("email")
        if (userEmail == null) {
            logger.warn("Unauthorized access attempt to serve file. User email not found.")
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
        }

        val incomingFile = incomingFileService.findByIdAndUserEmail(fileId, userEmail)
        if (incomingFile == null) {
            logger.warn("File with ID $fileId not found for user $userEmail or user does not own it.")
            return ResponseEntity.notFound().build()
        }

        val file = File(incomingFile.filePath)
        if (!file.exists()) {
            logger.error("File not found on disk for fileId: $fileId at path: ${incomingFile.filePath}")
            return ResponseEntity.notFound().build()
        }

        try {
            val fileBytes = Files.readAllBytes(file.toPath())
            val mediaType = determineMediaType(incomingFile.filename)

            logger.debug("Successfully served file with ID: $fileId for user: $userEmail")
            return ResponseEntity.ok()
                .contentType(mediaType)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"${incomingFile.filename}\"")
                .body(fileBytes)
        } catch (e: Exception) {
            logger.error("Error reading file for fileId: $fileId at path: ${incomingFile.filePath}", e)
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
        }
    }

    /**
     * Serves a thumbnail for the file
     */
    @GetMapping("/{fileId}/thumbnail")
    fun serveThumbnail(
        @PathVariable fileId: Long,
        @RequestParam(defaultValue = "200") width: Int,
        @RequestParam(defaultValue = "200") height: Int,
        authentication: OAuth2AuthenticationToken
    ): ResponseEntity<ByteArray> {
        logger.debug("Attempting to serve thumbnail for fileId: $fileId with dimensions ${width}x${height}")
        val userEmail = authentication.principal.getAttribute<String>("email")
        if (userEmail == null) {
            logger.warn("Unauthorized access attempt to serve thumbnail. User email not found.")
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
        }

        val incomingFile = incomingFileService.findByIdAndUserEmail(fileId, userEmail)
        if (incomingFile == null) {
            logger.warn("File with ID $fileId not found for user $userEmail or user does not own it (thumbnail request).")
            return ResponseEntity.notFound().build()
        }

        val thumbnailBytes = thumbnailService.generateThumbnail(
            incomingFile.filePath, 
            incomingFile.filename, 
            width, 
            height
        )
        if (thumbnailBytes == null) {
            logger.error("Failed to generate thumbnail for fileId: $fileId at path: ${incomingFile.filePath}")
            return ResponseEntity.notFound().build()
        }

        logger.debug("Successfully served thumbnail for fileId: $fileId for user: $userEmail")
        return ResponseEntity.ok()
            .contentType(MediaType.IMAGE_JPEG)
            .body(thumbnailBytes)
    }

    /**
     * Determines the appropriate MediaType based on file extension
     */
    private fun determineMediaType(filename: String): MediaType {
        val extension = filename.substringAfterLast('.', "").lowercase()
        return when (extension) {
            "pdf" -> MediaType.APPLICATION_PDF
            "jpg", "jpeg" -> MediaType.IMAGE_JPEG
            "png" -> MediaType.IMAGE_PNG
            "gif" -> MediaType.IMAGE_GIF
            "bmp" -> MediaType.parseMediaType("image/bmp")
            "tiff", "tif" -> MediaType.parseMediaType("image/tiff")
            else -> MediaType.APPLICATION_OCTET_STREAM
        }
    }
}