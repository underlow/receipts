package me.underlow.receipt.dto

import me.underlow.receipt.model.Bill
import me.underlow.receipt.model.ItemStatus
import me.underlow.receipt.model.Receipt
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * DTO for bill detail view with OCR data and associated receipts
 */
data class BillDetailDto(
    val id: Long,
    val filename: String,
    val filePath: String,
    val uploadDate: LocalDateTime,
    val status: ItemStatus,
    val statusDisplayName: String,
    val ocrRawJson: String?,
    val extractedAmount: Double?,
    val extractedDate: LocalDate?,
    val extractedProvider: String?,
    val imageUrl: String,
    val thumbnailUrl: String,
    val associatedReceipts: List<ReceiptSummaryDto>,
    val checksum: String?,
    val originalIncomingFileId: Long?,
    val ocrProcessedAt: LocalDateTime?,
    val ocrErrorMessage: String?
) {
    companion object {
        fun fromBill(bill: Bill, receipts: List<Receipt> = emptyList()): BillDetailDto {
            return BillDetailDto(
                id = bill.id!!,
                filename = bill.filename,
                filePath = bill.filePath,
                uploadDate = bill.uploadDate,
                status = bill.status,
                statusDisplayName = formatStatus(bill.status),
                ocrRawJson = bill.ocrRawJson,
                extractedAmount = bill.extractedAmount,
                extractedDate = bill.extractedDate,
                extractedProvider = bill.extractedProvider,
                imageUrl = "/api/bills/${bill.id}/image",
                thumbnailUrl = "/api/bills/${bill.id}/thumbnail",
                associatedReceipts = receipts.map { ReceiptSummaryDto.fromReceipt(it) },
                checksum = bill.checksum,
                originalIncomingFileId = bill.originalIncomingFileId,
                ocrProcessedAt = bill.ocrProcessedAt,
                ocrErrorMessage = bill.ocrErrorMessage
            )
        }

        private fun formatStatus(status: ItemStatus): String {
            return when (status) {
                ItemStatus.NEW -> "New"
                ItemStatus.APPROVED -> "Approved"
                ItemStatus.REJECTED -> "Rejected"
            }
        }
    }
}

/**
 * DTO for receipt summary in bill detail view
 */
data class ReceiptSummaryDto(
    val id: Long,
    val thumbnailUrl: String,
    val detailUrl: String
) {
    companion object {
        fun fromReceipt(receipt: Receipt): ReceiptSummaryDto {
            return ReceiptSummaryDto(
                id = receipt.id!!,
                thumbnailUrl = "/api/receipts/${receipt.id}/thumbnail",
                detailUrl = "/receipts/${receipt.id}"
            )
        }
    }
}

/**
 * DTO for bill form submission
 */
data class BillFormDto(
    val extractedAmount: Double?,
    val extractedDate: LocalDate?,
    val extractedProvider: String?,
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
 * DTO for bill operation responses
 */
data class BillOperationResponse(
    val success: Boolean,
    val message: String,
    val billId: Long? = null,
    val paymentId: Long? = null
)