package com.example.tilldock.ui.sales;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tilldock.R;
import com.example.tilldock.data.model.Product;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ProductPickerAdapter extends RecyclerView.Adapter<ProductPickerAdapter.Holder> {

    public interface Listener {
        void onProductSelected(Product product);
    }

    private final List<Product> data = new ArrayList<>();
    private final Listener listener;

    public ProductPickerAdapter(Listener listener) {
        this.listener = listener;
    }

    public void submit(List<Product> list) {
        data.clear();
        if (list != null) data.addAll(list);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_picker_product, parent, false);
        return new Holder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        Product product = data.get(position);
        holder.name.setText(product.getName() == null ? "" : product.getName());
        holder.sku.setText(product.getSku() == null ? "" : product.getSku());
        holder.price.setText(formatMoney(product.getSellingPrice()));
        int stock = product.getStockQuantity() == null ? 0 : product.getStockQuantity();
        holder.stock.setText(holder.itemView.getContext().getString(R.string.cart_in_stock_format, stock));
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onProductSelected(product);
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class Holder extends RecyclerView.ViewHolder {
        final TextView name;
        final TextView sku;
        final TextView price;
        final TextView stock;

        Holder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.picker_item_name);
            sku = itemView.findViewById(R.id.picker_item_sku);
            price = itemView.findViewById(R.id.picker_item_price);
            stock = itemView.findViewById(R.id.picker_item_stock);
        }
    }

    private static String formatMoney(BigDecimal value) {
        if (value == null) value = BigDecimal.ZERO;
        return String.format(Locale.US, "$%s", value.setScale(2, java.math.RoundingMode.HALF_UP).toPlainString());
    }
}