package me.underlow.receipt.model

import java.time.LocalDateTime

/**
 * Represents a service provider entity in the financial document management system.
 * A service provider represents companies, utilities, or vendors that issue bills and receipts.
 * Service providers can be managed by users to categorize and organize their financial documents.
 */
data class ServiceProvider(
    val id: Long,
    val name: String,
    val avatar: String? = null,
    val comment: String? = null,
    val commentForOcr: String? = null,
    val regular: RegularFrequency = RegularFrequency.NOT_REGULAR,
    val customFields: String? = null,
    val state: ServiceProviderState = ServiceProviderState.ACTIVE,
    val createdDate: LocalDateTime,
    val modifiedDate: LocalDateTime
) {
    /**
     * Transitions the service provider from ACTIVE to HIDDEN state.
     * Hidden service providers are preserved for historical data but not shown in UI.
     * 
     * @return new ServiceProvider instance in HIDDEN state
     * @throws IllegalStateException if the current state is not ACTIVE
     */
    fun hide(): ServiceProvider {
        if (state != ServiceProviderState.ACTIVE) {
            throw IllegalStateException("Cannot hide service provider from state $state")
        }
        
        return copy(state = ServiceProviderState.HIDDEN, modifiedDate = LocalDateTime.now())
    }

    /**
     * Transitions the service provider from HIDDEN to ACTIVE state.
     * Active service providers are visible in UI and can be used for new bills/receipts.
     * 
     * @return new ServiceProvider instance in ACTIVE state
     * @throws IllegalStateException if the current state is not HIDDEN
     */
    fun show(): ServiceProvider {
        if (state != ServiceProviderState.HIDDEN) {
            throw IllegalStateException("Cannot show service provider from state $state")
        }
        
        return copy(state = ServiceProviderState.ACTIVE, modifiedDate = LocalDateTime.now())
    }

    /**
     * Checks if the service provider is in active state.
     * Active service providers are visible and can be used for new bills/receipts.
     * 
     * @return true if the service provider is active (ACTIVE), false otherwise
     */
    fun isActive(): Boolean {
        return state == ServiceProviderState.ACTIVE
    }

    /**
     * Updates the name of the service provider.
     * The name cannot be blank.
     * 
     * @param newName the new name for the service provider
     * @return new ServiceProvider instance with updated name and modified date
     * @throws IllegalArgumentException if the name is blank
     */
    fun updateName(newName: String): ServiceProvider {
        if (newName.isBlank()) {
            throw IllegalArgumentException("Service provider name cannot be blank")
        }
        
        return copy(name = newName, modifiedDate = LocalDateTime.now())
    }

    /**
     * Updates the avatar path of the service provider.
     * The avatar can be null to clear the current avatar.
     * 
     * @param newAvatar the new avatar path for the service provider, or null to clear
     * @return new ServiceProvider instance with updated avatar and modified date
     */
    fun updateAvatar(newAvatar: String?): ServiceProvider {
        return copy(avatar = newAvatar, modifiedDate = LocalDateTime.now())
    }

    /**
     * Updates the comment of the service provider.
     * The comment can be null to clear the current comment.
     * 
     * @param newComment the new comment for the service provider, or null to clear
     * @return new ServiceProvider instance with updated comment and modified date
     */
    fun updateComment(newComment: String?): ServiceProvider {
        return copy(comment = newComment, modifiedDate = LocalDateTime.now())
    }

    /**
     * Updates the OCR comment of the service provider.
     * The OCR comment can be null to clear the current OCR comment.
     * 
     * @param newCommentForOcr the new OCR comment for the service provider, or null to clear
     * @return new ServiceProvider instance with updated OCR comment and modified date
     */
    fun updateCommentForOcr(newCommentForOcr: String?): ServiceProvider {
        return copy(commentForOcr = newCommentForOcr, modifiedDate = LocalDateTime.now())
    }

    /**
     * Updates the regular frequency of the service provider.
     * 
     * @param newRegular the new regular frequency for the service provider
     * @return new ServiceProvider instance with updated regular frequency and modified date
     */
    fun updateRegular(newRegular: RegularFrequency): ServiceProvider {
        return copy(regular = newRegular, modifiedDate = LocalDateTime.now())
    }

    /**
     * Updates the custom fields JSON of the service provider.
     * The custom fields can be null to clear all custom fields.
     * 
     * @param newCustomFields the new custom fields JSON for the service provider, or null to clear
     * @return new ServiceProvider instance with updated custom fields and modified date
     */
    fun updateCustomFields(newCustomFields: String?): ServiceProvider {
        return copy(customFields = newCustomFields, modifiedDate = LocalDateTime.now())
    }

    companion object {
        /**
         * Creates a new service provider entity with default values.
         * Used when users create new service providers manually.
         * 
         * @param name name of the service provider
         * @param createdDate timestamp when the service provider was created
         * @return new ServiceProvider instance with default values
         */
        fun createNew(
            name: String,
            createdDate: LocalDateTime = LocalDateTime.now()
        ): ServiceProvider {
            if (name.isBlank()) {
                throw IllegalArgumentException("Service provider name cannot be blank")
            }
            
            return ServiceProvider(
                id = 0L, // Placeholder ID for new entities
                name = name,
                avatar = null,
                comment = null,
                commentForOcr = null,
                regular = RegularFrequency.NOT_REGULAR,
                customFields = null,
                state = ServiceProviderState.ACTIVE,
                createdDate = createdDate,
                modifiedDate = createdDate
            )
        }
    }
}

/**
 * Represents the current state of a service provider entity in the system.
 */
enum class ServiceProviderState {
    /** Service provider is visible and can be used for new bills/receipts */
    ACTIVE,
    
    /** Service provider is hidden from UI but preserved for historical data */
    HIDDEN
}

/**
 * Represents the frequency of bills from a service provider.
 */
enum class RegularFrequency {
    /** Bills are issued yearly */
    YEARLY,
    
    /** Bills are issued monthly */
    MONTHLY,
    
    /** Bills are issued weekly */
    WEEKLY,
    
    /** Bills are not issued on a regular schedule */
    NOT_REGULAR
}