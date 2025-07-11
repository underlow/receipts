/**
 * Generic table rendering module for dashboard tables
 * Provides centralized table rendering functionality with pagination, sorting, and search
 */

class TableRenderer {
    constructor() {
        this.formatters = new Map();
        this.emptyStateIcons = new Map();
        this.setupDefaultFormatters();
        this.setupDefaultEmptyStateIcons();
    }

    /**
     * Set up default formatters for different table types
     */
    setupDefaultFormatters() {
        // Status badge formatter for inbox
        this.formatters.set('inbox-status', (cellValue) => {
            const statusMappings = {
                'Pending': 'status-pending',
                'Processed': 'status-done',
                'Done': 'status-done',
                'Failed': 'status-pending',
                'Approved': 'status-done',
                'In Process': 'status-in-progress'
            };

            const statusClass = statusMappings[cellValue] || 'status-pending';
            return `<span class="status-indicator ${statusClass}">${cellValue}</span>`;
        });

        // Action buttons formatter for bills
        this.formatters.set('bills-actions', (cellValue, rowId) => {
            if (cellValue === 'Edit | Remove') {
                return `
                    <div class="action-buttons">
                        <button class="action-btn action-btn-primary" onclick="billsModule.editBill('${rowId}')" title="Edit bill">
                            Edit
                        </button>
                        <button class="action-btn action-btn-danger" onclick="billsModule.removeBill('${rowId}')" title="Remove bill">
                            Remove
                        </button>
                        <button class="dropdown-toggle-btn" onclick="billsModule.toggleActions('${rowId}')" title="More actions">
                            ⋮
                        </button>
                    </div>
                `;
            } else if (cellValue === 'Removed') {
                return `<span class="text-muted">Removed</span>`;
            } else {
                return cellValue;
            }
        });

        // Action buttons formatter for receipts
        this.formatters.set('receipts-actions', (cellValue, rowId) => {
            if (cellValue === 'Edit | Remove') {
                return `
                    <div class="action-buttons">
                        <button class="action-btn action-btn-primary" onclick="receiptsModule.editReceipt('${rowId}')" title="Edit receipt">
                            Edit
                        </button>
                        <button class="action-btn action-btn-danger" onclick="receiptsModule.removeReceipt('${rowId}')" title="Remove receipt">
                            Remove
                        </button>
                        <button class="dropdown-toggle-btn" onclick="receiptsModule.toggleReceiptActions('${rowId}')" title="More actions">
                            ⋮
                        </button>
                    </div>
                `;
            } else if (cellValue === 'Removed') {
                return `<span class="text-muted">Removed</span>`;
            } else {
                return cellValue;
            }
        });
    }

    /**
     * Set up default empty state icons for different table types
     */
    setupDefaultEmptyStateIcons() {
        this.emptyStateIcons.set('inbox', {
            icon: 'fas fa-inbox',
            title: 'No data available',
            text: 'There are no items to display at the moment.'
        });

        this.emptyStateIcons.set('bills', {
            icon: 'fas fa-file-invoice-dollar',
            title: 'No bills available',
            text: 'There are no bills to display at the moment.'
        });

        this.emptyStateIcons.set('receipts', {
            icon: 'fas fa-receipt',
            title: 'No receipts available',
            text: 'There are no receipts to display at the moment.'
        });
    }

    /**
     * Register a custom formatter for a specific table-column combination
     * @param {string} key - The formatter key (e.g., 'inbox-status', 'bills-actions')
     * @param {Function} formatter - The formatter function
     */
    registerFormatter(key, formatter) {
        this.formatters.set(key, formatter);
    }

    /**
     * Register a custom empty state configuration for a table type
     * @param {string} tableType - The table type identifier
     * @param {Object} config - Empty state configuration with icon, title, and text
     */
    registerEmptyState(tableType, config) {
        this.emptyStateIcons.set(tableType, config);
    }

    /**
     * Generate HTML for a rendered table from JSON data
     * @param {Object} data - The table data from API
     * @returns {string} HTML string for the table
     */
    renderTable(data) {
        let html = '<div class="table-container">';

        // Add search box if enabled
        if (data.searchEnabled) {
            html += this.renderSearchBox(data.tableId);
        }

        // Add table wrapper
        html += this.renderTableWrapper(data);

        // Add pagination if configured
        if (data.paginationConfig) {
            html += this.renderPagination(data);
        }

        html += '</div>';
        return html;
    }

    /**
     * Render search box HTML
     * @param {string} tableId - The table ID
     * @returns {string} HTML string for search box
     */
    renderSearchBox(tableId) {
        return `
            <div class="search-container mb-3">
                <div class="input-group">
                    <span class="input-group-text">
                        <i class="fas fa-search"></i>
                    </span>
                    <input type="text" class="form-control" id="${tableId}-search" placeholder="Search..." aria-label="Search table data">
                </div>
            </div>
        `;
    }

    /**
     * Render table wrapper with headers and body
     * @param {Object} data - The table data from API
     * @returns {string} HTML string for table wrapper
     */
    renderTableWrapper(data) {
        let html = `
            <div class="modern-table-wrapper">
                <table class="modern-table" role="table" aria-label="Data table">
                    <thead class="modern-table-header">
                        <tr>
        `;

        // Add column headers
        data.columns.forEach(column => {
            html += this.renderTableHeader(column, data);
        });

        html += `
                        </tr>
                    </thead>
                    <tbody class="modern-table-body">
        `;

        // Add data rows or empty state
        if (data.data.length === 0) {
            html += this.renderEmptyState(data.tableId, data.columns.length);
        } else {
            data.data.forEach(row => {
                html += this.renderTableRow(row, data);
            });
        }

        html += `
                    </tbody>
                </table>
            </div>
        `;

        return html;
    }

    /**
     * Render table header cell
     * @param {Object} column - Column configuration
     * @param {Object} data - Table data
     * @returns {string} HTML string for header cell
     */
    renderTableHeader(column, data) {
        const sortClass = column.sortable ? 'sortable' : '';
        const sortIcon = column.sortable && data.sortKey === column.key ?
            (data.sortDirection === 'ASC' ? 'fa-sort-up' : 'fa-sort-down') :
            (column.sortable ? 'fa-sort' : '');

        return `
            <th scope="col" class="${sortClass}" ${column.sortable ? `data-sort="${column.key}"` : ''}>
                <div class="header-content">
                    ${column.label}
                    ${column.sortable ? `<i class="fas ${sortIcon} ms-1"></i>` : ''}
                </div>
            </th>
        `;
    }

    /**
     * Render table row
     * @param {Object} row - Row data
     * @param {Object} data - Table data
     * @returns {string} HTML string for table row
     */
    renderTableRow(row, data) {
        let html = '<tr class="modern-table-row">';
        
        data.columns.forEach(column => {
            const cellValue = row[column.key] || '';
            const formattedValue = this.formatCellValue(cellValue, column, row, data);
            html += `<td class="modern-table-cell">${formattedValue}</td>`;
        });
        
        html += '</tr>';
        return html;
    }

    /**
     * Format cell value using registered formatters
     * @param {string} cellValue - The raw cell value
     * @param {Object} column - Column configuration
     * @param {Object} row - Row data
     * @param {Object} data - Table data
     * @returns {string} Formatted cell value
     */
    formatCellValue(cellValue, column, row, data) {
        const formatterKey = `${data.tableId}-${column.key}`;
        const formatter = this.formatters.get(formatterKey);
        
        if (formatter) {
            return formatter(cellValue, row.id, row);
        }
        
        return cellValue;
    }

    /**
     * Render empty state for table
     * @param {string} tableId - The table ID
     * @param {number} columnCount - Number of columns
     * @returns {string} HTML string for empty state
     */
    renderEmptyState(tableId, columnCount) {
        const emptyState = this.emptyStateIcons.get(tableId) || this.emptyStateIcons.get('inbox');
        
        return `
            <tr>
                <td colspan="${columnCount}" class="empty-state-cell">
                    <div class="empty-state-content">
                        <i class="${emptyState.icon} empty-state-icon"></i>
                        <div class="empty-state-title">${emptyState.title}</div>
                        <p class="empty-state-text">${emptyState.text}</p>
                    </div>
                </td>
            </tr>
        `;
    }

    /**
     * Render pagination HTML
     * @param {Object} data - The table data from API
     * @returns {string} HTML string for pagination
     */
    renderPagination(data) {
        let html = `
            <nav aria-label="Table pagination">
                <ul class="pagination justify-content-center">
        `;

        // Previous button
        const prevDisabled = data.paginationConfig.currentPage <= 1 ? 'disabled' : '';
        html += `
            <li class="page-item ${prevDisabled}">
                <a class="page-link" href="#" data-page="${data.paginationConfig.currentPage - 1}" aria-label="Previous">
                    <span aria-hidden="true">&laquo;</span>
                    Previous
                </a>
            </li>
        `;

        // Page numbers
        for (let page = 1; page <= data.totalPages; page++) {
            const activeClass = page === data.paginationConfig.currentPage ? 'active' : '';
            html += `
                <li class="page-item ${activeClass}">
                    <a class="page-link" href="#" data-page="${page}">${page}</a>
                </li>
            `;
        }

        // Next button
        const nextDisabled = data.paginationConfig.currentPage >= data.totalPages ? 'disabled' : '';
        html += `
            <li class="page-item ${nextDisabled}">
                <a class="page-link" href="#" data-page="${data.paginationConfig.currentPage + 1}" aria-label="Next">
                    Next
                    <span aria-hidden="true">&raquo;</span>
                </a>
            </li>
                </ul>
            </nav>
        `;

        return html;
    }
}

// Create global table renderer instance
const tableRenderer = new TableRenderer();

// Export for module systems
if (typeof module !== 'undefined' && module.exports) {
    module.exports = TableRenderer;
}