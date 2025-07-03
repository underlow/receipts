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
---

## OCR Integration
10. **Define OCR abstraction**
    - Create an `OcrEngine` interface with a method to accept a file and return standardized JSON.

11. **Implement engine connectors**
    - Build three beans for OpenAI, Claude, and Google AI.
    - Each bean handles authentication with its API key and parses the engine’s JSON response into the common format.

12. **Dispatch logic**
    - Upon `Receipt` creation, invoke the selected engine bean.
    - Persist raw JSON and extract core fields (provider guess, amount, dates).
    - Flag ambiguous or missing provider info for manual review.

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


