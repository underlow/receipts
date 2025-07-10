/**
 * Upload JavaScript Module
 *
 * Handles file selection, drag-and-drop, and upload logic with comprehensive JavaScript functionality.
 * This module provides a reusable interface for handling image uploads with cropping capabilities.
 */

// Upload module state
let uploadState = {
    cropper: null,
    originalImageData: null,
    selectedFile: null,
    isInitialized: false,
    currentMode: null, // 'crop' or 'rotate'
    rotationAngle: 0,
    cropBackupData: null,
    rotateBackupData: null
};

/**
 * Initialize upload functionality.
 * Sets up event listeners and modal configuration.
 */
function initializeUpload() {
    if (uploadState.isInitialized) {
        return;
    }

    const uploadModal = document.getElementById('uploadModal');
    const fileInput = document.getElementById('fileInput');
    const selectFileBtn = document.getElementById('selectFileBtn');
    const cropperImage = document.getElementById('cropperImage');
    const confirmUpload = document.getElementById('confirmUpload');
    const cancelUpload = document.getElementById('cancelUpload');
    const fileDropZone = document.getElementById('fileDropZone');
    const imagePreview = document.getElementById('imagePreview');
    const imageControls = document.getElementById('imageControls');
    const cropButton = document.getElementById('cropButton');
    const rotateButton = document.getElementById('rotateButton');
    const cropControls = document.getElementById('cropControls');
    const acceptCrop = document.getElementById('acceptCrop');
    const cancelCrop = document.getElementById('cancelCrop');
    const rotateControls = document.getElementById('rotateControls');
    const acceptRotate = document.getElementById('acceptRotate');
    const cancelRotate = document.getElementById('cancelRotate');
    const modalCloseBtn = uploadModal ? uploadModal.querySelector('.btn-close') : null;

    if (!uploadModal || !fileInput || !selectFileBtn) {
        console.error('Upload modal elements not found');
        return;
    }

    // Handle file selection button click
    selectFileBtn.addEventListener('click', function() {
        fileInput.click();
    });

    // Handle file input change
    fileInput.addEventListener('change', function(event) {
        const file = event.target.files[0];
        if (file) {
            handleFileSelect(file);
        }
    });

    // Handle drag and drop events
    if (fileDropZone) {
        fileDropZone.addEventListener('dragover', function(event) {
            handleDragDrop(event);
        });

        fileDropZone.addEventListener('dragleave', function(event) {
            handleDragDrop(event);
        });

        fileDropZone.addEventListener('drop', function(event) {
            handleDragDrop(event);
        });
    }

    // Handle crop button
    if (cropButton) {
        cropButton.addEventListener('click', function(event) {
            event.preventDefault();
            event.stopPropagation();
            enterCropMode();
        });
    }

    // Handle rotate button
    if (rotateButton) {
        rotateButton.addEventListener('click', function(event) {
            event.preventDefault();
            event.stopPropagation();
            enterRotateMode();
        });
    }

    // Handle crop accept button
    if (acceptCrop) {
        acceptCrop.addEventListener('click', function(event) {
            event.preventDefault();
            event.stopPropagation();
            acceptCropChanges();
        });
    }

    // Handle crop cancel button
    if (cancelCrop) {
        cancelCrop.addEventListener('click', function(event) {
            event.preventDefault();
            event.stopPropagation();
            cancelCropChanges();
        });
    }

    // Handle rotate accept button
    if (acceptRotate) {
        acceptRotate.addEventListener('click', function(event) {
            event.preventDefault();
            event.stopPropagation();
            acceptRotateChanges();
        });
    }

    // Handle rotate cancel button
    if (cancelRotate) {
        cancelRotate.addEventListener('click', function(event) {
            event.preventDefault();
            event.stopPropagation();
            cancelRotateChanges();
        });
    }

    // Handle image hover for showing controls
    if (imagePreview) {
        imagePreview.addEventListener('mouseenter', function() {
            showImageControls();
        });
        imagePreview.addEventListener('mouseleave', function() {
            hideImageControls();
        });
    }

    // Handle upload confirmation
    if (confirmUpload) {
        confirmUpload.addEventListener('click', function() {
            processAndUploadImage();
        });
    }

    // Handle cancel upload button
    if (cancelUpload) {
        cancelUpload.addEventListener('click', function(event) {
            // Prevent default Bootstrap behavior
            event.preventDefault();
            event.stopPropagation();
            
            // Force cleanup and close
            forceCloseModal();
        });
    }

    // Handle modal close button (X)
    if (modalCloseBtn) {
        modalCloseBtn.addEventListener('click', function(event) {
            // Prevent default Bootstrap behavior
            event.preventDefault();
            event.stopPropagation();
            
            // Force cleanup and close
            forceCloseModal();
        });
    }

    // Handle modal close and hide events
    if (uploadModal) {
        uploadModal.addEventListener('hide.bs.modal', function() {
            // Called when modal is about to be hidden
            cleanupModal();
        });
        
        uploadModal.addEventListener('hidden.bs.modal', function() {
            // Called after modal is fully hidden
            resetModalState();
        });

        // Handle escape key and backdrop click
        uploadModal.addEventListener('keydown', function(event) {
            if (event.key === 'Escape') {
                event.preventDefault();
                event.stopPropagation();
                forceCloseModal();
            }
        });

        // Handle backdrop click (clicking outside the modal)
        uploadModal.addEventListener('click', function(event) {
            if (event.target === uploadModal) {
                event.preventDefault();
                event.stopPropagation();
                forceCloseModal();
            }
        });
    }

    uploadState.isInitialized = true;
}

/**
 * Handle file selection from input.
 * Validates file type and size, then displays in modal.
 *
 * @param {File} file - The selected file
 */
function handleFileSelect(file) {
    if (!file) {
        return;
    }

    // Clear any previous error messages when new file is selected
    clearModalErrors();

    // Store selected file
    uploadState.selectedFile = file;

    // Validate file type
    const allowedTypes = ['image/jpeg', 'image/png', 'image/gif', 'image/webp'];
    if (!allowedTypes.includes(file.type)) {
        showErrorMessage('Please select a valid image file (JPEG, PNG, GIF, WebP)');
        return;
    }

    // Validate file size (20MB limit)
    const maxSize = 20 * 1024 * 1024;
    if (file.size > maxSize) {
        showErrorMessage('File size must be less than 20MB');
        return;
    }

    // Read file and display in modal
    const reader = new FileReader();
    reader.onload = function(e) {
        uploadState.originalImageData = e.target.result;
        showUploadModal(e.target.result);
    };
    reader.onerror = function() {
        showErrorMessage('Error reading file. Please try again.');
    };
    reader.readAsDataURL(file);
}

/**
 * Handle drag and drop events.
 * Provides visual feedback and processes dropped files.
 *
 * @param {DragEvent} event - The drag event
 */
function handleDragDrop(event) {
    event.preventDefault();

    const fileDropZone = document.getElementById('fileDropZone');
    if (!fileDropZone) {
        return;
    }

    switch (event.type) {
        case 'dragover':
            fileDropZone.classList.add('border-primary');
            break;

        case 'dragleave':
            fileDropZone.classList.remove('border-primary');
            break;

        case 'drop':
            fileDropZone.classList.remove('border-primary');

            const files = event.dataTransfer.files;
            if (files.length > 0) {
                const file = files[0];
                if (file.type.startsWith('image/')) {
                    handleFileSelect(file);
                } else {
                    showErrorMessage('Please drop a valid image file');
                }
            }
            break;
    }
}

/**
 * Show upload modal with selected file.
 * Initializes Cropper.js and displays image preview.
 *
 * @param {string} imageSrc - The image source data URL
 */
function showUploadModal(imageSrc) {
    const uploadModal = document.getElementById('uploadModal');
    const cropperImage = document.getElementById('cropperImage');
    const fileDropZone = document.getElementById('fileDropZone');
    const imageControls = document.getElementById('imageControls');
    const confirmUpload = document.getElementById('confirmUpload');

    if (!uploadModal || !cropperImage || !fileDropZone || !imageControls || !confirmUpload) {
        console.error('Modal elements not found');
        return;
    }

    // Resize image to fit within modal dimensions if needed
    resizeImageToFitModal(imageSrc, (resizedImageSrc) => {
        // Update modal UI
        cropperImage.src = resizedImageSrc;
        cropperImage.style.display = 'block';
        fileDropZone.style.display = 'none';
        imageControls.style.display = 'none'; // Initially hidden, shown on hover
        confirmUpload.disabled = false;

        // Show the modal using Bootstrap
        const modal = new bootstrap.Modal(uploadModal);
        modal.show();

        // Initialize Cropper.js after modal is shown
        uploadModal.addEventListener('shown.bs.modal', function initializeCropperOnShow() {
            initializeCropper(cropperImage);
            // Remove this event listener after first use
            uploadModal.removeEventListener('shown.bs.modal', initializeCropperOnShow);
        });
    });
}

/**
 * Resize image to fit within modal dimensions if needed.
 * 
 * @param {string} imageSrc - The image source data URL
 * @param {Function} callback - Callback function to handle resized image
 */
function resizeImageToFitModal(imageSrc, callback) {
    const img = new Image();
    img.onload = function() {
        const maxWidth = window.innerWidth * 0.6; // 60% of viewport width (reduced for cropping padding)
        const maxHeight = window.innerHeight * 0.4; // 40% of viewport height (reduced for cropping padding)
        
        if (img.width <= maxWidth && img.height <= maxHeight) {
            // Image fits, no need to resize
            callback(imageSrc);
            return;
        }
        
        // Calculate resize ratio
        const widthRatio = maxWidth / img.width;
        const heightRatio = maxHeight / img.height;
        const ratio = Math.min(widthRatio, heightRatio);
        
        // Create canvas for resizing
        const canvas = document.createElement('canvas');
        const ctx = canvas.getContext('2d');
        
        canvas.width = img.width * ratio;
        canvas.height = img.height * ratio;
        
        // Draw resized image
        ctx.drawImage(img, 0, 0, canvas.width, canvas.height);
        
        // Convert to data URL and callback
        canvas.toBlob(function(blob) {
            const reader = new FileReader();
            reader.onload = function(e) {
                callback(e.target.result);
            };
            reader.readAsDataURL(blob);
        }, 'image/jpeg', 0.8);
    };
    img.src = imageSrc;
}

/**
 * Initialize Cropper.js for image editing.
 *
 * @param {HTMLImageElement} imageElement - The image element to crop
 */
function initializeCropper(imageElement) {
    // Destroy existing cropper if present
    if (uploadState.cropper) {
        try {
            uploadState.cropper.destroy();
        } catch (e) {
            console.warn('Error destroying existing cropper:', e);
        }
        uploadState.cropper = null;
    }

    // Create new Cropper instance
    uploadState.cropper = new Cropper(imageElement, {
        aspectRatio: NaN, // Allow free aspect ratio
        viewMode: 1,
        autoCropArea: 0.8, // Reduce crop area to 80% to provide padding around image
        responsive: true,
        restore: false,
        guides: true,
        center: true,
        highlight: false,
        cropBoxMovable: true,
        cropBoxResizable: true,
        toggleDragModeOnDblclick: false,
        ready: function() {
            // Cropper is ready
            addRotationHandles();
        },
        crop: function(event) {
            // Crop event handled
        }
    });
}


/**
 * Process and upload the cropped image.
 */
function processAndUploadImage() {
    if (!uploadState.cropper || !uploadState.selectedFile) {
        showErrorMessage('No image selected for upload');
        return;
    }

    // Get cropped canvas
    const canvas = uploadState.cropper.getCroppedCanvas();

    // Convert canvas to blob with default quality
    canvas.toBlob(function(blob) {
        if (blob) {
            uploadFile(blob);
        } else {
            showErrorMessage('Failed to process image. Please try again.');
        }
    }, uploadState.selectedFile.type, 0.8);
}

/**
 * Upload processed file to server.
 *
 * @param {Blob} processedFile - The processed image blob
 */
function uploadFile(processedFile) {
    if (!processedFile) {
        showErrorMessage('No file to upload');
        return;
    }

    // Create form data
    const formData = new FormData();
    formData.append('file', processedFile, uploadState.selectedFile.name);

    // Add CSRF token to form data
    const csrfToken = document.querySelector('meta[name="_csrf"]');
    const csrfParamName = document.querySelector('meta[name="_csrf_parameter_name"]');

    if (csrfToken && csrfParamName) {
        const tokenValue = csrfToken.getAttribute('content');
        const paramName = csrfParamName.getAttribute('content');
        formData.append(paramName, tokenValue);
        console.log('Added CSRF token:', paramName, '=', tokenValue);
        console.log('Token length:', tokenValue ? tokenValue.length : 'null');
        console.log('Param name:', paramName);
    } else {
        console.warn('CSRF token or parameter name not found in meta tags');
        console.log('CSRF Token element:', csrfToken);
        console.log('CSRF Param Name element:', csrfParamName);
        console.log('All meta tags:', Array.from(document.querySelectorAll('meta')).map(m => ({name: m.name, content: m.content})));
    }

    // Show loading state
    updateUploadProgress(0);

    // Create XMLHttpRequest for progress tracking
    const xhr = new XMLHttpRequest();

    // Track upload progress
    xhr.upload.addEventListener('progress', function(e) {
        if (e.lengthComputable) {
            const percentComplete = (e.loaded / e.total) * 100;
            updateUploadProgress(percentComplete);
        }
    });

    // Handle response
    xhr.addEventListener('load', function() {
        if (xhr.status === 200) {
            try {
                const response = JSON.parse(xhr.responseText);
                handleUploadResponse(response);
            } catch (e) {
                handleUploadResponse({
                    success: false,
                    error: 'Invalid response from server'
                });
            }
        } else {
            handleUploadResponse({
                success: false,
                error: `Upload failed with status ${xhr.status}`
            });
        }
    });

    // Handle network errors
    xhr.addEventListener('error', function() {
        handleUploadResponse({
            success: false,
            error: 'Network error occurred during upload'
        });
    });

    // Handle timeout
    xhr.addEventListener('timeout', function() {
        handleUploadResponse({
            success: false,
            error: 'Upload timeout. Please try again.'
        });
    });

    // Configure and send request
    xhr.open('POST', '/api/upload');
    xhr.timeout = 30000; // 30 second timeout
    xhr.send(formData);
}

/**
 * Handle upload progress updates.
 *
 * @param {number} percent - Upload progress percentage (0-100)
 */
function updateUploadProgress(percent) {
    const confirmUpload = document.getElementById('confirmUpload');
    if (!confirmUpload) {
        return;
    }

    if (percent === 0) {
        // Show loading state
        confirmUpload.disabled = true;
        confirmUpload.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Uploading...';
    } else if (percent < 100) {
        // Update progress
        confirmUpload.innerHTML = `<i class="fas fa-spinner fa-spin"></i> Uploading... ${Math.round(percent)}%`;
    }
}

/**
 * Handle upload success/error responses.
 *
 * @param {Object} response - The server response
 */
function handleUploadResponse(response) {
    const confirmUpload = document.getElementById('confirmUpload');
    if (confirmUpload) {
        confirmUpload.disabled = false;
        confirmUpload.innerHTML = 'Upload';
    }

    if (response.success) {
        // Handle successful upload
        console.log('Upload successful:', response);

        // Close modal
        closeModal();

        // Refresh inbox data if function exists
        if (typeof loadInboxData === 'function') {
            loadInboxData();
        }

        // Show success message
        showSuccessMessage(response.message || 'Image uploaded successfully!');
    } else {
        // Handle upload error
        console.error('Upload error:', response);
        const errorMessage = response.error || response.message || 'Upload failed. Please try again.';
        
        // Show error message (will be displayed in modal if modal is open)
        showErrorMessage(errorMessage);
        
        // Modal remains open for user to correct the issue or try again
        // Do not call closeModal() on error
    }
}

/**
 * Close the upload modal properly.
 * Ensures modal instance is properly closed and backdrop is removed.
 */
function closeModal() {
    const uploadModal = document.getElementById('uploadModal');
    if (uploadModal) {
        const modal = bootstrap.Modal.getInstance(uploadModal);
        if (modal) {
            modal.hide();
        } else {
            // If no instance exists, create one and hide it
            const newModal = new bootstrap.Modal(uploadModal);
            newModal.hide();
        }
    }
}

/**
 * Force close the modal with aggressive cleanup.
 * This is used when the cancel button is clicked to ensure complete cleanup.
 */
function forceCloseModal() {
    console.log('forceCloseModal called');
    debugModalState('Before cleanup');
    
    const uploadModal = document.getElementById('uploadModal');
    
    if (uploadModal) {
        // Manually hide the modal without relying on Bootstrap
        uploadModal.style.display = 'none';
        uploadModal.classList.remove('show');
        uploadModal.setAttribute('aria-hidden', 'true');
        uploadModal.removeAttribute('aria-modal');
        uploadModal.removeAttribute('role');
        
        // Try to dispose of any Bootstrap modal instances
        const modal = bootstrap.Modal.getInstance(uploadModal);
        if (modal) {
            try {
                modal.dispose();
            } catch (e) {
                console.warn('Error disposing modal:', e);
            }
        }
    }
    
    // Immediate aggressive cleanup
    cleanupModal();
    
    // Reset modal state immediately
    resetModalState();
    
    // Additional cleanup after a short delay to catch any lingering elements
    setTimeout(() => {
        cleanupModal();
        debugModalState('After cleanup');
    }, 150);
}

/**
 * Debug function to check modal state in DOM.
 */
function debugModalState(stage) {
    console.log(`=== Modal State Debug - ${stage} ===`);
    const backdrops = document.querySelectorAll('.modal-backdrop, [class*="modal-backdrop"]');
    console.log('Found backdrops:', backdrops.length);
    backdrops.forEach((backdrop, index) => {
        console.log(`Backdrop ${index}:`, backdrop.className, backdrop.style.cssText);
    });
    
    const bodyClass = document.body.className;
    const bodyStyle = document.body.style.cssText;
    console.log('Body class:', bodyClass);
    console.log('Body style:', bodyStyle);
    
    const uploadModal = document.getElementById('uploadModal');
    if (uploadModal) {
        console.log('Modal display:', uploadModal.style.display);
        console.log('Modal classes:', uploadModal.className);
    }
}

/**
 * Clean up modal immediately when it starts to close.
 * This prevents backdrop issues by cleaning up before the modal is fully hidden.
 */
function cleanupModal() {
    // Remove any existing modal backdrops immediately
    const modalBackdrops = document.querySelectorAll('.modal-backdrop, .modal-backdrop.fade, .modal-backdrop.show');
    modalBackdrops.forEach(backdrop => {
        backdrop.remove();
    });

    // Also check for any elements with backdrop classes
    const backdrops = document.querySelectorAll('[class*="modal-backdrop"]');
    backdrops.forEach(backdrop => {
        backdrop.remove();
    });

    // Restore body classes and styles immediately
    document.body.classList.remove('modal-open');
    document.body.style.removeProperty('padding-right');
    document.body.style.removeProperty('overflow');
    document.body.style.removeProperty('overflow-x');
    document.body.style.removeProperty('overflow-y');
    
    // Remove any inline styles that might prevent scrolling
    const htmlElement = document.documentElement;
    htmlElement.style.removeProperty('padding-right');
    htmlElement.style.removeProperty('overflow');
    htmlElement.style.removeProperty('overflow-x');
    htmlElement.style.removeProperty('overflow-y');
    
    // Reset any potential fixed positioning that might interfere
    document.body.style.position = '';
    document.body.style.width = '';
    document.body.style.height = '';
}

/**
 * Reset modal state to initial configuration.
 */
function resetModalState() {
    // Clear any error messages
    clearModalErrors();

    // Destroy cropper safely
    if (uploadState.cropper) {
        try {
            uploadState.cropper.destroy();
        } catch (e) {
            console.warn('Error destroying cropper:', e);
        }
        uploadState.cropper = null;
    }

    // Reset UI elements
    const cropperImage = document.getElementById('cropperImage');
    const fileDropZone = document.getElementById('fileDropZone');
    const imageControls = document.getElementById('imageControls');
    const cropControls = document.getElementById('cropControls');
    const rotateControls = document.getElementById('rotateControls');
    const confirmUpload = document.getElementById('confirmUpload');
    const fileInput = document.getElementById('fileInput');

    if (cropperImage) {
        cropperImage.style.display = 'none';
        cropperImage.src = '';
    }

    if (fileDropZone) {
        fileDropZone.style.display = 'block';
        fileDropZone.classList.remove('border-primary');
    }

    if (imageControls) {
        imageControls.style.display = 'none';
    }

    if (cropControls) {
        cropControls.style.display = 'none';
    }

    if (rotateControls) {
        rotateControls.style.display = 'none';
    }

    if (confirmUpload) {
        confirmUpload.disabled = true;
        confirmUpload.innerHTML = 'Upload';
    }

    if (fileInput) {
        fileInput.value = '';
    }

    // Reset state
    uploadState.selectedFile = null;
    uploadState.originalImageData = null;
    uploadState.currentMode = null;
    uploadState.rotationAngle = 0;

    // Clean up any lingering modal backdrops
    const modalBackdrops = document.querySelectorAll('.modal-backdrop');
    modalBackdrops.forEach(backdrop => {
        backdrop.remove();
    });

    // Restore body scroll if needed
    document.body.classList.remove('modal-open');
    document.body.style.paddingRight = '';
    document.body.style.overflow = '';
}

/**
 * Show success message to user.
 *
 * @param {string} message - The success message to display
 */
function showSuccessMessage(message) {
    const alertHtml = `
        <div class="alert alert-success alert-dismissible fade show" role="alert">
            <i class="fas fa-check-circle me-2"></i>
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
        const alert = document.querySelector('.alert-success');
        if (alert) {
            alert.remove();
        }
    }, 5000);
}

/**
 * Show error message to user.
 * If upload modal is open, displays error in modal; otherwise displays on page.
 *
 * @param {string} message - The error message to display
 */
function showErrorMessage(message) {
    const uploadModal = document.getElementById('uploadModal');
    const isModalOpen = uploadModal && uploadModal.classList.contains('show');
    
    if (isModalOpen) {
        // Display error in modal
        showModalErrorMessage(message);
    } else {
        // Display error on page (original behavior)
        showPageErrorMessage(message);
    }
}

/**
 * Show error message in the upload modal.
 *
 * @param {string} message - The error message to display
 */
function showModalErrorMessage(message) {
    const errorContainer = document.getElementById('uploadErrorContainer');
    const errorMessageElement = document.getElementById('uploadErrorMessage');
    
    if (errorContainer && errorMessageElement) {
        // Set the error message
        errorMessageElement.textContent = message;
        
        // Show the error container
        errorContainer.style.display = 'block';
        
        // Auto-dismiss after 10 seconds (longer than page errors since modal stays open)
        setTimeout(() => {
            if (errorContainer) {
                errorContainer.style.display = 'none';
            }
        }, 10000);
    }
}

/**
 * Show error message on the page (original behavior).
 *
 * @param {string} message - The error message to display
 */
function showPageErrorMessage(message) {
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
 * Clear error messages from the modal.
 * Called when new file is selected or modal is reset.
 */
function clearModalErrors() {
    const errorContainer = document.getElementById('uploadErrorContainer');
    if (errorContainer) {
        errorContainer.style.display = 'none';
    }
}

/**
 * Show image controls on hover
 */
function showImageControls() {
    const imageControls = document.getElementById('imageControls');
    if (imageControls && uploadState.cropper && uploadState.currentMode === null) {
        imageControls.style.display = 'block';
    }
}

/**
 * Hide image controls when not hovering
 */
function hideImageControls() {
    const imageControls = document.getElementById('imageControls');
    if (imageControls && uploadState.currentMode === null) {
        imageControls.style.display = 'none';
    }
}

/**
 * Enter crop mode
 */
function enterCropMode() {
    if (!uploadState.cropper) {
        return;
    }

    uploadState.currentMode = 'crop';
    uploadState.cropBackupData = uploadState.cropper.getData();

    // Hide image controls and show crop controls
    const imageControls = document.getElementById('imageControls');
    const cropControls = document.getElementById('cropControls');

    if (imageControls) {
        imageControls.style.display = 'none';
    }

    if (cropControls) {
        cropControls.style.display = 'block';
    }

    // Show rotation handles in crop mode too
    const handles = document.querySelectorAll('.rotation-handle');
    handles.forEach(handle => {
        handle.style.display = 'block';
    });

    // Enable crop mode
    uploadState.cropper.setDragMode('crop');
}

/**
 * Add rotation handles to the image corners for manual rotation
 */
function addRotationHandles() {
    const cropperContainer = document.querySelector('.cropper-container');
    if (!cropperContainer) return;
    
    // Remove existing handles
    const existingHandles = cropperContainer.querySelectorAll('.rotation-handle');
    existingHandles.forEach(handle => handle.remove());
    
    // Create rotation handles for each corner
    const corners = ['top-left', 'top-right', 'bottom-left', 'bottom-right'];
    corners.forEach(corner => {
        const handle = document.createElement('div');
        handle.className = `rotation-handle rotation-handle-${corner}`;
        handle.style.cssText = `
            position: absolute;
            width: 20px;
            height: 20px;
            background: #007bff;
            border: 2px solid white;
            border-radius: 50%;
            cursor: pointer;
            z-index: 1000;
            display: none;
        `;
        
        // Position handles at corners
        switch (corner) {
            case 'top-left':
                handle.style.top = '-10px';
                handle.style.left = '-10px';
                break;
            case 'top-right':
                handle.style.top = '-10px';
                handle.style.right = '-10px';
                break;
            case 'bottom-left':
                handle.style.bottom = '-10px';
                handle.style.left = '-10px';
                break;
            case 'bottom-right':
                handle.style.bottom = '-10px';
                handle.style.right = '-10px';
                break;
        }
        
        // Add click event for rotation
        handle.addEventListener('click', function(e) {
            e.stopPropagation();
            // Allow rotation in both crop and rotate modes
            if (uploadState.currentMode === 'rotate' || uploadState.currentMode === 'crop') {
                uploadState.cropper.rotate(90);
                uploadState.rotationAngle = (uploadState.rotationAngle + 90) % 360;
            }
        });
        
        cropperContainer.appendChild(handle);
    });
}

/**
 * Enter rotate mode
 */
function enterRotateMode() {
    if (!uploadState.cropper) {
        return;
    }

    uploadState.currentMode = 'rotate';
    uploadState.rotateBackupData = {
        angle: uploadState.rotationAngle,
        data: uploadState.cropper.getData()
    };

    // Hide image controls and show rotate controls
    const imageControls = document.getElementById('imageControls');
    const rotateControls = document.getElementById('rotateControls');

    if (imageControls) {
        imageControls.style.display = 'none';
    }

    if (rotateControls) {
        rotateControls.style.display = 'block';
    }

    // Show rotation handles
    const handles = document.querySelectorAll('.rotation-handle');
    handles.forEach(handle => {
        handle.style.display = 'block';
    });

    // Rotate by 90 degrees on initial click
    uploadState.cropper.rotate(90);
    uploadState.rotationAngle = (uploadState.rotationAngle + 90) % 360;
}

/**
 * Start rotation handling (simplified for now)
 * @param {MouseEvent} event - Mouse down event
 */
function startRotation(event) {
    // For now, this is not used since we're doing simple 90-degree rotation
    console.log('Start rotation called (not implemented)');
}

/**
 * Accept crop changes
 */
function acceptCropChanges() {
    uploadState.currentMode = null;
    uploadState.cropBackupData = null;

    // Hide crop controls and rotation handles
    const cropControls = document.getElementById('cropControls');
    if (cropControls) {
        cropControls.style.display = 'none';
    }

    const handles = document.querySelectorAll('.rotation-handle');
    handles.forEach(handle => {
        handle.style.display = 'none';
    });

    // Reset drag mode
    uploadState.cropper.setDragMode('move');
}

/**
 * Cancel crop changes
 */
function cancelCropChanges() {
    if (uploadState.cropBackupData) {
        uploadState.cropper.setData(uploadState.cropBackupData);
    }

    uploadState.currentMode = null;
    uploadState.cropBackupData = null;

    // Hide crop controls and rotation handles
    const cropControls = document.getElementById('cropControls');
    if (cropControls) {
        cropControls.style.display = 'none';
    }

    const handles = document.querySelectorAll('.rotation-handle');
    handles.forEach(handle => {
        handle.style.display = 'none';
    });

    // Reset drag mode
    uploadState.cropper.setDragMode('move');
}

/**
 * Accept rotate changes
 */
function acceptRotateChanges() {
    uploadState.currentMode = null;
    uploadState.rotateBackupData = null;

    // Hide rotate controls and rotation handles
    const rotateControls = document.getElementById('rotateControls');
    if (rotateControls) {
        rotateControls.style.display = 'none';
    }
    
    const handles = document.querySelectorAll('.rotation-handle');
    handles.forEach(handle => {
        handle.style.display = 'none';
    });
}

/**
 * Cancel rotate changes
 */
function cancelRotateChanges() {
    if (uploadState.rotateBackupData) {
        // Reset to backup rotation angle
        const currentAngle = uploadState.rotationAngle;
        const backupAngle = uploadState.rotateBackupData.angle;
        const deltaAngle = backupAngle - currentAngle;

        if (deltaAngle !== 0) {
            uploadState.cropper.rotate(deltaAngle);
        }

        uploadState.rotationAngle = backupAngle;
        uploadState.cropper.setData(uploadState.rotateBackupData.data);
    }

    uploadState.currentMode = null;
    uploadState.rotateBackupData = null;

    // Hide rotate controls and rotation handles
    const rotateControls = document.getElementById('rotateControls');
    if (rotateControls) {
        rotateControls.style.display = 'none';
    }
    
    const handles = document.querySelectorAll('.rotation-handle');
    handles.forEach(handle => {
        handle.style.display = 'none';
    });
}

// Initialize upload functionality when DOM is ready
document.addEventListener('DOMContentLoaded', function() {
    initializeUpload();
});

// Expose handleFileSelect function globally for integration with other modules
window.handleFileSelect = handleFileSelect;

// Expose showErrorMessage and showModalErrorMessage for testing
window.showErrorMessage = showErrorMessage;
window.showModalErrorMessage = showModalErrorMessage;

// Expose upload state for testing
window.uploadState = uploadState;

// Expose debug function for testing
window.debugModalState = debugModalState;

// Test function to verify modal cleanup
window.testModalCleanup = function() {
    console.log('Testing modal cleanup...');
    forceCloseModal();
    
    setTimeout(() => {
        const backdrops = document.querySelectorAll('.modal-backdrop, [class*="modal-backdrop"]');
        const hasModalOpen = document.body.classList.contains('modal-open');
        const hasOverflow = document.body.style.overflow !== '';
        
        console.log('=== Test Results ===');
        console.log('Backdrops found:', backdrops.length);
        console.log('Body has modal-open class:', hasModalOpen);
        console.log('Body has overflow style:', hasOverflow);
        console.log('Test result:', backdrops.length === 0 && !hasModalOpen && !hasOverflow ? 'PASS' : 'FAIL');
    }, 200);
};
