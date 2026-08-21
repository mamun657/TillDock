package com.example.tilldock.ui.auth;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.example.tilldock.R;
import com.example.tilldock.ui.BaseFragment;
import com.example.tilldock.ui.Nav;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class SignupFragment extends BaseFragment {

    private SignupViewModel viewModel;

    private TextInputEditText inputFullName;
    private TextInputEditText inputBusinessName;
    private TextInputEditText inputEmail;
    private TextInputEditText inputPhone;
    private TextInputEditText inputPassword;
    private TextInputEditText inputConfirmPassword;
    private TextInputLayout layoutFullName;
    private TextInputLayout layoutBusinessName;
    private TextInputLayout layoutEmail;
    private TextInputLayout layoutPhone;
    private TextInputLayout layoutPassword;
    private TextInputLayout layoutConfirmPassword;
    private TextView errorText;
    private MaterialButton submitButton;
    private MaterialButton haveAccountButton;
    private ProgressBar progress;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AuthViewModelFactory factory = new AuthViewModelFactory(repository());
        viewModel = new ViewModelProvider(this, factory).get(SignupViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_signup, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        inputFullName = view.findViewById(R.id.signup_input_full_name);
        inputBusinessName = view.findViewById(R.id.signup_input_business_name);
        inputEmail = view.findViewById(R.id.signup_input_email);
        inputPhone = view.findViewById(R.id.signup_input_phone);
        inputPassword = view.findViewById(R.id.signup_input_password);
        inputConfirmPassword = view.findViewById(R.id.signup_input_confirm_password);

        layoutFullName = view.findViewById(R.id.signup_input_full_name_layout);
        layoutBusinessName = view.findViewById(R.id.signup_input_business_name_layout);
        layoutEmail = view.findViewById(R.id.signup_input_email_layout);
        layoutPhone = view.findViewById(R.id.signup_input_phone_layout);
        layoutPassword = view.findViewById(R.id.signup_input_password_layout);
        layoutConfirmPassword = view.findViewById(R.id.signup_input_confirm_password_layout);

        errorText = view.findViewById(R.id.signup_error_text);
        submitButton = view.findViewById(R.id.signup_button_submit);
        haveAccountButton = view.findViewById(R.id.signup_button_have_account);
        progress = view.findViewById(R.id.signup_progress);

        submitButton.setOnClickListener(v -> {
            clearFieldErrors();
            viewModel.submit(
                    textOf(inputFullName),
                    textOf(inputBusinessName),
                    textOf(inputEmail),
                    textOf(inputPhone),
                    textOf(inputPassword),
                    textOf(inputConfirmPassword)
            );
        });
        haveAccountButton.setOnClickListener(v -> Nav.showLogin(requireActivity()));

        viewModel.loading().observe(getViewLifecycleOwner(), this::applyLoading);
        viewModel.errorMessage().observe(getViewLifecycleOwner(), this::applyErrorMessage);
        viewModel.fieldErrors().observe(getViewLifecycleOwner(), this::applyFieldErrors);
        viewModel.success().observe(getViewLifecycleOwner(), merchant -> {
            if (merchant != null) {
                session().setSession(merchant);
                Nav.showDashboard(requireActivity());
            }
        });
    }

    private void applyLoading(Boolean isLoading) {
        boolean loading = isLoading != null && isLoading;
        progress.setVisibility(loading ? View.VISIBLE : View.GONE);
        submitButton.setEnabled(!loading);
    }

    private void applyErrorMessage(@Nullable String message) {
        if (message == null || message.isEmpty()) {
            errorText.setVisibility(View.GONE);
            errorText.setText("");
        } else {
            errorText.setVisibility(View.VISIBLE);
            errorText.setText(message);
        }
    }

    private void applyFieldErrors(@Nullable SignupViewModel.FieldErrors fe) {
        clearFieldErrors();
        if (fe == null) return;
        if (fe.fullName != null) layoutFullName.setError(fe.fullName);
        if (fe.businessName != null) layoutBusinessName.setError(fe.businessName);
        if (fe.email != null) layoutEmail.setError(fe.email);
        if (fe.phone != null) layoutPhone.setError(fe.phone);
        if (fe.password != null) layoutPassword.setError(fe.password);
        if (fe.confirmPassword != null) layoutConfirmPassword.setError(fe.confirmPassword);
    }

    private void clearFieldErrors() {
        layoutFullName.setError(null);
        layoutBusinessName.setError(null);
        layoutEmail.setError(null);
        layoutPhone.setError(null);
        layoutPassword.setError(null);
        layoutConfirmPassword.setError(null);
    }

    private String textOf(TextInputEditText editText) {
        CharSequence cs = editText.getText();
        return cs == null ? "" : cs.toString();
    }
}
