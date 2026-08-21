package com.example.tilldock.ui.business;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.tilldock.R;
import com.example.tilldock.data.model.Business;
import com.example.tilldock.data.model.BusinessRequest;
import com.example.tilldock.ui.ViewModelFactories;
import com.example.tilldock.utils.ApiError;
import com.example.tilldock.utils.Validators;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;

import java.math.BigDecimal;

public class BusinessProfileFragment extends Fragment {

    private BusinessViewModel viewModel;
    private Business current;

    private ProgressBar progress;
    private TextView errorText;
    private View emptyState;
    private View formCard;
    private MaterialButton createButton;
    private MaterialButton saveButton;
    private MaterialButton deleteButton;

    private TextInputEditText inputName;
    private TextInputEditText inputAddress;
    private TextInputEditText inputPhone;
    private TextInputEditText inputEmail;
    private TextInputEditText inputLogo;
    private TextInputEditText inputCurrency;
    private TextInputEditText inputTax;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this, ViewModelFactories.business()).get(BusinessViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_business_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        progress = view.findViewById(R.id.business_progress);
        errorText = view.findViewById(R.id.business_error_text);
        emptyState = view.findViewById(R.id.business_empty_state);
        formCard = view.findViewById(R.id.business_form_card);
        createButton = view.findViewById(R.id.business_button_create);
        saveButton = view.findViewById(R.id.business_button_save);
        deleteButton = view.findViewById(R.id.business_button_delete);

        inputName = view.findViewById(R.id.business_input_name);
        inputAddress = view.findViewById(R.id.business_input_address);
        inputPhone = view.findViewById(R.id.business_input_phone);
        inputEmail = view.findViewById(R.id.business_input_email);
        inputLogo = view.findViewById(R.id.business_input_logo);
        inputCurrency = view.findViewById(R.id.business_input_currency);
        inputTax = view.findViewById(R.id.business_input_tax);

        createButton.setOnClickListener(v -> {
            current = null;
            clearForm();
            renderForm(null);
        });

        saveButton.setOnClickListener(v -> attemptSave());
        deleteButton.setOnClickListener(v -> confirmDelete());

        viewModel.state().observe(getViewLifecycleOwner(), this::render);
        viewModel.load();
    }

    private void render(BusinessViewModel.State state) {
        if (state == null) return;
        progress.setVisibility(state.status == BusinessViewModel.Status.LOADING ? View.VISIBLE : View.GONE);
        switch (state.status) {
            case IDLE:
                errorText.setVisibility(View.GONE);
                emptyState.setVisibility(View.GONE);
                formCard.setVisibility(View.GONE);
                break;
            case LOADING:
                errorText.setVisibility(View.GONE);
                emptyState.setVisibility(View.GONE);
                formCard.setVisibility(View.GONE);
                break;
            case SUCCESS:
                current = state.business;
                renderForm(state.business);
                break;
            case NOT_FOUND:
                current = null;
                clearForm();
                emptyState.setVisibility(View.VISIBLE);
                formCard.setVisibility(View.GONE);
                errorText.setVisibility(View.GONE);
                break;
            case ERROR:
                errorText.setVisibility(View.VISIBLE);
                errorText.setText(messageOf(state.error));
                break;
        }
    }

    private void renderForm(@Nullable Business business) {
        emptyState.setVisibility(View.GONE);
        formCard.setVisibility(View.VISIBLE);
        boolean editing = business != null;
        deleteButton.setVisibility(editing ? View.VISIBLE : View.GONE);
        if (editing) {
            inputName.setText(business.getBusinessName());
            inputAddress.setText(business.getAddress());
            inputPhone.setText(business.getPhone());
            inputEmail.setText(business.getEmail());
            inputLogo.setText(business.getLogoUrl());
            inputCurrency.setText(business.getCurrency());
            inputTax.setText(business.getTaxRate() == null ? "" : business.getTaxRate().toPlainString());
        }
    }

    private void clearForm() {
        inputName.setText("");
        inputAddress.setText("");
        inputPhone.setText("");
        inputEmail.setText("");
        inputLogo.setText("");
        inputCurrency.setText("BDT");
        inputTax.setText("0");
    }

    private void attemptSave() {
        String name = textOf(inputName);
        String address = textOf(inputAddress);
        String phone = textOf(inputPhone);
        String email = textOf(inputEmail);
        String logo = textOf(inputLogo);
        String currency = textOf(inputCurrency).toUpperCase();
        String tax = textOf(inputTax);

        String firstError = null;
        if (!Validators.isValidBusinessName(name)) firstError = getString(R.string.error_business_name_length);
        else if (TextUtils.isEmpty(address)) firstError = getString(R.string.error_business_address_required);
        else if (!Validators.isValidPhone(phone)) firstError = getString(R.string.error_phone_invalid);
        else if (!Validators.isValidEmail(email)) firstError = getString(R.string.error_email_invalid);
        else if (currency.length() != 3) firstError = getString(R.string.error_business_currency_invalid);

        BigDecimal taxRate = BigDecimal.ZERO;
        if (firstError == null) {
            try {
                taxRate = new BigDecimal(tax);
                if (taxRate.compareTo(BigDecimal.ZERO) < 0 || taxRate.compareTo(new BigDecimal(100)) > 0) {
                    firstError = getString(R.string.error_business_tax_invalid);
                }
            } catch (NumberFormatException ex) {
                firstError = getString(R.string.error_business_tax_invalid);
            }
        }

        if (firstError != null) {
            showError(firstError);
            return;
        }

        BusinessRequest request = new BusinessRequest(name, address, phone, email, logo, currency, taxRate);
        viewModel.save(request);
    }

    private void confirmDelete() {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.business_action_delete)
                .setMessage(R.string.business_deleted)
                .setNegativeButton(R.string.dialog_no, null)
                .setPositiveButton(R.string.dialog_yes, (d, w) -> viewModel.delete())
                .show();
    }

    private void showError(String message) {
        errorText.setVisibility(View.VISIBLE);
        errorText.setText(message);
    }

    private String textOf(TextInputEditText editText) {
        CharSequence cs = editText.getText();
        return cs == null ? "" : cs.toString().trim();
    }

    private String messageOf(ApiError error) {
        if (error == null) return "";
        return error.message() == null ? getString(R.string.error_unknown) : error.message();
    }
}