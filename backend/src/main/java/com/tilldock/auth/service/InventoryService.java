package com.tilldock.auth.service;

import com.tilldock.auth.dto.InventoryResponse;
import com.tilldock.auth.dto.StockAdjustmentRequest;
import com.tilldock.auth.dto.StockMovementResponse;
import com.tilldock.auth.dto.StockMutationRequest;
import com.tilldock.auth.dto.ThresholdRequest;
import com.tilldock.auth.entity.Business;
import com.tilldock.auth.entity.InventoryMovement;
import com.tilldock.auth.entity.MovementType;
import com.tilldock.auth.entity.Product;
import com.tilldock.auth.repository.BusinessRepository;
import com.tilldock.auth.repository.InventoryMovementRepository;
import com.tilldock.auth.repository.ProductRepository;
import com.tilldock.auth.repository.ProductStockUpdateRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class InventoryService {

    private final ProductRepository products;
    private final ProductStockUpdateRepository stockUpdates;
    private final InventoryMovementRepository movements;
    private final BusinessRepository businesses;

    public InventoryService(ProductRepository products,
                            ProductStockUpdateRepository stockUpdates,
                            InventoryMovementRepository movements,
                            BusinessRepository businesses) {
        this.products = products;
        this.stockUpdates = stockUpdates;
        this.movements = movements;
        this.businesses = businesses;
    }

    @Transactional(readOnly = true)
    public List<InventoryResponse> listForMerchant(UUID merchantId) {
        UUID businessId = requireBusiness(merchantId).getId();
        return products.findByBusinessIdOrderByNameAsc(businessId).stream()
                .map(InventoryResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public InventoryResponse getForMerchant(UUID merchantId, UUID productId) {
        UUID businessId = requireBusiness(merchantId).getId();
        Product product = products.findByIdAndBusinessId(productId, businessId)
                .orElseThrow(AuthExceptions.ProductNotFoundException::new);
        return InventoryResponse.from(product);
    }

    @Transactional
    public InventoryResponse stockIn(UUID merchantId, UUID productId, StockMutationRequest req) {
        UUID businessId = requireBusiness(merchantId).getId();
        Product product = requireProduct(businessId, productId);
        int quantity = req.getQuantity();
        int previous = product.getStockQuantity();
        int rows = stockUpdates.applyDelta(productId, businessId, quantity);
        if (rows == 0) {
            throw new AuthExceptions.ProductNotFoundException();
        }
        int newQty = previous + quantity;
        recordMovement(productId, MovementType.STOCK_IN, quantity, previous, newQty, req.getReason());
        return loadResponse(productId);
    }

    @Transactional
    public InventoryResponse stockOut(UUID merchantId, UUID productId, StockMutationRequest req) {
        UUID businessId = requireBusiness(merchantId).getId();
        Product product = requireProduct(businessId, productId);
        int quantity = req.getQuantity();
        int previous = product.getStockQuantity();
        int rows = stockUpdates.applyStockOut(productId, businessId, quantity);
        if (rows == 0) {
            throw new AuthExceptions.InsufficientStockException();
        }
        int newQty = previous - quantity;
        recordMovement(productId, MovementType.STOCK_OUT, -quantity, previous, newQty, req.getReason());
        return loadResponse(productId);
    }

    @Transactional
    public InventoryResponse adjust(UUID merchantId, UUID productId, StockAdjustmentRequest req) {
        UUID businessId = requireBusiness(merchantId).getId();
        Product product = requireProduct(businessId, productId);
        int newQty = req.getNewQuantity();
        int previous = product.getStockQuantity();
        int rows = stockUpdates.applySet(productId, businessId, newQty);
        if (rows == 0) {
            throw new AuthExceptions.ProductNotFoundException();
        }
        recordMovement(productId, MovementType.ADJUSTMENT, newQty - previous, previous, newQty, req.getReason());
        return loadResponse(productId);
    }

    @Transactional
    public InventoryResponse setThreshold(UUID merchantId, UUID productId, ThresholdRequest req) {
        UUID businessId = requireBusiness(merchantId).getId();
        requireProduct(businessId, productId);
        int rows = stockUpdates.applyThreshold(productId, businessId, req.getThreshold());
        if (rows == 0) {
            throw new AuthExceptions.ProductNotFoundException();
        }
        return loadResponse(productId);
    }

    @Transactional(readOnly = true)
    public List<StockMovementResponse> listMovements(UUID merchantId, UUID productId, int page, int size) {
        UUID businessId = requireBusiness(merchantId).getId();
        requireProduct(businessId, productId);
        int safePage = Math.max(0, page);
        int safeSize = Math.min(Math.max(size, 1), 100);
        return movements.findByProductIdOrderByCreatedAtDesc(productId, PageRequest.of(safePage, safeSize))
                .stream()
                .map(StockMovementResponse::from)
                .toList();
    }

    private Business requireBusiness(UUID merchantId) {
        return businesses.findByMerchantId(merchantId)
                .orElseThrow(AuthExceptions.BusinessSetupRequiredException::new);
    }

    private Product requireProduct(UUID businessId, UUID productId) {
        return products.findByIdAndBusinessId(productId, businessId)
                .orElseThrow(AuthExceptions.ProductNotFoundException::new);
    }

    private InventoryResponse loadResponse(UUID productId) {
        Product product = products.findById(productId)
                .orElseThrow(AuthExceptions.ProductNotFoundException::new);
        return InventoryResponse.from(product);
    }

    private void recordMovement(UUID productId, MovementType type, int delta,
                                int previous, int newQty, String reason) {
        InventoryMovement movement = new InventoryMovement();
        movement.setProductId(productId);
        movement.setMovementType(type);
        movement.setDelta(delta);
        movement.setPreviousQuantity(previous);
        movement.setNewQuantity(newQty);
        movement.setReason(trimToNull(reason));
        movements.save(movement);
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}