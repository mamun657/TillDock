package com.example.tilldock;

import android.app.Application;

import com.example.tilldock.auth.AuthSession;
import com.example.tilldock.data.api.ApiClient;
import com.example.tilldock.data.repository.AuthRepository;
import com.example.tilldock.data.repository.BusinessRepository;
import com.example.tilldock.data.repository.CategoryRepository;
import com.example.tilldock.data.repository.InventoryRepository;
import com.example.tilldock.data.repository.ProductRepository;
import com.example.tilldock.data.repository.SalesRepository;
import com.example.tilldock.data.repository.TokenStore;

public class TillDockApplication extends Application {

    private static TillDockApplication instance;

    private TokenStore tokenStore;
    private AuthRepository authRepository;
    private BusinessRepository businessRepository;
    private CategoryRepository categoryRepository;
    private ProductRepository productRepository;
    private InventoryRepository inventoryRepository;
    private SalesRepository salesRepository;
    private ApiClient apiClient;
    private AuthSession authSession;
    private com.example.tilldock.ui.products.ProductViewModel productViewModel;
    private com.example.tilldock.ui.products.ProductFormViewModel productFormViewModel;
    private com.example.tilldock.ui.sales.NewSaleViewModel newSaleViewModel;
    private com.example.tilldock.ui.sales.PaymentViewModel paymentViewModel;
    private com.example.tilldock.ui.transactions.TransactionsViewModel transactionsViewModel;
    private com.example.tilldock.ui.transactions.TransactionDetailViewModel transactionDetailViewModel;
    private com.example.tilldock.ui.reports.ReportsViewModel reportsViewModel;
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
        salesRepository = new SalesRepository(apiClient.salesApi());
        authSession = new AuthSession(tokenStore, authRepository);
        authSession.bootstrap();
        productViewModel = new com.example.tilldock.ui.products.ProductViewModel();
        productFormViewModel = new com.example.tilldock.ui.products.ProductFormViewModel();
        newSaleViewModel = new com.example.tilldock.ui.sales.NewSaleViewModel();
        paymentViewModel = new com.example.tilldock.ui.sales.PaymentViewModel();
        transactionsViewModel = new com.example.tilldock.ui.transactions.TransactionsViewModel();
        transactionDetailViewModel = new com.example.tilldock.ui.transactions.TransactionDetailViewModel();
        reportsViewModel = new com.example.tilldock.ui.reports.ReportsViewModel();
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

    public SalesRepository getSalesRepository() {
        return salesRepository;
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

    public com.example.tilldock.ui.sales.NewSaleViewModel newSaleViewModel() {
        return newSaleViewModel;
    }

    public com.example.tilldock.ui.sales.PaymentViewModel paymentViewModel() {
        return paymentViewModel;
    }

    public com.example.tilldock.ui.transactions.TransactionsViewModel transactionsViewModel() {
        return transactionsViewModel;
    }

    public com.example.tilldock.ui.transactions.TransactionDetailViewModel transactionDetailViewModel() {
        return transactionDetailViewModel;
    }

    public com.example.tilldock.ui.reports.ReportsViewModel reportsViewModel() {
        return reportsViewModel;
    }
}
