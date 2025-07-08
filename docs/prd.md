# Project Requirements Document

## 1. Purpose & Scope
**Purpose**  
Enable individuals and families to digitize, review, and manage household (and other) expenses via receipt and bill images, automated OCR extraction, manual approval, and rich reporting.

**Scope**
- Receipt and Bill ingestion (via web UI)
- OCR via OpenAI, Claude, Google AI, or STUB for development
- Manual review & approval for Receipts and Bills
- Association of Receipts with Bills
- Payment record management (one-off & recurring)
- Attachments & comments
- Reporting & exports (CSV/Excel)
- Admin settings (OCR engines, API keys)

## Main User Scenarios & Stories
1. **Ingest Receipt/Bill**
    - *As a user*, I want to upload images via UI (upload button or drag-and-drop) so that I don't have to manually enter data.
    - *As a user*, I want to edit (resize/crop) my images before upload so that I can optimize them for processing.
2. **Review Extraction**
    - *As a user*, I want to edit or approve OCR-extracted data from receipts or bills to ensure accuracy before payment.
3. **Associate Receipt with Bill**
    - *As a user*, I want to associate one or more receipts with a specific bill, or create a new bill from a receipt.


## Entities

### User 
#### User data
 - name 
 - email
 - avatar
#### User state
User do not have states.
#### User behaviour
User can log in into system only if its email in the list of allowed emails.

### InboxEntity
#### InboxEntity data
 - id: unique identifier for the inbox item
 - uploadedImage: path to the uploaded image file
 - uploadDate: timestamp when the document was uploaded
 - ocrResults: OCR extracted text results (nullable)
 - linkedEntityId: ID of the created bill or receipt entity (nullable)
 - linkedEntityType: type of entity created (BILL or RECEIPT, nullable)
 - state: current state of the inbox item (InboxState)
 - failureReason: reason for OCR failure (nullable)
#### InboxEntity state
InboxEntity can be in the following states:
 - CREATED: Just uploaded, OCR processing pending
 - PROCESSED: OCR processing completed successfully
 - FAILED: OCR processing failed
 - APPROVED: User approved, bill or receipt created
#### InboxEntity behaviour
InboxEntity represents uploaded documents in the processing workflow:
 - InboxEntity can be created when user uploads an image
 - InboxEntity transitions from CREATED to PROCESSED after successful OCR
 - InboxEntity transitions from CREATED to FAILED when OCR fails
 - InboxEntity transitions from PROCESSED to APPROVED when user approves
 - InboxEntity transitions from FAILED back to CREATED for retry
 - InboxEntity validates state transitions (canApprove, canRetry)
 - InboxEntity links to created bills or receipts via linkedEntityId

### Bill
#### Bill data
 - id: unique identifier for the bill
 - serviceProviderId: identifier of the service provider
 - billDate: date of the bill
 - amount: monetary amount of the bill (BigDecimal)
 - inboxEntityId: optional link to the inbox entity (if created from OCR)
 - state: current state of the bill (BillState)
 - createdDate: timestamp when the bill was created
 - description: optional description for the bill
#### Bill state
Bills can be in the following states:
 - CREATED: Bill exists and is active, can be modified and managed
 - REMOVED: Bill has been deleted and is no longer active
#### Bill behaviour
Bills represent financial obligations that users need to track and manage:
 - Bills can be created from approved inbox items (linking to OCR process)
 - Bills can be created manually without inbox item
 - Bills can be removed (transition to REMOVED state)
 - Bills can have their amount updated (must be greater than zero)
 - Bills can have their service provider updated
 - Bills track validation states (canRemove, isActive) 


## Functional Requirements

### Authentication & Authorization
- System MUST authenticate users via Google OAuth 2.0
- System MUST restrict access to users whose email addresses are in the configured allowlist
- System MUST redirect unauthenticated users to the login page
- System MUST redirect authenticated users to the dashboard upon successful login
- System MUST store user profile information (name, email, avatar) from Google OAuth response
- System MUST validate user email against environment-configured allowlist
- System MUST reject login attempts from non-allowlisted email addresses
- System MUST provide secure session management
- System MUST provide logout functionality

### Image Upload & File Management
- System MUST support file upload via upload button and drag-and-drop interface
- System MUST accept image files in JPEG, PNG, GIF, and WebP formats
- System MUST validate file size limits (maximum 20MB per file)
- System MUST provide client-side image processing capabilities (resize, crop, undo)
- System MUST store uploaded images in filesystem directory defined by INBOX_PATH configuration
- System MUST create InboxEntity records only upon successful upload confirmation
- System MUST provide secure file storage with proper access controls
- System MUST generate unique filenames to prevent conflicts
- System MUST validate file types and reject malicious files
- System MUST provide progress feedback during file upload
- System MUST handle upload errors gracefully with user-friendly error messages

## UI Requirements

### Login Interface
- Login page MUST display application branding (not "Airtable")
- Login page MUST provide "Continue with Google" button
- Login page MUST NOT display email input field or other OAuth providers
- Login page MUST display terms of service and privacy policy links
- Login page MUST be responsive and accessible
- Login page MUST handle authentication errors gracefully

### Dashboard Interface
- Dashboard MUST display as placeholder page for authenticated users
- Dashboard MUST show user profile information
- Dashboard MUST provide logout functionality

### Image Upload Interface
- Topbar MUST display a green "Upload" button for image uploading
- Inbox table MUST provide drag-and-drop area below records for image upload
- Upload dialog MUST open when upload button is clicked or image is dropped
- Upload dialog MUST display the selected/dropped image for preview
- Upload dialog MUST provide image resize functionality with preview
- Upload dialog MUST provide image crop functionality with preview
- Upload dialog MUST provide undo functionality for resize/crop operations
- Upload dialog MUST have "Upload" button to confirm and save the image
- Upload dialog MUST have "Cancel" button to abort the upload process
- Upload dialog MUST create InboxEntity only when "Upload" is clicked
- Upload dialog MUST NOT create InboxEntity when "Cancel" is clicked
- Drag-and-drop area MUST provide visual feedback during drag operations
- Drag-and-drop area MUST accept common image formats (JPEG, PNG, GIF, WebP)
- Upload system MUST store images in filesystem directory defined in properties

### Bills Management Interface
- Bills view MUST display bills in a table format with sortable columns
- Bills table MUST show bill date, service provider, amount, description, created date, and actions
- Bills table MUST format amounts as currency with proper formatting
- Bills table MUST show service provider names with creation source indication
- Bills table MUST truncate long descriptions with full text available on hover
- Bills table MUST provide edit and remove actions for active bills
- Bills table MUST show removed status for deleted bills
- Bills table MUST support sorting by bill date, amount, and created date
- Bills table MUST support pagination and search functionality
- Bills table MUST show empty state when no bills are available
- Bills table MUST integrate with BaseTable component for common functionality

## User Stories

### Authentication Stories
- As a user, I want to login with my Google account so that I can access the system securely
- As a user, I want to be redirected to login if I'm not authenticated so that the system remains secure
- As a user, I want to see a clear error message if my email is not authorized so that I understand why I cannot access the system
- As a user, I want to be automatically redirected to the dashboard after successful login so that I can start using the system
- As a user, I want to logout securely so that my session is properly terminated
- As a system administrator, I want to control who can access the system by configuring allowed email addresses

### Image Upload Stories
- As a user, I want to click a green "Upload" button in the topbar so that I can easily start uploading receipt images
- As a user, I want to drag and drop image files into the inbox area so that I can upload receipts quickly without clicking buttons
- As a user, I want to see a preview of my selected image in an upload dialog so that I can confirm I selected the correct file
- As a user, I want to resize my uploaded image so that I can optimize it for processing and storage
- As a user, I want to crop my uploaded image so that I can focus on the important parts of the receipt
- As a user, I want to undo my resize and crop operations so that I can correct mistakes without starting over
- As a user, I want to confirm my upload with an "Upload" button so that I can save the processed image
- As a user, I want to cancel my upload with a "Cancel" button so that I can abort if I change my mind
- As a user, I want visual feedback when dragging files over the drop area so that I know where to drop them
- As a user, I want the system to accept common image formats (JPEG, PNG, GIF, WebP) so that I can upload receipts from various sources
- As a user, I want uploaded images to be stored securely in the filesystem so that my data is preserved
- As a user, I want an InboxEntity to be created only when I confirm the upload so that cancelled uploads don't create unnecessary records

