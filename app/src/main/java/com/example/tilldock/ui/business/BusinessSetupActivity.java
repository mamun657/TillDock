package com.example.tilldock.ui.business;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.MutableLiveData;

import com.example.tilldock.R;
import com.example.tilldock.TillDockApplication;
import com.example.tilldock.data.model.Business;
import com.example.tilldock.data.model.BusinessRequest;
import com.example.tilldock.data.repository.BusinessRepository;
import com.example.tilldock.utils.ApiError;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class BusinessSetupActivity extends AppCompatActivity {

    public enum Status {IDLE, LOADING, SUCCESS, ERROR}

    private BusinessRepository repository;
    private final MutableLiveData<Status> statusHolder = new MutableLiveData<>(Status.IDLE);
    private final MutableLiveData<ApiError> errorHolder = new MutableLiveData<>();
    private TextInputLayout nameLayout;
    private TextInputLayout currencyLayout;
    private TextInputEditText nameInput;
    private TextInputEditText currencyInput;
    private TextView errorText;
    private ProgressBar progress;
    private MaterialButton saveButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_business_setup);

        nameLayout = findViewById(R.id.business_setup_name_layout);
        currencyLayout = findViewById(R.id.business_setup_currency_layout);
        nameInput = findViewById(R.id.business_setup_input_name);
        currencyInput = findViewById(R.id.business_setup_input_currency);
        errorText = findViewById(R.id.business_setup_error_text);
        progress = findViewById(R.id.business_setup_progress);
        saveButton = findViewById(R.id.business_setup_save_button);

        repository = TillDockApplication.get().getBusinessRepository();
        statusHolder.observe(this, this::renderStatus);
        errorHolder.observe(this, this::renderError);

        saveButton.setOnClickListener(v -> attemptSave());
    }

    private void attemptSave() {
        String name = nameInput.getText() == null ? "" : nameInput.getText().toString().trim();
        String currency = currencyInput.getText() == null ? "" : currencyInput.getText().toString().trim().toUpperCase();
        if (TextUtils.isEmpty(name)) {
            nameLayout.setError(getString(R.string.business_setup_name_required));
            return;
        }
        if (currency.length() != 3) {
            currencyLayout.setError(getString(R.string.business_setup_currency_invalid));
            return;
        }
        BusinessRequest request = new BusinessRequest();
        request.setBusinessName(name);
        request.setCurrency(currency);

        statusHolder.postValue(Status.LOADING);
        errorHolder.postValue(null);
        repository.upsert(request, new BusinessRepository.Callback<Business>() {
            @Override
            public void onSuccess(Business result) {
                statusHolder.postValue(Status.SUCCESS);
                Toast.makeText(BusinessSetupActivity.this, R.string.business_setup_saved, Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            }

            @Override
            public void onFailure(ApiError err) {
                statusHolder.postValue(Status.ERROR);
                errorHolder.postValue(err);
            }
        });
    }

    private void renderStatus(Status status) {
        boolean loading = status == Status.LOADING;
        progress.setVisibility(loading ? View.VISIBLE : View.GONE);
        saveButton.setEnabled(!loading);
    }

    private void renderError(ApiError error) {
        if (error == null) {
            errorText.setVisibility(View.GONE);
            return;
        }
        errorText.setVisibility(View.VISIBLE);
        errorText.setText(error.message());
    }
}