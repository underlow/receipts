/**
 * Service Provider Form Manager
 * Handles form rendering, validation, and data collection for service providers
 */
class ServiceProviderForm {
    /**
     * Initialize the form manager with dependencies
     * @param {CustomFieldsManager} customFieldsManager - Custom fields manager instance
     */
    constructor(customFieldsManager) {
        this.customFieldsManager = customFieldsManager;
    }

    /**
     * Render service provider form
     * @param {Object} selectedServiceProvider - The service provider data
     */
    renderServiceProviderForm(selectedServiceProvider) {
        const formContainer = document.getElementById('serviceProviderForm');
        if (!selectedServiceProvider) {
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
        const isNameEmpty = !selectedServiceProvider.name || selectedServiceProvider.name.trim() === '';

        const formHtml = `
            <form class="service-provider-form" data-test-id="service-provider-form" onsubmit="saveServiceProvider(event)" style="gap: 1rem;">
                <h2 class="form-title" data-test-id="form-title">${isNewProvider ? 'Create Service Provider' : 'Edit Service Provider'}</h2>
                <!-- Avatar and Name Section -->
                <div class="form-group">
                    <div style="display: flex; gap: 20px;">
                        <div style="display: flex; flex-direction: column;">
                            <label class="form-label">Avatar</label>
                            <div class="avatar-upload-section">
                                ${selectedServiceProvider.avatar ? 
                                    `<img src="/attachments/avatars/${selectedServiceProvider.avatar}" alt="Avatar" class="avatar-preview" data-test-id="avatar-preview" id="avatarPreview">` :
                                    `<div data-test-id="avatar-preview" id="avatarPreview"><div class="avatar-preview-fallback avatar-fallback">${selectedServiceProvider.name ? selectedServiceProvider.name.substring(0, 1).toUpperCase() : 'SP'}</div></div>`
                                }
                            </div>
                        </div>
                        <div style="flex: 1; display: flex; flex-direction: column;">
                            <div style="margin-bottom: 8px;">
                                <label for="providerName" class="form-label">Name *</label>
                            </div>
                            <div>
                                <input type="text" id="providerName" data-test-id="provider-name" class="form-control" value="${selectedServiceProvider.name || ''}" required oninput="validateNameField()">
                                <div class="invalid-feedback" data-test-id="name-validation-error" id="nameError" style="display: none;"></div>
                            </div>
                        </div>
                    </div>
                </div>

                <!-- Comment Field -->
                <div class="form-group" style="margin-bottom: 0;">
                    <label for="providerComment" class="form-label">Comment</label>
                    <textarea id="providerComment" data-test-id="provider-comment" class="form-control form-textarea" placeholder="Optional comment about this service provider">${selectedServiceProvider.comment || ''}</textarea>
                </div>

                <!-- OCR Comment Field -->
                <div class="form-group" style="margin-bottom: 0;">
                    <label for="providerOcrComment" class="form-label">OCR Comment</label>
                    <textarea id="providerOcrComment" data-test-id="provider-ocr-comment" class="form-control form-textarea" placeholder="Comment to help OCR recognition">${selectedServiceProvider.commentForOcr || ''}</textarea>
                </div>

                <!-- Regular Frequency Field -->
                <div class="form-group" style="margin-bottom: 0;">
                    <label for="providerFrequency" class="form-label">Regular Frequency</label>
                    <select id="providerFrequency" data-test-id="provider-frequency" class="form-control form-select">
                        <option value="NOT_REGULAR" ${selectedServiceProvider.regular === 'NOT_REGULAR' ? 'selected' : ''}>Not Regular</option>
                        <option value="YEARLY" ${selectedServiceProvider.regular === 'YEARLY' ? 'selected' : ''}>Yearly</option>
                        <option value="MONTHLY" ${selectedServiceProvider.regular === 'MONTHLY' ? 'selected' : ''}>Monthly</option>
                        <option value="WEEKLY" ${selectedServiceProvider.regular === 'WEEKLY' ? 'selected' : ''}>Weekly</option>
                    </select>
                </div>

                <!-- Custom Fields Section -->
                <div class="form-group" style="margin-bottom: 0;">
                    <label class="form-label">Custom Fields</label>
                    <div id="customFieldsContainer" data-test-id="custom-fields-container">
                        ${this.customFieldsManager.renderCustomFields(this.customFieldsManager.parseCustomFields(selectedServiceProvider.customFields))}
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
                    // Trigger custom field key update through global function
                    if (typeof window.updateCustomFieldKey === 'function') {
                        window.updateCustomFieldKey(index, e.target.value);
                    }
                } else if (e.target.classList.contains('custom-field-value-input')) {
                    const index = parseInt(e.target.getAttribute('data-field-index'));
                    // Need to get the key from the form to update the value
                    const keyInput = document.querySelector(`[data-field-index="${index}"].custom-field-key-input`);
                    if (keyInput && typeof window.updateCustomFieldValue === 'function') {
                        window.updateCustomFieldValue(keyInput.value, e.target.value);
                    }
                }
            });
        });

        // Add click event listener for remove buttons
        formContainer.addEventListener('click', (e) => {
            if (e.target.closest('.remove-custom-field-btn')) {
                const button = e.target.closest('.remove-custom-field-btn');
                const fieldKey = button.getAttribute('data-field-key');
                if (fieldKey !== null && typeof window.servicesModule !== 'undefined') {
                    window.servicesModule.removeCustomField(fieldKey);
                }
            }
        });

        // Mark that handlers are setup
        formContainer.setAttribute('data-handlers-setup', 'true');
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

        return !isNameEmpty;
    }

    /**
     * Collect form data from the rendered form
     * @returns {Object} The collected form data
     */
    collectFormData() {
        // Check if required elements exist
        const nameElement = document.getElementById('providerName');
        const commentElement = document.getElementById('providerComment');
        const ocrCommentElement = document.getElementById('providerOcrComment');
        const frequencyElement = document.getElementById('providerFrequency');

        if (!nameElement) {
            throw new Error('Provider name element not found');
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

        // Validate required fields
        if (!formData.name.trim()) {
            throw new Error('Name is required');
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

        return formData;
    }

    /**
     * Add custom field to the form
     * @param {Object} selectedServiceProvider - The service provider data
     */
    addCustomField(selectedServiceProvider) {
        if (!selectedServiceProvider.customFields) {
            selectedServiceProvider.customFields = {};
        }

        selectedServiceProvider.customFields = this.customFieldsManager.addCustomField(selectedServiceProvider.customFields);

        // Re-render only the custom fields section to avoid losing form data
        const customFieldsContainer = document.getElementById('customFieldsContainer');
        if (customFieldsContainer) {
            customFieldsContainer.innerHTML = this.customFieldsManager.renderCustomFields(selectedServiceProvider.customFields);

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
     * Update avatar preview in the form
     * @param {string} avatarPath - The new avatar path
     */
    updateAvatarPreview(avatarPath) {
        const avatarPreview = document.getElementById('avatarPreview');
        if (avatarPreview && avatarPath) {
            avatarPreview.outerHTML = `<img src="/attachments/avatars/${avatarPath}" alt="Avatar" class="avatar-preview" data-test-id="avatar-preview" id="avatarPreview">`;
        }
    }
}