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

#### 2.3 Bills & Document Management
- **Purpose**: Store and process uploaded receipt/bill documents
- **Processing States**: PENDING → PROCESSING → APPROVED/REJECTED
- **OCR Integration**: Automatic extraction of amount, date, and provider information
- **File Storage**: Secure storage with organized file paths
- **Fields**: filename, file path, upload date, status, OCR data, extracted information, user association

#### 2.4 Receipts
- **Purpose**: Represent individual expense items that may be associated with bills
- **Flexibility**: Can exist independently or be linked to bills
- **Organization**: Allows for grouping multiple receipts under a single bill
- **User Association**: Each receipt is tied to a specific user

#### 2.5 Payments
- **Purpose**: Finalized, approved transactions ready for reporting and analysis
- **Comprehensive Tracking**: Service provider, payment method, amounts, dates
- **Multi-Currency Support**: Currency field for international expenses
- **Audit Trail**: Links back to original bills and receipts
- **Fields**: service provider, payment method, amount, currency, invoice date, payment date, bill reference, user, comments

### 3. Workflow & Processing

#### 3.1 Receipt Ingestion
1. **File Upload**: Users can upload receipt images/PDFs through web interface
2. **Folder Watching**: Automated detection of files dropped into watched directories
3. **Bill Creation**: Each uploaded file creates a Bill entity in PENDING status
4. **OCR Processing**: Automatic extraction of key information using AI engines

#### 3.2 OCR & Data Extraction
- **Multi-Engine Support**: OpenAI, Claude, and Google AI integration
- **Configurable**: Users can select preferred OCR engine
- **Smart Extraction**: Automatically identifies provider, amount, dates
- **Raw Data Preservation**: Original OCR JSON stored for reference
- **Status Tracking**: Processing status updates throughout workflow

#### 3.3 Review & Approval Process
1. **Manual Review**: Users can edit and correct OCR-extracted data
2. **Provider Matching**: Associate bills with existing service providers
3. **Payment Method Selection**: Choose how the bill was/will be paid
4. **Receipt Association**: Link multiple receipts to a single bill if needed
5. **Approval**: Convert approved bills into payment records

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
        ├── Bills ──┬── Receipts (optional)
        ├── Receipts    └── Payments (optional)
        └── Payments ──┬── ServiceProviders
                       └── PaymentMethods
```

#### 4.3 Data Validation
- **Required Fields**: Enforced at entity level with validation annotations
- **Business Rules**: Amount must be positive, dates must be valid
- **Referential Integrity**: Foreign key constraints ensure data consistency
- **Type Safety**: Enums for status and payment method types

### 5. File Management
- **Storage Location**: `/data/inbox` for incoming receipts, `/data/attachments` for processed files
- **Security**: Files stored outside web root for security
- **Organization**: Structured file paths for easy management
- **Backup**: Volume-mounted storage for data persistence

### 6. Technology Stack
- **Backend**: Kotlin + Spring Boot
- **Frontend**: Thymeleaf server-side rendering
- **Database**: PostgreSQL (production), H2 (development)
- **Authentication**: OAuth2 with Google
- **OCR**: OpenAI, Claude, Google AI APIs
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