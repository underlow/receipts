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
        
        // Filter out HIDDEN providers - they should not appear in the list at all
        const activeProviders = this.serviceProviders.filter(provider => provider.state === 'ACTIVE');
        
        if (activeProviders.length === 0) {
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
                ${activeProviders.map(provider => `
                    <li class="service-provider-item ${this.selectedServiceProvider && this.selectedServiceProvider.id === provider.id ? 'selected' : ''}" 
                        onclick="selectServiceProvider(${provider.id})">
                        ${provider.avatar ? 
                            `<img src="/attachments/avatars/${provider.avatar}" alt="${provider.name}" class="service-provider-avatar">` :
                            `<div class="service-provider-avatar-fallback">${provider.name.substring(0, 1).toUpperCase()}</div>`
                        }
                        <div class="service-provider-info">
                            <h4 class="service-provider-name">${provider.name}</h4>
                            <p class="service-provider-state">Active</p>
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
        const isNameEmpty = !this.selectedServiceProvider.name || this.selectedServiceProvider.name.trim() === '';

        const formHtml = `
            <form class="service-provider-form" data-test-id="service-provider-form" onsubmit="saveServiceProvider(event)" style="gap: 1rem;">
                <h2 class="form-title" data-test-id="form-title">${isNewProvider ? 'Create Service Provider' : 'Edit Service Provider'}</h2>
                <!-- Avatar and Name Section -->
                <div class="form-group">
                    <div style="display: flex; gap: 20px;">
                        <div style="display: flex; flex-direction: column;">
                            <label class="form-label">Avatar</label>
                            <div class="avatar-upload-section">
                                ${this.selectedServiceProvider.avatar ? 
                                    `<img src="/attachments/avatars/${this.selectedServiceProvider.avatar}" alt="Avatar" class="avatar-preview" data-test-id="avatar-preview" id="avatarPreview">` :
                                    `<div data-test-id="avatar-preview" id="avatarPreview"><div class="avatar-preview-fallback avatar-fallback">${this.selectedServiceProvider.name ? this.selectedServiceProvider.name.substring(0, 1).toUpperCase() : 'SP'}</div></div>`
                                }
                            </div>
                        </div>
                        <div style="flex: 1; display: flex; flex-direction: column;">
                            <div style="display: flex; align-items: center; justify-content: space-between; margin-bottom: 8px;">
                                <label for="providerName" class="form-label" style="margin-bottom: 0;">Name *</label>
                                <div class="toggle-switch">
                                    <input type="checkbox" id="providerState" data-test-id="provider-state" class="toggle-input" ${this.selectedServiceProvider.state === 'ACTIVE' ? 'checked' : ''}>
                                    <label for="providerState" class="toggle-label">Active</label>
                                </div>
                            </div>
                            <div>
                                <input type="text" id="providerName" data-test-id="provider-name" class="form-control" value="${this.selectedServiceProvider.name || ''}" required oninput="validateNameField()">
                                <div class="invalid-feedback" data-test-id="name-validation-error" id="nameError" style="display: none;"></div>
                            </div>
                        </div>
                    </div>
                </div>

                <!-- Comment Field -->
                <div class="form-group">
                    <label for="providerComment" class="form-label">Comment</label>
                    <textarea id="providerComment" data-test-id="provider-comment" class="form-control form-textarea" placeholder="Optional comment about this service provider">${this.selectedServiceProvider.comment || ''}</textarea>
                </div>

                <!-- OCR Comment Field -->
                <div class="form-group">
                    <label for="providerOcrComment" class="form-label">OCR Comment</label>
                    <textarea id="providerOcrComment" data-test-id="provider-ocr-comment" class="form-control form-textarea" placeholder="Comment to help OCR recognition">${this.selectedServiceProvider.commentForOcr || ''}</textarea>
                </div>

                <!-- Regular Frequency Field -->
                <div class="form-group">
                    <label for="providerFrequency" class="form-label">Regular Frequency</label>
                    <select id="providerFrequency" data-test-id="provider-frequency" class="form-control form-select">
                        <option value="NOT_REGULAR" ${this.selectedServiceProvider.regular === 'NOT_REGULAR' ? 'selected' : ''}>Not Regular</option>
                        <option value="YEARLY" ${this.selectedServiceProvider.regular === 'YEARLY' ? 'selected' : ''}>Yearly</option>
                        <option value="MONTHLY" ${this.selectedServiceProvider.regular === 'MONTHLY' ? 'selected' : ''}>Monthly</option>
                        <option value="WEEKLY" ${this.selectedServiceProvider.regular === 'WEEKLY' ? 'selected' : ''}>Weekly</option>
                    </select>
                </div>


                <!-- Custom Fields Section -->
                <div class="form-group">
                    <label class="form-label">Custom Fields</label>
                    <div id="customFieldsContainer" data-test-id="custom-fields-container">
                        ${this.renderCustomFields(this.parseCustomFields(this.selectedServiceProvider.customFields))}
                    </div>
                    <button type="button" class="btn-upload" data-test-id="add-custom-field-button" onclick="addCustomField()">
                        <i class="fas fa-plus me-1"></i> Add Custom Field
                    </button>
                </div>

                <!-- Form Actions -->
                <div class="form-actions" style="justify-content: ${isNewProvider ? 'flex-end' : 'space-between'};">
                    ${!isNewProvider ? `
                        <button type="button" class="btn-delete" data-test-id="delete-button" onclick="deleteServiceProvider()">
                            <i class="fas fa-trash me-1"></i> Delete
                        </button>
                    ` : ''}
                    <div style="display: flex; gap: 10px;">
                        <button type="button" class="btn-cancel" data-test-id="cancel-button" onclick="cancelEdit()">
                            <i class="fas fa-times me-1"></i> Cancel
                        </button>
                        <button type="submit" class="btn-save" data-test-id="save-button" id="saveButton" ${isNameEmpty ? 'disabled' : ''}>
                            <i class="fas fa-save me-1"></i> Save
                        </button>
                    </div>
                </div>
            </form>
        `;

        formContainer.innerHTML = formHtml;
        this.setupCustomFieldEventListeners();
    }

    /**
     * Setup event listeners for custom field inputs using event delegation
     */
    setupCustomFieldEventListeners() {
        const formContainer = document.getElementById('serviceProviderForm');
        if (!formContainer) return;

        // Remove existing listeners to avoid duplicates
        const existingHandlers = formContainer.getAttribute('data-handlers-setup');
        if (existingHandlers === 'true') return;

        // Use event delegation for better reliability with dynamic content
        ['input', 'change', 'blur'].forEach(eventType => {
            formContainer.addEventListener(eventType, (e) => {
                if (e.target.classList.contains('custom-field-key-input')) {
                    const index = parseInt(e.target.getAttribute('data-field-index'));
                    this.updateCustomFieldKey(index, e.target.value);
                } else if (e.target.classList.contains('custom-field-value-input')) {
                    const index = parseInt(e.target.getAttribute('data-field-index'));
                    const entries = Object.entries(this.selectedServiceProvider.customFields || {});
                    if (entries[index]) {
                        const [key] = entries[index];
                        this.updateCustomFieldValue(key, e.target.value);
                    }
                }
            });
        });

        // Add click event listener for remove buttons
        formContainer.addEventListener('click', (e) => {
            if (e.target.closest('.remove-custom-field-btn')) {
                const button = e.target.closest('.remove-custom-field-btn');
                const fieldKey = button.getAttribute('data-field-key');
                if (fieldKey !== null) {
                    this.removeCustomField(fieldKey);
                }
            }
        });

        // Mark that handlers are setup
        formContainer.setAttribute('data-handlers-setup', 'true');
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
            <div class="custom-field-item mb-2" data-test-id="custom-field-item" data-field-index="${index}">
                <div class="row">
                    <div class="col-md-5">
                        <input type="text" class="form-control custom-field-key-input" data-test-id="custom-field-key" placeholder="Field name" value="${this.escapeHtml(key)}" data-field-index="${index}">
                    </div>
                    <div class="col-md-6">
                        <input type="text" class="form-control custom-field-value-input" data-test-id="custom-field-value" placeholder="Field value" value="${this.escapeHtml(value)}" data-field-index="${index}">
                    </div>
                    <div class="col-md-1">
                        <button type="button" class="btn-upload remove-custom-field-btn" data-test-id="remove-custom-field-button" data-field-key="${this.escapeHtml(key)}">
                            <i class="fas fa-trash"></i>
                        </button>
                    </div>
                </div>
            </div>
        `).join('');
    }

    /**
     * Parse custom fields from JSON string to object
     * @param {string|Object} customFields - The custom fields (JSON string or object)
     * @returns {Object} Parsed custom fields object
     */
    parseCustomFields(customFields) {
        if (!customFields) return {};
        if (typeof customFields === 'object') return customFields;
        try {
            return JSON.parse(customFields);
        } catch (e) {
            console.warn('Failed to parse custom fields JSON:', customFields, e);
            return {};
        }
    }

    /**
     * Escape HTML characters to prevent XSS
     * @param {string} text - The text to escape
     * @returns {string} Escaped HTML
     */
    escapeHtml(text) {
        if (typeof text !== 'string') return text;
        const div = document.createElement('div');
        div.textContent = text;
        return div.innerHTML;
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
     * Validate name field and toggle save button state
     */
    validateNameField() {
        const nameElement = document.getElementById('providerName');
        const saveButton = document.getElementById('saveButton');
        
        if (!nameElement || !saveButton) return;
        
        const isNameEmpty = !nameElement.value.trim();
        saveButton.disabled = isNameEmpty;
        
        // Update the selected service provider name to trigger re-render logic
        if (this.selectedServiceProvider) {
            this.selectedServiceProvider.name = nameElement.value;
        }
    }

    /**
     * Save service provider
     * @param {Event} event - The form submit event
     */
    saveServiceProvider(event) {
        event.preventDefault();
        
        // Check if required elements exist
        const nameElement = document.getElementById('providerName');
        const commentElement = document.getElementById('providerComment');
        const ocrCommentElement = document.getElementById('providerOcrComment');
        const frequencyElement = document.getElementById('providerFrequency');
        const stateElement = document.getElementById('providerState');
        
        if (!nameElement) {
            console.error('Provider name element not found');
            return;
        }
        
        // Collect custom fields from DOM
        const customFields = {};
        const customFieldItems = document.querySelectorAll('[data-test-id="custom-field-item"]');
        customFieldItems.forEach(item => {
            const keyInput = item.querySelector('[data-test-id="custom-field-key"]');
            const valueInput = item.querySelector('[data-test-id="custom-field-value"]');
            if (keyInput && valueInput && keyInput.value.trim()) {
                customFields[keyInput.value.trim()] = valueInput.value;
            }
        });

        const formData = {
            name: nameElement.value,
            comment: commentElement ? commentElement.value : '',
            commentForOcr: ocrCommentElement ? ocrCommentElement.value : '',
            regular: frequencyElement ? frequencyElement.value : 'NOT_REGULAR',
            customFields: customFields
        };
        
        // Handle state separately - we'll use the state change endpoint if needed
        const newState = stateElement ? (stateElement.checked ? 'ACTIVE' : 'HIDDEN') : 'ACTIVE';

        // Validate required fields - if name is empty, the save button should be disabled anyway
        if (!formData.name.trim()) {
            return;
        }

        // Filter out custom fields with empty keys and convert to JSON string
        if (formData.customFields) {
            // Remove empty keys
            const filteredCustomFields = {};
            for (const [key, value] of Object.entries(formData.customFields)) {
                if (key.trim()) {
                    filteredCustomFields[key.trim()] = value;
                }
            }
            // Convert to JSON string for backend
            formData.customFields = Object.keys(filteredCustomFields).length > 0 
                ? JSON.stringify(filteredCustomFields) 
                : null;
        }

        const isNewProvider = this.selectedServiceProvider ? this.selectedServiceProvider.id === null : true;
        const url = isNewProvider ? '/api/service-providers' : `/api/service-providers/${this.selectedServiceProvider.id}`;
        const method = isNewProvider ? 'POST' : 'PUT';


        const saveButton = document.getElementById('saveButton');
        if (saveButton) {
            saveButton.disabled = true;
        }

        // Check for CSRF token
        const csrfMeta = document.querySelector('meta[name="_csrf"]');
        if (!csrfMeta) {
            console.error('CSRF token meta tag not found');
            this.showErrorMessage('Security token not found. Please refresh the page.');
            if (saveButton) saveButton.disabled = false;
            return;
        }

        fetch(url, {
            method: method,
            headers: {
                'Content-Type': 'application/json',
                'X-CSRF-TOKEN': csrfMeta.getAttribute('content')
            },
            body: JSON.stringify(formData)
        })
        .then(response => {
            if (!response.ok) {
                return response.text().then(text => {
                    console.error('Server error response:', text);
                    let errorMessage = 'Failed to save service provider';
                    try {
                        const errorData = JSON.parse(text);
                        if (errorData.message) {
                            errorMessage = errorData.message;
                        }
                    } catch (e) {
                        // If not JSON, use the text as error message if it's reasonable length
                        if (text && text.length < 200) {
                            errorMessage = text;
                        }
                    }
                    throw new Error(errorMessage);
                });
            }
            return response.json();
        })
        .then(response => {
            // Handle both direct ServiceProvider and ServiceProviderResponse formats
            const data = response.data ? response.data : response;
            this.selectedServiceProvider = data;
            
            // Handle state change if needed (only for existing providers)
            if (!isNewProvider && this.selectedServiceProvider && 
                this.selectedServiceProvider.state !== newState) {
                return this.changeServiceProviderState(this.selectedServiceProvider.id, newState)
                    .then(() => {
                        this.loadServicesData(); // Reload the list
                        this.showSuccessMessage('Service provider updated successfully');
                    });
            } else {
                this.loadServicesData(); // Reload the list
                this.showSuccessMessage(isNewProvider ? 'Service provider created successfully' : 'Service provider updated successfully');
                return Promise.resolve();
            }
        })
        .catch(error => {
            console.error('Error saving service provider:', error);
            this.showErrorMessage(error.message || 'Failed to save service provider. Please try again.');
        })
        .finally(() => {
            const saveButton = document.getElementById('saveButton');
            if (saveButton) {
                saveButton.disabled = false;
            }
        });
    }

    /**
     * Change service provider state
     * @param {number} id - Service provider ID
     * @param {string} state - New state (ACTIVE or HIDDEN)
     * @returns {Promise} Promise that resolves when state is changed
     */
    changeServiceProviderState(id, state) {
        const csrfMeta = document.querySelector('meta[name="_csrf"]');
        if (!csrfMeta) {
            return Promise.reject(new Error('CSRF token not found'));
        }

        return fetch(`/api/service-providers/${id}/state`, {
            method: 'PATCH',
            headers: {
                'Content-Type': 'application/json',
                'X-CSRF-TOKEN': csrfMeta.getAttribute('content')
            },
            body: JSON.stringify({ state: state })
        })
        .then(response => {
            if (!response.ok) {
                return response.text().then(text => {
                    throw new Error(text || 'Failed to change service provider state');
                });
            }
            return response.json();
        })
        .then(response => {
            const data = response.data ? response.data : response;
            if (this.selectedServiceProvider) {
                this.selectedServiceProvider.state = state;
            }
            return data;
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
                `<div data-test-id="avatar-preview" id="avatarPreview" onclick="uploadAvatar()" style="cursor: pointer;"><div class="avatar-preview-fallback avatar-fallback">${this.selectedServiceProvider.name ? this.selectedServiceProvider.name.substring(0, 1).toUpperCase() : 'SP'}</div></div>`;
        }
    }

    /**
     * Add custom field
     */
    addCustomField() {
        if (!this.selectedServiceProvider.customFields) {
            this.selectedServiceProvider.customFields = {};
        }
        
        // Add empty field that user can fill in
        const fieldKey = '';
        this.selectedServiceProvider.customFields[fieldKey] = '';
        
        // Re-render only the custom fields section to avoid losing form data
        const customFieldsContainer = document.getElementById('customFieldsContainer');
        if (customFieldsContainer) {
            customFieldsContainer.innerHTML = this.renderCustomFields(this.selectedServiceProvider.customFields);
            
            // Focus on the newly added field key input
            setTimeout(() => {
                const customFields = document.querySelectorAll('[data-test-id="custom-field-key"]');
                if (customFields.length > 0) {
                    const lastField = customFields[customFields.length - 1];
                    lastField.focus();
                }
            }, 100);
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
     * Collect current custom field values from the form
     * @returns {Object} Current custom fields from form inputs
     */
    collectCurrentCustomFields() {
        const currentFields = {};
        const customFieldsContainer = document.getElementById('customFieldsContainer');
        if (!customFieldsContainer) return currentFields;

        const fieldItems = customFieldsContainer.querySelectorAll('.custom-field-item');
        fieldItems.forEach(item => {
            const keyInput = item.querySelector('.custom-field-key-input');
            const valueInput = item.querySelector('.custom-field-value-input');
            
            if (keyInput && valueInput) {
                const key = keyInput.value.trim();
                const value = valueInput.value.trim();
                if (key) {
                    currentFields[key] = value;
                }
            }
        });
        
        return currentFields;
    }

    /**
     * Remove custom field
     * @param {string} key - The field key to remove
     */
    removeCustomField(key) {
        if (!this.selectedServiceProvider.customFields) return;
        
        // Collect current form values before deletion
        const currentFields = this.collectCurrentCustomFields();
        
        // Remove the specified field
        delete currentFields[key];
        
        // Update the model with current values
        this.selectedServiceProvider.customFields = currentFields;
        
        // Re-render only the custom fields section
        const customFieldsContainer = document.getElementById('customFieldsContainer');
        if (customFieldsContainer) {
            customFieldsContainer.innerHTML = this.renderCustomFields(this.selectedServiceProvider.customFields);
        }
    }

    /**
     * Show success message
     * @param {string} message - The success message to display
     */
    showSuccessMessage(message) {
        const alertHtml = `
            <div class="alert alert-success alert-dismissible fade show" role="alert" data-test-id="success-message">
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

// Initialize the module
servicesModule.init();

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
window.validateNameField = () => servicesModule.validateNameField();

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