-- Align seeded account emails with application defaults.
-- Passwords remain BCrypt-encoded admin123 / user123.

UPDATE users
SET email = 'admin@example.com',
    updated_at = NOW()
WHERE email = 'admin@pesira.local';

UPDATE users
SET email = 'user@example.com',
    updated_at = NOW()
WHERE email = 'user@pesira.local';
