CREATE TABLE merchants (
    id UUID PRIMARY KEY,
    name VARCHAR(120) NOT NULL,
    business_name VARCHAR(160) NOT NULL,
    email VARCHAR(254) NOT NULL,
    phone VARCHAR(32) NOT NULL,
    password_hash VARCHAR(120) NOT NULL,
    role VARCHAR(32) NOT NULL DEFAULT 'MERCHANT',
    status VARCHAR(32) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    last_login_at TIMESTAMP WITH TIME ZONE
);

CREATE UNIQUE INDEX idx_merchants_email_lower ON merchants (LOWER(email));
CREATE INDEX idx_merchants_business_name ON merchants (business_name);