package com.example.tilldock.data.model;

import com.google.gson.annotations.SerializedName;

import java.math.BigDecimal;

public class BusinessRequest {

    @SerializedName("businessName")
    private String businessName;

    @SerializedName("address")
    private String address;

    @SerializedName("phone")
    private String phone;

    @SerializedName("email")
    private String email;

    @SerializedName("logoUrl")
    private String logoUrl;

    @SerializedName("currency")
    private String currency;

    @SerializedName("taxRate")
    private BigDecimal taxRate;

    public BusinessRequest() {
    }

    public BusinessRequest(String businessName, String address, String phone,
                           String email, String logoUrl, String currency, BigDecimal taxRate) {
        this.businessName = businessName;
        this.address = address;
        this.phone = phone;
        this.email = email;
        this.logoUrl = logoUrl;
        this.currency = currency;
        this.taxRate = taxRate;
    }

    public String getBusinessName() {
        return businessName;
    }

    public String getAddress() {
        return address;
    }

    public String getPhone() {
        return phone;
    }

    public String getEmail() {
        return email;
    }

    public String getLogoUrl() {
        return logoUrl;
    }

    public String getCurrency() {
        return currency;
    }

    public BigDecimal getTaxRate() {
        return taxRate;
    }

    public void setBusinessName(String businessName) {
        this.businessName = businessName;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setLogoUrl(String logoUrl) {
        this.logoUrl = logoUrl;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public void setTaxRate(BigDecimal taxRate) {
        this.taxRate = taxRate;
    }
}