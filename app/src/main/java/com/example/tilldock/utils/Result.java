package com.example.tilldock.utils;

public class Result<T> {

    private final T value;
    private final ApiError error;

    private Result(T value, ApiError error) {
        this.value = value;
        this.error = error;
    }

    public static <T> Result<T> success(T value) {
        return new Result<>(value, null);
    }

    public static <T> Result<T> failure(ApiError error) {
        return new Result<>(null, error);
    }

    public boolean isSuccess() {
        return error == null;
    }

    public boolean isFailure() {
        return error != null;
    }

    public T value() {
        return value;
    }

    public ApiError error() {
        return error;
    }
}
