package com.example.tilldock.data.model;

import com.google.gson.annotations.SerializedName;

import java.util.Collections;
import java.util.Map;

public class ApiErrorBody {

    @SerializedName("code")
    private String code;

    @SerializedName("message")
    private String message;

    @SerializedName("fieldErrors")
    private Map<String, String> fieldErrors;

    public ApiErrorBody() {
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Map<String, String> getFieldErrors() {
        return fieldErrors == null ? Collections.emptyMap() : fieldErrors;
    }

    public void setFieldErrors(Map<String, String> fieldErrors) {
        this.fieldErrors = fieldErrors;
    }
}
