package com.example.tilldock.ui.products;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.tilldock.data.model.Category;
import com.example.tilldock.data.model.Product;
import com.example.tilldock.data.model.StockStatus;
import com.example.tilldock.data.repository.CategoryRepository;
import com.example.tilldock.data.repository.ProductRepository;
import com.example.tilldock.utils.ApiError;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProductViewModel extends ViewModel {

    public enum Status {IDLE, LOADING, SUCCESS, EMPTY, ERROR, SAVED}

    public enum Sort {NAME_ASC, RECENT, STOCK_ASC}

    private final ProductRepository repository = com.example.tilldock.TillDockApplication.get().getProductRepository();
    private final CategoryRepository categoryRepository = com.example.tilldock.TillDockApplication.get().getCategoryRepository();

    private final MutableLiveData<List<Product>> sourceList = new MutableLiveData<>(Collections.emptyList());
    private final MutableLiveData<List<Product>> filteredList = new MutableLiveData<>(Collections.emptyList());
    private final MutableLiveData<Product> detail = new MutableLiveData<>();
    private final MutableLiveData<Status> status = new MutableLiveData<>(Status.IDLE);
    private final MutableLiveData<Status> detailStatus = new MutableLiveData<>(Status.IDLE);
    private final MutableLiveData<ApiError> error = new MutableLiveData<>();
    private final Map<String, String> categoryNames = new HashMap<>();

    private String searchTerm = "";
    private String statusFilter = "ALL";
    private boolean includeArchived = false;
    private Sort sort = Sort.NAME_ASC;

    public LiveData<List<Product>> getList() { return filteredList; }
    public LiveData<Product> getDetail() { return detail; }
    public LiveData<Status> getStatus() { return status; }
    public LiveData<Status> getDetailStatus() { return detailStatus; }
    public LiveData<ApiError> getError() { return error; }

    public void load() {
        status.postValue(Status.LOADING);
        error.postValue(null);
        ensureCategoriesLoaded();
        repository.list(includeArchived, null, null, null, "name", new ProductRepository.Callback<List<Product>>() {
            @Override
            public void onSuccess(List<Product> result) {
                if (result == null) result = Collections.emptyList();
                sourceList.postValue(result);
                status.postValue(result.isEmpty() ? Status.EMPTY : Status.SUCCESS);
                applyFilter();
            }

            @Override
            public void onFailure(ApiError err) {
                error.postValue(err);
                status.postValue(Status.ERROR);
            }
        });
    }

    private void ensureCategoriesLoaded() {
        if (!categoryNames.isEmpty()) return;
        categoryRepository.list(new CategoryRepository.Callback<List<Category>>() {
            @Override
            public void onSuccess(List<Category> result) {
                if (result == null) return;
                for (Category c : result) {
                    if (c != null && c.getId() != null) {
                        categoryNames.put(c.getId(), c.getName() == null ? "" : c.getName());
                    }
                }
            }

            @Override
            public void onFailure(ApiError err) {
            }
        });
    }

    public String categoryNameFor(String id) {
        if (id == null) return "";
        String n = categoryNames.get(id);
        return n == null ? "" : n;
    }

    public void refreshCategories() {
        categoryNames.clear();
        ensureCategoriesLoaded();
    }

    public void setSearchTerm(String value) {
        this.searchTerm = value == null ? "" : value.trim().toLowerCase();
        applyFilter();
    }

    public void setStatusFilter(String value) {
        this.statusFilter = value == null ? "ALL" : value;
        applyFilter();
    }

    public void setIncludeArchived(boolean value) {
        this.includeArchived = value;
        load();
    }

    public void setSort(Sort sort) {
        this.sort = sort == null ? Sort.NAME_ASC : sort;
        applyFilter();
    }

    private void applyFilter() {
        List<Product> source = sourceList.getValue();
        if (source == null) source = Collections.emptyList();
        List<Product> result = new ArrayList<>();
        for (Product product : source) {
            if (product == null) continue;
            if (!includeArchived && product.isArchived()) continue;
            if (!searchTerm.isEmpty()) {
                String name = product.getName() == null ? "" : product.getName().toLowerCase();
                String sku = product.getSku() == null ? "" : product.getSku().toLowerCase();
                if (!name.contains(searchTerm) && !sku.contains(searchTerm)) continue;
            }
            StockStatus st = product.getStockStatus();
            if (!"ALL".equals(statusFilter)) {
                if (st == null) continue;
                if ("IN_STOCK".equals(statusFilter) && st != StockStatus.IN_STOCK) continue;
                if ("LOW".equals(statusFilter) && st != StockStatus.LOW) continue;
                if ("OUT".equals(statusFilter) && st != StockStatus.OUT) continue;
            }
            result.add(product);
        }
        switch (sort) {
            case NAME_ASC:
                Collections.sort(result, (a, b) -> safeName(a).compareToIgnoreCase(safeName(b)));
                break;
            case STOCK_ASC:
                Collections.sort(result, (a, b) -> Integer.compare(a.getStockQuantity(), b.getStockQuantity()));
                break;
            case RECENT:
            default:
                Collections.sort(result, (a, b) -> safeTime(b).compareTo(safeTime(a)));
                break;
        }
        filteredList.postValue(result);
    }

    private String safeName(Product product) {
        return product.getName() == null ? "" : product.getName();
    }

    private String safeTime(Product product) {
        return product.getUpdatedAt() == null ? "" : product.getUpdatedAt();
    }

    public void loadDetail(String productId) {
        detailStatus.postValue(Status.LOADING);
        error.postValue(null);
        ensureCategoriesLoaded();
        repository.get(productId, new ProductRepository.Callback<Product>() {
            @Override
            public void onSuccess(Product result) {
                detail.postValue(result);
                detailStatus.postValue(Status.SUCCESS);
            }

            @Override
            public void onFailure(ApiError err) {
                error.postValue(err);
                detailStatus.postValue(Status.ERROR);
            }
        });
    }

    public void setArchived(String productId, boolean archived) {
        status.postValue(Status.LOADING);
        error.postValue(null);
        ProductRepository.Callback<Product> cb = new ProductRepository.Callback<Product>() {
            @Override
            public void onSuccess(Product result) {
                status.postValue(Status.SAVED);
                load();
            }

            @Override
            public void onFailure(ApiError err) {
                error.postValue(err);
                status.postValue(Status.ERROR);
            }
        };
        if (archived) {
            repository.archive(productId, cb);
        } else {
            repository.restore(productId, cb);
        }
    }

    public void deleteImage(String productId) {
        status.postValue(Status.LOADING);
        repository.deleteImage(productId, new ProductRepository.Callback<Product>() {
            @Override
            public void onSuccess(Product result) {
                status.postValue(Status.SAVED);
                loadDetail(productId);
            }

            @Override
            public void onFailure(ApiError err) {
                error.postValue(err);
                status.postValue(Status.ERROR);
            }
        });
    }
}