--liquibase formatted sql

--changeset underlow:004-create-receipts-table
--comment: Create receipts table for tracking expense records
CREATE TABLE receipts (
    id VARCHAR PRIMARY KEY,
    payment_type_id VARCHAR NOT NULL,
    payment_date DATE NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    inbox_entity_id VARCHAR,
    state VARCHAR NOT NULL DEFAULT 'CREATED',
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    description TEXT,
    merchant_name VARCHAR,
    service_provider_id BIGINT
);

-- Create indexes for common query patterns
CREATE INDEX idx_receipts_state ON receipts(state);
CREATE INDEX idx_receipts_payment_date ON receipts(payment_date);
CREATE INDEX idx_receipts_inbox_entity ON receipts(inbox_entity_id);
CREATE INDEX idx_receipts_service_provider ON receipts(service_provider_id);

-- Add constraints for enum values
ALTER TABLE receipts ADD CONSTRAINT chk_receipt_state 
    CHECK (state IN ('CREATED', 'REMOVED'));

-- Add constraint for positive amounts
ALTER TABLE receipts ADD CONSTRAINT chk_receipt_amount_positive 
    CHECK (amount > 0);

-- Add foreign key constraint to service_providers table
ALTER TABLE receipts ADD CONSTRAINT fk_receipts_service_provider 
    FOREIGN KEY (service_provider_id) REFERENCES service_providers(id);

-- Add foreign key constraint to inbox table
ALTER TABLE receipts ADD CONSTRAINT fk_receipts_inbox_entity 
    FOREIGN KEY (inbox_entity_id) REFERENCES inbox(id);

--rollback DROP TABLE receipts;