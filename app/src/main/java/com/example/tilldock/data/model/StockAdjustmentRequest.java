package com.example.tilldock.data.model;

import com.google.gson.annotations.SerializedName;

public class StockAdjustmentRequest {

    @SerializedName("newQuantity")
    private Integer newQuantity;

    @SerializedName("reason")
    private String reason;

    public StockAdjustmentRequest() {
    }

    public StockAdjustmentRequest(Integer newQuantity, String reason) {
        this.newQuantity = newQuantity;
        this.reason = reason;
    }

    public Integer getNewQuantity() {
        return newQuantity;
    }

    public void setNewQuantity(Integer newQuantity) {
        this.newQuantity = newQuantity;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}