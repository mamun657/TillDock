package com.tilldock.auth.controller;

import com.tilldock.auth.dto.SaleReportDto;
import com.tilldock.auth.dto.SaleRequest;
import com.tilldock.auth.dto.SaleResponse;
import com.tilldock.auth.security.AuthenticatedMerchant;
import com.tilldock.auth.service.SaleService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/sales")
public class SaleController {

    private final SaleService saleService;

    public SaleController(SaleService saleService) {
        this.saleService = saleService;
    }

    @PostMapping
    public ResponseEntity<SaleResponse> create(
            @AuthenticationPrincipal AuthenticatedMerchant principal,
            @Valid @RequestBody SaleRequest request) {
        if (principal == null) {
            return ResponseEntity.status(401).build();
        }
        SaleResponse response = saleService.createSale(principal.id(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<SaleResponse>> list(
            @AuthenticationPrincipal AuthenticatedMerchant principal,
            @RequestParam(value = "businessId", required = false) UUID businessId) {
        if (principal == null) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(saleService.listSales(principal.id(), businessId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SaleResponse> get(
            @AuthenticationPrincipal AuthenticatedMerchant principal,
            @PathVariable("id") UUID id) {
        if (principal == null) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(saleService.getSale(principal.id(), id));
    }

    @GetMapping("/reports")
    public ResponseEntity<SaleReportDto> report(
            @AuthenticationPrincipal AuthenticatedMerchant principal,
            @RequestParam(value = "period", required = false) String period) {
        if (principal == null) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(saleService.getReport(principal.id(), period));
    }
}