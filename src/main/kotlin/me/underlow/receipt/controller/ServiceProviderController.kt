package me.underlow.receipt.controller

import me.underlow.receipt.model.ServiceProvider
import me.underlow.receipt.model.ServiceProviderState
import me.underlow.receipt.model.RegularFrequency
import me.underlow.receipt.service.ServiceProviderService
import me.underlow.receipt.service.AvatarService
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import jakarta.validation.Valid
import org.slf4j.LoggerFactory

/**
 * REST API controller for Service Provider management.
 * Provides endpoints for CRUD operations, state management, and avatar upload.
 */
@RestController
@RequestMapping("/api/service-providers")
class ServiceProviderController(
    private val serviceProviderService: ServiceProviderService,
    private val avatarService: AvatarService
) {
    
    private val logger = LoggerFactory.getLogger(ServiceProviderController::class.java)

    /**
     * Retrieves all service providers.
     * Returns list of all service providers regardless of state.
     *
     * @return List of service providers
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    fun getAllServiceProviders(): ResponseEntity<List<ServiceProvider>> {
        logger.info("GET /api/service-providers - Retrieving all service providers")
        return try {
            val serviceProviders = serviceProviderService.findAll()
            logger.info("Successfully retrieved ${serviceProviders.size} service providers")
            ResponseEntity.ok(serviceProviders)
        } catch (e: Exception) {
            logger.error("Error retrieving all service providers", e)
            ResponseEntity.internalServerError().build()
        }
    }

    /**
     * Retrieves a specific service provider by ID.
     *
     * @param id Service provider ID
     * @return Service provider if found
     */
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    fun getServiceProvider(@PathVariable id: Long): ResponseEntity<ServiceProvider> {
        logger.info("GET /api/service-providers/$id - Retrieving service provider")
        return try {
            val serviceProvider = serviceProviderService.findById(id)
            if (serviceProvider != null) {
                logger.info("Successfully retrieved service provider with ID: $id")
                ResponseEntity.ok(serviceProvider)
            } else {
                logger.warn("Service provider with ID: $id not found")
                ResponseEntity.notFound().build()
            }
        } catch (e: Exception) {
            logger.error("Error retrieving service provider with ID: $id", e)
            ResponseEntity.internalServerError().build()
        }
    }

    /**
     * Creates a new service provider.
     *
     * @param request Service provider creation request
     * @return Created service provider
     */
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    fun createServiceProvider(@Valid @RequestBody request: CreateServiceProviderRequest): ResponseEntity<ServiceProviderResponse> {
        logger.info("POST /api/service-providers - Creating service provider with name: ${request.name}")
        return try {
            val serviceProvider = serviceProviderService.createServiceProvider(
                name = request.name,
                comment = request.comment,
                commentForOcr = request.commentForOcr,
                regular = request.regular,
                customFields = request.customFields
            )
            logger.info("Successfully created service provider with ID: ${serviceProvider.id}")
            ResponseEntity.ok(ServiceProviderResponse.success(serviceProvider))
        } catch (e: IllegalArgumentException) {
            logger.warn("Failed to create service provider: ${e.message}")
            ResponseEntity.badRequest().body(ServiceProviderResponse.error(e.message ?: "Invalid request"))
        } catch (e: Exception) {
            logger.error("Error creating service provider", e)
            ResponseEntity.internalServerError().body(ServiceProviderResponse.error("Internal server error"))
        }
    }

    /**
     * Updates an existing service provider.
     *
     * @param id Service provider ID
     * @param request Service provider update request
     * @return Updated service provider
     */
    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    fun updateServiceProvider(
        @PathVariable id: Long,
        @Valid @RequestBody request: UpdateServiceProviderRequest
    ): ResponseEntity<ServiceProviderResponse> {
        logger.info("PUT /api/service-providers/$id - Updating service provider with name: ${request.name}")
        return try {
            val serviceProvider = serviceProviderService.updateServiceProvider(
                id = id,
                name = request.name,
                comment = request.comment,
                commentForOcr = request.commentForOcr,
                regular = request.regular,
                customFields = request.customFields
            )
            logger.info("Successfully updated service provider with ID: $id")
            ResponseEntity.ok(ServiceProviderResponse.success(serviceProvider))
        } catch (e: IllegalArgumentException) {
            logger.warn("Failed to update service provider with ID: $id - ${e.message}")
            ResponseEntity.badRequest().body(ServiceProviderResponse.error(e.message ?: "Invalid request"))
        } catch (e: Exception) {
            logger.error("Error updating service provider with ID: $id", e)
            ResponseEntity.internalServerError().body(ServiceProviderResponse.error("Internal server error"))
        }
    }

    /**
     * Changes the state of a service provider.
     *
     * @param id Service provider ID
     * @param request State change request
     * @return Updated service provider
     */
    @PatchMapping("/{id}/state")
    @PreAuthorize("isAuthenticated()")
    fun changeServiceProviderState(
        @PathVariable id: Long,
        @Valid @RequestBody request: ChangeStateRequest
    ): ResponseEntity<ServiceProviderResponse> {
        logger.info("PATCH /api/service-providers/$id/state - Changing state to: ${request.state}")
        return try {
            val serviceProvider = when (request.state) {
                ServiceProviderState.ACTIVE -> serviceProviderService.showServiceProvider(id)
                ServiceProviderState.HIDDEN -> serviceProviderService.hideServiceProvider(id)
            }
            logger.info("Successfully changed state of service provider with ID: $id to ${request.state}")
            ResponseEntity.ok(ServiceProviderResponse.success(serviceProvider))
        } catch (e: IllegalArgumentException) {
            logger.warn("Failed to change state of service provider with ID: $id - ${e.message}")
            ResponseEntity.badRequest().body(ServiceProviderResponse.error(e.message ?: "Invalid request"))
        } catch (e: IllegalStateException) {
            logger.warn("Invalid state transition for service provider with ID: $id - ${e.message}")
            ResponseEntity.badRequest().body(ServiceProviderResponse.error(e.message ?: "Invalid state transition"))
        } catch (e: Exception) {
            logger.error("Error changing state of service provider with ID: $id", e)
            ResponseEntity.internalServerError().body(ServiceProviderResponse.error("Internal server error"))
        }
    }

    /**
     * Soft deletes a service provider by hiding it.
     *
     * @param id Service provider ID
     * @return Hidden service provider
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    fun deleteServiceProvider(@PathVariable id: Long): ResponseEntity<ServiceProviderResponse> {
        logger.info("DELETE /api/service-providers/$id - Soft deleting service provider")
        return try {
            val serviceProvider = serviceProviderService.hideServiceProvider(id)
            logger.info("Successfully soft deleted service provider with ID: $id")
            ResponseEntity.ok(ServiceProviderResponse.success(serviceProvider))
        } catch (e: IllegalArgumentException) {
            logger.warn("Failed to delete service provider with ID: $id - ${e.message}")
            ResponseEntity.badRequest().body(ServiceProviderResponse.error(e.message ?: "Invalid request"))
        } catch (e: IllegalStateException) {
            logger.warn("Invalid state transition when deleting service provider with ID: $id - ${e.message}")
            ResponseEntity.badRequest().body(ServiceProviderResponse.error(e.message ?: "Invalid state transition"))
        } catch (e: Exception) {
            logger.error("Error deleting service provider with ID: $id", e)
            ResponseEntity.internalServerError().body(ServiceProviderResponse.error("Internal server error"))
        }
    }

    /**
     * Uploads and sets avatar for a service provider.
     * Accepts multipart file and automatically resizes to 200x200 pixels.
     *
     * @param id Service provider ID
     * @param file Avatar image file
     * @return Avatar upload response with updated path
     */
    @PostMapping("/{id}/avatar")
    @PreAuthorize("isAuthenticated()")
    fun uploadAvatar(
        @PathVariable id: Long,
        @RequestParam("avatar") file: MultipartFile?
    ): ResponseEntity<AvatarUploadResponse> {
        logger.info("POST /api/service-providers/$id/avatar - Uploading avatar")
        return try {
            if (file == null || file.isEmpty) {
                logger.warn("Avatar upload failed for service provider ID: $id - No file provided")
                return ResponseEntity.badRequest().body(
                    AvatarUploadResponse.error("No file provided")
                )
            }

            // Validate file
            if (!avatarService.validateAvatarFile(file)) {
                logger.warn("Avatar upload failed for service provider ID: $id - Invalid file format or size")
                return ResponseEntity.badRequest().body(
                    AvatarUploadResponse.error("Invalid avatar file format or size")
                )
            }

            // Get old avatar for cleanup
            val existingProvider = serviceProviderService.findById(id)
            if (existingProvider == null) {
                logger.warn("Avatar upload failed - Service provider with ID: $id not found")
                return ResponseEntity.notFound().build()
            }

            // Upload and resize avatar
            val avatarFilename = avatarService.uploadAndResizeAvatar(file)
            logger.debug("Avatar uploaded and resized: $avatarFilename")

            // Update service provider with new avatar path
            val updatedProvider = serviceProviderService.updateAvatar(id, avatarFilename)

            // Clean up old avatar
            avatarService.cleanupOldAvatar(existingProvider.avatar)

            logger.info("Successfully uploaded avatar for service provider with ID: $id")
            ResponseEntity.ok(AvatarUploadResponse.success(updatedProvider, avatarFilename))
        } catch (e: IllegalArgumentException) {
            logger.warn("Avatar upload failed for service provider ID: $id - ${e.message}")
            ResponseEntity.badRequest().body(AvatarUploadResponse.error(e.message ?: "Invalid request"))
        } catch (e: Exception) {
            logger.error("Error uploading avatar for service provider with ID: $id", e)
            ResponseEntity.internalServerError().body(AvatarUploadResponse.error("Upload failed. Please try again."))
        }
    }

    /**
     * Removes avatar from a service provider.
     * Deletes the avatar file and updates the service provider record.
     *
     * @param id Service provider ID
     * @return Avatar removal response
     */
    @DeleteMapping("/{id}/avatar")
    @PreAuthorize("isAuthenticated()")
    fun removeAvatar(@PathVariable id: Long): ResponseEntity<AvatarUploadResponse> {
        logger.info("DELETE /api/service-providers/$id/avatar - Removing avatar")
        return try {
            // Get existing provider
            val existingProvider = serviceProviderService.findById(id)
            if (existingProvider == null) {
                logger.warn("Avatar removal failed - Service provider with ID: $id not found")
                return ResponseEntity.notFound().build()
            }

            if (existingProvider.avatar == null) {
                logger.warn("Avatar removal failed for service provider ID: $id - No avatar to remove")
                return ResponseEntity.badRequest().body(
                    AvatarUploadResponse.error("No avatar to remove")
                )
            }

            // Clean up old avatar file
            avatarService.cleanupOldAvatar(existingProvider.avatar)

            // Update service provider to remove avatar
            val updatedProvider = serviceProviderService.updateAvatar(id, null)

            logger.info("Successfully removed avatar for service provider with ID: $id")
            ResponseEntity.ok(
                AvatarUploadResponse.success(
                    updatedProvider,
                    "",
                    "Avatar removed successfully"
                )
            )
        } catch (e: IllegalArgumentException) {
            logger.warn("Avatar removal failed for service provider ID: $id - ${e.message}")
            ResponseEntity.badRequest().body(AvatarUploadResponse.error(e.message ?: "Invalid request"))
        } catch (e: Exception) {
            logger.error("Error removing avatar for service provider with ID: $id", e)
            ResponseEntity.internalServerError().body(AvatarUploadResponse.error("Avatar removal failed"))
        }
    }

    /**
     * Exception handler for validation errors.
     */
    @ExceptionHandler(IllegalArgumentException::class)
    fun handleValidationException(e: IllegalArgumentException): ResponseEntity<ServiceProviderResponse> {
        return ResponseEntity.badRequest().body(ServiceProviderResponse.error(e.message ?: "Validation error"))
    }

    /**
     * Exception handler for illegal state transitions.
     */
    @ExceptionHandler(IllegalStateException::class)
    fun handleIllegalStateException(e: IllegalStateException): ResponseEntity<ServiceProviderResponse> {
        return ResponseEntity.badRequest().body(ServiceProviderResponse.error(e.message ?: "Invalid state transition"))
    }

    /**
     * Exception handler for validation errors.
     */
    @ExceptionHandler(org.springframework.web.bind.MethodArgumentNotValidException::class)
    fun handleValidationErrors(e: org.springframework.web.bind.MethodArgumentNotValidException): ResponseEntity<ServiceProviderResponse> {
        val errorMessage = e.bindingResult.fieldErrors
            .firstOrNull()?.defaultMessage 
            ?: "Validation error"
        logger.warn("Validation error: $errorMessage")
        return ResponseEntity.badRequest().body(ServiceProviderResponse.error(errorMessage))
    }

    /**
     * Exception handler for general exceptions.
     */
    @ExceptionHandler(Exception::class)
    fun handleGeneralException(e: Exception): ResponseEntity<ServiceProviderResponse> {
        return ResponseEntity.internalServerError().body(ServiceProviderResponse.error("Internal server error"))
    }
}

/**
 * Request DTO for creating a new service provider.
 */
data class CreateServiceProviderRequest(
    @field:jakarta.validation.constraints.NotBlank(message = "Name is required")
    val name: String,
    val comment: String? = null,
    val commentForOcr: String? = null,
    val regular: RegularFrequency = RegularFrequency.NOT_REGULAR,
    val customFields: String? = null
)

/**
 * Request DTO for updating an existing service provider.
 */
data class UpdateServiceProviderRequest(
    @field:jakarta.validation.constraints.NotBlank(message = "Name is required")
    val name: String,
    val comment: String? = null,
    val commentForOcr: String? = null,
    val regular: RegularFrequency = RegularFrequency.NOT_REGULAR,
    val customFields: String? = null
)

/**
 * Request DTO for changing service provider state.
 */
data class ChangeStateRequest(
    val state: ServiceProviderState
)

/**
 * Response DTO for service provider operations.
 */
data class ServiceProviderResponse(
    val success: Boolean,
    val data: ServiceProvider? = null,
    val message: String? = null,
    val error: String? = null
) {
    companion object {
        fun success(serviceProvider: ServiceProvider, message: String? = null): ServiceProviderResponse {
            return ServiceProviderResponse(
                success = true,
                data = serviceProvider,
                message = message
            )
        }

        fun error(errorMessage: String): ServiceProviderResponse {
            return ServiceProviderResponse(
                success = false,
                error = errorMessage
            )
        }
    }
}

/**
 * Response DTO for avatar upload operations.
 */
data class AvatarUploadResponse(
    val success: Boolean,
    val data: ServiceProvider? = null,
    val avatarPath: String? = null,
    val message: String? = null,
    val error: String? = null
) {
    companion object {
        fun success(
            serviceProvider: ServiceProvider,
            avatarPath: String,
            message: String? = "Avatar uploaded successfully"
        ): AvatarUploadResponse {
            return AvatarUploadResponse(
                success = true,
                data = serviceProvider,
                avatarPath = avatarPath,
                message = message
            )
        }

        fun error(errorMessage: String): AvatarUploadResponse {
            return AvatarUploadResponse(
                success = false,
                error = errorMessage
            )
        }
    }
}