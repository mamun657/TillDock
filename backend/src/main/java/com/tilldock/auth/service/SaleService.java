package com.tilldock.auth.service;

import com.tilldock.auth.dto.SaleItemRequest;
import com.tilldock.auth.dto.SaleReportDto;
import com.tilldock.auth.dto.SaleRequest;
import com.tilldock.auth.dto.SaleResponse;
import com.tilldock.auth.entity.Business;
import com.tilldock.auth.entity.PaymentMethod;
import com.tilldock.auth.entity.Product;
import com.tilldock.auth.entity.Sale;
import com.tilldock.auth.entity.SaleItem;
import com.tilldock.auth.entity.TransactionStatus;
import com.tilldock.auth.repository.BusinessRepository;
import com.tilldock.auth.repository.SaleItemRepository;
import com.tilldock.auth.repository.SaleRepository;
import com.tilldock.auth.repository.SaleItemRepository.TopProductProjection;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class SaleService {

    private static final BigDecimal TAX_RATE = new BigDecimal("0.00");
    private static final String DEFAULT_CURRENCY = "USD";

    private final SaleRepository saleRepository;
    private final SaleItemRepository saleItemRepository;
    private final ProductService productService;
    private final BusinessRepository businessRepository;

    public SaleService(SaleRepository saleRepository,
                       SaleItemRepository saleItemRepository,
                       ProductService productService,
                       BusinessRepository businessRepository) {
        this.saleRepository = saleRepository;
        this.saleItemRepository = saleItemRepository;
        this.productService = productService;
        this.businessRepository = businessRepository;
    }

    @Transactional
    public SaleResponse createSale(UUID merchantId, SaleRequest req) {
        if (req.getItems() == null || req.getItems().isEmpty()) {
            throw new IllegalArgumentException("Sale must contain at least one item");
        }

        Business business = businessRepository.findByMerchantId(merchantId)
                .orElseThrow(() -> new IllegalArgumentException("Business not set up"));
        UUID businessId = business.getId();

        Map<UUID, Integer> qtyByProduct = new HashMap<>();
        for (SaleItemRequest item : req.getItems()) {
            qtyByProduct.merge(item.getProductId(), item.getQuantity(), Integer::sum);
        }

        Map<UUID, Product> productMap = new HashMap<>();
        for (UUID productId : qtyByProduct.keySet()) {
            Product p = productService.getByIdAndMerchant(productId, merchantId);
            if (p == null) {
                throw new IllegalArgumentException("Unknown product: " + productId);
            }
            if (p.isArchived()) {
                throw new IllegalArgumentException("Product archived: " + p.getName());
            }
            int desired = qtyByProduct.get(productId);
            int current = p.getStockQuantity() == null ? 0 : p.getStockQuantity();
            if (current < desired) {
                throw new IllegalArgumentException("Insufficient stock for " + p.getName());
            }
            productMap.put(productId, p);
        }

        BigDecimal subtotal = BigDecimal.ZERO;
        int itemCount = 0;
        List<SaleItem> createdItems = new ArrayList<>();

        for (SaleItemRequest item : req.getItems()) {
            Product product = productMap.get(item.getProductId());
            BigDecimal unitPrice = product.getSellingPrice() == null
                    ? BigDecimal.ZERO
                    : product.getSellingPrice();
            BigDecimal lineTotal = unitPrice.multiply(BigDecimal.valueOf(item.getQuantity()));
            subtotal = subtotal.add(lineTotal);
            itemCount += item.getQuantity();

            SaleItem si = new SaleItem();
            si.setSaleId(null);
            si.setProductId(product.getId());
            si.setProductName(product.getName());
            si.setProductSku(product.getSku());
            si.setQuantity(item.getQuantity());
            si.setUnitPrice(unitPrice);
            si.setLineTotal(lineTotal);
            createdItems.add(si);

            productService.decrementStock(product.getId(), merchantId, item.getQuantity());
        }

        BigDecimal discount = req.getDiscount() == null ? BigDecimal.ZERO : req.getDiscount();
        if (discount.signum() < 0) discount = BigDecimal.ZERO;
        if (discount.compareTo(subtotal) > 0) discount = subtotal;

        BigDecimal taxableBase = subtotal.subtract(discount);
        BigDecimal tax = taxableBase.multiply(TAX_RATE).setScale(2, RoundingMode.HALF_UP);

        BigDecimal total = subtotal.subtract(discount).add(tax).setScale(2, RoundingMode.HALF_UP);

        BigDecimal cashReceived = null;
        BigDecimal changeGiven = null;
        if (req.getPaymentMethod() == PaymentMethod.CASH) {
            cashReceived = req.getCashReceived() == null ? BigDecimal.ZERO : req.getCashReceived();
            changeGiven = cashReceived.subtract(total).max(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);
            if (cashReceived.compareTo(total) < 0) {
                throw new IllegalArgumentException("Cash received is less than total");
            }
        }

        Sale sale = new Sale();
        sale.setMerchantId(merchantId);
        sale.setBusinessId(businessId);
        sale.setTxnNumber(generateTxnNumber());
        sale.setCustomerName(req.getCustomerName());
        sale.setSubtotal(subtotal.setScale(2, RoundingMode.HALF_UP));
        sale.setDiscount(discount.setScale(2, RoundingMode.HALF_UP));
        sale.setTax(tax);
        sale.setTotal(total);
        sale.setPaymentMethod(req.getPaymentMethod());
        sale.setCashReceived(cashReceived == null ? null : cashReceived.setScale(2, RoundingMode.HALF_UP));
        sale.setChangeGiven(changeGiven);
        sale.setStatus(TransactionStatus.COMPLETED);
        sale.setNote(req.getNote());
        sale.setItemCount(itemCount);

        Sale saved = saleRepository.save(sale);

        for (SaleItem si : createdItems) {
            si.setSaleId(saved.getId());
        }
        List<SaleItem> savedItems = saleItemRepository.saveAll(createdItems);

        return SaleResponse.from(saved, DEFAULT_CURRENCY, savedItems);
    }

    @Transactional(readOnly = true)
    public List<SaleResponse> listSales(UUID merchantId, UUID businessId) {
        List<Sale> sales = businessId == null
                ? saleRepository.findByMerchantIdOrderByCreatedAtDesc(merchantId)
                : saleRepository.findByMerchantIdAndBusinessIdOrderByCreatedAtDesc(merchantId, businessId);

        if (sales.isEmpty()) return List.of();

        List<UUID> saleIds = sales.stream().map(Sale::getId).toList();
        Map<UUID, List<SaleItem>> itemsBySale = new HashMap<>();
        for (SaleItem si : saleItemRepository.findBySaleIdInOrderByCreatedAtAsc(saleIds)) {
            itemsBySale.computeIfAbsent(si.getSaleId(), k -> new ArrayList<>()).add(si);
        }

        List<SaleResponse> result = new ArrayList<>(sales.size());
        for (Sale s : sales) {
            List<SaleItem> items = itemsBySale.getOrDefault(s.getId(), List.of());
            result.add(SaleResponse.from(s, DEFAULT_CURRENCY, items));
        }
        return result;
    }

    @Transactional(readOnly = true)
    public SaleResponse getSale(UUID merchantId, UUID saleId) {
        Sale sale = saleRepository.findByIdAndMerchantId(saleId, merchantId)
                .orElseThrow(() -> new IllegalArgumentException("Sale not found"));
        List<SaleItem> items = saleItemRepository.findBySaleIdOrderByCreatedAtAsc(saleId);
        return SaleResponse.from(sale, DEFAULT_CURRENCY, items);
    }

    @Transactional(readOnly = true)
    public SaleReportDto getReport(UUID merchantId, String period) {
        PeriodRange range = resolvePeriod(period);
        String periodKey = range.period;
        String label = range.label;

        BigDecimal totalSales = Optional
                .ofNullable(saleRepository.sumTotalForMerchantBetween(merchantId, range.from, range.to))
                .orElse(BigDecimal.ZERO)
                .setScale(2, RoundingMode.HALF_UP);

        long totalOrders = saleRepository.countCompletedForMerchantBetween(merchantId, range.from, range.to);
        long itemsSold = saleRepository.sumItemCountForMerchantBetween(merchantId, range.from, range.to);

        BigDecimal averageTicket = totalOrders == 0
                ? BigDecimal.ZERO
                : totalSales.divide(BigDecimal.valueOf(totalOrders), 2, RoundingMode.HALF_UP);

        List<TopProductProjection> rows = saleItemRepository.findTopProducts(merchantId, range.from, range.to);
        List<SaleReportDto.TopProduct> top = new ArrayList<>();
        int limit = Math.min(rows.size(), 5);
        for (int i = 0; i < limit; i++) {
            TopProductProjection p = rows.get(i);
            top.add(new SaleReportDto.TopProduct(
                    p.getProductId() == null ? null : p.getProductId().toString(),
                    p.getProductName(),
                    p.getTotalQuantity(),
                    p.getTotalRevenue() == null ? BigDecimal.ZERO : p.getTotalRevenue().setScale(2, RoundingMode.HALF_UP)
            ));
        }

        return new SaleReportDto(periodKey, label, totalSales, totalOrders, itemsSold, averageTicket, top);
    }

    private PeriodRange resolvePeriod(String period) {
        if (period == null || period.isBlank()) period = "today";
        ZoneOffset offset = ZoneOffset.UTC;
        OffsetDateTime now = OffsetDateTime.now(offset);
        LocalDate today = now.toLocalDate();

        switch (period.toLowerCase()) {
            case "week": {
                LocalDate monday = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
                LocalDate sunday = monday.plusDays(6);
                OffsetDateTime from = monday.atStartOfDay().atOffset(offset);
                OffsetDateTime to = sunday.atTime(23, 59, 59).atOffset(offset);
                return new PeriodRange("week", "This Week", from, to);
            }
            case "month": {
                LocalDate first = today.withDayOfMonth(1);
                LocalDate last = first.with(TemporalAdjusters.lastDayOfMonth());
                OffsetDateTime from = first.atStartOfDay().atOffset(offset);
                OffsetDateTime to = last.atTime(23, 59, 59).atOffset(offset);
                return new PeriodRange("month", "This Month", from, to);
            }
            default: {
                OffsetDateTime from = today.atStartOfDay().atOffset(offset);
                OffsetDateTime to = today.atTime(23, 59, 59).atOffset(offset);
                return new PeriodRange("today", "Today", from, to);
            }
        }
    }

    private String generateTxnNumber() {
        String date = LocalDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        int random = ThreadLocalRandom.current().nextInt(1000, 9999);
        return "TXN-" + date + "-" + random;
    }

    private static class PeriodRange {
        final String period;
        final String label;
        final OffsetDateTime from;
        final OffsetDateTime to;

        PeriodRange(String period, String label, OffsetDateTime from, OffsetDateTime to) {
            this.period = period;
            this.label = label;
            this.from = from;
            this.to = to;
        }
    }
}