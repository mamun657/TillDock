package com.tilldock.auth.repository;

import com.tilldock.auth.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CategoryRepository extends JpaRepository<Category, UUID> {

    List<Category> findByBusinessIdOrderByNameAsc(UUID businessId);

    Optional<Category> findByIdAndBusinessId(UUID id, UUID businessId);

    @Query("SELECT c FROM Category c WHERE c.businessId = :businessId AND lower(c.name) = lower(:name)")
    Optional<Category> findByBusinessIdAndNameIgnoreCase(@Param("businessId") UUID businessId,
                                                         @Param("name") String name);
}
