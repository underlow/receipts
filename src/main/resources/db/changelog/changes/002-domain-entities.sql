--liquibase formatted sql

--changeset domain-entities:1
-- Domain entities for receipt processing application

-- Service providers table
CREATE TABLE service_providers
(
    id                      BIGSERIAL PRIMARY KEY,
    name                    VARCHAR(255) NOT NULL,
    category                VARCHAR(255) NOT NULL,
    default_payment_method  VARCHAR(50)  NOT NULL,
    is_active              BOOLEAN      NOT NULL DEFAULT TRUE,
    comment                TEXT
);

-- Payment methods table
CREATE TABLE payment_methods
(
    id      BIGSERIAL PRIMARY KEY,
    name    VARCHAR(255) NOT NULL,
    type    VARCHAR(20)  NOT NULL CHECK (type IN ('CARD', 'BANK', 'CASH', 'OTHER')),
    comment TEXT
);

-- Bills table for uploaded receipt files
CREATE TABLE bills
(
    id                BIGSERIAL PRIMARY KEY,
    filename          VARCHAR(500) NOT NULL,
    file_path         VARCHAR(1000) NOT NULL,
    upload_date       TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    status            VARCHAR(20)  NOT NULL CHECK (status IN ('PENDING', 'PROCESSING', 'APPROVED', 'REJECTED')),
    ocr_raw_json      TEXT,
    extracted_amount  DECIMAL(10, 2),
    extracted_date    DATE,
    extracted_provider VARCHAR(255),
    user_id           BIGINT       NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users (id)
);

-- Receipts table for aggregating expenses
CREATE TABLE receipts
(
    id      BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    bill_id BIGINT,
    FOREIGN KEY (user_id) REFERENCES users (id),
    FOREIGN KEY (bill_id) REFERENCES bills (id)
);

-- Payments table for processed and approved transactions
CREATE TABLE payments
(
    id                  BIGSERIAL PRIMARY KEY,
    service_provider_id BIGINT         NOT NULL,
    payment_method_id   BIGINT         NOT NULL,
    amount             DECIMAL(10, 2) NOT NULL CHECK (amount > 0),
    currency           VARCHAR(3)     NOT NULL,
    invoice_date       DATE           NOT NULL,
    payment_date       DATE           NOT NULL,
    bill_id            BIGINT,
    user_id            BIGINT         NOT NULL,
    comment            TEXT,
    FOREIGN KEY (service_provider_id) REFERENCES service_providers (id),
    FOREIGN KEY (payment_method_id) REFERENCES payment_methods (id),
    FOREIGN KEY (bill_id) REFERENCES bills (id),
    FOREIGN KEY (user_id) REFERENCES users (id)
);

-- Indexes for performance
CREATE INDEX idx_service_providers_name ON service_providers (name);
CREATE INDEX idx_service_providers_category ON service_providers (category);
CREATE INDEX idx_service_providers_active ON service_providers (is_active);

CREATE INDEX idx_payment_methods_name ON payment_methods (name);
CREATE INDEX idx_payment_methods_type ON payment_methods (type);

CREATE INDEX idx_bills_user_id ON bills (user_id);
CREATE INDEX idx_bills_status ON bills (status);
CREATE INDEX idx_bills_upload_date ON bills (upload_date);
CREATE INDEX idx_bills_filename ON bills (filename);

CREATE INDEX idx_receipts_user_id ON receipts (user_id);
CREATE INDEX idx_receipts_bill_id ON receipts (bill_id);

CREATE INDEX idx_payments_user_id ON payments (user_id);
CREATE INDEX idx_payments_service_provider_id ON payments (service_provider_id);
CREATE INDEX idx_payments_payment_method_id ON payments (payment_method_id);
CREATE INDEX idx_payments_invoice_date ON payments (invoice_date);
CREATE INDEX idx_payments_payment_date ON payments (payment_date);
CREATE INDEX idx_payments_bill_id ON payments (bill_id);