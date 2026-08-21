package com.example.tilldock.utils;

import android.util.Patterns;

import java.util.regex.Pattern;

public final class Validators {

    private static final Pattern PHONE_PATTERN = Pattern.compile("^[+]?[0-9 ()\\-]{7,20}$");
    private static final Pattern STRONG_PASSWORD = Pattern.compile(
            "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).{8,}$"
    );

    private Validators() {
    }

    public static boolean isValidEmail(String email) {
        return email != null && Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    public static boolean isValidPhone(String phone) {
        return phone != null && PHONE_PATTERN.matcher(phone).matches();
    }

    public static boolean isValidFullName(String name) {
        if (name == null) return false;
        String trimmed = name.trim();
        return trimmed.length() >= 2 && trimmed.length() <= 120;
    }

    public static boolean isValidBusinessName(String name) {
        if (name == null) return false;
        String trimmed = name.trim();
        return trimmed.length() >= 2 && trimmed.length() <= 160;
    }

    public static boolean isStrongPassword(String password) {
        return password != null && STRONG_PASSWORD.matcher(password).matches();
    }
}
