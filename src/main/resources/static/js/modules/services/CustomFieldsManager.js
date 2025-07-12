/**
 * CustomFieldsManager handles custom fields functionality for service providers
 * Provides methods to render, parse, and manipulate custom fields
 */
class CustomFieldsManager {
    
    /**
     * Render custom fields as HTML
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
     * Add custom field to existing fields
     * @param {Object} customFields - The existing custom fields object
     * @returns {Object} Updated custom fields with new empty field
     */
    addCustomField(customFields) {
        if (!customFields) {
            customFields = {};
        }

        // Add empty field that user can fill in
        const fieldKey = '';
        customFields[fieldKey] = '';

        return customFields;
    }

    /**
     * Update custom field key
     * @param {Object} customFields - The custom fields object
     * @param {number} index - The field index
     * @param {string} newKey - The new key value
     * @returns {Object} Updated custom fields
     */
    updateCustomFieldKey(customFields, index, newKey) {
        if (!customFields) return {};

        const entries = Object.entries(customFields);
        if (entries[index]) {
            const [oldKey, value] = entries[index];
            delete customFields[oldKey];
            customFields[newKey] = value;
        }

        return customFields;
    }

    /**
     * Update custom field value
     * @param {Object} customFields - The custom fields object
     * @param {string} key - The field key
     * @param {string} newValue - The new value
     * @returns {Object} Updated custom fields
     */
    updateCustomFieldValue(customFields, key, newValue) {
        if (!customFields) return {};
        customFields[key] = newValue;
        return customFields;
    }

    /**
     * Remove custom field
     * @param {Object} customFields - The custom fields object
     * @param {string} key - The field key to remove
     * @returns {Object} Updated custom fields with field removed
     */
    removeCustomField(customFields, key) {
        if (!customFields) return {};
        delete customFields[key];
        return customFields;
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
}