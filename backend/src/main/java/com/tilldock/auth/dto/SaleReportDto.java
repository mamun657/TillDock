package com.tilldock.auth.dto;

import java.math.BigDecimal;
import java.util.List;

public class SaleReportDto {

    private String period;
    private String periodLabel;
    private BigDecimal totalSales;
    private long totalOrders;
    private long itemsSold;
    private BigDecimal averageTicket;
    private List<TopProduct> topProducts;

    public SaleReportDto() {
    }

    public SaleReportDto(String period,
                         String periodLabel,
                         BigDecimal totalSales,
                         long totalOrders,
                         long itemsSold,
                         BigDecimal averageTicket,
                         List<TopProduct> topProducts) {
        this.period = period;
        this.periodLabel = periodLabel;
        this.totalSales = totalSales;
        this.totalOrders = totalOrders;
        this.itemsSold = itemsSold;
        this.averageTicket = averageTicket;
        this.topProducts = topProducts;
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
        private String productId;
        private String productName;
        private long totalQuantity;
        private BigDecimal totalRevenue;

        public TopProduct() {
        }

        public TopProduct(String productId,
                          String productName,
                          long totalQuantity,
                          BigDecimal totalRevenue) {
            this.productId = productId;
            this.productName = productName;
            this.totalQuantity = totalQuantity;
            this.totalRevenue = totalRevenue;
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