package com.example.tilldock.ui.transactions;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tilldock.R;
import com.example.tilldock.data.model.SaleItem;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class TransactionDetailAdapter extends RecyclerView.Adapter<TransactionDetailAdapter.Holder> {

    private final List<SaleItem> items = new ArrayList<>();

    public void submit(List<SaleItem> data) {
        items.clear();
        if (data != null) items.addAll(data);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_txn_detail_item, parent, false);
        return new Holder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        SaleItem item = items.get(position);
        String name = item.getProductName() == null || item.getProductName().isEmpty() ? "â€”" : item.getProductName();
        int qty = item.getQuantity() == null ? 0 : item.getQuantity();
        BigDecimal unit = item.getUnitPrice() == null ? BigDecimal.ZERO : item.getUnitPrice();
        BigDecimal line = unit.multiply(BigDecimal.valueOf(qty));

        holder.name.setText(name);
        holder.meta.setText(holder.itemView.getContext().getString(
                R.string.transaction_detail_item_meta, qty, formatMoney(unit)));
        holder.total.setText(formatMoney(line));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    private static String formatMoney(BigDecimal value) {
        if (value == null) value = BigDecimal.ZERO;
        return String.format(Locale.US, "$%s", value.setScale(2, RoundingMode.HALF_UP).toPlainString());
    }

    static class Holder extends RecyclerView.ViewHolder {
        final TextView name;
        final TextView meta;
        final TextView total;

        Holder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.txn_detail_item_name);
            meta = itemView.findViewById(R.id.txn_detail_item_meta);
            total = itemView.findViewById(R.id.txn_detail_item_total);
        }
    }
}