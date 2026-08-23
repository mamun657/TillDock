package com.tilldock.auth.dto;

import com.tilldock.auth.entity.SaleItem;

import java.math.BigDecimal;
import java.util.UUID;

public class SaleItemDto {

    private UUID id;
    private UUID productId;
    private String productName;
    private String productSku;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal lineTotal;

    public static SaleItemDto from(SaleItem si) {
        SaleItemDto d = new SaleItemDto();
        d.id = si.getId();
        d.productId = si.getProductId();
        d.productName = si.getProductName();
        d.productSku = si.getProductSku();
        d.quantity = si.getQuantity();
        d.unitPrice = si.getUnitPrice();
        d.lineTotal = si.getLineTotal();
        return d;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getProductId() { return productId; }
    public void setProductId(UUID productId) { this.productId = productId; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public String getProductSku() { return productSku; }
    public void setProductSku(String productSku) { this.productSku = productSku; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }

    public BigDecimal getLineTotal() { return lineTotal; }
    public void setLineTotal(BigDecimal lineTotal) { this.lineTotal = lineTotal; }
}