package com.example.tilldock.ui.products;

import android.Manifest;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.tilldock.R;
import com.example.tilldock.TillDockApplication;
import com.example.tilldock.data.model.Category;
import com.example.tilldock.data.model.ProductRequest;
import com.example.tilldock.utils.ApiError;
import com.example.tilldock.utils.ImageLoader;
import com.example.tilldock.utils.ImagePicker;
import com.example.tilldock.utils.ImageUtil;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public class AddEditProductActivity extends AppCompatActivity {

    public static final String EXTRA_PRODUCT_ID = "extra_product_id";

    private final ProductFormViewModel viewModel = TillDockApplication.get().productFormViewModel();
    private String productId;
    private boolean isEdit;

    private TextInputLayout nameLayout;
    private TextInputLayout skuLayout;
    private TextInputLayout descriptionLayout;
    private TextInputLayout purchaseLayout;
    private TextInputLayout sellingLayout;
    private TextInputLayout stockLayout;
    private TextInputLayout thresholdLayout;
    private TextInputEditText nameInput;
    private TextInputEditText skuInput;
    private TextInputEditText descriptionInput;
    private TextInputEditText purchaseInput;
    private TextInputEditText sellingInput;
    private TextInputEditText stockInput;
    private TextInputEditText thresholdInput;
    private ChipGroup categoryChips;
    private ImageView imageView;
    private ProgressBar progress;
    private TextView errorText;
    private TextView titleView;
    private MaterialButton removeImageButton;

    private String selectedCategoryId;
    private ImageUtil.Prepared pendingImage;
    private String existingImageUrl;
    private boolean imageDirty;

    private ActivityResultLauncher<String> galleryLauncher;
    private ActivityResultLauncher<Uri> cameraLauncher;
    private ActivityResultLauncher<String> cameraPermissionLauncher;
    private static final int REQUEST_CAMERA_PERMISSION = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_product_form);

        productId = getIntent().getStringExtra(EXTRA_PRODUCT_ID);
        isEdit = productId != null;

        bindViews();
        registerPickers();

        ImageButton backButton = findViewById(R.id.product_form_back);
        backButton.setOnClickListener(v -> handleBack());
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                handleBack();
            }
        });

        MaterialButton saveButton = findViewById(R.id.product_form_button_save);
        saveButton.setOnClickListener(v -> attemptSave());

        MaterialButton cameraButton = findViewById(R.id.product_form_button_camera);
        MaterialButton galleryButton = findViewById(R.id.product_form_button_gallery);
        cameraButton.setOnClickListener(v -> launchCameraWithPermission());
        galleryButton.setOnClickListener(v -> galleryLauncher.launch("image/*"));
        removeImageButton.setOnClickListener(v -> clearImage());

        titleView.setText(isEdit ? R.string.products_edit : R.string.products_add_product);
        viewModel.getCategories().observe(this, this::renderCategories);
        viewModel.getDraft().observe(this, this::renderDraft);
        viewModel.getStatus().observe(this, this::renderStatus);
        viewModel.getError().observe(this, this::renderError);
        viewModel.getCompletion().observe(this, this::onCompletion);

        viewModel.bootstrap(isEdit ? productId : null);
    }

    private Uri pickCaptureUri() {
        try {
            return ImagePicker.createCaptureUri(this);
        } catch (Exception ex) {
            Toast.makeText(this, R.string.product_image_load_failed, Toast.LENGTH_SHORT).show();
            return Uri.EMPTY;
        }
    }

    private void launchCameraWithPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            cameraLauncher.launch(pickCaptureUri());
            return;
        }
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
            new com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
                    .setTitle(R.string.product_image_camera_title)
                    .setMessage(R.string.product_image_camera_rationale)
                    .setNegativeButton(R.string.common_cancel, null)
                    .setPositiveButton(R.string.common_continue_text,
                            (d, w) -> cameraPermissionLauncher.launch(Manifest.permission.CAMERA))
                    .show();
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    private void bindViews() {
        titleView = findViewById(R.id.product_form_title);
        nameLayout = findViewById(R.id.product_form_name_layout);
        skuLayout = findViewById(R.id.product_form_sku_layout);
        descriptionLayout = findViewById(R.id.product_form_description_layout);
        purchaseLayout = findViewById(R.id.product_form_purchase_layout);
        sellingLayout = findViewById(R.id.product_form_selling_layout);
        stockLayout = findViewById(R.id.product_form_stock_layout);
        thresholdLayout = findViewById(R.id.product_form_threshold_layout);
        nameInput = findViewById(R.id.product_form_input_name);
        skuInput = findViewById(R.id.product_form_input_sku);
        descriptionInput = findViewById(R.id.product_form_input_description);
        purchaseInput = findViewById(R.id.product_form_input_purchase_price);
        sellingInput = findViewById(R.id.product_form_input_selling_price);
        stockInput = findViewById(R.id.product_form_input_stock);
        thresholdInput = findViewById(R.id.product_form_input_threshold);
        categoryChips = findViewById(R.id.product_form_category_chips);
        imageView = findViewById(R.id.product_form_image);
        progress = findViewById(R.id.product_form_progress);
        errorText = findViewById(R.id.product_form_error_text);
        removeImageButton = findViewById(R.id.product_form_button_remove_image);
    }

    private void registerPickers() {
        cameraPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(), granted -> {
                    if (Boolean.TRUE.equals(granted)) {
                        cameraLauncher.launch(pickCaptureUri());
                    } else {
                        Toast.makeText(this, R.string.product_image_camera_denied, Toast.LENGTH_SHORT).show();
                    }
                });

        galleryLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
            if (uri == null) return;
            try {
                pendingImage = ImageUtil.fromUri(this, uri);
                imageView.setImageURI(uri);
                imageDirty = true;
                existingImageUrl = null;
                removeImageButton.setVisibility(View.VISIBLE);
            } catch (Exception ex) {
                Toast.makeText(this, R.string.product_image_load_failed, Toast.LENGTH_SHORT).show();
            }
        });

        cameraLauncher = registerForActivityResult(new ActivityResultContracts.TakePicture(), success -> {
            if (!Boolean.TRUE.equals(success)) return;
            try {
                Uri captureUri = ImagePicker.createCaptureUri(this);
                pendingImage = ImageUtil.fromUri(this, captureUri);
                imageView.setImageURI(captureUri);
                imageDirty = true;
                existingImageUrl = null;
                removeImageButton.setVisibility(View.VISIBLE);
            } catch (Exception ex) {
                Toast.makeText(this, R.string.product_image_load_failed, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void renderCategories(List<Category> categories) {
        categoryChips.removeAllViews();
        if (categories == null) return;
        for (Category category : categories) {
            Chip chip = new Chip(this);
            chip.setText(category.getName());
            chip.setCheckable(true);
            chip.setTag(category.getId());
            chip.setChipBackgroundColorResource(R.color.surface_default);
            chip.setTextColor(getColor(R.color.text_default));
            chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    selectedCategoryId = category.getId();
                }
            });
            categoryChips.addView(chip);
        }
        applyCategorySelection();
    }

    private void applyCategorySelection() {
        if (selectedCategoryId == null) return;
        for (int i = 0; i < categoryChips.getChildCount(); i++) {
            Chip chip = (Chip) categoryChips.getChildAt(i);
            if (selectedCategoryId.equals(chip.getTag())) {
                chip.setChecked(true);
                break;
            }
        }
    }

    private void renderDraft(ProductFormViewModel.Draft draft) {
        if (draft == null) return;
        if (TextUtils.isEmpty(nameInput.getText())) nameInput.setText(draft.name);
        if (TextUtils.isEmpty(skuInput.getText())) skuInput.setText(draft.sku);
        if (TextUtils.isEmpty(descriptionInput.getText())) descriptionInput.setText(draft.description);
        if (TextUtils.isEmpty(purchaseInput.getText()) && draft.purchasePrice != null) purchaseInput.setText(draft.purchasePrice.toPlainString());
        if (TextUtils.isEmpty(sellingInput.getText()) && draft.sellingPrice != null) sellingInput.setText(draft.sellingPrice.toPlainString());
        if (TextUtils.isEmpty(stockInput.getText()) && draft.stock != null) stockInput.setText(String.valueOf(draft.stock));
        if (TextUtils.isEmpty(thresholdInput.getText()) && draft.lowStockThreshold != null) thresholdInput.setText(String.valueOf(draft.lowStockThreshold));
        if (draft.categoryId != null) {
            selectedCategoryId = draft.categoryId;
            applyCategorySelection();
        }
        if (draft.imageUrl != null && !imageDirty) {
            existingImageUrl = draft.imageUrl;
            ImageLoader.get().load(existingImageUrl, imageView);
            removeImageButton.setVisibility(View.VISIBLE);
        }
    }

    private void renderStatus(ProductFormViewModel.Status status) {
        progress.setVisibility(status == ProductFormViewModel.Status.LOADING ? View.VISIBLE : View.GONE);
    }

    private void renderError(ApiError error) {
        if (error == null) {
            errorText.setVisibility(View.GONE);
            return;
        }
        errorText.setVisibility(View.VISIBLE);
        Map<String, String> fields = error.fieldErrors();
        if (fields != null && !fields.isEmpty()) {
            applyFieldErrors(fields);
            errorText.setText(R.string.error_form_fix_fields);
        } else {
            errorText.setText(error.message());
        }
    }

    private void applyFieldErrors(Map<String, String> errors) {
        clearLayoutErrors();
        if (errors.containsKey("name")) nameLayout.setError(errors.get("name"));
        if (errors.containsKey("sku")) skuLayout.setError(errors.get("sku"));
        if (errors.containsKey("description")) descriptionLayout.setError(errors.get("description"));
        if (errors.containsKey("purchasePrice")) purchaseLayout.setError(errors.get("purchasePrice"));
        if (errors.containsKey("sellingPrice")) sellingLayout.setError(errors.get("sellingPrice"));
        if (errors.containsKey("stockQuantity")) stockLayout.setError(errors.get("stockQuantity"));
        if (errors.containsKey("lowStockThreshold")) thresholdLayout.setError(errors.get("lowStockThreshold"));
    }

    private void clearLayoutErrors() {
        nameLayout.setError(null);
        skuLayout.setError(null);
        descriptionLayout.setError(null);
        purchaseLayout.setError(null);
        sellingLayout.setError(null);
        stockLayout.setError(null);
        thresholdLayout.setError(null);
    }

    private void onCompletion(Boolean success) {
        if (Boolean.TRUE.equals(success)) {
            setResult(RESULT_OK);
            finish();
        }
    }

    private void clearImage() {
        pendingImage = null;
        existingImageUrl = null;
        imageDirty = true;
        imageView.setImageResource(R.drawable.ic_image_placeholder);
        removeImageButton.setVisibility(View.GONE);
    }

    private void attemptSave() {
        clearLayoutErrors();
        ProductRequest request = collectRequest();
        if (request == null) return;
        viewModel.save(isEdit ? productId : null, request, pendingImage, existingImageUrl);
    }

    private ProductRequest collectRequest() {
        String name = textOf(nameInput);
        if (TextUtils.isEmpty(name)) {
            nameLayout.setError(getString(R.string.error_product_name_required));
            return null;
        }
        String sku = textOf(skuInput);
        if (TextUtils.isEmpty(sku)) {
            skuLayout.setError(getString(R.string.error_product_sku_required));
            return null;
        }
        String description = textOf(descriptionInput);
        String purchaseText = textOf(purchaseInput);
        String sellingText = textOf(sellingInput);
        String stockText = textOf(stockInput);
        String thresholdText = textOf(thresholdInput);
        BigDecimal purchase = parseDecimal(purchaseText);
        BigDecimal selling = parseDecimal(sellingText);
        Integer stock = parseInteger(stockText);
        Integer threshold = parseInteger(thresholdText);

        if (purchase == null || purchase.signum() < 0) {
            purchaseLayout.setError(getString(R.string.error_product_purchase_invalid));
            return null;
        }
        if (selling == null || selling.signum() < 0) {
            sellingLayout.setError(getString(R.string.error_product_selling_invalid));
            return null;
        }
        if (stock == null || stock < 0) {
            stockLayout.setError(getString(R.string.error_product_stock_invalid));
            return null;
        }
        if (threshold == null || threshold < 0) {
            thresholdLayout.setError(getString(R.string.error_product_threshold_invalid));
            return null;
        }
        if (selectedCategoryId == null) {
            Toast.makeText(this, R.string.error_product_category_required, Toast.LENGTH_SHORT).show();
            return null;
        }

        String imageUrl = (pendingImage == null && existingImageUrl != null && !imageDirty) ? existingImageUrl : null;
        ProductRequest request = new ProductRequest();
        request.setName(name);
        request.setSku(sku);
        request.setDescription(description);
        request.setPurchasePrice(purchase);
        request.setSellingPrice(selling);
        request.setStockQuantity(stock);
        request.setLowStockThreshold(threshold);
        request.setCategoryId(selectedCategoryId);
        request.setImageUrl(imageUrl);
        return request;
    }

    private BigDecimal parseDecimal(String raw) {
        if (TextUtils.isEmpty(raw)) return null;
        try {
            return new BigDecimal(raw.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Integer parseInteger(String raw) {
        if (TextUtils.isEmpty(raw)) return null;
        try {
            return Integer.parseInt(raw.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String textOf(TextInputEditText input) {
        return input.getText() == null ? "" : input.getText().toString().trim();
    }

    private void handleBack() {
        if (isDirty()) {
            new MaterialAlertDialogBuilder(this)
                    .setTitle(R.string.common_discard_changes_title)
                    .setMessage(R.string.common_discard_changes_message)
                    .setNegativeButton(R.string.common_cancel, null)
                    .setPositiveButton(R.string.common_discard, (d, w) -> finish())
                    .show();
            return;
        }
        finish();
    }

    private boolean isDirty() {
        if (imageDirty || pendingImage != null) return true;
        if (!TextUtils.isEmpty(textOf(nameInput))) return true;
        if (!TextUtils.isEmpty(textOf(skuInput))) return true;
        if (!TextUtils.isEmpty(textOf(descriptionInput))) return true;
        if (!TextUtils.isEmpty(textOf(purchaseInput))) return true;
        if (!TextUtils.isEmpty(textOf(sellingInput))) return true;
        if (!TextUtils.isEmpty(textOf(stockInput))) return true;
        if (!TextUtils.isEmpty(textOf(thresholdInput))) return true;
        return false;
    }
}