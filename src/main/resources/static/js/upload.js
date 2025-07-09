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
    isInitialized: false
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
    const resizeControls = document.querySelector('.resize-controls');
    const imageWidth = document.getElementById('imageWidth');
    const imageHeight = document.getElementById('imageHeight');
    const imageQuality = document.getElementById('imageQuality');
    const undoChanges = document.getElementById('undoChanges');
    const resetCropper = document.getElementById('resetCropper');

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

    // Handle width input change
    if (imageWidth) {
        imageWidth.addEventListener('input', function() {
            updateCropperDimensions('width', this.value);
        });
    }

    // Handle height input change
    if (imageHeight) {
        imageHeight.addEventListener('input', function() {
            updateCropperDimensions('height', this.value);
        });
    }

    // Handle undo changes button
    if (undoChanges) {
        undoChanges.addEventListener('click', function() {
            undoImageChanges();
        });
    }

    // Handle reset cropper button
    if (resetCropper) {
        resetCropper.addEventListener('click', function() {
            resetCropperState();
        });
    }

    // Handle upload confirmation
    if (confirmUpload) {
        confirmUpload.addEventListener('click', function() {
            processAndUploadImage();
        });
    }

    // Handle modal close
    if (uploadModal) {
        uploadModal.addEventListener('hidden.bs.modal', function() {
            resetModalState();
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
    const cropperImage = document.getElementById('cropperImage');
    const fileDropZone = document.getElementById('fileDropZone');
    const resizeControls = document.querySelector('.resize-controls');
    const confirmUpload = document.getElementById('confirmUpload');

    if (!cropperImage || !fileDropZone || !resizeControls || !confirmUpload) {
        console.error('Modal elements not found');
        return;
    }

    // Update modal UI
    cropperImage.src = imageSrc;
    cropperImage.style.display = 'block';
    fileDropZone.style.display = 'none';
    resizeControls.style.display = 'block';
    confirmUpload.disabled = false;

    // Initialize Cropper.js
    initializeCropper(cropperImage);
}

/**
 * Initialize Cropper.js for image editing.
 * 
 * @param {HTMLImageElement} imageElement - The image element to crop
 */
function initializeCropper(imageElement) {
    // Destroy existing cropper if present
    if (uploadState.cropper) {
        uploadState.cropper.destroy();
    }

    // Create new Cropper instance
    uploadState.cropper = new Cropper(imageElement, {
        aspectRatio: NaN, // Allow free aspect ratio
        viewMode: 1,
        autoCropArea: 1,
        responsive: true,
        restore: false,
        guides: true,
        center: true,
        highlight: false,
        cropBoxMovable: true,
        cropBoxResizable: true,
        toggleDragModeOnDblclick: false,
        ready: function() {
            updateDimensionInputs();
        },
        crop: function(event) {
            updateDimensionInputs();
        }
    });
}

/**
 * Update dimension input fields based on cropper state.
 */
function updateDimensionInputs() {
    if (!uploadState.cropper) {
        return;
    }

    const imageWidth = document.getElementById('imageWidth');
    const imageHeight = document.getElementById('imageHeight');
    
    if (imageWidth && imageHeight) {
        const cropBoxData = uploadState.cropper.getCropBoxData();
        imageWidth.value = Math.round(cropBoxData.width);
        imageHeight.value = Math.round(cropBoxData.height);
    }
}

/**
 * Update cropper dimensions based on input values.
 * 
 * @param {string} dimension - Either 'width' or 'height'
 * @param {string} value - The new dimension value
 */
function updateCropperDimensions(dimension, value) {
    if (!uploadState.cropper || !value) {
        return;
    }

    const dimensionValue = parseInt(value);
    if (isNaN(dimensionValue) || dimensionValue <= 0) {
        return;
    }

    const cropBoxData = uploadState.cropper.getCropBoxData();
    const newCropBoxData = { ...cropBoxData };
    
    if (dimension === 'width') {
        newCropBoxData.width = dimensionValue;
    } else if (dimension === 'height') {
        newCropBoxData.height = dimensionValue;
    }
    
    uploadState.cropper.setCropBoxData(newCropBoxData);
}

/**
 * Undo image changes by restoring original image.
 */
function undoImageChanges() {
    if (uploadState.originalImageData) {
        showUploadModal(uploadState.originalImageData);
    }
}

/**
 * Reset cropper to initial state.
 */
function resetCropperState() {
    if (uploadState.cropper) {
        uploadState.cropper.reset();
        updateDimensionInputs();
    }
}

/**
 * Process and upload the cropped image.
 */
function processAndUploadImage() {
    if (!uploadState.cropper || !uploadState.selectedFile) {
        showErrorMessage('No image selected for upload');
        return;
    }

    // Get quality setting
    const imageQuality = document.getElementById('imageQuality');
    const quality = imageQuality ? parseFloat(imageQuality.value) : 0.8;
    
    // Get cropped canvas
    const canvas = uploadState.cropper.getCroppedCanvas();
    
    // Convert canvas to blob
    canvas.toBlob(function(blob) {
        if (blob) {
            uploadFile(blob);
        } else {
            showErrorMessage('Failed to process image. Please try again.');
        }
    }, uploadState.selectedFile.type, quality);
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
        const uploadModal = document.getElementById('uploadModal');
        if (uploadModal) {
            const modal = bootstrap.Modal.getInstance(uploadModal);
            if (modal) {
                modal.hide();
            }
        }
        
        // Reset modal state
        resetModalState();
        
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
        showErrorMessage(errorMessage);
    }
}

/**
 * Reset modal state to initial configuration.
 */
function resetModalState() {
    // Destroy cropper
    if (uploadState.cropper) {
        uploadState.cropper.destroy();
        uploadState.cropper = null;
    }
    
    // Reset UI elements
    const cropperImage = document.getElementById('cropperImage');
    const fileDropZone = document.getElementById('fileDropZone');
    const resizeControls = document.querySelector('.resize-controls');
    const confirmUpload = document.getElementById('confirmUpload');
    const fileInput = document.getElementById('fileInput');
    const imageWidth = document.getElementById('imageWidth');
    const imageHeight = document.getElementById('imageHeight');
    const imageQuality = document.getElementById('imageQuality');
    
    if (cropperImage) {
        cropperImage.style.display = 'none';
        cropperImage.src = '';
    }
    
    if (fileDropZone) {
        fileDropZone.style.display = 'block';
        fileDropZone.classList.remove('border-primary');
    }
    
    if (resizeControls) {
        resizeControls.style.display = 'none';
    }
    
    if (confirmUpload) {
        confirmUpload.disabled = true;
        confirmUpload.innerHTML = 'Upload';
    }
    
    if (fileInput) {
        fileInput.value = '';
    }
    
    if (imageWidth) {
        imageWidth.value = '';
    }
    
    if (imageHeight) {
        imageHeight.value = '';
    }
    
    if (imageQuality) {
        imageQuality.value = '0.8';
    }
    
    // Reset state
    uploadState.selectedFile = null;
    uploadState.originalImageData = null;
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
 * 
 * @param {string} message - The error message to display
 */
function showErrorMessage(message) {
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

// Initialize upload functionality when DOM is ready
document.addEventListener('DOMContentLoaded', function() {
    initializeUpload();
});