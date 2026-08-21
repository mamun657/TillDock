package com.example.tilldock.data.model;

import com.google.gson.annotations.SerializedName;

public class StockMutationRequest {

    @SerializedName("quantity")
    private Integer quantity;

    @SerializedName("reason")
    private String reason;

    public StockMutationRequest() {
    }

    public StockMutationRequest(Integer quantity, String reason) {
        this.quantity = quantity;
        this.reason = reason;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}