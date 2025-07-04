package me.underlow.receipt.controller

import me.underlow.receipt.dto.*
import me.underlow.receipt.service.*
import me.underlow.receipt.model.EntityType
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

    /**
     * Shows the receipt detail page
     */
    @GetMapping("/{receiptId}")
    fun showReceiptDetail(
        @PathVariable receiptId: Long,
        authentication: OAuth2AuthenticationToken,
        model: Model
    ): String {
        val userEmail = authentication.principal.getAttribute<String>("email") ?: return "redirect:/login"
        val userName = authentication.principal.getAttribute<String>("name") ?: "Unknown User"

        val receipt = receiptService.findByIdAndUserEmail(receiptId, userEmail) ?: return "redirect:/inbox"
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
        val userEmail = authentication.principal.getAttribute<String>("email")
            ?: return ResponseEntity.badRequest().body(
                ReceiptOperationResponse(false, "User not authenticated")
            )

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
            ResponseEntity.ok(
                ReceiptOperationResponse(true, message, receiptId, request.billId)
            )
        } else {
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
        val userEmail = authentication.principal.getAttribute<String>("email")
            ?: return ResponseEntity.badRequest().body(
                ReceiptOperationResponse(false, "User not authenticated")
            )

        // Verify receipt exists and is standalone
        val receipt = receiptService.findByIdAndUserEmail(receiptId, userEmail)
        if (receipt == null) {
            return ResponseEntity.badRequest().body(
                ReceiptOperationResponse(false, "Receipt not found")
            )
        }

        // If payment data is provided, create payment
        val payment = if (formData.hasPaymentData()) {
            paymentService.createPaymentFromReceipt(
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
        } else {
            null
        }

        // Update receipt association if billId is provided
        if (formData.billId != null) {
            receiptService.associateWithBill(receiptId, formData.billId, userEmail)
        }

        val message = if (payment != null) {
            "Receipt accepted and payment created successfully"
        } else {
            "Receipt accepted successfully"
        }

        return ResponseEntity.ok(
            ReceiptOperationResponse(true, message, receiptId, formData.billId, payment?.id)
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
        val userEmail = authentication.principal.getAttribute<String>("email")
            ?: return ResponseEntity.badRequest().body(
                ReceiptOperationResponse(false, "User not authenticated")
            )

        // Update receipt association if billId is provided
        val updatedReceipt = if (formData.billId != null) {
            receiptService.associateWithBill(receiptId, formData.billId, userEmail)
        } else {
            receiptService.removeFromBill(receiptId, userEmail)
        }

        return if (updatedReceipt != null) {
            ResponseEntity.ok(
                ReceiptOperationResponse(true, "Draft saved successfully", receiptId, formData.billId)
            )
        } else {
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
        val userEmail = authentication.principal.getAttribute<String>("email")
            ?: return ResponseEntity.badRequest().body(
                ReceiptOperationResponse(false, "User not authenticated")
            )

        val success = receiptService.deleteReceipt(receiptId, userEmail)
        
        return if (success) {
            ResponseEntity.ok(
                ReceiptOperationResponse(true, "Receipt deleted successfully")
            )
        } else {
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
        val userEmail = authentication.principal.getAttribute<String>("email")
            ?: return ResponseEntity.badRequest().build()

        val receipt = receiptService.findByIdAndUserEmail(receiptId, userEmail) 
            ?: return ResponseEntity.notFound().build()
        val associatedBill = receiptService.getAssociatedBill(receiptId, userEmail)
        val availableBills = receiptService.findAvailableBills(receiptId, userEmail)
        val receiptDetail = ReceiptDetailDto.fromReceipt(receipt, associatedBill, availableBills)

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
        val userEmail = authentication.principal.getAttribute<String>("email")
            ?: return ResponseEntity.badRequest().build()

        val availableBills = receiptService.findAvailableBills(receiptId, userEmail)
        val billSummaries = availableBills.map { BillSummaryDto.fromBill(it) }

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
        val userEmail = authentication.principal.getAttribute<String>("email")
            ?: return ResponseEntity.badRequest().body(
                ReceiptOperationResponse(false, "User not authenticated")
            )

        val incomingFile = entityConversionService.revertReceiptToIncomingFile(receiptId, userEmail)
        
        return if (incomingFile != null) {
            ResponseEntity.ok(
                ReceiptOperationResponse(true, "Receipt reverted to IncomingFile successfully", incomingFile.id)
            )
        } else {
            ResponseEntity.badRequest().body(
                ReceiptOperationResponse(false, "Failed to revert Receipt to IncomingFile")
            )
        }
    }
}