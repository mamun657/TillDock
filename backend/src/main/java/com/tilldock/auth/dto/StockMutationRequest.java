package com.tilldock.auth.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

public class StockMutationRequest {

    @Min(value = 1, message = "quantity must be 1 or greater")
    @Max(value = 1_000_000, message = "quantity must be 1,000,000 or less")
    private int quantity;

    @Size(max = 255)
    private String reason;

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}