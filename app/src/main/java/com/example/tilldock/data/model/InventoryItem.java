package com.example.tilldock.data.model;

import com.google.gson.annotations.SerializedName;

import java.math.BigDecimal;

public class InventoryItem {

    @SerializedName("productId")
    private String productId;

    @SerializedName("businessId")
    private String businessId;

    @SerializedName("categoryId")
    private String categoryId;

    @SerializedName("name")
    private String name;

    @SerializedName("sku")
    private String sku;

    @SerializedName("purchasePrice")
    private BigDecimal purchasePrice;

    @SerializedName("sellingPrice")
    private BigDecimal sellingPrice;

    @SerializedName("stockQuantity")
    private Integer stockQuantity;

    @SerializedName("lowStockThreshold")
    private Integer lowStockThreshold;

    @SerializedName("status")
    private String status;

    @SerializedName("updatedAt")
    private String updatedAt;

    public InventoryItem() {
    }

    public String getProductId() {
        return productId;
    }

    public String getBusinessId() {
        return businessId;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public String getName() {
        return name;
    }

    public String getSku() {
        return sku;
    }

    public BigDecimal getPurchasePrice() {
        return purchasePrice;
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

    public String getUpdatedAt() {
        return updatedAt;
    }
}
