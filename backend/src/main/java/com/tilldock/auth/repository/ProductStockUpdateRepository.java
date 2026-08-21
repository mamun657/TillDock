package com.tilldock.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.tilldock.auth.entity.Product;

import java.util.UUID;

@Repository
public interface ProductStockUpdateRepository extends JpaRepository<Product, UUID> {

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Product p SET p.stockQuantity = p.stockQuantity + :delta, " +
           "p.updatedAt = CURRENT_TIMESTAMP " +
           "WHERE p.id = :id AND p.businessId = :businessId")
    int applyDelta(@Param("id") UUID id,
                   @Param("businessId") UUID businessId,
                   @Param("delta") int delta);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Product p SET p.stockQuantity = p.stockQuantity - :quantity, " +
           "p.updatedAt = CURRENT_TIMESTAMP " +
           "WHERE p.id = :id AND p.businessId = :businessId AND p.stockQuantity >= :quantity")
    int applyStockOut(@Param("id") UUID id,
                      @Param("businessId") UUID businessId,
                      @Param("quantity") int quantity);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Product p SET p.stockQuantity = :newQuantity, " +
           "p.updatedAt = CURRENT_TIMESTAMP " +
           "WHERE p.id = :id AND p.businessId = :businessId")
    int applySet(@Param("id") UUID id,
                 @Param("businessId") UUID businessId,
                 @Param("newQuantity") int newQuantity);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Product p SET p.lowStockThreshold = :threshold, " +
           "p.updatedAt = CURRENT_TIMESTAMP " +
           "WHERE p.id = :id AND p.businessId = :businessId")
    int applyThreshold(@Param("id") UUID id,
                       @Param("businessId") UUID businessId,
                       @Param("threshold") int threshold);
}