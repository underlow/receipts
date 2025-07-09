# Code Structure Documentation

## Overview
This document describes the code structure and architecture of the receipt management system.

## Package Structure

### me.underlow.receipt
Main application package containing all business logic.

### me.underlow.receipt.model
Contains entity classes representing the core business domain objects.

#### InboxEntity
Represents an inbox item in the document processing system.
- **File**: `src/main/kotlin/me/underlow/receipt/model/InboxEntity.kt`
- **Purpose**: Tracks the lifecycle of an uploaded document from initial upload through OCR processing to final approval
- **States**: CREATED, PROCESSED, FAILED, APPROVED
- **Key Methods**: 
  - `processOCR()`: Transition to PROCESSED state
  - `failOCR()`: Transition to FAILED state
  - `approve()`: Transition to APPROVED state
  - `retryOCR()`: Retry from FAILED state
  - `canApprove()`, `canRetry()`: Validation methods

#### BillEntity
Represents a bill entity in the financial document management system.
- **File**: `src/main/kotlin/me/underlow/receipt/model/BillEntity.kt`
- **Purpose**: Tracks financial obligations that users need to manage, including bills created from approved inbox items and manually entered bills
- **States**: CREATED, REMOVED
- **Key Methods**:
  - `remove()`: Transition to REMOVED state
  - `updateAmount()`: Update bill amount with validation
  - `updateServiceProvider()`: Update service provider
  - `canRemove()`, `isActive()`: Validation methods
- **Companion Methods**:
  - `createFromInbox()`: Create bill from approved inbox item
  - `createManually()`: Create bill without inbox item

### me.underlow.receipt.controller
Contains Spring MVC controllers handling web requests.

### me.underlow.receipt.service
Contains business logic services and external integrations.

### me.underlow.receipt.dao
Contains data access objects for database operations.

### me.underlow.receipt.config
Contains configuration classes for Spring Security and other framework configurations.

### me.underlow.receipt.dashboard
Contains dashboard-related UI components.

#### BaseTable
Base table component providing common table functionality for all entity views.
- **File**: `src/main/kotlin/me/underlow/receipt/dashboard/BaseTable.kt`
- **Purpose**: Provides reusable table functionality including sorting, pagination, search/filter capabilities, and proper empty state handling
- **Key Methods**:
  - `render()`: Renders HTML table with columns and data
  - `applySorting()`: Applies sorting to data
  - `applyPagination()`: Applies pagination to data
  - `applySearch()`: Applies search filtering to data

#### InboxView
Inbox view component for displaying inbox items in table format.
- **File**: `src/main/kotlin/me/underlow/receipt/dashboard/InboxView.kt`
- **Purpose**: Displays inbox items with OCR status indicators and state-based action buttons
- **Integration**: Uses BaseTable component for common functionality

#### BillsView
Bills view component for displaying bills in table format.
- **File**: `src/main/kotlin/me/underlow/receipt/dashboard/BillsView.kt`
- **Purpose**: Displays bills with amount formatting, service provider display, and state-based action buttons
- **Key Features**:
  - Currency formatting for amounts
  - Service provider name resolution
  - Description truncation with tooltips
  - Creation source indication (inbox vs manual)
  - State-based action buttons (edit/remove for active bills)
- **Integration**: Uses BaseTable component for common functionality
- **Key Methods**:
  - `render()`: Renders bills table view
  - `getTableColumns()`: Defines table columns
  - `convertEntityToRowData()`: Converts BillEntity to table row
  - `formatAmount()`: Formats monetary amounts as currency
  - `formatServiceProvider()`: Formats service provider display
  - `formatDescription()`: Handles description truncation
  - `formatActions()`: Creates state-based action buttons

## Entity Relationships

### Inbox to Bill Relationship
- InboxEntity can be linked to BillEntity through the `inboxEntityId` field
- When an inbox item is approved, it creates a BillEntity with the inbox ID
- This maintains traceability from original upload to final bill

## Testing Structure

### Test Package Structure
Tests follow the same package structure as main code under `src/test/kotlin/me/underlow/receipt/`

### Test Patterns
- **Given-When-Then**: All tests follow this pattern with clear business logic comments
- **Comprehensive Coverage**: Tests cover entity creation, state transitions, validation methods, and edge cases
- **TDD Approach**: Tests are written first, then implementation follows

### Entity Test Files
- `InboxEntityTest.kt`: Comprehensive tests for InboxEntity lifecycle and behavior
- `BillEntityTest.kt`: Comprehensive tests for BillEntity lifecycle and behavior

### Dashboard Component Test Files
- `BaseTableTest.kt`: Tests for BaseTable component functionality
- `InboxViewTest.kt`: Tests for InboxView component rendering and behavior
- `BillsViewTest.kt`: Tests for BillsView component rendering and behavior
  - Tests table column definitions
  - Tests entity to row data conversion
  - Tests amount formatting
  - Tests service provider formatting
  - Tests description truncation
  - Tests action button generation
  - Tests integration with BaseTable functionality

## Key Design Patterns

### Immutable Entities
All entities are implemented as Kotlin data classes with immutable properties:
- State changes create new instances using `copy()`
- Ensures thread safety and predictable behavior
- Follows functional programming principles

### State Management
Entities use enum classes for state management:
- Clear state transitions with validation
- Prevents invalid state changes
- Supports business logic validation

### Validation Methods
Entities provide validation methods like `canApprove()`, `canRemove()`:
- Encapsulate business rules within entities
- Provide clear API for UI layer
- Support consistent behavior across the application

## Static Resources

### Client-Side Libraries
The application includes client-side libraries for enhanced user experience:

#### Cropper.js
Client-side image processing library for crop, resize, and editing functionality.
- **Files**: 
  - `src/main/resources/static/js/cropper.min.js` - JavaScript library (v1.6.2)
  - `src/main/resources/static/css/cropper.min.css` - CSS stylesheet (v1.6.2)
- **Purpose**: Enables image cropping, resizing, rotation, and zoom functionality for uploaded images
- **Features**:
  - Crop with aspect ratio constraints
  - Resize and rotate images
  - Zoom in/out functionality
  - Undo/redo operations
  - Touch and mouse support
- **Template Integration**: 
  - CSS loaded in dashboard template head section
  - JavaScript loaded after Bootstrap but before custom scripts
  - Available globally as `Cropper` constructor
- **Library Info**: MIT License, maintained by fengyuanchen
- **Documentation**: https://github.com/fengyuanchen/cropperjs

### Template Integration
Static resources are included in the main dashboard template:
- CSS files loaded in `<head>` section for proper styling
- JavaScript files loaded before closing `</body>` tag
- Proper loading order: Bootstrap → External Libraries → Custom Scripts

## Build Configuration
- **Build Tool**: Gradle
- **Language**: Kotlin
- **Framework**: Spring Boot
- **Testing**: JUnit 5 with Mockito
- **Database**: Configured with Liquibase migrations