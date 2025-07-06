--liquibase formatted sql

--changeset incoming-files:1
-- IncomingFile table for folder-watcher service

-- IncomingFiles table for files detected in inbox directory
CREATE TABLE incoming_files
(
    id          BIGSERIAL PRIMARY KEY,
    filename    VARCHAR(500) NOT NULL,
    file_path   VARCHAR(1000) NOT NULL,
    upload_date TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    status      VARCHAR(20)  NOT NULL CHECK (status IN ('PENDING', 'PROCESSING', 'APPROVED', 'REJECTED')),
    checksum    VARCHAR(64)  NOT NULL UNIQUE,
    user_id     BIGINT       NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users (id)
);

-- Indexes for performance
CREATE INDEX idx_incoming_files_user_id ON incoming_files (user_id);
CREATE INDEX idx_incoming_files_status ON incoming_files (status);
CREATE INDEX idx_incoming_files_upload_date ON incoming_files (upload_date);
CREATE INDEX idx_incoming_files_filename ON incoming_files (filename);
CREATE INDEX idx_incoming_files_checksum ON incoming_files (checksum);