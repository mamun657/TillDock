package com.example.tilldock.data.repository;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

import java.io.IOException;
import java.security.GeneralSecurityException;

public class TokenStore {

    private static final String PREFS_NAME = "tilldock_secure_prefs";
    private static final String TOKEN_KEY = "auth_token";
    private static final String FALLBACK_PREFS = "tilldock_fallback_prefs";

    private final SharedPreferences prefs;

    public TokenStore(Context context) {
        SharedPreferences resolved;
        try {
            MasterKey masterKey = new MasterKey.Builder(context)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build();
            resolved = EncryptedSharedPreferences.create(
                    context,
                    PREFS_NAME,
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
        } catch (GeneralSecurityException | IOException e) {
            Log.w("TokenStore", "Falling back to plain prefs: " + e.getMessage());
            resolved = context.getSharedPreferences(FALLBACK_PREFS, Context.MODE_PRIVATE);
        }
        this.prefs = resolved;
    }

    public String token() {
        return prefs.getString(TOKEN_KEY, null);
    }

    public String getToken() {
        return token();
    }

    public void saveToken(String token) {
        prefs.edit().putString(TOKEN_KEY, token).apply();
    }

    public void clear() {
        prefs.edit().clear().apply();
    }
}
