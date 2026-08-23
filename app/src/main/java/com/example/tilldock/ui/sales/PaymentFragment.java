package com.example.tilldock.ui.sales;

import android.app.Dialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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
import com.example.tilldock.data.model.PaymentMethod;
import com.example.tilldock.data.model.Sale;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Locale;

public class PaymentFragment extends DialogFragment {

    private final PaymentViewModel viewModel = TillDockApplication.get().paymentViewModel();
    private final NewSaleViewModel saleViewModel = TillDockApplication.get().newSaleViewModel();

    private TextView totalText;
    private TextView changeText;
    private TextInputEditText cashInput;
    private MaterialButton completeButton;
    private MaterialButton[] methodButtons;

    private PaymentMethod selectedMethod = PaymentMethod.CASH;
    private NewSaleViewModel.SubmitListener submitListener;

    public static PaymentFragment newInstance() {
        return new PaymentFragment();
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
        return inflater.inflate(R.layout.fragment_payment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        totalText = view.findViewById(R.id.payment_total_value);
        changeText = view.findViewById(R.id.payment_change_value);
        cashInput = view.findViewById(R.id.payment_cash_input);
        completeButton = view.findViewById(R.id.payment_complete_button);

        MaterialButton cash = view.findViewById(R.id.payment_method_cash);
        MaterialButton card = view.findViewById(R.id.payment_method_card);
        MaterialButton qr = view.findViewById(R.id.payment_method_qr);
        MaterialButton wallet = view.findViewById(R.id.payment_method_wallet);
        MaterialButton bank = view.findViewById(R.id.payment_method_bank);
        MaterialButton other = view.findViewById(R.id.payment_method_other);
        methodButtons = new MaterialButton[]{cash, card, qr, wallet, bank, other};
        PaymentMethod[] methods = new PaymentMethod[]{
                PaymentMethod.CASH, PaymentMethod.CARD, PaymentMethod.QR,
                PaymentMethod.WALLET, PaymentMethod.BANK, PaymentMethod.OTHER
        };
        for (int i = 0; i < methodButtons.length; i++) {
            final int idx = i;
            MaterialButton btn = methodButtons[i];
            btn.setOnClickListener(v -> selectMethod(methods[idx]));
        }

        BigDecimal total = saleViewModel.total();
        totalText.setText(formatMoney(total));
        BigDecimal initialCash = total;
        cashInput.setText(initialCash.setScale(2, RoundingMode.HALF_UP).toPlainString());

        cashInput.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { }
            @Override public void afterTextChanged(Editable s) {
                recomputeChange();
            }
        });

        viewModel.status().observe(getViewLifecycleOwner(), status -> {
            if (status == PaymentViewModel.Status.PROCESSING) {
                completeButton.setEnabled(false);
            } else {
                completeButton.setEnabled(true);
            }
        });
        viewModel.errorMessage().observe(getViewLifecycleOwner(), msg -> {
            if (msg != null && !msg.isEmpty()) {
                Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
            }
        });
        viewModel.completedSale().observe(getViewLifecycleOwner(), this::onCompleted);

        submitListener = new NewSaleViewModel.SubmitListener() {
            @Override
            public void onSubmitSuccess(Sale sale) {
                viewModel.onSaleSuccess(sale);
            }

            @Override
            public void onSubmitError(String message) {
                viewModel.onSaleError(message);
            }
        };
        saleViewModel.setSubmitListener(submitListener);

        selectMethod(PaymentMethod.CASH);
        recomputeChange();
    }

    @Override
    public void onDestroyView() {
        if (submitListener != null) {
            saleViewModel.setSubmitListener(null);
            submitListener = null;
        }
        super.onDestroyView();
    }

    private void selectMethod(PaymentMethod method) {
        selectedMethod = method;
        for (int i = 0; i < methodButtons.length; i++) {
            methodButtons[i].setSelected(false);
        }
        int idx = indexOf(method);
        if (idx >= 0) methodButtons[idx].setSelected(true);
        boolean isCash = method == PaymentMethod.CASH;
        cashInput.setEnabled(isCash);
        if (!isCash) {
            cashInput.setText(formatBig(saleViewModel.total()));
            cashInput.setEnabled(false);
        }
        recomputeChange();
    }

    private int indexOf(PaymentMethod method) {
        PaymentMethod[] arr = new PaymentMethod[]{
                PaymentMethod.CASH, PaymentMethod.CARD, PaymentMethod.QR,
                PaymentMethod.WALLET, PaymentMethod.BANK, PaymentMethod.OTHER
        };
        for (int i = 0; i < arr.length; i++) {
            if (arr[i] == method) return i;
        }
        return -1;
    }

    private void recomputeChange() {
        BigDecimal total = saleViewModel.total();
        BigDecimal cash = parseBig(cashInput);
        BigDecimal change = BigDecimal.ZERO;
        if (selectedMethod == PaymentMethod.CASH) {
            change = cash.subtract(total);
            if (change.signum() < 0) change = BigDecimal.ZERO;
        }
        if (change.signum() == 0) {
            changeText.setText(R.string.payment_change_zero);
        } else {
            changeText.setText(formatMoney(change));
        }
        boolean insufficient = selectedMethod == PaymentMethod.CASH && cash.compareTo(total) < 0;
        completeButton.setEnabled(!insufficient);
    }

    private void onCompleted(Sale sale) {
        if (sale == null) return;
        dismissAllowingStateLoss();
        TransactionSuccessFragment.newInstance(sale).show(getParentFragmentManager(), "sale-success");
    }

    @Nullable
    private BigDecimal parseBig(TextInputEditText input) {
        if (input == null) return BigDecimal.ZERO;
        String raw = input.getText() == null ? "" : input.getText().toString().trim();
        if (raw.isEmpty()) return BigDecimal.ZERO;
        try {
            return new BigDecimal(raw);
        } catch (NumberFormatException e) {
            return BigDecimal.ZERO;
        }
    }

    private String formatBig(BigDecimal value) {
        if (value == null) value = BigDecimal.ZERO;
        return value.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }

    private String formatMoney(BigDecimal value) {
        if (value == null) value = BigDecimal.ZERO;
        return String.format(Locale.US, "$%s", value.setScale(2, RoundingMode.HALF_UP).toPlainString());
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        completeButton.setOnClickListener(v -> {
            BigDecimal total = saleViewModel.total();
            BigDecimal cash = selectedMethod == PaymentMethod.CASH ? parseBig(cashInput) : total;
            if (selectedMethod == PaymentMethod.CASH && cash.compareTo(total) < 0) {
                Toast.makeText(requireContext(), R.string.payment_error_short_cash, Toast.LENGTH_SHORT).show();
                return;
            }
            viewModel.startSubmit(selectedMethod, cash);
        });
    }
}