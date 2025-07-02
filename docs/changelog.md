# Changelog

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