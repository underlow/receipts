package me.underlow.receipt.controller

import me.underlow.receipt.service.IncomingFileService
import me.underlow.receipt.service.ThumbnailService
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

    /**
     * Serves the original file for authenticated users who own the file
     */
    @GetMapping("/{fileId}")
    fun serveFile(
        @PathVariable fileId: Long,
        authentication: OAuth2AuthenticationToken
    ): ResponseEntity<ByteArray> {
        val userEmail = authentication.principal.getAttribute<String>("email")
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()

        val incomingFile = incomingFileService.findByIdAndUserEmail(fileId, userEmail)
            ?: return ResponseEntity.notFound().build()

        val file = File(incomingFile.filePath)
        if (!file.exists()) {
            return ResponseEntity.notFound().build()
        }

        val fileBytes = Files.readAllBytes(file.toPath())
        val mediaType = determineMediaType(incomingFile.filename)

        return ResponseEntity.ok()
            .contentType(mediaType)
            .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"${incomingFile.filename}\"")
            .body(fileBytes)
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
        val userEmail = authentication.principal.getAttribute<String>("email")
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()

        val incomingFile = incomingFileService.findByIdAndUserEmail(fileId, userEmail)
            ?: return ResponseEntity.notFound().build()

        val thumbnailBytes = thumbnailService.generateThumbnail(
            incomingFile.filePath, 
            incomingFile.filename, 
            width, 
            height
        ) ?: return ResponseEntity.notFound().build()

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