package com.example.tilldock.data.model;

import com.google.gson.annotations.SerializedName;

public enum TransactionStatus {

    @SerializedName("COMPLETED")
    COMPLETED,

    @SerializedName("CANCELLED")
    CANCELLED,

    @SerializedName("REFUNDED")
    REFUNDED;

    public String displayName() {
        switch (this) {
            case CANCELLED: return "Cancelled";
            case REFUNDED: return "Refunded";
            default: return "Completed";
        }
    }
}