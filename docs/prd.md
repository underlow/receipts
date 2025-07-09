# Project Requirements Document

## 1. Purpose & Scope
**Purpose**  
Enable individuals and families to digitize, review, and manage household (and other) expenses via receipt and bill images, automated OCR extraction, manual approval, and rich reporting.

**Scope**
- Receipt and Bill ingestion (via web UI)
- OCR via OpenAI, Claude, Google AI, or STUB for development
- Manual review & approval for Receipts and Bills
- Association of Receipts with Bills
- Service Provider management with custom fields and avatars
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
    - *As a user*, I want to link bills and receipts to service providers so that I can track which company issued each document.
4. **Manage Service Providers**
    - *As a user*, I want to create new service providers so that I can categorize my bills and receipts by company/vendor
    - *As a user*, I want to edit service provider information so that I can keep their details up to date
    - *As a user*, I want to hide service providers I no longer use so that my active list stays clean while preserving historical data
    - *As a user*, I want to upload and resize service provider avatars so that I can visually identify providers quickly
5. **Customize Service Provider Fields**
    - *As a user*, I want to add custom fields to service providers so that I can track information specific to each provider
    - *As a user*, I want to edit custom field values so that I can update provider-specific information
    - *As a user*, I want to remove custom fields I no longer need so that my provider forms stay relevant


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
 - serviceProviderId: identifier of the service provider (nullable, links to ServiceProvider)
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

### Receipt
#### Receipt data
 - id: unique identifier for the receipt
 - serviceProviderId: identifier of the service provider (nullable, links to ServiceProvider)
 - receiptDate: date of the receipt
 - amount: monetary amount of the receipt (BigDecimal)
 - inboxEntityId: optional link to the inbox entity (if created from OCR)
 - state: current state of the receipt (ReceiptState)
 - createdDate: timestamp when the receipt was created
 - description: optional description for the receipt
#### Receipt state
Receipts can be in the following states:
 - CREATED: Receipt exists and is active, can be modified and managed
 - REMOVED: Receipt has been deleted and is no longer active
#### Receipt behaviour
Receipts represent expense documents that users need to track:
 - Receipts can be created from approved inbox items (linking to OCR process)
 - Receipts can be created manually without inbox item
 - Receipts can be removed (transition to REMOVED state)
 - Receipts can have their amount updated (must be greater than zero)
 - Receipts can have their service provider updated
 - Receipts track validation states (canRemove, isActive)

### ServiceProvider
#### ServiceProvider data
 - id: Long - unique identifier for the service provider
 - name: String - service provider name
 - avatar: String - file path to uploaded avatar image (200x200)
 - comment: String - general notes about the service provider
 - commentForOcr: String - specific notes to help OCR recognize this provider
 - regular: Enum - frequency of bills (YEARLY, MONTHLY, WEEKLY, NOT_REGULAR)
 - customFields: JSON - user-defined fields array [{name: String, value: String, comment: String}]
 - state: ServiceProviderState - current state of the service provider
 - createdDate: timestamp when the service provider was created
 - modifiedDate: timestamp when the service provider was last modified
#### ServiceProvider state
ServiceProvider can be in the following states:
 - ACTIVE: Service provider is visible and can be used for new bills/receipts
 - HIDDEN: Service provider is hidden from UI but preserved for historical data
#### ServiceProvider behaviour
ServiceProvider represents companies, utilities, or vendors that issue bills and receipts:
 - ServiceProvider can be created by users manually
 - ServiceProvider can be edited to update name, avatar, comments, and custom fields
 - ServiceProvider can transition from ACTIVE to HIDDEN (hide from UI)
 - ServiceProvider can transition from HIDDEN to ACTIVE (show in UI)
 - ServiceProvider validates custom fields structure (name, value, comment)
 - ServiceProvider links to bills via Bill.serviceProviderId (nullable)
 - ServiceProvider links to receipts via Receipt.serviceProviderId (nullable)


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
- System MUST support separate avatar upload workflow for service providers
- System MUST provide different image processing for avatars (200x200 resize) vs receipts/bills
- System MUST store avatar images in configurable AVATAR_PATH directory separate from INBOX_PATH

### Service Provider Management
- System MUST provide CRUD operations for service providers (create, read, update, hide/show)
- System MUST validate service provider name is not empty
- System MUST store service provider custom fields as valid JSON structure
- System MUST validate custom field structure (name, value, comment fields)
- System MUST allow users to add unlimited custom fields to service providers
- System MUST allow users to remove custom fields from service providers
- System MUST transition service providers between ACTIVE and HIDDEN states
- System MUST preserve service provider data when set to HIDDEN state
- System MUST filter HIDDEN service providers from new bill/receipt associations
- System MUST maintain historical links to HIDDEN service providers in existing bills/receipts

### Avatar Upload & Management
- System MUST support avatar image upload for service providers
- System MUST accept image files in JPEG, PNG, GIF, and WebP formats for avatars
- System MUST validate avatar file size limits (maximum 10MB per file)
- System MUST resize uploaded avatar images to exactly 200x200 pixels
- System MUST store avatar images in separate directory from receipt/bill images
- System MUST generate unique avatar filenames to prevent conflicts
- System MUST provide avatar upload dialog separate from receipt/bill upload
- System MUST handle avatar upload errors gracefully with user-friendly error messages
- System MUST allow users to replace existing avatars
- System MUST clean up old avatar files when replaced

### Receipt Management
- System MUST provide CRUD operations for receipts (create, read, update, remove)
- System MUST validate receipt amounts are greater than zero
- System MUST link receipts to service providers via serviceProviderId (nullable)
- System MUST transition receipts between CREATED and REMOVED states
- System MUST preserve receipt data when set to REMOVED state
- System MUST create receipts from approved inbox items
- System MUST allow manual receipt creation without inbox items

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
- Dashboard MUST display Services tab as 4th tab in left sidebar
- Dashboard MUST show visual separator between Receipts and Services tabs
- Dashboard MUST support navigation between all tabs (including Services)

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
- System MUST provide separate avatar upload dialog for service providers
- Avatar upload dialog MUST resize images to 200x200 pixels specifically
- Avatar upload system MUST store images in AVATAR_PATH directory separate from INBOX_PATH

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

### Services Management Interface
- Services tab MUST be added as 4th tab in dashboard left sidebar
- Services tab MUST be positioned below Receipts tab with visual separator
- Services tab MUST display "Services" label and appropriate icon
- Services view MUST use split-panel layout with list on left and form on right
- Services list panel MUST display all service providers (both ACTIVE and HIDDEN)
- Services list panel MUST show service provider name, avatar thumbnail, and state
- Services list panel MUST highlight selected service provider
- Services list panel MUST support scrolling for long lists
- Services list panel MUST show empty state when no service providers exist
- Services form panel MUST display form for creating new service providers when none selected
- Services form panel MUST display form for editing selected service provider
- Services form panel MUST include name input field (required)
- Services form panel MUST include avatar upload button with 200x200 preview
- Services form panel MUST include comment textarea field
- Services form panel MUST include OCR comment textarea field
- Services form panel MUST include regular frequency dropdown (Yearly, Monthly, Weekly, Not regular)
- Services form panel MUST include custom fields section with add/remove functionality
- Services form panel MUST include ACTIVE/HIDDEN state toggle
- Services form panel MUST include Save and Cancel buttons
- Services form panel MUST validate required fields and show error messages

### Avatar Upload Interface
- Avatar upload dialog MUST open when avatar upload button is clicked
- Avatar upload dialog MUST display selected image preview
- Avatar upload dialog MUST show 200x200 resize preview
- Avatar upload dialog MUST provide crop functionality if needed
- Avatar upload dialog MUST have "Upload" button to confirm avatar selection
- Avatar upload dialog MUST have "Cancel" button to abort avatar upload
- Avatar upload dialog MUST accept common image formats (JPEG, PNG, GIF, WebP)
- Avatar upload dialog MUST validate file size (maximum 10MB)
- Avatar upload dialog MUST show progress indicator during upload
- Avatar upload dialog MUST handle upload errors with user-friendly messages

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

### Service Provider Management Stories
- As a user, I want to create new service providers so that I can categorize my bills and receipts by company
- As a user, I want to edit service provider details so that I can keep their information up to date
- As a user, I want to hide service providers I no longer use so that my active list stays clean
- As a user, I want to show hidden service providers so that I can reactivate them if needed
- As a user, I want to view all my service providers in a list so that I can see what companies I track
- As a user, I want to select a service provider from the list so that I can view or edit their details
- As a user, I want to see service provider avatars so that I can quickly identify companies visually
- As a user, I want to add comments to service providers so that I can remember important details about them
- As a user, I want to add OCR comments to service providers so that the system can better recognize their documents

### Avatar Upload Stories
- As a user, I want to upload an avatar for a service provider so that I can visually identify them
- As a user, I want to see a preview of the avatar before uploading so that I can confirm it looks correct
- As a user, I want avatars to be automatically resized to 200x200 pixels so that they display consistently
- As a user, I want to replace existing avatars so that I can update provider images when needed
- As a user, I want to crop avatar images so that I can focus on the important part of the logo or image
- As a user, I want to cancel avatar uploads so that I can abort if I change my mind
- As a user, I want clear error messages if avatar upload fails so that I know what went wrong

### Custom Fields Stories
- As a user, I want to add custom fields to service providers so that I can track information specific to each company
- As a user, I want to edit custom field values so that I can update provider-specific information
- As a user, I want to remove custom fields I no longer need so that my forms stay clean and relevant
- As a user, I want to add comments to custom fields so that I can document what each field represents
- As a user, I want to see all custom fields for a service provider so that I have complete information about them

### Regular Frequency Stories
- As a user, I want to set the billing frequency for service providers so that I know how often to expect bills
- As a user, I want to choose from standard frequencies (Yearly, Monthly, Weekly) so that I can categorize regular bills
- As a user, I want to mark providers as "Not regular" so that I can distinguish one-time or irregular billing

### Service Provider Navigation Stories
- As a user, I want to access service providers through a dedicated Services tab so that I can manage them easily
- As a user, I want the Services tab to be clearly separated from other tabs so that I can find it quickly
- As a user, I want to see the Services tab below the Receipts tab so that it follows a logical order
- As a user, I want to switch between service providers and other features seamlessly so that I can work efficiently

### Receipt Management Stories
- As a user, I want to create receipts from uploaded images so that I can digitize my expense records
- As a user, I want to link receipts to service providers so that I can organize expenses by company
- As a user, I want to edit receipt details so that I can correct any mistakes or add missing information
- As a user, I want to remove receipts I no longer need so that my records stay accurate

