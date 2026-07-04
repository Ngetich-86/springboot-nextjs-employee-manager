-- Users table and seed data for default admin and user accounts.
-- Passwords are BCrypt-encoded: admin123 / user123

CREATE TABLE users (
    id         BIGSERIAL PRIMARY KEY,
    email      VARCHAR(255) NOT NULL,
    password   VARCHAR(255) NOT NULL,
    role       VARCHAR(20)  NOT NULL,
    created_at TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT uk_users_email UNIQUE (email),
    CONSTRAINT chk_users_role CHECK (role IN ('ADMIN', 'USER'))
);

CREATE INDEX idx_users_email ON users (email);
CREATE INDEX idx_users_role ON users (role);

INSERT INTO users (email, password, role)
VALUES
    (
        'admin@pesira.local',
        '$2b$10$pKeOCREmSn5IwOx/TBTtAOc0v8i/ACK8ByYU1kN2Lepw4Mm1TJwtu',
        'ADMIN'
    ),
    (
        'user@pesira.local',
        '$2b$10$4.tyfjiXmoEwfKviE7dyLu4mk9UmpmskUDZFq4oWjOMxSVCKeI7QK',
        'USER'
    );
