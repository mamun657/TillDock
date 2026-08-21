package com.example.tilldock.ui.inventory;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tilldock.R;
import com.example.tilldock.data.model.InventoryItem;
import com.example.tilldock.ui.ViewModelFactories;
import com.example.tilldock.utils.ApiError;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;

public class InventoryFragment extends Fragment {

    private InventoryViewModel viewModel;
    private InventoryAdapter adapter;
    private ProgressBar progress;
    private TextView errorText;
    private View emptyState;
    private TextView emptyTitle;
    private TextView emptyBody;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this, ViewModelFactories.inventory()).get(InventoryViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_inventory, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        TextView title = view.findViewById(R.id.inventory_title);
        TextView subtitle = view.findViewById(R.id.inventory_subtitle);
        progress = view.findViewById(R.id.inventory_progress);
        errorText = view.findViewById(R.id.inventory_error_text);
        emptyState = view.findViewById(R.id.inventory_empty_state);
        emptyTitle = view.findViewById(R.id.inventory_empty_title);
        emptyBody = view.findViewById(R.id.inventory_empty_body);

        title.setText(R.string.inventory_title);
        subtitle.setText(R.string.inventory_subtitle);
        emptyTitle.setText(R.string.inventory_empty_title);
        emptyBody.setText(R.string.inventory_empty_body);

        RecyclerView recycler = view.findViewById(R.id.inventory_recycler);
        recycler.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new InventoryAdapter(item -> showActions(item));
        recycler.setAdapter(adapter);

        viewModel.state().observe(getViewLifecycleOwner(), this::render);
        viewModel.load();
    }

    private void render(InventoryViewModel.State state) {
        if (state == null) return;
        if (state.mode == InventoryViewModel.Mode.MOVEMENTS) {
            if (state.status == InventoryViewModel.Status.SUCCESS || state.status == InventoryViewModel.Status.EMPTY) {
                MovementsBottomSheet.show(getChildFragmentManager(), state.movements);
            } else if (state.status == InventoryViewModel.Status.ERROR) {
                showError(messageOf(state.error));
            }
            return;
        }
        if (state.mode == InventoryViewModel.Mode.MUTATION) {
            if (state.status == InventoryViewModel.Status.MUTATED) {
                viewModel.load();
                return;
            }
            if (state.status == InventoryViewModel.Status.ERROR) {
                showError(messageOf(state.error));
                return;
            }
            progress.setVisibility(state.status == InventoryViewModel.Status.LOADING ? View.VISIBLE : View.GONE);
            return;
        }
        progress.setVisibility(state.status == InventoryViewModel.Status.LOADING ? View.VISIBLE : View.GONE);
        switch (state.status) {
            case IDLE:
                break;
            case LOADING:
                errorText.setVisibility(View.GONE);
                emptyState.setVisibility(View.GONE);
                break;
            case SUCCESS:
            case EMPTY:
                errorText.setVisibility(View.GONE);
                adapter.submit(state.items);
                boolean empty = state.items == null || state.items.isEmpty();
                emptyState.setVisibility(empty ? View.VISIBLE : View.GONE);
                break;
            case ERROR:
                errorText.setVisibility(View.VISIBLE);
                errorText.setText(messageOf(state.error));
                break;
        }
    }

    private void showActions(InventoryItem item) {
        String[] options = new String[]{
                getString(R.string.inventory_action_stock_in),
                getString(R.string.inventory_action_stock_out),
                getString(R.string.inventory_action_adjust),
                getString(R.string.inventory_action_threshold),
                getString(R.string.inventory_action_movements)
        };
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(item.getName())
                .setItems(options, (dialog, which) -> {
                    switch (which) {
                        case 0:
                            showStockMutationDialog(item, true);
                            break;
                        case 1:
                            showStockMutationDialog(item, false);
                            break;
                        case 2:
                            showAdjustDialog(item);
                            break;
                        case 3:
                            showThresholdDialog(item);
                            break;
                        case 4:
                            viewModel.loadMovements(item.getProductId());
                            break;
                    }
                })
                .setNegativeButton(R.string.common_cancel, null)
                .show();
    }

    private void showStockMutationDialog(InventoryItem item, boolean isStockIn) {
        View view = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_stock_mutation, null);
        TextInputEditText quantityInput = view.findViewById(R.id.stock_mutation_input_quantity);
        TextInputEditText reasonInput = view.findViewById(R.id.stock_mutation_input_reason);

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(isStockIn ? R.string.inventory_dialog_stock_in_title : R.string.inventory_dialog_stock_out_title)
                .setView(view)
                .setNegativeButton(R.string.common_cancel, null)
                .setPositiveButton(R.string.common_save, (d, w) -> {
                    String quantityText = textOf(quantityInput);
                    String reason = textOf(reasonInput);
                    String error = validateStockQuantity(quantityText);
                    if (error != null) {
                        showError(error);
                        return;
                    }
                    int qty = Integer.parseInt(quantityText);
                    if (isStockIn) {
                        viewModel.stockIn(item.getProductId(), qty, reason);
                    } else {
                        viewModel.stockOut(item.getProductId(), qty, reason);
                    }
                })
                .show();
    }

    private void showAdjustDialog(InventoryItem item) {
        View view = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_stock_mutation, null);
        com.google.android.material.textfield.TextInputLayout quantityLayout = view.findViewById(R.id.stock_mutation_input_quantity_layout);
        TextInputEditText quantityInput = view.findViewById(R.id.stock_mutation_input_quantity);
        TextInputEditText reasonInput = view.findViewById(R.id.stock_mutation_input_reason);
        quantityLayout.setHint(getString(R.string.inventory_label_new_quantity));
        Integer currentStock = item.getStockQuantity();
        if (currentStock != null) {
            quantityInput.setText(String.valueOf(currentStock));
        }

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.inventory_dialog_adjust_title)
                .setView(view)
                .setNegativeButton(R.string.common_cancel, null)
                .setPositiveButton(R.string.common_save, (d, w) -> {
                    String quantityText = textOf(quantityInput);
                    String reason = textOf(reasonInput);
                    String error = validateStockQuantity(quantityText);
                    if (error != null) {
                        showError(error);
                        return;
                    }
                    int qty = Integer.parseInt(quantityText);
                    viewModel.adjust(item.getProductId(), qty, reason);
                })
                .show();
    }

    private void showThresholdDialog(InventoryItem item) {
        View view = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_threshold, null);
        TextInputEditText thresholdInput = view.findViewById(R.id.threshold_input_value);
        if (item.getLowStockThreshold() != null) {
            thresholdInput.setText(String.valueOf(item.getLowStockThreshold()));
        }

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.inventory_dialog_threshold_title)
                .setView(view)
                .setNegativeButton(R.string.common_cancel, null)
                .setPositiveButton(R.string.common_save, (d, w) -> {
                    String thresholdText = textOf(thresholdInput);
                    String error = validateStockQuantity(thresholdText);
                    if (error != null) {
                        showError(error);
                        return;
                    }
                    int threshold = Integer.parseInt(thresholdText);
                    viewModel.setThreshold(item.getProductId(), threshold);
                })
                .show();
    }

    private void showError(String message) {
        errorText.setVisibility(View.VISIBLE);
        errorText.setText(message);
    }

    private String validateStockQuantity(String text) {
        if (text.isEmpty()) {
            return getString(R.string.error_product_stock_invalid);
        }
        int qty;
        try {
            qty = Integer.parseInt(text);
        } catch (NumberFormatException ex) {
            return getString(R.string.error_product_stock_invalid);
        }
        if (qty < 0 || qty > 1_000_000) {
            return getString(R.string.error_product_stock_invalid);
        }
        return null;
    }

    private String textOf(TextInputEditText editText) {
        CharSequence cs = editText.getText();
        return cs == null ? "" : cs.toString().trim();
    }

    private String messageOf(ApiError error) {
        if (error == null) return "";
        return error.message() == null ? getString(R.string.error_unknown) : error.message();
    }
}