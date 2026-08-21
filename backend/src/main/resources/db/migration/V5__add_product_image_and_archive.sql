ALTER TABLE products
    ADD COLUMN image_url VARCHAR(500);

ALTER TABLE products
    ADD COLUMN is_archived BOOLEAN NOT NULL DEFAULT FALSE;

ALTER TABLE products
    ADD COLUMN archived_at TIMESTAMP WITH TIME ZONE;

ALTER TABLE products
    ADD CONSTRAINT chk_products_image_url_length
        CHECK (image_url IS NULL OR char_length(image_url) <= 500);

CREATE INDEX idx_products_business_active
    ON products (business_id, is_archived, LOWER(name));