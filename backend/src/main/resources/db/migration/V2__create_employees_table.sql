-- Employee records table

CREATE TABLE employees (
    id          BIGSERIAL PRIMARY KEY,
    first_name  VARCHAR(100) NOT NULL,
    last_name   VARCHAR(100) NOT NULL,
    email       VARCHAR(255) NOT NULL,
    department  VARCHAR(100) NOT NULL,
    position    VARCHAR(100) NOT NULL,
    salary      NUMERIC(12, 2) NOT NULL,
    hire_date   DATE         NOT NULL,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT uk_employees_email UNIQUE (email),
    CONSTRAINT chk_employees_salary CHECK (salary > 0)
);

CREATE INDEX idx_employees_email ON employees (email);
CREATE INDEX idx_employees_department ON employees (department);
CREATE INDEX idx_employees_last_name ON employees (last_name);
