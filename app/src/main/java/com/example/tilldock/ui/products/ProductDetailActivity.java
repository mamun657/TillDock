package com.example.tilldock.ui.products;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.tilldock.R;
import com.example.tilldock.TillDockApplication;
import com.example.tilldock.data.model.Product;
import com.example.tilldock.data.model.StockStatus;
import com.example.tilldock.utils.ApiError;
import com.example.tilldock.utils.ImageLoader;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import android.util.Log;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

public class ProductDetailActivity extends AppCompatActivity {

    public static final String EXTRA_PRODUCT_ID = "extra_product_id";

    private final ProductViewModel viewModel = TillDockApplication.get().productViewModel();
    private String productId;

    private ImageView imageView;
    private TextView nameView;
    private TextView skuCategoryView;
    private TextView statusPill;
    private TextView purchaseView;
    private TextView sellingView;
    private TextView marginView;
    private TextView stockView;
    private TextView thresholdView;
    private TextView descriptionView;
    private TextView errorText;
    private ProgressBar progress;
    private MaterialButton editButton;
    private MaterialButton inventoryButton;
    private MaterialButton archiveButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_product_detail);

        productId = getIntent().getStringExtra(EXTRA_PRODUCT_ID);
        if (productId == null) {
            finish();
            return;
        }

        imageView = findViewById(R.id.product_detail_image);
        nameView = findViewById(R.id.product_detail_name);
        skuCategoryView = findViewById(R.id.product_detail_sku_category);
        statusPill = findViewById(R.id.product_detail_status_pill);
        purchaseView = findViewById(R.id.product_detail_purchase_price);
        sellingView = findViewById(R.id.product_detail_selling_price);
        marginView = findViewById(R.id.product_detail_margin);
        stockView = findViewById(R.id.product_detail_stock);
        thresholdView = findViewById(R.id.product_detail_threshold);
        descriptionView = findViewById(R.id.product_detail_description);
        errorText = findViewById(R.id.product_detail_error_text);
        progress = findViewById(R.id.product_detail_progress);
        editButton = findViewById(R.id.product_detail_button_edit);
        inventoryButton = findViewById(R.id.product_detail_button_inventory);
        archiveButton = findViewById(R.id.product_detail_button_archive);

        ImageButton backButton = findViewById(R.id.product_detail_back);
        backButton.setOnClickListener(v -> finish());

        editButton.setOnClickListener(v -> openEdit());
        inventoryButton.setOnClickListener(v -> openInventory());
        archiveButton.setOnClickListener(v -> toggleArchive());

        viewModel.getDetail().observe(this, this::renderProduct);
        viewModel.getStatus().observe(this, this::renderStatus);
        viewModel.getError().observe(this, this::renderError);

        viewModel.loadDetail(productId);
    }

    @Override
    protected void onResume() {
        super.onResume();
        viewModel.loadDetail(productId);
    }

    private void renderProduct(Product product) {
        if (product == null) return;
        nameView.setText(safe(product.getName(), ""));
        String sku = safe(product.getSku(), "");
        String category = product.getCategoryId() == null ? "" : viewModel.categoryNameFor(product.getCategoryId());
        String separator = !sku.isEmpty() && !category.isEmpty() ? " • " : "";
        skuCategoryView.setText(sku + separator + category);
        NumberFormat currency = NumberFormat.getCurrencyInstance(Locale.getDefault());
        BigDecimal purchase = product.getPurchasePrice();
        BigDecimal selling = product.getSellingPrice();
        purchaseView.setText(purchase != null ? currency.format(purchase) : "—");
        sellingView.setText(selling != null ? currency.format(selling) : "—");
        if (purchase != null && selling != null && purchase.signum() > 0) {
            BigDecimal margin = selling.subtract(purchase)
                    .multiply(BigDecimal.valueOf(100))
                    .divide(purchase, 1, java.math.RoundingMode.HALF_UP);
            marginView.setText(margin + "%");
        } else {
            marginView.setText("—");
        }
        Integer qty = product.getStockQuantity();
        stockView.setText(qty != null ? String.valueOf(qty) : "—");
        Integer threshold = product.getLowStockThreshold();
        thresholdView.setText(threshold != null ? String.valueOf(threshold) : "—");
        descriptionView.setText(safe(product.getDescription(), getString(R.string.products_label_description_empty)));
        String imageUrl = product.getImageUrl();
        Log.d("ProductDetail", "imageUrl=[" + imageUrl + "]");
        if (imageUrl != null && !imageUrl.isEmpty()) {
            ImageLoader.get().load(imageUrl, imageView);
        } else {
            imageView.setImageResource(R.drawable.ic_image_placeholder);
        }
        StockStatus status = product.getStockStatus();
        if (product.isArchived() || status == null) {
            statusPill.setBackgroundResource(R.drawable.bg_pill_archived);
            statusPill.setTextColor(getColor(R.color.stock_archived));
            statusPill.setText(getString(R.string.product_status_archived));
            archiveButton.setText(R.string.product_overflow_restore);
        } else if (status == StockStatus.IN_STOCK) {
            statusPill.setBackgroundResource(R.drawable.bg_pill_in_stock);
            statusPill.setTextColor(getColor(R.color.stock_success));
            statusPill.setText(getString(R.string.product_status_in_stock));
            archiveButton.setText(R.string.product_overflow_archive);
        } else if (status == StockStatus.LOW) {
            statusPill.setBackgroundResource(R.drawable.bg_pill_low);
            statusPill.setTextColor(getColor(R.color.stock_warning));
            statusPill.setText(getString(R.string.product_status_low));
            archiveButton.setText(R.string.product_overflow_archive);
        } else {
            statusPill.setBackgroundResource(R.drawable.bg_pill_out);
            statusPill.setTextColor(getColor(R.color.stock_danger));
            statusPill.setText(getString(R.string.product_status_out));
            archiveButton.setText(R.string.product_overflow_archive);
        }
    }

    private void renderStatus(ProductViewModel.Status status) {
        if (status == ProductViewModel.Status.LOADING) {
            progress.setVisibility(View.VISIBLE);
            errorText.setVisibility(View.GONE);
        } else {
            progress.setVisibility(View.GONE);
        }
    }

    private void renderError(ApiError error) {
        if (error == null) {
            errorText.setVisibility(View.GONE);
            return;
        }
        errorText.setVisibility(View.VISIBLE);
        errorText.setText(error.message());
    }

    private void openEdit() {
        Intent intent = new Intent(this, AddEditProductActivity.class);
        intent.putExtra(AddEditProductActivity.EXTRA_PRODUCT_ID, productId);
        startActivity(intent);
    }

    private void openInventory() {
        Intent intent = new Intent(this, com.example.tilldock.ui.inventory.StockMutationActivity.class);
        intent.putExtra(com.example.tilldock.ui.inventory.StockMutationActivity.EXTRA_PRODUCT_ID, productId);
        startActivity(intent);
    }

    private void toggleArchive() {
        Product product = viewModel.getDetail().getValue();
        if (product == null) return;
        boolean archived = product.isArchived();
        new MaterialAlertDialogBuilder(this)
                .setTitle(archived ? R.string.product_restore_confirm_title : R.string.product_archive_confirm_title)
                .setMessage(archived ? R.string.product_restore_confirm_message : R.string.product_archive_confirm_message)
                .setNegativeButton(R.string.common_cancel, null)
                .setPositiveButton(archived ? R.string.product_overflow_restore : R.string.product_overflow_archive,
                        (d, w) -> viewModel.setArchived(productId, !archived))
                .show();
    }

    private String safe(String value, String fallback) {
        return value == null ? fallback : value;
    }
}