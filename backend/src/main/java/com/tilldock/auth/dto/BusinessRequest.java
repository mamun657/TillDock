package com.tilldock.auth.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public class BusinessRequest {

    @NotBlank
    @Size(min = 2, max = 160)
    private String businessName;

    @Size(max = 255)
    private String address;

    @Pattern(regexp = "^[+0-9 ()\\-]{7,32}$", message = "phone format is invalid")
    private String phone;

    @Size(max = 254)
    private String email;

    @Size(max = 512)
    private String logoUrl;

    @Pattern(regexp = "^[A-Z]{3}$", message = "currency must be a 3-letter uppercase code")
    private String currency;

    @DecimalMin(value = "0.00", message = "tax rate must be 0 or greater")
    @DecimalMax(value = "100.00", message = "tax rate must be 100 or less")
    private BigDecimal taxRate;

    public String getBusinessName() {
        return businessName;
    }

    public void setBusinessName(String businessName) {
        this.businessName = businessName;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getLogoUrl() {
        return logoUrl;
    }

    public void setLogoUrl(String logoUrl) {
        this.logoUrl = logoUrl;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public BigDecimal getTaxRate() {
        return taxRate;
    }

    public void setTaxRate(BigDecimal taxRate) {
        this.taxRate = taxRate;
    }
}
