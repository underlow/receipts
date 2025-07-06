package me.underlow.receipt.controller

import me.underlow.receipt.dto.*
import me.underlow.receipt.service.*
import me.underlow.receipt.model.EntityType
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*
import java.math.BigDecimal

/**
 * Web and API controller for receipt detail views and operations
 */
@Controller
@RequestMapping("/receipts")
class ReceiptController(
    private val receiptService: ReceiptService,
    private val billService: BillService,
    private val paymentService: PaymentService,
    private val serviceProviderService: ServiceProviderService,
    private val paymentMethodService: PaymentMethodService,
    private val entityConversionService: EntityConversionService
) {

    private val logger = LoggerFactory.getLogger(ReceiptController::class.java)

    /**
     * Shows the receipt detail page
     */
    @GetMapping("/{receiptId}")
    fun showReceiptDetail(
        @PathVariable receiptId: Long,
        authentication: OAuth2AuthenticationToken,
        model: Model
    ): String {
        logger.info("Attempting to show receipt detail for receiptId: $receiptId")
        val userEmail = authentication.principal.getAttribute<String>("email")
        if (userEmail == null) {
            logger.warn("Unauthorized access attempt to receipt detail. User email not found.")
            return "redirect:/login"
        }
        val userName = authentication.principal.getAttribute<String>("name") ?: "Unknown User"

        val receipt = receiptService.findByIdAndUserEmail(receiptId, userEmail)
        if (receipt == null) {
            logger.warn("Receipt with ID $receiptId not found for user $userEmail or user does not own it.")
            return "redirect:/inbox"
        }
        val associatedBill = receiptService.getAssociatedBill(receiptId, userEmail)
        val availableBills = receiptService.findAvailableBills(receiptId, userEmail)
        val receiptDetail = ReceiptDetailDto.fromReceipt(receipt, associatedBill, availableBills)

        // Get dropdown options for forms
        val serviceProviders = serviceProviderService.findAllActive()
            .map { ServiceProviderOption.fromServiceProvider(it) }
        val paymentMethods = paymentMethodService.findAll()
            .map { PaymentMethodOption.fromPaymentMethod(it) }

        model.addAttribute("userEmail", userEmail)
        model.addAttribute("userName", userName)
        model.addAttribute("receipt", receiptDetail)
        model.addAttribute("serviceProviders", serviceProviders)
        model.addAttribute("paymentMethods", paymentMethods)

        logger.info("Successfully showed receipt detail for receiptId: $receiptId for user: $userEmail")
        return "receipt-detail"
    }

    /**
     * API endpoint to associate receipt with a bill
     */
    @PostMapping("/api/{receiptId}/associate")
    @ResponseBody
    fun associateWithBill(
        @PathVariable receiptId: Long,
        @RequestBody request: ReceiptAssociationRequest,
        authentication: OAuth2AuthenticationToken
    ): ResponseEntity<ReceiptOperationResponse> {
        logger.info("Attempting to associate receipt $receiptId with bill ${request.billId}")
        val userEmail = authentication.principal.getAttribute<String>("email")
        if (userEmail == null) {
            logger.warn("Unauthorized access attempt to associate receipt. User email not found.")
            return ResponseEntity.badRequest().body(
                ReceiptOperationResponse(false, "User not authenticated")
            )
        }

        val updatedReceipt = if (request.billId != null) {
            receiptService.associateWithBill(receiptId, request.billId, userEmail)
        } else {
            receiptService.removeFromBill(receiptId, userEmail)
        }

        return if (updatedReceipt != null) {
            val message = if (request.billId != null) {
                "Receipt associated with bill successfully"
            } else {
                "Receipt removed from bill successfully"
            }
            logger.info("Receipt $receiptId association updated successfully by user: $userEmail. Bill ID: ${request.billId}")
            ResponseEntity.ok(
                ReceiptOperationResponse(true, message, receiptId, request.billId)
            )
        } else {
            logger.error("Failed to update receipt $receiptId association for user: $userEmail. Bill ID: ${request.billId}")
            ResponseEntity.badRequest().body(
                ReceiptOperationResponse(false, "Failed to update receipt association")
            )
        }
    }

    /**
     * API endpoint to accept receipt as payment (for standalone receipts)
     */
    @PostMapping("/api/{receiptId}/accept-as-payment")
    @ResponseBody
    fun acceptAsPayment(
        @PathVariable receiptId: Long,
        @RequestBody formData: ReceiptFormDto,
        authentication: OAuth2AuthenticationToken
    ): ResponseEntity<ReceiptOperationResponse> {
        logger.info("Attempting to accept receipt $receiptId as payment.")
        val userEmail = authentication.principal.getAttribute<String>("email")
        if (userEmail == null) {
            logger.warn("Unauthorized access attempt to accept receipt as payment. User email not found.")
            return ResponseEntity.badRequest().body(
                ReceiptOperationResponse(false, "User not authenticated")
            )
        }

        // Verify receipt exists and is standalone
        val receipt = receiptService.findByIdAndUserEmail(receiptId, userEmail)
        if (receipt == null) {
            logger.warn("Receipt $receiptId not found for user $userEmail or user does not own it.")
            return ResponseEntity.badRequest().body(
                ReceiptOperationResponse(false, "Receipt not found")
            )
        }

        // If payment data is provided, create payment
        var paymentId: Long? = null
        if (formData.hasPaymentData()) {
            try {
                val payment = paymentService.createPaymentFromReceipt(
                    receiptId = receiptId,
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
                    logger.info("Payment created with ID: $paymentId for receiptId: $receiptId by user: $userEmail")
                } else {
                    logger.warn("Payment creation returned null for receiptId: $receiptId by user: $userEmail")
                }
            } catch (e: Exception) {
                logger.error("Error creating payment for receiptId: $receiptId by user: $userEmail", e)
            }
        }

        // Update receipt association if billId is provided
        if (formData.billId != null) {
            val updatedReceipt = receiptService.associateWithBill(receiptId, formData.billId, userEmail)
            if (updatedReceipt != null) {
                logger.info("Receipt $receiptId associated with bill ${formData.billId} successfully by user: $userEmail")
            } else {
                logger.warn("Failed to associate receipt $receiptId with bill ${formData.billId} for user: $userEmail")
            }
        }

        val message = if (paymentId != null) {
            "Receipt accepted and payment created successfully"
        } else {
            "Receipt accepted successfully"
        }

        logger.info("Receipt $receiptId accepted as payment for user: $userEmail. Payment ID: $paymentId")
        return ResponseEntity.ok(
            ReceiptOperationResponse(true, message, receiptId, formData.billId, paymentId)
        )
    }

    /**
     * API endpoint to save draft changes to a receipt
     */
    @PostMapping("/api/{receiptId}/save-draft")
    @ResponseBody
    fun saveDraft(
        @PathVariable receiptId: Long,
        @RequestBody formData: ReceiptFormDto,
        authentication: OAuth2AuthenticationToken
    ): ResponseEntity<ReceiptOperationResponse> {
        logger.info("Attempting to save draft for receiptId: $receiptId")
        val userEmail = authentication.principal.getAttribute<String>("email")
        if (userEmail == null) {
            logger.warn("Unauthorized access attempt to save receipt draft. User email not found.")
            return ResponseEntity.badRequest().body(
                ReceiptOperationResponse(false, "User not authenticated")
            )
        }

        // Update receipt association if billId is provided
        val updatedReceipt = if (formData.billId != null) {
            receiptService.associateWithBill(receiptId, formData.billId, userEmail)
        } else {
            receiptService.removeFromBill(receiptId, userEmail)
        }

        return if (updatedReceipt != null) {
            logger.info("Draft saved successfully for receiptId: $receiptId by user: $userEmail. Bill ID: ${formData.billId}")
            ResponseEntity.ok(
                ReceiptOperationResponse(true, "Draft saved successfully", receiptId, formData.billId)
            )
        } else {
            logger.error("Failed to save draft for receiptId: $receiptId by user: $userEmail. Bill ID: ${formData.billId}")
            ResponseEntity.badRequest().body(
                ReceiptOperationResponse(false, "Failed to save draft")
            )
        }
    }

    /**
     * API endpoint to delete a receipt
     */
    @DeleteMapping("/api/{receiptId}")
    @ResponseBody
    fun deleteReceipt(
        @PathVariable receiptId: Long,
        authentication: OAuth2AuthenticationToken
    ): ResponseEntity<ReceiptOperationResponse> {
        logger.info("Attempting to delete receipt with ID: $receiptId")
        val userEmail = authentication.principal.getAttribute<String>("email")
        if (userEmail == null) {
            logger.warn("Unauthorized access attempt to delete receipt. User email not found.")
            return ResponseEntity.badRequest().body(
                ReceiptOperationResponse(false, "User not authenticated")
            )
        }

        val success = receiptService.deleteReceipt(receiptId, userEmail)
        
        return if (success) {
            logger.info("Receipt $receiptId deleted successfully by user: $userEmail")
            ResponseEntity.ok(
                ReceiptOperationResponse(true, "Receipt deleted successfully")
            )
        } else {
            logger.error("Failed to delete receipt $receiptId for user: $userEmail")
            ResponseEntity.badRequest().body(
                ReceiptOperationResponse(false, "Failed to delete receipt")
            )
        }
    }

    /**
     * API endpoint to get receipt data (for AJAX requests)
     */
    @GetMapping("/api/{receiptId}")
    @ResponseBody
    fun getReceiptDetail(
        @PathVariable receiptId: Long,
        authentication: OAuth2AuthenticationToken
    ): ResponseEntity<ReceiptDetailDto> {
        logger.debug("Fetching receipt detail via API for receiptId: $receiptId")
        val userEmail = authentication.principal.getAttribute<String>("email")
        if (userEmail == null) {
            logger.warn("Unauthorized API access attempt to get receipt detail. User email not found.")
            return ResponseEntity.badRequest().build()
        }

        val receipt = receiptService.findByIdAndUserEmail(receiptId, userEmail) 
        if (receipt == null) {
            logger.warn("Receipt with ID $receiptId not found for user $userEmail or user does not own it (API request).")
            return ResponseEntity.notFound().build()
        }
        val associatedBill = receiptService.getAssociatedBill(receiptId, userEmail)
        val availableBills = receiptService.findAvailableBills(receiptId, userEmail)
        val receiptDetail = ReceiptDetailDto.fromReceipt(receipt, associatedBill, availableBills)

        logger.debug("Successfully fetched receipt detail via API for receiptId: $receiptId for user: $userEmail")
        return ResponseEntity.ok(receiptDetail)
    }

    /**
     * API endpoint to get available bills for association
     */
    @GetMapping("/api/{receiptId}/available-bills")
    @ResponseBody
    fun getAvailableBills(
        @PathVariable receiptId: Long,
        authentication: OAuth2AuthenticationToken
    ): ResponseEntity<List<BillSummaryDto>> {
        logger.debug("Fetching available bills for receiptId: $receiptId")
        val userEmail = authentication.principal.getAttribute<String>("email")
        if (userEmail == null) {
            logger.warn("Unauthorized API access attempt to get available bills. User email not found.")
            return ResponseEntity.badRequest().build()
        }

        val availableBills = receiptService.findAvailableBills(receiptId, userEmail)
        val billSummaries = availableBills.map { BillSummaryDto.fromBill(it) }

        logger.debug("Successfully fetched available bills for receiptId: $receiptId for user: $userEmail. Found ${billSummaries.size} bills.")
        return ResponseEntity.ok(billSummaries)
    }
    
    /**
     * API endpoint to revert Receipt back to IncomingFile
     */
    @PostMapping("/api/{receiptId}/revert")
    @ResponseBody
    fun revertToIncomingFile(
        @PathVariable receiptId: Long,
        authentication: OAuth2AuthenticationToken
    ): ResponseEntity<ReceiptOperationResponse> {
        logger.info("Attempting to revert receipt $receiptId to IncomingFile.")
        val userEmail = authentication.principal.getAttribute<String>("email")
        if (userEmail == null) {
            logger.warn("Unauthorized access attempt to revert receipt. User email not found.")
            return ResponseEntity.badRequest().body(
                ReceiptOperationResponse(false, "User not authenticated")
            )
        }

        val incomingFile = entityConversionService.revertReceiptToIncomingFile(receiptId, userEmail)
        
        return if (incomingFile != null) {
            logger.info("Receipt $receiptId reverted to IncomingFile ${incomingFile.id} successfully by user: $userEmail")
            ResponseEntity.ok(
                ReceiptOperationResponse(true, "Receipt reverted to IncomingFile successfully", incomingFile.id)
            )
        } else {
            logger.error("Failed to revert receipt $receiptId to IncomingFile for user: $userEmail")
            ResponseEntity.badRequest().body(
                ReceiptOperationResponse(false, "Failed to revert Receipt to IncomingFile")
            )
        }
    }
}