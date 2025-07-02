# Changelog

## 2025-07-02

### Folder-Watcher Service Implementation (Item 8)
- **Automatic File Ingestion**: Implemented background service for seamless receipt/bill file processing
  - `FileWatcherService`: Scheduled task polling inbox directory every 30 seconds with `@Scheduled` annotation
  - `FileProcessingService`: Core file operations including checksum calculation, duplicate detection, file movement
  - `IncomingFile` entity: New data model for files detected in inbox with PENDING status for OCR workflow
  - `SchedulingConfig`: Spring configuration enabling scheduled task support

- **Smart File Management**:
  - **Duplicate Detection**: SHA-256 checksum-based duplicate prevention to avoid processing same files
  - **Organized Storage**: Files stored in format `/attachments/yyyy-MM-dd-filename` with date prefixes
  - **Automatic Conflict Resolution**: Incremental suffixes (-1, -2, etc.) for filename duplicates
  - **File Validation**: Support for PDF, JPG, JPEG, PNG, GIF, BMP, TIFF with readability checks
  - **Error Handling**: Comprehensive logging and graceful handling of locked files, permissions, corrupted files

- **Database Integration**:
  - Created `IncomingFile` entity with validation annotations and Spring Data JDBC compatibility
  - Added `IncomingFileRepository` interface and JdbcTemplate implementation following existing patterns
  - Database migration (003-incoming-files.sql) with proper constraints, indexes, and foreign keys
  - Updated `JdbcConfig` to register new repository bean

- **Comprehensive Testing**:
  - Unit tests for `IncomingFile` entity creation and validation
  - Service tests for `FileProcessingService` functionality including checksum calculation and path generation
  - Integration tests for `FileWatcherService` workflow including duplicate handling and file processing
  - Full test coverage with TDD approach ensuring reliability

- **Technical Features**:
  - **Path Format**: Changed from hierarchical `/yyyy/MM/dd/` to flat `/yyyy-MM-dd-filename` structure
  - **Concurrent Safety**: Thread-safe file operations with proper locking and atomic operations
  - **Configuration Driven**: Uses `ReceiptsProperties` for configurable inbox and attachments paths
  - **Spring Integration**: Proper bean lifecycle management and dependency injection
  - **Production Ready**: Robust error handling, logging, and monitoring capabilities

### Domain Entities Implementation (Item 6)
- **Core Domain Model Complete**: Implemented comprehensive entity model for receipt processing application
  - `ServiceProvider`: Manage companies/services with name, category, default payment method, active status, comments
  - `PaymentMethod`: Track payment types (CARD/BANK/CASH/OTHER) with name and comments
  - `Bill`: Store uploaded receipt documents with filename, file path, upload date, processing status, OCR data, extracted information
  - `Receipt`: Represent individual expense items with optional bill association for flexible organization
  - `Payment`: Finalized transactions with service provider, payment method, amounts, currency, dates, bill reference

- **Enum Classes for Type Safety**:
  - `BillStatus`: PENDING, PROCESSING, APPROVED, REJECTED workflow states
  - `PaymentMethodType`: CARD, BANK, CASH, OTHER payment categorization

- **Repository Layer with Spring Data JDBC**:
  - Created repository interfaces for all entities with CRUD operations
  - Implemented repository classes using JdbcTemplate following existing patterns
  - Updated `JdbcConfig` to register all new repository beans as Spring components
  - Maintained consistency with existing `UserRepository` and `LoginEventRepository` patterns

- **Database Schema & Migration**:
  - Created comprehensive Liquibase migration (002-domain-entities.sql)
  - Added all entity tables with proper constraints, foreign keys, and indexes
  - Enforced data integrity with CHECK constraints and NOT NULL requirements
  - Updated master changelog to include new migration
  - Optimized for PostgreSQL with proper data types and performance indexes

- **Validation & Data Integrity**:
  - Added Jakarta Bean Validation annotations (@NotBlank, @NotNull, @Positive)
  - Added `spring-boot-starter-validation` dependency for validation support
  - Implemented business rules (positive amounts, required fields, valid relationships)
  - Ensured null safety and proper constraint enforcement

- **Test-Driven Development**:
  - Followed TDD approach with red-green-refactor cycle
  - Created comprehensive unit tests for all domain entities
  - Verified enum functionality and validation constraints
  - Added integration tests for entity creation and relationships
  - All tests passing with 100% coverage for new domain model

- **Technical Implementation**:
  - Used Kotlin data classes for immutable, type-safe entities
  - Implemented Interface + Implementation pattern for repositories
  - Manual SQL queries with JdbcTemplate for performance and control
  - Spring Data JDBC compatibility (no JPA annotations)
  - Proper foreign key relationships and cascade handling

## 2025-07-02

### OAuth2 Authentication & User Management
- **Implemented complete OAuth2 login flow with Google**:
    - Configured `application.yaml` and `application-test.yaml` for Google OAuth2
    - Created `SecurityConfig.kt` for Spring Security setup with proper OIDC configuration
    - Fixed `CustomOAuth2UserService` to extend `OidcUserService` for Google OAuth2 compatibility
    - Added `/dashboard` route and created `dashboard.html` template
    - Fixed redirect loops by configuring proper success/logout URLs
    - Implemented user and login event persistence in database

### Database Schema & Repository Layer
- **Database schema with timestamp defaults**:
    - Created `users` table with `id`, `email`, `name`, `created_at`, `last_login_at`
    - Created `login_events` table with `id`, `user_id`, `timestamp`, `ip_address`
    - Added `DEFAULT CURRENT_TIMESTAMP` for all timestamp fields
    - Fixed `GeneratedKeyHolder` issues by using database-generated timestamps
- **Repository implementation with JdbcTemplate**:
    - Implemented `UserRepositoryImpl` and `LoginEventRepositoryImpl` using `JdbcTemplate`
    - Fixed key extraction to use `keyHolder.keyList.firstOrNull()?.get("id")`
    - Optimized INSERT statements to only include necessary fields
    - Added IP address tracking for login events
- **Liquibase integration**:
    - Configured Liquibase for database schema management
    - Created SQL-based changesets for PostgreSQL compatibility
    - Added proper foreign key constraints

### Configuration & Infrastructure
- **Spring Security & OAuth2**:
    - Configured OIDC user service integration
    - Added proper endpoint permissions (`/login`, `/static/**`, `/error`)
    - Set up logout functionality with proper redirect handling
- **Database configuration**:
    - Primary: PostgreSQL (production)
    - Test: H2 in-memory database with OAuth2 test credentials
    - Proper JDBC configuration with `JdbcConfig` bean definitions

### Features Implemented
- **User Authentication**: Google OAuth2 login with automatic user provisioning
- **Session Management**: Login event logging with timestamps and IP addresses  
- **Security**: Protected routes requiring authentication except login/static resources
- **Dashboard**: Basic welcome page after successful login with logout functionality

### Technical Improvements
- Fixed database schema compatibility issues
- Resolved `GeneratedKeyHolder` multiple keys error
- Added comprehensive error handling and debugging
- Proper separation of concerns with repository pattern