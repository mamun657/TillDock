package com.tilldock.auth.repository;

import com.tilldock.auth.entity.Business;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface BusinessRepository extends JpaRepository<Business, UUID> {

    Optional<Business> findByMerchantId(UUID merchantId);

    boolean existsByMerchantId(UUID merchantId);
}
