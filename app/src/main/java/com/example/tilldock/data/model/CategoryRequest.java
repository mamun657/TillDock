package com.example.tilldock.data.model;

import com.google.gson.annotations.SerializedName;

public class CategoryRequest {

    @SerializedName("name")
    private String name;

    @SerializedName("description")
    private String description;

    public CategoryRequest() {
    }

    public CategoryRequest(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}