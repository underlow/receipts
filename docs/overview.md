# Household Expense Tracker - Functionality Overview

## Overview

The Household Expense Tracker is a web-based application designed to streamline the management of household bills and payments through automated receipt processing, OCR extraction, and comprehensive expense tracking.

## Core Features

### 1. User Authentication & Management
- **OAuth2 Integration**: Secure login via Google OAuth2
- **Automatic User Provisioning**: Users are created automatically on first login
- **Session Management**: Login events are tracked with timestamps and IP addresses
- **Security**: All routes are protected except login and static resources

### 2. Domain Model & Data Management

#### 2.1 Service Providers
- **Purpose**: Manage companies/services that you pay (electricity, gas, internet, etc.)
- **Fields**: name, category, default payment method, active status, comments
- **Features**: Categorization for better organization and reporting

#### 2.2 Payment Methods
- **Purpose**: Track different ways you pay bills (credit cards, bank transfers, cash, etc.)
- **Types**: CARD, BANK, CASH, OTHER
- **Fields**: name, type, comments
- **Integration**: Links to payments for expense tracking

#### 2.3 File Ingestion & Management

##### 2.3.1 IncomingFiles ✅ **IMPLEMENTED WITH STATUS CONSOLIDATION**
- **Purpose**: Track files detected by the folder-watcher service through complete OCR processing workflow
- **Processing States**: NEW (consolidated from pending, processing, draft) → APPROVED/REJECTED with automatic transitions
- **Status Consolidation**: Legacy statuses (pending, processing, draft) are consolidated to NEW for simplified UX
- **OCR Integration**: Stores OCR results, extracted data, and error information
- **Duplicate Prevention**: SHA-256 checksum-based duplicate detection
- **File Storage**: Organized storage with date-prefixed naming (`yyyy-MM-dd-filename`)
- **Tab Visibility**: IncomingFile items only appear in the New tab
- **Fields**: filename, file path, upload date, status, checksum, user association, OCR raw JSON, extracted amount/date/provider, processing timestamps, error messages

##### 2.3.2 Bills & Document Management ✅ **IMPLEMENTED WITH STATUS-BASED TABS**
- **Purpose**: Store and process uploaded receipt/bill documents after OCR processing
- **Processing States**: NEW (consolidated from pending, processing, draft) → APPROVED/REJECTED
- **Status Consolidation**: Legacy statuses (pending, processing, draft) are consolidated to NEW for simplified UX
- **OCR Integration**: Automatic extraction of amount, date, and provider information
- **File Storage**: Links to processed files from IncomingFiles
- **Tab Visibility**: Bills can appear in all tabs (New, Approved, Rejected) based on status
- **Fields**: filename, file path, upload date, status, OCR data, extracted information, user association

#### 2.4 Receipts ✅ **IMPLEMENTED WITH STATUS-BASED TABS**
- **Purpose**: Represent individual expense items that may be associated with bills
- **Processing States**: NEW (consolidated from pending, processing, draft) → APPROVED/REJECTED
- **Status Consolidation**: Legacy statuses (pending, processing, draft) are consolidated to NEW for simplified UX
- **Flexibility**: Can exist independently or be linked to bills
- **Organization**: Allows for grouping multiple receipts under a single bill
- **Tab Visibility**: Receipts can appear in all tabs (New, Approved, Rejected) based on status
- **User Association**: Each receipt is tied to a specific user

#### 2.5 Payments
- **Purpose**: Finalized, approved transactions ready for reporting and analysis
- **Comprehensive Tracking**: Service provider, payment method, amounts, dates
- **Multi-Currency Support**: Currency field for international expenses
- **Audit Trail**: Links back to original bills and receipts
- **Fields**: service provider, payment method, amount, currency, invoice date, payment date, bill reference, user, comments

### 3. Workflow & Processing

#### 3.1 Receipt Ingestion
1. **Folder-Watcher Service**: ✅ **IMPLEMENTED** - Automated detection of files dropped into inbox directory
   - Polls `/data/inbox` directory every 30 seconds
   - SHA-256 checksum-based duplicate detection
   - Creates `IncomingFile` entities in PENDING status
   - Organized storage with date-prefixed naming (`yyyy-MM-dd-filename`)
   - Automatic conflict resolution for duplicate filenames
2. **File Upload**: ✅ **IMPLEMENTED** - Users can upload receipt images/PDFs through web interface
   - Multi-part file upload with drag-and-drop support
   - Modern web UI with drag-and-drop file upload at `/upload`
   - Real-time upload progress and status feedback
   - File type validation (PDF, JPG, PNG, GIF, BMP, TIFF)
   - File size limits and validation (10MB maximum)
   - Duplicate detection and prevention
3. **OCR Processing**: ✅ **IMPLEMENTED** - Synchronous extraction of key information using AI engines
   - Multi-engine support (OpenAI, Claude, Google AI) with fallback mechanisms
   - **Synchronous OCR Flow**: Files trigger OCR immediately after upload with modal progress indicator
   - **Non-closable Processing**: Modal cannot be closed during OCR processing
   - **Success/Failure Feedback**: Clear feedback with retry options for failed processing
   - **Intelligent Processing**: 
     - IncomingFile OCR: Guesses type and extracts info
     - Bill/Receipt OCR: Only extracts info (no type guessing)
   - Status management through NEW → APPROVED/REJECTED workflow (processing state removed)
   - Error handling and retry mechanisms with unlimited user fixes
   - **OCR History**: Complete attempts history preserved across entity transitions

#### 3.2 OCR & Data Extraction ✅ **IMPLEMENTED**
- **Multi-Engine Support**: OpenAI GPT-4 Vision, Claude Vision, Google Gemini, and STUB integration
- **STUB Engine**: Development mode with random OCR results when no real engines configured
- **Fallback Mechanisms**: Automatic retry with different engines or STUB fallback
- **Smart Extraction**: Automatically identifies provider, amount, dates, and currency
- **Service Provider Handling**: Unknown providers trigger manual input dialog with retry options
- **Service Provider Management**: Complete CRUD operations for service providers with dialog interface
- **Immediate Detail View Navigation**: OCR completion automatically opens appropriate bill/receipt detail view
- **Raw Data Preservation**: Original OCR JSON stored in database for reference
- **Status Tracking**: Real-time processing status updates throughout workflow
- **Error Handling**: Comprehensive error capture and retry mechanisms
- **Performance Monitoring**: Processing time tracking and confidence scoring
- **Integration Points**: Seamless integration with ServiceProvider and PaymentMethod entities
- **Type Detection**: Can either automatically detect document type or use user-specified type
- **Manual Type Selection**: Users can pre-select Bill or Receipt type before OCR processing
- **OCR Prompt Behavior**: 
  - For IncomingFile: OCR guesses type and extracts info
  - For Bill/Receipt: OCR only extracts info (no type guessing)
- **OCR Attempts History**: Complete history of all OCR processing attempts stored for audit

#### 3.3 Review & Approval Process ✅ **IMPLEMENTED**
1. **Inbox Management**: ✅ Comprehensive inbox interface for file review and management
   - Paginated grid view with thumbnails for uploaded files
   - Status filtering (PENDING, PROCESSING, APPROVED, REJECTED) with counts
   - Sorting capabilities by filename, upload date, and status
   - File operations: manual type selection (Bill/Receipt), OCR processing, delete with AJAX support
   - Modal image viewer for detailed file inspection
   - Navigation to detail views via "Detail" buttons
   - **Enhanced Panel Actions**: 
     - "Send to OCR" - processes file with automatic type detection (moved to actions panel)
     - "Bill" - converts file to Bill entity with OCR data for editing
     - "Receipt" - converts file to Receipt entity with OCR data for editing
     - Removed "Approve File" button for simplified workflow
   - **Flexible Workflows**:
     - Automatic OCR on upload with type detection
     - Manual type selection then OCR for info extraction only
     - User can accept, reject (revert to IncomingFile), or retry OCR anytime
   - **Entity Conversion System**: Seamless conversion between IncomingFile ↔ Bill ↔ Receipt with complete data preservation
2. **Detail Views**: ✅ **IMPLEMENTED** - Split-pane interfaces for bill and receipt processing
   - **Bill Detail View**: Zoomable image viewer with OCR data editing and payment creation
   - **Receipt Detail View**: Receipt association management and standalone payment processing
   - **Interactive Forms**: Pre-populated with OCR data, service provider/method selection
   - **Service Provider Management**: "Edit Providers" button opens management dialog for CRUD operations
   - **Workflow Actions**: Save draft, approve/reject, associate/dissociate operations
   - **Two-Tab Interface**: When IncomingFile is converted to Bill/Receipt:
     - **Bill/Receipt Tab**: Editable entity fields prefilled with OCR values, Accept/Revert buttons (available anytime)
     - **Information Tab**: Technical file details, metadata, and complete OCR attempts history
   - **Navigation**: Breadcrumbs removed for simplified interface with direct navigation
   - **OCR History Tracking**: Complete audit trail of all OCR processing attempts preserved across entity conversions
   - **Revert Functionality**: Users can revert Bill/Receipt back to IncomingFile anytime with full data preservation
3. **OCR Processing Management**: ✅ Complete OCR workflow control through intuitive interface
   - Manual OCR trigger via "Send to OCR" button for unprocessed files
   - Automatic OCR processing for newly uploaded files
   - Real-time status monitoring with visual indicators
   - Error handling with "Retry OCR" functionality for failed processing
   - Extracted data preview (provider, amount, date) in both inbox and detail views
4. **Manual Review**: ✅ Users can edit and correct OCR-extracted data in detail views
5. **Provider Matching**: ✅ Associate bills with existing service providers via dropdown selection
6. **Payment Method Selection**: ✅ Choose how the bill was/will be paid via dropdown selection
7. **Receipt Association**: ✅ Link multiple receipts to a single bill or process as standalone
8. **Approval**: ✅ Convert approved bills into payment records with comprehensive data

#### 3.4 OCR Dispatch & Bill Creation ✅ **IMPLEMENTED**
1. **Automatic Processing**: Files trigger OCR processing immediately after upload/detection
2. **Status Management**: Real-time status updates through PENDING → PROCESSING → APPROVED/REJECTED
3. **Error Handling**: Failed OCR processing is captured with detailed error messages
4. **Retry Mechanisms**: Users can retry failed OCR processing through the UI
5. **Bill Dispatch**: Approved OCR results are automatically converted to Bill entities
6. **Data Extraction**: Provider, amount, date, and currency are extracted and stored
7. **User Interface**: Comprehensive OCR interface integration
   - Real-time status indicators in inbox file grid
   - Detailed OCR information panel in file detail view
   - Interactive OCR action buttons ("Send to OCR", "Retry OCR", "Create Bill")
   - Extracted data visualization with provider, amount, and date display
   - Processing timestamps and error message reporting
8. **API Endpoints**: RESTful endpoints for complete OCR workflow management
   - `POST /api/files/{fileId}/ocr`: Trigger OCR processing
   - `POST /api/files/{fileId}/ocr-retry`: Retry failed OCR processing  
   - `POST /api/files/{fileId}/dispatch`: Dispatch approved files to Bills
   - `GET /api/ocr-statistics`: Get user OCR processing statistics

### 4. Data Architecture

#### 4.1 Database Design
- **PostgreSQL**: Production database with full ACID compliance
- **H2**: In-memory database for development and testing
- **Spring Data JDBC**: Modern data access with JdbcTemplate
- **Liquibase**: Database schema versioning and migrations
- **Validation**: Jakarta Bean Validation for data integrity

#### 4.2 Entity Relationships
```
Users ──┬── LoginEvents
        ├── IncomingFiles ──┬── Bills (via conversion)
        ├── Bills ──┬── Receipts (optional)    └── Receipts (via conversion)
        ├── Receipts    └── Payments (optional)
        ├── Payments ──┬── ServiceProviders
        │              └── PaymentMethods
        └── OcrAttempts ──┬── IncomingFiles (entity tracking)
                          ├── Bills (entity tracking)
                          └── Receipts (entity tracking)

Status Model:
- ItemStatus enum: NEW, APPROVED, REJECTED
- Status consolidation: pending + processing + draft → NEW
- Tab visibility: 
  - New tab: IncomingFile, Bill, Receipt with status=NEW
  - Approved/Rejected tabs: Bills & Receipts with type filters
```

#### 4.3 Data Validation
- **Required Fields**: Enforced at entity level with validation annotations
- **Business Rules**: Amount must be positive, dates must be valid
- **Referential Integrity**: Foreign key constraints ensure data consistency
- **Type Safety**: Enums for status and payment method types

### 5. User Interface & Experience ✅ **IMPLEMENTED**

#### 5.1 Dashboard
- **Welcome Page**: Clean, modern interface with user authentication display
- **Navigation**: Quick access to key features (Inbox, Upload)
- **Feature Cards**: Intuitive layout for accessing main functionality

#### 5.2 Inbox Management Interface ✅ **SIDEBAR NAVIGATION WITH UNIFIED TABLE**
- **Sidebar Navigation**: Left sidebar with navigation tabs (Inbox, Bills, Receipts, Service Provider tabs)
- **Unified Table Layout**: Right panel with sortable table showing entities based on selected tab
- **Inbox Tab**: Shows IncomingFile, Bill, Receipt items with NEW status (consolidated from pending, processing, draft)
- **Entity Tabs**: Bills and Receipts tabs show all items with all statuses (NEW, APPROVED, REJECTED)
- **Dynamic Provider Tabs**: Automatically created tabs for each service provider with associated items
- **Responsive Design**: Mobile-friendly layout with collapsible sidebar
- **Smart Thumbnails**: Automatic thumbnail generation for images and PDF placeholders
- **Status Consolidation**: Legacy statuses (pending, processing, draft) consolidated to NEW
- **OCR Status Indicators**: Synchronous OCR processing with modal progress feedback
  - Modal progress indicator during OCR execution (non-closable)
  - Success/failure feedback with retry options
  - 🔍 Extracted data display (amount, provider) for completed processing
  - ❌ Error indicators for failed OCR processing with retry functionality
- **Real-time Operations**: AJAX-powered tab switching and table data loading
- **Sorting**: Multi-column sorting (filename, date, status) with direction control
- **Pagination**: Efficient pagination for large item collections
- **Modal Viewer**: Full-screen file preview with keyboard shortcuts
- ✅ **Tab Content Loading System** (GitHub issue #20):
  - **Performance Optimized**: Dedicated API endpoints for each tab type with pagination support
  - **Loading States**: Proper loading indicators and error handling for enhanced UX
  - **Data Caching**: Client-side caching of tab data to avoid unnecessary requests
  - **Real-time Updates**: Tab data automatically refreshes after user actions (approve/reject)
  - **Pagination**: Configurable page size with efficient server-side pagination
  - **Error Handling**: Comprehensive error messages and retry mechanisms

#### 5.3 File Operations
- **Secure Access**: User-scoped file access with authentication verification
- **Thumbnail Service**: On-demand thumbnail generation with caching
- **File Serving**: Direct file access with proper MIME type handling
- **Error Handling**: Comprehensive error feedback and validation

#### 5.4 Detail View Interfaces ✅ **IMPLEMENTED**
- **Split-Pane Design**: Image viewer (left) and metadata/actions interface (right)
- **Interactive Image Viewer**: Zoom, pan controls with support for images and PDFs
- **OCR Processing Section**: Complete OCR workflow management interface
  - Real-time OCR status display (pending, processing, completed, failed)
  - Extracted data visualization (provider, amount, date, currency)
  - Processing timestamps and error message display
  - OCR action buttons: "Send to OCR", "Retry OCR", "Create Bill"
- **File Actions**: Status-aware approve, reject, delete operations
- **Technical Details**: Comprehensive file metadata and system information
- **AJAX Operations**: Seamless OCR processing, approval, and dispatch actions
- **User Feedback**: Success/error notifications with auto-dismiss functionality
- **Navigation**: Integrated navigation between inbox and detail views

### 6. File Management
- **Storage Location**: `/data/inbox` for incoming receipts, `/data/attachments` for processed files
- **Security**: Files stored outside web root for security
- **Organization**: Structured file paths for easy management
- **Backup**: Volume-mounted storage for data persistence
- **Thumbnail Cache**: Generated thumbnails for improved performance

### 7. Technology Stack
- **Backend**: Kotlin + Spring Boot with coroutines for async processing
- **Frontend**: Thymeleaf server-side rendering with modern CSS and JavaScript
- **Database**: PostgreSQL (production), H2 (development) with Liquibase migrations
- **Authentication**: OAuth2 with Google
- **File Processing**: Apache PDFBox for PDF handling, Java AWT for image processing
- **OCR Integration**: ✅ **IMPLEMENTED**
  - OpenAI GPT-4 Vision API for receipt text extraction
  - Claude (Anthropic) Vision API for document analysis
  - Google Gemini Vision API for image processing
  - STUB engine for development with random OCR results when no engines configured
  - Fallback mechanisms with configurable engine selection
  - Asynchronous processing with coroutines
  - Immediate detail view opening after OCR completion
  - Service provider unknown handling with manual input dialog
  - Complete service provider management interface
- **Testing**: JUnit 5, Mockito, comprehensive OCR workflow testing
- **Deployment**: Docker containers with volume mounts

## Future Enhancements

### Planned Features
- **Attachment Management**: Additional file attachments for payments
- **Recurring Payments**: Automated tracking of monthly/yearly bills
- **Reporting & Analytics**: Monthly/yearly spending reports by provider
- **Export Functionality**: CSV/Excel export of payment data
- **Settings Management**: User-configurable OCR engines and paths
- **Notification System**: Reminders for upcoming payments

### Advanced Features
- **Bank Integration**: Automatic transaction import from bank APIs
- **Mobile App**: Companion mobile app for receipt capture
- **AI Enhancements**: Smart categorization and duplicate detection
- **Multi-User**: Household member management and expense splitting

## Benefits

1. **Time Saving**: Automated OCR eliminates manual data entry
2. **Organization**: Centralized storage and categorization of all receipts
3. **Accuracy**: Validation and review processes ensure data quality
4. **Flexibility**: Support for various payment methods and providers
5. **Security**: OAuth2 authentication and secure file storage
6. **Scalability**: Modern architecture supports growing data volumes
7. **Auditability**: Complete transaction history and document trail