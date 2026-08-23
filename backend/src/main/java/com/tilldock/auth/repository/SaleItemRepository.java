package com.tilldock.auth.repository;

import com.tilldock.auth.entity.SaleItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface SaleItemRepository extends JpaRepository<SaleItem, UUID> {

    List<SaleItem> findBySaleIdOrderByCreatedAtAsc(UUID saleId);

    List<SaleItem> findBySaleIdInOrderByCreatedAtAsc(List<UUID> saleIds);

    @Query("SELECT si.productId AS productId, si.productName AS productName, " +
            "SUM(si.quantity) AS totalQuantity, SUM(si.lineTotal) AS totalRevenue, " +
            "COUNT(si.id) AS orderCount " +
            "FROM SaleItem si, Sale s " +
            "WHERE si.saleId = s.id " +
            "AND s.merchantId = :merchantId " +
            "AND s.status = com.tilldock.auth.entity.TransactionStatus.COMPLETED " +
            "AND s.createdAt >= :from AND s.createdAt <= :to " +
            "GROUP BY si.productId, si.productName " +
            "ORDER BY SUM(si.quantity) DESC")
    List<TopProductProjection> findTopProducts(
            @Param("merchantId") java.util.UUID merchantId,
            @Param("from") java.time.OffsetDateTime from,
            @Param("to") java.time.OffsetDateTime to);

    interface TopProductProjection {
        UUID getProductId();
        String getProductName();
        Long getTotalQuantity();
        java.math.BigDecimal getTotalRevenue();
        Long getOrderCount();
    }
}