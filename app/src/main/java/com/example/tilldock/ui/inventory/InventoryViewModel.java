package com.example.tilldock.ui.inventory;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.tilldock.data.model.InventoryItem;
import com.example.tilldock.data.model.StockMovement;
import com.example.tilldock.data.repository.InventoryRepository;
import com.example.tilldock.utils.ApiError;

import java.util.Collections;
import java.util.List;

public class InventoryViewModel extends ViewModel {

    public enum Status {IDLE, LOADING, SUCCESS, EMPTY, ERROR, MUTATED}

    public enum Mode {LIST, MUTATION, MOVEMENTS}

    public static class State {
        public final Status status;
        public final List<InventoryItem> items;
        public final List<StockMovement> movements;
        public final Mode mode;
        public final ApiError error;

        private State(Status status, List<InventoryItem> items, List<StockMovement> movements, Mode mode, ApiError error) {
            this.status = status;
            this.items = items;
            this.movements = movements;
            this.mode = mode;
            this.error = error;
        }

        public static State loading(Mode mode) {
            return new State(Status.LOADING, null, null, mode, null);
        }

        public static State success(List<InventoryItem> items) {
            return new State(items == null || items.isEmpty() ? Status.EMPTY : Status.SUCCESS,
                    items == null ? Collections.emptyList() : items, null, Mode.LIST, null);
        }

        public static State mutated(InventoryItem item) {
            return new State(Status.MUTATED, Collections.emptyList(), null, Mode.MUTATION, null);
        }

        public static State movements(List<StockMovement> list) {
            return new State(list == null || list.isEmpty() ? Status.EMPTY : Status.SUCCESS,
                    null, list == null ? Collections.emptyList() : list, Mode.MOVEMENTS, null);
        }

        public static State error(ApiError err) {
            return new State(Status.ERROR, null, null, Mode.LIST, err);
        }

        public static State error(ApiError err, Mode mode) {
            return new State(Status.ERROR, null, null, mode, err);
        }
    }

    private final InventoryRepository repository;
    private final MutableLiveData<State> state = new MutableLiveData<>(new State(Status.IDLE, null, null, Mode.LIST, null));

    public InventoryViewModel(InventoryRepository repository) {
        this.repository = repository;
    }

    public LiveData<State> state() {
        return state;
    }

    public void load() {
        state.postValue(State.loading(Mode.LIST));
        repository.list(new InventoryRepository.Callback<List<InventoryItem>>() {
            @Override
            public void onSuccess(List<InventoryItem> value) {
                state.postValue(State.success(value));
            }

            @Override
            public void onFailure(ApiError error) {
                state.postValue(State.error(error));
            }
        });
    }

    public void stockIn(String productId, int quantity, String reason) {
        state.postValue(State.loading(Mode.MUTATION));
        repository.stockIn(productId, quantity, reason, mutatingCallback());
    }

    public void stockOut(String productId, int quantity, String reason) {
        state.postValue(State.loading(Mode.MUTATION));
        repository.stockOut(productId, quantity, reason, mutatingCallback());
    }

    public void adjust(String productId, int newQuantity, String reason) {
        state.postValue(State.loading(Mode.MUTATION));
        repository.adjust(productId, newQuantity, reason, mutatingCallback());
    }

    public void setThreshold(String productId, int threshold) {
        state.postValue(State.loading(Mode.MUTATION));
        repository.setThreshold(productId, threshold, mutatingCallback());
    }

    public void loadMovements(String productId) {
        state.postValue(State.loading(Mode.MOVEMENTS));
        repository.movements(productId, 0, 100, new InventoryRepository.Callback<List<StockMovement>>() {
            @Override
            public void onSuccess(List<StockMovement> value) {
                state.postValue(State.movements(value));
            }

            @Override
            public void onFailure(ApiError error) {
                state.postValue(State.error(error, Mode.MOVEMENTS));
            }
        });
    }

    private InventoryRepository.Callback<InventoryItem> mutatingCallback() {
        return new InventoryRepository.Callback<InventoryItem>() {
            @Override
            public void onSuccess(InventoryItem value) {
                state.postValue(State.mutated(value));
            }

            @Override
            public void onFailure(ApiError error) {
                state.postValue(State.error(error, Mode.MUTATION));
            }
        };
    }
}