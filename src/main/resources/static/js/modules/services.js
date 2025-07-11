/**
 * Services functionality module for the dashboard
 * Handles service providers data loading, form management, and event listeners
 */

class ServicesModule {
    constructor() {
        // Services tab state
        this.serviceProviders = [];
        this.selectedServiceProvider = null;
        this.isEditingServiceProvider = false;
    }

    /**
     * Initialize the services module
     * Sets up event listeners and loads initial data when tab is shown
     */
    init() {
        this.setupTabEventListener();
    }

    /**
     * Set up tab change event listener for services tab
     */
    setupTabEventListener() {
        const servicesTab = document.querySelector('a[href="#services"]');
        if (servicesTab) {
            servicesTab.addEventListener('shown.bs.tab', () => {
                this.loadServicesData();
            });
        }
    }

    /**
     * Load services data from the API
     */
    loadServicesData() {
        fetch('/api/service-providers')
            .then(response => {
                if (!response.ok) {
                    throw new Error('Failed to load service providers');
                }
                return response.json();
            })
            .then(data => {
                this.serviceProviders = data;
                this.renderServiceProviderList();
            })
            .catch(error => {
                console.error('Error loading service providers:', error);
                document.getElementById('serviceProviderList').innerHTML = `
                    <div class="alert alert-danger m-3" role="alert">
                        <i class="fas fa-exclamation-triangle me-2"></i>
                        Failed to load service providers. Please try again.
                        <button class="btn btn-sm btn-outline-danger ms-2" onclick="loadServicesData()">
                            <i class="fas fa-redo me-1"></i> Retry
                        </button>
                    </div>
                `;
            });
    }

    /**
     * Render service provider list
     */
    renderServiceProviderList() {
        const listContainer = document.getElementById('serviceProviderList');
        
        if (this.serviceProviders.length === 0) {
            listContainer.innerHTML = `
                <div class="empty-state">
                    <i class="fas fa-building empty-state-icon"></i>
                    <div class="empty-state-title">No Service Providers</div>
                    <p class="empty-state-text">Create your first service provider to get started.</p>
                </div>
            `;
            return;
        }

        const listHtml = `
            <ul class="service-provider-list">
                ${this.serviceProviders.map(provider => `
                    <li class="service-provider-item ${provider.state === 'HIDDEN' ? 'hidden' : ''} ${this.selectedServiceProvider && this.selectedServiceProvider.id === provider.id ? 'selected' : ''}" 
                        onclick="selectServiceProvider(${provider.id})">
                        ${provider.avatar ? 
                            `<img src="/attachments/avatars/${provider.avatar}" alt="${provider.name}" class="service-provider-avatar">` :
                            `<div class="service-provider-avatar-fallback">${provider.name.substring(0, 1).toUpperCase()}</div>`
                        }
                        <div class="service-provider-info">
                            <h4 class="service-provider-name">${provider.name}</h4>
                            <p class="service-provider-state">${provider.state === 'ACTIVE' ? 'Active' : 'Hidden'}</p>
                        </div>
                    </li>
                `).join('')}
            </ul>
        `;
        
        listContainer.innerHTML = listHtml;
    }

    /**
     * Select a service provider
     * @param {number} providerId - The provider ID to select
     */
    selectServiceProvider(providerId) {
        this.selectedServiceProvider = this.serviceProviders.find(p => p.id === providerId);
        if (this.selectedServiceProvider) {
            this.renderServiceProviderList(); // Re-render to show selection
            this.renderServiceProviderForm();
        }
    }

    /**
     * Render service provider form
     */
    renderServiceProviderForm() {
        const formContainer = document.getElementById('serviceProviderForm');
        if (!this.selectedServiceProvider) {
            formContainer.innerHTML = `
                <div class="empty-state">
                    <i class="fas fa-building empty-state-icon"></i>
                    <div class="empty-state-title">No Service Provider Selected</div>
                    <p class="empty-state-text">Select a service provider from the list to view and edit details, or create a new one.</p>
                </div>
            `;
            return;
        }

        const isNewProvider = this.selectedServiceProvider.id === null;

        const formHtml = `
            <form class="service-provider-form" onsubmit="saveServiceProvider(event)" style="gap: 1rem;">
                <!-- Avatar and Name Section -->
                <div class="form-group">
                    <div style="display: flex; align-items: flex-start; gap: 20px;">
                        <div>
                            <label class="form-label">Avatar</label>
                            <div class="avatar-upload-section">
                                ${this.selectedServiceProvider.avatar ? 
                                    `<img src="/attachments/avatars/${this.selectedServiceProvider.avatar}" alt="Avatar" class="avatar-preview" id="avatarPreview" onclick="uploadAvatar()" style="cursor: pointer;">` :
                                    `<div class="avatar-preview-fallback" id="avatarPreview" onclick="uploadAvatar()" style="cursor: pointer;">${this.selectedServiceProvider.name ? this.selectedServiceProvider.name.substring(0, 1).toUpperCase() : 'SP'}</div>`
                                }
                                <div class="avatar-upload-controls">
                                    <!-- Upload and remove buttons removed -->
                                </div>
                            </div>
                        </div>
                        <div style="flex: 1;">
                            <div style="display: flex; align-items: center; justify-content: space-between; margin-bottom: 8px;">
                                <label for="providerName" class="form-label" style="margin-bottom: 0;">Name *</label>
                                <div class="toggle-switch">
                                    <input type="checkbox" id="providerState" class="toggle-input" ${this.selectedServiceProvider.state === 'ACTIVE' ? 'checked' : ''}>
                                    <label for="providerState" class="toggle-label">Active</label>
                                </div>
                            </div>
                            <input type="text" id="providerName" class="form-control" value="${this.selectedServiceProvider.name || ''}" required>
                            <div class="invalid-feedback" id="nameError"></div>
                        </div>
                    </div>
                </div>

                <!-- Comment Field -->
                <div class="form-group">
                    <label for="providerComment" class="form-label">Comment</label>
                    <textarea id="providerComment" class="form-control form-textarea" placeholder="Optional comment about this service provider">${this.selectedServiceProvider.comment || ''}</textarea>
                </div>

                <!-- OCR Comment Field -->
                <div class="form-group">
                    <label for="providerOcrComment" class="form-label">OCR Comment</label>
                    <textarea id="providerOcrComment" class="form-control form-textarea" placeholder="Comment to help OCR recognition">${this.selectedServiceProvider.commentForOcr || ''}</textarea>
                </div>

                <!-- Regular Frequency Field -->
                <div class="form-group">
                    <label for="providerFrequency" class="form-label">Regular Frequency</label>
                    <select id="providerFrequency" class="form-control form-select">
                        <option value="NOT_REGULAR" ${this.selectedServiceProvider.regular === 'NOT_REGULAR' ? 'selected' : ''}>Not Regular</option>
                        <option value="YEARLY" ${this.selectedServiceProvider.regular === 'YEARLY' ? 'selected' : ''}>Yearly</option>
                        <option value="MONTHLY" ${this.selectedServiceProvider.regular === 'MONTHLY' ? 'selected' : ''}>Monthly</option>
                        <option value="WEEKLY" ${this.selectedServiceProvider.regular === 'WEEKLY' ? 'selected' : ''}>Weekly</option>
                    </select>
                </div>


                <!-- Custom Fields Section -->
                <div class="form-group">
                    <label class="form-label">Custom Fields</label>
                    <div id="customFieldsContainer">
                        ${this.renderCustomFields(this.selectedServiceProvider.customFields)}
                    </div>
                    <button type="button" class="btn-upload" onclick="addCustomField()">
                        <i class="fas fa-plus me-1"></i> Add Custom Field
                    </button>
                </div>

                <!-- Form Actions -->
                <div class="form-actions">
                    <button type="submit" class="btn-save" id="saveButton">
                        <i class="fas fa-save me-1"></i> Save
                    </button>
                    <button type="button" class="btn-cancel" onclick="cancelEdit()">
                        <i class="fas fa-times me-1"></i> Cancel
                    </button>
                    ${!isNewProvider ? 
                        `<button type="button" class="btn-cancel" onclick="deleteServiceProvider()" style="margin-left: auto;">
                            <i class="fas fa-trash me-1"></i> Delete
                        </button>` : ''
                    }
                </div>
            </form>
        `;

        formContainer.innerHTML = formHtml;
    }

    /**
     * Render custom fields
     * @param {Object} customFields - The custom fields object
     * @returns {string} HTML string for custom fields
     */
    renderCustomFields(customFields) {
        if (!customFields || Object.keys(customFields).length === 0) {
            return '<p class="text-muted">No custom fields defined.</p>';
        }

        return Object.entries(customFields).map(([key, value], index) => `
            <div class="custom-field-item mb-2">
                <div class="row">
                    <div class="col-md-5">
                        <input type="text" class="form-control" placeholder="Field name" value="${key}" onchange="updateCustomFieldKey(${index}, this.value)">
                    </div>
                    <div class="col-md-6">
                        <input type="text" class="form-control" placeholder="Field value" value="${value}" onchange="updateCustomFieldValue('${key}', this.value)">
                    </div>
                    <div class="col-md-1">
                        <button type="button" class="btn-upload" onclick="removeCustomField('${key}')">
                            <i class="fas fa-trash"></i>
                        </button>
                    </div>
                </div>
            </div>
        `).join('');
    }

    /**
     * Create new service provider
     */
    createNewServiceProvider() {
        this.selectedServiceProvider = {
            id: null,
            name: '',
            avatar: null,
            comment: '',
            commentForOcr: '',
            regular: 'NOT_REGULAR',
            customFields: {},
            state: 'ACTIVE'
        };
        this.renderServiceProviderForm();
    }

    /**
     * Save service provider
     * @param {Event} event - The form submit event
     */
    saveServiceProvider(event) {
        event.preventDefault();
        
        const formData = {
            name: document.getElementById('providerName').value,
            comment: document.getElementById('providerComment').value,
            commentForOcr: document.getElementById('providerOcrComment').value,
            regular: document.getElementById('providerFrequency').value,
            state: document.getElementById('providerState').checked ? 'ACTIVE' : 'HIDDEN',
            customFields: this.selectedServiceProvider.customFields
        };

        // Validate
        if (!formData.name.trim()) {
            document.getElementById('nameError').textContent = 'Name is required';
            document.getElementById('providerName').classList.add('is-invalid');
            return;
        }

        const isNewProvider = this.selectedServiceProvider.id === null;
        const url = isNewProvider ? '/api/service-providers' : `/api/service-providers/${this.selectedServiceProvider.id}`;
        const method = isNewProvider ? 'POST' : 'PUT';

        document.getElementById('saveButton').disabled = true;

        fetch(url, {
            method: method,
            headers: {
                'Content-Type': 'application/json',
                'X-CSRF-TOKEN': document.querySelector('meta[name="_csrf"]').getAttribute('content')
            },
            body: JSON.stringify(formData)
        })
        .then(response => {
            if (!response.ok) {
                throw new Error('Failed to save service provider');
            }
            return response.json();
        })
        .then(data => {
            this.selectedServiceProvider = data;
            this.loadServicesData(); // Reload the list
            this.showSuccessMessage(isNewProvider ? 'Service provider created successfully' : 'Service provider updated successfully');
        })
        .catch(error => {
            console.error('Error saving service provider:', error);
            this.showErrorMessage('Failed to save service provider. Please try again.');
        })
        .finally(() => {
            document.getElementById('saveButton').disabled = false;
        });
    }

    /**
     * Cancel edit
     */
    cancelEdit() {
        if (this.selectedServiceProvider.id === null) {
            // If it's a new provider, clear selection
            this.selectedServiceProvider = null;
            this.renderServiceProviderForm();
        } else {
            // If editing existing, reload from server
            this.selectServiceProvider(this.selectedServiceProvider.id);
        }
    }

    /**
     * Delete service provider
     */
    deleteServiceProvider() {
        if (!this.selectedServiceProvider || this.selectedServiceProvider.id === null) return;

        if (confirm('Are you sure you want to delete this service provider? This action cannot be undone.')) {
            fetch(`/api/service-providers/${this.selectedServiceProvider.id}`, {
                method: 'DELETE',
                headers: {
                    'X-CSRF-TOKEN': document.querySelector('meta[name="_csrf"]').getAttribute('content')
                }
            })
            .then(response => {
                if (!response.ok) {
                    throw new Error('Failed to delete service provider');
                }
                this.selectedServiceProvider = null;
                this.loadServicesData(); // Reload the list
                this.renderServiceProviderForm();
                this.showSuccessMessage('Service provider deleted successfully');
            })
            .catch(error => {
                console.error('Error deleting service provider:', error);
                this.showErrorMessage('Failed to delete service provider. Please try again.');
            });
        }
    }

    /**
     * Upload avatar
     */
    uploadAvatar() {
        if (!this.selectedServiceProvider) return;

        // Use the proper avatar upload modal function
        if (typeof window.openAvatarUploadModal === 'function') {
            window.openAvatarUploadModal(this.selectedServiceProvider.id, (response) => {
                // Update the service provider avatar after successful upload
                if (response.success && response.data) {
                    this.selectedServiceProvider.avatar = response.avatarPath;
                    this.renderServiceProviderForm();
                    this.renderServiceProviderList();
                }
            });
        } else {
            console.error('openAvatarUploadModal function not available');
        }
    }

    /**
     * Remove avatar
     */
    removeAvatar() {
        if (!this.selectedServiceProvider || !this.selectedServiceProvider.avatar) return;

        if (confirm('Are you sure you want to remove the avatar?')) {
            this.selectedServiceProvider.avatar = null;
            document.getElementById('avatarPreview').outerHTML = 
                `<div class="avatar-preview-fallback" id="avatarPreview" onclick="uploadAvatar()" style="cursor: pointer;">${this.selectedServiceProvider.name ? this.selectedServiceProvider.name.substring(0, 1).toUpperCase() : 'SP'}</div>`;
        }
    }

    /**
     * Add custom field
     */
    addCustomField() {
        if (!this.selectedServiceProvider.customFields) {
            this.selectedServiceProvider.customFields = {};
        }
        
        const fieldName = prompt('Enter field name:');
        if (fieldName && fieldName.trim()) {
            this.selectedServiceProvider.customFields[fieldName.trim()] = '';
            this.renderServiceProviderForm();
        }
    }

    /**
     * Update custom field key
     * @param {number} index - The field index
     * @param {string} newKey - The new key value
     */
    updateCustomFieldKey(index, newKey) {
        if (!this.selectedServiceProvider.customFields) return;
        
        const entries = Object.entries(this.selectedServiceProvider.customFields);
        if (entries[index]) {
            const [oldKey, value] = entries[index];
            delete this.selectedServiceProvider.customFields[oldKey];
            this.selectedServiceProvider.customFields[newKey] = value;
        }
    }

    /**
     * Update custom field value
     * @param {string} key - The field key
     * @param {string} newValue - The new value
     */
    updateCustomFieldValue(key, newValue) {
        if (!this.selectedServiceProvider.customFields) return;
        this.selectedServiceProvider.customFields[key] = newValue;
    }

    /**
     * Remove custom field
     * @param {string} key - The field key to remove
     */
    removeCustomField(key) {
        if (!this.selectedServiceProvider.customFields) return;
        delete this.selectedServiceProvider.customFields[key];
        this.renderServiceProviderForm();
    }

    /**
     * Show success message
     * @param {string} message - The success message to display
     */
    showSuccessMessage(message) {
        const alertHtml = `
            <div class="alert alert-success alert-dismissible fade show" role="alert">
                <i class="fas fa-check-circle me-2"></i>
                ${message}
                <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
            </div>
        `;
        this.showAlert(alertHtml);
    }

    /**
     * Show error message
     * @param {string} message - The error message to display
     */
    showErrorMessage(message) {
        const alertHtml = `
            <div class="alert alert-danger alert-dismissible fade show" role="alert">
                <i class="fas fa-exclamation-triangle me-2"></i>
                ${message}
                <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
            </div>
        `;
        this.showAlert(alertHtml);
    }

    /**
     * Show alert
     * @param {string} alertHtml - The alert HTML to display
     */
    showAlert(alertHtml) {
        const alertContainer = document.createElement('div');
        alertContainer.innerHTML = alertHtml;
        
        const dashboardLayout = document.querySelector('.dashboard-layout');
        if (dashboardLayout) {
            dashboardLayout.insertBefore(alertContainer.firstElementChild, dashboardLayout.firstElementChild);
        }
        
        // Auto-dismiss after 5 seconds
        setTimeout(() => {
            const alert = document.querySelector('.alert');
            if (alert) {
                alert.remove();
            }
        }, 5000);
    }
}

// Create global services module instance
const servicesModule = new ServicesModule();

// Make functions globally available for backwards compatibility
window.loadServicesData = () => servicesModule.loadServicesData();
window.createNewServiceProvider = () => servicesModule.createNewServiceProvider();
window.selectServiceProvider = (providerId) => servicesModule.selectServiceProvider(providerId);
window.saveServiceProvider = (event) => servicesModule.saveServiceProvider(event);
window.cancelEdit = () => servicesModule.cancelEdit();
window.deleteServiceProvider = () => servicesModule.deleteServiceProvider();
window.uploadAvatar = () => servicesModule.uploadAvatar();
window.removeAvatar = () => servicesModule.removeAvatar();
window.addCustomField = () => servicesModule.addCustomField();
window.updateCustomFieldKey = (index, newKey) => servicesModule.updateCustomFieldKey(index, newKey);
window.updateCustomFieldValue = (key, newValue) => servicesModule.updateCustomFieldValue(key, newValue);
window.removeCustomField = (key) => servicesModule.removeCustomField(key);

// Note: openAvatarUploadModal is now defined in avatar-upload.js

// Global function to update service provider avatar after upload
window.updateServiceProviderAvatar = (serviceProviderId, avatarPath) => {
    // Update the service provider in the services module
    if (servicesModule.serviceProviders) {
        const provider = servicesModule.serviceProviders.find(p => p.id == serviceProviderId);
        if (provider) {
            provider.avatar = avatarPath;
            servicesModule.renderServiceProviderList();
            if (servicesModule.selectedServiceProvider && servicesModule.selectedServiceProvider.id == serviceProviderId) {
                servicesModule.selectedServiceProvider.avatar = avatarPath;
                servicesModule.renderServiceProviderForm();
            }
        }
    }
};