package com.example.tilldock.data.model;

import com.google.gson.annotations.SerializedName;

public class MerchantResponse {

    @SerializedName("id")
    private String id;

    @SerializedName("name")
    private String fullName;

    @SerializedName("businessName")
    private String businessName;

    @SerializedName("email")
    private String email;

    @SerializedName("phone")
    private String phone;

    @SerializedName("role")
    private String role;

    @SerializedName("status")
    private String status;

    public MerchantResponse() {
    }

    public Merchant toMerchant() {
        return new Merchant(id, fullName, businessName, email, phone, role, status);
    }

    public String getId() {
        return id;
    }

    public String getFullName() {
        return fullName;
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
}
