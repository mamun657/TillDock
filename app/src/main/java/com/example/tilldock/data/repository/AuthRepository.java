package com.example.tilldock.data.repository;

import androidx.annotation.Nullable;

import com.example.tilldock.data.api.AuthApi;
import com.example.tilldock.data.model.ApiErrorBody;
import com.example.tilldock.data.model.AuthResponse;
import com.example.tilldock.data.model.LoginRequest;
import com.example.tilldock.data.model.Merchant;
import com.example.tilldock.data.model.MerchantResponse;
import com.example.tilldock.data.model.SignupRequest;
import com.example.tilldock.utils.ApiError;
import com.example.tilldock.utils.Result;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Response;

public class AuthRepository {

    private final AuthApi authApi;
    private final TokenStore tokenStore;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Gson gson = new Gson();

    public AuthRepository(AuthApi authApi, TokenStore tokenStore) {
        this.authApi = authApi;
        this.tokenStore = tokenStore;
    }

    public void signup(SignupRequest request, Callback<Merchant> callback) {
        executor.execute(() -> {
            try {
                Response<AuthResponse> response = authApi.signup(request).execute();
                Result<Merchant> result = handleAuthResponse(response);
                deliverResult(result, callback);
            } catch (IOException e) {
                deliverResult(Result.failure(ApiError.network("Unable to reach server")), callback);
            } catch (RuntimeException e) {
                deliverResult(Result.failure(ApiError.unknown("Unexpected error")), callback);
            }
        });
    }

    public void login(LoginRequest request, Callback<Merchant> callback) {
        executor.execute(() -> {
            try {
                Response<AuthResponse> response = authApi.login(request).execute();
                Result<Merchant> result = handleAuthResponse(response);
                deliverResult(result, callback);
            } catch (IOException e) {
                deliverResult(Result.failure(ApiError.network("Unable to reach server")), callback);
            } catch (RuntimeException e) {
                deliverResult(Result.failure(ApiError.unknown("Unexpected error")), callback);
            }
        });
    }

    public void me(Callback<Merchant> callback) {
        executor.execute(() -> {
            try {
                Response<MerchantResponse> response = authApi.me().execute();
                if (response.isSuccessful() && response.body() != null) {
                    deliverResult(Result.success(response.body().toMerchant()), callback);
                } else {
                    ApiError err = parseError(response.code(), response.errorBody() != null ? response.errorBody().string() : null);
                    deliverResult(Result.failure(err), callback);
                }
            } catch (IOException e) {
                deliverResult(Result.failure(ApiError.network("Unable to reach server")), callback);
            } catch (RuntimeException e) {
                deliverResult(Result.failure(ApiError.unknown("Unexpected error")), callback);
            }
        });
    }

    public void logout(Callback<Void> callback) {
        executor.execute(() -> {
            try {
                authApi.logout().execute();
            } catch (IOException ignored) {
            } finally {
                tokenStore.clear();
            }
            deliverResult(Result.success(null), callback);
        });
    }

    private Result<Merchant> handleAuthResponse(Response<AuthResponse> response) {
        if (response.isSuccessful() && response.body() != null) {
            AuthResponse body = response.body();
            if (body.getToken() != null) {
                tokenStore.saveToken(body.getToken());
            }
            return Result.success(body.getMerchant().toMerchant());
        }
        String raw = null;
        try {
            if (response.errorBody() != null) {
                raw = response.errorBody().string();
            }
        } catch (IOException ignored) {
        }
        return Result.failure(parseError(response.code(), raw));
    }

    private ApiError parseError(int code, @Nullable String rawBody) {
        ApiErrorBody body = null;
        if (rawBody != null && !rawBody.isEmpty()) {
            try {
                body = gson.fromJson(rawBody, ApiErrorBody.class);
            } catch (JsonSyntaxException ignored) {
            }
        }
        String message = body != null && body.getMessage() != null ? body.getMessage() : "Request failed";
        Map<String, String> fieldErrors = body != null ? new HashMap<>(body.getFieldErrors()) : new HashMap<>();

        if (code == 400) {
            return new ApiError(ApiError.Kind.VALIDATION, message, fieldErrors);
        }
        if (code == 401) {
            if (message != null && message.toLowerCase().contains("inactive")) {
                return ApiError.accountInactive(message);
            }
            return ApiError.unauthorized(message);
        }
        if (code == 403) {
            if (message != null && message.toLowerCase().contains("inactive")) {
                return ApiError.accountInactive(message);
            }
            return ApiError.unauthorized(message);
        }
        if (code == 409) {
            return new ApiError(ApiError.Kind.CONFLICT, message, fieldErrors);
        }
        if (code >= 500) {
            return ApiError.server(message);
        }
        return ApiError.unknown(message);
    }

    private <T> void deliverResult(Result<T> result, Callback<T> callback) {
        if (callback == null) return;
        if (result.isSuccess()) {
            callback.onSuccess(result.value());
        } else {
            callback.onFailure(result.error());
        }
    }

    public interface Callback<T> {
        void onSuccess(T value);

        void onFailure(ApiError error);
    }
}
