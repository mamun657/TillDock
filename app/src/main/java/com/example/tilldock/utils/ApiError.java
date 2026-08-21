package com.example.tilldock.utils;

import java.util.Collections;
import java.util.Map;

public class ApiError {

    public enum Kind {
        VALIDATION,
        UNAUTHORIZED,
        CONFLICT,
        NETWORK,
        SERVER,
        ACCOUNT_INACTIVE,
        BUSINESS_SETUP_REQUIRED,
        UNKNOWN
    }

    private final Kind kind;
    private final String message;
    private final Map<String, String> fieldErrors;

    public ApiError(Kind kind, String message, Map<String, String> fieldErrors) {
        this.kind = kind;
        this.message = message;
        this.fieldErrors = fieldErrors == null ? Collections.emptyMap() : fieldErrors;
    }

    public Kind kind() {
        return kind;
    }

    public String message() {
        return message;
    }

    public Map<String, String> fieldErrors() {
        return fieldErrors;
    }

    public String fieldError(String field) {
        return fieldErrors.get(field);
    }

    public static ApiError network(String message) {
        return new ApiError(Kind.NETWORK, message, Collections.emptyMap());
    }

    public static ApiError server(String message) {
        return new ApiError(Kind.SERVER, message, Collections.emptyMap());
    }

    public static ApiError unauthorized(String message) {
        return new ApiError(Kind.UNAUTHORIZED, message, Collections.emptyMap());
    }

    public static ApiError accountInactive(String message) {
        return new ApiError(Kind.ACCOUNT_INACTIVE, message, Collections.emptyMap());
    }

    public static ApiError businessSetupRequired(String message) {
        return new ApiError(Kind.BUSINESS_SETUP_REQUIRED, message, Collections.emptyMap());
    }

    public static ApiError unknown(String message) {
        return new ApiError(Kind.UNKNOWN, message, Collections.emptyMap());
    }
}
