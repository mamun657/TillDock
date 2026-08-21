package com.tilldock.auth.dto;

import com.tilldock.auth.entity.Merchant;

import java.time.OffsetDateTime;
import java.util.UUID;

public class MerchantDto {

    private UUID id;
    private String name;
    private String businessName;
    private String email;
    private String phone;
    private String role;
    private String status;
    private OffsetDateTime createdAt;
    private OffsetDateTime lastLoginAt;

    public static MerchantDto from(Merchant m) {
        MerchantDto dto = new MerchantDto();
        dto.id = m.getId();
        dto.name = m.getName();
        dto.businessName = m.getBusinessName();
        dto.email = m.getEmail();
        dto.phone = m.getPhone();
        dto.role = m.getRole() == null ? null : m.getRole().name();
        dto.status = m.getStatus() == null ? null : m.getStatus().name();
        dto.createdAt = m.getCreatedAt();
        dto.lastLoginAt = m.getLastLoginAt();
        return dto;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getBusinessName() {
        return businessName;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public String getRole() {
        return role;
    }

    public String getStatus() {
        return status;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getLastLoginAt() {
        return lastLoginAt;
    }
}