package com.tilldock.auth.dto;

import com.tilldock.auth.entity.Product;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public class InventoryResponse {

    private UUID productId;
    private UUID businessId;
    private UUID categoryId;
    private String name;
    private String sku;
    private BigDecimal sellingPrice;
    private Integer stockQuantity;
    private Integer lowStockThreshold;
    private String status;
    private OffsetDateTime updatedAt;

    public static InventoryResponse from(Product p) {
        InventoryResponse r = new InventoryResponse();
        r.productId = p.getId();
        r.businessId = p.getBusinessId();
        r.categoryId = p.getCategoryId();
        r.name = p.getName();
        r.sku = p.getSku();
        r.sellingPrice = p.getSellingPrice();
        r.stockQuantity = p.getStockQuantity();
        r.lowStockThreshold = p.getLowStockThreshold();
        r.status = computeStatus(p.getStockQuantity(), p.getLowStockThreshold());
        r.updatedAt = p.getUpdatedAt();
        return r;
    }

    public static String computeStatus(int stock, int threshold) {
        if (stock <= 0) return "OUT_OF_STOCK";
        if (threshold > 0 && stock <= threshold) return "LOW_STOCK";
        return "IN_STOCK";
    }

    public UUID getProductId() {
        return productId;
    }

    public UUID getBusinessId() {
        return businessId;
    }

    public UUID getCategoryId() {
        return categoryId;
    }

    public String getName() {
        return name;
    }

    public String getSku() {
        return sku;
    }

    public BigDecimal getSellingPrice() {
        return sellingPrice;
    }

    public Integer getStockQuantity() {
        return stockQuantity;
    }

    public Integer getLowStockThreshold() {
        return lowStockThreshold;
    }

    public String getStatus() {
        return status;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }
}