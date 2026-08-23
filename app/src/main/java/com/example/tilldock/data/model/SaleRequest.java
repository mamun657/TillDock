package com.example.tilldock.data.model;

import com.google.gson.annotations.SerializedName;

import java.math.BigDecimal;
import java.util.List;

public class SaleRequest {

    @SerializedName("items")
    private List<SaleItemRequest> items;

    @SerializedName("customerName")
    private String customerName;

    @SerializedName("discount")
    private BigDecimal discount;

    @SerializedName("note")
    private String note;

    @SerializedName("paymentMethod")
    private PaymentMethod paymentMethod;

    @SerializedName("cashReceived")
    private BigDecimal cashReceived;

    public SaleRequest() {
    }

    public List<SaleItemRequest> getItems() { return items; }
    public void setItems(List<SaleItemRequest> items) { this.items = items; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public BigDecimal getDiscount() { return discount; }
    public void setDiscount(BigDecimal discount) { this.discount = discount; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public PaymentMethod getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(PaymentMethod paymentMethod) { this.paymentMethod = paymentMethod; }

    public BigDecimal getCashReceived() { return cashReceived; }
    public void setCashReceived(BigDecimal cashReceived) { this.cashReceived = cashReceived; }
}