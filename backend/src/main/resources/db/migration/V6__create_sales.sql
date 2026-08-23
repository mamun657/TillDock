CREATE TABLE sales (
    id UUID PRIMARY KEY,
    merchant_id UUID NOT NULL,
    business_id UUID NOT NULL,
    txn_number VARCHAR(40) NOT NULL,
    customer_name VARCHAR(160),
    subtotal NUMERIC(14, 2) NOT NULL DEFAULT 0,
    discount NUMERIC(14, 2) NOT NULL DEFAULT 0,
    tax NUMERIC(14, 2) NOT NULL DEFAULT 0,
    total NUMERIC(14, 2) NOT NULL DEFAULT 0,
    payment_method VARCHAR(16) NOT NULL,
    cash_received NUMERIC(14, 2),
    change_given NUMERIC(14, 2),
    status VARCHAR(16) NOT NULL DEFAULT 'COMPLETED',
    note VARCHAR(500),
    item_count INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_sales_merchant FOREIGN KEY (merchant_id)
        REFERENCES merchants(id) ON DELETE CASCADE,
    CONSTRAINT fk_sales_business FOREIGN KEY (business_id)
        REFERENCES businesses(id) ON DELETE CASCADE,
    CONSTRAINT chk_sales_payment_method
        CHECK (payment_method IN ('CASH', 'CARD', 'QR', 'WALLET', 'BANK', 'OTHER')),
    CONSTRAINT chk_sales_status
        CHECK (status IN ('COMPLETED', 'CANCELLED', 'REFUNDED')),
    CONSTRAINT chk_sales_subtotal_nonneg CHECK (subtotal >= 0),
    CONSTRAINT chk_sales_discount_nonneg CHECK (discount >= 0),
    CONSTRAINT chk_sales_tax_nonneg CHECK (tax >= 0),
    CONSTRAINT chk_sales_total_nonneg CHECK (total >= 0),
    CONSTRAINT chk_sales_item_count_nonneg CHECK (item_count >= 0)
);

CREATE UNIQUE INDEX uq_sales_merchant_txn ON sales (merchant_id, txn_number);
CREATE INDEX idx_sales_business_created ON sales (business_id, created_at DESC);
CREATE INDEX idx_sales_merchant_created ON sales (merchant_id, created_at DESC);

CREATE TABLE sale_items (
    id UUID PRIMARY KEY,
    sale_id UUID NOT NULL,
    product_id UUID NOT NULL,
    product_name VARCHAR(160) NOT NULL,
    product_sku VARCHAR(64),
    quantity INTEGER NOT NULL,
    unit_price NUMERIC(12, 2) NOT NULL,
    line_total NUMERIC(14, 2) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_sale_items_sale FOREIGN KEY (sale_id)
        REFERENCES sales(id) ON DELETE CASCADE,
    CONSTRAINT fk_sale_items_product FOREIGN KEY (product_id)
        REFERENCES products(id) ON DELETE RESTRICT,
    CONSTRAINT chk_sale_items_quantity_positive CHECK (quantity > 0),
    CONSTRAINT chk_sale_items_unit_price_nonneg CHECK (unit_price >= 0),
    CONSTRAINT chk_sale_items_line_total_nonneg CHECK (line_total >= 0)
);

CREATE INDEX idx_sale_items_sale ON sale_items (sale_id);
CREATE INDEX idx_sale_items_product ON sale_items (product_id);
