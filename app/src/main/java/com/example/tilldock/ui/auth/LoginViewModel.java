package com.example.tilldock.ui.auth;

import androidx.annotation.MainThread;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.tilldock.data.model.LoginRequest;
import com.example.tilldock.data.model.Merchant;
import com.example.tilldock.data.repository.AuthRepository;
import com.example.tilldock.utils.ApiError;
import com.example.tilldock.utils.Validators;

public class LoginViewModel extends ViewModel {

    private final AuthRepository repository;
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>(null);
    private final MutableLiveData<Merchant> success = new MutableLiveData<>(null);
    private final MutableLiveData<Boolean> accountInactive = new MutableLiveData<>(false);

    public LoginViewModel(AuthRepository repository) {
        this.repository = repository;
    }

    public LiveData<Boolean> loading() { return loading; }
    public LiveData<String> errorMessage() { return errorMessage; }
    public LiveData<Merchant> success() { return success; }
    public LiveData<Boolean> accountInactive() { return accountInactive; }

    @MainThread
    public void submit(String email, String password) {
        errorMessage.setValue(null);
        accountInactive.setValue(false);

        if (!Validators.isValidEmail(email)) {
            errorMessage.setValue("Enter a valid email address");
            return;
        }
        if (password == null || password.isEmpty()) {
            errorMessage.setValue("Enter your password");
            return;
        }

        loading.setValue(true);
        LoginRequest request = new LoginRequest(email.trim(), password);
        repository.login(request, new AuthRepository.Callback<Merchant>() {
            @Override
            public void onSuccess(Merchant value) {
                loading.postValue(false);
                success.postValue(value);
            }

            @Override
            public void onFailure(ApiError error) {
                loading.postValue(false);
                if (error.kind() == ApiError.Kind.ACCOUNT_INACTIVE) {
                    accountInactive.postValue(true);
                }
                errorMessage.postValue(error.message());
            }
        });
    }
}
