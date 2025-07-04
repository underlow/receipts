--liquibase formatted sql

--changeset enhanced-workflow:1
-- Enhanced workflow for items 38-42: OCR history and extended Bill/Receipt models

-- OCR attempts history table
CREATE TABLE ocr_attempts
(
    id                  BIGSERIAL PRIMARY KEY,
    entity_type         VARCHAR(20)  NOT NULL CHECK (entity_type IN ('INCOMING_FILE', 'BILL', 'RECEIPT')),
    entity_id           BIGINT       NOT NULL,
    attempt_timestamp   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ocr_engine_used     VARCHAR(50)  NOT NULL,
    processing_status   VARCHAR(20)  NOT NULL CHECK (processing_status IN ('SUCCESS', 'FAILED', 'IN_PROGRESS')),
    extracted_data_json TEXT,
    error_message       TEXT,
    raw_response        TEXT,
    user_id             BIGINT       NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users (id)
);

-- Add file metadata fields to bills table
ALTER TABLE bills ADD COLUMN checksum VARCHAR(64);
ALTER TABLE bills ADD COLUMN original_incoming_file_id BIGINT;

-- Add file metadata fields to receipts table  
ALTER TABLE receipts ADD COLUMN filename VARCHAR(500);
ALTER TABLE receipts ADD COLUMN file_path VARCHAR(1000);
ALTER TABLE receipts ADD COLUMN upload_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE receipts ADD COLUMN checksum VARCHAR(64);
ALTER TABLE receipts ADD COLUMN status VARCHAR(20) DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'PROCESSING', 'APPROVED', 'REJECTED'));
ALTER TABLE receipts ADD COLUMN ocr_raw_json TEXT;
ALTER TABLE receipts ADD COLUMN extracted_amount DECIMAL(10, 2);
ALTER TABLE receipts ADD COLUMN extracted_date DATE;
ALTER TABLE receipts ADD COLUMN extracted_provider VARCHAR(255);
ALTER TABLE receipts ADD COLUMN ocr_processed_at TIMESTAMP;
ALTER TABLE receipts ADD COLUMN ocr_error_message TEXT;
ALTER TABLE receipts ADD COLUMN original_incoming_file_id BIGINT;

-- Indexes for performance
CREATE INDEX idx_ocr_attempts_entity ON ocr_attempts (entity_type, entity_id);
CREATE INDEX idx_ocr_attempts_user_id ON ocr_attempts (user_id);
CREATE INDEX idx_ocr_attempts_timestamp ON ocr_attempts (attempt_timestamp);
CREATE INDEX idx_ocr_attempts_status ON ocr_attempts (processing_status);

CREATE INDEX idx_bills_checksum ON bills (checksum);
CREATE INDEX idx_bills_original_file ON bills (original_incoming_file_id);

CREATE INDEX idx_receipts_filename ON receipts (filename);
CREATE INDEX idx_receipts_status ON receipts (status);
CREATE INDEX idx_receipts_upload_date ON receipts (upload_date);
CREATE INDEX idx_receipts_checksum ON receipts (checksum);
CREATE INDEX idx_receipts_original_file ON receipts (original_incoming_file_id);