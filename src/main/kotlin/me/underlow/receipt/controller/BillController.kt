package me.underlow.receipt.controller

import me.underlow.receipt.dto.*
import me.underlow.receipt.model.ItemStatus
import me.underlow.receipt.service.*
import me.underlow.receipt.model.EntityType
import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*
import java.io.File
import java.math.BigDecimal
import java.nio.file.Files

/**
 * Web and API controller for bill detail views and operations
 */
@Controller
@RequestMapping("/bills")
class BillController(
    private val billService: BillService,
    private val receiptService: ReceiptService,
    private val paymentService: PaymentService,
    private val serviceProviderService: ServiceProviderService,
    private val paymentMethodService: PaymentMethodService,
    private val entityConversionService: EntityConversionService,
    private val thumbnailService: ThumbnailService
) {

    private val logger = LoggerFactory.getLogger(BillController::class.java)

    /**
     * Shows the bill detail page
     */
    @GetMapping("/{billId}")
    fun showBillDetail(
        @PathVariable billId: Long,
        authentication: OAuth2AuthenticationToken,
        model: Model
    ): String {
        logger.info("Attempting to show bill detail for billId: $billId")
        val userEmail = authentication.principal.getAttribute<String>("email")
        if (userEmail == null) {
            logger.warn("Unauthorized access attempt to bill detail. User email not found.")
            return "redirect:/login"
        }
        val userName = authentication.principal.getAttribute<String>("name") ?: "Unknown User"

        val bill = billService.findByIdAndUserEmail(billId, userEmail)
        if (bill == null) {
            logger.warn("Bill with ID $billId not found for user $userEmail or user does not own it.")
            return "redirect:/inbox"
        }
        val receipts = billService.getAssociatedReceipts(billId, userEmail)
        val billDetail = BillDetailDto.fromBill(bill, receipts)

        // Get dropdown options for forms
        val serviceProviders = serviceProviderService.findAllActive()
            .map { ServiceProviderOption.fromServiceProvider(it) }
        val paymentMethods = paymentMethodService.findAll()
            .map { PaymentMethodOption.fromPaymentMethod(it) }

        model.addAttribute("userEmail", userEmail)
        model.addAttribute("userName", userName)
        model.addAttribute("bill", billDetail)
        model.addAttribute("serviceProviders", serviceProviders)
        model.addAttribute("paymentMethods", paymentMethods)
        model.addAttribute("canApprove", bill.status == ItemStatus.NEW || bill.status == ItemStatus.PROCESSING)
        model.addAttribute("canReject", bill.status == ItemStatus.NEW || bill.status == ItemStatus.PROCESSING)

        logger.info("Successfully showed bill detail for billId: $billId for user: $userEmail")
        return "bill-detail"
    }

    /**
     * API endpoint to save draft changes to a bill
     */
    @PostMapping("/api/{billId}/save-draft")
    @ResponseBody
    fun saveDraft(
        @PathVariable billId: Long,
        @RequestBody formData: BillFormDto,
        authentication: OAuth2AuthenticationToken
    ): ResponseEntity<BillOperationResponse> {
        logger.info("Attempting to save draft for billId: $billId")
        val userEmail = authentication.principal.getAttribute<String>("email")
        if (userEmail == null) {
            logger.warn("Unauthorized access attempt to save draft. User email not found.")
            return ResponseEntity.badRequest().body(
                BillOperationResponse(false, "User not authenticated")
            )
        }

        val updatedBill = billService.updateOcrData(
            billId = billId,
            userEmail = userEmail,
            ocrRawJson = "", // Keep existing OCR data
            extractedAmount = formData.extractedAmount,
            extractedDate = formData.extractedDate,
            extractedProvider = formData.extractedProvider
        )

        return if (updatedBill != null) {
            logger.info("Draft saved successfully for billId: $billId by user: $userEmail")
            ResponseEntity.ok(
                BillOperationResponse(true, "Draft saved successfully", billId)
            )
        } else {
            logger.error("Failed to save draft for billId: $billId by user: $userEmail")
            ResponseEntity.badRequest().body(
                BillOperationResponse(false, "Failed to save draft")
            )
        }
    }

    /**
     * API endpoint to approve a bill and convert to payment
     */
    @PostMapping("/api/{billId}/approve")
    @ResponseBody
    fun approveBill(
        @PathVariable billId: Long,
        @RequestBody formData: BillFormDto,
        authentication: OAuth2AuthenticationToken
    ): ResponseEntity<BillOperationResponse> {
        logger.info("Attempting to approve bill for billId: $billId")
        val userEmail = authentication.principal.getAttribute<String>("email")
        if (userEmail == null) {
            logger.warn("Unauthorized access attempt to approve bill. User email not found.")
            return ResponseEntity.badRequest().body(
                BillOperationResponse(false, "User not authenticated")
            )
        }

        // First update the bill with form data
        val updatedBill = billService.updateOcrData(
            billId = billId,
            userEmail = userEmail,
            ocrRawJson = "", // Keep existing OCR data
            extractedAmount = formData.extractedAmount,
            extractedDate = formData.extractedDate,
            extractedProvider = formData.extractedProvider
        )

        if (updatedBill == null) {
            logger.error("Failed to update bill $billId before approval for user: $userEmail")
            return ResponseEntity.badRequest().body(
                BillOperationResponse(false, "Failed to update bill")
            )
        }

        // Approve the bill
        val approvedBill = billService.approveBill(billId, userEmail)
        if (approvedBill == null) {
            logger.error("Failed to approve bill $billId for user: $userEmail")
            return ResponseEntity.badRequest().body(
                BillOperationResponse(false, "Failed to approve bill")
            )
        }

        // If payment data is provided, create payment
        var paymentId: Long? = null
        if (formData.hasPaymentData()) {
            try {
                val payment = paymentService.createPaymentFromBill(
                    billId = billId,
                    userEmail = userEmail,
                    serviceProviderId = formData.serviceProviderId!!,
                    paymentMethodId = formData.paymentMethodId!!,
                    amount = BigDecimal.valueOf(formData.amount!!),
                    currency = formData.currency,
                    invoiceDate = formData.invoiceDate!!,
                    paymentDate = formData.paymentDate!!,
                    comment = formData.comment
                )
                paymentId = payment?.id
                if (paymentId != null) {
                    logger.info("Payment created with ID: $paymentId for billId: $billId by user: $userEmail")
                } else {
                    logger.warn("Payment creation returned null for billId: $billId by user: $userEmail")
                }
            } catch (e: Exception) {
                logger.error("Error creating payment for billId: $billId by user: $userEmail", e)
            }
        }

        val message = if (paymentId != null) {
            "Bill approved and payment created successfully"
        } else {
            "Bill approved successfully"
        }

        logger.info("Bill $billId approved successfully for user: $userEmail. Payment ID: $paymentId")
        return ResponseEntity.ok(
            BillOperationResponse(true, message, billId, paymentId)
        )
    }

    /**
     * API endpoint to reject a bill
     */
    @PostMapping("/api/{billId}/reject")
    @ResponseBody
    fun rejectBill(
        @PathVariable billId: Long,
        authentication: OAuth2AuthenticationToken
    ): ResponseEntity<BillOperationResponse> {
        logger.info("Attempting to reject bill for billId: $billId")
        val userEmail = authentication.principal.getAttribute<String>("email")
        if (userEmail == null) {
            logger.warn("Unauthorized access attempt to reject bill. User email not found.")
            return ResponseEntity.badRequest().body(
                BillOperationResponse(false, "User not authenticated")
            )
        }

        val rejectedBill = billService.rejectBill(billId, userEmail)

        return if (rejectedBill != null) {
            logger.info("Bill $billId rejected successfully by user: $userEmail")
            ResponseEntity.ok(
                BillOperationResponse(true, "Bill rejected successfully", billId)
            )
        } else {
            logger.error("Failed to reject bill $billId for user: $userEmail")
            ResponseEntity.badRequest().body(
                BillOperationResponse(false, "Failed to reject bill")
            )
        }
    }

    /**
     * API endpoint to delete a bill
     */
    @DeleteMapping("/api/{billId}")
    @ResponseBody
    fun deleteBill(
        @PathVariable billId: Long,
        authentication: OAuth2AuthenticationToken
    ): ResponseEntity<BillOperationResponse> {
        logger.info("Attempting to delete bill for billId: $billId")
        val userEmail = authentication.principal.getAttribute<String>("email")
        if (userEmail == null) {
            logger.warn("Unauthorized access attempt to delete bill. User email not found.")
            return ResponseEntity.badRequest().body(
                BillOperationResponse(false, "User not authenticated")
            )
        }

        val success = billService.deleteBill(billId, userEmail)

        return if (success) {
            logger.info("Bill $billId deleted successfully by user: $userEmail")
            ResponseEntity.ok(
                BillOperationResponse(true, "Bill deleted successfully")
            )
        } else {
            logger.error("Failed to delete bill $billId for user: $userEmail")
            ResponseEntity.badRequest().body(
                BillOperationResponse(false, "Failed to delete bill")
            )
        }
    }

    /**
     * API endpoint to get bill data (for AJAX requests)
     */
    @GetMapping("/api/{billId}")
    @ResponseBody
    fun getBillDetail(
        @PathVariable billId: Long,
        authentication: OAuth2AuthenticationToken
    ): ResponseEntity<BillDetailDto> {
        logger.debug("Fetching bill detail via API for billId: $billId")
        val userEmail = authentication.principal.getAttribute<String>("email")
        if (userEmail == null) {
            logger.warn("Unauthorized API access attempt to get bill detail. User email not found.")
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
        }

        val bill = billService.findByIdAndUserEmail(billId, userEmail)
        if (bill == null) {
            logger.warn("Bill with ID $billId not found for user $userEmail or user does not own it (API request).")
            return ResponseEntity.notFound().build()
        }
        val receipts = billService.getAssociatedReceipts(billId, userEmail)
        val billDetail = BillDetailDto.fromBill(bill, receipts)

        logger.debug("Successfully fetched bill detail via API for billId: $billId for user: $userEmail")
        return ResponseEntity.ok(billDetail)
    }

    /**
     * API endpoint to revert Bill back to IncomingFile
     */
    @PostMapping("/api/{billId}/revert")
    @ResponseBody
    fun revertToIncomingFile(
        @PathVariable billId: Long,
        authentication: OAuth2AuthenticationToken
    ): ResponseEntity<BillOperationResponse> {
        logger.info("Attempting to revert bill $billId to IncomingFile.")
        val userEmail = authentication.principal.getAttribute<String>("email")
        if (userEmail == null) {
            logger.warn("Unauthorized access attempt to revert bill. User email not found.")
            return ResponseEntity.badRequest().body(
                BillOperationResponse(false, "User not authenticated")
            )
        }

        val incomingFile = entityConversionService.revertBillToIncomingFile(billId, userEmail)

        return if (incomingFile != null) {
            logger.info("Bill $billId reverted to IncomingFile ${incomingFile.id} successfully by user: $userEmail")
            ResponseEntity.ok(
                BillOperationResponse(true, "Bill reverted to IncomingFile successfully", incomingFile.id)
            )
        } else {
            logger.error("Failed to revert bill $billId to IncomingFile for user: $userEmail")
            ResponseEntity.badRequest().body(
                BillOperationResponse(false, "Failed to revert Bill to IncomingFile")
            )
        }
    }

    /**
     * Serves the bill image file for authenticated users who own the bill
     */
    @GetMapping("/api/{billId}/image")
    @ResponseBody
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
    @GetMapping("/api/{billId}/thumbnail")
    @ResponseBody
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
