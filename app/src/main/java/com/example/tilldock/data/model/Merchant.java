package com.example.tilldock.data.model;

public class Merchant {

    private String id;
    private String fullName;
    private String businessName;
    private String email;
    private String phone;
    private String role;
    private String status;

    public Merchant() {
    }

    public Merchant(String id, String fullName, String businessName,
                    String email, String phone, String role, String status) {
        this.id = id;
        this.fullName = fullName;
        this.businessName = businessName;
        this.email = email;
        this.phone = phone;
        this.role = role;
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getBusinessName() {
        return businessName;
    }

    public void setBusinessName(String businessName) {
        this.businessName = businessName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
