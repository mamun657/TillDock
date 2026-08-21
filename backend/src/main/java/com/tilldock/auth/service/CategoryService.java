package com.tilldock.auth.service;

import com.tilldock.auth.dto.CategoryDto;
import com.tilldock.auth.dto.CategoryRequest;
import com.tilldock.auth.entity.Business;
import com.tilldock.auth.entity.Category;
import com.tilldock.auth.repository.BusinessRepository;
import com.tilldock.auth.repository.CategoryRepository;
import com.tilldock.auth.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class CategoryService {

    private final CategoryRepository categories;
    private final BusinessRepository businesses;
    private final ProductRepository products;

    public CategoryService(CategoryRepository categories, BusinessRepository businesses, ProductRepository products) {
        this.categories = categories;
        this.businesses = businesses;
        this.products = products;
    }

    @Transactional(readOnly = true)
    public List<CategoryDto> listForMerchant(UUID merchantId) {
        UUID businessId = requireBusiness(merchantId).getId();
        return categories.findByBusinessIdOrderByNameAsc(businessId).stream()
                .map(CategoryDto::from)
                .toList();
    }

    @Transactional
    public CategoryDto createForMerchant(UUID merchantId, CategoryRequest req) {
        Business business = requireBusiness(merchantId);
        String name = req.getName().trim();
        categories.findByBusinessIdAndNameIgnoreCase(business.getId(), name).ifPresent(c -> {
            throw new AuthExceptions.DuplicateCategoryNameException();
        });
        Category category = new Category();
        category.setBusinessId(business.getId());
        category.setName(name);
        category.setDescription(trimToNull(req.getDescription()));
        Category saved = categories.save(category);
        return CategoryDto.from(saved);
    }

    @Transactional
    public CategoryDto updateForMerchant(UUID merchantId, UUID categoryId, CategoryRequest req) {
        Business business = requireBusiness(merchantId);
        Category category = categories.findByIdAndBusinessId(categoryId, business.getId())
                .orElseThrow(AuthExceptions.CategoryNotFoundException::new);
        String name = req.getName().trim();
        if (!category.getName().equalsIgnoreCase(name)) {
            categories.findByBusinessIdAndNameIgnoreCase(business.getId(), name).ifPresent(existing -> {
                if (!existing.getId().equals(category.getId())) {
                    throw new AuthExceptions.DuplicateCategoryNameException();
                }
            });
        }
        category.setName(name);
        category.setDescription(trimToNull(req.getDescription()));
        Category saved = categories.save(category);
        return CategoryDto.from(saved);
    }

    @Transactional
    public void deleteForMerchant(UUID merchantId, UUID categoryId) {
        Business business = requireBusiness(merchantId);
        Category category = categories.findByIdAndBusinessId(categoryId, business.getId())
                .orElseThrow(AuthExceptions.CategoryNotFoundException::new);
        if (products.existsByBusinessIdAndCategoryId(business.getId(), category.getId())) {
            throw new AuthExceptions.CategoryHasProductsException();
        }
        categories.delete(category);
    }

    private Business requireBusiness(UUID merchantId) {
        return businesses.findByMerchantId(merchantId)
                .orElseThrow(AuthExceptions.BusinessSetupRequiredException::new);
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
