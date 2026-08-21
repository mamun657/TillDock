package com.example.tilldock.ui.inventory;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.tilldock.R;
import com.example.tilldock.TillDockApplication;
import com.example.tilldock.data.model.InventoryItem;
import com.example.tilldock.data.model.Product;
import com.example.tilldock.utils.ApiError;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class StockMutationActivity extends AppCompatActivity {

    public static final String EXTRA_PRODUCT_ID = "extra_product_id";

    private String productId;
    private InventoryViewModel viewModel;
    private ProgressBar progress;
    private TextView errorText;
    private TextView subtitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stock_mutation);

        productId = getIntent().getStringExtra(EXTRA_PRODUCT_ID);
        if (productId == null) {
            finish();
            return;
        }

        viewModel = new androidx.lifecycle.ViewModelProvider(this,
                com.example.tilldock.ui.ViewModelFactories.inventory()).get(InventoryViewModel.class);
        progress = findViewById(R.id.stock_mutation_progress);
        errorText = findViewById(R.id.stock_mutation_error_text);
        subtitle = findViewById(R.id.stock_mutation_subtitle);
        ImageButton back = findViewById(R.id.stock_mutation_back);
        back.setOnClickListener(v -> finish());

        MaterialButton stockInButton = findViewById(R.id.stock_mutation_button_stock_in);
        MaterialButton stockOutButton = findViewById(R.id.stock_mutation_button_stock_out);
        MaterialButton adjustButton = findViewById(R.id.stock_mutation_button_adjust);
        MaterialButton thresholdButton = findViewById(R.id.stock_mutation_button_threshold);

        stockInButton.setOnClickListener(v -> showMutationDialog(true));
        stockOutButton.setOnClickListener(v -> showMutationDialog(false));
        adjustButton.setOnClickListener(v -> showAdjustDialog());
        thresholdButton.setOnClickListener(v -> showThresholdDialog());

        viewModel.state().observe(this, this::render);
        viewModel.load();
    }

    private void render(InventoryViewModel.State state) {
        if (state == null) return;
        if (state.status == InventoryViewModel.Status.LOADING) {
            progress.setVisibility(View.VISIBLE);
            errorText.setVisibility(View.GONE);
        } else {
            progress.setVisibility(View.GONE);
        }
        if (state.status == InventoryViewModel.Status.SUCCESS && state.items != null) {
            InventoryItem match = null;
            for (InventoryItem item : state.items) {
                if (item.getProductId() != null && item.getProductId().equals(productId)) {
                    match = item;
                    break;
                }
            }
            if (match != null) {
                subtitle.setText(getString(R.string.stock_mutation_subtitle_format,
                        match.getName(), match.getStockQuantity() == null ? 0 : match.getStockQuantity()));
            } else {
                subtitle.setText(R.string.stock_mutation_subtitle_missing);
            }
        }
        if (state.status == InventoryViewModel.Status.ERROR) {
            errorText.setVisibility(View.VISIBLE);
            errorText.setText(state.error == null ? getString(R.string.error_unknown) : state.error.message());
        }
    }

    private void showMutationDialog(boolean isStockIn) {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_stock_mutation, null);
        TextInputLayout quantityLayout = view.findViewById(R.id.stock_mutation_input_quantity_layout);
        TextInputEditText quantityInput = view.findViewById(R.id.stock_mutation_input_quantity);
        TextInputEditText reasonInput = view.findViewById(R.id.stock_mutation_input_reason);
        quantityLayout.setHint(getString(R.string.inventory_label_quantity));
        new MaterialAlertDialogBuilder(this)
                .setTitle(isStockIn ? R.string.inventory_dialog_stock_in_title : R.string.inventory_dialog_stock_out_title)
                .setView(view)
                .setNegativeButton(R.string.common_cancel, null)
                .setPositiveButton(R.string.common_save, (d, w) -> {
                    String qty = textOf(quantityInput);
                    String reason = textOf(reasonInput);
                    String err = validateStockQuantity(qty);
                    if (err != null) {
                        errorText.setVisibility(View.VISIBLE);
                        errorText.setText(err);
                        return;
                    }
                    int value = Integer.parseInt(qty);
                    if (isStockIn) viewModel.stockIn(productId, value, reason);
                    else viewModel.stockOut(productId, value, reason);
                })
                .show();
    }

    private void showAdjustDialog() {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_stock_mutation, null);
        TextInputLayout quantityLayout = view.findViewById(R.id.stock_mutation_input_quantity_layout);
        TextInputEditText quantityInput = view.findViewById(R.id.stock_mutation_input_quantity);
        TextInputEditText reasonInput = view.findViewById(R.id.stock_mutation_input_reason);
        quantityLayout.setHint(getString(R.string.inventory_label_new_quantity));
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.inventory_dialog_adjust_title)
                .setView(view)
                .setNegativeButton(R.string.common_cancel, null)
                .setPositiveButton(R.string.common_save, (d, w) -> {
                    String qty = textOf(quantityInput);
                    String reason = textOf(reasonInput);
                    String err = validateStockQuantity(qty);
                    if (err != null) {
                        errorText.setVisibility(View.VISIBLE);
                        errorText.setText(err);
                        return;
                    }
                    int value = Integer.parseInt(qty);
                    viewModel.adjust(productId, value, reason);
                })
                .show();
    }

    private void showThresholdDialog() {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_threshold, null);
        TextInputEditText thresholdInput = view.findViewById(R.id.threshold_input_value);
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.inventory_dialog_threshold_title)
                .setView(view)
                .setNegativeButton(R.string.common_cancel, null)
                .setPositiveButton(R.string.common_save, (d, w) -> {
                    String value = textOf(thresholdInput);
                    String err = validateStockQuantity(value);
                    if (err != null) {
                        errorText.setVisibility(View.VISIBLE);
                        errorText.setText(err);
                        return;
                    }
                    int threshold = Integer.parseInt(value);
                    viewModel.setThreshold(productId, threshold);
                })
                .show();
    }

    private String validateStockQuantity(String text) {
        if (text.isEmpty()) return getString(R.string.error_product_stock_invalid);
        try {
            int qty = Integer.parseInt(text);
            if (qty < 0 || qty > 1_000_000) return getString(R.string.error_product_stock_invalid);
            return null;
        } catch (NumberFormatException ex) {
            return getString(R.string.error_product_stock_invalid);
        }
    }

    private String textOf(TextInputEditText editText) {
        CharSequence cs = editText.getText();
        return cs == null ? "" : cs.toString().trim();
    }
}