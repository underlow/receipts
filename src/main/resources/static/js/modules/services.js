/**
 * Services functionality global wrapper
 * Provides global API for HTML onclick compatibility while using modular architecture
 */

// Create global services module instance
const servicesModule = new ServicesModule();
window.servicesModule = servicesModule;

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