package me.underlow.receipt.service

import me.underlow.receipt.model.ServiceProvider
import me.underlow.receipt.repository.ServiceProviderRepository
import org.springframework.stereotype.Service

/**
 * Business logic service for managing ServiceProvider entities
 */
@Service
class ServiceProviderService(
    private val serviceProviderRepository: ServiceProviderRepository
) {

    /**
     * Finds a ServiceProvider by ID
     */
    fun findById(id: Long): ServiceProvider? {
        return serviceProviderRepository.findById(id)
    }

    /**
     * Finds all ServiceProviders
     */
    fun findAll(): List<ServiceProvider> {
        return serviceProviderRepository.findAll()
    }

    /**
     * Finds all active ServiceProviders
     */
    fun findAllActive(): List<ServiceProvider> {
        return serviceProviderRepository.findAll().filter { it.isActive }
    }

    /**
     * Finds ServiceProviders by category
     */
    fun findByCategory(category: String): List<ServiceProvider> {
        return serviceProviderRepository.findAll().filter { 
            it.category.equals(category, ignoreCase = true) 
        }
    }

    /**
     * Creates a new ServiceProvider
     */
    fun createServiceProvider(
        name: String,
        category: String,
        defaultPaymentMethod: String = "",
        comment: String? = null
    ): ServiceProvider {
        val serviceProvider = ServiceProvider(
            name = name,
            category = category,
            defaultPaymentMethod = defaultPaymentMethod,
            isActive = true,
            comment = comment
        )
        
        return serviceProviderRepository.save(serviceProvider)
    }

    /**
     * Updates an existing ServiceProvider
     */
    fun updateServiceProvider(
        id: Long,
        name: String,
        category: String,
        defaultPaymentMethod: String = "",
        isActive: Boolean,
        comment: String? = null
    ): ServiceProvider? {
        val existingProvider = serviceProviderRepository.findById(id) ?: return null
        
        val updatedProvider = existingProvider.copy(
            name = name,
            category = category,
            defaultPaymentMethod = defaultPaymentMethod,
            isActive = isActive,
            comment = comment
        )
        
        return serviceProviderRepository.save(updatedProvider)
    }

    /**
     * Activates a ServiceProvider
     */
    fun activateServiceProvider(id: Long): ServiceProvider? {
        val provider = serviceProviderRepository.findById(id) ?: return null
        val updatedProvider = provider.copy(isActive = true)
        return serviceProviderRepository.save(updatedProvider)
    }

    /**
     * Deactivates a ServiceProvider
     */
    fun deactivateServiceProvider(id: Long): ServiceProvider? {
        val provider = serviceProviderRepository.findById(id) ?: return null
        val updatedProvider = provider.copy(isActive = false)
        return serviceProviderRepository.save(updatedProvider)
    }

    /**
     * Deletes a ServiceProvider
     */
    fun deleteServiceProvider(id: Long): Boolean {
        return serviceProviderRepository.delete(id)
    }

    /**
     * Searches ServiceProviders by name
     */
    fun searchByName(searchTerm: String): List<ServiceProvider> {
        return serviceProviderRepository.findAll().filter { 
            it.name.contains(searchTerm, ignoreCase = true) 
        }
    }

    /**
     * Gets all unique categories
     */
    fun getAllCategories(): List<String> {
        return serviceProviderRepository.findAll()
            .map { it.category }
            .distinct()
            .sorted()
    }

    /**
     * Gets statistics about ServiceProviders
     */
    fun getStatistics(): Map<String, Any> {
        val providers = serviceProviderRepository.findAll()
        val activeCount = providers.count { it.isActive }
        val inactiveCount = providers.count { !it.isActive }
        val categoryCounts = providers.groupingBy { it.category }.eachCount()
        
        return mapOf(
            "totalProviders" to providers.size,
            "activeProviders" to activeCount,
            "inactiveProviders" to inactiveCount,
            "categoryCounts" to categoryCounts
        )
    }
}