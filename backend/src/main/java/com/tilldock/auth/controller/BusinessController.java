package com.tilldock.auth.controller;

import com.tilldock.auth.dto.BusinessDto;
import com.tilldock.auth.dto.BusinessRequest;
import com.tilldock.auth.security.AuthenticatedMerchant;
import com.tilldock.auth.service.BusinessService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/business")
public class BusinessController {

    private final BusinessService businessService;

    public BusinessController(BusinessService businessService) {
        this.businessService = businessService;
    }

    @GetMapping
    public ResponseEntity<BusinessDto> get(@AuthenticationPrincipal AuthenticatedMerchant principal) {
        if (principal == null) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(businessService.getForMerchant(principal.id()));
    }

    @PutMapping
    public ResponseEntity<BusinessDto> upsert(@AuthenticationPrincipal AuthenticatedMerchant principal,
                                              @Valid @RequestBody BusinessRequest request) {
        if (principal == null) {
            return ResponseEntity.status(401).build();
        }
        BusinessDto dto = businessService.upsertForMerchant(principal.id(), request);
        return ResponseEntity.ok(dto);
    }

    @DeleteMapping
    public ResponseEntity<Void> delete(@AuthenticationPrincipal AuthenticatedMerchant principal) {
        if (principal == null) {
            return ResponseEntity.status(401).build();
        }
        businessService.deleteForMerchant(principal.id());
        return ResponseEntity.noContent().build();
    }
}
