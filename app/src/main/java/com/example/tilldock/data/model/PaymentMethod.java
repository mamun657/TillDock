package com.example.tilldock.data.model;

import com.google.gson.annotations.SerializedName;

public enum PaymentMethod {

    @SerializedName("CASH")
    CASH,

    @SerializedName("CARD")
    CARD,

    @SerializedName("QR")
    QR,

    @SerializedName("WALLET")
    WALLET,

    @SerializedName("BANK")
    BANK,

    @SerializedName("OTHER")
    OTHER;

    public String displayName() {
        switch (this) {
            case CASH: return "Cash";
            case CARD: return "Card";
            case QR: return "QR Code";
            case WALLET: return "Mobile Wallet";
            case BANK: return "Bank Transfer";
            default: return "Other";
        }
    }
}