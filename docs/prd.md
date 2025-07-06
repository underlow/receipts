# Project Requirements Document

## 1. Purpose & Scope
**Purpose**  
Enable individuals and families to digitize, review, and manage household (and other) expenses via receipt and bill images, automated OCR extraction, manual approval, and rich reporting.

**Scope**
- Receipt and Bill ingestion (via web UI)
- OCR via OpenAI, Claude, Google AI, or STUB for development
- Manual review & approval for Receipts and Bills
- Association of Receipts with Bills
- Payment record management (one-off & recurring)
- Attachments & comments
- Reporting & exports (CSV/Excel)
- Admin settings (OCR engines, API keys)

## Main User Scenarios & Stories
1. **Ingest Receipt/Bill**
    - *As a user*, I want to upload via UI so that I don’t have to manually enter data.
2. **Review Extraction**
    - *As a user*, I want to edit or approve OCR-extracted data from receipts or bills to ensure accuracy before payment.
3. **Associate Receipt with Bill**
    - *As a user*, I want to associate one or more receipts with a specific bill, or create a new bill from a receipt.


## Entities

### User 
#### User data
 - name 
 - email
 - avatar
#### User state
User do not have states.
#### User behaviour
User can log in into system only if its email in the list of allowed emails. 


## Functional Requirements

### Authentication & Authorization
- System MUST authenticate users via Google OAuth 2.0
- System MUST restrict access to users whose email addresses are in the configured allowlist
- System MUST redirect unauthenticated users to the login page
- System MUST redirect authenticated users to the dashboard upon successful login
- System MUST store user profile information (name, email, avatar) from Google OAuth response
- System MUST validate user email against environment-configured allowlist
- System MUST reject login attempts from non-allowlisted email addresses
- System MUST provide secure session management
- System MUST provide logout functionality

## UI Requirements

### Login Interface
- Login page MUST display application branding (not "Airtable")
- Login page MUST provide "Continue with Google" button
- Login page MUST NOT display email input field or other OAuth providers
- Login page MUST display terms of service and privacy policy links
- Login page MUST be responsive and accessible
- Login page MUST handle authentication errors gracefully

### Dashboard Interface
- Dashboard MUST display as placeholder page for authenticated users
- Dashboard MUST show user profile information
- Dashboard MUST provide logout functionality

## User Stories

### Authentication Stories
- As a user, I want to login with my Google account so that I can access the system securely
- As a user, I want to be redirected to login if I'm not authenticated so that the system remains secure
- As a user, I want to see a clear error message if my email is not authorized so that I understand why I cannot access the system
- As a user, I want to be automatically redirected to the dashboard after successful login so that I can start using the system
- As a user, I want to logout securely so that my session is properly terminated
- As a system administrator, I want to control who can access the system by configuring allowed email addresses

