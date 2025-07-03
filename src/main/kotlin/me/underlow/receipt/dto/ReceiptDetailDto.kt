package me.underlow.receipt.dto

import me.underlow.receipt.model.Receipt
import me.underlow.receipt.model.Bill
import me.underlow.receipt.model.BillStatus
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * DTO for receipt detail view with bill association options
 */
data class ReceiptDetailDto(
    val id: Long,
    val userId: Long,
    val billId: Long?,
    val associatedBill: BillSummaryDto?,
    val availableBills: List<BillSummaryDto>,
    val imageUrl: String,
    val thumbnailUrl: String,
    val isStandalone: Boolean
) {
    companion object {
        fun fromReceipt(
            receipt: Receipt, 
            associatedBill: Bill? = null,
            availableBills: List<Bill> = emptyList()
        ): ReceiptDetailDto {
            return ReceiptDetailDto(
                id = receipt.id!!,
                userId = receipt.userId,
                billId = receipt.billId,
                associatedBill = associatedBill?.let { BillSummaryDto.fromBill(it) },
                availableBills = availableBills.map { BillSummaryDto.fromBill(it) },
                imageUrl = "/api/receipts/${receipt.id}/image",
                thumbnailUrl = "/api/receipts/${receipt.id}/thumbnail",
                isStandalone = receipt.billId == null
            )
        }
    }
}

/**
 * DTO for bill summary in receipt detail view
 */
data class BillSummaryDto(
    val id: Long,
    val filename: String,
    val uploadDate: LocalDateTime,
    val status: BillStatus,
    val statusDisplayName: String,
    val extractedAmount: Double?,
    val extractedDate: LocalDate?,
    val extractedProvider: String?,
    val thumbnailUrl: String,
    val detailUrl: String
) {
    companion object {
        fun fromBill(bill: Bill): BillSummaryDto {
            return BillSummaryDto(
                id = bill.id!!,
                filename = bill.filename,
                uploadDate = bill.uploadDate,
                status = bill.status,
                statusDisplayName = formatStatus(bill.status),
                extractedAmount = bill.extractedAmount,
                extractedDate = bill.extractedDate,
                extractedProvider = bill.extractedProvider,
                thumbnailUrl = "/api/bills/${bill.id}/thumbnail",
                detailUrl = "/bills/${bill.id}"
            )
        }

        private fun formatStatus(status: BillStatus): String {
            return when (status) {
                BillStatus.PENDING -> "Pending Review"
                BillStatus.PROCESSING -> "Processing"
                BillStatus.APPROVED -> "Approved"
                BillStatus.REJECTED -> "Rejected"
            }
        }
    }
}

/**
 * DTO for receipt form submission
 */
data class ReceiptFormDto(
    val billId: Long?,
    val serviceProviderId: Long?,
    val paymentMethodId: Long?,
    val amount: Double?,
    val currency: String = "USD",
    val invoiceDate: LocalDate?,
    val paymentDate: LocalDate?,
    val comment: String?
) {
    fun hasPaymentData(): Boolean {
        return serviceProviderId != null && 
               paymentMethodId != null && 
               amount != null && 
               invoiceDate != null && 
               paymentDate != null
    }
}

/**
 * DTO for receipt operation responses
 */
data class ReceiptOperationResponse(
    val success: Boolean,
    val message: String,
    val receiptId: Long? = null,
    val billId: Long? = null,
    val paymentId: Long? = null
)

/**
 * DTO for receipt association operations
 */
data class ReceiptAssociationRequest(
    val billId: Long?
)