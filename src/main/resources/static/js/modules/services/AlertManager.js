/**
 * AlertManager - Centralized alert/message handling for the dashboard
 * Handles display of success messages, error messages, and general alerts
 */
class AlertManager {
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