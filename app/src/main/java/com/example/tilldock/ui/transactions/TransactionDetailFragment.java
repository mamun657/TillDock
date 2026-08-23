package com.example.tilldock.ui.transactions;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tilldock.R;
import com.example.tilldock.data.model.Sale;
import com.example.tilldock.data.model.SaleItem;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TransactionDetailFragment extends Fragment {

    private static final String ARG_SALE_ID = "sale_id";

    public static TransactionDetailFragment newInstance(String saleId) {
        TransactionDetailFragment fragment = new TransactionDetailFragment();
        Bundle args = new Bundle();
        args.putString(ARG_SALE_ID, saleId);
        fragment.setArguments(args);
        return fragment;
    }

    private TransactionDetailViewModel viewModel;
    private TransactionDetailAdapter adapter;

    private TextView txnNumber;
    private TextView dateText;
    private TextView statusChip;
    private TextView subtotal;
    private TextView tax;
    private TextView total;
    private TextView paymentMethod;
    private TextView paymentRef;
    private ProgressBar progress;
    private MaterialButton back;

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d, yyyy · h:mm a", Locale.getDefault());
    private final SimpleDateFormat[] inputFormats = new SimpleDateFormat[]{
            new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", Locale.US),
            new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.US),
            new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US),
            new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US),
            new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US),
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_transaction_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = TillDockViewModelProvider.get(this, TransactionDetailViewModel.class);

        MaterialToolbar toolbar = view.findViewById(R.id.detail_toolbar);
        toolbar.setNavigationOnClickListener(v -> requireActivity().onBackPressed());

        RecyclerView items = view.findViewById(R.id.detail_items_recycler);
        adapter = new TransactionDetailAdapter();
        items.setLayoutManager(new LinearLayoutManager(requireContext()));
        items.setAdapter(adapter);

        txnNumber = view.findViewById(R.id.detail_txn_number);
        dateText = view.findViewById(R.id.detail_date);
        statusChip = view.findViewById(R.id.detail_status_chip);
        subtotal = view.findViewById(R.id.detail_subtotal);
        tax = view.findViewById(R.id.detail_tax);
        total = view.findViewById(R.id.detail_total);
        paymentMethod = view.findViewById(R.id.detail_payment_method);
        paymentRef = view.findViewById(R.id.detail_payment_ref);
        progress = view.findViewById(R.id.detail_progress);
        back = view.findViewById(R.id.detail_btn_back);

        back.setOnClickListener(v -> requireActivity().onBackPressed());

        viewModel.status().observe(getViewLifecycleOwner(), s -> {
            progress.setVisibility(s == TransactionDetailViewModel.Status.LOADING ? View.VISIBLE : View.GONE);
        });
        viewModel.sale().observe(getViewLifecycleOwner(), this::bind);
        viewModel.errorMessage().observe(getViewLifecycleOwner(), msg -> {
            if (msg != null && !msg.isEmpty()) {
                paymentRef.setText(msg);
            }
        });

        Bundle args = getArguments();
        String saleId = args == null ? null : args.getString(ARG_SALE_ID);
        viewModel.load(saleId);
    }

    private void bind(Sale sale) {
        if (sale == null) {
            adapter.submit(Collections.emptyList());
            return;
        }
        txnNumber.setText(sale.getTxnNumber() == null || sale.getTxnNumber().isEmpty() ? "—" : sale.getTxnNumber());
        Date parsed = parseDate(sale.getCreatedAt());
        dateText.setText(parsed == null ? "" : dateFormat.format(parsed));
        statusChip.setText(sale.getStatus() == null ? "Completed" : sale.getStatus().displayName());

        List<SaleItem> lineItems = sale.getItems();
        adapter.submit(lineItems);

        BigDecimal sub = sale.getSubtotal() == null ? BigDecimal.ZERO : sale.getSubtotal();
        BigDecimal tx = sale.getTax() == null ? BigDecimal.ZERO : sale.getTax();
        BigDecimal tot = sale.getTotal() == null ? sub.add(tx) : sale.getTotal();

        subtotal.setText(formatMoney(sub));
        tax.setText(formatMoney(tx));
        total.setText(formatMoney(tot));

        paymentMethod.setText(sale.getPaymentMethod() == null ? "" : sale.getPaymentMethod().displayName());
        if (sale.getPaymentRef() != null && !sale.getPaymentRef().isEmpty()) {
            paymentRef.setVisibility(View.VISIBLE);
            paymentRef.setText(getString(R.string.transaction_detail_payment_ref_format, sale.getPaymentRef()));
        } else {
            paymentRef.setVisibility(View.GONE);
        }
    }

    private Date parseDate(String raw) {
        if (raw == null || raw.isEmpty()) return null;
        for (SimpleDateFormat f : inputFormats) {
            try {
                return f.parse(raw);
            } catch (java.text.ParseException ignored) {
            }
        }
        return null;
    }

    private static String formatMoney(BigDecimal value) {
        if (value == null) value = BigDecimal.ZERO;
        return String.format(Locale.US, "$%s", value.setScale(2, RoundingMode.HALF_UP).toPlainString());
    }
}