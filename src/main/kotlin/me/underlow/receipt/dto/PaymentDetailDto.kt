package me.underlow.receipt.dto

import me.underlow.receipt.model.Payment
import me.underlow.receipt.model.ServiceProvider
import me.underlow.receipt.model.PaymentMethod
import me.underlow.receipt.model.PaymentMethodType
import java.math.BigDecimal
import java.time.LocalDate

/**
 * DTO for payment detail view with provider/method information
 */
data class PaymentDetailDto(
    val id: Long,
    val serviceProviderId: Long,
    val serviceProviderName: String,
    val serviceProviderCategory: String,
    val paymentMethodId: Long,
    val paymentMethodName: String,
    val paymentMethodType: PaymentMethodType,
    val amount: BigDecimal,
    val currency: String,
    val invoiceDate: LocalDate,
    val paymentDate: LocalDate,
    val billId: Long?,
    val comment: String?
) {
    companion object {
        fun fromPayment(
            payment: Payment,
            serviceProvider: ServiceProvider,
            paymentMethod: PaymentMethod
        ): PaymentDetailDto {
            return PaymentDetailDto(
                id = payment.id!!,
                serviceProviderId = payment.serviceProviderId,
                serviceProviderName = serviceProvider.name,
                serviceProviderCategory = serviceProvider.category,
                paymentMethodId = payment.paymentMethodId,
                paymentMethodName = paymentMethod.name,
                paymentMethodType = paymentMethod.type,
                amount = payment.amount,
                currency = payment.currency,
                invoiceDate = payment.invoiceDate,
                paymentDate = payment.paymentDate,
                billId = payment.billId,
                comment = payment.comment
            )
        }
    }
}

/**
 * DTO for payment form submission
 */
data class PaymentFormDto(
    val serviceProviderId: Long,
    val paymentMethodId: Long,
    val amount: BigDecimal,
    val currency: String = "USD",
    val invoiceDate: LocalDate,
    val paymentDate: LocalDate,
    val comment: String?
)

/**
 * DTO for payment operation responses
 */
data class PaymentOperationResponse(
    val success: Boolean,
    val message: String,
    val paymentId: Long? = null
)

/**
 * DTO for payment summary in lists
 */
data class PaymentSummaryDto(
    val id: Long,
    val serviceProviderName: String,
    val serviceProviderCategory: String,
    val paymentMethodName: String,
    val paymentMethodType: PaymentMethodType,
    val amount: BigDecimal,
    val currency: String,
    val invoiceDate: LocalDate,
    val paymentDate: LocalDate,
    val billId: Long?,
    val comment: String?
) {
    companion object {
        fun fromPayment(
            payment: Payment,
            serviceProvider: ServiceProvider,
            paymentMethod: PaymentMethod
        ): PaymentSummaryDto {
            return PaymentSummaryDto(
                id = payment.id!!,
                serviceProviderName = serviceProvider.name,
                serviceProviderCategory = serviceProvider.category,
                paymentMethodName = paymentMethod.name,
                paymentMethodType = paymentMethod.type,
                amount = payment.amount,
                currency = payment.currency,
                invoiceDate = payment.invoiceDate,
                paymentDate = payment.paymentDate,
                billId = payment.billId,
                comment = payment.comment
            )
        }
    }
}