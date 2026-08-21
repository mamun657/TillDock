package com.example.tilldock.auth;

import androidx.annotation.MainThread;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.tilldock.data.model.Merchant;
import com.example.tilldock.data.repository.AuthRepository;
import com.example.tilldock.data.repository.TokenStore;
import com.example.tilldock.utils.ApiError;

public class AuthSession {

    private final TokenStore tokenStore;
    private final AuthRepository repository;
    private final MutableLiveData<Merchant> merchant = new MutableLiveData<>();

    public AuthSession(TokenStore tokenStore, AuthRepository repository) {
        this.tokenStore = tokenStore;
        this.repository = repository;
    }

    public LiveData<Merchant> merchant() {
        return merchant;
    }

    @Nullable
    public Merchant current() {
        return merchant.getValue();
    }

    public boolean hasToken() {
        return tokenStore.getToken() != null;
    }

    @MainThread
    public void bootstrap() {
        if (tokenStore.getToken() == null) {
            merchant.setValue(null);
            return;
        }
        repository.me(new AuthRepository.Callback<Merchant>() {
            @Override
            public void onSuccess(Merchant value) {
                merchant.postValue(value);
            }

            @Override
            public void onFailure(ApiError error) {
                tokenStore.clear();
                merchant.postValue(null);
            }
        });
    }

    @MainThread
    public void refresh() {
        if (tokenStore.getToken() == null) {
            merchant.setValue(null);
            return;
        }
        repository.me(new AuthRepository.Callback<Merchant>() {
            @Override
            public void onSuccess(Merchant value) {
                merchant.postValue(value);
            }

            @Override
            public void onFailure(ApiError error) {
                tokenStore.clear();
                merchant.postValue(null);
            }
        });
    }

    @MainThread
    public void setSession(Merchant value) {
        merchant.setValue(value);
    }

    @MainThread
    public void logout(@Nullable Runnable onDone) {
        repository.logout(new AuthRepository.Callback<Void>() {
            @Override
            public void onSuccess(Void value) {
                merchant.postValue(null);
                if (onDone != null) onDone.run();
            }

            @Override
            public void onFailure(ApiError error) {
                merchant.postValue(null);
                if (onDone != null) onDone.run();
            }
        });
    }

    @MainThread
    public void clear() {
        merchant.setValue(null);
    }
}
