/**
 * Main coordinator module for service providers functionality
 * Orchestrates all service provider components and maintains state
 */
class ServicesModule {
    constructor() {
        // Services tab state
        this.serviceProviders = [];
        this.selectedServiceProvider = null;
        this.isEditingServiceProvider = false;
        // Initialize components
        this.alertManager = new AlertManager();
        this.serviceProviderAPI = new ServiceProviderAPI(this.alertManager);
        this.customFieldsManager = new CustomFieldsManager();
        this.serviceProviderForm = new ServiceProviderForm(this.customFieldsManager);
        this.serviceProviderList = new ServiceProviderList();
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
        this.serviceProviderAPI.fetchServiceProviders()
            .then(data => {
                this.serviceProviders = data;
                this.renderServiceProviderList();
            })
            .catch(error => {
                console.error('Error loading service providers:', error);
                this.serviceProviderList.renderErrorState();
            });
    }

    /**
     * Render service provider list
     */
    renderServiceProviderList() {
        this.serviceProviderList.renderServiceProviderList(this.serviceProviders, this.selectedServiceProvider);
    }

    /**
     * Select a service provider
     * @param {number} providerId - The provider ID to select
     */
    selectServiceProvider(providerId) {
        this.selectedServiceProvider = this.serviceProviderList.selectServiceProvider(this.serviceProviders, providerId);
        if (this.selectedServiceProvider) {
            this.renderServiceProviderList(); // Re-render to show selection
            this.renderServiceProviderForm();
        }
    }

    /**
     * Render service provider form
     */
    renderServiceProviderForm() {
        this.serviceProviderForm.renderServiceProviderForm(this.selectedServiceProvider);
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
        const isValid = this.serviceProviderForm.validateNameField();
        
        // Update the selected service provider name to trigger re-render logic
        if (this.selectedServiceProvider) {
            const nameElement = document.getElementById('providerName');
            if (nameElement) {
                this.selectedServiceProvider.name = nameElement.value;
            }
        }
        
        return isValid;
    }

    /**
     * Save service provider
     * @param {Event} event - The form submit event
     */
    saveServiceProvider(event) {
        event.preventDefault();

        try {
            const formData = this.serviceProviderForm.collectFormData();
            const isNewProvider = this.selectedServiceProvider ? this.selectedServiceProvider.id === null : true;

            const saveButton = document.getElementById('saveButton');
            if (saveButton) {
                saveButton.disabled = true;
            }

            this.serviceProviderAPI.saveServiceProvider(formData, isNewProvider, this.selectedServiceProvider.id)
                .then(data => {
                    this.selectedServiceProvider = data;
                    this.loadServicesData(); // Reload the list
                    return Promise.resolve();
                })
                .catch(error => {
                    console.error('Error saving service provider:', error);
                })
                .finally(() => {
                    const saveButton = document.getElementById('saveButton');
                    if (saveButton) {
                        saveButton.disabled = false;
                    }
                });
        } catch (error) {
            console.error('Error collecting form data:', error);
        }
    }

    /**
     * Change service provider state
     * @param {number} id - Service provider ID
     * @param {string} state - New state (ACTIVE or HIDDEN)
     * @returns {Promise} Promise that resolves when state is changed
     */
    changeServiceProviderState(id, state) {
        return this.serviceProviderAPI.changeServiceProviderState(id, state)
            .then(data => {
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
            this.serviceProviderAPI.deleteServiceProvider(this.selectedServiceProvider.id)
                .then(() => {
                    this.selectedServiceProvider = null;
                    this.loadServicesData(); // Reload the list
                    this.renderServiceProviderForm();
                })
                .catch(error => {
                    console.error('Error deleting service provider:', error);
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
        if (this.selectedServiceProvider) {
            this.serviceProviderForm.addCustomField(this.selectedServiceProvider);
        }
    }

    /**
     * Update custom field key
     * @param {number} index - The field index
     * @param {string} newKey - The new key value
     */
    updateCustomFieldKey(index, newKey) {
        if (!this.selectedServiceProvider.customFields) return;
        this.selectedServiceProvider.customFields = this.customFieldsManager.updateCustomFieldKey(
            this.selectedServiceProvider.customFields, index, newKey);
    }

    /**
     * Update custom field value
     * @param {string} key - The field key
     * @param {string} newValue - The new value
     */
    updateCustomFieldValue(key, newValue) {
        if (!this.selectedServiceProvider.customFields) return;
        this.selectedServiceProvider.customFields = this.customFieldsManager.updateCustomFieldValue(
            this.selectedServiceProvider.customFields, key, newValue);
    }

    /**
     * Remove custom field
     * @param {string} key - The field key to remove
     */
    removeCustomField(key) {
        if (!this.selectedServiceProvider.customFields) return;

        // Collect current form values before deletion
        const currentFields = this.customFieldsManager.collectCurrentCustomFields();

        // Remove the specified field
        this.selectedServiceProvider.customFields = this.customFieldsManager.removeCustomField(currentFields, key);

        // Re-render only the custom fields section
        const customFieldsContainer = document.getElementById('customFieldsContainer');
        if (customFieldsContainer) {
            customFieldsContainer.innerHTML = this.customFieldsManager.renderCustomFields(this.selectedServiceProvider.customFields);
        }
    }
}