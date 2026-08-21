package com.example.tilldock.ui.business;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.tilldock.data.model.Business;
import com.example.tilldock.data.model.BusinessRequest;
import com.example.tilldock.data.repository.BusinessRepository;
import com.example.tilldock.utils.ApiError;

public class BusinessViewModel extends ViewModel {

    public enum Status {IDLE, LOADING, SUCCESS, NOT_FOUND, ERROR}

    public static class State {
        public final Status status;
        public final Business business;
        public final ApiError error;
        public final String message;

        private State(Status status, Business business, ApiError error, String message) {
            this.status = status;
            this.business = business;
            this.error = error;
            this.message = message;
        }

        public static State loading() {
            return new State(Status.LOADING, null, null, null);
        }

        public static State success(Business b) {
            return new State(Status.SUCCESS, b, null, null);
        }

        public static State notFound(String msg) {
            return new State(Status.NOT_FOUND, null, null, msg);
        }

        public static State error(ApiError err) {
            return new State(Status.ERROR, null, err, null);
        }
    }

    private final BusinessRepository repository;
    private final MutableLiveData<State> state = new MutableLiveData<>(new State(Status.IDLE, null, null, null));

    public BusinessViewModel(BusinessRepository repository) {
        this.repository = repository;
    }

    public LiveData<State> state() {
        return state;
    }

    public void load() {
        state.postValue(State.loading());
        repository.get(new BusinessRepository.Callback<Business>() {
            @Override
            public void onSuccess(Business value) {
                state.postValue(State.success(value));
            }

            @Override
            public void onFailure(ApiError error) {
                if (error.kind() == ApiError.Kind.UNKNOWN && error.message() != null
                        && error.message().toLowerCase().contains("business_not_found")) {
                    state.postValue(State.notFound(error.message()));
                } else {
                    state.postValue(State.error(error));
                }
            }
        });
    }

    public void save(BusinessRequest request) {
        state.postValue(State.loading());
        repository.upsert(request, new BusinessRepository.Callback<Business>() {
            @Override
            public void onSuccess(Business value) {
                state.postValue(State.success(value));
            }

            @Override
            public void onFailure(ApiError error) {
                state.postValue(State.error(error));
            }
        });
    }

    public void delete() {
        state.postValue(State.loading());
        repository.delete(new BusinessRepository.Callback<Void>() {
            @Override
            public void onSuccess(Void value) {
                state.postValue(State.notFound("Business deleted"));
            }

            @Override
            public void onFailure(ApiError error) {
                state.postValue(State.error(error));
            }
        });
    }
}