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
        return tableRenderer.renderTable(data);
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