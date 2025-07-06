--liquibase formatted sql

--changeset initial:1
-- Initial database schema for receipt tracking application

-- Users table for OAuth2 authentication
CREATE TABLE users
(
    id            BIGSERIAL PRIMARY KEY,
    email         VARCHAR(255) NOT NULL UNIQUE,
    name          VARCHAR(255) NOT NULL,
    created_at    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_login_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Login events table for audit logging
CREATE TABLE login_events
(
    id        BIGSERIAL PRIMARY KEY,
    user_id   BIGINT    NOT NULL,
    timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users (id)
);

-- Indexes for performance
CREATE INDEX idx_users_email ON users (email);
CREATE INDEX idx_login_events_user_id ON login_events (user_id);
CREATE INDEX idx_login_events_timestamp ON login_events (timestamp);
