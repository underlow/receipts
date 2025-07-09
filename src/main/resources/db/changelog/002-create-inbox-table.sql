--liquibase formatted sql

--changeset underlow:002-create-inbox-table
--comment: Create inbox table for tracking uploaded receipts and bills
CREATE TABLE inbox (
    id VARCHAR PRIMARY KEY,
    uploaded_image VARCHAR NOT NULL,
    upload_date TIMESTAMP NOT NULL,
    ocr_results TEXT,
    linked_entity_id VARCHAR,
    linked_entity_type VARCHAR,
    state VARCHAR NOT NULL DEFAULT 'CREATED',
    failure_reason TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);

-- Create indexes for common query patterns
CREATE INDEX idx_inbox_state ON inbox(state);
CREATE INDEX idx_inbox_upload_date ON inbox(upload_date);
CREATE INDEX idx_inbox_linked_entity ON inbox(linked_entity_id, linked_entity_type);

--rollback DROP TABLE inbox;