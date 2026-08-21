package com.tilldock.auth.dto;

import com.tilldock.auth.entity.Business;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public class BusinessDto {

    private UUID id;
    private UUID merchantId;
    private String businessName;
    private String address;
    private String phone;
    private String email;
    private String logoUrl;
    private String currency;
    private BigDecimal taxRate;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    public static BusinessDto from(Business b) {
        BusinessDto dto = new BusinessDto();
        dto.id = b.getId();
        dto.merchantId = b.getMerchantId();
        dto.businessName = b.getBusinessName();
        dto.address = b.getAddress();
        dto.phone = b.getPhone();
        dto.email = b.getEmail();
        dto.logoUrl = b.getLogoUrl();
        dto.currency = b.getCurrency();
        dto.taxRate = b.getTaxRate();
        dto.createdAt = b.getCreatedAt();
        dto.updatedAt = b.getUpdatedAt();
        return dto;
    }

    public UUID getId() {
        return id;
    }

    public UUID getMerchantId() {
        return merchantId;
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

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }
}
