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

public class LoginFragment extends BaseFragment {

    private LoginViewModel viewModel;
    private TextInputEditText inputEmail;
    private TextInputEditText inputPassword;
    private TextView errorText;
    private MaterialButton submitButton;
    private MaterialButton noAccountButton;
    private ProgressBar progress;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AuthViewModelFactory factory = new AuthViewModelFactory(repository());
        viewModel = new ViewModelProvider(this, factory).get(LoginViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        inputEmail = view.findViewById(R.id.login_input_email);
        inputPassword = view.findViewById(R.id.login_input_password);
        errorText = view.findViewById(R.id.login_error_text);
        submitButton = view.findViewById(R.id.login_button_submit);
        noAccountButton = view.findViewById(R.id.login_button_no_account);
        progress = view.findViewById(R.id.login_progress);

        submitButton.setOnClickListener(v -> viewModel.submit(textOf(inputEmail), textOf(inputPassword)));
        noAccountButton.setOnClickListener(v -> Nav.showSignup(requireActivity()));

        viewModel.loading().observe(getViewLifecycleOwner(), this::applyLoading);
        viewModel.errorMessage().observe(getViewLifecycleOwner(), this::applyErrorMessage);
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

    private String textOf(TextInputEditText editText) {
        CharSequence cs = editText.getText();
        return cs == null ? "" : cs.toString();
    }
}
