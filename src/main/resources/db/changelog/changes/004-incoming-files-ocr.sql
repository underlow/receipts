--liquibase formatted sql

--changeset incoming-files-ocr:1
-- Add OCR processing fields to incoming_files table

-- Add OCR result fields to incoming_files table
ALTER TABLE incoming_files ADD COLUMN ocr_raw_json TEXT;
ALTER TABLE incoming_files ADD COLUMN extracted_amount DECIMAL(10,2);
ALTER TABLE incoming_files ADD COLUMN extracted_date DATE;
ALTER TABLE incoming_files ADD COLUMN extracted_provider VARCHAR(255);
ALTER TABLE incoming_files ADD COLUMN ocr_processed_at TIMESTAMP;
ALTER TABLE incoming_files ADD COLUMN ocr_error_message TEXT;

-- Indexes for OCR-related queries
CREATE INDEX idx_incoming_files_ocr_processed_at ON incoming_files (ocr_processed_at);
CREATE INDEX idx_incoming_files_extracted_provider ON incoming_files (extracted_provider);
CREATE INDEX idx_incoming_files_extracted_date ON incoming_files (extracted_date);