package com.example.tilldock.ui.inventory;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tilldock.R;
import com.example.tilldock.data.model.InventoryItem;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class InventoryAdapter extends RecyclerView.Adapter<InventoryAdapter.VH> {

    public interface Listener {
        void onItem(InventoryItem item);
    }

    private final List<InventoryItem> items = new ArrayList<>();
    private final Listener listener;

    public InventoryAdapter(Listener listener) {
        this.listener = listener;
    }

    public void submit(List<InventoryItem> next) {
        items.clear();
        if (next != null) items.addAll(next);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_inventory, parent, false);
        return new VH(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        InventoryItem item = items.get(position);
        holder.name.setText(item.getName());
        holder.sku.setText(item.getSku());
        Integer stock = item.getStockQuantity();
        Integer threshold = item.getLowStockThreshold();
        Context ctx = holder.itemView.getContext();
        holder.stock.setText(ctx.getString(R.string.inventory_stock_value, stock == null ? 0 : stock));
        holder.threshold.setText(ctx.getString(R.string.inventory_threshold_value, threshold == null ? 0 : threshold));
        String status = item.getStatus() == null ? "" : item.getStatus();
        holder.status.setText(ctx.getString(R.string.inventory_status_value, status));
        int color;
        if ("OUT_OF_STOCK".equals(status)) {
            color = ContextCompat.getColor(ctx, R.color.stock_danger);
        } else if ("LOW_STOCK".equals(status)) {
            color = ContextCompat.getColor(ctx, R.color.stock_warning);
        } else {
            color = ContextCompat.getColor(ctx, R.color.stock_success);
        }
        holder.status.setBackgroundColor(color);
        holder.manage.setOnClickListener(v -> {
            if (listener != null) listener.onItem(item);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        final TextView name;
        final TextView sku;
        final TextView stock;
        final TextView threshold;
        final TextView status;
        final MaterialButton manage;

        VH(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.inventory_item_name);
            sku = itemView.findViewById(R.id.inventory_item_sku);
            stock = itemView.findViewById(R.id.inventory_item_stock);
            threshold = itemView.findViewById(R.id.inventory_item_threshold);
            status = itemView.findViewById(R.id.inventory_item_status);
            manage = itemView.findViewById(R.id.inventory_item_manage);
        }
    }
}