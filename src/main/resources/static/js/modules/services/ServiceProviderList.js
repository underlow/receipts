/**
 * ServiceProviderList component handles the display and management of service provider lists
 * Responsible for rendering provider lists, handling selection, and managing empty states
 */
class ServiceProviderList {
    constructor() {
    }

    /**
     * Render service provider list with proper selection state
     * @param {Array} serviceProviders - Array of all service providers  
     * @param {Object} selectedServiceProvider - Currently selected provider
     */
    renderServiceProviderList(serviceProviders, selectedServiceProvider) {
        const listContainer = document.getElementById('serviceProviderList');

        // Filter out HIDDEN providers - they should not appear in the list at all
        const activeProviders = serviceProviders.filter(provider => provider.state === 'ACTIVE');

        if (activeProviders.length === 0) {
            this.renderEmptyState(listContainer);
            return;
        }

        const listHtml = `
            <ul class="service-provider-list">
                ${activeProviders.map(provider => `
                    <li class="service-provider-item ${selectedServiceProvider && selectedServiceProvider.id === provider.id ? 'selected' : ''}" 
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
     * Render empty state when no service providers exist
     * @param {HTMLElement} listContainer - Container element for the list
     */
    renderEmptyState(listContainer) {
        listContainer.innerHTML = `
            <div class="empty-state">
                <i class="fas fa-building empty-state-icon"></i>
                <div class="empty-state-title">No Service Providers</div>
                <p class="empty-state-text">Create your first service provider to get started.</p>
            </div>
        `;
    }

    /**
     * Select a service provider from the list
     * @param {Array} serviceProviders - Array of all service providers
     * @param {number} providerId - ID of provider to select
     * @returns {Object|null} Selected service provider or null if not found
     */
    selectServiceProvider(serviceProviders, providerId) {
        return serviceProviders.find(p => p.id === providerId) || null;
    }

    /**
     * Update provider data in the list and re-render if needed
     * @param {Array} serviceProviders - Array of all service providers
     * @param {number} id - Provider ID to update
     * @param {Object} data - New provider data
     * @param {Object} selectedServiceProvider - Currently selected provider
     * @returns {Array} Updated service providers array
     */
    updateProvider(serviceProviders, id, data, selectedServiceProvider) {
        const providerIndex = serviceProviders.findIndex(p => p.id === id);
        if (providerIndex !== -1) {
            serviceProviders[providerIndex] = { ...serviceProviders[providerIndex], ...data };
            // Re-render list to reflect changes
            this.renderServiceProviderList(serviceProviders, selectedServiceProvider);
        }
        return serviceProviders;
    }

    /**
     * Clear current selection and update display
     * @param {Array} serviceProviders - Array of all service providers
     */
    clearSelection(serviceProviders) {
        this.renderServiceProviderList(serviceProviders, null);
    }

    /**
     * Render error state when loading fails
     * @param {string} errorMessage - Error message to display
     */
    renderErrorState(errorMessage = 'Failed to load service providers. Please try again.') {
        const listContainer = document.getElementById('serviceProviderList');
        listContainer.innerHTML = `
            <div class="alert alert-danger m-3" role="alert">
                <i class="fas fa-exclamation-triangle me-2"></i>
                ${errorMessage}
                <button class="btn btn-sm btn-outline-danger ms-2" onclick="loadServicesData()">
                    <i class="fas fa-redo me-1"></i> Retry
                </button>
            </div>
        `;
    }
}