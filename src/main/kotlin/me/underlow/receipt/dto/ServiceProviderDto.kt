package me.underlow.receipt.dto

import me.underlow.receipt.model.ServiceProvider

/**
 * DTO for service provider dropdown options
 */
data class ServiceProviderDto(
    val id: Long,
    val name: String,
    val category: String,
    val defaultPaymentMethod: String?,
    val isActive: Boolean,
    val comment: String?
) {
    companion object {
        fun fromServiceProvider(serviceProvider: ServiceProvider): ServiceProviderDto {
            return ServiceProviderDto(
                id = serviceProvider.id!!,
                name = serviceProvider.name,
                category = serviceProvider.category,
                defaultPaymentMethod = serviceProvider.defaultPaymentMethod,
                isActive = serviceProvider.isActive,
                comment = serviceProvider.comment
            )
        }
    }
}

/**
 * DTO for service provider form submission
 */
data class ServiceProviderFormDto(
    val name: String,
    val category: String,
    val defaultPaymentMethod: String?,
    val isActive: Boolean = true,
    val comment: String?
)

/**
 * DTO for service provider operation responses
 */
data class ServiceProviderOperationResponse(
    val success: Boolean,
    val message: String,
    val serviceProviderId: Long? = null
)

/**
 * DTO for service provider with category grouping
 */
data class ServiceProvidersByCategory(
    val category: String,
    val providers: List<ServiceProviderDto>
)

/**
 * DTO for service provider dropdown option (simplified)
 */
data class ServiceProviderOption(
    val id: Long,
    val name: String,
    val category: String
) {
    companion object {
        fun fromServiceProvider(serviceProvider: ServiceProvider): ServiceProviderOption {
            return ServiceProviderOption(
                id = serviceProvider.id!!,
                name = serviceProvider.name,
                category = serviceProvider.category
            )
        }
    }
}