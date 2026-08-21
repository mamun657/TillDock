CREATE TABLE businesses (
    id UUID PRIMARY KEY,
    merchant_id UUID NOT NULL,
    business_name VARCHAR(160) NOT NULL,
    address VARCHAR(255),
    phone VARCHAR(32),
    email VARCHAR(254),
    logo_url VARCHAR(512),
    currency CHAR(3) NOT NULL DEFAULT 'BDT',
    tax_rate NUMERIC(5,2) NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_businesses_merchant FOREIGN KEY (merchant_id)
        REFERENCES merchants(id) ON DELETE CASCADE
);

CREATE UNIQUE INDEX idx_businesses_merchant_id ON businesses (merchant_id);
CREATE INDEX idx_businesses_business_name ON businesses (business_name);
CREATE INDEX idx_businesses_lower_email ON businesses (LOWER(email));
