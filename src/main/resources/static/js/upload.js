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
    const uploadModal = document.getElementById('uploadModal');
    const cropperImage = document.getElementById('cropperImage');
    const fileDropZone = document.getElementById('fileDropZone');
    const imageControls = document.getElementById('imageControls');
    const confirmUpload = document.getElementById('confirmUpload');

    if (!uploadModal || !cropperImage || !fileDropZone || !imageControls || !confirmUpload) {
        console.error('Modal elements not found');
        return;
    }

    // Update modal UI
    cropperImage.src = imageSrc;
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
            // Cropper is ready
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
    
    // Enable crop mode
    uploadState.cropper.setDragMode('crop');
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
    
    // Enable rotate mode - add mouse rotation handling to the cropper container
    const cropperContainer = uploadState.cropper.getContainer();
    if (cropperContainer) {
        cropperContainer.addEventListener('mousedown', startRotation);
        cropperContainer.style.cursor = 'grab';
    }
}

/**
 * Start rotation handling
 * @param {MouseEvent} event - Mouse down event
 */
function startRotation(event) {
    if (uploadState.currentMode !== 'rotate') {
        return;
    }
    
    event.preventDefault();
    
    const cropperContainer = uploadState.cropper.getContainer();
    const rect = cropperContainer.getBoundingClientRect();
    const centerX = rect.left + rect.width / 2;
    const centerY = rect.top + rect.height / 2;
    
    let startAngle = Math.atan2(event.clientY - centerY, event.clientX - centerX);
    let currentRotation = uploadState.rotationAngle;
    
    function onMouseMove(e) {
        const currentAngle = Math.atan2(e.clientY - centerY, e.clientX - centerX);
        const deltaAngle = currentAngle - startAngle;
        const degrees = deltaAngle * (180 / Math.PI);
        
        // Apply rotation incrementally
        uploadState.cropper.rotate(degrees);
        uploadState.rotationAngle = (uploadState.rotationAngle + degrees) % 360;
        
        startAngle = currentAngle;
    }
    
    function onMouseUp() {
        document.removeEventListener('mousemove', onMouseMove);
        document.removeEventListener('mouseup', onMouseUp);
        cropperContainer.style.cursor = 'grab';
    }
    
    cropperContainer.style.cursor = 'grabbing';
    document.addEventListener('mousemove', onMouseMove);
    document.addEventListener('mouseup', onMouseUp);
}

/**
 * Accept crop changes
 */
function acceptCropChanges() {
    uploadState.currentMode = null;
    uploadState.cropBackupData = null;
    
    // Hide crop controls
    const cropControls = document.getElementById('cropControls');
    if (cropControls) {
        cropControls.style.display = 'none';
    }
    
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
    
    // Hide crop controls
    const cropControls = document.getElementById('cropControls');
    if (cropControls) {
        cropControls.style.display = 'none';
    }
    
    // Reset drag mode
    uploadState.cropper.setDragMode('move');
}

/**
 * Accept rotate changes
 */
function acceptRotateChanges() {
    uploadState.currentMode = null;
    uploadState.rotateBackupData = null;
    
    // Hide rotate controls
    const rotateControls = document.getElementById('rotateControls');
    if (rotateControls) {
        rotateControls.style.display = 'none';
    }
    
    // Remove rotation event listeners
    const cropperContainer = uploadState.cropper.getContainer();
    if (cropperContainer) {
        cropperContainer.removeEventListener('mousedown', startRotation);
        cropperContainer.style.cursor = 'default';
    }
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
    
    // Hide rotate controls
    const rotateControls = document.getElementById('rotateControls');
    if (rotateControls) {
        rotateControls.style.display = 'none';
    }
    
    // Remove rotation event listeners
    const cropperContainer = uploadState.cropper.getContainer();
    if (cropperContainer) {
        cropperContainer.removeEventListener('mousedown', startRotation);
        cropperContainer.style.cursor = 'default';
    }
}

// Initialize upload functionality when DOM is ready
document.addEventListener('DOMContentLoaded', function() {
    initializeUpload();
});