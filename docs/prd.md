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

## UI Requirements

## User stories

