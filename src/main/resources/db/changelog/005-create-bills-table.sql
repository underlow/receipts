--liquibase formatted sql

--changeset underlow:005-create-bills-table
--comment: Create bills table for tracking financial obligations
CREATE TABLE bills (
    id VARCHAR PRIMARY KEY,
    service_provider_id BIGINT,
    bill_date DATE NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    inbox_entity_id VARCHAR,
    state VARCHAR NOT NULL DEFAULT 'CREATED',
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    description TEXT
);

-- Create indexes for common query patterns
CREATE INDEX idx_bills_state ON bills(state);
CREATE INDEX idx_bills_bill_date ON bills(bill_date);
CREATE INDEX idx_bills_inbox_entity ON bills(inbox_entity_id);
CREATE INDEX idx_bills_service_provider ON bills(service_provider_id);

-- Add constraints for enum values
ALTER TABLE bills ADD CONSTRAINT chk_bill_state 
    CHECK (state IN ('CREATED', 'REMOVED'));

-- Add constraint for positive amounts
ALTER TABLE bills ADD CONSTRAINT chk_bill_amount_positive 
    CHECK (amount > 0);

-- Add foreign key constraint to service_providers table
ALTER TABLE bills ADD CONSTRAINT fk_bills_service_provider 
    FOREIGN KEY (service_provider_id) REFERENCES service_providers(id);

-- Add foreign key constraint to inbox table
ALTER TABLE bills ADD CONSTRAINT fk_bills_inbox_entity 
    FOREIGN KEY (inbox_entity_id) REFERENCES inbox(id);

--rollback DROP TABLE bills;