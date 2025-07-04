# Code Documentation

## Overview
This is a Household Expense Tracker application built with Kotlin + Spring Boot that automates receipt processing through OCR (Optical Character Recognition) technology. The application follows modern Spring Boot architecture with comprehensive OAuth2 authentication, file management, and AI-powered document processing.

## Technology Stack
- **Language**: Kotlin 1.9.25
- **Framework**: Spring Boot 3.5.3
- **Database**: PostgreSQL (production), H2 (development/testing)
- **Authentication**: OAuth2 with Google
- **Frontend**: Thymeleaf server-side rendering
- **OCR Integration**: OpenAI GPT-4 Vision, Claude Vision, Google Gemini
- **Build Tool**: Gradle (Kotlin DSL)
- **Testing**: JUnit 5, Mockito, Selenide for E2E tests

## Package Structure

### Application Entry Point
- **`ReceiptApplication.kt`** - Main Spring Boot application class with @SpringBootApplication annotation

### Configuration Layer (`config/`)
- **`JdbcConfig.kt`** - Database configuration with connection pooling and transaction management
- **`OcrConfig.kt`** - OCR engines configuration for OpenAI, Claude, and Google services
- **`ReceiptsProperties.kt`** - Custom application properties binding with @ConfigurationProperties
- **`SchedulingConfig.kt`** - Background task scheduling configuration with @EnableScheduling
- **`SecurityConfig.kt`** - OAuth2 security configuration with Google authentication

### Controller Layer (`controller/`)
- **`BillController.kt`** - Bill management endpoints (web + API) with approval/rejection workflow
- **`FileServingController.kt`** - Secure file serving with thumbnail generation and user-scoped access
- **`FileUploadController.kt`** - File upload functionality with drag-and-drop support
- **`InboxController.kt`** - Inbox management with tabbed interface (NEW, APPROVED, REJECTED)
- **`LoginController.kt`** - Authentication endpoints and OAuth2 flow management
- **`ReceiptController.kt`** - Receipt management endpoints with bill association features

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

#### OCR Integration
- **`OcrService.kt`** - Multi-engine OCR orchestration with fallback mechanisms
- **`IncomingFileOcrService.kt`** - OCR workflow management and status tracking
- **`OcrAttemptService.kt`** - OCR history, audit trails, and retry logic
- **`ocr/`** - OCR engine implementations:
  - **`OpenAiOcrEngine.kt`** - OpenAI GPT-4 Vision integration
  - **`ClaudeOcrEngine.kt`** - Claude Vision integration
  - **`GoogleOcrEngine.kt`** - Google Gemini Vision integration

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
- **`ServiceProviderDto.kt`** - Service provider dropdown options
- **`IncomingFileDetailDto.kt`** - File details with OCR status and metadata
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

### Key Architecture Features

#### 1. OCR Processing Workflow
- Multi-engine support with fallback mechanisms
- Automatic processing with status tracking
- Complete audit trail preservation
- Comprehensive error handling and retry logic

#### 2. File Management System
- Automated folder monitoring
- SHA-256 checksum-based duplicate prevention
- Date-prefixed organized storage
- On-demand thumbnail generation
- User-scoped secure access

#### 3. Entity Conversion System
- Seamless IncomingFile ↔ Bill ↔ Receipt transitions
- Data preservation across conversions
- Revert functionality for user corrections
- Status-based operation availability

#### 4. Tabbed Inbox Interface
- NEW tab for all unprocessed items
- APPROVED tab with type filtering
- REJECTED tab with reprocessing options
- Real-time AJAX updates with count tracking

#### 5. Security Implementation
- OAuth2 Google authentication
- User-scoped data access verification
- Session management with login tracking
- File security with controlled access patterns

This codebase represents a comprehensive, production-ready application with modern architectural patterns and extensive testing coverage for household expense tracking through automated document processing.