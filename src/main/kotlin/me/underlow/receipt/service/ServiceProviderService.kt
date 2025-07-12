package me.underlow.receipt.service

import me.underlow.receipt.dao.ServiceProviderDao
import me.underlow.receipt.model.ServiceProvider
import me.underlow.receipt.model.ServiceProviderState
import me.underlow.receipt.model.RegularFrequency
import org.springframework.stereotype.Service
import com.fasterxml.jackson.databind.ObjectMapper
import java.io.IOException
import org.slf4j.LoggerFactory

/**
 * Service for managing service provider operations including business logic and validation.
 * Provides CRUD operations, state transitions, and validation for service providers.
 */
@Service
class ServiceProviderService(
    private val serviceProviderDao: ServiceProviderDao
) {
    
    private val logger = LoggerFactory.getLogger(ServiceProviderService::class.java)
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
        return createServiceProvider(name, null, null, RegularFrequency.NOT_REGULAR, null)
    }
    
    /**
     * Creates a new service provider with all fields and validation.
     * Validates that the name is not blank and does not already exist.
     * 
     * @param name Service provider name
     * @param comment Service provider comment (optional)
     * @param commentForOcr OCR comment (optional)
     * @param regular Regular frequency
     * @param customFields Custom fields JSON (optional)
     * @return Created service provider
     * @throws IllegalArgumentException if name is blank or already exists, or custom fields are invalid
     */
    fun createServiceProvider(
        name: String,
        comment: String?,
        commentForOcr: String?,
        regular: RegularFrequency,
        customFields: String?
    ): ServiceProvider {
        logger.info("Creating service provider with name: $name")
        
        if (name.isBlank()) {
            logger.warn("Attempted to create service provider with blank name")
            throw IllegalArgumentException("Service provider name cannot be blank")
        }
        
        if (serviceProviderDao.existsByName(name)) {
            logger.warn("Attempted to create service provider with existing name: $name")
            throw IllegalArgumentException("Service provider with name '$name' already exists")
        }
        
        // Validate custom fields if provided
        if (customFields != null && !validateCustomFields(customFields)) {
            logger.warn("Invalid custom fields JSON provided for new service provider with name: $name")
            throw IllegalArgumentException("Invalid custom fields JSON format")
        }
        
        val serviceProvider = ServiceProvider.createNew(name)
            .updateComment(comment)
            .updateCommentForOcr(commentForOcr)
            .updateRegular(regular)
            .updateCustomFields(customFields)
            
        val savedProvider = serviceProviderDao.save(serviceProvider)
        logger.info("Successfully created service provider with ID: ${savedProvider.id} and name: $name")
        return savedProvider
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
        logger.info("Updating service provider with ID: $id, new name: $name")
        
        val existingProvider = serviceProviderDao.findById(id)
            ?: throw IllegalArgumentException("Service provider with ID $id not found").also {
                logger.warn("Attempted to update non-existent service provider with ID: $id")
            }
        
        if (name.isBlank()) {
            logger.warn("Attempted to update service provider ID: $id with blank name")
            throw IllegalArgumentException("Service provider name cannot be blank")
        }
        
        // Check for duplicate name only if the name is being changed
        if (name != existingProvider.name && serviceProviderDao.existsByName(name)) {
            logger.warn("Attempted to update service provider ID: $id with existing name: $name")
            throw IllegalArgumentException("Service provider with name '$name' already exists")
        }
        
        // Validate custom fields if provided
        if (customFields != null && !validateCustomFields(customFields)) {
            logger.warn("Invalid custom fields JSON provided for service provider ID: $id")
            throw IllegalArgumentException("Invalid custom fields JSON format")
        }
        
        val updatedProvider = existingProvider
            .updateName(name)
            .updateComment(comment)
            .updateCommentForOcr(commentForOcr)
            .updateRegular(regular)
            .updateCustomFields(customFields)
        
        val savedProvider = serviceProviderDao.save(updatedProvider)
        logger.info("Successfully updated service provider with ID: $id")
        return savedProvider
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
        logger.info("Hiding service provider with ID: $id")
        
        val existingProvider = serviceProviderDao.findById(id)
            ?: throw IllegalArgumentException("Service provider with ID $id not found").also {
                logger.warn("Attempted to hide non-existent service provider with ID: $id")
            }
        
        val hiddenProvider = existingProvider.hide()
        val savedProvider = serviceProviderDao.save(hiddenProvider)
        logger.info("Successfully hidden service provider with ID: $id")
        return savedProvider
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
        logger.info("Showing service provider with ID: $id")
        
        val existingProvider = serviceProviderDao.findById(id)
            ?: throw IllegalArgumentException("Service provider with ID $id not found").also {
                logger.warn("Attempted to show non-existent service provider with ID: $id")
            }
        
        val activeProvider = existingProvider.show()
        val savedProvider = serviceProviderDao.save(activeProvider)
        logger.info("Successfully shown service provider with ID: $id")
        return savedProvider
    }
    
    /**
     * Finds a service provider by ID.
     * 
     * @param id Service provider ID
     * @return Service provider if found, null otherwise
     */
    fun findById(id: Long): ServiceProvider? {
        logger.debug("Finding service provider by ID: $id")
        val provider = serviceProviderDao.findById(id)
        if (provider != null) {
            logger.debug("Found service provider with ID: $id")
        } else {
            logger.debug("Service provider with ID: $id not found")
        }
        return provider
    }
    
    /**
     * Finds all service providers.
     * 
     * @return List of all service providers
     */
    fun findAll(): List<ServiceProvider> {
        logger.debug("Finding all service providers")
        val providers = serviceProviderDao.findAll()
        logger.debug("Found ${providers.size} service providers")
        return providers
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
        logger.info("Updating avatar for service provider with ID: $id, avatar: $avatar")
        
        val existingProvider = serviceProviderDao.findById(id)
            ?: throw IllegalArgumentException("Service provider with ID $id not found").also {
                logger.warn("Attempted to update avatar for non-existent service provider with ID: $id")
            }
        
        val updatedProvider = existingProvider.updateAvatar(avatar)
        val savedProvider = serviceProviderDao.save(updatedProvider)
        logger.info("Successfully updated avatar for service provider with ID: $id")
        return savedProvider
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
            logger.debug("Custom fields JSON validation successful")
            true
        } catch (e: IOException) {
            logger.warn("Custom fields JSON validation failed: ${e.message}")
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