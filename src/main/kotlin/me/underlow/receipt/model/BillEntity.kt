package me.underlow.receipt.model

import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Represents a bill entity in the financial document management system.
 * A bill entity tracks financial obligations that users need to manage,
 * including bills created from approved inbox items and manually entered bills.
 */
data class BillEntity(
    val id: String,
    val serviceProviderId: String,
    val billDate: LocalDate,
    val amount: BigDecimal,
    val inboxEntityId: String? = null,
    val state: BillState = BillState.CREATED,
    val createdDate: LocalDateTime,
    val description: String? = null
) {
    /**
     * Transitions the bill entity from CREATED to REMOVED state.
     * Once removed, the bill is no longer active and cannot be modified.
     * 
     * @return new BillEntity instance in REMOVED state
     * @throws IllegalStateException if the current state is not CREATED
     */
    fun remove(): BillEntity {
        if (state != BillState.CREATED) {
            throw IllegalStateException("Cannot remove bill from state $state")
        }
        
        return copy(state = BillState.REMOVED)
    }

    /**
     * Checks if the bill entity can be removed by the user.
     * Only bills in CREATED state can be removed.
     * 
     * @return true if the bill can be removed, false otherwise
     */
    fun canRemove(): Boolean {
        return state == BillState.CREATED
    }

    /**
     * Checks if the bill entity is in active state.
     * Active bills are those that are currently being tracked and managed.
     * 
     * @return true if the bill is active (CREATED), false otherwise
     */
    fun isActive(): Boolean {
        return state == BillState.CREATED
    }

    /**
     * Updates the amount of the bill entity.
     * The amount must be greater than zero.
     * 
     * @param newAmount the new amount for the bill
     * @return new BillEntity instance with updated amount
     * @throws IllegalArgumentException if the amount is negative or zero
     */
    fun updateAmount(newAmount: BigDecimal): BillEntity {
        if (newAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw IllegalArgumentException("Bill amount cannot be negative")
        }
        if (newAmount.compareTo(BigDecimal.ZERO) == 0) {
            throw IllegalArgumentException("Bill amount must be greater than zero")
        }
        
        return copy(amount = newAmount)
    }

    /**
     * Updates the service provider of the bill entity.
     * The service provider ID cannot be blank.
     * 
     * @param providerId the new service provider ID
     * @return new BillEntity instance with updated service provider
     * @throws IllegalArgumentException if the provider ID is blank
     */
    fun updateServiceProvider(providerId: String): BillEntity {
        if (providerId.isBlank()) {
            throw IllegalArgumentException("Service provider ID cannot be blank")
        }
        
        return copy(serviceProviderId = providerId)
    }

    companion object {
        /**
         * Creates a new bill entity from an approved inbox item.
         * Links the bill to the original inbox entity that was processed.
         * 
         * @param id unique identifier for the bill
         * @param serviceProviderId identifier of the service provider
         * @param billDate date of the bill
         * @param amount amount of the bill
         * @param inboxEntityId identifier of the linked inbox entity
         * @param description optional description for the bill
         * @param createdDate timestamp when the bill was created
         * @return new BillEntity instance created from inbox
         */
        fun createFromInbox(
            id: String,
            serviceProviderId: String,
            billDate: LocalDate,
            amount: BigDecimal,
            inboxEntityId: String,
            description: String? = null,
            createdDate: LocalDateTime = LocalDateTime.now()
        ): BillEntity {
            return BillEntity(
                id = id,
                serviceProviderId = serviceProviderId,
                billDate = billDate,
                amount = amount,
                inboxEntityId = inboxEntityId,
                state = BillState.CREATED,
                createdDate = createdDate,
                description = description
            )
        }

        /**
         * Creates a new bill entity manually without linking to an inbox item.
         * Used when users enter bills directly without going through the OCR process.
         * 
         * @param id unique identifier for the bill
         * @param serviceProviderId identifier of the service provider
         * @param billDate date of the bill
         * @param amount amount of the bill
         * @param description optional description for the bill
         * @param createdDate timestamp when the bill was created
         * @return new BillEntity instance created manually
         */
        fun createManually(
            id: String,
            serviceProviderId: String,
            billDate: LocalDate,
            amount: BigDecimal,
            description: String? = null,
            createdDate: LocalDateTime = LocalDateTime.now()
        ): BillEntity {
            return BillEntity(
                id = id,
                serviceProviderId = serviceProviderId,
                billDate = billDate,
                amount = amount,
                inboxEntityId = null,
                state = BillState.CREATED,
                createdDate = createdDate,
                description = description
            )
        }
    }
}

/**
 * Represents the current state of a bill entity in the system.
 */
enum class BillState {
    /** Bill exists and is active, can be modified and managed */
    CREATED,
    
    /** Bill has been deleted and is no longer active */
    REMOVED
}