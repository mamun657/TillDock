package com.example.tilldock.ui.products;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tilldock.R;
import com.example.tilldock.TillDockApplication;
import com.example.tilldock.data.model.Product;
import com.example.tilldock.ui.Nav;
import com.example.tilldock.ui.business.BusinessSetupActivity;
import com.example.tilldock.utils.ApiError;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.switchmaterial.SwitchMaterial;

import java.util.List;

public class ProductsFragment extends Fragment implements ProductAdapter.Listener {

    private final ProductViewModel viewModel = TillDockApplication.get().productViewModel();
    private RecyclerView recycler;
    private ProductAdapter adapter;
    private ProgressBar progress;
    private TextView emptyText;
    private TextView errorText;
    private SearchView search;
    private SwitchMaterial archivedSwitch;

    private ActivityResultLauncher<Intent> formLauncher;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_products_with_search, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        bindViews(view);

        Activity activity = requireActivity();
        formLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == Activity.RESULT_OK) viewModel.load();
        });

        adapter = new ProductAdapter(this, viewModel::categoryNameFor);
        recycler.setLayoutManager(new LinearLayoutManager(activity));
        recycler.setAdapter(adapter);

        search.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) { return false; }
            @Override
            public boolean onQueryTextChange(String newText) {
                viewModel.setSearchTerm(newText);
                return true;
            }
        });

        view.findViewById(R.id.products_filter_in_stock).setOnClickListener(v -> viewModel.setStatusFilter("IN_STOCK"));
        view.findViewById(R.id.products_filter_low).setOnClickListener(v -> viewModel.setStatusFilter("LOW"));
        view.findViewById(R.id.products_filter_out).setOnClickListener(v -> viewModel.setStatusFilter("OUT"));
        view.findViewById(R.id.products_filter_all).setOnClickListener(v -> viewModel.setStatusFilter("ALL"));
        AutoCompleteTextView sortInput = view.findViewById(R.id.products_sort_input);
        ArrayAdapter<String> sortAdapter = new ArrayAdapter<>(activity,
                android.R.layout.simple_list_item_1,
                new String[]{
                        getString(R.string.products_sort_name),
                        getString(R.string.products_sort_recent),
                        getString(R.string.products_sort_stock)
                });
        sortInput.setAdapter(sortAdapter);
        sortInput.setText(getString(R.string.products_sort_name), false);
        sortInput.setOnItemClickListener((parent, v, position, id) -> {
            ProductViewModel.Sort sort;
            if (position == 1) sort = ProductViewModel.Sort.RECENT;
            else if (position == 2) sort = ProductViewModel.Sort.STOCK_ASC;
            else sort = ProductViewModel.Sort.NAME_ASC;
            viewModel.setSort(sort);
        });

        archivedSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> viewModel.setIncludeArchived(isChecked));
        MaterialButton addButton = view.findViewById(R.id.products_button_add);
        addButton.setOnClickListener(v -> openForm(null));

        viewModel.getList().observe(getViewLifecycleOwner(), this::renderList);
        viewModel.getStatus().observe(getViewLifecycleOwner(), this::renderStatus);
        viewModel.getError().observe(getViewLifecycleOwner(), this::renderError);
        viewModel.load();
    }

    private void bindViews(View root) {
        recycler = root.findViewById(R.id.products_recycler);
        progress = root.findViewById(R.id.products_progress);
        emptyText = root.findViewById(R.id.products_empty_text);
        errorText = root.findViewById(R.id.products_error_text);
        search = root.findViewById(R.id.products_search);
        archivedSwitch = root.findViewById(R.id.products_switch_archived);
    }

    private void renderList(List<Product> products) {
        adapter.submit(products);
        boolean empty = products == null || products.isEmpty();
        emptyText.setVisibility(empty ? View.VISIBLE : View.GONE);
        recycler.setVisibility(empty ? View.GONE : View.VISIBLE);
    }

    private void renderStatus(ProductViewModel.Status status) {
        progress.setVisibility(status == ProductViewModel.Status.LOADING ? View.VISIBLE : View.GONE);
    }

    private void renderError(ApiError error) {
        if (error == null) {
            errorText.setVisibility(View.GONE);
            return;
        }
        errorText.setVisibility(View.VISIBLE);
        errorText.setText(error.message());
        if (error.kind() == ApiError.Kind.BUSINESS_SETUP_REQUIRED) {
            promptBusinessSetup();
        }
    }

    private void promptBusinessSetup() {
        Toast.makeText(requireContext(), R.string.business_setup_required_toast, Toast.LENGTH_LONG).show();
        Intent intent = new Intent(requireContext(), BusinessSetupActivity.class);
        startActivity(intent);
    }

    @Override
    public void onResume() {
        super.onResume();
        viewModel.load();
    }

    @Override
    public void onProductClick(Product product) {
        Nav.showProductDetail(requireActivity(), product.getId());
    }

    @Override
    public void onProductEdit(Product product) {
        openForm(product.getId());
    }

    @Override
    public void onProductInventory(Product product) {
        Intent intent = new Intent(requireContext(),
                com.example.tilldock.ui.inventory.StockMutationActivity.class);
        intent.putExtra(com.example.tilldock.ui.inventory.StockMutationActivity.EXTRA_PRODUCT_ID,
                product.getId());
        startActivity(intent);
    }

    @Override
    public void onProductArchiveToggle(Product product) {
        boolean target = !product.isArchived();
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(target ? R.string.product_archive_confirm_title : R.string.product_restore_confirm_title)
                .setMessage(target ? R.string.product_archive_confirm_message : R.string.product_restore_confirm_message)
                .setNegativeButton(R.string.common_cancel, null)
                .setPositiveButton(target ? R.string.product_overflow_archive : R.string.product_overflow_restore,
                        (d, w) -> viewModel.setArchived(product.getId(), target))
                .show();
    }

    @Override
    public void onProductChangeImage(Product product) {
        Intent intent = new Intent(requireContext(), AddEditProductActivity.class);
        intent.putExtra(AddEditProductActivity.EXTRA_PRODUCT_ID, product.getId());
        startActivity(intent);
    }

    @Override
    public void onProductRemoveImage(Product product) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.product_overflow_delete_image)
                .setMessage(R.string.product_image_remove_confirm)
                .setNegativeButton(R.string.common_cancel, null)
                .setPositiveButton(R.string.product_overflow_delete_image,
                        (d, w) -> viewModel.deleteImage(product.getId()))
                .show();
    }

    private void openForm(String productId) {
        Intent intent = new Intent(requireContext(), AddEditProductActivity.class);
        if (productId != null) intent.putExtra(AddEditProductActivity.EXTRA_PRODUCT_ID, productId);
        startActivity(intent);
    }
}