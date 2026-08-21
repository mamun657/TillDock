package com.tilldock.auth.controller;

import com.tilldock.auth.dto.ProductDto;
import com.tilldock.auth.dto.ProductRequest;
import com.tilldock.auth.entity.StockStatus;
import com.tilldock.auth.security.AuthenticatedMerchant;
import com.tilldock.auth.service.ProductImageService;
import com.tilldock.auth.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;
    private final ProductImageService imageService;

    public ProductController(ProductService productService, ProductImageService imageService) {
        this.productService = productService;
        this.imageService = imageService;
    }

    @GetMapping
    public ResponseEntity<List<ProductDto>> list(
            @AuthenticationPrincipal AuthenticatedMerchant principal,
            @RequestParam(value = "includeArchived", required = false, defaultValue = "false") boolean includeArchived,
            @RequestParam(value = "q", required = false) String query,
            @RequestParam(value = "categoryId", required = false) UUID categoryId,
            @RequestParam(value = "status", required = false) StockStatus status,
            @RequestParam(value = "sort", required = false) String sort) {
        if (principal == null) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(productService.listForMerchant(
                principal.id(), includeArchived, query, categoryId, status, sort));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductDto> get(@AuthenticationPrincipal AuthenticatedMerchant principal,
                                          @PathVariable("id") UUID id) {
        if (principal == null) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(productService.getForMerchant(principal.id(), id));
    }

    @PostMapping
    public ResponseEntity<ProductDto> create(@AuthenticationPrincipal AuthenticatedMerchant principal,
                                             @Valid @RequestBody ProductRequest request) {
        if (principal == null) {
            return ResponseEntity.status(401).build();
        }
        ProductDto dto = productService.createForMerchant(principal.id(), request);
        return ResponseEntity.status(201).body(dto);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductDto> update(@AuthenticationPrincipal AuthenticatedMerchant principal,
                                             @PathVariable("id") UUID id,
                                             @Valid @RequestBody ProductRequest request) {
        if (principal == null) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(productService.updateForMerchant(principal.id(), id, request));
    }

    @PostMapping("/{id}/archive")
    public ResponseEntity<ProductDto> archive(@AuthenticationPrincipal AuthenticatedMerchant principal,
                                              @PathVariable("id") UUID id) {
        if (principal == null) {
            return ResponseEntity.status(401).build();
        }
        productService.archiveForMerchant(principal.id(), id);
        return ResponseEntity.ok(productService.getForMerchant(principal.id(), id));
    }

    @PostMapping("/{id}/restore")
    public ResponseEntity<ProductDto> restore(@AuthenticationPrincipal AuthenticatedMerchant principal,
                                              @PathVariable("id") UUID id) {
        if (principal == null) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(productService.restoreForMerchant(principal.id(), id));
    }

    @PostMapping(value = "/{id}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ProductDto> uploadImage(@AuthenticationPrincipal AuthenticatedMerchant principal,
                                                  @PathVariable("id") UUID id,
                                                  @RequestPart("file") MultipartFile file) {
        if (principal == null) {
            return ResponseEntity.status(401).build();
        }
        String previous = productService.getForMerchant(principal.id(), id).getImageUrl();
        ProductImageService.StoredImage stored = imageService.store(file);
        try {
            return ResponseEntity.ok(productService.updateImageUrl(principal.id(), id, stored.url()));
        } catch (RuntimeException e) {
            imageService.delete(stored.url());
            throw e;
        } finally {
            if (previous != null && !previous.isBlank()) {
                imageService.delete(previous);
            }
        }
    }

    @DeleteMapping("/{id}/image")
    public ResponseEntity<ProductDto> deleteImage(@AuthenticationPrincipal AuthenticatedMerchant principal,
                                                  @PathVariable("id") UUID id) {
        if (principal == null) {
            return ResponseEntity.status(401).build();
        }
        String previous = productService.getForMerchant(principal.id(), id).getImageUrl();
        ProductDto updated = productService.updateImageUrl(principal.id(), id, null);
        if (previous != null && !previous.isBlank()) {
            imageService.delete(previous);
        }
        return ResponseEntity.ok(updated);
    }

    @PutMapping(value = "/{id}/image-url", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ProductDto> setImageUrl(@AuthenticationPrincipal AuthenticatedMerchant principal,
                                                  @PathVariable("id") UUID id,
                                                  @RequestBody Map<String, String> body) {
        if (principal == null) {
            return ResponseEntity.status(401).build();
        }
        String url = body == null ? null : body.get("imageUrl");
        return ResponseEntity.ok(productService.updateImageUrl(principal.id(), id, url));
    }
}
