/**
 * Inbox functionality module for the dashboard
 * Handles inbox data loading, table rendering, drag-and-drop, and event listeners
 */

class InboxModule {
    constructor() {
        // Inbox tab state
        this.currentPage = 0;
        this.currentSort = 'uploadDate';
        this.currentDirection = 'DESC';
        this.currentSearch = '';
        this.dragCounter = 0;
    }

    /**
     * Initialize the inbox module
     * Sets up event listeners and loads initial data
     */
    init() {
        this.loadInboxData();
        this.setupTabEventListener();
        this.setupInboxDragAndDrop();
    }

    /**
     * Set up tab change event listener for inbox tab
     */
    setupTabEventListener() {
        const inboxTab = document.querySelector('a[href="#inbox"]');
        if (inboxTab) {
            inboxTab.addEventListener('shown.bs.tab', () => {
                this.loadInboxData();
            });
        }
    }

    /**
     * Load inbox data from the API
     * @param {number} page - Page number (0-based)
     * @param {string} search - Search term
     */
    loadInboxData(page = 0, search = '') {
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

        fetch(`/api/inbox?${params.toString()}`)
            .then(response => {
                if (!response.ok) {
                    throw new Error('Failed to load inbox data');
                }
                return response.json();
            })
            .then(data => {
                this.renderInboxTable(data);
                document.getElementById('inbox-content').innerHTML = this.getRenderedTable(data);
                this.setupTableEventListeners();
            })
            .catch(error => {
                console.error('Error loading inbox data:', error);
                document.getElementById('inbox-content').innerHTML = `
                    <div class="alert alert-danger" role="alert">
                        <i class="fas fa-exclamation-triangle me-2"></i>
                        Failed to load inbox data. Please try again.
                        <button class="btn btn-sm btn-outline-danger ms-2" onclick="inboxModule.loadInboxData()">
                            <i class="fas fa-redo me-1"></i> Retry
                        </button>
                    </div>
                `;
            });
    }

    /**
     * Set up drag-and-drop functionality for inbox table
     */
    setupInboxDragAndDrop() {
        const inboxTableContainer = document.getElementById('inboxTableContainer');
        const dropOverlay = document.getElementById('dropOverlay');

        if (!inboxTableContainer || !dropOverlay) {
            console.error('Inbox drag-and-drop elements not found');
            return;
        }

        // Prevent default drag behaviors
        ['dragenter', 'dragover', 'dragleave', 'drop'].forEach(eventName => {
            inboxTableContainer.addEventListener(eventName, this.preventDefaults, false);
            document.body.addEventListener(eventName, this.preventDefaults, false);
        });

        // Handle drag enter
        inboxTableContainer.addEventListener('dragenter', (e) => {
            this.dragCounter++;
            this.handleDragEnter(e);
        });

        // Handle drag over
        inboxTableContainer.addEventListener('dragover', (e) => {
            this.handleDragOver(e);
        });

        // Handle drag leave
        inboxTableContainer.addEventListener('dragleave', (e) => {
            this.dragCounter--;
            if (this.dragCounter === 0) {
                this.handleDragLeave(e);
            }
        });

        // Handle drop
        inboxTableContainer.addEventListener('drop', (e) => {
            this.dragCounter = 0;
            this.handleDrop(e);
        });
    }

    /**
     * Prevent default drag behaviors
     * @param {Event} e - The event object
     */
    preventDefaults(e) {
        e.preventDefault();
        e.stopPropagation();
    }

    /**
     * Handle drag enter events
     * @param {Event} e - The event object
     */
    handleDragEnter(e) {
        const inboxTableContainer = document.getElementById('inboxTableContainer');
        const dropOverlay = document.getElementById('dropOverlay');

        if (inboxTableContainer && dropOverlay) {
            inboxTableContainer.classList.add('drag-over');
            dropOverlay.style.display = 'flex';
        }
    }

    /**
     * Handle drag over events
     * @param {Event} e - The event object
     */
    handleDragOver(e) {
        e.dataTransfer.dropEffect = 'copy';
    }

    /**
     * Handle drag leave events
     * @param {Event} e - The event object
     */
    handleDragLeave(e) {
        const inboxTableContainer = document.getElementById('inboxTableContainer');
        const dropOverlay = document.getElementById('dropOverlay');

        if (inboxTableContainer && dropOverlay) {
            inboxTableContainer.classList.remove('drag-over');
            dropOverlay.style.display = 'none';
        }
    }

    /**
     * Handle drop events
     * @param {Event} e - The event object
     */
    handleDrop(e) {
        const inboxTableContainer = document.getElementById('inboxTableContainer');
        const dropOverlay = document.getElementById('dropOverlay');

        // Remove visual feedback
        if (inboxTableContainer && dropOverlay) {
            inboxTableContainer.classList.remove('drag-over');
            dropOverlay.style.display = 'none';
        }

        // Get dropped files
        const files = e.dataTransfer.files;

        if (files.length > 0) {
            // Find the first valid image file
            let firstImageFile = null;
            for (let i = 0; i < files.length; i++) {
                if (files[i].type.startsWith('image/')) {
                    firstImageFile = files[i];
                    break;
                }
            }

            if (firstImageFile) {
                // Try using existing upload.js functionality first
                if (typeof handleFileSelect === 'function') {
                    handleFileSelect(firstImageFile);
                } else {
                    console.warn('handleFileSelect function not found, using fallback');
                    this.triggerUploadModalWithFile(firstImageFile);
                }
            } else {
                this.showDashboardErrorMessage('Please drop a valid image file');
            }
        }
    }

    /**
     * Set up event listeners for table interactions
     */
    setupTableEventListeners() {
        // Search functionality
        const searchInput = document.getElementById('inbox-search');
        if (searchInput) {
            searchInput.addEventListener('input', (e) => {
                const searchTerm = e.target.value;
                clearTimeout(searchInput.timeout);
                searchInput.timeout = setTimeout(() => {
                    this.loadInboxData(0, searchTerm);
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
                    this.loadInboxData(page - 1, this.currentSearch); // Convert to 0-based
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
                    this.loadInboxData(0, this.currentSearch);
                }
            });

            // Add cursor pointer style
            header.style.cursor = 'pointer';
        });
    }

    /**
     * Render inbox table (placeholder for any inbox-specific rendering logic)
     * @param {Object} data - The table data from API
     */
    renderInboxTable(data) {
        console.log('Rendering inbox table with data:', data);
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
     * Show error message specific to dashboard
     * @param {string} message - The error message to display
     */
    showDashboardErrorMessage(message) {
        const alertHtml = `
            <div class="alert alert-danger alert-dismissible fade show" role="alert">
                <i class="fas fa-exclamation-triangle me-2"></i>
                ${message}
                <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
            </div>
        `;

        const alertContainer = document.createElement('div');
        alertContainer.innerHTML = alertHtml;

        const dashboardLayout = document.querySelector('.dashboard-layout');
        if (dashboardLayout) {
            dashboardLayout.insertBefore(alertContainer.firstElementChild, dashboardLayout.firstElementChild);
        }

        // Auto-dismiss after 5 seconds
        setTimeout(() => {
            const alert = document.querySelector('.alert-danger');
            if (alert) {
                alert.remove();
            }
        }, 5000);
    }

    /**
     * Trigger upload modal with file as fallback
     * @param {File} file - The file to upload
     */
    triggerUploadModalWithFile(file) {
        // Use the upload.js handleFileSelect function directly
        if (typeof handleFileSelect === 'function') {
            handleFileSelect(file);
        } else {
            // Fallback: Store the file and show modal manually
            window.dragDroppedFile = file;

            // Show the upload modal
            const uploadModal = document.getElementById('uploadModal');
            if (uploadModal) {
                const modal = new bootstrap.Modal(uploadModal);
                modal.show();

                // Try to simulate file selection after modal is shown
                setTimeout(() => {
                    const fileInput = document.getElementById('fileInput');
                    if (fileInput) {
                        // Create a new FileList with our file
                        const dataTransfer = new DataTransfer();
                        dataTransfer.items.add(file);
                        fileInput.files = dataTransfer.files;

                        // Trigger the file change event
                        const changeEvent = new Event('change', { bubbles: true });
                        fileInput.dispatchEvent(changeEvent);
                    }
                }, 100);
            } else {
                this.showDashboardErrorMessage('Upload modal not found');
            }
        }
    }

    /**
     * Approve an item with specified entity type
     * @param {string} itemId - The item ID to approve
     * @param {string} entityType - The entity type for approval
     */
    approveItem(itemId, entityType) {
        if (confirm(`Approve this item as a ${entityType.toLowerCase()}?`)) {
            // TODO: Implement approve functionality
            console.log(`Approving item ${itemId} as ${entityType}`);
            alert(`Item ${itemId} approved as ${entityType}. (Functionality not yet implemented)`);
        }
    }

    /**
     * Retry OCR processing for an item
     * @param {string} itemId - The item ID to retry
     */
    retryItem(itemId) {
        if (confirm('Retry OCR processing for this item?')) {
            // TODO: Implement retry functionality
            console.log(`Retrying OCR for item ${itemId}`);
            alert(`Retrying OCR for item ${itemId}. (Functionality not yet implemented)`);
        }
    }

    /**
     * Show image modal for an item
     * @param {string} imagePath - The path to the image
     */
    showImageModal(imagePath) {
        // TODO: Implement image modal
        console.log(`Showing image modal for ${imagePath}`);
        alert(`Image viewer for ${imagePath}. (Functionality not yet implemented)`);
    }
}

// Create global inbox module instance
const inboxModule = new InboxModule();

// Make functions globally available for backwards compatibility
window.loadInboxData = (page, search) => inboxModule.loadInboxData(page, search);
window.approveItem = (itemId, entityType) => inboxModule.approveItem(itemId, entityType);
window.retryItem = (itemId) => inboxModule.retryItem(itemId);
window.showImageModal = (imagePath) => inboxModule.showImageModal(imagePath);