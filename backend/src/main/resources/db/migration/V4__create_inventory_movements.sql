ALTER TABLE products
    ADD COLUMN low_stock_threshold INTEGER NOT NULL DEFAULT 0;

ALTER TABLE products
    ADD CONSTRAINT chk_products_low_stock_threshold_nonneg
        CHECK (low_stock_threshold >= 0);

CREATE TABLE inventory_movements (
    id UUID PRIMARY KEY,
    product_id UUID NOT NULL,
    movement_type VARCHAR(16) NOT NULL,
    delta INTEGER NOT NULL,
    previous_quantity INTEGER NOT NULL,
    new_quantity INTEGER NOT NULL,
    reason VARCHAR(255),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_inventory_movements_product FOREIGN KEY (product_id)
        REFERENCES products(id) ON DELETE CASCADE,
    CONSTRAINT chk_inventory_movements_type
        CHECK (movement_type IN ('STOCK_IN', 'STOCK_OUT', 'ADJUSTMENT', 'INITIAL'))
);

CREATE INDEX idx_inventory_movements_product_id ON inventory_movements (product_id);
CREATE INDEX idx_inventory_movements_product_created
    ON inventory_movements (product_id, created_at DESC);
