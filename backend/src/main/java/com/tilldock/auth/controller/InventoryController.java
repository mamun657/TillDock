package com.tilldock.auth.controller;

import com.tilldock.auth.dto.InventoryResponse;
import com.tilldock.auth.dto.StockAdjustmentRequest;
import com.tilldock.auth.dto.StockMovementResponse;
import com.tilldock.auth.dto.StockMutationRequest;
import com.tilldock.auth.dto.ThresholdRequest;
import com.tilldock.auth.security.AuthenticatedMerchant;
import com.tilldock.auth.service.InventoryService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/inventory")
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @GetMapping
    public ResponseEntity<List<InventoryResponse>> list(@AuthenticationPrincipal AuthenticatedMerchant principal) {
        if (principal == null) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(inventoryService.listForMerchant(principal.id()));
    }

    @GetMapping("/{productId}")
    public ResponseEntity<InventoryResponse> get(@AuthenticationPrincipal AuthenticatedMerchant principal,
                                                 @PathVariable("productId") UUID productId) {
        if (principal == null) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(inventoryService.getForMerchant(principal.id(), productId));
    }

    @PostMapping("/{productId}/stock-in")
    public ResponseEntity<InventoryResponse> stockIn(@AuthenticationPrincipal AuthenticatedMerchant principal,
                                                     @PathVariable("productId") UUID productId,
                                                     @Valid @RequestBody StockMutationRequest request) {
        if (principal == null) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(inventoryService.stockIn(principal.id(), productId, request));
    }

    @PostMapping("/{productId}/stock-out")
    public ResponseEntity<InventoryResponse> stockOut(@AuthenticationPrincipal AuthenticatedMerchant principal,
                                                      @PathVariable("productId") UUID productId,
                                                      @Valid @RequestBody StockMutationRequest request) {
        if (principal == null) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(inventoryService.stockOut(principal.id(), productId, request));
    }

    @PostMapping("/{productId}/adjust")
    public ResponseEntity<InventoryResponse> adjust(@AuthenticationPrincipal AuthenticatedMerchant principal,
                                                    @PathVariable("productId") UUID productId,
                                                    @Valid @RequestBody StockAdjustmentRequest request) {
        if (principal == null) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(inventoryService.adjust(principal.id(), productId, request));
    }

    @PatchMapping("/{productId}/threshold")
    public ResponseEntity<InventoryResponse> setThreshold(@AuthenticationPrincipal AuthenticatedMerchant principal,
                                                          @PathVariable("productId") UUID productId,
                                                          @Valid @RequestBody ThresholdRequest request) {
        if (principal == null) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(inventoryService.setThreshold(principal.id(), productId, request));
    }

    @GetMapping("/{productId}/movements")
    public ResponseEntity<List<StockMovementResponse>> listMovements(@AuthenticationPrincipal AuthenticatedMerchant principal,
                                                                     @PathVariable("productId") UUID productId,
                                                                     @RequestParam(value = "page", defaultValue = "0") int page,
                                                                     @RequestParam(value = "size", defaultValue = "50") int size) {
        if (principal == null) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(inventoryService.listMovements(principal.id(), productId, page, size));
    }
}