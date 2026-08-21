package com.tilldock.auth.repository;

import com.tilldock.auth.entity.Merchant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface MerchantRepository extends JpaRepository<Merchant, UUID> {

    @Query("SELECT m FROM Merchant m WHERE lower(m.email) = lower(:email)")
    Optional<Merchant> findByEmailIgnoreCase(@Param("email") String email);

    @Query("SELECT COUNT(m) > 0 FROM Merchant m WHERE lower(m.email) = lower(:email)")
    boolean existsByEmailIgnoreCase(@Param("email") String email);
}