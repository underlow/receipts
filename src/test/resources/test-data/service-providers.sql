-- Test data for service providers table
-- Insert sample service providers for testing avatar upload functionality

-- Clean up existing data first
DELETE FROM service_providers WHERE id IN (1, 2, 3);

INSERT INTO service_providers (id, name, avatar, comment, comment_for_ocr, regular, custom_fields, state, created_date, modified_date)
VALUES 
    (1, 'Test Service Provider 1', null, 'Test provider for avatar upload', 'OCR comment 1', 'MONTHLY', null, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (2, 'Test Service Provider 2', 'existing-avatar.jpg', 'Test provider with existing avatar', 'OCR comment 2', 'YEARLY', null, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (3, 'Test Service Provider 3', null, 'Another test provider', 'OCR comment 3', 'NOT_REGULAR', null, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);