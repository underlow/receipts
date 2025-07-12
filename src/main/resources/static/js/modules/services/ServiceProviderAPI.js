/**
 * ServiceProviderAPI - Handles all HTTP communication with backend for service providers
 * Manages CRUD operations and state changes for service provider entities
 */
class ServiceProviderAPI {
    constructor(alertManager) {
        this.alertManager = alertManager;
    }

    /**
     * Fetch all service providers from the API
     * @returns {Promise<Array>} Promise that resolves to array of service providers
     */
    fetchServiceProviders() {
        return fetch('/api/service-providers')
            .then(response => {
                if (!response.ok) {
                    throw new Error('Failed to load service providers');
                }
                return response.json();
            })
            .catch(error => {
                console.error('Error loading service providers:', error);
                throw error;
            });
    }

    /**
     * Save service provider (create new or update existing)
     * @param {Object} data - Service provider data
     * @param {boolean} isNew - Whether this is a new provider
     * @param {number|null} providerId - Provider ID (null for new providers)
     * @returns {Promise<Object>} Promise that resolves to saved service provider data
     */
    saveServiceProvider(data, isNew, providerId) {
        const url = isNew ? '/api/service-providers' : `/api/service-providers/${providerId}`;
        const method = isNew ? 'POST' : 'PUT';

        const csrfToken = this.getCsrfToken();
        if (!csrfToken) {
            const error = new Error('Security token not found. Please refresh the page.');
            this.alertManager.showErrorMessage(error.message);
            throw error;
        }

        return fetch(url, {
            method: method,
            headers: {
                'Content-Type': 'application/json',
                'X-CSRF-TOKEN': csrfToken
            },
            body: JSON.stringify(data)
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
            const responseData = response.data ? response.data : response;
            this.alertManager.showSuccessMessage(isNew ? 'Service provider created successfully' : 'Service provider updated successfully');
            return responseData;
        })
        .catch(error => {
            console.error('Error saving service provider:', error);
            this.alertManager.showErrorMessage(error.message || 'Failed to save service provider. Please try again.');
            throw error;
        });
    }

    /**
     * Change service provider state
     * @param {number} id - Service provider ID
     * @param {string} state - New state (ACTIVE or HIDDEN)
     * @returns {Promise<Object>} Promise that resolves when state is changed
     */
    changeServiceProviderState(id, state) {
        const csrfToken = this.getCsrfToken();
        if (!csrfToken) {
            throw new Error('CSRF token not found');
        }

        return fetch(`/api/service-providers/${id}/state`, {
            method: 'PATCH',
            headers: {
                'Content-Type': 'application/json',
                'X-CSRF-TOKEN': csrfToken
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
            return data;
        });
    }

    /**
     * Delete service provider
     * @param {number} id - Service provider ID
     * @returns {Promise<void>} Promise that resolves when provider is deleted
     */
    deleteServiceProvider(id) {
        const csrfToken = this.getCsrfToken();
        if (!csrfToken) {
            throw new Error('CSRF token not found');
        }

        return fetch(`/api/service-providers/${id}`, {
            method: 'DELETE',
            headers: {
                'X-CSRF-TOKEN': csrfToken
            }
        })
        .then(response => {
            if (!response.ok) {
                throw new Error('Failed to delete service provider');
            }
            this.alertManager.showSuccessMessage('Service provider deleted successfully');
        })
        .catch(error => {
            console.error('Error deleting service provider:', error);
            this.alertManager.showErrorMessage('Failed to delete service provider. Please try again.');
            throw error;
        });
    }

    /**
     * Get CSRF token from meta tag
     * @returns {string|null} CSRF token or null if not found
     */
    getCsrfToken() {
        const csrfMeta = document.querySelector('meta[name="_csrf"]');
        if (!csrfMeta) {
            console.error('CSRF token meta tag not found');
            return null;
        }
        return csrfMeta.getAttribute('content');
    }
}