package me.underlow.receipt.model

import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Represents a receipt entity in the financial document management system.
 * A receipt entity tracks expense records that users need to manage,
 * including receipts created from approved inbox items and manually entered receipts.
 */
data class ReceiptEntity(
    val id: String,
    val paymentTypeId: String,
    val paymentDate: LocalDate,
    val amount: BigDecimal,
    val inboxEntityId: String? = null,
    val state: ReceiptState = ReceiptState.CREATED,
    val createdDate: LocalDateTime,
    val description: String? = null,
    val merchantName: String? = null
) {
    /**
     * Transitions the receipt entity from CREATED to REMOVED state.
     * Once removed, the receipt is no longer active and cannot be modified.
     * 
     * @return new ReceiptEntity instance in REMOVED state
     * @throws IllegalStateException if the current state is not CREATED
     */
    fun remove(): ReceiptEntity {
        if (state != ReceiptState.CREATED) {
            throw IllegalStateException("Cannot remove receipt from state $state")
        }
        
        return copy(state = ReceiptState.REMOVED)
    }

    /**
     * Checks if the receipt entity is in active state.
     * Active receipts are those that are currently being tracked and managed.
     * 
     * @return true if the receipt is active (CREATED), false otherwise
     */
    fun isActive(): Boolean {
        return state == ReceiptState.CREATED
    }

    /**
     * Updates the amount of the receipt entity.
     * The amount must be greater than zero.
     * 
     * @param newAmount the new amount for the receipt
     * @return new ReceiptEntity instance with updated amount
     * @throws IllegalArgumentException if the amount is negative or zero
     */
    fun updateAmount(newAmount: BigDecimal): ReceiptEntity {
        if (newAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw IllegalArgumentException("Receipt amount cannot be negative")
        }
        if (newAmount.compareTo(BigDecimal.ZERO) == 0) {
            throw IllegalArgumentException("Receipt amount must be greater than zero")
        }
        
        return copy(amount = newAmount)
    }

    /**
     * Updates the payment type of the receipt entity.
     * The payment type ID cannot be blank.
     * 
     * @param paymentTypeId the new payment type ID
     * @return new ReceiptEntity instance with updated payment type
     * @throws IllegalArgumentException if the payment type ID is blank
     */
    fun updatePaymentType(paymentTypeId: String): ReceiptEntity {
        if (paymentTypeId.isBlank()) {
            throw IllegalArgumentException("Payment type ID cannot be blank")
        }
        
        return copy(paymentTypeId = paymentTypeId)
    }

    /**
     * Updates the merchant name of the receipt entity.
     * If the merchant name is null or blank, it will be set to null.
     * 
     * @param merchantName the new merchant name, or null to clear
     * @return new ReceiptEntity instance with updated merchant name
     */
    fun updateMerchant(merchantName: String?): ReceiptEntity {
        val normalizedMerchantName = if (merchantName.isNullOrBlank()) null else merchantName
        return copy(merchantName = normalizedMerchantName)
    }

    companion object {
        /**
         * Creates a new receipt entity from an approved inbox item.
         * Links the receipt to the original inbox entity that was processed.
         * 
         * @param id unique identifier for the receipt
         * @param paymentTypeId identifier of the payment type
         * @param paymentDate date of the payment
         * @param amount amount of the receipt
         * @param inboxEntityId identifier of the linked inbox entity
         * @param description optional description for the receipt
         * @param merchantName optional merchant name for the receipt
         * @param createdDate timestamp when the receipt was created
         * @return new ReceiptEntity instance created from inbox
         */
        fun createFromInbox(
            id: String,
            paymentTypeId: String,
            paymentDate: LocalDate,
            amount: BigDecimal,
            inboxEntityId: String,
            description: String? = null,
            merchantName: String? = null,
            createdDate: LocalDateTime = LocalDateTime.now()
        ): ReceiptEntity {
            return ReceiptEntity(
                id = id,
                paymentTypeId = paymentTypeId,
                paymentDate = paymentDate,
                amount = amount,
                inboxEntityId = inboxEntityId,
                state = ReceiptState.CREATED,
                createdDate = createdDate,
                description = description,
                merchantName = merchantName
            )
        }

        /**
         * Creates a new receipt entity manually without linking to an inbox item.
         * Used when users enter receipts directly without going through the OCR process.
         * 
         * @param id unique identifier for the receipt
         * @param paymentTypeId identifier of the payment type
         * @param paymentDate date of the payment
         * @param amount amount of the receipt
         * @param description optional description for the receipt
         * @param merchantName optional merchant name for the receipt
         * @param createdDate timestamp when the receipt was created
         * @return new ReceiptEntity instance created manually
         */
        fun createManually(
            id: String,
            paymentTypeId: String,
            paymentDate: LocalDate,
            amount: BigDecimal,
            description: String? = null,
            merchantName: String? = null,
            createdDate: LocalDateTime = LocalDateTime.now()
        ): ReceiptEntity {
            return ReceiptEntity(
                id = id,
                paymentTypeId = paymentTypeId,
                paymentDate = paymentDate,
                amount = amount,
                inboxEntityId = null,
                state = ReceiptState.CREATED,
                createdDate = createdDate,
                description = description,
                merchantName = merchantName
            )
        }
    }
}

/**
 * Represents the current state of a receipt entity in the system.
 */
enum class ReceiptState {
    /** Receipt exists and is active, can be modified and managed */
    CREATED,
    
    /** Receipt has been deleted and is no longer active */
    REMOVED
}