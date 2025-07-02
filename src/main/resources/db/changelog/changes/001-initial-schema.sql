--liquibase formatted sql

--changeset yourname:1
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    last_login_at TIMESTAMP NOT NULL
);

CREATE TABLE login_events (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    timestamp TIMESTAMP NOT NULL,
    ip_address VARCHAR(255),
    FOREIGN KEY (user_id) REFERENCES users(id)
);
