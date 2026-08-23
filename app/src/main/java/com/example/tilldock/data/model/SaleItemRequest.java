package com.example.tilldock.data.model;

import com.google.gson.annotations.SerializedName;

public class SaleItemRequest {

    @SerializedName("productId")
    private String productId;

    @SerializedName("quantity")
    private Integer quantity;

    public SaleItemRequest() {
    }

    public SaleItemRequest(String productId, Integer quantity) {
        this.productId = productId;
        this.quantity = quantity;
    }

    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
}