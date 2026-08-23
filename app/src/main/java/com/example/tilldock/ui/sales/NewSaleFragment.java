package com.example.tilldock.ui.sales;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tilldock.R;
import com.example.tilldock.TillDockApplication;
import com.example.tilldock.data.model.Product;
import com.example.tilldock.data.model.Sale;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.math.BigDecimal;
import java.util.Locale;

public class NewSaleFragment extends Fragment implements CartAdapter.Listener {

    private final NewSaleViewModel viewModel = TillDockApplication.get().newSaleViewModel();
    private CartAdapter cartAdapter;
    private RecyclerView cartRecycler;
    private RecyclerView pickerRecycler;
    private ProductPickerAdapter pickerAdapter;

    private ProgressBar progress;
    private TextView emptyText;
    private TextView errorText;
    private TextView subtotalText;
    private TextView discountText;
    private TextView taxText;
    private TextView totalText;
    private TextView itemCountText;
    private TextView pickerEmpty;

    private TextInputEditText customerInput;
    private TextInputEditText discountInput;
    private MaterialButton checkoutButton;
    private MaterialButton clearButton;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_new_sale, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        bindViews(view);

        cartAdapter = new CartAdapter(this);
        cartRecycler.setLayoutManager(new LinearLayoutManager(requireContext()));
        cartRecycler.setAdapter(cartAdapter);

        pickerAdapter = new ProductPickerAdapter(this::openPayment);
        pickerRecycler.setLayoutManager(new LinearLayoutManager(requireContext()));
        pickerRecycler.setAdapter(pickerAdapter);

        viewModel.getCart().observe(getViewLifecycleOwner(), lines -> {
            cartAdapter.submit(lines);
            boolean empty = lines == null || lines.isEmpty();
            emptyText.setVisibility(empty ? View.VISIBLE : View.GONE);
            cartRecycler.setVisibility(empty ? View.GONE : View.VISIBLE);
            refreshTotals();
        });

        viewModel.getDiscount().observe(getViewLifecycleOwner(), d -> refreshTotals());

        viewModel.getProducts().observe(getViewLifecycleOwner(), products -> {
            pickerAdapter.submit(products);
            boolean empty = products == null || products.isEmpty();
            pickerEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
            pickerRecycler.setVisibility(empty ? View.GONE : View.VISIBLE);
        });

        viewModel.getStatus().observe(getViewLifecycleOwner(), this::renderStatus);
        viewModel.getError().observe(getViewLifecycleOwner(), this::renderError);

        viewModel.getLastSale().observe(getViewLifecycleOwner(), this::onSaleCreated);

        customerInput.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                viewModel.setCustomerName(s == null ? "" : s.toString());
            }
        });

        discountInput.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                String raw = s == null ? "" : s.toString().trim();
                BigDecimal value = BigDecimal.ZERO;
                if (!raw.isEmpty()) {
                    try {
                        value = new BigDecimal(raw);
                    } catch (NumberFormatException ignored) {
                    }
                }
                viewModel.setDiscount(value);
                refreshTotals();
            }
        });

        clearButton.setOnClickListener(v -> viewModel.clearCart());

        checkoutButton.setOnClickListener(v -> {
            if (viewModel.isCartEmpty()) {
                Toast.makeText(requireContext(), R.string.cart_empty_warning, Toast.LENGTH_SHORT).show();
                return;
            }
            viewModel.setPaymentMethod(com.example.tilldock.data.model.PaymentMethod.CASH);
            viewModel.setCashReceived(viewModel.total());
            openPayment(null);
        });
    }

    private void openPayment(Product picked) {
        if (picked != null) {
            viewModel.addProduct(picked);
            return;
        }
        if (viewModel.isCartEmpty()) {
            Toast.makeText(requireContext(), R.string.cart_empty_warning, Toast.LENGTH_SHORT).show();
            return;
        }
        PaymentFragment.newInstance().show(getParentFragmentManager(), "payment");
    }

    @Override
    public void onResume() {
        super.onResume();
        viewModel.loadProducts();
    }

    public void resetCart() {
        viewModel.clearCart();
    }

    private void renderStatus(NewSaleViewModel.Status status) {
        progress.setVisibility(status == NewSaleViewModel.Status.LOADING_PRODUCTS ? View.VISIBLE : View.GONE);
        checkoutButton.setEnabled(status != NewSaleViewModel.Status.LOADING_PRODUCTS);
    }

    private void renderError(com.example.tilldock.utils.ApiError error) {
        if (error == null) {
            errorText.setVisibility(View.GONE);
            return;
        }
        errorText.setVisibility(View.VISIBLE);
        errorText.setText(error.message());
    }

    private void refreshTotals() {
        BigDecimal sub = viewModel.subtotal();
        BigDecimal discount = viewModel.getDiscount().getValue() == null ? BigDecimal.ZERO : viewModel.getDiscount().getValue();
        BigDecimal total = viewModel.total();
        int items = viewModel.itemCount();
        subtotalText.setText(formatMoney(sub));
        discountText.setText("-" + formatMoney(discount));
        taxText.setText(formatMoney(BigDecimal.ZERO));
        totalText.setText(formatMoney(total));
        itemCountText.setText(getString(R.string.cart_item_count_format, items));
        checkoutButton.setText(getString(R.string.cart_checkout_format, formatMoney(total)));
    }

    private void onSaleCreated(Sale sale) {
        if (sale == null) return;
        viewModel.clearCart();
        TransactionSuccessFragment.newInstance(sale).show(getParentFragmentManager(), "sale-success");
    }

    private void bindViews(View root) {
        cartRecycler = root.findViewById(R.id.cart_recycler);
        pickerRecycler = root.findViewById(R.id.cart_picker_recycler);
        progress = root.findViewById(R.id.cart_progress);
        emptyText = root.findViewById(R.id.cart_empty);
        errorText = root.findViewById(R.id.cart_error);
        subtotalText = root.findViewById(R.id.cart_subtotal_value);
        discountText = root.findViewById(R.id.cart_discount_value);
        taxText = root.findViewById(R.id.cart_tax_value);
        totalText = root.findViewById(R.id.cart_total_value);
        itemCountText = root.findViewById(R.id.cart_item_count);
        pickerEmpty = root.findViewById(R.id.cart_picker_empty);
        customerInput = root.findViewById(R.id.cart_customer_input);
        discountInput = root.findViewById(R.id.cart_discount_input);
        checkoutButton = root.findViewById(R.id.cart_checkout_button);
        clearButton = root.findViewById(R.id.cart_clear_button);
    }

    private static String formatMoney(BigDecimal value) {
        if (value == null) value = BigDecimal.ZERO;
        return String.format(Locale.US, "$%s", value.setScale(2, java.math.RoundingMode.HALF_UP).toPlainString());
    }

    @Override
    public void onIncrement(String productId) {
        viewModel.increment(productId);
    }

    @Override
    public void onDecrement(String productId) {
        viewModel.decrement(productId);
    }

    @Override
    public void onRemove(String productId) {
        viewModel.removeLine(productId);
    }

    private abstract static class SimpleTextWatcher implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }
    }
}
