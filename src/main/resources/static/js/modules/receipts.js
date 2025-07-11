/**
 * Receipts functionality module for the dashboard
 * Handles receipts data loading, table rendering, and event listeners
 */

class ReceiptsModule {
    constructor() {
        // Receipts tab state
        this.currentPage = 0;
        this.currentSort = 'paymentDate';
        this.currentDirection = 'DESC';
        this.currentSearch = '';
    }

    /**
     * Initialize the receipts module
     * Sets up event listeners and loads initial data when tab is shown
     */
    init() {
        this.setupTabEventListener();
    }

    /**
     * Set up tab change event listener for receipts tab
     */
    setupTabEventListener() {
        const receiptsTab = document.querySelector('a[href="#receipts"]');
        if (receiptsTab) {
            receiptsTab.addEventListener('shown.bs.tab', () => {
                this.loadReceiptsData();
            });
        }
    }

    /**
     * Load receipts data from the API
     * @param {number} page - Page number (0-based)
     * @param {string} search - Search term
     */
    loadReceiptsData(page = 0, search = '') {
        this.currentPage = page;
        this.currentSearch = search;

        const params = new URLSearchParams({
            page: page,
            size: 10,
            sortBy: this.currentSort,
            sortDirection: this.currentDirection
        });

        if (search && search.trim() !== '') {
            params.append('search', search.trim());
        }

        fetch(`/api/receipts?${params.toString()}`)
            .then(response => {
                if (!response.ok) {
                    throw new Error('Failed to load receipts data');
                }
                return response.json();
            })
            .then(data => {
                this.renderReceiptsTable(data);
                document.getElementById('receipts-content').innerHTML = this.getRenderedTable(data);
                this.setupTableEventListeners();
            })
            .catch(error => {
                console.error('Error loading receipts data:', error);
                document.getElementById('receipts-content').innerHTML = `
                    <div class="alert alert-danger" role="alert">
                        <i class="fas fa-exclamation-triangle me-2"></i>
                        Failed to load receipts data. Please try again.
                        <button class="btn btn-sm btn-outline-danger ms-2" onclick="receiptsModule.loadReceiptsData()">
                            <i class="fas fa-redo me-1"></i> Retry
                        </button>
                    </div>
                `;
            });
    }

    /**
     * Set up event listeners for receipts table interactions
     */
    setupTableEventListeners() {
        // Search functionality
        const searchInput = document.getElementById('receipts-search');
        if (searchInput) {
            searchInput.addEventListener('input', (e) => {
                const searchTerm = e.target.value;
                clearTimeout(searchInput.timeout);
                searchInput.timeout = setTimeout(() => {
                    this.loadReceiptsData(0, searchTerm);
                }, 500);
            });
        }

        // Pagination
        const paginationLinks = document.querySelectorAll('.pagination .page-link');
        paginationLinks.forEach(link => {
            link.addEventListener('click', (e) => {
                e.preventDefault();
                const page = parseInt(link.getAttribute('data-page'));
                if (!isNaN(page) && page > 0) {
                    this.loadReceiptsData(page - 1, this.currentSearch); // Convert to 0-based
                }
            });
        });

        // Sorting
        const sortableHeaders = document.querySelectorAll('th.sortable');
        sortableHeaders.forEach(header => {
            header.addEventListener('click', () => {
                const sortKey = header.getAttribute('data-sort');
                if (sortKey) {
                    // Toggle direction if same column, otherwise use DESC as default
                    if (this.currentSort === sortKey) {
                        this.currentDirection = this.currentDirection === 'ASC' ? 'DESC' : 'ASC';
                    } else {
                        this.currentSort = sortKey;
                        this.currentDirection = 'DESC';
                    }
                    this.loadReceiptsData(0, this.currentSearch);
                }
            });

            // Add cursor pointer style
            header.style.cursor = 'pointer';
        });
    }

    /**
     * Render receipts table (placeholder for any receipts-specific rendering logic)
     * @param {Object} data - The table data from API
     */
    renderReceiptsTable(data) {
        console.log('Rendering receipts table with data:', data);
    }

    /**
     * Generate HTML for the rendered table from JSON data
     * @param {Object} data - The table data from API
     * @returns {string} HTML string for the table
     */
    getRenderedTable(data) {
        let html = '<div class="table-container">';

        // Add search box if enabled
        if (data.searchEnabled) {
            html += `
                <div class="search-container mb-3">
                    <div class="input-group">
                        <span class="input-group-text">
                            <i class="fas fa-search"></i>
                        </span>
                        <input type="text" class="form-control" id="${data.tableId}-search" placeholder="Search..." aria-label="Search table data">
                    </div>
                </div>
            `;
        }

        // Add table wrapper
        html += `
            <div class="modern-table-wrapper">
                <table class="modern-table" role="table" aria-label="Data table">
                    <thead class="modern-table-header">
                        <tr>
        `;

        // Add column headers
        data.columns.forEach(column => {
            const sortClass = column.sortable ? 'sortable' : '';
            const sortIcon = column.sortable && data.sortKey === column.key ?
                (data.sortDirection === 'ASC' ? 'fa-sort-up' : 'fa-sort-down') :
                (column.sortable ? 'fa-sort' : '');

            html += `
                <th scope="col" class="${sortClass}" ${column.sortable ? `data-sort="${column.key}"` : ''}>
                    <div class="header-content">
                        ${column.label}
                        ${column.sortable ? `<i class="fas ${sortIcon} ms-1"></i>` : ''}
                    </div>
                </th>
            `;
        });

        html += `
                        </tr>
                    </thead>
                    <tbody class="modern-table-body">
        `;

        // Add data rows or empty state
        if (data.data.length === 0) {
            html += `
                <tr>
                    <td colspan="${data.columns.length}" class="empty-state-cell">
                        <div class="empty-state-content">
                            <i class="fas fa-receipt empty-state-icon"></i>
                            <div class="empty-state-title">No receipts available</div>
                            <p class="empty-state-text">There are no receipts to display at the moment.</p>
                        </div>
                    </td>
                </tr>
            `;
        } else {
            data.data.forEach(row => {
                html += '<tr class="modern-table-row">';
                data.columns.forEach(column => {
                    const cellValue = row[column.key] || '';
                    let formattedValue = cellValue;

                    // Special formatting for actions column in receipts table
                    if (data.tableId === 'receipts' && column.key === 'actions') {
                        formattedValue = this.formatReceiptsActions(cellValue, row.id);
                    }

                    html += `<td class="modern-table-cell">${formattedValue}</td>`;
                });
                html += '</tr>';
            });
        }

        html += `
                    </tbody>
                </table>
            </div>
        `;

        // Add pagination if configured
        if (data.paginationConfig) {
            html += this.renderPagination(data);
        }

        html += '</div>';
        return html;
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

    /**
     * Format action buttons for receipts table
     * @param {string} actionsText - The actions text to format
     * @param {string} receiptId - The receipt ID
     * @returns {string} HTML string for action buttons
     */
    formatReceiptsActions(actionsText, receiptId) {
        if (actionsText === 'Edit | Remove') {
            return `
                <div class="action-buttons">
                    <button class="action-btn action-btn-primary" onclick="receiptsModule.editReceipt('${receiptId}')" title="Edit receipt">
                        Edit
                    </button>
                    <button class="action-btn action-btn-danger" onclick="receiptsModule.removeReceipt('${receiptId}')" title="Remove receipt">
                        Remove
                    </button>
                    <button class="dropdown-toggle-btn" onclick="receiptsModule.toggleReceiptActions('${receiptId}')" title="More actions">
                        ⋮
                    </button>
                </div>
            `;
        } else if (actionsText === 'Removed') {
            return `<span class="text-muted">Removed</span>`;
        } else {
            return actionsText;
        }
    }

    /**
     * Edit a receipt
     * @param {string} receiptId - The receipt ID to edit
     */
    editReceipt(receiptId) {
        // TODO: Implement edit receipt functionality
        console.log(`Editing receipt ${receiptId}`);
        alert(`Edit receipt ${receiptId}. (Functionality not yet implemented)`);
    }

    /**
     * Remove a receipt
     * @param {string} receiptId - The receipt ID to remove
     */
    removeReceipt(receiptId) {
        if (confirm('Are you sure you want to remove this receipt?')) {
            // TODO: Implement remove receipt functionality
            console.log(`Removing receipt ${receiptId}`);
            alert(`Receipt ${receiptId} removed. (Functionality not yet implemented)`);
        }
    }

    /**
     * Toggle dropdown actions for a receipt
     * @param {string} receiptId - The receipt ID
     */
    toggleReceiptActions(receiptId) {
        console.log(`Toggle actions for receipt ${receiptId}`);
        // TODO: Implement dropdown toggle functionality
    }
}

// Create global receipts module instance
const receiptsModule = new ReceiptsModule();

// Make functions globally available for backwards compatibility
window.loadReceiptsData = (page, search) => receiptsModule.loadReceiptsData(page, search);
window.editReceipt = (receiptId) => receiptsModule.editReceipt(receiptId);
window.removeReceipt = (receiptId) => receiptsModule.removeReceipt(receiptId);
window.toggleReceiptActions = (receiptId) => receiptsModule.toggleReceiptActions(receiptId);