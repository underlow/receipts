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
        return tableRenderer.renderTable(data);
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