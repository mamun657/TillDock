package com.example.tilldock.ui.transactions;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.tilldock.TillDockApplication;

public final class TillDockViewModelProvider {

    private TillDockViewModelProvider() {
    }

    @SuppressWarnings("unchecked")
    public static <T extends ViewModel> T get(Fragment fragment, Class<T> cls) {
        TillDockApplication app = TillDockApplication.get();
        if (cls == TransactionsViewModel.class) {
            return (T) app.transactionsViewModel();
        }
        if (cls == TransactionDetailViewModel.class) {
            return (T) app.transactionDetailViewModel();
        }
        return new ViewModelProvider(fragment).get(cls);
    }
}