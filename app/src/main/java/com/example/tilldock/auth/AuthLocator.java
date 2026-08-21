package com.example.tilldock.auth;

import android.content.Context;

import com.example.tilldock.TillDockApplication;
import com.example.tilldock.data.repository.AuthRepository;
import com.example.tilldock.data.repository.TokenStore;

public final class AuthLocator {

    private AuthLocator() {
    }

    public static AuthSession session(Context context) {
        return app(context).getAuthSession();
    }

    public static AuthRepository repository(Context context) {
        return app(context).getAuthRepository();
    }

    public static TokenStore tokenStore(Context context) {
        return app(context).getTokenStore();
    }

    private static TillDockApplication app(Context context) {
        return (TillDockApplication) context.getApplicationContext();
    }
}
