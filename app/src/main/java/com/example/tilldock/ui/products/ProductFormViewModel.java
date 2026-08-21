package com.example.tilldock.ui.products;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.tilldock.TillDockApplication;
import com.example.tilldock.data.model.Category;
import com.example.tilldock.data.model.Product;
import com.example.tilldock.data.model.ProductRequest;
import com.example.tilldock.data.repository.CategoryRepository;
import com.example.tilldock.data.repository.ProductRepository;
import com.example.tilldock.utils.ApiError;
import com.example.tilldock.utils.ImageUtil;

import java.math.BigDecimal;
import java.util.List;

public class ProductFormViewModel extends ViewModel {

    public enum Status {IDLE, LOADING, SUCCESS, ERROR}

    public static class Draft {
        public String name;
        public String sku;
        public String description;
        public BigDecimal purchasePrice;
        public BigDecimal sellingPrice;
        public Integer stock;
        public Integer lowStockThreshold;
        public String categoryId;
        public String imageUrl;
    }

    private final CategoryRepository categoryRepository = TillDockApplication.get().getCategoryRepository();
    private final ProductRepository productRepository = TillDockApplication.get().getProductRepository();
    private final MutableLiveData<List<Category>> categories = new MutableLiveData<>();
    private final MutableLiveData<Draft> draft = new MutableLiveData<>();
    private final MutableLiveData<Status> status = new MutableLiveData<>(Status.IDLE);
    private final MutableLiveData<ApiError> error = new MutableLiveData<>();
    private final MutableLiveData<Boolean> completion = new MutableLiveData<>();

    public LiveData<List<Category>> getCategories() { return categories; }
    public LiveData<Draft> getDraft() { return draft; }
    public LiveData<Status> getStatus() { return status; }
    public LiveData<ApiError> getError() { return error; }
    public LiveData<Boolean> getCompletion() { return completion; }

    public void bootstrap(String productId) {
        completion.postValue(null);
        status.postValue(Status.IDLE);
        error.postValue(null);
        loadCategories();
        if (productId != null) {
            loadDraft(productId);
        } else {
            draft.setValue(new Draft());
        }
    }

    private void loadCategories() {
        categoryRepository.list(new CategoryRepository.Callback<List<Category>>() {
            @Override
            public void onSuccess(List<Category> result) {
                categories.postValue(result);
            }

            @Override
            public void onFailure(ApiError err) {
                error.postValue(err);
            }
        });
    }

    private void loadDraft(String productId) {
        status.postValue(Status.LOADING);
        productRepository.get(productId, new ProductRepository.Callback<Product>() {
            @Override
            public void onSuccess(Product product) {
                Draft d = new Draft();
                d.name = product.getName();
                d.sku = product.getSku();
                d.description = product.getDescription();
                d.purchasePrice = product.getPurchasePrice();
                d.sellingPrice = product.getSellingPrice();
                d.stock = product.getStockQuantity();
                d.lowStockThreshold = product.getLowStockThreshold();
                d.categoryId = product.getCategoryId();
                d.imageUrl = product.getImageUrl();
                draft.postValue(d);
                status.postValue(Status.IDLE);
            }

            @Override
            public void onFailure(ApiError err) {
                error.postValue(err);
                status.postValue(Status.ERROR);
            }
        });
    }

    public void save(String productId, ProductRequest request, ImageUtil.Prepared pendingImage, String existingImageUrl) {
        status.postValue(Status.LOADING);
        error.postValue(null);
        if (productId == null) {
            createProduct(request, pendingImage);
        } else {
            updateProduct(productId, request, pendingImage, existingImageUrl);
        }
    }

    private void createProduct(ProductRequest request, ImageUtil.Prepared pendingImage) {
        productRepository.create(request, new ProductRepository.Callback<Product>() {
            @Override
            public void onSuccess(Product product) {
                if (pendingImage != null) {
                    uploadAndFinish(product.getId(), pendingImage);
                } else {
                    status.postValue(Status.SUCCESS);
                    completion.postValue(true);
                }
            }

            @Override
            public void onFailure(ApiError err) {
                status.postValue(Status.ERROR);
                error.postValue(err);
            }
        });
    }

    private void updateProduct(String productId, ProductRequest request, ImageUtil.Prepared pendingImage, String existingImageUrl) {
        productRepository.update(productId, request, new ProductRepository.Callback<Product>() {
            @Override
            public void onSuccess(Product product) {
                if (pendingImage != null) {
                    uploadAndFinish(productId, pendingImage);
                } else {
                    status.postValue(Status.SUCCESS);
                    completion.postValue(true);
                }
            }

            @Override
            public void onFailure(ApiError err) {
                status.postValue(Status.ERROR);
                error.postValue(err);
            }
        });
    }

    private void uploadAndFinish(String productId, ImageUtil.Prepared prepared) {
        productRepository.uploadImage(productId, prepared.bytes, prepared.mimeType, prepared.filename,
                new ProductRepository.Callback<com.example.tilldock.data.model.Product>() {
                    @Override
                    public void onSuccess(Product result) {
                        status.postValue(Status.SUCCESS);
                        completion.postValue(true);
                    }

                    @Override
                    public void onFailure(ApiError err) {
                        status.postValue(Status.ERROR);
                        error.postValue(err);
                    }
                });
    }
}