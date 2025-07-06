# Code Documentation

## Technology Stack
- **Language**: Kotlin 1.9.25
- **Framework**: Spring Boot 3.5.3
- **Database**: PostgreSQL (production), H2 (development/testing)
- **Authentication**: OAuth2 with Google
- **Frontend**: Thymeleaf server-side rendering
- **OCR Integration**: OpenAI GPT-4 Vision, Claude Vision, Google Gemini, STUB (development)
- **Build Tool**: Gradle (Kotlin DSL)
- **Testing**: JUnit 5, Mockito, Selenide for E2E tests

## Package Structure

### Application Entry Point
- **`ReceiptApplication.kt`** - Main Spring Boot application class with @SpringBootApplication annotation

### Configuration Layer (`config/`)
- **`JdbcConfig.kt`** - Database configuration with connection pooling and transaction management
- **`OcrConfig.kt`** - OCR engines configuration for OpenAI, Claude, Google, and STUB services
- **`ReceiptsProperties.kt`** - Custom application properties binding with @ConfigurationProperties
- **`SchedulingConfig.kt`** - Background task scheduling configuration with @EnableScheduling
- **`SecurityConfig.kt`** - OAuth2 security configuration with Google authentication

### Controller Layer (`controller/`)
- **`BillController.kt`** - Bill management endpoints (web + API) with approval/rejection workflow
- **`FileServingController.kt`** - Secure file serving with thumbnail generation and user-scoped access
- **`FileUploadController.kt`** - File upload functionality with drag-and-drop support and progress tracking
- **`InboxController.kt`** - Inbox management with tabbed interface (NEW, APPROVED, REJECTED)
- **`LoginController.kt`** - Authentication endpoints and OAuth2 flow management
- **`ProgressModalController.kt`** - Real-time progress tracking endpoints for synchronous OCR processing
- **`ReceiptController.kt`** - Receipt management endpoints with bill association features
- **`ServiceProviderController.kt`** - Service provider management API endpoints with full CRUD operations

### Service Layer (`service/`)

#### Core Business Logic
- **`BillService.kt`** - Bill entity operations, approval workflow, and status management
- **`ReceiptService.kt`** - Receipt operations, bill associations, and conversion logic
- **`InboxService.kt`** - Tabbed inbox operations combining IncomingFile, Bill, Receipt entities
- **`IncomingFileService.kt`** - File ingestion, initial processing, and entity lifecycle
- **`EntityConversionService.kt`** - Seamless conversion between IncomingFile ↔ Bill ↔ Receipt

#### File Processing
- **`FileProcessingService.kt`** - File handling, storage, and organization with date-prefixed naming
- **`FileWatcherService.kt`** - Automated folder monitoring with @Scheduled annotation (30-second intervals)
- **`FileDispatchService.kt`** - OCR result processing and entity creation from JSON responses
- **`ThumbnailService.kt`** - On-demand thumbnail generation for images and PDF placeholders
- **`ProgressTrackingService.kt`** / **`ProgressTrackingServiceImpl.kt`** - Real-time progress tracking for synchronous OCR processing

#### OCR Integration
- **`OcrService.kt`** - Multi-engine OCR orchestration with fallback mechanisms
- **`IncomingFileOcrService.kt`** - OCR workflow management and status tracking
- **`OcrAttemptService.kt`** - OCR history, audit trails, and retry logic
- **`ocr/`** - OCR engine implementations:
  - **`OpenAiOcrEngine.kt`** - OpenAI GPT-4 Vision integration
  - **`ClaudeOcrEngine.kt`** - Claude Vision integration
  - **`GoogleOcrEngine.kt`** - Google Gemini Vision integration
  - **`StubOcrEngine.kt`** - STUB engine for development with random results

#### Supporting Services
- **`UserService.kt`** - User management with OAuth2 integration
- **`PaymentService.kt`** - Payment creation from approved items
- **`ServiceProviderService.kt`** - Service provider management and categorization
- **`PaymentMethodService.kt`** - Payment method management
- **`CustomOAuth2UserService.kt`** - OAuth2 user provisioning and profile management

### Model Layer (`model/`)

#### Core Domain Entities
- **`User.kt`** - User management with OAuth2 integration and session tracking
- **`IncomingFile.kt`** - Files detected by folder-watcher service with OCR status
- **`Bill.kt`** - Processed bill documents with service provider associations
- **`Receipt.kt`** - Receipt documents with optional bill associations
- **`Payment.kt`** - Finalized payment records with amounts and dates
- **`ServiceProvider.kt`** - Companies/services being paid with custom fields
- **`PaymentMethod.kt`** - Payment methods (cards, bank transfers, etc.)
- **`OcrAttempt.kt`** - OCR processing history and audit with JSON results
- **`BillStatus.kt`** - Status enumeration (NEW, PROCESSING, APPROVED, REJECTED)

### Repository Layer (`repository/`)

#### Data Access with Spring Data JDBC
- **`UserRepository.kt`** / **`UserRepositoryImpl.kt`** - User data access with OAuth2 profile management
- **`IncomingFileRepository.kt`** / **`IncomingFileRepositoryImpl.kt`** - File ingestion data access
- **`BillRepository.kt`** / **`BillRepositoryImpl.kt`** - Bill entity data access with status filtering
- **`ReceiptRepository.kt`** / **`ReceiptRepositoryImpl.kt`** - Receipt data access with bill associations
- **`PaymentRepository.kt`** / **`PaymentRepositoryImpl.kt`** - Payment data access with provider filtering
- **`ServiceProviderRepository.kt`** / **`ServiceProviderRepositoryImpl.kt`** - Service provider data access
- **`PaymentMethodRepository.kt`** / **`PaymentMethodRepositoryImpl.kt`** - Payment method data access
- **`OcrAttemptRepository.kt`** / **`OcrAttemptRepositoryImpl.kt`** - OCR history data access

### DTO Layer (`dto/`)

#### Data Transfer Objects
- **`BillDetailDto.kt`** - Complete bill information with OCR data and associations
- **`ReceiptDetailDto.kt`** - Receipt information with bill associations
- **`InboxTabDto.kt`** - Unified inbox interface data for tabbed view
- **`PaymentDetailDto.kt`** - Payment information with provider details
- **`ServiceProviderDto.kt`** - Service provider data transfer objects including ServiceProviderDto, ServiceProviderFormDto, ServiceProviderOperationResponse, ServiceProvidersByCategory, ServiceProviderOption, and ActiveServiceProviderDto
- **`IncomingFileDetailDto.kt`** - File details with OCR status and metadata
- **`ProgressStatusResponse.kt`** - Real-time progress status for OCR processing modal
- **`ProgressUpdateRequest.kt`** - Internal progress update requests for OCR tracking
- **`ErrorResponse.kt`** - Standardized error responses for API endpoints

### Database Schema (`src/main/resources/db/changelog/`)

#### Liquibase Migrations
- **`001-initial-schema.sql`** - Basic user and authentication tables
- **`002-domain-entities.sql`** - Core domain model (bills, receipts, payments)
- **`003-incoming-files.sql`** - File ingestion system tables
- **`004-incoming-files-ocr.sql`** - OCR integration tables and audit
- **`005-enhanced-workflow.sql`** - Enhanced status-based operations
- **`006-fix-bills-ocr-columns.sql`** - OCR column corrections and indexing
- **`007-rename-bill-status-to-item-status.sql`** - Status standardization across entities

### Test Structure (`src/test/kotlin/`)

#### Unit Tests
- **`model/`** - Domain entity validation and business logic tests
- **`service/`** - Business logic testing with mocking (Mockito)
- **`repository/`** - Data access layer testing with test containers
- **`controller/`** - Web layer testing with MockMvc and security contexts

#### Integration Tests
- **`e2e/`** - Complete workflow testing with Selenide
- **`oauth2/`** - Authentication flow testing with WireMock
- **`file/`** - End-to-end file handling and OCR integration tests

#### Test Configuration
- **`TestSecurityConfig.kt`** - Security configuration for tests
- **`MockOAuth2Config.kt`** - OAuth2 mocking for unit tests
- **`WireMockOAuth2Config.kt`** - External OAuth2 service mocking

### Build and Configuration

#### Build Configuration
- **`build.gradle.kts`** - Kotlin DSL build configuration with Spring Boot dependencies
- **`gradle.properties`** - Build properties and JVM settings
- **`settings.gradle.kts`** - Project settings and plugin management

#### Application Configuration
- **`application.yaml`** - Main application configuration (database, OAuth2, file upload)
- **`application-dev.yaml`** - Development profile configuration
- **`application-test.yaml`** - Test profile configuration

### Right Panel Table Component

#### Overview
The right panel table component provides a unified interface for displaying and managing entities (IncomingFile, Bill, Receipt, Payment) with advanced sorting, filtering, and bulk action capabilities.

#### Features
- **Unified Table Display**: Replaces the previous grid layout with a comprehensive table view
- **Sortable Columns**: Type, Name, Provider, Amount, Date, Status (client-side sorting)
- **Filterable Columns**: Type, Provider, Status with real-time filtering
- **Row Selection**: Single and bulk selection with visual feedback
- **Bulk Actions**: Approve, reject, and delete multiple items simultaneously
- **Responsive Design**: Mobile-friendly table that adapts to different screen sizes
- **Loading States**: Loading indicators and empty state messages

#### Table Columns
| Column | Data Source | Sortable | Filterable | Description |
|--------|-------------|----------|------------|-------------|
| Selection | N/A | No | No | Checkbox for item selection |
| Type | type | Yes | Yes | Entity type badge (File/Bill/Receipt/Payment) |
| Thumbnail | thumbnailUrl | No | No | Image preview with modal view |
| Name | filename | Yes | Yes | File name with font weight styling |
| Provider | extractedProvider | Yes | Yes | Service provider from OCR data |
| Amount | extractedAmount | Yes | Yes | Monetary amount (formatted as currency) |
| Date | uploadDate | Yes | Yes | Upload/creation date |
| Status | statusDisplayName | Yes | Yes | Entity status badge |
| Actions | actions | No | No | Available operations (View, Edit, Delete, etc.) |

#### Technical Implementation
- **HTML Structure**: Semantic table with thead/tbody organization
- **CSS Styling**: Responsive table with hover states and selection highlighting
- **JavaScript Functions**:
  - `renderTableRows()`: Populates table with filtered and sorted data
  - `applyFilters()`: Real-time filtering based on user inputs
  - `sortTable()`: Client-side sorting with direction indicators
  - `toggleSelectAll()`: Bulk selection management
  - `bulkAction()`: Execute operations on selected items
  - `setupResponsiveTable()`: Mobile adaptation logic

#### Responsive Behavior
- **Desktop**: Full table with all columns visible
- **Tablet**: Hides thumbnail and provider columns
- **Mobile**: Further reduces to essential columns (name, status, actions)
- **Action Buttons**: Stack vertically on small screens

#### Integration Points
- **Data Source**: `/inbox/api/sidebar-tabs/{tabName}` endpoint
- **Bulk Operations**: `/inbox/api/tabs/items/{itemId}/{action}` endpoints
- **Entity-Specific Actions**: Routes to detail views and file operations
- **Real-time Updates**: Refreshes data after operations complete

### Progress Modal Component

#### Overview
The progress modal component provides real-time feedback during synchronous OCR processing with a non-closable modal dialog that prevents user interruption during critical processing stages.

#### Features
- **Synchronous OCR Processing**: Modal dialog displays during file upload and OCR processing
- **Real-time Progress Updates**: Progress bar and stage indicators update every second via API polling
- **Non-closable During Processing**: Modal cannot be closed by clicking outside or pressing ESC while processing
- **Success/Failure Handling**: Clear success messages and detailed error reporting
- **Retry Functionality**: Failed processing can be retried with one-click retry button
- **Auto-close Success**: Successful processing automatically closes modal after 2 seconds

#### Progress Stages
| Stage | Progress % | Description |
|-------|------------|-------------|
| Uploading | 10% | File upload to server |
| File uploaded, starting OCR | 30% | File saved, OCR initialization |
| File Validation | 40% | File format and content validation |
| Image Preprocessing | 50% | Image enhancement for OCR |
| OCR Processing | 60-80% | AI engine processing (OpenAI/Claude/Google) |
| Data Extraction | 90% | Structured data extraction from OCR results |
| Completed | 100% | Processing finished successfully |

#### Technical Implementation
- **Frontend**: Modal dialog with CSS animations and real-time progress updates
- **Backend**: `/api/progress/status/{fileId}` endpoint for progress polling
- **State Management**: In-memory progress tracking with ConcurrentHashMap
- **Error Handling**: Retry logic with `/api/progress/retry/{fileId}` endpoint
- **Security**: User-scoped progress tracking with authentication validation

#### Modal States
- **Processing State**: Spinner animation, progress bar, non-closable
- **Success State**: Green checkmark, success message, auto-close timer
- **Error State**: Red error icon, error message, retry/close buttons
- **Retry State**: Reset to processing state with retry attempt tracking

#### API Endpoints
- **GET** `/api/progress/status/{fileId}` - Get current progress status
- **POST** `/api/progress/retry/{fileId}` - Retry failed OCR processing

#### User Experience Flow
1. User uploads file via drag-and-drop or file picker
2. Modal opens immediately with progress bar at 10%
3. Real-time updates show current processing stage
4. On success: Green success message, auto-close after 2 seconds
5. On failure: Red error message with retry option
6. User can retry processing or close modal to return to upload page

### Service Provider Management Component

#### Overview
The service provider management component provides a comprehensive interface for creating, editing, and managing service providers from within the Bill and Receipt detail views. It includes a modal dialog with full CRUD operations and real-time filtering.

#### Features
- **Modal Dialog Interface**: Non-intrusive overlay accessible from "Edit Providers" buttons in detail views
- **Full CRUD Operations**: Create, read, update, delete, activate, and deactivate service providers
- **Real-time Filtering**: Filter by category, status, and search by name with instant results
- **Category Management**: Auto-complete category input with datalist of existing categories
- **Status Management**: Toggle providers between active and inactive states
- **Form Validation**: Client-side validation with server-side error handling
- **Responsive Design**: Mobile-friendly interface that adapts to different screen sizes

#### Technical Implementation
- **Frontend**: HTML modal dialog with CSS styling and JavaScript for dynamic interactions
- **Backend**: RESTful API endpoints in ServiceProviderController for all operations
- **State Management**: Real-time updates to dropdown selections in parent views
- **Security**: User authentication validation for all operations
- **Error Handling**: Comprehensive error handling with user-friendly messages

#### API Endpoints
- **GET** `/service-providers/api/all` - Get all service providers
- **GET** `/service-providers/api/by-category` - Get providers grouped by category
- **GET** `/service-providers/api/categories` - Get all unique categories
- **POST** `/service-providers/api/create` - Create new service provider
- **PUT** `/service-providers/api/{providerId}` - Update existing service provider
- **DELETE** `/service-providers/api/{providerId}` - Delete service provider
- **POST** `/service-providers/api/{providerId}/activate` - Activate service provider
- **POST** `/service-providers/api/{providerId}/deactivate` - Deactivate service provider

#### Integration Points
- **Bill Detail View**: "Edit Providers" button next to service provider dropdown
- **Receipt Detail View**: "Edit Providers" button next to service provider dropdown
- **Data Refresh**: Automatic refresh of parent view dropdowns after management operations
- **Template Inclusion**: Reusable dialog component included via Thymeleaf fragments

#### User Experience Flow
1. User accesses Bill or Receipt detail view
2. User clicks "Edit Providers" button next to service provider dropdown
3. Modal dialog opens with current providers and management interface
4. User can filter, search, create, edit, or delete providers as needed
5. Changes are immediately reflected in the backend database
6. Parent view dropdown is automatically refreshed with updated provider list
7. User closes modal to return to detail view with updated provider options
