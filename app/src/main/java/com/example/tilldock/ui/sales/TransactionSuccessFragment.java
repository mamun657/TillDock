package com.example.tilldock.ui.sales;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.tilldock.R;
import com.example.tilldock.TillDockApplication;
import com.example.tilldock.data.model.Sale;
import com.example.tilldock.ui.transactions.TransactionDetailFragment;
import com.google.android.material.button.MaterialButton;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Locale;

public class TransactionSuccessFragment extends DialogFragment {

    private static final String ARG_TXN = "txn";
    private static final String ARG_TOTAL = "total";
    private static final String ARG_CUSTOMER = "customer";
    private static final String ARG_CASH = "cash";
    private static final String ARG_CHANGE = "change";
    private static final String ARG_METHOD = "method";
    private static final String ARG_SALE_ID = "sale_id";

    public static TransactionSuccessFragment newInstance(Sale sale) {
        TransactionSuccessFragment f = new TransactionSuccessFragment();
        Bundle b = new Bundle();
        b.putString(ARG_SALE_ID, sale.getId());
        b.putString(ARG_TXN, sale.getTxnNumber());
        b.putString(ARG_CUSTOMER, sale.getCustomerName());
        b.putString(ARG_TOTAL, formatMoney(sale.getTotal()));
        b.putString(ARG_CASH, formatMoney(sale.getCashReceived()));
        b.putString(ARG_CHANGE, formatMoney(sale.getChangeGiven()));
        b.putString(ARG_METHOD, sale.getPaymentMethod() == null ? "" : sale.getPaymentMethod().name());
        f.setArguments(b);
        return f;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        setStyle(STYLE_NORMAL, R.style.Theme_TillDock_FullDialog);
        return super.onCreateDialog(savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog d = getDialog();
        if (d != null && d.getWindow() != null) {
            d.getWindow().setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_transaction_success, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Bundle args = getArguments();
        if (args == null) {
            dismissAllowingStateLoss();
            return;
        }

        TextView totalText = view.findViewById(R.id.success_total_value);
        TextView txnText = view.findViewById(R.id.success_txn_value);
        TextView customerText = view.findViewById(R.id.success_customer_value);
        TextView methodText = view.findViewById(R.id.success_method_value);
        TextView cashText = view.findViewById(R.id.success_cash_value);
        TextView changeText = view.findViewById(R.id.success_change_value);

        totalText.setText(args.getString(ARG_TOTAL, "$0.00"));
        txnText.setText(args.getString(ARG_TXN, "—"));
        customerText.setText(args.getString(ARG_CUSTOMER, "Walk-in"));
        methodText.setText(args.getString(ARG_METHOD, ""));
        cashText.setText(args.getString(ARG_CASH, "$0.00"));
        changeText.setText(args.getString(ARG_CHANGE, "$0.00"));

        MaterialButton viewReceipt = view.findViewById(R.id.success_receipt_button);
        MaterialButton newSale = view.findViewById(R.id.success_new_sale_button);

        viewReceipt.setOnClickListener(v -> {
            String id = args.getString(ARG_SALE_ID);
            if (id == null || id.isEmpty()) {
                Toast.makeText(requireContext(), R.string.transaction_detail_not_available, Toast.LENGTH_SHORT).show();
                return;
            }
            dismissAllowingStateLoss();
            try {
                TransactionDetailFragment detail = TransactionDetailFragment.newInstance(id);
                requireActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.main_container, detail)
                        .addToBackStack(null)
                        .commit();
            } catch (Throwable t) {
                Toast.makeText(requireContext(), R.string.transaction_detail_not_available, Toast.LENGTH_SHORT).show();
            }
        });

        newSale.setOnClickListener(v -> {
            TillDockApplication.get().newSaleViewModel().clearCart();
            dismissAllowingStateLoss();
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.main_container, new com.example.tilldock.ui.sales.NewSaleFragment())
                    .commit();
        });
    }

    private static String formatMoney(BigDecimal value) {
        if (value == null) return "$0.00";
        return String.format(Locale.US, "$%s", value.setScale(2, RoundingMode.HALF_UP).toPlainString());
    }
}
