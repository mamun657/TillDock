CREATE TABLE categories (
    id UUID PRIMARY KEY,
    business_id UUID NOT NULL,
    name VARCHAR(120) NOT NULL,
    description VARCHAR(255),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_categories_business FOREIGN KEY (business_id)
        REFERENCES businesses(id) ON DELETE CASCADE
);

CREATE UNIQUE INDEX idx_categories_business_name ON categories (business_id, LOWER(name));
CREATE INDEX idx_categories_business_id ON categories (business_id);

CREATE TABLE products (
    id UUID PRIMARY KEY,
    business_id UUID NOT NULL,
    category_id UUID NOT NULL,
    name VARCHAR(160) NOT NULL,
    sku VARCHAR(64) NOT NULL,
    description VARCHAR(500),
    purchase_price NUMERIC(12,2) NOT NULL DEFAULT 0,
    selling_price NUMERIC(12,2) NOT NULL DEFAULT 0,
    stock_quantity INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_products_business FOREIGN KEY (business_id)
        REFERENCES businesses(id) ON DELETE CASCADE,
    CONSTRAINT fk_products_category FOREIGN KEY (category_id)
        REFERENCES categories(id) ON DELETE RESTRICT
);

CREATE UNIQUE INDEX idx_products_business_sku ON products (business_id, LOWER(sku));
CREATE INDEX idx_products_business_id ON products (business_id);
CREATE INDEX idx_products_category_id ON products (category_id);
CREATE INDEX idx_products_lower_name ON products (business_id, LOWER(name));
