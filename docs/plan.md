# Detailed Development Plan

## Project Initialization
1. **Set up project skeleton**
    - ✅Generate a Kotlin/Spring Boot project (Web, Thymeleaf, Spring Data JDBC, OAuth2 Client).
    - Define package structure: `config`, `controller`, `service`, `repository`, `model`, `security`.

2. **Configure Docker environment**
    - ✅Write a `Dockerfile` to containerize the Spring Boot app.
    - ✅Create `docker-compose.yml` with services:
        - `app` (Kotlin/Spring Boot)
        - `db` (PostgreSQL or SQLite)
        - ✅Volume mounts for `/data/inbox` and `/data/attachments`.
    - ✅Pass OAuth client IDs, OCR API keys, folder paths via environment variables.

3. **Establish application settings mechanism**
    - ✅Implement a settings loader that reads from environment variables and encrypted properties.
    - ✅Define properties for inbox path, attachment path, OCR engines, API keys, and database type.
    - ✅Bind properties into Spring via `@ConfigurationProperties`.

---

## Authentication & Security
4. **Implement OAuth2 login flow**
    - ✅Configure Spring Security for Google OAuth2.
    - ✅Define public routes (login, static) and secured routes (all others).
    - ✅Persist minimal user profile (email, name) on first login.
    - ✅Log each login event with timestamp and user ID.
    - ✅implement a minimal login web page 

## Database Management

36. **Replace Spring Data JPA with Spring Data JDBC and integrate Liquibase**
    - ✅Update `build.gradle.kts` to include `liquibase-core`.
    - ✅Adjust Model Classes (`User.kt`, `LoginEvent.kt`) for JDBC compatibility.
    - ✅Configure Liquibase in `application.yaml` and `application-test.yaml`.
    - ✅Create Liquibase Changelog Files (`db.changelog-master.yaml` and `001-initial-schema.yaml`).
    - ✅Remove `schema.sql` for the main application.
    - ✅Rewrote Liquibase changelog from YAML to SQL.
37. **Rework repositories to use JdbcTemplate**
    - ✅Modified `UserRepository` and `LoginEventRepository` interfaces.
    - ✅Created `UserRepositoryImpl` and `LoginEventRepositoryImpl` using `JdbcTemplate`.
    - ✅Removed `@Repository` annotations from implementation classes.
    - ✅Removed `@Table` and `@Id` annotations from model classes.
    - ✅Updated `build.gradle.kts` to use `spring-boot-starter-jdbc`.
    - ✅Created `JdbcConfig` to define repository beans.
    - ✅Configured `ReceiptApplication` and `ReceiptApplicationTests` to import `JdbcConfig`.

## Core Domain Model

6. **Define domain entities** ✅
    - ✅Create `ServiceProvider` entity with fields: id, name, category, defaultPaymentMethod, isActive, comment
    - ✅Create `PaymentMethod` entity with fields: id, name, type (CARD/BANK/CASH/OTHER), comment
    - ✅Create `Bill` entity with fields: id, filename, filePath, uploadDate, status (PENDING/PROCESSING/APPROVED/REJECTED), ocrRawJson, extractedAmount, extractedDate, extractedProvider, userId
    - ✅Create `Receipt` entity with fields: id, userId (representing the aggregated expense, potentially composed of one or more Receipts), billId (nullable FK to Bill)
    - ✅Create `Payment` entity with fields: id, serviceProviderId, paymentMethodId, amount, currency, invoiceDate, paymentDate, billId, userId, comment
    - ✅Define relationships: Receipt -> Bill (one-to-many), Bill -> User, Payment -> ServiceProvider/PaymentMethod/Bill/User, Attachment -> Payment/User
    - ✅Add validation annotations for required fields and constraints
    - ✅Ensure all entities are compatible with Spring Data JDBC (no JPA annotations)

7. **Implement repository layer** ✅
    - ✅Define repository interfaces for each entity.
    - ✅Add basic CRUD queries.
    - ✅Implement repository classes using JdbcTemplate following existing patterns.
    - ✅Update JdbcConfig to register all new repository beans.

---

## File Ingestion & Inbox
8. **Folder-watcher service** ✅
    - ✅Develop a scheduled task that polls the inbox directory every 30 seconds.
    - ✅Detect new files (by filename or checksum) and move to app storage.
    - ✅Create a `IncomingFile` entity in “Pending” status with file metadata. It will become either bill or receipt after ocr/accept

9. **Web-UI upload endpoint** ✅
    - ✅Build a multipart upload REST endpoint.
    - ✅On upload, save file to the same storage area and create a matching `IncomingFile` record.
    - ✅Return success/failure feedback to the user.
    - ✅Create upload page template with drag-and-drop functionality.
    - ✅Add controller mapping for upload page (/upload).

## Inbox Review & Approval UI
13. **Inbox list page** ✅
    - ✅Create a Thymeleaf template showing a paginated grid with: thumbnail, filename, upload date, status badge for `IncomingFile`s
    - ✅Implement file serving controller with secure access and thumbnail generation
    - ✅Add status filtering (PENDING, PROCESSING, APPROVED, REJECTED) with live counts
    - ✅Implement sorting by filename, upload date, and status
    - ✅Add AJAX file operations (approve, reject, delete) with user feedback
    - ✅Create modal image viewer with keyboard shortcuts
    - ✅Fix critical bug with null status counts causing dashboard crashes

14. **Receipt/Bill detail view** ✅
    - ✅Design a split-pane template for `Receipt` detail view:
        - ✅**Left pane**: zoomable receipt image viewer
        - ✅**Right pane**: form with OCR-populated fields (provider, method, amount, dates, recurrence, custom fields)
    - ✅Add “Associate with Bill” (link this receipt to an existing bill or create a new bill), “Accept as Payment” (approve & convert to Payment for standalone receipts), and “Save Draft” (persist edits) buttons.
    - ✅Design a split-pane template for `Bill` detail view:
        - ✅**Left pane**: List of associated receipts with thumbnails (clickable to view Receipt Detail).
        - ✅**Right pane**: Aggregated form fields for the Bill (provider, method, amount, dates, recurrence, custom fields).
    - ✅Add “Accept” (approve & convert to Payment) and “Save Draft” (persist edits) buttons.

36. **IncomingFile detail view** ✅
    - ✅Design a comprehensive detail view template for `IncomingFile`:
        - ✅**Left pane**: Large file viewer with support for images and PDFs
        - ✅**Right pane**: Complete file metadata display with sections for:
          - ✅File Information (filename, upload date, file size, status, checksum)
          - ✅File Actions (approve, reject, delete) with proper status-based permissions
          - ✅Technical Details (file ID, path, type detection)
    - ✅Implement controller endpoints:
        - ✅`GET /inbox/files/{fileId}` - HTML detail view page
        - ✅`GET /inbox/api/files/{fileId}/detail` - JSON API endpoint
    - ✅Add comprehensive test coverage for all detail view functionality
    - ✅Update inbox template to link Detail button to new endpoint
    - ✅Create `IncomingFileDetailDto` with computed fields and action permissions
    - ✅Ensure proper user authentication and file ownership verification

38. **Enhanced Panel Actions for IncomingFile**
    - Remove "Approve File" button from actions panel
    - Move "Send to OCR" button to actions panel (from OCR section)
    - Add "Bill" button that converts IncomingFile to Bill entity in pending state
    - Add "Receipt" button that converts IncomingFile to Receipt entity in pending state
    - Update UI to show current entity type and available actions
    - Implement manual type selection workflow before OCR processing
    - When OCR is triggered with manual type selection, OCR should extract info without guessing type
    - Add revert functionality to convert Bill/Receipt back to IncomingFile if user declines OCR results

39. **Two-Tab Interface for Bill/Receipt Detail Views**
    - When IncomingFile is converted to Bill or Receipt, change UI to show two tabs in left pane:
      - **Bill/Receipt Tab**: Contains editable entity fields prefilled with OCR values
        - Add Accept button to finalize the conversion
        - Add Revert button to convert back to IncomingFile
        - Form fields for provider, amount, date, currency, etc.
      - **Information Tab**: Contains technical file details and metadata
        - File information (filename, upload date, size, checksum)
        - OCR processing history and raw data
        - System technical details
    - Preserve existing image viewer functionality in right pane
    - Update navigation and breadcrumbs to reflect current entity type
    - Ensure proper state management between IncomingFile ↔ Bill/Receipt transitions

40. **Enhanced User Stories Implementation**
    - **IncomingFile Creation**: When user uploads a file, it creates an IncomingFile entity
    - **OCR Processing Options**:
        - Automatic type detection: User sends to OCR, OCR guesses type and extracts info, converts to corresponding entity
        - Manual type selection: User selects Bill/Receipt type, then sends to OCR for info extraction only
    - **OCR Results Handling**: User can accept OCR results or decline and revert back to IncomingFile
    - **Entity State Management**: Clear transitions between IncomingFile → Bill/Receipt → back to IncomingFile
    - **UI State Synchronization**: Interface updates based on current entity type and processing state

41. **OCR Attempts History System**
    - Add OCR attempts history to all entities (IncomingFile, Bill, Receipt)
    - Track: timestamp, OCR engine used, processing status, extracted data, error messages
    - Store raw OCR responses for audit and debugging
    - Display history in Information tab of detail views
    - Allow users to view previous OCR attempts and results
    - Maintain history across entity type transitions (IncomingFile → Bill/Receipt)

42. **UI Navigation Improvements**
    - Remove breadcrumbs from all pages
    - Simplify navigation to focus on main actions
    - Allow users to revert/fix errors at any time without restrictions
    - Update page titles and headers to reflect current entity type


## Workflow Requirements Summary

**File Upload Workflow**:
1. User uploads file → IncomingFile entity created
2. User can either:
    - Click "Send to OCR" → OCR guesses type and extracts info → converts to Bill/Receipt
    - Click "Bill" → converts to Bill in pending state → can send to OCR for info extraction
    - Click "Receipt" → converts to Receipt in pending state → can send to OCR for info extraction
3. If user declines OCR results → entity reverts back to IncomingFile
4. When entity is Bill/Receipt → UI shows two-tab interface (Bill/Receipt tab + Information tab)

**User Interface Changes**:
- Remove "Approve File" button from actions panel
- Move "Send to OCR" button to actions panel
- Add "Bill" and "Receipt" buttons to actions panel
- Implement two-tab interface for Bill/Receipt detail views
- Add Accept/Revert buttons in entity-specific tabs

---

---

## OCR Integration
10. **Define OCR abstraction** ✅
    - ✅Create an `OcrEngine` interface with a method to accept a file and return standardized JSON.
    - ✅Create `OcrResult` data class with standardized fields (provider, amount, date, currency, rawJson).
    - ✅Create `OcrRequest` data class for request validation and configuration.

11. **Implement engine connectors** ✅
    - ✅Build three beans for OpenAI, Claude, and Google AI.
    - Each bean handles authentication with its API key and parses the engine’s JSON response into the common format.

## Additional OCR Implementation Details (Items 10-11 Completed) ✅
- ✅Created `OcrEngine` interface with `processFile()` method
- ✅Implemented `OpenAiOcrEngine` using GPT-4 Vision API
- ✅Implemented `ClaudeOcrEngine` using Anthropic Claude Vision API  
- ✅Implemented `GoogleAiOcrEngine` using Google Gemini Vision API
- ✅Added `OcrConfig` with conditional bean creation based on API key availability
- ✅Created `OcrService` for orchestrating OCR processing with fallback mechanisms
- ✅Added comprehensive test coverage including unit tests for all components
- ✅Added coroutines support for async processing
- ✅Implemented standardized `OcrResult` and `OcrRequest` data classes

12. **Dispatch logic** ✅
    - ✅Upon `IncomingFile` creation, invoke the selected engine bean automatically.
    - ✅Information sent to OCR: prompt, image, set of `ServiceProvider` and `PaymentMethod` as reference data
    - ✅Persist raw JSON and extract core fields (provider, amount, dates, currency).
    - ✅Fallback mechanisms implemented when no API keys are configured.
    - ✅OCR information (provider, date, result, errors) accessible in UI through inbox and detail views
    - ✅Automatic status management through PENDING → PROCESSING → APPROVED/REJECTED workflow
    - ✅Comprehensive error handling with retry mechanisms
    - ✅Dispatch logic to convert approved IncomingFiles to Bill entities
    - ✅Complete UI integration with OCR action buttons ("Send to OCR", "Retry OCR", "Create Bill")
    - ✅Real-time status indicators in inbox grid and detailed OCR section in file detail view
    - ✅Enhanced API endpoints for manual OCR operations and statistics
    

---

## Payments Module
15. **Approve-to-Payment flow**
    - In controller, map approved `Bill` data into a new `Payment` entity, linking back to the original receipt.

16. **Payments table view**
    - Build an Excel-style grid with sortable columns: Provider, Method, Amount, Currency, Invoice Date, Payment Date, Recurrent
    - Implement server-side pagination and sorting.

17. **Search & filter controls**
    - Add dropdowns, date-pickers, and checkboxes above the grid to filter by provider, method, date range, status, and recurrence.

18. **Bulk actions**
    - Enable multi-select rows for actions: mark paid/unpaid and trigger export.

---

## Recurring Payments & Tabs
19. **Sidebar tabs component**
    - Develop a dynamic left sidebar listing user-created tabs and a “Create Tab” button.

20. **Create-tab dialog**
    - Build a modal to select a Service Provider and save a Tab configuration record in the database.

21. **Tab persistence**
    - Load saved tabs on startup and persist user additions/removals across sessions.

22. **Tab view rendering**
    - For each tab, filter the Payments view by provider and render a calendar-style grid or table highlighting months without a payment.
    - Include an option to mark months as “Skipped.”

---

## Attachments Feature
23. **Attachment upload UI**
    - In Payment detail, add “Add Attachment” to open a file picker for PDFs/images.
    - On upload, store file in attachments folder and create an `Attachment` record with a comment field.

24. **Attachments list & editing**
    - Display attachments as thumbnails with filename, upload date, and comment.
    - Allow inline editing of comments and deletion (soft-archive record, keep the file).

---

## Reporting & Export
25. **Reporting service**
    - Implement a service to aggregate monthly and yearly spend by provider, returning tabular data.

26. **Reports UI**
    - Create pages for “Monthly Spend by Provider” and “Yearly Spend by Provider,” with tables and totals.

27. **Export functionality**
    - Add an “Export” button on any table or report to stream CSV or Excel, respecting current filters and sort order.

---

## Settings & Configuration UI
28. **General settings page**
    - Build a form for configuring inbox/attachment paths, selecting the active OCR engine, and updating API keys.

29. **Database selection toggle**
    - Provide a switch between SQLite (dev) and PostgreSQL (prod), with a migration warning.

---

## Testing & Validation
30. **Unit tests**
    - Write tests for services, controllers, and repositories, mocking external dependencies (OCR APIs, file system).

31. **Integration tests**
    - Develop end-to-end tests simulating file ingestion, OCR processing (stubbed), review approval, and payment workflows.

32. **UI tests**
    - Automate key user journeys (upload receipt, approve, filter payments, create tabs, export) using a headless browser tool.

---

## CI/CD Pipeline Development
33. **Build & test workflow**
    - Configure GitHub Actions to compile the project, run tests, and build Docker images on each push to `main`.

34. **Docker publish step**
    - Tag and push successful builds to a container registry using semantic versioning.

35. **Staging deployment job**
    - Automate deployment to a staging environment post-publish, with a health-check validation step.

---

5. **Audit logging framework**
    - Add an interceptor or aspect to log all create/update/delete on Bills, Payments, Service Providers, Attachments.
    - Store logs with: user ID, entity type, operation, timestamp, before/after data.



