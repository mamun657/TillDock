package com.tilldock.auth.dto;

public class AuthResponse {

    private String token;
    private String tokenType;
    private long expiresInSeconds;
    private MerchantDto merchant;

    public AuthResponse(String token, String tokenType, long expiresInSeconds, MerchantDto merchant) {
        this.token = token;
        this.tokenType = tokenType;
        this.expiresInSeconds = expiresInSeconds;
        this.merchant = merchant;
    }

    public String getToken() {
        return token;
    }

    public String getTokenType() {
        return tokenType;
    }

    public long getExpiresInSeconds() {
        return expiresInSeconds;
    }

    public MerchantDto getMerchant() {
        return merchant;
    }
}