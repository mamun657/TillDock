package com.tilldock.auth.controller;

import com.tilldock.auth.dto.CategoryDto;
import com.tilldock.auth.dto.CategoryRequest;
import com.tilldock.auth.security.AuthenticatedMerchant;
import com.tilldock.auth.service.CategoryService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    public ResponseEntity<List<CategoryDto>> list(@AuthenticationPrincipal AuthenticatedMerchant principal) {
        if (principal == null) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(categoryService.listForMerchant(principal.id()));
    }

    @PostMapping
    public ResponseEntity<CategoryDto> create(@AuthenticationPrincipal AuthenticatedMerchant principal,
                                              @Valid @RequestBody CategoryRequest request) {
        if (principal == null) {
            return ResponseEntity.status(401).build();
        }
        CategoryDto dto = categoryService.createForMerchant(principal.id(), request);
        return ResponseEntity.status(201).body(dto);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CategoryDto> update(@AuthenticationPrincipal AuthenticatedMerchant principal,
                                              @PathVariable("id") UUID id,
                                              @Valid @RequestBody CategoryRequest request) {
        if (principal == null) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(categoryService.updateForMerchant(principal.id(), id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal AuthenticatedMerchant principal,
                                         @PathVariable("id") UUID id) {
        if (principal == null) {
            return ResponseEntity.status(401).build();
        }
        categoryService.deleteForMerchant(principal.id(), id);
        return ResponseEntity.noContent().build();
    }
}
