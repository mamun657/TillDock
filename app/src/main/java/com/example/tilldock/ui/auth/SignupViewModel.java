package com.example.tilldock.ui.auth;

import androidx.annotation.MainThread;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.tilldock.data.model.Merchant;
import com.example.tilldock.data.model.SignupRequest;
import com.example.tilldock.data.repository.AuthRepository;
import com.example.tilldock.utils.ApiError;
import com.example.tilldock.utils.Validators;

public class SignupViewModel extends ViewModel {

    public static final class FieldErrors {
        @Nullable public final String fullName;
        @Nullable public final String businessName;
        @Nullable public final String email;
        @Nullable public final String phone;
        @Nullable public final String password;
        @Nullable public final String confirmPassword;

        public FieldErrors(@Nullable String fullName, @Nullable String businessName, @Nullable String email,
                           @Nullable String phone, @Nullable String password, @Nullable String confirmPassword) {
            this.fullName = fullName;
            this.businessName = businessName;
            this.email = email;
            this.phone = phone;
            this.password = password;
            this.confirmPassword = confirmPassword;
        }

        public boolean isEmpty() {
            return fullName == null && businessName == null && email == null
                    && phone == null && password == null && confirmPassword == null;
        }
    }

    private final AuthRepository repository;
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MutableLiveData<FieldErrors> fieldErrors = new MutableLiveData<>(null);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>(null);
    private final MutableLiveData<Merchant> success = new MutableLiveData<>(null);

    public SignupViewModel(AuthRepository repository) {
        this.repository = repository;
    }

    public LiveData<Boolean> loading() { return loading; }
    public LiveData<FieldErrors> fieldErrors() { return fieldErrors; }
    public LiveData<String> errorMessage() { return errorMessage; }
    public LiveData<Merchant> success() { return success; }

    @MainThread
    public void submit(String fullName, String businessName, String email, String phone,
                       String password, String confirmPassword) {
        fieldErrors.setValue(null);
        errorMessage.setValue(null);

        FieldErrors clientErrors = validate(fullName, businessName, email, phone, password, confirmPassword);
        if (clientErrors != null) {
            fieldErrors.setValue(clientErrors);
            return;
        }

        loading.setValue(true);
        SignupRequest request = new SignupRequest(fullName.trim(), businessName.trim(),
                email.trim(), phone.trim(), password);
        repository.signup(request, new AuthRepository.Callback<Merchant>() {
            @Override
            public void onSuccess(Merchant value) {
                loading.postValue(false);
                success.postValue(value);
            }

            @Override
            public void onFailure(ApiError error) {
                loading.postValue(false);
                if (error.kind() == ApiError.Kind.VALIDATION || error.kind() == ApiError.Kind.CONFLICT) {
                    FieldErrors fe = new FieldErrors(
                            error.fieldError("fullName"),
                            error.fieldError("businessName"),
                            error.fieldError("email"),
                            error.fieldError("phone"),
                            error.fieldError("password"),
                            error.fieldError("confirmPassword"));
                    if (!fe.isEmpty()) {
                        fieldErrors.postValue(fe);
                        return;
                    }
                }
                errorMessage.postValue(error.message());
            }
        });
    }

    @Nullable
    private FieldErrors validate(String fullName, String businessName, String email, String phone,
                                 String password, String confirmPassword) {
        String fn = null, bn = null, em = null, ph = null, pw = null, cp = null;

        if (!Validators.isValidFullName(fullName)) {
            fn = "Enter your full name (2-120 characters)";
        }
        if (!Validators.isValidBusinessName(businessName)) {
            bn = "Enter your business name (2-160 characters)";
        }
        if (!Validators.isValidEmail(email)) {
            em = "Enter a valid email address";
        }
        if (!Validators.isValidPhone(phone)) {
            ph = "Enter a valid phone number";
        }
        if (!Validators.isStrongPassword(password)) {
            pw = "Use 8+ chars with upper, lower, number, symbol";
        }
        if (password == null || !password.equals(confirmPassword)) {
            cp = "Passwords do not match";
        }

        if (fn == null && bn == null && em == null && ph == null && pw == null && cp == null) {
            return null;
        }
        return new FieldErrors(fn, bn, em, ph, pw, cp);
    }
}
