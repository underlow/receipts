/**
 * Drag and Drop Module
 * 
 * Handles drag and drop functionality for file uploads across the application.
 * Provides reusable drag-drop functionality with visual feedback and event handling.
 */

const dragDropModule = {
    // Module state
    isInitialized: false,
    activeDropZones: new Set(),
    
    /**
     * Initialize drag and drop functionality
     */
    init() {
        if (this.isInitialized) {
            return;
        }
        
        this.setupGlobalDragEvents();
        this.initializeDropZones();
        this.isInitialized = true;
    },
    
    /**
     * Setup global drag events to handle drag over entire document
     */
    setupGlobalDragEvents() {
        // Prevent default drag behaviors on document
        document.addEventListener('dragover', (e) => {
            e.preventDefault();
            this.handleGlobalDragOver(e);
        });
        
        document.addEventListener('dragleave', (e) => {
            this.handleGlobalDragLeave(e);
        });
        
        document.addEventListener('drop', (e) => {
            e.preventDefault();
            this.handleGlobalDrop(e);
        });
    },
    
    /**
     * Initialize all drop zones in the document
     */
    initializeDropZones() {
        const dropZones = document.querySelectorAll('.drop-zone, [data-drop-zone]');
        dropZones.forEach(zone => this.setupDropZone(zone));
    },
    
    /**
     * Setup a specific drop zone with drag and drop functionality
     * @param {HTMLElement} element - The drop zone element
     * @param {Object} options - Configuration options
     */
    setupDropZone(element, options = {}) {
        const config = {
            acceptedTypes: ['image/*'],
            maxFiles: 1,
            maxSize: 20 * 1024 * 1024, // 20MB
            onDrop: null,
            onDragEnter: null,
            onDragLeave: null,
            onError: null,
            ...options
        };
        
        // Store config on element
        element._dragDropConfig = config;
        
        // Setup event listeners
        element.addEventListener('dragenter', (e) => {
            e.preventDefault();
            this.handleDragEnter(e, element);
        });
        
        element.addEventListener('dragover', (e) => {
            e.preventDefault();
            this.handleDragOver(e, element);
        });
        
        element.addEventListener('dragleave', (e) => {
            this.handleDragLeave(e, element);
        });
        
        element.addEventListener('drop', (e) => {
            e.preventDefault();
            this.handleDrop(e, element);
        });
        
        // Add to active drop zones
        this.activeDropZones.add(element);
    },
    
    /**
     * Handle drag enter event
     * @param {DragEvent} event - The drag event
     * @param {HTMLElement} element - The drop zone element
     */
    handleDragEnter(event, element) {
        const config = element._dragDropConfig;
        
        // Check if dragged items are valid
        if (!this.isValidDragData(event, config)) {
            return;
        }
        
        // Add visual feedback
        this.addDragOverState(element);
        
        // Show drop overlay if it exists
        this.showDropOverlay(element);
        
        // Call custom handler
        if (config.onDragEnter) {
            config.onDragEnter(event, element);
        }
    },
    
    /**
     * Handle drag over event
     * @param {DragEvent} event - The drag event
     * @param {HTMLElement} element - The drop zone element
     */
    handleDragOver(event, element) {
        const config = element._dragDropConfig;
        
        // Check if dragged items are valid
        if (!this.isValidDragData(event, config)) {
            event.dataTransfer.dropEffect = 'none';
            return;
        }
        
        event.dataTransfer.dropEffect = 'copy';
        this.addDragOverState(element);
    },
    
    /**
     * Handle drag leave event
     * @param {DragEvent} event - The drag event
     * @param {HTMLElement} element - The drop zone element
     */
    handleDragLeave(event, element) {
        const config = element._dragDropConfig;
        
        // Only remove state if we're actually leaving the drop zone
        if (!element.contains(event.relatedTarget)) {
            this.removeDragOverState(element);
            this.hideDropOverlay(element);
            
            // Call custom handler
            if (config.onDragLeave) {
                config.onDragLeave(event, element);
            }
        }
    },
    
    /**
     * Handle drop event
     * @param {DragEvent} event - The drag event
     * @param {HTMLElement} element - The drop zone element
     */
    handleDrop(event, element) {
        const config = element._dragDropConfig;
        
        // Remove visual feedback
        this.removeDragOverState(element);
        this.hideDropOverlay(element);
        
        // Get dropped files
        const files = this.getDroppedFiles(event);
        
        // Validate files
        const validationResult = this.validateFiles(files, config);
        
        if (!validationResult.isValid) {
            if (config.onError) {
                config.onError(validationResult.error, element);
            }
            return;
        }
        
        // Call custom drop handler
        if (config.onDrop) {
            config.onDrop(files, element);
        }
    },
    
    /**
     * Handle global drag over (for document-level drag detection)
     * @param {DragEvent} event - The drag event
     */
    handleGlobalDragOver(event) {
        // Add global drag state if files are being dragged
        if (this.isDraggingFiles(event)) {
            document.body.classList.add('dragging-files');
        }
    },
    
    /**
     * Handle global drag leave
     * @param {DragEvent} event - The drag event
     */
    handleGlobalDragLeave(event) {
        // Remove global drag state if leaving document
        if (event.clientX === 0 && event.clientY === 0) {
            document.body.classList.remove('dragging-files');
        }
    },
    
    /**
     * Handle global drop
     * @param {DragEvent} event - The drag event
     */
    handleGlobalDrop(event) {
        // Remove global drag state
        document.body.classList.remove('dragging-files');
    },
    
    /**
     * Check if drag data is valid for the drop zone
     * @param {DragEvent} event - The drag event
     * @param {Object} config - Drop zone configuration
     * @returns {boolean} - Whether the drag data is valid
     */
    isValidDragData(event, config) {
        const items = event.dataTransfer.items;
        
        // Check if we have files
        if (!items || items.length === 0) {
            return false;
        }
        
        // Check file count
        if (items.length > config.maxFiles) {
            return false;
        }
        
        // Check file types
        for (let item of items) {
            if (item.kind === 'file') {
                const file = item.getAsFile();
                if (file && !this.isValidFileType(file.type, config.acceptedTypes)) {
                    return false;
                }
            }
        }
        
        return true;
    },
    
    /**
     * Check if files are being dragged
     * @param {DragEvent} event - The drag event
     * @returns {boolean} - Whether files are being dragged
     */
    isDraggingFiles(event) {
        return event.dataTransfer.types.includes('Files');
    },
    
    /**
     * Check if file type is valid
     * @param {string} fileType - The file MIME type
     * @param {string[]} acceptedTypes - Array of accepted types
     * @returns {boolean} - Whether the file type is valid
     */
    isValidFileType(fileType, acceptedTypes) {
        return acceptedTypes.some(type => {
            if (type.endsWith('/*')) {
                return fileType.startsWith(type.slice(0, -1));
            }
            return fileType === type;
        });
    },
    
    /**
     * Get dropped files from drag event
     * @param {DragEvent} event - The drag event
     * @returns {File[]} - Array of dropped files
     */
    getDroppedFiles(event) {
        const files = [];
        
        if (event.dataTransfer.files) {
            for (let file of event.dataTransfer.files) {
                files.push(file);
            }
        }
        
        return files;
    },
    
    /**
     * Validate dropped files
     * @param {File[]} files - Array of files to validate
     * @param {Object} config - Drop zone configuration
     * @returns {Object} - Validation result
     */
    validateFiles(files, config) {
        if (files.length === 0) {
            return { isValid: false, error: 'No files provided' };
        }
        
        if (files.length > config.maxFiles) {
            return { isValid: false, error: `Maximum ${config.maxFiles} file(s) allowed` };
        }
        
        for (let file of files) {
            // Check file type
            if (!this.isValidFileType(file.type, config.acceptedTypes)) {
                return { isValid: false, error: `File type ${file.type} is not supported` };
            }
            
            // Check file size
            if (file.size > config.maxSize) {
                const maxSizeMB = Math.round(config.maxSize / (1024 * 1024));
                return { isValid: false, error: `File size exceeds ${maxSizeMB}MB limit` };
            }
        }
        
        return { isValid: true };
    },
    
    /**
     * Add drag over visual state
     * @param {HTMLElement} element - The drop zone element
     */
    addDragOverState(element) {
        element.classList.add('drag-over');
    },
    
    /**
     * Remove drag over visual state
     * @param {HTMLElement} element - The drop zone element
     */
    removeDragOverState(element) {
        element.classList.remove('drag-over');
    },
    
    /**
     * Show drop overlay
     * @param {HTMLElement} element - The drop zone element
     */
    showDropOverlay(element) {
        let overlay = element.querySelector('.drop-overlay');
        
        if (!overlay) {
            overlay = this.createDropOverlay();
            element.appendChild(overlay);
        }
        
        overlay.style.display = 'flex';
    },
    
    /**
     * Hide drop overlay
     * @param {HTMLElement} element - The drop zone element
     */
    hideDropOverlay(element) {
        const overlay = element.querySelector('.drop-overlay');
        if (overlay) {
            overlay.style.display = 'none';
        }
    },
    
    /**
     * Create drop overlay element
     * @returns {HTMLElement} - The drop overlay element
     */
    createDropOverlay() {
        const overlay = document.createElement('div');
        overlay.className = 'drop-overlay';
        overlay.innerHTML = `
            <div class="drop-message">
                <i class="fas fa-cloud-upload-alt"></i>
                <p>Drop files here to upload</p>
            </div>
        `;
        return overlay;
    },
    
    /**
     * Remove drop zone functionality
     * @param {HTMLElement} element - The drop zone element
     */
    removeDropZone(element) {
        this.activeDropZones.delete(element);
        element._dragDropConfig = null;
        
        // Remove overlay
        const overlay = element.querySelector('.drop-overlay');
        if (overlay) {
            overlay.remove();
        }
        
        // Remove visual state
        this.removeDragOverState(element);
    },
    
    /**
     * Clean up all drop zones
     */
    cleanup() {
        this.activeDropZones.forEach(element => {
            this.removeDropZone(element);
        });
        this.activeDropZones.clear();
        this.isInitialized = false;
    }
};

// Auto-initialize when DOM is ready
document.addEventListener('DOMContentLoaded', () => {
    dragDropModule.init();
});

// Export for use in other modules
window.dragDropModule = dragDropModule;