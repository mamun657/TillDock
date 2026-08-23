package com.tilldock.auth.repository;

import com.tilldock.auth.entity.Sale;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SaleRepository extends JpaRepository<Sale, UUID> {

    List<Sale> findByMerchantIdOrderByCreatedAtDesc(UUID merchantId);

    List<Sale> findByMerchantIdAndBusinessIdOrderByCreatedAtDesc(UUID merchantId, UUID businessId);

    Optional<Sale> findByIdAndMerchantId(UUID id, UUID merchantId);

    Optional<Sale> findByMerchantIdAndTxnNumber(UUID merchantId, String txnNumber);

    @Query("SELECT COUNT(s) FROM Sale s WHERE s.merchantId = :merchantId")
    long countByMerchant(@Param("merchantId") UUID merchantId);

    @Query("SELECT COALESCE(SUM(s.total), 0) FROM Sale s " +
            "WHERE s.merchantId = :merchantId " +
            "AND s.status = com.tilldock.auth.entity.TransactionStatus.COMPLETED " +
            "AND s.createdAt >= :from AND s.createdAt < :to")
    java.math.BigDecimal sumTotalForMerchantBetween(
            @Param("merchantId") UUID merchantId,
            @Param("from") OffsetDateTime from,
            @Param("to") OffsetDateTime to);

    @Query("SELECT COUNT(s) FROM Sale s " +
            "WHERE s.merchantId = :merchantId " +
            "AND s.status = com.tilldock.auth.entity.TransactionStatus.COMPLETED " +
            "AND s.createdAt >= :from AND s.createdAt < :to")
    long countCompletedForMerchantBetween(
            @Param("merchantId") UUID merchantId,
            @Param("from") OffsetDateTime from,
            @Param("to") OffsetDateTime to);

    @Query("SELECT COALESCE(SUM(s.itemCount), 0) FROM Sale s " +
            "WHERE s.merchantId = :merchantId " +
            "AND s.status = com.tilldock.auth.entity.TransactionStatus.COMPLETED " +
            "AND s.createdAt >= :from AND s.createdAt < :to")
    long sumItemCountForMerchantBetween(
            @Param("merchantId") UUID merchantId,
            @Param("from") OffsetDateTime from,
            @Param("to") OffsetDateTime to);
}