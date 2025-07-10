--liquibase formatted sql

--changeset underlow:003-create-service-provider-table
--comment: Create service_providers table for managing service provider entities
CREATE TABLE service_providers (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR NOT NULL,
    avatar VARCHAR,
    comment TEXT,
    comment_for_ocr TEXT,
    regular VARCHAR NOT NULL DEFAULT 'NOT_REGULAR',
    custom_fields TEXT,
    state VARCHAR NOT NULL DEFAULT 'ACTIVE',
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    modified_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);

-- Create indexes for common query patterns
CREATE INDEX idx_service_providers_state ON service_providers(state);
CREATE INDEX idx_service_providers_name ON service_providers(name);
CREATE INDEX idx_service_providers_regular ON service_providers(regular);

-- Add constraints for enum values
ALTER TABLE service_providers ADD CONSTRAINT chk_service_provider_state 
    CHECK (state IN ('ACTIVE', 'HIDDEN'));

ALTER TABLE service_providers ADD CONSTRAINT chk_service_provider_regular 
    CHECK (regular IN ('YEARLY', 'MONTHLY', 'WEEKLY', 'NOT_REGULAR'));

--rollback DROP TABLE service_providers;