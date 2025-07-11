/**
 * Bills functionality module for the dashboard
 * Handles bills data loading, table rendering, and event listeners
 */

class BillsModule {
    constructor() {
        // Bills tab state
        this.currentPage = 0;
        this.currentSort = 'billDate';
        this.currentDirection = 'DESC';
        this.currentSearch = '';
    }

    /**
     * Initialize the bills module
     * Sets up event listeners and loads initial data when tab is shown
     */
    init() {
        this.setupTabEventListener();
    }

    /**
     * Set up tab change event listener for bills tab
     */
    setupTabEventListener() {
        const billsTab = document.querySelector('a[href="#bills"]');
        if (billsTab) {
            billsTab.addEventListener('shown.bs.tab', () => {
                this.loadBillsData();
            });
        }
    }

    /**
     * Load bills data from the API
     * @param {number} page - Page number (0-based)
     * @param {string} search - Search term
     */
    loadBillsData(page = 0, search = '') {
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

        fetch(`/api/bills?${params.toString()}`)
            .then(response => {
                if (!response.ok) {
                    throw new Error('Failed to load bills data');
                }
                return response.json();
            })
            .then(data => {
                this.renderBillsTable(data);
                document.getElementById('bills-content').innerHTML = this.getRenderedTable(data);
                this.setupTableEventListeners();
            })
            .catch(error => {
                console.error('Error loading bills data:', error);
                document.getElementById('bills-content').innerHTML = `
                    <div class="alert alert-danger" role="alert">
                        <i class="fas fa-exclamation-triangle me-2"></i>
                        Failed to load bills data. Please try again.
                        <button class="btn btn-sm btn-outline-danger ms-2" onclick="billsModule.loadBillsData()">
                            <i class="fas fa-redo me-1"></i> Retry
                        </button>
                    </div>
                `;
            });
    }

    /**
     * Set up event listeners for bills table interactions
     */
    setupTableEventListeners() {
        // Search functionality
        const searchInput = document.getElementById('bills-search');
        if (searchInput) {
            searchInput.addEventListener('input', (e) => {
                const searchTerm = e.target.value;
                clearTimeout(searchInput.timeout);
                searchInput.timeout = setTimeout(() => {
                    this.loadBillsData(0, searchTerm);
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
                    this.loadBillsData(page - 1, this.currentSearch); // Convert to 0-based
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
                    this.loadBillsData(0, this.currentSearch);
                }
            });

            // Add cursor pointer style
            header.style.cursor = 'pointer';
        });
    }

    /**
     * Render bills table (placeholder for any bills-specific rendering logic)
     * @param {Object} data - The table data from API
     */
    renderBillsTable(data) {
        console.log('Rendering bills table with data:', data);
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
                            <i class="fas fa-file-invoice-dollar empty-state-icon"></i>
                            <div class="empty-state-title">No bills available</div>
                            <p class="empty-state-text">There are no bills to display at the moment.</p>
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

                    // Special formatting for actions column in bills table
                    if (data.tableId === 'bills' && column.key === 'actions') {
                        formattedValue = this.formatBillsActions(cellValue, row.id);
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
     * Format action buttons for bills table
     * @param {string} actionsText - The actions text to format
     * @param {string} billId - The bill ID
     * @returns {string} HTML string for action buttons
     */
    formatBillsActions(actionsText, billId) {
        if (actionsText === 'Edit | Remove') {
            return `
                <div class="action-buttons">
                    <button class="action-btn action-btn-primary" onclick="billsModule.editBill('${billId}')" title="Edit bill">
                        Edit
                    </button>
                    <button class="action-btn action-btn-danger" onclick="billsModule.removeBill('${billId}')" title="Remove bill">
                        Remove
                    </button>
                    <button class="dropdown-toggle-btn" onclick="billsModule.toggleActions('${billId}')" title="More actions">
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
     * Edit a bill
     * @param {string} billId - The bill ID to edit
     */
    editBill(billId) {
        // TODO: Implement edit bill functionality
        console.log(`Editing bill ${billId}`);
        alert(`Edit bill ${billId}. (Functionality not yet implemented)`);
    }

    /**
     * Remove a bill
     * @param {string} billId - The bill ID to remove
     */
    removeBill(billId) {
        if (confirm('Are you sure you want to remove this bill?')) {
            // TODO: Implement remove bill functionality
            console.log(`Removing bill ${billId}`);
            alert(`Bill ${billId} removed. (Functionality not yet implemented)`);
        }
    }

    /**
     * Toggle dropdown actions for a bill
     * @param {string} billId - The bill ID
     */
    toggleActions(billId) {
        console.log(`Toggle actions for bill ${billId}`);
        // TODO: Implement dropdown toggle functionality
    }
}

// Create global bills module instance
const billsModule = new BillsModule();

// Make functions globally available for backwards compatibility
window.loadBillsData = (page, search) => billsModule.loadBillsData(page, search);
window.editBill = (billId) => billsModule.editBill(billId);
window.removeBill = (billId) => billsModule.removeBill(billId);
window.toggleActions = (billId) => billsModule.toggleActions(billId);