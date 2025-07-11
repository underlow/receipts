/**
 * Avatar Upload JavaScript Module
 *
 * Handles avatar file selection, drag-and-drop, cropping, and upload specifically for service providers.
 * Provides 200x200 pixel resize functionality and real-time preview updates.
 */

// Avatar upload module state
let avatarUploadState = {
    cropper: null,
    originalImageData: null,
    selectedFile: null,
    isInitialized: false,
    currentMode: null, // 'crop' or 'rotate'
    rotationAngle: 0,
    cropBackupData: null,
    rotateBackupData: null,
    serviceProviderId: null,
    onUploadSuccess: null // Callback function for successful upload
};

/**
 * Initialize avatar upload functionality.
 * Sets up event listeners and modal configuration.
 */
function initializeAvatarUpload() {
    if (avatarUploadState.isInitialized) {
        return;
    }

    const avatarUploadModal = document.getElementById('avatarUploadModal');
    const avatarFileInput = document.getElementById('avatarFileInput');
    const avatarSelectFileBtn = document.getElementById('avatarSelectFileBtn');
    const avatarCropperImage = document.getElementById('avatarCropperImage');
    const avatarConfirmUpload = document.getElementById('avatarConfirmUpload');
    const avatarCancelUpload = document.getElementById('avatarCancelUpload');
    const avatarFileDropZone = document.getElementById('avatarFileDropZone');
    const avatarImagePreview = document.getElementById('avatarImagePreview');
    const avatarImageControls = document.getElementById('avatarImageControls');
    const avatarCropButton = document.getElementById('avatarCropButton');
    const avatarRotateButton = document.getElementById('avatarRotateButton');
    const avatarCropControls = document.getElementById('avatarCropControls');
    const avatarAcceptCrop = document.getElementById('avatarAcceptCrop');
    const avatarCancelCrop = document.getElementById('avatarCancelCrop');
    const avatarRotateControls = document.getElementById('avatarRotateControls');
    const avatarAcceptRotate = document.getElementById('avatarAcceptRotate');
    const avatarCancelRotate = document.getElementById('avatarCancelRotate');
    const modalCloseBtn = avatarUploadModal ? avatarUploadModal.querySelector('.btn-close') : null;

    if (!avatarUploadModal || !avatarFileInput || !avatarSelectFileBtn) {
        console.error('Avatar upload modal elements not found');
        return;
    }

    // Handle file selection button click
    avatarSelectFileBtn.addEventListener('click', function() {
        avatarFileInput.click();
    });

    // Handle file input change
    avatarFileInput.addEventListener('change', function(event) {
        const file = event.target.files[0];
        if (file) {
            handleAvatarFileSelect(file);
        }
    });

    // Handle drag and drop events
    if (avatarFileDropZone) {
        avatarFileDropZone.addEventListener('dragover', function(event) {
            handleAvatarDragDrop(event);
        });

        avatarFileDropZone.addEventListener('dragleave', function(event) {
            handleAvatarDragDrop(event);
        });

        avatarFileDropZone.addEventListener('drop', function(event) {
            handleAvatarDragDrop(event);
        });
    }

    // Handle crop button
    if (avatarCropButton) {
        avatarCropButton.addEventListener('click', function(event) {
            event.preventDefault();
            event.stopPropagation();
            enterAvatarCropMode();
        });
    }

    // Handle rotate button
    if (avatarRotateButton) {
        avatarRotateButton.addEventListener('click', function(event) {
            event.preventDefault();
            event.stopPropagation();
            enterAvatarRotateMode();
        });
    }

    // Handle crop accept button
    if (avatarAcceptCrop) {
        avatarAcceptCrop.addEventListener('click', function(event) {
            event.preventDefault();
            event.stopPropagation();
            acceptAvatarCropChanges();
        });
    }

    // Handle crop cancel button
    if (avatarCancelCrop) {
        avatarCancelCrop.addEventListener('click', function(event) {
            event.preventDefault();
            event.stopPropagation();
            cancelAvatarCropChanges();
        });
    }

    // Handle rotate accept button
    if (avatarAcceptRotate) {
        avatarAcceptRotate.addEventListener('click', function(event) {
            event.preventDefault();
            event.stopPropagation();
            acceptAvatarRotateChanges();
        });
    }

    // Handle rotate cancel button
    if (avatarCancelRotate) {
        avatarCancelRotate.addEventListener('click', function(event) {
            event.preventDefault();
            event.stopPropagation();
            cancelAvatarRotateChanges();
        });
    }

    // Handle image hover for showing controls
    if (avatarImagePreview) {
        avatarImagePreview.addEventListener('mouseenter', function() {
            showAvatarImageControls();
        });
        avatarImagePreview.addEventListener('mouseleave', function() {
            hideAvatarImageControls();
        });
    }

    // Handle upload confirmation
    if (avatarConfirmUpload) {
        avatarConfirmUpload.addEventListener('click', function() {
            processAndUploadAvatar();
        });
    }

    // Handle cancel upload button
    if (avatarCancelUpload) {
        avatarCancelUpload.addEventListener('click', function(event) {
            event.preventDefault();
            event.stopPropagation();
            forceCloseAvatarModal();
        });
    }

    // Handle modal close button (X)
    if (modalCloseBtn) {
        modalCloseBtn.addEventListener('click', function(event) {
            event.preventDefault();
            event.stopPropagation();
            forceCloseAvatarModal();
        });
    }

    // Handle modal close and hide events
    if (avatarUploadModal) {
        avatarUploadModal.addEventListener('hide.bs.modal', function() {
            cleanupAvatarModal();
        });
        
        avatarUploadModal.addEventListener('hidden.bs.modal', function() {
            resetAvatarModalState();
        });

        // Handle escape key and backdrop click
        avatarUploadModal.addEventListener('keydown', function(event) {
            if (event.key === 'Escape') {
                event.preventDefault();
                event.stopPropagation();
                forceCloseAvatarModal();
            }
        });

        avatarUploadModal.addEventListener('click', function(event) {
            if (event.target === avatarUploadModal) {
                event.preventDefault();
                event.stopPropagation();
                forceCloseAvatarModal();
            }
        });
    }

    avatarUploadState.isInitialized = true;
}

/**
 * Open avatar upload modal for a specific service provider.
 * @param {number} serviceProviderId - The ID of the service provider
 * @param {function} onSuccess - Callback function called on successful upload
 */
function openAvatarUploadModal(serviceProviderId, onSuccess) {
    if (!serviceProviderId) {
        console.error('Service provider ID is required');
        return;
    }

    avatarUploadState.serviceProviderId = serviceProviderId;
    avatarUploadState.onUploadSuccess = onSuccess;

    const avatarUploadModal = document.getElementById('avatarUploadModal');
    if (avatarUploadModal) {
        const modal = new bootstrap.Modal(avatarUploadModal);
        modal.show();
    }
}

/**
 * Handle avatar file selection from input.
 * Validates file type and size, then displays in modal.
 */
function handleAvatarFileSelect(file) {
    if (!file) {
        return;
    }

    // Clear any previous error messages
    clearAvatarModalErrors();

    // Store selected file
    avatarUploadState.selectedFile = file;

    // Validate file type
    const allowedTypes = ['image/jpeg', 'image/png', 'image/gif', 'image/webp'];
    if (!allowedTypes.includes(file.type)) {
        showAvatarErrorMessage('Please select a valid image file (JPEG, PNG, GIF, WebP)');
        return;
    }

    // Validate file size (10MB limit for avatars)
    const maxSize = 10 * 1024 * 1024;
    if (file.size > maxSize) {
        showAvatarErrorMessage('File size must be less than 10MB');
        return;
    }

    // Read file and display in modal
    const reader = new FileReader();
    reader.onload = function(e) {
        avatarUploadState.originalImageData = e.target.result;
        showAvatarInModal(e.target.result);
    };
    reader.onerror = function() {
        showAvatarErrorMessage('Error reading file. Please try again.');
    };
    reader.readAsDataURL(file);
}

/**
 * Handle drag and drop events for avatar upload.
 */
function handleAvatarDragDrop(event) {
    event.preventDefault();

    const avatarFileDropZone = document.getElementById('avatarFileDropZone');
    if (!avatarFileDropZone) {
        return;
    }

    switch (event.type) {
        case 'dragover':
            avatarFileDropZone.classList.add('border-primary');
            break;

        case 'dragleave':
            avatarFileDropZone.classList.remove('border-primary');
            break;

        case 'drop':
            avatarFileDropZone.classList.remove('border-primary');

            const files = event.dataTransfer.files;
            if (files.length > 0) {
                const file = files[0];
                if (file.type.startsWith('image/')) {
                    handleAvatarFileSelect(file);
                } else {
                    showAvatarErrorMessage('Please drop a valid image file');
                }
            }
            break;
    }
}

/**
 * Show avatar image in modal and initialize cropper.
 */
function showAvatarInModal(imageSrc) {
    const avatarCropperImage = document.getElementById('avatarCropperImage');
    const avatarFileDropZone = document.getElementById('avatarFileDropZone');
    const avatarImageControls = document.getElementById('avatarImageControls');
    const avatarConfirmUpload = document.getElementById('avatarConfirmUpload');

    if (!avatarCropperImage || !avatarFileDropZone || !avatarImageControls || !avatarConfirmUpload) {
        console.error('Avatar modal elements not found');
        return;
    }

    // Update modal UI
    avatarCropperImage.src = imageSrc;
    avatarCropperImage.style.display = 'block';
    avatarFileDropZone.style.display = 'none';
    avatarImageControls.style.display = 'none';
    avatarConfirmUpload.disabled = false;

    // Initialize cropper and update preview
    setTimeout(() => {
        initializeAvatarCropper(avatarCropperImage);
        updateAvatarPreview();
    }, 100);
}

/**
 * Initialize Cropper.js for avatar editing.
 */
function initializeAvatarCropper(imageElement) {
    // Destroy existing cropper if present
    if (avatarUploadState.cropper) {
        try {
            avatarUploadState.cropper.destroy();
        } catch (e) {
            console.warn('Error destroying existing avatar cropper:', e);
        }
        avatarUploadState.cropper = null;
    }

    // Create new Cropper instance with square aspect ratio for avatars
    avatarUploadState.cropper = new Cropper(imageElement, {
        aspectRatio: 1, // Square aspect ratio for avatars
        viewMode: 1,
        autoCropArea: 0.8,
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
            addAvatarRotationHandles();
            updateAvatarPreview();
        },
        crop: function(event) {
            // Update preview on crop
            updateAvatarPreview();
        }
    });
}

/**
 * Update the 200x200 avatar preview.
 */
function updateAvatarPreview() {
    if (!avatarUploadState.cropper) {
        return;
    }

    const avatarPreviewImage = document.getElementById('avatarPreviewImage');
    const avatarPreviewPlaceholder = document.getElementById('avatarPreviewPlaceholder');

    if (!avatarPreviewImage || !avatarPreviewPlaceholder) {
        return;
    }

    // Get cropped canvas at 200x200 size
    const canvas = avatarUploadState.cropper.getCroppedCanvas({
        width: 200,
        height: 200
    });

    if (canvas) {
        // Convert to data URL and update preview
        const previewDataUrl = canvas.toDataURL();
        avatarPreviewImage.src = previewDataUrl;
        avatarPreviewImage.style.display = 'block';
        avatarPreviewPlaceholder.style.display = 'none';
    }
}

/**
 * Process and upload the cropped avatar.
 */
function processAndUploadAvatar() {
    if (!avatarUploadState.cropper || !avatarUploadState.selectedFile || !avatarUploadState.serviceProviderId) {
        showAvatarErrorMessage('No image selected for upload or missing service provider ID');
        return;
    }

    // Get cropped canvas at 200x200 size
    const canvas = avatarUploadState.cropper.getCroppedCanvas({
        width: 200,
        height: 200
    });

    // Convert canvas to blob
    canvas.toBlob(function(blob) {
        if (blob) {
            uploadAvatarFile(blob);
        } else {
            showAvatarErrorMessage('Failed to process avatar image. Please try again.');
        }
    }, 'image/jpeg', 0.8);
}

/**
 * Upload processed avatar file to server.
 */
function uploadAvatarFile(processedFile) {
    if (!processedFile) {
        showAvatarErrorMessage('No file to upload');
        return;
    }

    // Create form data
    const formData = new FormData();
    formData.append('avatar', processedFile, 'avatar.jpg');

    // Add CSRF token
    const csrfToken = document.querySelector('meta[name="_csrf"]');
    const csrfParamName = document.querySelector('meta[name="_csrf_parameter_name"]');

    if (csrfToken && csrfParamName) {
        const tokenValue = csrfToken.getAttribute('content');
        const paramName = csrfParamName.getAttribute('content');
        formData.append(paramName, tokenValue);
    }

    // Show loading state
    updateAvatarUploadProgress(0);

    // Create XMLHttpRequest for progress tracking
    const xhr = new XMLHttpRequest();

    // Track upload progress
    xhr.upload.addEventListener('progress', function(e) {
        if (e.lengthComputable) {
            const percentComplete = (e.loaded / e.total) * 100;
            updateAvatarUploadProgress(percentComplete);
        }
    });

    // Handle response
    xhr.addEventListener('load', function() {
        if (xhr.status === 200) {
            try {
                const response = JSON.parse(xhr.responseText);
                handleAvatarUploadResponse(response);
            } catch (e) {
                handleAvatarUploadResponse({
                    success: false,
                    error: 'Invalid response from server'
                });
            }
        } else {
            handleAvatarUploadResponse({
                success: false,
                error: `Avatar upload failed with status ${xhr.status}`
            });
        }
    });

    // Handle network errors
    xhr.addEventListener('error', function() {
        handleAvatarUploadResponse({
            success: false,
            error: 'Network error occurred during avatar upload'
        });
    });

    // Handle timeout
    xhr.addEventListener('timeout', function() {
        handleAvatarUploadResponse({
            success: false,
            error: 'Avatar upload timeout. Please try again.'
        });
    });

    // Configure and send request
    xhr.open('POST', `/api/service-providers/${avatarUploadState.serviceProviderId}/avatar`);
    xhr.timeout = 30000; // 30 second timeout
    xhr.send(formData);
}

/**
 * Handle avatar upload progress updates.
 */
function updateAvatarUploadProgress(percent) {
    const avatarConfirmUpload = document.getElementById('avatarConfirmUpload');
    const avatarProgressContainer = document.getElementById('avatarProgressContainer');
    const avatarProgressBar = document.getElementById('avatarProgressBar');
    const avatarProgressText = document.getElementById('avatarProgressText');

    if (!avatarConfirmUpload || !avatarProgressContainer || !avatarProgressBar || !avatarProgressText) {
        return;
    }

    if (percent === 0) {
        // Show loading state
        avatarConfirmUpload.disabled = true;
        avatarConfirmUpload.innerHTML = '<i class="fas fa-spinner fa-spin me-2"></i>Uploading...';
        avatarProgressContainer.style.display = 'block';
        avatarProgressBar.style.width = '0%';
        avatarProgressText.textContent = '0%';
    } else if (percent < 100) {
        // Update progress
        const roundedPercent = Math.round(percent);
        avatarProgressBar.style.width = `${percent}%`;
        avatarProgressText.textContent = `${roundedPercent}%`;
        avatarConfirmUpload.innerHTML = `<i class="fas fa-spinner fa-spin me-2"></i>Uploading... ${roundedPercent}%`;
    }
}

/**
 * Handle avatar upload success/error responses.
 */
function handleAvatarUploadResponse(response) {
    const avatarConfirmUpload = document.getElementById('avatarConfirmUpload');
    const avatarProgressContainer = document.getElementById('avatarProgressContainer');

    if (avatarConfirmUpload) {
        avatarConfirmUpload.disabled = false;
        avatarConfirmUpload.innerHTML = '<i class="fas fa-upload me-2"></i>Upload Avatar';
    }

    if (avatarProgressContainer) {
        avatarProgressContainer.style.display = 'none';
    }

    if (response.success) {
        // Handle successful upload
        console.log('Avatar upload successful:', response);

        // Show success message inside modal first
        showAvatarSuccessMessageInModal(response.message || 'Avatar uploaded successfully!');

        // Close modal after showing success message
        setTimeout(() => {
            closeAvatarModal();
        }, 2000);

        // Call success callback if provided
        if (avatarUploadState.onUploadSuccess && typeof avatarUploadState.onUploadSuccess === 'function') {
            avatarUploadState.onUploadSuccess(response);
        }

        // Also show global success message
        showAvatarSuccessMessage(response.message || 'Avatar uploaded successfully!');
    } else {
        // Handle upload error
        console.error('Avatar upload error:', response);
        const errorMessage = response.error || response.message || 'Avatar upload failed. Please try again.';
        showAvatarErrorMessage(errorMessage);
    }
}

/**
 * Show avatar image controls on hover.
 */
function showAvatarImageControls() {
    const avatarImageControls = document.getElementById('avatarImageControls');
    if (avatarImageControls && avatarUploadState.cropper && avatarUploadState.currentMode === null) {
        avatarImageControls.style.display = 'block';
    }
}

/**
 * Hide avatar image controls when not hovering.
 */
function hideAvatarImageControls() {
    const avatarImageControls = document.getElementById('avatarImageControls');
    if (avatarImageControls && avatarUploadState.currentMode === null) {
        avatarImageControls.style.display = 'none';
    }
}

/**
 * Enter avatar crop mode.
 */
function enterAvatarCropMode() {
    if (!avatarUploadState.cropper) {
        return;
    }

    avatarUploadState.currentMode = 'crop';
    avatarUploadState.cropBackupData = avatarUploadState.cropper.getData();

    // Hide image controls and show crop controls
    const avatarImageControls = document.getElementById('avatarImageControls');
    const avatarCropControls = document.getElementById('avatarCropControls');

    if (avatarImageControls) {
        avatarImageControls.style.display = 'none';
    }

    if (avatarCropControls) {
        avatarCropControls.style.display = 'block';
    }

    // Show rotation handles
    const handles = document.querySelectorAll('.avatar-rotation-handle');
    handles.forEach(handle => {
        handle.style.display = 'block';
    });

    // Enable crop mode
    avatarUploadState.cropper.setDragMode('crop');
}

/**
 * Enter avatar rotate mode.
 */
function enterAvatarRotateMode() {
    if (!avatarUploadState.cropper) {
        return;
    }

    avatarUploadState.currentMode = 'rotate';
    avatarUploadState.rotateBackupData = {
        angle: avatarUploadState.rotationAngle,
        data: avatarUploadState.cropper.getData()
    };

    // Hide image controls and show rotate controls
    const avatarImageControls = document.getElementById('avatarImageControls');
    const avatarRotateControls = document.getElementById('avatarRotateControls');

    if (avatarImageControls) {
        avatarImageControls.style.display = 'none';
    }

    if (avatarRotateControls) {
        avatarRotateControls.style.display = 'block';
    }

    // Show rotation handles
    const handles = document.querySelectorAll('.avatar-rotation-handle');
    handles.forEach(handle => {
        handle.style.display = 'block';
    });

    // Rotate by 90 degrees
    avatarUploadState.cropper.rotate(90);
    avatarUploadState.rotationAngle = (avatarUploadState.rotationAngle + 90) % 360;
    updateAvatarPreview();
}

/**
 * Add rotation handles to the avatar cropper.
 */
function addAvatarRotationHandles() {
    const cropperContainer = document.querySelector('.cropper-container');
    if (!cropperContainer) return;
    
    // Remove existing handles
    const existingHandles = cropperContainer.querySelectorAll('.avatar-rotation-handle');
    existingHandles.forEach(handle => handle.remove());
    
    // Create rotation handles for each corner
    const corners = ['top-left', 'top-right', 'bottom-left', 'bottom-right'];
    corners.forEach(corner => {
        const handle = document.createElement('div');
        handle.className = `avatar-rotation-handle avatar-rotation-handle-${corner}`;
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
            if (avatarUploadState.currentMode === 'rotate' || avatarUploadState.currentMode === 'crop') {
                avatarUploadState.cropper.rotate(90);
                avatarUploadState.rotationAngle = (avatarUploadState.rotationAngle + 90) % 360;
                updateAvatarPreview();
            }
        });
        
        cropperContainer.appendChild(handle);
    });
}

/**
 * Accept avatar crop changes.
 */
function acceptAvatarCropChanges() {
    avatarUploadState.currentMode = null;
    avatarUploadState.cropBackupData = null;

    // Hide crop controls and rotation handles
    const avatarCropControls = document.getElementById('avatarCropControls');
    if (avatarCropControls) {
        avatarCropControls.style.display = 'none';
    }

    const handles = document.querySelectorAll('.avatar-rotation-handle');
    handles.forEach(handle => {
        handle.style.display = 'none';
    });

    // Reset drag mode
    avatarUploadState.cropper.setDragMode('move');
    updateAvatarPreview();
}

/**
 * Cancel avatar crop changes.
 */
function cancelAvatarCropChanges() {
    if (avatarUploadState.cropBackupData) {
        avatarUploadState.cropper.setData(avatarUploadState.cropBackupData);
    }

    avatarUploadState.currentMode = null;
    avatarUploadState.cropBackupData = null;

    // Hide crop controls and rotation handles
    const avatarCropControls = document.getElementById('avatarCropControls');
    if (avatarCropControls) {
        avatarCropControls.style.display = 'none';
    }

    const handles = document.querySelectorAll('.avatar-rotation-handle');
    handles.forEach(handle => {
        handle.style.display = 'none';
    });

    // Reset drag mode
    avatarUploadState.cropper.setDragMode('move');
    updateAvatarPreview();
}

/**
 * Accept avatar rotate changes.
 */
function acceptAvatarRotateChanges() {
    avatarUploadState.currentMode = null;
    avatarUploadState.rotateBackupData = null;

    // Hide rotate controls and rotation handles
    const avatarRotateControls = document.getElementById('avatarRotateControls');
    if (avatarRotateControls) {
        avatarRotateControls.style.display = 'none';
    }
    
    const handles = document.querySelectorAll('.avatar-rotation-handle');
    handles.forEach(handle => {
        handle.style.display = 'none';
    });

    updateAvatarPreview();
}

/**
 * Cancel avatar rotate changes.
 */
function cancelAvatarRotateChanges() {
    if (avatarUploadState.rotateBackupData) {
        // Reset to backup rotation angle
        const currentAngle = avatarUploadState.rotationAngle;
        const backupAngle = avatarUploadState.rotateBackupData.angle;
        const deltaAngle = backupAngle - currentAngle;

        if (deltaAngle !== 0) {
            avatarUploadState.cropper.rotate(deltaAngle);
        }

        avatarUploadState.rotationAngle = backupAngle;
        avatarUploadState.cropper.setData(avatarUploadState.rotateBackupData.data);
    }

    avatarUploadState.currentMode = null;
    avatarUploadState.rotateBackupData = null;

    // Hide rotate controls and rotation handles
    const avatarRotateControls = document.getElementById('avatarRotateControls');
    if (avatarRotateControls) {
        avatarRotateControls.style.display = 'none';
    }
    
    const handles = document.querySelectorAll('.avatar-rotation-handle');
    handles.forEach(handle => {
        handle.style.display = 'none';
    });

    updateAvatarPreview();
}

/**
 * Close the avatar upload modal properly.
 */
function closeAvatarModal() {
    const avatarUploadModal = document.getElementById('avatarUploadModal');
    if (avatarUploadModal) {
        const modal = bootstrap.Modal.getInstance(avatarUploadModal);
        if (modal) {
            modal.hide();
        } else {
            const newModal = new bootstrap.Modal(avatarUploadModal);
            newModal.hide();
        }
    }
}

/**
 * Force close the avatar modal with aggressive cleanup.
 */
function forceCloseAvatarModal() {
    const avatarUploadModal = document.getElementById('avatarUploadModal');
    
    if (avatarUploadModal) {
        // Manually hide the modal
        avatarUploadModal.style.display = 'none';
        avatarUploadModal.classList.remove('show');
        avatarUploadModal.setAttribute('aria-hidden', 'true');
        avatarUploadModal.removeAttribute('aria-modal');
        avatarUploadModal.removeAttribute('role');
        
        // Try to dispose of Bootstrap modal instances
        const modal = bootstrap.Modal.getInstance(avatarUploadModal);
        if (modal) {
            try {
                modal.dispose();
            } catch (e) {
                console.warn('Error disposing avatar modal:', e);
            }
        }
    }
    
    // Cleanup
    cleanupAvatarModal();
    resetAvatarModalState();
}

/**
 * Clean up avatar modal immediately.
 */
function cleanupAvatarModal() {
    // Remove modal backdrops
    const modalBackdrops = document.querySelectorAll('.modal-backdrop, .modal-backdrop.fade, .modal-backdrop.show');
    modalBackdrops.forEach(backdrop => {
        backdrop.remove();
    });

    // Restore body classes and styles
    document.body.classList.remove('modal-open');
    document.body.style.removeProperty('padding-right');
    document.body.style.removeProperty('overflow');
}

/**
 * Reset avatar modal state to initial configuration.
 */
function resetAvatarModalState() {
    // Clear any error messages
    clearAvatarModalErrors();

    // Destroy cropper safely
    if (avatarUploadState.cropper) {
        try {
            avatarUploadState.cropper.destroy();
        } catch (e) {
            console.warn('Error destroying avatar cropper:', e);
        }
        avatarUploadState.cropper = null;
    }

    // Reset UI elements
    const avatarCropperImage = document.getElementById('avatarCropperImage');
    const avatarFileDropZone = document.getElementById('avatarFileDropZone');
    const avatarImageControls = document.getElementById('avatarImageControls');
    const avatarCropControls = document.getElementById('avatarCropControls');
    const avatarRotateControls = document.getElementById('avatarRotateControls');
    const avatarConfirmUpload = document.getElementById('avatarConfirmUpload');
    const avatarFileInput = document.getElementById('avatarFileInput');
    const avatarProgressContainer = document.getElementById('avatarProgressContainer');

    if (avatarCropperImage) {
        avatarCropperImage.style.display = 'none';
        avatarCropperImage.src = '';
    }

    if (avatarFileDropZone) {
        avatarFileDropZone.style.display = 'block';
        avatarFileDropZone.classList.remove('border-primary');
    }

    if (avatarImageControls) {
        avatarImageControls.style.display = 'none';
    }

    if (avatarCropControls) {
        avatarCropControls.style.display = 'none';
    }

    if (avatarRotateControls) {
        avatarRotateControls.style.display = 'none';
    }

    if (avatarConfirmUpload) {
        avatarConfirmUpload.disabled = true;
        avatarConfirmUpload.innerHTML = '<i class="fas fa-upload me-2"></i>Upload Avatar';
    }

    if (avatarFileInput) {
        avatarFileInput.value = '';
    }

    if (avatarProgressContainer) {
        avatarProgressContainer.style.display = 'none';
    }

    // Reset success alert
    const avatarSuccessAlert = document.getElementById('avatarSuccessAlert');
    if (avatarSuccessAlert) {
        avatarSuccessAlert.classList.add('d-none');
        avatarSuccessAlert.style.display = 'none';
    }

    // Reset state
    avatarUploadState.selectedFile = null;
    avatarUploadState.originalImageData = null;
    avatarUploadState.currentMode = null;
    avatarUploadState.rotationAngle = 0;
    avatarUploadState.serviceProviderId = null;
    avatarUploadState.onUploadSuccess = null;
}

/**
 * Show avatar success message inside the modal.
 */
function showAvatarSuccessMessageInModal(message) {
    const avatarSuccessAlert = document.getElementById('avatarSuccessAlert');
    const avatarSuccessMessage = document.getElementById('avatarSuccessMessage');
    
    if (avatarSuccessAlert && avatarSuccessMessage) {
        avatarSuccessMessage.textContent = message;
        avatarSuccessAlert.classList.remove('d-none');
        avatarSuccessAlert.style.display = 'block';
    }
}

/**
 * Show avatar success message.
 */
function showAvatarSuccessMessage(message) {
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
 * Show avatar error message.
 */
function showAvatarErrorMessage(message) {
    const avatarErrorContainer = document.getElementById('avatarErrorContainer');
    const avatarErrorMessage = document.getElementById('avatarErrorMessage');
    
    if (avatarErrorContainer && avatarErrorMessage) {
        avatarErrorMessage.textContent = message;
        avatarErrorContainer.style.display = 'block';
        
        // Auto-dismiss after 10 seconds
        setTimeout(() => {
            if (avatarErrorContainer) {
                avatarErrorContainer.style.display = 'none';
            }
        }, 10000);
    }
}

/**
 * Clear avatar error messages.
 */
function clearAvatarModalErrors() {
    const avatarErrorContainer = document.getElementById('avatarErrorContainer');
    if (avatarErrorContainer) {
        avatarErrorContainer.style.display = 'none';
    }
}

// Initialize avatar upload functionality when DOM is ready
document.addEventListener('DOMContentLoaded', function() {
    initializeAvatarUpload();
});

// Expose functions globally for integration
window.openAvatarUploadModal = openAvatarUploadModal;
window.avatarUploadState = avatarUploadState;