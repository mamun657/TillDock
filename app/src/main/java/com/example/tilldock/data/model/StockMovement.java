package com.example.tilldock.data.model;

import com.google.gson.annotations.SerializedName;

public class StockMovement {

    @SerializedName("id")
    private String id;

    @SerializedName("productId")
    private String productId;

    @SerializedName("movementType")
    private String movementType;

    @SerializedName("delta")
    private Integer delta;

    @SerializedName("previousQuantity")
    private Integer previousQuantity;

    @SerializedName("newQuantity")
    private Integer newQuantity;

    @SerializedName("reason")
    private String reason;

    @SerializedName("createdAt")
    private String createdAt;

    public StockMovement() {
    }

    public String getId() {
        return id;
    }

    public String getProductId() {
        return productId;
    }

    public String getMovementType() {
        return movementType;
    }

    public Integer getDelta() {
        return delta;
    }

    public Integer getPreviousQuantity() {
        return previousQuantity;
    }

    public Integer getNewQuantity() {
        return newQuantity;
    }

    public String getReason() {
        return reason;
    }

    public String getCreatedAt() {
        return createdAt;
    }
}