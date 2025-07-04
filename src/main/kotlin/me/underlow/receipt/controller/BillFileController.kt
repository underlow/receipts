package me.underlow.receipt.controller

import me.underlow.receipt.service.BillService
import me.underlow.receipt.service.ReceiptService
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
 * REST controller for serving bill and receipt files and thumbnails with user access control
 */
@RestController
@RequestMapping("/api")
class BillFileController(
    private val billService: BillService,
    private val receiptService: ReceiptService,
    private val thumbnailService: ThumbnailService
) {

    private val logger = LoggerFactory.getLogger(BillFileController::class.java)

    /**
     * Serves the bill image file for authenticated users who own the bill
     */
    @GetMapping("/bills/{billId}/image")
    fun serveBillImage(
        @PathVariable billId: Long,
        authentication: OAuth2AuthenticationToken
    ): ResponseEntity<ByteArray> {
        logger.debug("Attempting to serve image for billId: $billId")
        val userEmail = authentication.principal.getAttribute<String>("email")
        if (userEmail == null) {
            logger.warn("Unauthorized access attempt to serve bill image. User email not found.")
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
        }

        val bill = billService.findByIdAndUserEmail(billId, userEmail)
        if (bill == null) {
            logger.warn("Bill with ID $billId not found for user $userEmail or user does not own it (image request).")
            return ResponseEntity.notFound().build()
        }

        val file = File(bill.filePath)
        if (!file.exists()) {
            logger.error("Image file not found on disk for billId: $billId at path: ${bill.filePath}")
            return ResponseEntity.notFound().build()
        }

        try {
            val fileBytes = Files.readAllBytes(file.toPath())
            val mediaType = determineMediaType(bill.filename)

            logger.debug("Successfully served image for billId: $billId for user: $userEmail")
            return ResponseEntity.ok()
                .contentType(mediaType)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"${bill.filename}\"")
                .body(fileBytes)
        } catch (e: Exception) {
            logger.error("Error reading image file for billId: $billId at path: ${bill.filePath}", e)
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
        }
    }

    /**
     * Serves a thumbnail for the bill image
     */
    @GetMapping("/bills/{billId}/thumbnail")
    fun serveBillThumbnail(
        @PathVariable billId: Long,
        @RequestParam(defaultValue = "200") width: Int,
        @RequestParam(defaultValue = "200") height: Int,
        authentication: OAuth2AuthenticationToken
    ): ResponseEntity<ByteArray> {
        logger.debug("Attempting to serve thumbnail for billId: $billId with dimensions ${width}x${height}")
        val userEmail = authentication.principal.getAttribute<String>("email")
        if (userEmail == null) {
            logger.warn("Unauthorized access attempt to serve bill thumbnail. User email not found.")
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
        }

        val bill = billService.findByIdAndUserEmail(billId, userEmail)
        if (bill == null) {
            logger.warn("Bill with ID $billId not found for user $userEmail or user does not own it (thumbnail request).")
            return ResponseEntity.notFound().build()
        }

        val thumbnailBytes = thumbnailService.generateThumbnail(
            bill.filePath,
            bill.filename,
            width,
            height
        )
        if (thumbnailBytes == null) {
            logger.error("Failed to generate thumbnail for billId: $billId at path: ${bill.filePath}")
            return ResponseEntity.notFound().build()
        }

        logger.debug("Successfully served thumbnail for billId: $billId for user: $userEmail")
        return ResponseEntity.ok()
            .contentType(MediaType.IMAGE_JPEG)
            .body(thumbnailBytes)
    }

    /**
     * Serves the receipt image file for authenticated users who own the receipt
     */
    @GetMapping("/receipts/{receiptId}/image")
    fun serveReceiptImage(
        @PathVariable receiptId: Long,
        authentication: OAuth2AuthenticationToken
    ): ResponseEntity<ByteArray> {
        logger.debug("Attempting to serve image for receiptId: $receiptId")
        val userEmail = authentication.principal.getAttribute<String>("email")
        if (userEmail == null) {
            logger.warn("Unauthorized access attempt to serve receipt image. User email not found.")
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
        }

        val receipt = receiptService.findByIdAndUserEmail(receiptId, userEmail)
        if (receipt == null) {
            logger.warn("Receipt with ID $receiptId not found for user $userEmail or user does not own it (image request).")
            return ResponseEntity.notFound().build()
        }

        // Check if receipt has file path
        if (receipt.filePath == null) {
            logger.warn("Receipt with ID $receiptId has no file path.")
            return ResponseEntity.notFound().build()
        }

        val file = File(receipt.filePath)
        if (!file.exists()) {
            logger.error("Image file not found on disk for receiptId: $receiptId at path: ${receipt.filePath}")
            return ResponseEntity.notFound().build()
        }

        try {
            val fileBytes = Files.readAllBytes(file.toPath())
            val mediaType = receipt.filename?.let { determineMediaType(it) } ?: MediaType.APPLICATION_OCTET_STREAM

            logger.debug("Successfully served image for receiptId: $receiptId for user: $userEmail")
            return ResponseEntity.ok()
                .contentType(mediaType)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"${receipt.filename ?: "receipt"}\"")
                .body(fileBytes)
        } catch (e: Exception) {
            logger.error("Error reading image file for receiptId: $receiptId at path: ${receipt.filePath}", e)
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
        }
    }

    /**
     * Serves a thumbnail for the receipt image
     */
    @GetMapping("/receipts/{receiptId}/thumbnail")
    fun serveReceiptThumbnail(
        @PathVariable receiptId: Long,
        @RequestParam(defaultValue = "200") width: Int,
        @RequestParam(defaultValue = "200") height: Int,
        authentication: OAuth2AuthenticationToken
    ): ResponseEntity<ByteArray> {
        logger.debug("Attempting to serve thumbnail for receiptId: $receiptId with dimensions ${width}x${height}")
        val userEmail = authentication.principal.getAttribute<String>("email")
        if (userEmail == null) {
            logger.warn("Unauthorized access attempt to serve receipt thumbnail. User email not found.")
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
        }

        val receipt = receiptService.findByIdAndUserEmail(receiptId, userEmail)
        if (receipt == null) {
            logger.warn("Receipt with ID $receiptId not found for user $userEmail or user does not own it (thumbnail request).")
            return ResponseEntity.notFound().build()
        }

        // Check if receipt has file path and filename
        if (receipt.filePath == null || receipt.filename == null) {
            logger.warn("Receipt with ID $receiptId has no file path or filename.")
            return ResponseEntity.notFound().build()
        }

        val thumbnailBytes = thumbnailService.generateThumbnail(
            receipt.filePath,
            receipt.filename,
            width,
            height
        )
        if (thumbnailBytes == null) {
            logger.error("Failed to generate thumbnail for receiptId: $receiptId at path: ${receipt.filePath}")
            return ResponseEntity.notFound().build()
        }

        logger.debug("Successfully served thumbnail for receiptId: $receiptId for user: $userEmail")
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
            "webp" -> MediaType.parseMediaType("image/webp")
            "tiff", "tif" -> MediaType.parseMediaType("image/tiff")
            else -> MediaType.APPLICATION_OCTET_STREAM
        }
    }
}