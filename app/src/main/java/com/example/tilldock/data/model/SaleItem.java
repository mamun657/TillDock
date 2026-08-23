package com.example.tilldock.data.model;

import com.google.gson.annotations.SerializedName;

import java.math.BigDecimal;

public class SaleItem {

    @SerializedName("id")
    private String id;

    @SerializedName("productId")
    private String productId;

    @SerializedName("productName")
    private String productName;

    @SerializedName("productSku")
    private String productSku;

    @SerializedName("quantity")
    private Integer quantity;

    @SerializedName("unitPrice")
    private BigDecimal unitPrice;

    @SerializedName("lineTotal")
    private BigDecimal lineTotal;

    public SaleItem() {
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }

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