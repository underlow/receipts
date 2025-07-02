# Changelog

## 2025-07-02

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