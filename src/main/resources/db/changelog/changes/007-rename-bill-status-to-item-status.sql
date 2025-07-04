--liquibase formatted sql

--changeset rename-bill-status-to-item-status:1
-- Update database schema to use ItemStatus enum with NEW, PROCESSING, APPROVED, REJECTED
-- This migration renames PENDING to NEW and keeps other status values

-- Step 0: Update check constraints to use both NEW and PENDING
ALTER TABLE bills DROP CONSTRAINT IF EXISTS bills_status_check;
ALTER TABLE bills ADD CONSTRAINT bills_status_check CHECK (status IN ('NEW', 'PENDING' ,'PROCESSING', 'APPROVED', 'REJECTED'));
ALTER TABLE receipts DROP CONSTRAINT IF EXISTS receipts_status_check;
ALTER TABLE receipts ADD CONSTRAINT receipts_status_check CHECK (status IN ('NEW', 'PENDING' ,'PROCESSING', 'APPROVED', 'REJECTED'));

-- Step 1: Update existing PENDING records to NEW in bills table
UPDATE bills SET status = 'NEW' WHERE status = 'PENDING';

-- Step 2: Update existing PENDING records to NEW in receipts table
UPDATE receipts SET status = 'NEW' WHERE status = 'PENDING';

-- Step 3: Update check constraints to use NEW instead of PENDING
ALTER TABLE bills DROP CONSTRAINT IF EXISTS bills_status_check;
ALTER TABLE bills ADD CONSTRAINT bills_status_check CHECK (status IN ('NEW', 'PROCESSING', 'APPROVED', 'REJECTED'));

ALTER TABLE receipts DROP CONSTRAINT IF EXISTS receipts_status_check;
ALTER TABLE receipts ADD CONSTRAINT receipts_status_check CHECK (status IN ('NEW', 'PROCESSING', 'APPROVED', 'REJECTED'));

-- Step 4: Update default values for receipts table
ALTER TABLE receipts ALTER COLUMN status SET DEFAULT 'NEW';
