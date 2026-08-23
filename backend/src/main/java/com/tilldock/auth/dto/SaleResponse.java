package com.tilldock.auth.dto;

import com.tilldock.auth.entity.PaymentMethod;
import com.tilldock.auth.entity.Sale;
import com.tilldock.auth.entity.SaleItem;
import com.tilldock.auth.entity.TransactionStatus;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class SaleResponse {

    private UUID id;
    private UUID businessId;
    private String txnNumber;
    private String customerName;
    private BigDecimal subtotal;
    private BigDecimal discount;
    private BigDecimal tax;
    private BigDecimal total;
    private PaymentMethod paymentMethod;
    private BigDecimal cashReceived;
    private BigDecimal changeGiven;
    private TransactionStatus status;
    private String note;
    private Integer itemCount;
    private String currency;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    private List<SaleItemDto> items;

    public static SaleResponse from(Sale sale, String currency, List<SaleItem> items) {
        SaleResponse r = new SaleResponse();
        r.id = sale.getId();
        r.businessId = sale.getBusinessId();
        r.txnNumber = sale.getTxnNumber();
        r.customerName = sale.getCustomerName();
        r.subtotal = sale.getSubtotal();
        r.discount = sale.getDiscount();
        r.tax = sale.getTax();
        r.total = sale.getTotal();
        r.paymentMethod = sale.getPaymentMethod();
        r.cashReceived = sale.getCashReceived();
        r.changeGiven = sale.getChangeGiven();
        r.status = sale.getStatus();
        r.note = sale.getNote();
        r.itemCount = sale.getItemCount();
        r.currency = currency;
        r.createdAt = sale.getCreatedAt();
        r.updatedAt = sale.getUpdatedAt();
        r.items = items.stream().map(SaleItemDto::from).collect(Collectors.toList());
        return r;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getBusinessId() { return businessId; }
    public void setBusinessId(UUID businessId) { this.businessId = businessId; }

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

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }

    public OffsetDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; }

    public List<SaleItemDto> getItems() { return items; }
    public void setItems(List<SaleItemDto> items) { this.items = items; }
}