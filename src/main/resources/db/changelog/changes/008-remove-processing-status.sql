-- Migration to remove PROCESSING status from ItemStatus enum
-- Convert all existing PROCESSING items to NEW status
-- Update CHECK constraints to remove PROCESSING option

-- Update existing PROCESSING records to NEW status in incoming_files table
UPDATE incoming_files 
SET status = 'NEW' 
WHERE status = 'PROCESSING';

-- Update existing PROCESSING records to NEW status in bills table
UPDATE bills 
SET status = 'NEW' 
WHERE status = 'PROCESSING';

-- Update existing PROCESSING records to NEW status in receipts table
UPDATE receipts 
SET status = 'NEW' 
WHERE status = 'PROCESSING';

-- Drop existing CHECK constraints and recreate without PROCESSING
ALTER TABLE incoming_files DROP CONSTRAINT IF EXISTS incoming_files_status_check;
ALTER TABLE bills DROP CONSTRAINT IF EXISTS bills_status_check;
ALTER TABLE receipts DROP CONSTRAINT IF EXISTS receipts_status_check;

-- Recreate CHECK constraints with only NEW, APPROVED, REJECTED
ALTER TABLE incoming_files 
ADD CONSTRAINT incoming_files_status_check 
CHECK (status IN ('NEW', 'APPROVED', 'REJECTED'));

ALTER TABLE bills 
ADD CONSTRAINT bills_status_check 
CHECK (status IN ('NEW', 'APPROVED', 'REJECTED'));

ALTER TABLE receipts 
ADD CONSTRAINT receipts_status_check 
CHECK (status IN ('NEW', 'APPROVED', 'REJECTED'));