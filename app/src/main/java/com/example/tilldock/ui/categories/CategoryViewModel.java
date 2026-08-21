package com.example.tilldock.ui.categories;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.tilldock.data.model.Category;
import com.example.tilldock.data.model.CategoryRequest;
import com.example.tilldock.data.repository.CategoryRepository;
import com.example.tilldock.utils.ApiError;

import java.util.Collections;
import java.util.List;

public class CategoryViewModel extends ViewModel {

    public enum Status {IDLE, LOADING, SUCCESS, EMPTY, ERROR, SAVED}

    public static class State {
        public final Status status;
        public final List<Category> categories;
        public final Category updated;
        public final ApiError error;
        public final String deletedId;

        private State(Status status, List<Category> categories, Category updated, ApiError error, String deletedId) {
            this.status = status;
            this.categories = categories;
            this.updated = updated;
            this.error = error;
            this.deletedId = deletedId;
        }

        public static State loading() {
            return new State(Status.LOADING, null, null, null, null);
        }

        public static State success(List<Category> items) {
            return new State(items == null || items.isEmpty() ? Status.EMPTY : Status.SUCCESS,
                    items == null ? Collections.emptyList() : items, null, null, null);
        }

        public static State saved(Category updated) {
            return new State(Status.SAVED, null, updated, null, null);
        }

        public static State deleted(String id) {
            return new State(Status.SUCCESS, null, null, null, id);
        }

        public static State error(ApiError err) {
            return new State(Status.ERROR, null, null, err, null);
        }
    }

    private final CategoryRepository repository;
    private final MutableLiveData<State> state = new MutableLiveData<>(new State(Status.IDLE, null, null, null, null));

    public CategoryViewModel(CategoryRepository repository) {
        this.repository = repository;
    }

    public LiveData<State> state() {
        return state;
    }

    public void load() {
        state.postValue(State.loading());
        repository.list(new CategoryRepository.Callback<List<Category>>() {
            @Override
            public void onSuccess(List<Category> value) {
                state.postValue(State.success(value));
            }

            @Override
            public void onFailure(ApiError error) {
                state.postValue(State.error(error));
            }
        });
    }

    public void create(CategoryRequest request) {
        repository.create(request, new CategoryRepository.Callback<Category>() {
            @Override
            public void onSuccess(Category value) {
                state.postValue(State.saved(value));
            }

            @Override
            public void onFailure(ApiError error) {
                state.postValue(State.error(error));
            }
        });
    }

    public void update(String id, CategoryRequest request) {
        repository.update(id, request, new CategoryRepository.Callback<Category>() {
            @Override
            public void onSuccess(Category value) {
                state.postValue(State.saved(value));
            }

            @Override
            public void onFailure(ApiError error) {
                state.postValue(State.error(error));
            }
        });
    }

    public void delete(String id) {
        repository.delete(id, new CategoryRepository.Callback<Void>() {
            @Override
            public void onSuccess(Void value) {
                state.postValue(State.deleted(id));
            }

            @Override
            public void onFailure(ApiError error) {
                state.postValue(State.error(error));
            }
        });
    }
}