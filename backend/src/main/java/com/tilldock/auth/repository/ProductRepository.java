package com.tilldock.auth.repository;

import com.tilldock.auth.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProductRepository extends JpaRepository<Product, UUID> {

    List<Product> findByBusinessIdOrderByNameAsc(UUID businessId);

    Optional<Product> findByIdAndBusinessId(UUID id, UUID businessId);

    boolean existsByBusinessIdAndCategoryId(UUID businessId, UUID categoryId);

    @Query("SELECT p FROM Product p WHERE p.businessId = :businessId AND lower(p.sku) = lower(:sku)")
    Optional<Product> findByBusinessIdAndSkuIgnoreCase(@Param("businessId") UUID businessId,
                                                       @Param("sku") String sku);
}

