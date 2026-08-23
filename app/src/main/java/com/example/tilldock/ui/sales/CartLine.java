package com.example.tilldock.ui.sales;

import java.math.BigDecimal;

public class CartLine {

    private final String productId;
    private final String name;
    private final String sku;
    private final BigDecimal unitPrice;
    private final int availableStock;
    private int quantity;

    public CartLine(String productId, String name, String sku, BigDecimal unitPrice, int availableStock, int quantity) {
        this.productId = productId;
        this.name = name;
        this.sku = sku;
        this.unitPrice = unitPrice;
        this.availableStock = availableStock;
        this.quantity = quantity;
    }

    public String getProductId() {
        return productId;
    }

    public String getName() {
        return name;
    }

    public String getSku() {
        return sku;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public int getAvailableStock() {
        return availableStock;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public BigDecimal lineTotal() {
        if (unitPrice == null) return BigDecimal.ZERO;
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }
}