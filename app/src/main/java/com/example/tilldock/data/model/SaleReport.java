package com.example.tilldock.data.model;

import com.google.gson.annotations.SerializedName;

import java.math.BigDecimal;
import java.util.List;

public class SaleReport {

    @SerializedName("period")
    private String period;

    @SerializedName("periodLabel")
    private String periodLabel;

    @SerializedName("totalSales")
    private BigDecimal totalSales;

    @SerializedName("totalOrders")
    private long totalOrders;

    @SerializedName("itemsSold")
    private long itemsSold;

    @SerializedName("averageTicket")
    private BigDecimal averageTicket;

    @SerializedName("topProducts")
    private List<TopProduct> topProducts;

    public SaleReport() {
    }

    public String getPeriod() { return period; }
    public void setPeriod(String period) { this.period = period; }

    public String getPeriodLabel() { return periodLabel; }
    public void setPeriodLabel(String periodLabel) { this.periodLabel = periodLabel; }

    public BigDecimal getTotalSales() { return totalSales; }
    public void setTotalSales(BigDecimal totalSales) { this.totalSales = totalSales; }

    public long getTotalOrders() { return totalOrders; }
    public void setTotalOrders(long totalOrders) { this.totalOrders = totalOrders; }

    public long getItemsSold() { return itemsSold; }
    public void setItemsSold(long itemsSold) { this.itemsSold = itemsSold; }

    public BigDecimal getAverageTicket() { return averageTicket; }
    public void setAverageTicket(BigDecimal averageTicket) { this.averageTicket = averageTicket; }

    public List<TopProduct> getTopProducts() { return topProducts; }
    public void setTopProducts(List<TopProduct> topProducts) { this.topProducts = topProducts; }

    public static class TopProduct {

        @SerializedName("productId")
        private String productId;

        @SerializedName("productName")
        private String productName;

        @SerializedName("totalQuantity")
        private long totalQuantity;

        @SerializedName("totalRevenue")
        private BigDecimal totalRevenue;

        public TopProduct() {
        }

        public String getProductId() { return productId; }
        public void setProductId(String productId) { this.productId = productId; }

        public String getProductName() { return productName; }
        public void setProductName(String productName) { this.productName = productName; }

        public long getTotalQuantity() { return totalQuantity; }
        public void setTotalQuantity(long totalQuantity) { this.totalQuantity = totalQuantity; }

        public BigDecimal getTotalRevenue() { return totalRevenue; }
        public void setTotalRevenue(BigDecimal totalRevenue) { this.totalRevenue = totalRevenue; }
    }
}