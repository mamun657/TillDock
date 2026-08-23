package com.example.tilldock.data.model;

import com.google.gson.annotations.SerializedName;

import java.math.BigDecimal;
import java.util.List;

public class Sale {

    @SerializedName("id")
    private String id;

    @SerializedName("businessId")
    private String businessId;

    @SerializedName("txnNumber")
    private String txnNumber;

    @SerializedName("customerName")
    private String customerName;

    @SerializedName("subtotal")
    private BigDecimal subtotal;

    @SerializedName("discount")
    private BigDecimal discount;

    @SerializedName("tax")
    private BigDecimal tax;

    @SerializedName("total")
    private BigDecimal total;

    @SerializedName("paymentMethod")
    private PaymentMethod paymentMethod;

    @SerializedName("cashReceived")
    private BigDecimal cashReceived;

    @SerializedName("changeGiven")
    private BigDecimal changeGiven;

    @SerializedName("status")
    private TransactionStatus status;

    @SerializedName("note")
    private String note;

    @SerializedName("itemCount")
    private Integer itemCount;

    @SerializedName("currency")
    private String currency;

    @SerializedName("createdAt")
    private String createdAt;

    @SerializedName("updatedAt")
    private String updatedAt;

    @SerializedName("items")
    private List<SaleItem> items;

    @SerializedName("paymentRef")
    private String paymentRef;


    public Sale() {
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getBusinessId() { return businessId; }
    public void setBusinessId(String businessId) { this.businessId = businessId; }

    public String getTxnNumber() { return txnNumber; }
    public void setTxnNumber(String txnNumber) { this.txnNumber = txnNumber; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }

    public BigDecimal getDiscount() { return discount; }
    public void setDiscount(BigDecimal discount) { this.discount = discount; }

    public BigDecimal getTax() { return tax; }
    public void setTax(BigDecimal tax) { this.tax = tax; }

    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal total) { this.total = total; }

    public PaymentMethod getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(PaymentMethod paymentMethod) { this.paymentMethod = paymentMethod; }

    public BigDecimal getCashReceived() { return cashReceived; }
    public void setCashReceived(BigDecimal cashReceived) { this.cashReceived = cashReceived; }

    public BigDecimal getChangeGiven() { return changeGiven; }
    public void setChangeGiven(BigDecimal changeGiven) { this.changeGiven = changeGiven; }

    public TransactionStatus getStatus() { return status; }
    public void setStatus(TransactionStatus status) { this.status = status; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public Integer getItemCount() { return itemCount; }
    public void setItemCount(Integer itemCount) { this.itemCount = itemCount; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }

    public List<SaleItem> getItems() { return items; }
    public void setItems(List<SaleItem> items) { this.items = items; }

    public String getPaymentRef() { return paymentRef; }
    public void setPaymentRef(String paymentRef) { this.paymentRef = paymentRef; }
}
