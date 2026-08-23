package com.example.tilldock.ui.sales;

import android.app.Dialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tilldock.R;
import com.example.tilldock.data.model.Product;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class ProductPickerBottomSheet extends BottomSheetDialogFragment {

    public interface Listener {
        void onProductPicked(Product product);
    }

    private static final String ARG_PRODUCTS_JSON = "products";

    private ProductPickerAdapter adapter;
    private Listener listener;
    private List<Product> source = new ArrayList<>();

    public static ProductPickerBottomSheet newInstance(List<Product> products) {
        ProductPickerBottomSheet sheet = new ProductPickerBottomSheet();
        sheet.source = products == null ? new ArrayList<>() : new ArrayList<>(products);
        return sheet;
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new BottomSheetDialog(requireContext(), getTheme());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_product_picker, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        TextView title = view.findViewById(R.id.picker_title);
        TextView empty = view.findViewById(R.id.picker_empty);
        EditText search = view.findViewById(R.id.picker_search);
        RecyclerView recycler = view.findViewById(R.id.picker_recycler);

        title.setText(R.string.picker_title);
        adapter = new ProductPickerAdapter(product -> {
            if (listener != null) listener.onProductPicked(product);
            dismissAllowingStateLoss();
        });
        recycler.setLayoutManager(new LinearLayoutManager(requireContext()));
        recycler.setAdapter(adapter);
        adapter.submit(source);
        empty.setVisibility(source.isEmpty() ? View.VISIBLE : View.GONE);

        search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String q = s == null ? "" : s.toString().trim().toLowerCase(Locale.US);
                if (q.isEmpty()) {
                    adapter.submit(source);
                    empty.setVisibility(source.isEmpty() ? View.VISIBLE : View.GONE);
                    return;
                }
                List<Product> filtered = new ArrayList<>();
                for (Product p : source) {
                    if (p == null) continue;
                    String n = p.getName() == null ? "" : p.getName().toLowerCase(Locale.US);
                    String sku = p.getSku() == null ? "" : p.getSku().toLowerCase(Locale.US);
                    if (n.contains(q) || sku.contains(q)) filtered.add(p);
                }
                adapter.submit(filtered);
                empty.setVisibility(filtered.isEmpty() ? View.VISIBLE : View.GONE);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }
}