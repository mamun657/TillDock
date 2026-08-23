package com.example.tilldock.ui.sales;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tilldock.R;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.Holder> {

    public interface Listener {
        void onIncrement(String productId);

        void onDecrement(String productId);

        void onRemove(String productId);
    }

    private final List<CartLine> lines = new ArrayList<>();
    private final Listener listener;

    public CartAdapter(Listener listener) {
        this.listener = listener;
    }

    public void submit(List<CartLine> data) {
        lines.clear();
        if (data != null) lines.addAll(data);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_cart_product, parent, false);
        return new Holder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        CartLine line = lines.get(position);
        holder.name.setText(line.getName());
        if (line.getSku() == null || line.getSku().isEmpty()) {
            holder.sku.setVisibility(View.GONE);
        } else {
            holder.sku.setVisibility(View.VISIBLE);
            holder.sku.setText(line.getSku());
        }
        holder.price.setText(formatMoney(line.getUnitPrice()));
        holder.quantity.setText(String.valueOf(line.getQuantity()));
        holder.lineTotal.setText(formatMoney(line.lineTotal()));
        holder.stock.setText(holder.itemView.getContext().getString(R.string.cart_in_stock_format, line.getAvailableStock()));
        holder.increment.setOnClickListener(v -> {
            if (listener != null) listener.onIncrement(line.getProductId());
        });
        holder.decrement.setOnClickListener(v -> {
            if (listener != null) listener.onDecrement(line.getProductId());
        });
        holder.remove.setOnClickListener(v -> {
            if (listener != null) listener.onRemove(line.getProductId());
        });
    }

    @Override
    public int getItemCount() {
        return lines.size();
    }

    static class Holder extends RecyclerView.ViewHolder {
        final TextView name;
        final TextView sku;
        final TextView price;
        final TextView quantity;
        final TextView lineTotal;
        final TextView stock;
        final ImageButton increment;
        final ImageButton decrement;
        final ImageButton remove;

        Holder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.cart_item_name);
            sku = itemView.findViewById(R.id.cart_item_sku);
            price = itemView.findViewById(R.id.cart_item_unit_price);
            quantity = itemView.findViewById(R.id.cart_item_quantity);
            lineTotal = itemView.findViewById(R.id.cart_item_line_total);
            stock = itemView.findViewById(R.id.cart_item_stock);
            increment = itemView.findViewById(R.id.cart_item_increment);
            decrement = itemView.findViewById(R.id.cart_item_decrement);
            remove = itemView.findViewById(R.id.cart_item_remove);
        }
    }

    private static String formatMoney(BigDecimal value) {
        if (value == null) value = BigDecimal.ZERO;
        return String.format(Locale.US, "$%s", value.setScale(2, java.math.RoundingMode.HALF_UP).toPlainString());
    }
}