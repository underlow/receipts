# Detailed Development Plan

## 1. Project Initialization
1. **Set up project skeleton**
    - ✅Generate a Kotlin/Spring Boot project (Web, Thymeleaf, Spring Data JDBC, OAuth2 Client).
    - Define package structure: `config`, `controller`, `service`, `repository`, `model`, `security`.
    - Configure Gradle/Maven with formatting and static-analysis plugins.

2. **Configure Docker environment**
    - Write a `Dockerfile` to containerize the Spring Boot app.
    - Create `docker-compose.yml` with services:
        - `app` (Kotlin/Spring Boot)
        - `db` (PostgreSQL or SQLite)
        - Volume mounts for `/data/inbox` and `/data/attachments`.
    - Pass OAuth client IDs, OCR API keys, folder paths via environment variables.

3. **Establish application settings mechanism**
    - Implement a settings loader that reads from environment variables and encrypted properties.
    - Define properties for inbox path, attachment path, OCR engines, API keys, and database type.
    - Bind properties into Spring via `@ConfigurationProperties`.

---

## 2. Authentication & Security
4. **Implement OAuth2 login flow**
    - Configure Spring Security for Google OAuth2.
    - Define public routes (login, static) and secured routes (all others).
    - Persist minimal user profile (email, name) on first login.
    - Log each login event with timestamp and user ID.

5. **Audit logging framework**
    - Add an interceptor or aspect to log all create/update/delete on Receipts, Payments, Service Providers, Attachments.
    - Store logs with: user ID, entity type, operation, timestamp, before/after data.

---

## 3. Core Domain Model
6. **Define domain entities**
    - Create model classes for `ServiceProvider`, `PaymentMethod`, `Receipt`, `Payment`, `Attachment`.
    - Annotate JPA relationships (e.g. `@OneToMany`, `@ManyToOne`).

7. **Implement repository layer**
    - Define repository interfaces for each entity.
    - Add custom queries for filtering by provider, date ranges, payment status, recurrence.

---

## 4. File Ingestion & Inbox
8. **Folder-watcher service**
    - Develop a scheduled task that polls the inbox directory every 30 seconds.
    - Detect new files (by filename or checksum) and move to app storage.
    - Create a `Receipt` entity in “Pending” status with file metadata.

9. **Web-UI upload endpoint**
    - Build a multipart upload REST endpoint.
    - On upload, save file to the same storage area and create a matching `Receipt` record.
    - Return success/failure feedback to the user.

---

## 5. OCR Integration
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

## 6. Inbox Review & Approval UI
13. **Inbox list page**
    - Create a Thymeleaf template showing a paginated table with: thumbnail, filename, upload date, guessed provider, OCR engine, status badge.

14. **Receipt detail view**
    - Design a split-pane template:
        - **Left pane**: zoomable receipt image viewer
        - **Right pane**: form with OCR-populated fields (provider, method, amount, dates, recurrence, custom fields)
    - Add “Accept” (approve & convert to Payment) and “Save Draft” (persist edits) buttons.

---

## 7. Payments Module
15. **Approve-to-Payment flow**
    - In controller, map approved `Receipt` data into a new `Payment` entity, linking back to the original receipt.

16. **Payments table view**
    - Build an Excel-style grid with sortable columns: Provider, Method, Amount, Currency, Invoice Date, Payment Date, Recurrent
    - Implement server-side pagination and sorting.

17. **Search & filter controls**
    - Add dropdowns, date-pickers, and checkboxes above the grid to filter by provider, method, date range, status, and recurrence.

18. **Bulk actions**
    - Enable multi-select rows for actions: mark paid/unpaid and trigger export.

---

## 8. Recurring Payments & Tabs
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

## 9. Attachments Feature
23. **Attachment upload UI**
    - In Payment detail, add “Add Attachment” to open a file picker for PDFs/images.
    - On upload, store file in attachments folder and create an `Attachment` record with a comment field.

24. **Attachments list & editing**
    - Display attachments as thumbnails with filename, upload date, and comment.
    - Allow inline editing of comments and deletion (soft-archive record, keep the file).

---

## 10. Reporting & Export
25. **Reporting service**
    - Implement a service to aggregate monthly and yearly spend by provider, returning tabular data.

26. **Reports UI**
    - Create pages for “Monthly Spend by Provider” and “Yearly Spend by Provider,” with tables and totals.

27. **Export functionality**
    - Add an “Export” button on any table or report to stream CSV or Excel, respecting current filters and sort order.

---

## 11. Settings & Configuration UI
28. **General settings page**
    - Build a form for configuring inbox/attachment paths, selecting the active OCR engine, and updating API keys.

29. **Database selection toggle**
    - Provide a switch between SQLite (dev) and PostgreSQL (prod), with a migration warning.

---

## 12. Testing & Validation
30. **Unit tests**
    - Write tests for services, controllers, and repositories, mocking external dependencies (OCR APIs, file system).

31. **Integration tests**
    - Develop end-to-end tests simulating file ingestion, OCR processing (stubbed), review approval, and payment workflows.

32. **UI tests**
    - Automate key user journeys (upload receipt, approve, filter payments, create tabs, export) using a headless browser tool.

---

## 13. CI/CD Pipeline Development
33. **Build & test workflow**
    - Configure GitHub Actions to compile the project, run tests, and build Docker images on each push to `main`.

34. **Docker publish step**
    - Tag and push successful builds to a container registry using semantic versioning.

35. **Staging deployment job**
    - Automate deployment to a staging environment post-publish, with a health-check validation step.
