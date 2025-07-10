package me.underlow.receipt.service

import me.underlow.receipt.dao.ServiceProviderDao
import me.underlow.receipt.model.ServiceProvider
import me.underlow.receipt.model.ServiceProviderState
import me.underlow.receipt.model.RegularFrequency
import org.springframework.stereotype.Service
import com.fasterxml.jackson.databind.ObjectMapper
import java.io.IOException

/**
 * Service for managing service provider operations including business logic and validation.
 * Provides CRUD operations, state transitions, and validation for service providers.
 */
@Service
class ServiceProviderService(
    private val serviceProviderDao: ServiceProviderDao
) {
    
    private val objectMapper = ObjectMapper()
    
    /**
     * Creates a new service provider with validation.
     * Validates that the name is not blank and does not already exist.
     * 
     * @param name Service provider name
     * @return Created service provider
     * @throws IllegalArgumentException if name is blank or already exists
     */
    fun createServiceProvider(name: String): ServiceProvider {
        if (name.isBlank()) {
            throw IllegalArgumentException("Service provider name cannot be blank")
        }
        
        if (serviceProviderDao.existsByName(name)) {
            throw IllegalArgumentException("Service provider with name '$name' already exists")
        }
        
        val serviceProvider = ServiceProvider.createNew(name)
        return serviceProviderDao.save(serviceProvider)
    }
    
    /**
     * Updates an existing service provider with validation.
     * Validates that the service provider exists, the name is not blank,
     * and the name does not conflict with other service providers.
     * 
     * @param id Service provider ID
     * @param name New service provider name
     * @param comment New comment (optional)
     * @param commentForOcr New OCR comment (optional)
     * @param regular New regular frequency
     * @param customFields New custom fields JSON (optional)
     * @return Updated service provider
     * @throws IllegalArgumentException if validation fails
     */
    fun updateServiceProvider(
        id: Long,
        name: String,
        comment: String?,
        commentForOcr: String?,
        regular: RegularFrequency,
        customFields: String?
    ): ServiceProvider {
        val existingProvider = serviceProviderDao.findById(id)
            ?: throw IllegalArgumentException("Service provider with ID $id not found")
        
        if (name.isBlank()) {
            throw IllegalArgumentException("Service provider name cannot be blank")
        }
        
        // Check for duplicate name only if the name is being changed
        if (name != existingProvider.name && serviceProviderDao.existsByName(name)) {
            throw IllegalArgumentException("Service provider with name '$name' already exists")
        }
        
        // Validate custom fields if provided
        if (customFields != null && !validateCustomFields(customFields)) {
            throw IllegalArgumentException("Invalid custom fields JSON format")
        }
        
        val updatedProvider = existingProvider
            .updateName(name)
            .updateComment(comment)
            .updateCommentForOcr(commentForOcr)
            .updateRegular(regular)
            .updateCustomFields(customFields)
        
        return serviceProviderDao.save(updatedProvider)
    }
    
    /**
     * Hides a service provider by transitioning it to HIDDEN state.
     * Hidden service providers are preserved for historical data but not shown in UI.
     * 
     * @param id Service provider ID
     * @return Hidden service provider
     * @throws IllegalArgumentException if service provider not found
     * @throws IllegalStateException if service provider is not in ACTIVE state
     */
    fun hideServiceProvider(id: Long): ServiceProvider {
        val existingProvider = serviceProviderDao.findById(id)
            ?: throw IllegalArgumentException("Service provider with ID $id not found")
        
        val hiddenProvider = existingProvider.hide()
        return serviceProviderDao.save(hiddenProvider)
    }
    
    /**
     * Shows a service provider by transitioning it to ACTIVE state.
     * Active service providers are visible in UI and can be used for new bills/receipts.
     * 
     * @param id Service provider ID
     * @return Active service provider
     * @throws IllegalArgumentException if service provider not found
     * @throws IllegalStateException if service provider is not in HIDDEN state
     */
    fun showServiceProvider(id: Long): ServiceProvider {
        val existingProvider = serviceProviderDao.findById(id)
            ?: throw IllegalArgumentException("Service provider with ID $id not found")
        
        val activeProvider = existingProvider.show()
        return serviceProviderDao.save(activeProvider)
    }
    
    /**
     * Finds a service provider by ID.
     * 
     * @param id Service provider ID
     * @return Service provider if found, null otherwise
     */
    fun findById(id: Long): ServiceProvider? {
        return serviceProviderDao.findById(id)
    }
    
    /**
     * Finds all service providers.
     * 
     * @return List of all service providers
     */
    fun findAll(): List<ServiceProvider> {
        return serviceProviderDao.findAll()
    }
    
    /**
     * Finds service providers by state.
     * 
     * @param state Service provider state to filter by
     * @return List of service providers with matching state
     */
    fun findByState(state: ServiceProviderState): List<ServiceProvider> {
        return serviceProviderDao.findByState(state)
    }
    
    /**
     * Finds active service providers that have custom fields.
     * 
     * @return List of active service providers with custom fields
     */
    fun findActiveProvidersWithCustomFields(): List<ServiceProvider> {
        return serviceProviderDao.findActiveProvidersWithCustomFields()
    }
    
    /**
     * Updates the avatar of a service provider.
     * 
     * @param id Service provider ID
     * @param avatar New avatar path
     * @return Updated service provider
     * @throws IllegalArgumentException if service provider not found
     */
    fun updateAvatar(id: Long, avatar: String?): ServiceProvider {
        val existingProvider = serviceProviderDao.findById(id)
            ?: throw IllegalArgumentException("Service provider with ID $id not found")
        
        val updatedProvider = existingProvider.updateAvatar(avatar)
        return serviceProviderDao.save(updatedProvider)
    }
    
    /**
     * Validates custom fields JSON format.
     * 
     * @param customFields JSON string to validate
     * @return true if valid JSON or null, false otherwise
     */
    fun validateCustomFields(customFields: String?): Boolean {
        if (customFields == null || customFields.isBlank()) {
            return true
        }
        
        return try {
            objectMapper.readTree(customFields)
            true
        } catch (e: IOException) {
            false
        }
    }
    
    /**
     * Checks if a service provider name already exists.
     * 
     * @param name Service provider name to check
     * @return true if name exists, false otherwise
     */
    fun existsByName(name: String): Boolean {
        return serviceProviderDao.existsByName(name)
    }
}