package com.tilldock.auth.dto;

import com.tilldock.auth.entity.Category;

import java.time.OffsetDateTime;
import java.util.UUID;

public class CategoryDto {

    private UUID id;
    private UUID businessId;
    private String name;
    private String description;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    public static CategoryDto from(Category c) {
        CategoryDto dto = new CategoryDto();
        dto.id = c.getId();
        dto.businessId = c.getBusinessId();
        dto.name = c.getName();
        dto.description = c.getDescription();
        dto.createdAt = c.getCreatedAt();
        dto.updatedAt = c.getUpdatedAt();
        return dto;
    }

    public UUID getId() {
        return id;
    }

    public UUID getBusinessId() {
        return businessId;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }
}
