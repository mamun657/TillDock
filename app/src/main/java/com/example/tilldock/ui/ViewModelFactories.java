package com.example.tilldock.ui;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.tilldock.TillDockApplication;
import com.example.tilldock.ui.business.BusinessViewModel;
import com.example.tilldock.ui.categories.CategoryViewModel;
import com.example.tilldock.ui.inventory.InventoryViewModel;
import com.example.tilldock.ui.products.ProductViewModel;

public final class ViewModelFactories {

    private ViewModelFactories() {
    }

    public static ViewModelProvider.Factory business() {
        return new ViewModelProvider.Factory() {
            @NonNull
            @Override
            @SuppressWarnings("unchecked")
            public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
                if (modelClass.isAssignableFrom(BusinessViewModel.class)) {
                    return (T) new BusinessViewModel(TillDockApplication.get().getBusinessRepository());
                }
                throw new IllegalArgumentException("Unknown ViewModel: " + modelClass.getName());
            }
        };
    }

    public static ViewModelProvider.Factory categories() {
        return new ViewModelProvider.Factory() {
            @NonNull
            @Override
            @SuppressWarnings("unchecked")
            public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
                if (modelClass.isAssignableFrom(CategoryViewModel.class)) {
                    return (T) new CategoryViewModel(TillDockApplication.get().getCategoryRepository());
                }
                throw new IllegalArgumentException("Unknown ViewModel: " + modelClass.getName());
            }
        };
    }

    public static ViewModelProvider.Factory products() {
        return new ViewModelProvider.Factory() {
            @NonNull
            @Override
            @SuppressWarnings("unchecked")
            public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
                if (modelClass.isAssignableFrom(ProductViewModel.class)) {
                    return (T) new ProductViewModel();
                }
                throw new IllegalArgumentException("Unknown ViewModel: " + modelClass.getName());
            }
        };
    }

    public static ViewModelProvider.Factory inventory() {
        return new ViewModelProvider.Factory() {
            @NonNull
            @Override
            @SuppressWarnings("unchecked")
            public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
                if (modelClass.isAssignableFrom(InventoryViewModel.class)) {
                    return (T) new InventoryViewModel(
                            TillDockApplication.get().getInventoryRepository());
                }
                throw new IllegalArgumentException("Unknown ViewModel: " + modelClass.getName());
            }
        };
    }
}