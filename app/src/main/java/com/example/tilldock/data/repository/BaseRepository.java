package com.example.tilldock.data.repository;

import androidx.annotation.Nullable;

import com.example.tilldock.data.model.ApiErrorBody;
import com.example.tilldock.utils.ApiError;
import com.example.tilldock.utils.Result;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Response;

public abstract class BaseRepository {

    protected static final ExecutorService IO = Executors.newSingleThreadExecutor();
    protected final Gson gson = new Gson();

    protected <T> Result<T> parseOk(Response<T> response) {
        if (response.isSuccessful() && response.body() != null) {
            return Result.success(response.body());
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

    protected <T> Result<T> parseVoid(Response<T> response) {
        if (response.isSuccessful()) {
            return Result.success(null);
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

    protected ApiError parseError(int code, @Nullable String rawBody) {
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
        if (code == 404) {
            return ApiError.unknown(message);
        }
        if (code == 409) {
            return new ApiError(ApiError.Kind.CONFLICT, message, fieldErrors);
        }
        if (code == 412) {
            if (body != null && "business_setup_required".equalsIgnoreCase(body.getCode())) {
                return ApiError.businessSetupRequired(message);
            }
            return ApiError.unknown(message);
        }
        if (code >= 500) {
            return ApiError.server(message);
        }
        return ApiError.unknown(message);
    }
}