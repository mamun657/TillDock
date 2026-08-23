package com.example.tilldock.ui.reports;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tilldock.R;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class TopProductAdapter extends RecyclerView.Adapter<TopProductAdapter.Holder> {

    private final List<ReportsViewModel.ProductRow> items = new ArrayList<>();

    public void submit(List<ReportsViewModel.ProductRow> data) {
        items.clear();
        if (data != null) items.addAll(data);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_top_product, parent, false);
        return new Holder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        ReportsViewModel.ProductRow row = items.get(position);
        holder.rank.setText(String.format(Locale.US, "%d", position + 1));
        holder.name.setText(row.name);
        holder.units.setText(holder.itemView.getContext().getString(R.string.reports_top_product_units, row.units));
        holder.revenue.setText(formatMoney(row.revenue));
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
        final TextView rank;
        final TextView name;
        final TextView units;
        final TextView revenue;

        Holder(@NonNull View itemView) {
            super(itemView);
            rank = itemView.findViewById(R.id.top_product_rank);
            name = itemView.findViewById(R.id.top_product_name);
            units = itemView.findViewById(R.id.top_product_units);
            revenue = itemView.findViewById(R.id.top_product_revenue);
        }
    }
}