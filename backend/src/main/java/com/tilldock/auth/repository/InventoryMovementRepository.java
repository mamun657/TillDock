package com.tilldock.auth.repository;

import com.tilldock.auth.entity.InventoryMovement;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface InventoryMovementRepository extends JpaRepository<InventoryMovement, UUID> {

    List<InventoryMovement> findByProductIdOrderByCreatedAtDesc(UUID productId, Pageable pageable);
}