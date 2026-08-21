package com.example.tilldock;

import android.app.Application;

import com.example.tilldock.auth.AuthSession;
import com.example.tilldock.data.api.ApiClient;
import com.example.tilldock.data.repository.AuthRepository;
import com.example.tilldock.data.repository.BusinessRepository;
import com.example.tilldock.data.repository.CategoryRepository;
import com.example.tilldock.data.repository.InventoryRepository;
import com.example.tilldock.data.repository.ProductRepository;
import com.example.tilldock.data.repository.TokenStore;

public class TillDockApplication extends Application {

    private static TillDockApplication instance;

    private TokenStore tokenStore;
    private AuthRepository authRepository;
    private BusinessRepository businessRepository;
    private CategoryRepository categoryRepository;
    private ProductRepository productRepository;
    private InventoryRepository inventoryRepository;
    private ApiClient apiClient;
    private AuthSession authSession;
    private com.example.tilldock.ui.products.ProductViewModel productViewModel;
    private com.example.tilldock.ui.products.ProductFormViewModel productFormViewModel;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;

        String baseUrl = BuildConfig.API_BASE_URL;
        tokenStore = new TokenStore(this);
        apiClient = new ApiClient(baseUrl, tokenStore);
        authRepository = new AuthRepository(apiClient.authApi(), tokenStore);
        businessRepository = new BusinessRepository(apiClient.businessApi());
        categoryRepository = new CategoryRepository(apiClient.categoryApi());
        productRepository = new ProductRepository(apiClient.productApi());
        inventoryRepository = new InventoryRepository(apiClient.inventoryApi());
        authSession = new AuthSession(tokenStore, authRepository);
        authSession.bootstrap();
        productViewModel = new com.example.tilldock.ui.products.ProductViewModel();
        productFormViewModel = new com.example.tilldock.ui.products.ProductFormViewModel();
    }

    public static TillDockApplication get() {
        return instance;
    }

    public TokenStore getTokenStore() {
        return tokenStore;
    }

    public AuthRepository getAuthRepository() {
        return authRepository;
    }

    public BusinessRepository getBusinessRepository() {
        return businessRepository;
    }

    public CategoryRepository getCategoryRepository() {
        return categoryRepository;
    }

    public ProductRepository getProductRepository() {
        return productRepository;
    }

    public InventoryRepository getInventoryRepository() {
        return inventoryRepository;
    }

    public ApiClient getApiClient() {
        return apiClient;
    }

    public AuthSession getAuthSession() {
        return authSession;
    }

    public com.example.tilldock.ui.products.ProductViewModel productViewModel() {
        return productViewModel;
    }

    public com.example.tilldock.ui.products.ProductFormViewModel productFormViewModel() {
        return productFormViewModel;
    }
}
