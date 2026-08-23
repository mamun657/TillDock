package com.example.tilldock.ui.sales;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.tilldock.TillDockApplication;
import com.example.tilldock.data.model.PaymentMethod;
import com.example.tilldock.data.model.Product;
import com.example.tilldock.data.model.Sale;
import com.example.tilldock.data.model.SaleItemRequest;
import com.example.tilldock.data.model.SaleRequest;
import com.example.tilldock.data.repository.ProductRepository;
import com.example.tilldock.data.repository.SalesRepository;
import com.example.tilldock.utils.ApiError;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class NewSaleViewModel extends ViewModel {

    public enum Status {IDLE, LOADING_PRODUCTS, READY, SUBMITTING, SUCCESS, ERROR}

    private final ProductRepository productRepository = TillDockApplication.get().getProductRepository();
    private final SalesRepository salesRepository = TillDockApplication.get().getSalesRepository();

    private final MutableLiveData<List<Product>> products = new MutableLiveData<>(Collections.emptyList());
    private final MutableLiveData<List<CartLine>> cart = new MutableLiveData<>(Collections.emptyList());
    private final MutableLiveData<String> customerName = new MutableLiveData<>("");
    private final MutableLiveData<BigDecimal> discount = new MutableLiveData<>(BigDecimal.ZERO);
    private final MutableLiveData<Status> status = new MutableLiveData<>(Status.IDLE);
    private final MutableLiveData<ApiError> error = new MutableLiveData<>();
    private final MutableLiveData<Sale> lastSale = new MutableLiveData<>();
    private final MutableLiveData<PaymentMethod> paymentMethod = new MutableLiveData<>(PaymentMethod.CASH);
    private final MutableLiveData<BigDecimal> cashReceived = new MutableLiveData<>(BigDecimal.ZERO);

    public interface SubmitListener {
        void onSubmitSuccess(Sale sale);
        void onSubmitError(String message);
    }

    private SubmitListener submitListener;

    public LiveData<List<Product>> getProducts() {
        return products;
    }

    public LiveData<List<CartLine>> getCart() {
        return cart;
    }

    public LiveData<String> getCustomerName() {
        return customerName;
    }

    public LiveData<BigDecimal> getDiscount() {
        return discount;
    }

    public LiveData<Status> getStatus() {
        return status;
    }

    public LiveData<ApiError> getError() {
        return error;
    }

    public LiveData<Sale> getLastSale() {
        return lastSale;
    }

    public LiveData<PaymentMethod> getPaymentMethod() {
        return paymentMethod;
    }

    public LiveData<BigDecimal> getCashReceived() {
        return cashReceived;
    }

    public void setSubmitListener(SubmitListener listener) {
        this.submitListener = listener;
    }

    public void loadProducts() {
        status.postValue(Status.LOADING_PRODUCTS);
        error.postValue(null);
        productRepository.list(false, null, null, "IN_STOCK", "name", new ProductRepository.Callback<List<Product>>() {
            @Override
            public void onSuccess(List<Product> result) {
                if (result == null) result = Collections.emptyList();
                List<Product> available = new ArrayList<>();
                for (Product p : result) {
                    if (p == null) continue;
                    if (p.isArchived()) continue;
                    if (p.getStockQuantity() == null || p.getStockQuantity() <= 0) continue;
                    available.add(p);
                }
                products.postValue(available);
                status.postValue(Status.READY);
            }

            @Override
            public void onFailure(ApiError err) {
                error.postValue(err);
                status.postValue(Status.ERROR);
            }
        });
    }

    public void addProduct(Product product) {
        if (product == null || product.getId() == null) return;
        List<CartLine> current = new ArrayList<>(cart.getValue() == null ? Collections.emptyList() : cart.getValue());
        int stock = product.getStockQuantity() == null ? 0 : product.getStockQuantity();
        BigDecimal price = product.getSellingPrice() == null ? BigDecimal.ZERO : product.getSellingPrice();
        int idx = indexOf(current, product.getId());
        if (idx >= 0) {
            CartLine line = current.get(idx);
            int next = Math.min(line.getQuantity() + 1, stock);
            line.setQuantity(next);
        } else {
            int qty = Math.min(1, stock);
            if (qty <= 0) return;
            current.add(new CartLine(
                    product.getId(),
                    product.getName() == null ? "" : product.getName(),
                    product.getSku() == null ? "" : product.getSku(),
                    price,
                    stock,
                    qty
            ));
        }
        cart.postValue(current);
    }

    public void increment(String productId) {
        List<CartLine> current = new ArrayList<>(cart.getValue() == null ? Collections.emptyList() : cart.getValue());
        int idx = indexOf(current, productId);
        if (idx < 0) return;
        CartLine line = current.get(idx);
        if (line.getQuantity() < line.getAvailableStock()) {
            line.setQuantity(line.getQuantity() + 1);
            cart.postValue(current);
        }
    }

    public void decrement(String productId) {
        List<CartLine> current = new ArrayList<>(cart.getValue() == null ? Collections.emptyList() : cart.getValue());
        int idx = indexOf(current, productId);
        if (idx < 0) return;
        CartLine line = current.get(idx);
        if (line.getQuantity() <= 1) {
            current.remove(idx);
        } else {
            line.setQuantity(line.getQuantity() - 1);
        }
        cart.postValue(current);
    }

    public void removeLine(String productId) {
        List<CartLine> current = new ArrayList<>(cart.getValue() == null ? Collections.emptyList() : cart.getValue());
        int idx = indexOf(current, productId);
        if (idx < 0) return;
        current.remove(idx);
        cart.postValue(current);
    }

    public void clearCart() {
        cart.postValue(new ArrayList<>());
        customerName.postValue("");
        discount.postValue(BigDecimal.ZERO);
        cashReceived.postValue(BigDecimal.ZERO);
        paymentMethod.postValue(PaymentMethod.CASH);
        lastSale.postValue(null);
        error.postValue(null);
    }

    public void setCustomerName(String name) {
        customerName.postValue(name == null ? "" : name);
    }

    public void setDiscount(BigDecimal value) {
        if (value == null) value = BigDecimal.ZERO;
        if (value.signum() < 0) value = BigDecimal.ZERO;
        discount.postValue(value);
    }

    public void setPaymentMethod(PaymentMethod method) {
        paymentMethod.postValue(method == null ? PaymentMethod.CASH : method);
    }

    public void setCashReceived(BigDecimal value) {
        if (value == null) value = BigDecimal.ZERO;
        if (value.signum() < 0) value = BigDecimal.ZERO;
        cashReceived.postValue(value);
    }

    public BigDecimal subtotal() {
        BigDecimal sum = BigDecimal.ZERO;
        List<CartLine> current = cart.getValue();
        if (current == null) return sum;
        for (CartLine line : current) {
            sum = sum.add(line.lineTotal());
        }
        return sum;
    }

    public BigDecimal total() {
        BigDecimal s = subtotal();
        BigDecimal d = discount.getValue() == null ? BigDecimal.ZERO : discount.getValue();
        BigDecimal after = s.subtract(d);
        if (after.signum() < 0) after = BigDecimal.ZERO;
        return after;
    }

    public int itemCount() {
        int count = 0;
        List<CartLine> current = cart.getValue();
        if (current == null) return 0;
        for (CartLine line : current) {
            count += line.getQuantity();
        }
        return count;
    }

    public boolean isCartEmpty() {
        List<CartLine> current = cart.getValue();
        return current == null || current.isEmpty();
    }

    public void submit() {
        if (isCartEmpty()) {
            error.postValue(new ApiError(ApiError.Kind.VALIDATION, "Cart is empty", Collections.emptyMap()));
            return;
        }
        List<CartLine> current = cart.getValue();
        List<SaleItemRequest> items = new ArrayList<>();
        for (CartLine line : current) {
            items.add(new SaleItemRequest(line.getProductId(), line.getQuantity()));
        }
        BigDecimal cash = cashReceived.getValue();
        SaleRequest request = new SaleRequest();
        request.setItems(items);
        request.setCustomerName(customerName.getValue());
        request.setDiscount(discount.getValue() == null ? BigDecimal.ZERO : discount.getValue());
        request.setNote(null);
        request.setPaymentMethod(paymentMethod.getValue() == null ? PaymentMethod.CASH : paymentMethod.getValue());
        request.setCashReceived(cash);
        status.postValue(Status.SUBMITTING);
        error.postValue(null);
        salesRepository.create(request, new SalesRepository.Callback<Sale>() {
            @Override
            public void onSuccess(Sale value) {
                lastSale.postValue(value);
                status.postValue(Status.SUCCESS);
                if (submitListener != null) submitListener.onSubmitSuccess(value);
            }

            @Override
            public void onFailure(ApiError err) {
                error.postValue(err);
                status.postValue(Status.ERROR);
                if (submitListener != null) submitListener.onSubmitError(err.message());
            }
        });
    }

    public void resetSubmitState() {
        if (status.getValue() == Status.SUCCESS || status.getValue() == Status.ERROR) {
            status.postValue(Status.READY);
        }
    }

    private int indexOf(List<CartLine> list, String productId) {
        for (int i = 0; i < list.size(); i++) {
            if (productId != null && productId.equals(list.get(i).getProductId())) return i;
        }
        return -1;
    }
}