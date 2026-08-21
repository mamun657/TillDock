package com.tilldock.auth.service;

import com.tilldock.auth.dto.ProductDto;
import com.tilldock.auth.dto.ProductRequest;
import com.tilldock.auth.entity.Business;
import com.tilldock.auth.entity.Category;
import com.tilldock.auth.entity.InventoryMovement;
import com.tilldock.auth.entity.MovementType;
import com.tilldock.auth.entity.Product;
import com.tilldock.auth.entity.StockStatus;
import com.tilldock.auth.repository.BusinessRepository;
import com.tilldock.auth.repository.CategoryRepository;
import com.tilldock.auth.repository.InventoryMovementRepository;
import com.tilldock.auth.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
public class ProductService {

    private final ProductRepository products;
    private final CategoryRepository categories;
    private final BusinessRepository businesses;
    private final InventoryMovementRepository movements;

    public ProductService(ProductRepository products,
                          CategoryRepository categories,
                          BusinessRepository businesses,
                          InventoryMovementRepository movements) {
        this.products = products;
        this.categories = categories;
        this.businesses = businesses;
        this.movements = movements;
    }

    @Transactional(readOnly = true)
    public List<ProductDto> listForMerchant(UUID merchantId,
                                            boolean includeArchived,
                                            String query,
                                            UUID categoryId,
                                            StockStatus statusFilter,
                                            String sort) {
        UUID businessId = requireBusiness(merchantId).getId();
        List<Product> all = products.findByBusinessIdOrderByNameAsc(businessId);

        String q = query == null ? "" : query.trim().toLowerCase();
        List<Product> filtered = all.stream()
                .filter(p -> includeArchived || !p.isArchived())
                .filter(p -> q.isEmpty()
                        || (p.getName() != null && p.getName().toLowerCase().contains(q))
                        || (p.getSku() != null && p.getSku().toLowerCase().contains(q)))
                .filter(p -> categoryId == null || categoryId.equals(p.getCategoryId()))
                .filter(p -> statusFilter == null || p.stockStatus() == statusFilter)
                .toList();

        Comparator<Product> comparator = switch (sort == null ? "name" : sort) {
            case "recent" -> Comparator.comparing(Product::getUpdatedAt,
                    Comparator.nullsLast(Comparator.reverseOrder()));
            case "stock" -> Comparator.comparingInt(Product::getStockQuantity);
            default -> Comparator.comparing(Product::getName,
                    Comparator.nullsLast(Comparator.naturalOrder()));
        };

        return filtered.stream()
                .sorted(comparator)
                .map(ProductDto::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public ProductDto getForMerchant(UUID merchantId, UUID productId) {
        UUID businessId = requireBusiness(merchantId).getId();
        Product product = products.findByIdAndBusinessId(productId, businessId)
                .orElseThrow(AuthExceptions.ProductNotFoundException::new);
        return ProductDto.from(product);
    }

    @Transactional
    public ProductDto createForMerchant(UUID merchantId, ProductRequest req) {
        Business business = requireBusiness(merchantId);
        Category category = requireCategory(business.getId(), req.getCategoryId());
        String sku = req.getSku().trim();
        products.findByBusinessIdAndSkuIgnoreCase(business.getId(), sku).ifPresent(p -> {
            throw new AuthExceptions.DuplicateProductSkuException();
        });
        Product product = new Product();
        product.setBusinessId(business.getId());
        product.setCategoryId(category.getId());
        product.setName(req.getName().trim());
        product.setSku(sku);
        product.setDescription(trimToNull(req.getDescription()));
        product.setPurchasePrice(req.getPurchasePrice());
        product.setSellingPrice(req.getSellingPrice());
        product.setStockQuantity(req.getStockQuantity());
        product.setLowStockThreshold(req.getLowStockThreshold() == null ? 0 : req.getLowStockThreshold());
        product.setImageUrl(trimToNull(req.getImageUrl()));
        Product saved = products.save(product);
        recordInitialMovement(saved);
        return ProductDto.from(saved);
    }

    @Transactional
    public ProductDto updateForMerchant(UUID merchantId, UUID productId, ProductRequest req) {
        Business business = requireBusiness(merchantId);
        Product product = products.findByIdAndBusinessId(productId, business.getId())
                .orElseThrow(AuthExceptions.ProductNotFoundException::new);
        Category category = requireCategory(business.getId(), req.getCategoryId());
        String sku = req.getSku().trim();
        if (!product.getSku().equalsIgnoreCase(sku)) {
            products.findByBusinessIdAndSkuIgnoreCase(business.getId(), sku).ifPresent(existing -> {
                if (!existing.getId().equals(product.getId())) {
                    throw new AuthExceptions.DuplicateProductSkuException();
                }
            });
        }
        product.setCategoryId(category.getId());
        product.setName(req.getName().trim());
        product.setSku(sku);
        product.setDescription(trimToNull(req.getDescription()));
        product.setPurchasePrice(req.getPurchasePrice());
        product.setSellingPrice(req.getSellingPrice());
        int previousStock = product.getStockQuantity();
        int newStock = req.getStockQuantity();
        product.setStockQuantity(newStock);
        product.setLowStockThreshold(req.getLowStockThreshold() == null ? 0 : req.getLowStockThreshold());
        product.setImageUrl(trimToNull(req.getImageUrl()));
        Product saved = products.save(product);
        if (previousStock != newStock) {
            recordStockChangeMovement(saved, previousStock, newStock);
        }
        return ProductDto.from(saved);
    }

    @Transactional
    public void archiveForMerchant(UUID merchantId, UUID productId) {
        Business business = requireBusiness(merchantId);
        Product product = products.findByIdAndBusinessId(productId, business.getId())
                .orElseThrow(AuthExceptions.ProductNotFoundException::new);
        if (!product.isArchived()) {
            product.setArchived(true);
            product.setArchivedAt(OffsetDateTime.now());
            products.save(product);
        }
    }

    @Transactional
    public ProductDto restoreForMerchant(UUID merchantId, UUID productId) {
        Business business = requireBusiness(merchantId);
        Product product = products.findByIdAndBusinessId(productId, business.getId())
                .orElseThrow(AuthExceptions.ProductNotFoundException::new);
        if (product.isArchived()) {
            product.setArchived(false);
            product.setArchivedAt(null);
            products.save(product);
        }
        return ProductDto.from(product);
    }

    @Transactional
    public ProductDto updateImageUrl(UUID merchantId, UUID productId, String imageUrl) {
        Business business = requireBusiness(merchantId);
        Product product = products.findByIdAndBusinessId(productId, business.getId())
                .orElseThrow(AuthExceptions.ProductNotFoundException::new);
        product.setImageUrl(trimToNull(imageUrl));
        Product saved = products.save(product);
        return ProductDto.from(saved);
    }

    private Business requireBusiness(UUID merchantId) {
        return businesses.findByMerchantId(merchantId)
                .orElseThrow(AuthExceptions.BusinessSetupRequiredException::new);
    }

    private Category requireCategory(UUID businessId, UUID categoryId) {
        return categories.findByIdAndBusinessId(categoryId, businessId)
                .orElseThrow(AuthExceptions.CategoryNotFoundException::new);
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private void recordInitialMovement(Product saved) {
        InventoryMovement movement = new InventoryMovement();
        movement.setProductId(saved.getId());
        movement.setMovementType(MovementType.INITIAL);
        movement.setDelta(saved.getStockQuantity());
        movement.setPreviousQuantity(0);
        movement.setNewQuantity(saved.getStockQuantity());
        movement.setReason("Product created");
        movements.save(movement);
    }

    private void recordStockChangeMovement(Product saved, int previousStock, int newStock) {
        InventoryMovement movement = new InventoryMovement();
        movement.setProductId(saved.getId());
        movement.setMovementType(MovementType.ADJUSTMENT);
        movement.setDelta(newStock - previousStock);
        movement.setPreviousQuantity(previousStock);
        movement.setNewQuantity(newStock);
        movement.setReason("Product updated");
        movements.save(movement);
    }
}
