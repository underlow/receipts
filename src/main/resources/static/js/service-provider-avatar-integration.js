/**
 * Service Provider Avatar Integration Module
 *
 * Integrates the avatar upload functionality with the existing service provider form.
 * Provides callback functions and UI updates for avatar operations.
 */

/**
 * Update service provider avatar after successful upload.
 * Called as callback from avatar upload modal.
 * 
 * @param {Object} response - Upload response from server
 */
function updateServiceProviderAvatar(response) {
    if (!response.success || !response.data) {
        console.error('Invalid avatar upload response:', response);
        return;
    }

    const updatedProvider = response.data;
    const avatarPath = response.avatarPath;

    // Update the selectedServiceProvider object
    if (selectedServiceProvider && selectedServiceProvider.id === updatedProvider.id) {
        selectedServiceProvider.avatar = avatarPath;
    }

    // Update the provider in the serviceProviders array
    const providerIndex = serviceProviders.findIndex(p => p.id === updatedProvider.id);
    if (providerIndex !== -1) {
        serviceProviders[providerIndex] = updatedProvider;
    }

    // Update the form avatar preview
    updateFormAvatarPreview(avatarPath);

    // Update the list item avatar
    updateListItemAvatar(updatedProvider.id, avatarPath);

    // Show success message
    showServiceProviderMessage('Avatar updated successfully!', 'success');
}

/**
 * Update form avatar preview after upload or removal.
 * 
 * @param {string|null} avatarPath - Path to the new avatar or null to remove
 */
function updateFormAvatarPreview(avatarPath) {
    const avatarPreview = document.getElementById('avatarPreview');
    const avatarControls = document.querySelector('.avatar-upload-controls');
    
    if (!avatarPreview) {
        return;
    }

    if (avatarPath) {
        // Update to show new avatar
        avatarPreview.innerHTML = `<img src="/attachments/${avatarPath}" alt="Avatar" class="avatar-image avatar-image-large">`;
        avatarPreview.className = 'avatar-preview avatar-preview-large';
        
        // Update controls to show remove option
        if (avatarControls) {
            const isNewProvider = selectedServiceProvider.id === null;
            avatarControls.innerHTML = `
                <button type="button" class="btn btn-outline-primary btn-sm me-2" 
                        onclick="${isNewProvider ? 'alert(\"Please save the service provider first before uploading an avatar.\");' : `openAvatarUploadModal(${selectedServiceProvider.id}, updateServiceProviderAvatar)`}"
                        ${isNewProvider ? 'disabled title="Save the service provider first"' : ''}>
                    <i class="fas fa-camera me-1"></i>Change Avatar
                </button>
                <button type="button" class="btn btn-outline-danger btn-sm" 
                        onclick="${isNewProvider ? 'alert(\"Please save the service provider first before removing an avatar.\");' : `removeServiceProviderAvatar(${selectedServiceProvider.id})`}"
                        ${isNewProvider ? 'disabled title="Save the service provider first"' : ''}>
                    <i class="fas fa-trash me-1"></i>Remove
                </button>
            `;
        }
    } else {
        // Remove avatar and show placeholder
        const initials = selectedServiceProvider.name ? selectedServiceProvider.name.substring(0, 1).toUpperCase() : 'SP';
        avatarPreview.innerHTML = `<div class="avatar-placeholder avatar-placeholder-large">
            <i class="fas fa-building avatar-icon avatar-icon-large"></i>
        </div>`;
        avatarPreview.className = 'avatar-preview avatar-preview-large';
        
        // Update controls to show only upload option
        if (avatarControls) {
            const isNewProvider = selectedServiceProvider.id === null;
            avatarControls.innerHTML = `
                <button type="button" class="btn btn-outline-primary btn-sm" 
                        onclick="${isNewProvider ? 'alert(\"Please save the service provider first before uploading an avatar.\");' : `openAvatarUploadModal(${selectedServiceProvider.id}, updateServiceProviderAvatar)`}"
                        ${isNewProvider ? 'disabled title="Save the service provider first"' : ''}>
                    <i class="fas fa-camera me-1"></i>Upload Avatar
                </button>
            `;
        }
    }
}

/**
 * Update avatar in the service provider list item.
 * 
 * @param {number} serviceProviderId - ID of the service provider
 * @param {string|null} avatarPath - Path to the new avatar or null to remove
 */
function updateListItemAvatar(serviceProviderId, avatarPath) {
    const listItem = document.querySelector(`[data-service-provider-id="${serviceProviderId}"]`);
    if (!listItem) {
        return;
    }

    const avatarElement = listItem.querySelector('.service-provider-avatar');
    if (!avatarElement) {
        return;
    }

    if (avatarPath) {
        avatarElement.innerHTML = `<img src="/attachments/${avatarPath}" alt="Avatar" class="avatar-image avatar-image-medium">`;
        avatarElement.className = 'service-provider-avatar avatar-preview avatar-preview-medium';
    } else {
        const provider = serviceProviders.find(p => p.id === serviceProviderId);
        const initials = provider && provider.name ? provider.name.substring(0, 1).toUpperCase() : 'SP';
        avatarElement.innerHTML = `<div class="avatar-placeholder avatar-placeholder-medium">
            <i class="fas fa-building avatar-icon avatar-icon-medium"></i>
        </div>`;
        avatarElement.className = 'service-provider-avatar avatar-preview avatar-preview-medium';
    }
}

/**
 * Remove service provider avatar.
 * 
 * @param {number} serviceProviderId - ID of the service provider
 */
function removeServiceProviderAvatar(serviceProviderId) {
    if (!serviceProviderId) {
        console.error('Service provider ID is required');
        return;
    }

    // Show confirmation dialog
    const provider = serviceProviders.find(p => p.id === serviceProviderId);
    const providerName = provider ? provider.name : 'this service provider';
    
    if (!confirm(`Are you sure you want to remove the avatar for ${providerName}?`)) {
        return;
    }

    // Make API call to remove avatar
    fetch(`/api/service-providers/${serviceProviderId}/avatar`, {
        method: 'DELETE',
        headers: {
            'Content-Type': 'application/json',
            [getCsrfParameterName()]: getCsrfToken()
        }
    })
    .then(response => {
        if (!response.ok) {
            throw new Error(`Failed to remove avatar: ${response.status}`);
        }
        return response.json();
    })
    .then(response => {
        if (response.success) {
            // Update local data
            if (selectedServiceProvider && selectedServiceProvider.id === serviceProviderId) {
                selectedServiceProvider.avatar = null;
            }
            
            const providerIndex = serviceProviders.findIndex(p => p.id === serviceProviderId);
            if (providerIndex !== -1) {
                serviceProviders[providerIndex].avatar = null;
            }
            
            // Update UI
            updateFormAvatarPreview(null);
            updateListItemAvatar(serviceProviderId, null);
            
            // Show success message
            showServiceProviderMessage('Avatar removed successfully!', 'success');
        } else {
            throw new Error(response.error || 'Failed to remove avatar');
        }
    })
    .catch(error => {
        console.error('Error removing avatar:', error);
        showServiceProviderMessage('Failed to remove avatar. Please try again.', 'error');
    });
}

/**
 * Update form avatar for form submission context.
 * 
 * @param {Object} response - Upload response from server
 */
function updateFormAvatar(response) {
    if (!response.success || !response.data) {
        console.error('Invalid avatar upload response:', response);
        return;
    }

    const avatarPath = response.avatarPath;
    
    // Update hidden form field if it exists
    const avatarInput = document.getElementById('serviceProviderAvatar');
    if (avatarInput) {
        avatarInput.value = avatarPath;
    }

    // Update form preview
    updateFormAvatarPreview(avatarPath);

    // Show success message
    showServiceProviderMessage('Avatar uploaded successfully!', 'success');
}

/**
 * Remove avatar from form context.
 * 
 * @param {number} serviceProviderId - ID of the service provider
 */
function removeFormAvatar(serviceProviderId) {
    // Update hidden form field
    const avatarInput = document.getElementById('serviceProviderAvatar');
    if (avatarInput) {
        avatarInput.value = '';
    }

    // Update form preview
    updateFormAvatarPreview(null);

    // Show success message
    showServiceProviderMessage('Avatar removed!', 'success');
}

/**
 * Enhanced service provider form rendering with avatar support.
 * Replaces the existing renderServiceProviderForm function.
 */
function renderServiceProviderFormWithAvatar() {
    const formContainer = document.getElementById('serviceProviderForm');
    const formTitle = document.getElementById('formTitle');
    
    if (!selectedServiceProvider) {
        formTitle.textContent = 'Select a Service Provider';
        formContainer.innerHTML = `
            <div class="empty-state">
                <i class="fas fa-building empty-state-icon"></i>
                <div class="empty-state-title">No Service Provider Selected</div>
                <p class="empty-state-text">Select a service provider from the list to view and edit details, or create a new one.</p>
            </div>
        `;
        return;
    }

    const isNewProvider = selectedServiceProvider.id === null;
    formTitle.textContent = isNewProvider ? 'Create New Service Provider' : 'Edit Service Provider';

    const avatarSection = selectedServiceProvider.avatar ? `
        <div class="avatar-preview avatar-preview-large">
            <img src="/attachments/${selectedServiceProvider.avatar}" alt="Avatar" class="avatar-image avatar-image-large">
        </div>
    ` : `
        <div class="avatar-preview avatar-preview-large">
            <div class="avatar-placeholder avatar-placeholder-large">
                <i class="fas fa-building avatar-icon avatar-icon-large"></i>
            </div>
        </div>
    `;

    const avatarControls = selectedServiceProvider.avatar ? `
        <button type="button" class="btn btn-outline-primary btn-sm me-2" 
                onclick="${isNewProvider ? 'alert(\"Please save the service provider first before uploading an avatar.\");' : `openAvatarUploadModal(${selectedServiceProvider.id}, updateServiceProviderAvatar)`}"
                ${isNewProvider ? 'disabled title="Save the service provider first"' : ''}>
            <i class="fas fa-camera me-1"></i>Change Avatar
        </button>
        <button type="button" class="btn btn-outline-danger btn-sm" 
                onclick="${isNewProvider ? 'alert(\"Please save the service provider first before removing an avatar.\");' : `removeServiceProviderAvatar(${selectedServiceProvider.id})`}"
                ${isNewProvider ? 'disabled title="Save the service provider first"' : ''}>
            <i class="fas fa-trash me-1"></i>Remove
        </button>
    ` : `
        <button type="button" class="btn btn-outline-primary btn-sm" 
                onclick="${isNewProvider ? 'alert(\"Please save the service provider first before uploading an avatar.\");' : `openAvatarUploadModal(${selectedServiceProvider.id}, updateServiceProviderAvatar)`}"
                ${isNewProvider ? 'disabled title="Save the service provider first"' : ''}>
            <i class="fas fa-camera me-1"></i>Upload Avatar
        </button>
    `;

    const formHtml = `
        <form class="service-provider-form" onsubmit="saveServiceProvider(event)">
            <!-- Avatar Upload Section -->
            <div class="form-group mb-4">
                <label class="form-label">Service Provider Avatar</label>
                <div class="d-flex align-items-start">
                    <div id="avatarPreview" class="me-3">
                        ${avatarSection}
                    </div>
                    <div class="flex-grow-1">
                        <div class="avatar-upload-controls mb-2">
                            ${avatarControls}
                        </div>
                        <div class="form-text">
                            Recommended size: 200x200 pixels. Supported formats: JPEG, PNG, GIF, WebP.
                            ${isNewProvider ? '<br><small class="text-muted">Save the service provider first to enable avatar upload.</small>' : ''}
                        </div>
                    </div>
                </div>
            </div>

            <!-- Name Field -->
            <div class="form-group mb-3">
                <label for="providerName" class="form-label">Name *</label>
                <input type="text" id="providerName" class="form-control" value="${selectedServiceProvider.name || ''}" required>
                <div class="invalid-feedback" id="nameError"></div>
            </div>

            <!-- Comment Field -->
            <div class="form-group mb-3">
                <label for="providerComment" class="form-label">Comment</label>
                <textarea id="providerComment" class="form-control" rows="3" placeholder="Add notes about this service provider...">${selectedServiceProvider.comment || ''}</textarea>
            </div>

            <!-- OCR Comment Field -->
            <div class="form-group mb-3">
                <label for="providerCommentForOcr" class="form-label">OCR Comment</label>
                <textarea id="providerCommentForOcr" class="form-control" rows="2" placeholder="Additional information for OCR processing...">${selectedServiceProvider.commentForOcr || ''}</textarea>
                <div class="form-text">This information helps with automatic text recognition.</div>
            </div>

            <!-- Regular Frequency Field -->
            <div class="form-group mb-3">
                <label for="providerRegular" class="form-label">Billing Frequency</label>
                <select id="providerRegular" class="form-select">
                    <option value="NOT_REGULAR" ${selectedServiceProvider.regular === 'NOT_REGULAR' ? 'selected' : ''}>Not Regular</option>
                    <option value="WEEKLY" ${selectedServiceProvider.regular === 'WEEKLY' ? 'selected' : ''}>Weekly</option>
                    <option value="MONTHLY" ${selectedServiceProvider.regular === 'MONTHLY' ? 'selected' : ''}>Monthly</option>
                    <option value="YEARLY" ${selectedServiceProvider.regular === 'YEARLY' ? 'selected' : ''}>Yearly</option>
                </select>
            </div>

            <!-- Custom Fields -->
            <div class="form-group mb-3">
                <label for="providerCustomFields" class="form-label">Custom Fields</label>
                <textarea id="providerCustomFields" class="form-control" rows="3" placeholder="Custom field data (JSON format)...">${selectedServiceProvider.customFields || ''}</textarea>
                <div class="form-text">Store additional custom data in JSON format.</div>
            </div>

            <!-- State Field -->
            <div class="form-group mb-4">
                <label for="providerState" class="form-label">State</label>
                <select id="providerState" class="form-select">
                    <option value="ACTIVE" ${selectedServiceProvider.state === 'ACTIVE' ? 'selected' : ''}>Active</option>
                    <option value="HIDDEN" ${selectedServiceProvider.state === 'HIDDEN' ? 'selected' : ''}>Hidden</option>
                </select>
                <div class="form-text">Hidden providers are not shown in active lists but preserve historical data.</div>
            </div>

            <!-- Form Actions -->
            <div class="form-actions">
                <button type="submit" class="btn btn-primary">
                    <i class="fas fa-save me-1"></i>
                    ${isNewProvider ? 'Create Service Provider' : 'Update Service Provider'}
                </button>
                ${!isNewProvider ? `
                    <button type="button" class="btn btn-outline-danger ms-2" onclick="deleteServiceProvider(${selectedServiceProvider.id})">
                        <i class="fas fa-trash me-1"></i>Delete
                    </button>
                ` : ''}
                <button type="button" class="btn btn-secondary ms-2" onclick="cancelServiceProviderEdit()">
                    <i class="fas fa-times me-1"></i>Cancel
                </button>
            </div>
        </form>
    `;

    formContainer.innerHTML = formHtml;
}

/**
 * Show service provider specific messages.
 * 
 * @param {string} message - The message to show
 * @param {string} type - Message type: 'success', 'error', 'warning', 'info'
 */
function showServiceProviderMessage(message, type = 'info') {
    const alertClass = type === 'error' ? 'alert-danger' : `alert-${type}`;
    const iconClass = type === 'success' ? 'fa-check-circle' : 
                      type === 'error' ? 'fa-exclamation-triangle' : 
                      type === 'warning' ? 'fa-exclamation-triangle' : 'fa-info-circle';

    const alertHtml = `
        <div class="alert ${alertClass} alert-dismissible fade show" role="alert">
            <i class="fas ${iconClass} me-2"></i>
            ${message}
            <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
        </div>
    `;

    // Find the services content area to show the message
    const servicesContent = document.getElementById('services-content');
    if (servicesContent) {
        const alertContainer = document.createElement('div');
        alertContainer.innerHTML = alertHtml;
        servicesContent.insertBefore(alertContainer.firstElementChild, servicesContent.firstElementChild);

        // Auto-dismiss after 5 seconds
        setTimeout(() => {
            const alert = servicesContent.querySelector('.alert');
            if (alert) {
                alert.remove();
            }
        }, 5000);
    }
}

/**
 * Get CSRF token from meta tag.
 */
function getCsrfToken() {
    const csrfToken = document.querySelector('meta[name="_csrf"]');
    return csrfToken ? csrfToken.getAttribute('content') : '';
}

/**
 * Get CSRF parameter name from meta tag.
 */
function getCsrfParameterName() {
    const csrfParamName = document.querySelector('meta[name="_csrf_parameter_name"]');
    return csrfParamName ? csrfParamName.getAttribute('content') : '_csrf';
}

// Override the existing renderServiceProviderForm function
if (typeof window !== 'undefined') {
    window.renderServiceProviderForm = renderServiceProviderFormWithAvatar;
    window.updateServiceProviderAvatar = updateServiceProviderAvatar;
    window.removeServiceProviderAvatar = removeServiceProviderAvatar;
    window.updateFormAvatar = updateFormAvatar;
    window.removeFormAvatar = removeFormAvatar;
}