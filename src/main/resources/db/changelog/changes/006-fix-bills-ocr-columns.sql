--liquibase formatted sql

--changeset fix-bills-ocr-columns:1
-- Add missing OCR processing fields to bills table

-- Add OCR result fields to bills table to match the code expectations
ALTER TABLE bills ADD COLUMN ocr_processed_at TIMESTAMP;
ALTER TABLE bills ADD COLUMN ocr_error_message TEXT;

-- Add indexes for performance
CREATE INDEX idx_bills_ocr_processed_at ON bills (ocr_processed_at);