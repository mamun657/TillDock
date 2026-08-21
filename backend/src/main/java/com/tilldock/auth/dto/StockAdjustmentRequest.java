package com.tilldock.auth.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

public class StockAdjustmentRequest {

    @Min(value = 0, message = "newQuantity must be 0 or greater")
    @Max(value = 1_000_000, message = "newQuantity must be 1,000,000 or less")
    private int newQuantity;

    @Size(max = 255)
    private String reason;

    public int getNewQuantity() {
        return newQuantity;
    }

    public void setNewQuantity(int newQuantity) {
        this.newQuantity = newQuantity;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}