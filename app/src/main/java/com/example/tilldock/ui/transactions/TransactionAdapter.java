package com.example.tilldock.ui.transactions;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tilldock.R;
import com.example.tilldock.data.model.Sale;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.Holder> {

    public interface Listener {
        void onTransactionSelected(Sale sale);
    }

    private final List<Sale> items = new ArrayList<>();
    private final Listener listener;
    private final SimpleDateFormat dateFormatter = new SimpleDateFormat("MMM d, h:mm a", Locale.getDefault());

    public TransactionAdapter(Listener listener) {
        this.listener = listener;
    }

    public void submit(List<Sale> data) {
        items.clear();
        if (data != null) items.addAll(data);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_transaction, parent, false);
        return new Holder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        Sale sale = items.get(position);
        holder.title.setText(sale.getTxnNumber() == null || sale.getTxnNumber().isEmpty() ? "â€”" : sale.getTxnNumber());

        String customer = sale.getCustomerName();
        if (customer == null || customer.isEmpty()) {
            customer = holder.itemView.getContext().getString(R.string.transaction_item_customer_walk_in);
        }
        String method = sale.getPaymentMethod() == null ? "" : sale.getPaymentMethod().displayName();
        holder.subtitle.setText(holder.itemView.getContext().getString(R.string.transaction_item_method_format, customer, method));

        Date parsed = parseDate(sale.getCreatedAt());
        holder.meta.setText(parsed == null ? "" : dateFormatter.format(parsed));

        holder.total.setText(formatMoney(sale.getTotal()));
        String status = sale.getStatus() == null ? "Completed" : sale.getStatus().displayName();
        holder.status.setText(status);
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onTransactionSelected(sale);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    private static Date parseDate(String raw) {
        if (raw == null) return null;
        java.text.SimpleDateFormat[] formats = new java.text.SimpleDateFormat[]{
                new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", Locale.US),
                new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.US),
                new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US),
                new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US),
                new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US),
        };
        for (java.text.SimpleDateFormat f : formats) {
            try {
                return f.parse(raw);
            } catch (java.text.ParseException ignored) {
            }
        }
        return null;
    }

    private static String formatMoney(BigDecimal value) {
        if (value == null) value = BigDecimal.ZERO;
        return String.format(Locale.US, "$%s", value.setScale(2, java.math.RoundingMode.HALF_UP).toPlainString());
    }

    static class Holder extends RecyclerView.ViewHolder {
        final TextView title;
        final TextView subtitle;
        final TextView meta;
        final TextView total;
        final TextView status;

        Holder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.txn_row_title);
            subtitle = itemView.findViewById(R.id.txn_row_subtitle);
            meta = itemView.findViewById(R.id.txn_row_meta);
            total = itemView.findViewById(R.id.txn_row_total);
            status = itemView.findViewById(R.id.txn_row_status);
        }
    }
}