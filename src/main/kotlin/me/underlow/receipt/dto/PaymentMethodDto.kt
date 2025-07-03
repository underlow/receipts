package me.underlow.receipt.dto

import me.underlow.receipt.model.PaymentMethod
import me.underlow.receipt.model.PaymentMethodType

/**
 * DTO for payment method dropdown options
 */
data class PaymentMethodDto(
    val id: Long,
    val name: String,
    val type: PaymentMethodType,
    val typeDisplayName: String,
    val comment: String?
) {
    companion object {
        fun fromPaymentMethod(paymentMethod: PaymentMethod): PaymentMethodDto {
            return PaymentMethodDto(
                id = paymentMethod.id!!,
                name = paymentMethod.name,
                type = paymentMethod.type,
                typeDisplayName = formatType(paymentMethod.type),
                comment = paymentMethod.comment
            )
        }

        private fun formatType(type: PaymentMethodType): String {
            return when (type) {
                PaymentMethodType.CARD -> "Card"
                PaymentMethodType.BANK -> "Bank Transfer"
                PaymentMethodType.CASH -> "Cash"
                PaymentMethodType.OTHER -> "Other"
            }
        }
    }
}

/**
 * DTO for payment method form submission
 */
data class PaymentMethodFormDto(
    val name: String,
    val type: PaymentMethodType,
    val comment: String?
)

/**
 * DTO for payment method operation responses
 */
data class PaymentMethodOperationResponse(
    val success: Boolean,
    val message: String,
    val paymentMethodId: Long? = null
)

/**
 * DTO for payment method with type grouping
 */
data class PaymentMethodsByType(
    val type: PaymentMethodType,
    val typeDisplayName: String,
    val methods: List<PaymentMethodDto>
) {
    companion object {
        fun fromPaymentMethods(type: PaymentMethodType, methods: List<PaymentMethod>): PaymentMethodsByType {
            return PaymentMethodsByType(
                type = type,
                typeDisplayName = formatType(type),
                methods = methods.map { PaymentMethodDto.fromPaymentMethod(it) }
            )
        }

        private fun formatType(type: PaymentMethodType): String {
            return when (type) {
                PaymentMethodType.CARD -> "Card"
                PaymentMethodType.BANK -> "Bank Transfer"
                PaymentMethodType.CASH -> "Cash"
                PaymentMethodType.OTHER -> "Other"
            }
        }
    }
}

/**
 * DTO for payment method dropdown option (simplified)
 */
data class PaymentMethodOption(
    val id: Long,
    val name: String,
    val type: PaymentMethodType,
    val typeDisplayName: String
) {
    companion object {
        fun fromPaymentMethod(paymentMethod: PaymentMethod): PaymentMethodOption {
            return PaymentMethodOption(
                id = paymentMethod.id!!,
                name = paymentMethod.name,
                type = paymentMethod.type,
                typeDisplayName = formatType(paymentMethod.type)
            )
        }

        private fun formatType(type: PaymentMethodType): String {
            return when (type) {
                PaymentMethodType.CARD -> "Card"
                PaymentMethodType.BANK -> "Bank Transfer"
                PaymentMethodType.CASH -> "Cash"
                PaymentMethodType.OTHER -> "Other"
            }
        }
    }
}