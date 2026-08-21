package com.example.tilldock.data.model;

import com.google.gson.annotations.SerializedName;

public class AuthResponse {

    @SerializedName("token")
    private String token;

    @SerializedName("merchant")
    private MerchantResponse merchant;

    public AuthResponse() {
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public MerchantResponse getMerchant() {
        return merchant;
    }

    public void setMerchant(MerchantResponse merchant) {
        this.merchant = merchant;
    }
}
